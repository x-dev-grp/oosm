const { Client } = require('pg');
const fs = require('fs');

async function main() {
  const args = process.argv.slice(2).filter((a) => a !== '--also-null');
  const alsoNull = process.argv.includes('--also-null');
  const tenantArg = args[0];
  const tenantId = tenantArg || fs.readFileSync('F:/oosm/scripts/_tenant_id.txt', 'utf8').trim();
  if (!/^[0-9a-fA-F-]{36}$/.test(tenantId)) {
    throw new Error('Invalid tenant id: ' + tenantId);
  }

  const c = new Client({
    host: process.env.PGHOST || 'localhost',
    port: Number(process.env.PGPORT || 5432),
    database: process.env.PGDATABASE || 'osm',
    user: process.env.PGUSER || 'postgres',
    password: process.env.PGPASSWORD || 'root'
  });
  await c.connect();

  const company = await c.query('SELECT id, legal_name FROM company_profile WHERE id = $1', [tenantId]);
  if (!company.rowCount) throw new Error('No company_profile for ' + tenantId);
  console.log('Cleaning tenant', company.rows[0].id, company.rows[0].legal_name);
  if (alsoNull) console.log('Also wiping rows with tenant_id IS NULL');

  const tenantPredicate = alsoNull
    ? 'tenant_id = $1 OR tenant_id IS NULL'
    : 'tenant_id = $1';

  const keep = new Set([
    'company_profile',
    'oosmuser',
    'tenant_enabled_module',
    'oauth2_registered_client',
    'oauth2_authorization',
    'oauth2_authorization_consent',
    'role',
    'permission',
    'roles_permissions',
    'app_setting',
    'app_setting_audit',
    'flyway_schema_history',
    'flyway_schema_history_finance',
    'flyway_schema_history_hr',
    'flyway_schema_history_production',
    'flyway_schema_history_security',
    'flyway_schema_history_shared',
    'parameter'
  ]);

  async function tryQuery(sql, params) {
    await c.query('SAVEPOINT sp_try');
    try {
      const res = await c.query(sql, params);
      await c.query('RELEASE SAVEPOINT sp_try');
      return { ok: true, rowCount: res.rowCount || 0 };
    } catch (e) {
      await c.query('ROLLBACK TO SAVEPOINT sp_try');
      return { ok: false, error: e };
    }
  }

  const tablesRes = await c.query(`
    SELECT c.table_name
    FROM information_schema.columns c
    JOIN information_schema.tables t
      ON t.table_schema = c.table_schema AND t.table_name = c.table_name
    WHERE c.table_schema = 'public'
      AND c.column_name = 'tenant_id'
      AND t.table_type = 'BASE TABLE'
    ORDER BY c.table_name`);

  let remaining = tablesRes.rows.map((r) => r.table_name).filter((n) => !keep.has(String(n).toLowerCase()));
  console.log('Target tables', remaining.length);

  await c.query('BEGIN');
  let total = 0;
  const maxRounds = Math.max(8, remaining.length + 2);

  for (let round = 0; round < maxRounds && remaining.length; round++) {
    const next = [];
    let progress = 0;
    for (const table of remaining) {
      const del = await tryQuery(`DELETE FROM "${table}" WHERE ${tenantPredicate}`, [tenantId]);
      if (del.ok) {
        total += del.rowCount;
        progress++;
        if (del.rowCount) console.log(`[t ${round}] ${table}: ${del.rowCount}`);
      } else {
        next.push(table);
      }
    }
    if (!progress) break;
    remaining = next;
  }

  // FK-break pass for leftovers
  for (let round = 0; round < maxRounds && remaining.length; round++) {
    const next = [];
    let progress = 0;
    for (const table of remaining) {
      const refs = await c.query(
        `SELECT tc.table_name AS referencing_table, kcu.column_name AS referencing_column, ccu.column_name AS referenced_column
         FROM information_schema.table_constraints tc
         JOIN information_schema.key_column_usage kcu
           ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
         JOIN information_schema.constraint_column_usage ccu
           ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema
         WHERE tc.constraint_type = 'FOREIGN KEY'
           AND tc.table_schema = 'public'
           AND ccu.table_name = $1`,
        [table]
      );
      for (const ref of refs.rows) {
        if (keep.has(String(ref.referencing_table).toLowerCase())) continue;
        const del = await tryQuery(
          `DELETE FROM "${ref.referencing_table}"
           WHERE "${ref.referencing_column}" IN (
             SELECT "${ref.referenced_column}" FROM "${table}" WHERE ${tenantPredicate}
           )`,
          [tenantId]
        );
        if (del.ok) {
          total += del.rowCount;
          if (del.rowCount) {
            console.log(`[fk] ${ref.referencing_table} via ${table}.${ref.referencing_column}: ${del.rowCount}`);
          }
        }
      }
      const del = await tryQuery(`DELETE FROM "${table}" WHERE ${tenantPredicate}`, [tenantId]);
      if (del.ok) {
        total += del.rowCount;
        progress++;
        console.log(`[fk ${round}] ${table}: ${del.rowCount}`);
      } else {
        next.push(table);
      }
    }
    if (!progress && next.length) {
      await c.query('ROLLBACK');
      throw new Error('FK blockers remain: ' + next.join(','));
    }
    remaining = next;
  }

  if (remaining.length) {
    await c.query('ROLLBACK');
    throw new Error('Leftover tables: ' + remaining.join(','));
  }

  const oauth = await tryQuery(
    `DELETE FROM oauth2_authorization
     WHERE principal_name IN (
       SELECT username FROM oosmuser
       WHERE (tenant_id = $1 OR ($2 AND tenant_id IS NULL)) AND username IS NOT NULL
     )`,
    [tenantId, alsoNull]
  );
  if (oauth.ok) console.log('oauth sessions', oauth.rowCount);
  else console.log('oauth skip', oauth.error && oauth.error.message);

  await c.query('COMMIT');
  console.log('Done. Deleted rows ≈', total);
  await c.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
