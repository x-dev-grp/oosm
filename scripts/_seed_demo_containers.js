const { Client } = require('pg');
const { randomUUID } = require('crypto');

const TENANT = 'ba25db13-13e3-42b9-a965-06e5c9a0f31f';

(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const cols = await c.query(
    `SELECT column_name FROM information_schema.columns WHERE table_name='oil_container' ORDER BY ordinal_position`
  );
  console.log(cols.rows.map((r) => r.column_name).join(', '));

  const now = new Date();
  for (const row of [
    { name: 'BIN-5L-DEMO', cap: 5, stock: 200, buy: 2, sell: 6 },
    { name: 'BIN-10L-DEMO', cap: 10, stock: 100, buy: 3, sell: 10 }
  ]) {
    const id = randomUUID();
    await c.query(
      `INSERT INTO oil_container (
         id, name, description, capacity_in_liters, stock_quantity,
         buy_price, selling_price, active, tenant_id,
         created_date, last_modified_date, is_deleted
       ) VALUES ($1,$2,$2,$3,$4,$5,$6,true,$7,$8,$8,false)`,
      [id, row.name, row.cap, row.stock, row.buy, row.sell, TENANT, now]
    );
    console.log('seeded', row.name, id);
  }
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
