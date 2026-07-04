const { Client } = require('pg');
const bcrypt = require('bcryptjs');

async function main() {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const r = await c.query(
    `SELECT username, password, enabled, is_deleted, is_locked, is_new_user, role_id
     FROM oosmuser WHERE LOWER(username) = 'oosmadmin'`
  );
  const row = r.rows[0];
  console.log('row:', row);
  console.log('bcrypt match osmAdmin123:', bcrypt.compareSync('osmAdmin123', row.password));
  const role = await c.query('SELECT role_name FROM role WHERE id = $1', [row.role_id]);
  console.log('role:', role.rows[0]?.role_name);
  await c.end();
}

main();
