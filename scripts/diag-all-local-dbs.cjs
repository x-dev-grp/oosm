const { Client } = require('pg');

async function inspect(db) {
  const c = new Client({ host: 'localhost', database: db, user: 'postgres', password: 'root' });
  await c.connect();
  const tables = await c.query(
    `SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('oosmuser','osmuser')`
  );
  console.log(`\nDB ${db}:`, tables.rows.map((r) => r.table_name).join(', ') || '(no user tables)');
  try {
    const users = await c.query(
      `SELECT username, LEFT(password, 20) AS password_prefix FROM oosmuser ORDER BY username`
    );
    console.log(users.rows);
  } catch (err) {
    console.log('oosmuser query:', err.message);
  }
  await c.end();
}

async function main() {
  for (const db of ['osm', 'oosm', 'postgres']) {
    try {
      await inspect(db);
    } catch (err) {
      console.log(`DB ${db}:`, err.message);
    }
  }
}

main();
