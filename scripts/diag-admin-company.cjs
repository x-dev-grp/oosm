const { Client } = require('pg');

async function main() {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const tables = await c.query(
    `SELECT table_name FROM information_schema.tables
     WHERE table_schema='public' AND table_name LIKE '%user%' ORDER BY 1`
  );
  console.log('tables:', tables.rows.map((r) => r.table_name));
  const admin = await c.query(
    `SELECT u.username, u.tenant_id, r.role_name, cp.legal_name, cp.is_active
     FROM oosmuser u
     LEFT JOIN role r ON r.id = u.role_id
     LEFT JOIN company_profile cp ON cp.id = u.tenant_id
     WHERE LOWER(u.username)='oosmadmin'`
  );
  console.log('admin:', admin.rows[0]);
  await c.query(`UPDATE role SET role_name='OOSMADMIN' WHERE role_name='OSMADMIN'`);
  await c.end();
}

main();
