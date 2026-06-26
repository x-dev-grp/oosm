const { Client } = require('pg');

async function main() {
  const client = new Client({ connectionString: process.argv[2] });
  await client.connect();

  const tables = await client.query(
    "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name LIKE '%user%' ORDER BY 1"
  );
  console.log('User tables:', tables.rows.map((r) => r.table_name).join(', '));

  for (const table of ['oosmuser', 'osmuser']) {
    try {
      const user = await client.query(
        `SELECT u.id, u.username, u.email, u.enabled, u.is_locked, u.is_new_user, u.tenant_id, r.role_name
         FROM ${table} u
         LEFT JOIN role r ON r.id = u.role_id
         WHERE LOWER(u.username) = LOWER($1)`,
        ['wajdibh']
      );
      console.log(`\n${table}:`, JSON.stringify(user.rows, null, 2));
    } catch (err) {
      console.log(`${table}:`, err.message);
    }
  }

  const similar = await client.query(
    `SELECT u.id, u.username, u.enabled, u.is_locked, u.is_new_user, u.tenant_id, r.role_name
     FROM oosmuser u
     LEFT JOIN role r ON r.id = u.role_id
     WHERE LOWER(u.username) LIKE '%wajdi%'`
  );
  console.log('\nSimilar users:', JSON.stringify(similar.rows, null, 2));

  const userRow = await client.query(
    `SELECT u.*, r.role_name FROM oosmuser u LEFT JOIN role r ON r.id = u.role_id WHERE LOWER(u.username)=LOWER($1)`,
    ['wajdibh']
  );
  if (userRow.rows[0]?.tenant_id) {
    const tenantId = userRow.rows[0].tenant_id;
    const company = await client.query(
      'SELECT id, legal_name, is_active, is_deleted FROM company_profile WHERE id = $1',
      [tenantId]
    );
    console.log('\nCompany profile:', JSON.stringify(company.rows, null, 2));
  }

  await client.end();
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
