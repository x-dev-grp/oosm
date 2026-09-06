const { Client } = require('pg');
const fs = require('fs');
const path = require('path');

(async () => {
  const c = new Client({
    host: 'localhost',
    database: 'osm',
    user: 'postgres',
    password: process.env.PGPASSWORD || 'root'
  });
  await c.connect();
  const cols = await c.query(
    "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name='storage_unit' ORDER BY 1"
  );
  console.log('cols:', cols.rows.map((r) => r.column_name).join(', '));
  const tenant = fs.readFileSync(path.join(__dirname, '_tenant_id.txt'), 'utf8').trim();
  console.log('tenant', tenant);
  try {
    const d = await c.query(
      "SELECT count(*)::int AS c FROM unified_delivery WHERE tenant_id=$1::uuid AND description ILIKE '%[IMP:%'",
      [tenant]
    );
    console.log('imp deliveries', d.rows[0].c);
  } catch (e) {
    console.log('delivery count err', e.message);
  }
  try {
    const t = await c.query(
      "SELECT name, current_volume FROM storage_unit WHERE tenant_id=$1::uuid AND COALESCE(is_deleted,false)=false",
      [tenant]
    );
    console.log('tanks', t.rows);
  } catch (e) {
    console.log('tank err', e.message);
  }
  await c.end();
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
