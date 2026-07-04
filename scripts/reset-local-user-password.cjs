const { Client } = require('pg');
const bcrypt = require('bcryptjs');

async function main() {
  const password = process.argv[2] || 'osmAdmin123';
  const username = process.argv[3] || 'oosmAdmin';
  const hash = bcrypt.hashSync(password, 10);

  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  await c.query(
    `UPDATE oosmuser
     SET password = $1, enabled = true, is_locked = false, is_new_user = false
     WHERE LOWER(username) = LOWER($2)`,
    [hash, username]
  );
  const user = await c.query(
    `SELECT username, enabled, is_locked, is_new_user FROM oosmuser WHERE LOWER(username) = LOWER($1)`,
    [username]
  );
  console.log('Updated:', user.rows[0]);
  await c.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
