const { Client } = require('../scripts/node_modules/pg');
const fs = require('fs');
const path = require('path');

(async () => {
  const cs =
    'postgresql://oosm_0n6u_user:jp7WiR5493hdCKbSS2LM4I768CeAbyVH@dpg-dacp1rfqj5pc738k9ung-a.oregon-postgres.render.com:5432/oosm_0n6u?sslmode=require';
  const c = new Client({ connectionString: cs, ssl: { rejectUnauthorized: false } });
  await c.connect();
  const sql = fs.readFileSync(path.join(__dirname, '../app/src/main/resources/db/fcm-token-rename.sql'), 'utf8');
  await c.query(sql);
  const r = await c.query(
    "select column_name from information_schema.columns where table_name='oosmuser' and column_name in ('fcm_token','one_signal_player_id') order by 1"
  );
  console.log(r.rows);
  await c.end();
})().catch((e) => {
  console.error(e.message);
  process.exit(1);
});
