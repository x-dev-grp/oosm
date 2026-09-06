const { Client } = require('pg');

(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const tenant = 'ba25db13-13e3-42b9-a965-06e5c9a0f31f';
  const rows = await c.query(
    `SELECT delivery_number, lot_number, delivery_type, olive_type, oil_type, operation_type, left(description, 40) AS desc
     FROM unified_delivery
     WHERE tenant_id=$1::uuid AND COALESCE(is_deleted,false)=false
     ORDER BY delivery_number::int NULLS LAST, created_date`,
    [tenant]
  );
  console.table(rows.rows);
  const sales = await c.query(
    `SELECT invoice_number, quantity, left(description,40) d FROM oil_sale WHERE tenant_id=$1::uuid`,
    [tenant]
  );
  console.log('sales', sales.rows);
  const tank = await c.query(
    `SELECT name, current_volume FROM storage_unit WHERE tenant_id=$1::uuid AND name='Cuve-Demo-Today'`,
    [tenant]
  );
  console.log('tank', tank.rows);
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
