const { Client } = require('pg');
const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const TENANT = (process.argv[2] || fs.readFileSync(path.join(__dirname, '_tenant_id.txt'), 'utf8')).trim();

async function main() {
  console.log('1) Cleaning tenant business data', TENANT);
  execFileSync(process.execPath, [path.join(__dirname, 'run-clean-tenant-data.cjs'), TENANT], {
    stdio: 'inherit',
    env: process.env,
    cwd: __dirname
  });

  const c = new Client({
    host: process.env.PGHOST || 'localhost',
    port: Number(process.env.PGPORT || 5432),
    database: process.env.PGDATABASE || 'osm',
    user: process.env.PGUSER || 'postgres',
    password: process.env.PGPASSWORD || 'root'
  });
  await c.connect();

  const cols = await c.query(`
    SELECT column_name FROM information_schema.columns
    WHERE table_schema='public' AND table_name='storage_unit'
    ORDER BY ordinal_position`);
  console.log('storage_unit columns:', cols.rows.map((r) => r.column_name).join(', '));

  // Soft-create tanks needed by the demo workbook (volume for oil-sale dry-run).
  const hasTenant = cols.rows.some((r) => r.column_name === 'tenant_id');
  const hasDeleted = cols.rows.some((r) => r.column_name === 'is_deleted');
  const volCol = cols.rows.some((r) => r.column_name === 'current_volume')
    ? 'current_volume'
    : cols.rows.some((r) => r.column_name === 'current_volume_liters')
      ? 'current_volume_liters'
      : null;
  if (!volCol) throw new Error('No current volume column on storage_unit');

  for (const name of ['Cuve-Demo-Today', 'Cuve-Demo-Reserve']) {
    const existing = await c.query(
      `SELECT id FROM storage_unit WHERE lower(name)=lower($1) ${hasTenant ? 'AND tenant_id=$2::uuid' : ''} ${hasDeleted ? 'AND COALESCE(is_deleted,false)=false' : ''} LIMIT 1`,
      hasTenant ? [name, TENANT] : [name]
    );
    if (existing.rowCount) {
      await c.query(
        `UPDATE storage_unit SET ${volCol}=GREATEST(COALESCE(${volCol},0), 50)
         WHERE id=$1`,
        [existing.rows[0].id]
      );
      console.log('Updated tank', name, existing.rows[0].id);
    } else {
      const id = await c.query('SELECT gen_random_uuid() AS id');
      const uid = id.rows[0].id;
      const fields = ['id', 'name', volCol, 'max_capacity'];
      const values = [uid, name, 50, 10000];
      if (hasTenant) {
        fields.push('tenant_id');
        values.push(TENANT);
      }
      if (hasDeleted) {
        fields.push('is_deleted');
        values.push(false);
      }
      if (cols.rows.some((r) => r.column_name === 'created_date')) {
        fields.push('created_date');
        values.push(new Date());
      }
      if (cols.rows.some((r) => r.column_name === 'created_by')) {
        fields.push('created_by');
        values.push('reimport-seed');
      }
      if (cols.rows.some((r) => r.column_name === 'status')) {
        fields.push('status');
        values.push('AVAILABLE');
      }
      const placeholders = values.map((_, i) => `$${i + 1}`).join(',');
      await c.query(
        `INSERT INTO storage_unit (${fields.join(',')}) VALUES (${placeholders})`,
        values
      );
      console.log('Created tank', name, uid);
    }
  }

  const tanks = await c.query(
    `SELECT name, ${volCol} AS vol FROM storage_unit
     WHERE ${hasTenant ? 'tenant_id=$1::uuid' : 'true'}
       ${hasDeleted ? 'AND COALESCE(is_deleted,false)=false' : ''}
       AND name ILIKE 'Cuve-Demo-%'`,
    hasTenant ? [TENANT] : []
  );
  console.log('Tanks ready:', tanks.rows);
  await c.end();
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
