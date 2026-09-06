const { Client } = require('pg');
(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const r = await c.query(`
    SELECT username, tenant_id, is_deleted, enabled, is_locked
    FROM oosmuser
    WHERE username ILIKE '%sam%' OR username ILIKE '%admin%' OR tenant_id='ba25db13-13e3-42b9-a965-06e5c9a0f31f'::uuid
    ORDER BY username`);
  console.log(r.rows);
  await c.end();
})().catch((e) => { console.error(e); process.exit(1); });
