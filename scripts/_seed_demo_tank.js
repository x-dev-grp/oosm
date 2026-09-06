const { Client } = require('pg');
const { randomUUID } = require('crypto');

const TENANT = 'ba25db13-13e3-42b9-a965-06e5c9a0f31f';

(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const id = randomUUID();
  const now = new Date();
  await c.query(
    `INSERT INTO storage_unit (
       id, name, current_volume, max_capacity, avg_cost, total_cost, tenant_id,
       created_date, last_modified_date, is_deleted, status
     ) VALUES ($1, $2, $3, $4, 0, 0, $5, $6, $6, false, 'AVAILABLE')`,
    [id, 'Cuve-Demo-Today', 500, 5000, TENANT, now]
  );
  console.log('Seeded Cuve-Demo-Today', id);
  const check = await c.query(
    `SELECT id::text, name, tenant_id::text, current_volume, max_capacity, status FROM storage_unit`
  );
  console.log(check.rows);
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
