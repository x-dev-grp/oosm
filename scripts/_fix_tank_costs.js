const { Client } = require('pg');
(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const r = await c.query(
    `UPDATE storage_unit
     SET avg_cost = COALESCE(avg_cost, 0),
         total_cost = COALESCE(total_cost, 0),
         current_volume = COALESCE(current_volume, 0),
         max_capacity = COALESCE(max_capacity, 5000)
     WHERE name = 'Cuve-Demo-Today'
     RETURNING id::text, name, current_volume, avg_cost, total_cost, tenant_id::text`
  );
  console.log(r.rows);
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
