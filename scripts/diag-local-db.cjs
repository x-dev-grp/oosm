const { Client } = require('pg');

async function inspect(cfg) {
  const c = new Client(cfg);
  await c.connect();
  console.log('Connected:', cfg.database, '@', cfg.host);

  for (const table of ['oosmuser', 'osmuser', 'osm_user']) {
    try {
      const count = await c.query(`SELECT COUNT(*)::int AS n FROM ${table}`);
      const users = await c.query(
        `SELECT username, enabled, is_locked, is_new_user FROM ${table} ORDER BY username LIMIT 15`
      );
      console.log(`\n${table}: ${count.rows[0].n} rows`);
      console.log(JSON.stringify(users.rows, null, 2));
    } catch (err) {
      console.log(`${table}: ${err.message}`);
    }
  }

  try {
    const oauth = await c.query('SELECT client_id FROM oauth2_registered_client ORDER BY 1');
    console.log('\noauth2 clients:', oauth.rows.map((r) => r.client_id).join(', '));
  } catch (err) {
    console.log('oauth2_registered_client:', err.message);
  }

  await c.end();
}

async function main() {
  const configs = [
    { host: 'localhost', database: 'osm', user: 'postgres', password: 'root' },
    { host: 'localhost', database: 'osm', user: 'postgres', password: 'postgres' },
    { host: 'localhost', database: 'postgres', user: 'postgres', password: 'root' }
  ];

  for (const cfg of configs) {
    try {
      await inspect(cfg);
      return;
    } catch (err) {
      console.log(`Failed ${cfg.database}/${cfg.password}: ${err.message}`);
    }
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
