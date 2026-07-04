const fs = require('fs');
const path = require('path');
const { Client } = require('pg');

async function main() {
  const client = new Client({ connectionString: process.argv[2] });
  await client.connect();

    const sqlPath = process.argv[3] || path.join(__dirname, 'migrate-osmuser-to-oosmuser.sql');
    const sql = fs.readFileSync(sqlPath, 'utf8');
    await client.query(sql);

    for (const table of ['oosmuser', 'osmuser']) {
        try {
            const count = await client.query(`SELECT COUNT(*)::int AS n
                                              FROM ${table}`);
            console.log(`${table} rows:`, count.rows[0].n);
            const users = await client.query(
                `SELECT username, enabled, is_locked
                 FROM ${table}
                 ORDER BY username`
            );
            console.log(`${table} users:`, JSON.stringify(users.rows, null, 2));
        } catch (err) {
            console.log(`${table}:`, err.message);
        }
    }

  await client.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
