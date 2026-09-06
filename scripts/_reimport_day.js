const fs = require('fs');
const path = require('path');

const API = process.env.OOSM_API || 'http://localhost:8084';
const USER = process.env.OOSM_USER || 'sam';
const PASS = process.env.OOSM_PASS || 'oosmAdmin123+';
const FILE =
  process.env.DAY_IMPORT_FILE ||
  'c:\\Users\\Smail\\Downloads\\oosm-day-import-2026-09-05-FULL.xlsx';

async function login() {
  const body = new URLSearchParams({
    grant_type: 'TOKEN',
    client_id: 'oosm-client',
    username: USER,
    password: PASS
  });
  const res = await fetch(`${API}/oauth2/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`login ${res.status}: ${text}`);
  const json = JSON.parse(text);
  if (!json.access_token) throw new Error('no access_token: ' + text);
  return json.access_token;
}

function decodeTenant(token) {
  try {
    const payload = JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString('utf8'));
    return payload.tenantId || payload.tenant_id || null;
  } catch {
    return null;
  }
}

async function postImport(token, endpoint) {
  const buf = fs.readFileSync(FILE);
  const form = new FormData();
  form.append('file', new Blob([buf], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }), path.basename(FILE));
  const res = await fetch(`${API}/api/production/import/day/${endpoint}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: form
  });
  const text = await res.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch {
    throw new Error(`${endpoint} ${res.status}: ${text.slice(0, 500)}`);
  }
  return json;
}

function summarize(report) {
  if (!report) return {};
  const rows = report.rows || report.rowResults || [];
  const by = {};
  for (const r of rows) {
    const s = r.status || r.rowStatus || '?';
    by[s] = (by[s] || 0) + 1;
  }
  return {
    businessDate: report.businessDate,
    canCommit: report.canCommit,
    totals: by,
    errors: rows
      .filter((r) => String(r.status || r.rowStatus || '').includes('ERROR') || (r.errors && r.errors.length))
      .slice(0, 15)
      .map((r) => ({
        sheet: r.sheet,
        ref: r.externalRef || r.ref || r.rowNumber,
        status: r.status || r.rowStatus,
        msg: (r.errors && r.errors.map((e) => e.message || e).join('; ')) || r.message
      }))
  };
}

(async () => {
  console.log('API', API, 'user', USER, 'file', FILE);
  if (!fs.existsSync(FILE)) throw new Error('missing file ' + FILE);
  const token = await login();
  console.log('JWT tenantId', decodeTenant(token));
  fs.writeFileSync('F:/oosm/scripts/_day_import_token.txt', token);

  const dry = await postImport(token, 'dry-run');
  fs.writeFileSync('F:/oosm/scripts/_day_import_dry_run.json', JSON.stringify(dry, null, 2));
  console.log('DRY', dry.success, dry.message);
  console.log(JSON.stringify(summarize(dry.data), null, 2));
  if (!dry.success || (dry.data && dry.data.canCommit === false)) {
    process.exitCode = 2;
    return;
  }

  const commit = await postImport(token, 'commit');
  fs.writeFileSync('F:/oosm/scripts/_day_import_commit.json', JSON.stringify(commit, null, 2));
  console.log('COMMIT', commit.success, commit.message);
  console.log(JSON.stringify(summarize(commit.data), null, 2));
  if (!commit.success) process.exitCode = 3;
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
