const fs = require('fs');
const { Client } = require('pg');

async function main() {
  const client = new Client({ connectionString: process.argv[2] });
  await client.connect();

  for (const table of ['oosmuser', 'osmuser']) {
    try {
      const count = await client.query(`SELECT COUNT(*)::int AS n FROM ${table}`);
      console.log(`${table} rows:`, count.rows[0].n);
    } catch (err) {
      console.log(`${table}:`, err.message);
    }
  }

  const sqlPath = process.argv[3] || 'migrate-osmuser-to-oosmuser.sql';
  const sql = fs.readFileSync(sqlPath, 'utf8');
  await client.query(sql);
  console.log('Migration applied.');

  for (const table of ['oosmuser', 'osmuser']) {
    try {
      const count = await client.query(`SELECT COUNT(*)::int AS n FROM ${table}`);
      console.log(`${table} rows after:`, count.rows[0].n);
    } catch (err) {
      console.log(`${table} after:`, err.message);
    }
  }

  const user = await client.query(
    `SELECT username, enabled, is_locked, is_new_user FROM oosmuser WHERE LOWER(username)=LOWER($1)`,
    ['wajdibh']
  );
  console.log('wajdibh:', JSON.stringify(user.rows));

  await client.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
