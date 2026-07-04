const { Client } = require('pg');

async function main() {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();

  for (const table of ['osmuser', 'oosmuser']) {
    const cols = await c.query(
      `SELECT column_name, data_type, udt_name
       FROM information_schema.columns
       WHERE table_schema='public' AND table_name=$1
       ORDER BY ordinal_position`,
      [table]
    );
    console.log(`\n=== ${table} ===`);
    console.log(cols.rows.map((r) => `${r.column_name}: ${r.data_type} (${r.udt_name})`).join('\n'));
  }

  await c.end();
}

main();
