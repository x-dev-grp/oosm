const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

async function main() {
  const client = new Client({
    host: 'localhost',
    database: 'osm',
    user: 'postgres',
    password: 'root'
  });
  await client.connect();

  const sqlPath = path.join(__dirname, 'migrate-osmuser-to-oosmuser.sql');
  await client.query(fs.readFileSync(sqlPath, 'utf8'));

  for (const table of ['oosmuser', 'osmuser']) {
    try {
      const count = await client.query(`SELECT COUNT(*)::int AS n FROM ${table}`);
      const users = await client.query(
        `SELECT username, enabled, is_locked, is_new_user FROM ${table} ORDER BY username`
      );
      console.log(`${table}: ${count.rows[0].n} rows`);
      console.log(JSON.stringify(users.rows, null, 2));
    } catch (err) {
      console.log(`${table}: ${err.message}`);
    }
  }

  await client.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
