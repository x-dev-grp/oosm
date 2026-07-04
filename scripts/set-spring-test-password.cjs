const { Client } = require('pg');

// Spring Security test vector: raw password "password"
const SPRING_TEST_HASH = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK9R/RnAa2oqeE.OJuLQ9zRBS';

async function main() {
  const c = new Client({ host: 'localhost', database: 'osm', user: 'postgres', password: 'root' });
  await c.connect();
  await c.query(`UPDATE role SET role_name = 'OOSMADMIN' WHERE role_name = 'OSMADMIN'`);
  await c.query(
    `UPDATE oosmuser
     SET password = $1, enabled = true, is_locked = false, is_new_user = false
     WHERE LOWER(username) = 'oosmadmin'`,
    [SPRING_TEST_HASH]
  );
  console.log('Set oosmAdmin password to: password');
  await c.end();
}

main();
