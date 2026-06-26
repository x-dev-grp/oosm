const { Client } = require('pg');

async function main() {
  const client = new Client({ connectionString: process.argv[2] });
  await client.connect();

  const inserted = await client.query(`
    INSERT INTO oosmuser
    SELECT o.*
    FROM osmuser o
    WHERE NOT EXISTS (
      SELECT 1 FROM oosmuser n WHERE LOWER(n.username) = LOWER(o.username)
    )
  `);
  console.log('Inserted rows:', inserted.rowCount);

  const users = await client.query('SELECT username, enabled, is_locked FROM oosmuser ORDER BY username');
  console.log('oosmuser:', JSON.stringify(users.rows, null, 2));

  await client.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
