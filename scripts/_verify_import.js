const { Client } = require('pg');
(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  console.log(
    'deliveries',
    (
      await c.query(
        `SELECT left(description,70) AS d, status::text, operation_type::text,
                oil_quantity, tenant_id IS NOT NULL AS has_tenant
         FROM unified_delivery ORDER BY created_date`
      )
    ).rows
  );
  console.log(
    'oil_tx',
    (
      await c.query(
        `SELECT transaction_type, quantity_kg, unit_price, reception_id IS NOT NULL AS has_reception,
                storage_unit_destination_id IS NOT NULL AS has_dest
         FROM oil_transaction ORDER BY created_date`
      )
    ).rows
  );
  console.log(
    'storage',
    (await c.query(`SELECT name, current_volume, avg_cost, total_cost FROM storage_unit`)).rows
  );
  console.log(
    'sales',
    (await c.query(`SELECT invoice_number, quantity FROM oil_sale`)).rows
  );
  console.log(
    'counts',
    (
      await c.query(
        `SELECT
           (SELECT count(*) FROM unified_delivery)::int AS deliveries,
           (SELECT count(*) FROM oil_sale)::int AS sales,
           (SELECT count(*) FROM oil_transaction)::int AS oil_tx,
           (SELECT count(*) FROM expense)::int AS expenses,
           (SELECT count(*) FROM quality_control_result)::int AS qc`
      )
    ).rows[0]
  );
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
