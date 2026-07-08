#!/usr/bin/env node
/**
 * Upsert New Relic app_setting rows from Setup-NewRelic.ps1 JSON on stdin.
 * Requires: DB_URL/DB_USER/DB_PASS in oosm/.env or environment.
 */
const fs = require('fs');
const path = require('path');
const { Client } = require('pg');
const crypto = require('crypto');

function loadDotEnv(filePath) {
  if (!fs.existsSync(filePath)) return;
  for (const line of fs.readFileSync(filePath, 'utf8').split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const idx = trimmed.indexOf('=');
    if (idx <= 0) continue;
    const key = trimmed.slice(0, idx).trim();
    const value = trimmed.slice(idx + 1).trim();
    if (!process.env[key]) process.env[key] = value;
  }
}

function jdbcToPg(url) {
  if (!url) return null;
  let u = url.replace(/^jdbc:postgresql:\/\//, '');
  const slash = u.indexOf('/');
  const hostPort = slash >= 0 ? u.slice(0, slash) : u;
  const db = slash >= 0 ? u.slice(slash + 1).split('?')[0] : 'postgres';
  const [host, port = '5432'] = hostPort.split(':');
  return { host, port, database: db };
}

function resolveEncryptionMaterial() {
  return (
    process.env.APP_SETTINGS_ENCRYPTION_KEY ||
    process.env.JWT_SECRET ||
    'oosm-local-app-settings-encryption-key-32b'
  );
}

function deriveEncryptionKey(raw) {
  return crypto.createHash('sha256').update(raw, 'utf8').digest();
}

function encrypt(masterKey, settingKey, plaintext) {
  const nonce = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv('aes-256-gcm', masterKey, nonce, { authTagLength: 16 });
  cipher.setAAD(Buffer.from(settingKey, 'utf8'));
  const ciphertext = Buffer.concat([cipher.update(plaintext, 'utf8'), cipher.final(), cipher.getAuthTag()]);
  return `v1:${nonce.toString('base64')}:${ciphertext.toString('base64')}`;
}

const DEFINITIONS = {
  NEW_RELIC_APM_ENABLED: { category: 'INTEGRATIONS', label: 'New Relic APM enabled', type: 'BOOLEAN', secret: false },
  NEW_RELIC_APP_NAME: { category: 'INTEGRATIONS', label: 'New Relic application name', type: 'STRING', secret: false },
  NEW_RELIC_LICENSE_KEY: { category: 'INTEGRATIONS', label: 'New Relic license key', type: 'SECRET', secret: true },
  NEW_RELIC_LOG_FORWARDING_ENABLED: { category: 'INTEGRATIONS', label: 'New Relic log forwarding', type: 'BOOLEAN', secret: false },
  NEW_RELIC_REGION: { category: 'INTEGRATIONS', label: 'New Relic region', type: 'STRING', secret: false },
  NEW_RELIC_BROWSER_ENABLED: { category: 'INTEGRATIONS', label: 'New Relic browser monitoring', type: 'BOOLEAN', secret: false },
  NEW_RELIC_BROWSER_ACCOUNT_ID: { category: 'INTEGRATIONS', label: 'New Relic browser account ID', type: 'STRING', secret: false },
  NEW_RELIC_BROWSER_APPLICATION_ID: { category: 'INTEGRATIONS', label: 'New Relic browser application ID', type: 'STRING', secret: false },
  NEW_RELIC_BROWSER_LICENSE_KEY: { category: 'INTEGRATIONS', label: 'New Relic browser license key', type: 'STRING', secret: false }
};

async function main() {
  const repoRoot = path.resolve(__dirname, '..');
  loadDotEnv(path.join(repoRoot, '.env'));

  const stdin = fs.readFileSync(0, 'utf8').trim();
  const payload = JSON.parse(stdin || '{}');
  const settings = payload.settings || {};
  const licenseKey = payload.licenseKey;

  const jdbc = process.env.DB_URL;
  const pg = jdbcToPg(jdbc) || {
    host: process.env.PGHOST || 'localhost',
    port: process.env.PGPORT || '5432',
    database: process.env.PGDATABASE || 'osm'
  };

  const client = new Client({
    host: pg.host,
    port: Number(pg.port),
    database: pg.database,
    user: process.env.DB_USER || process.env.PGUSER || 'postgres',
    password: process.env.DB_PASS || process.env.PGPASSWORD || 'root'
  });

  const encKey = deriveEncryptionKey(resolveEncryptionMaterial());

  await client.connect();
  try {
    for (const [settingKey, def] of Object.entries(DEFINITIONS)) {
      let value = settings[settingKey];
      if (settingKey === 'NEW_RELIC_LICENSE_KEY') value = licenseKey;
      if (value == null || value === '') continue;

      const existing = await client.query('SELECT id FROM app_setting WHERE setting_key = $1', [settingKey]);
      const encryptedValue = def.secret ? encrypt(encKey, settingKey, String(value)) : null;
      const plainValue = def.secret ? null : String(value);

      if (existing.rowCount > 0) {
        await client.query(
          `UPDATE app_setting SET value = $2, encrypted_value = $3, updated_at = NOW() WHERE setting_key = $1`,
          [settingKey, plainValue, encryptedValue]
        );
      } else {
        await client.query(
          `INSERT INTO app_setting (
            id, setting_key, value, encrypted_value, encryption_key_id, value_type, category,
            label, description, sensitive, editable, restart_required, required_for_feature, created_at, updated_at
          ) VALUES (
            gen_random_uuid(), $1, $2, $3, 'v1', $4, $5, $6, $7, $8, true, $9, false, NOW(), NOW()
          )`,
          [
            settingKey,
            plainValue,
            encryptedValue,
            def.type,
            def.category,
            def.label,
            def.label,
            def.secret,
            ['NEW_RELIC_APM_ENABLED', 'NEW_RELIC_APP_NAME', 'NEW_RELIC_LOG_FORWARDING_ENABLED', 'NEW_RELIC_REGION'].includes(settingKey)
          ]
        );
      }
      console.log(`upserted ${settingKey}`);
    }
    await client.query(
      `UPDATE app_setting_meta SET settings_version = settings_version + 1, updated_at = NOW() WHERE id = 'global'`
    );
  } finally {
    await client.end();
  }
}

main().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});
