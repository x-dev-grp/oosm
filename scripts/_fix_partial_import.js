const { Client } = require('pg');

(async () => {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  const tenant = 'ba25db13-13e3-42b9-a965-06e5c9a0f31f';

  console.log(
    'deliveries',
    (
      await c.query(
        'SELECT count(*)::int AS c, tenant_id::text FROM unified_delivery GROUP BY tenant_id'
      )
    ).rows
  );
  console.log(
    'tanks',
    (
      await c.query(
        "SELECT name, current_volume, total_cost, avg_cost, tenant_id::text FROM storage_unit WHERE name ILIKE 'Cuve-Demo%'"
      )
    ).rows
  );

  // Fix null cost fields that break oil stock movements
  await c.query(
    `UPDATE storage_unit
     SET total_cost = COALESCE(total_cost, 0),
         avg_cost = COALESCE(avg_cost, 0),
         current_volume = COALESCE(current_volume, 0),
         max_capacity = COALESCE(max_capacity, 10000)
     WHERE tenant_id = $1::uuid`,
    [tenant]
  );

  // Soft-clean partial import rows (if any) by IMP stamp
  for (const q of [
    `DELETE FROM quality_control_result WHERE tenant_id=$1::uuid`,
    `DELETE FROM oil_transaction WHERE tenant_id=$1::uuid`,
    `DELETE FROM oil_container_sale WHERE tenant_id=$1::uuid`,
    `DELETE FROM oil_sale WHERE tenant_id=$1::uuid`,
    `DELETE FROM financial_transaction WHERE tenant_id=$1::uuid`,
    `DELETE FROM expense WHERE tenant_id=$1::uuid`,
    `DELETE FROM unified_delivery WHERE tenant_id=$1::uuid`
  ]) {
    try {
      const r = await c.query(q, [tenant]);
      console.log('deleted', r.rowCount, q.split(' ')[2]);
    } catch (e) {
      console.log('skip', e.message.split('\n')[0]);
    }
  }

  // Reset tank volume for sales dry-run
  await c.query(
    `UPDATE storage_unit SET current_volume=50, total_cost=0, avg_cost=0
     WHERE tenant_id=$1::uuid AND name ILIKE 'Cuve-Demo%'`,
    [tenant]
  );
  console.log(
    'tanks after',
    (
      await c.query(
        "SELECT name, current_volume, total_cost, avg_cost FROM storage_unit WHERE tenant_id=$1::uuid AND name ILIKE 'Cuve-Demo%'",
        [tenant]
      )
    ).rows
  );
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
