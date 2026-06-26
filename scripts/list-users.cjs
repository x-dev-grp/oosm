const { Client } = require('pg');

async function main() {
  const client = new Client({ connectionString: process.argv[2] });
  await client.connect();

  const osm = await client.query(
    `SELECT id, username, email, tenant_id FROM osmuser ORDER BY username`
  );
  const oosm = await client.query(
    `SELECT id, username, email, tenant_id FROM oosmuser ORDER BY username`
  );
  console.log('osmuser:', JSON.stringify(osm.rows, null, 2));
  console.log('oosmuser:', JSON.stringify(oosm.rows, null, 2));

  await client.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
