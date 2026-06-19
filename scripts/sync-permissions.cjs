#!/usr/bin/env node
/**
 * OSM permission catalog sync
 *
 * Single source of truth: oosm/modules/security/src/main/resources/permissions/permissions-spec.json
 *
 * Usage:
 *   node oosm/scripts/sync-permissions.cjs              # generate SQL seed + FE types + report
 *   node oosm/scripts/sync-permissions.cjs --extract    # bootstrap spec from legacy insert Permissions.sql
 *   node oosm/scripts/sync-permissions.cjs --check      # validate spec only (exit 1 on errors)
 *
 * Adding a new entity:
 *   1. Add entry under "entities" with module + profile (or explicit permissions[])
 *   2. Run this script
 *   3. Re-run DB seed OR enable app.security.permissions.sync-on-startup=true
 *
 * Adding a new action to one entity:
 *   - use "addActions": ["NEW_ACTION"] on that entity, OR
 *   - extend the shared "profile" if all entities using it should get the action
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.join(__dirname, '..');
const SPEC_PATH = path.join(ROOT, 'modules/security/src/main/resources/permissions/permissions-spec.json');
const LEGACY_SQL = path.join(
  ROOT,
  'modules/production/src/main/resources/legacy/osm-prod/db/migration/insert Permissions.sql'
);
const GENERATED_SEED_JSON = path.join(
  ROOT,
  'modules/security/src/main/resources/permissions/permissions-seed.generated.json'
);
const GENERATED_TS = path.join(
  ROOT,
  '../osm-ms-fe/src/app/theme/types/permissions.generated.ts'
);
const ACTION_JAVA = path.join(
  ROOT,
  'modules/shared-kernel/src/main/java/com/xdev/ooms/sharedkernel/models/Action.java'
);

const MODULE_ORDER = ['HR', 'RECEPTION', 'PRODUCTION', 'FINANCE', 'HABILITATION', 'INVENTAIR', 'CONDITIONING'];

const MODULE_TO_FE_ENUM = {
  HR: 'HREntity',
  RECEPTION: 'ReceptionEntity',
  PRODUCTION: 'ProductionEntity',
  FINANCE: 'FinanceEntity',
  HABILITATION: 'HabilitationEntity',
  INVENTAIR: 'InventoryEntity',
  CONDITIONING: 'ConditioningEntity',
};

const DEFAULT_PROFILES = {
  CRUD: ['READ', 'CREATE', 'UPDATE', 'DELETE'],
  CRUD_PDF: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'GEN_PDF'],
  FINANCE_DOC: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'VALIDATE', 'PAY', 'GEN_PDF'],
  FINANCE_SALE: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'CANCEL', 'VALIDATE', 'APPROVE', 'REJECT', 'PAY', 'GEN_PDF', 'COMPLETE',
  ],
  FINANCE_TRANSACTION: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'VALIDATE', 'PAY', 'GEN_PDF', 'COMPLETE_PAYMENT_DETAILS',
  ],
  OIL_CREDIT: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'VALIDATE', 'COMPLETE', 'GEN_PDF'],
  OIL_TRANSACTION: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'OIL_IN_TRANSACTION', 'OIL_OUT_TRANSACTION', 'OIL_PAYMENT', 'VALIDATE', 'GEN_PDF', 'COMPLETE',
  ],
  OIL_CONTAINER: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'GEN_PDF'],
  SUPPLIER_RECEPTION: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'ASSIGN_SUPPLIER', 'VALIDATE', 'GEN_PDF'],
  UNIFIED_DELIVERY: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'COMPLETE', 'TO_PROD', 'GEN_PDF',
    'OLIVE_QUALITY', 'OIL_QUALITY', 'UPDATE_OLIVE_QUALITY', 'UPDATE_OIL_QUALITY',
    'GEN_PDF_QC_OIL', 'GEN_PDF_QC_OLIVE', 'GEN_PDF_PRODUCTION', 'SET_PRICE', 'PAY',
    'OIL_RECEPTION', 'COMPLETE_PAYMENT_DETAILS', 'PLANNING',
  ],
  QC_RESULT: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'OLIVE_QUALITY', 'OIL_QUALITY',
    'UPDATE_OLIVE_QUALITY', 'UPDATE_OIL_QUALITY', 'VALIDATE', 'GEN_PDF',
  ],
  QC_RULE: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'GEN_PDF'],
  STORAGE_UNIT: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'SET_PRICE', 'GEN_PDF'],
  MACHINE_PLAN: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'APPROVE', 'REJECT', 'MAINTENANCE', 'GEN_PDF'],
  ARTICLE: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'ENTREE_STOCK', 'SORTIE_STOCK'],
  STOCK_FULL: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'ENTREE_STOCK', 'SORTIE_STOCK', 'AJUSTER_STOCK',
    'ASSIGN_EMPLACEMENT', 'RESERVER_STOCK', 'LIBERER_STOCK', 'CHECK_STOCK', 'TRANSFERER_STOCK',
  ],
  STOCK_LOCATION: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'ASSIGN_EMPLACEMENT', 'RESERVER_STOCK', 'LIBERER_STOCK', 'TRANSFERER_STOCK',
  ],
  BON_COMMANDE: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'GEN_PDF'],
  CONDITIONING_OF: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'START', 'PAUSE', 'RESUME', 'CLOSE', 'AJUSTER_STOCK', 'GEN_PDF',
  ],
  CONDITIONING_PROJECT: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'CANCEL', 'UPDATE_STATUS', 'GEN_PDF'],
  CONDITIONING_SHIPPING: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'ADD_LINE', 'REMOVE_LINE', 'UPDATE_STATUS', 'SHIP', 'DELIVER', 'GEN_PDF',
  ],
  CONDITIONING_EXPEDITION: [
    'READ', 'CREATE', 'UPDATE', 'DELETE', 'ADD_LINE', 'REMOVE_LINE', 'VALIDATE', 'SHIP', 'DELIVER', 'CLOSE', 'CANCEL', 'GEN_PDF',
  ],
  CONDITIONING_QUALITY: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'VALIDATE', 'UPDATE_STATUS', 'GEN_PDF'],
  CONDITIONING_LABEL: ['READ', 'CREATE', 'UPDATE', 'DELETE', 'DRAFT', 'VALIDATE', 'FINALIZE', 'EXPORT', 'GEN_PDF'],
  CONDITIONING_ANALYTICS: ['READ', 'REPORT', 'GEN_PDF', 'EXPORT'],
  CONDITIONING_SYNC: ['READ', 'SYNC'],
  READ_ONLY: ['READ'],
};

/** Guess profile name from permission list (for --extract). */
function guessProfile(permissions) {
  const sorted = [...new Set(permissions.map((p) => p.toUpperCase()))].sort();
  for (const [name, actions] of Object.entries(DEFAULT_PROFILES)) {
    const profileSorted = [...new Set(actions)].sort();
    if (sorted.length === profileSorted.length && sorted.every((v, i) => v === profileSorted[i])) {
      return name;
    }
  }
  return null;
}

function loadJavaActions() {
  const src = fs.readFileSync(ACTION_JAVA, 'utf8');
  const actions = [];
  for (const line of src.split('\n')) {
    const m = line.match(/^\s*([A-Z][A-Z0-9_]*)\s*,?\s*(?:\/\/.*)?$/);
    if (m && m[1] !== 'Action') {
      actions.push(m[1]);
    }
  }
  return new Set(actions);
}

function parseLegacySeedJson() {
  const sql = fs.readFileSync(LEGACY_SQL, 'utf8');
  const marker = 'seed_permissions_from_json($$';
  const start = sql.indexOf(marker);
  if (start === -1) {
    throw new Error(`Could not find seed JSON in ${LEGACY_SQL}`);
  }
  const jsonStart = start + marker.length;
  const end = sql.indexOf('$$::jsonb)', jsonStart);
  if (end === -1) {
    throw new Error('Could not find end of seed JSON');
  }
  return JSON.parse(sql.slice(jsonStart, end));
}

function entityToSpecEntry(name, def, section) {
  const permissions = (def.permissions || []).map((p) => p.toUpperCase()).filter(Boolean);
  const profile = guessProfile(permissions);
  const entry = {
    module: def.module,
    description: def.description || name,
    section: section === 'security_entities' ? 'security_entities' : 'entities',
  };
  if (profile) {
    entry.profile = profile;
  } else {
    entry.permissions = permissions;
  }
  return entry;
}

function extractSpecFromLegacy() {
  const seed = parseLegacySeedJson();
  const entities = {};
  for (const [name, def] of Object.entries(seed.entities || {})) {
    entities[name] = entityToSpecEntry(name, def, 'entities');
  }
  for (const [name, def] of Object.entries(seed.security_entities || {})) {
    if (entities[name]) {
      entities[name].section = 'security_entities';
      continue;
    }
    entities[name] = entityToSpecEntry(name, def, 'security_entities');
  }

  // Known legacy aliases / mirrors from SQL comments and migrations
  if (entities.FOURNISSEUR) {
    entities.MATERIEL_SUPPLIER = entities.MATERIEL_SUPPLIER || entities.FOURNISSEUR;
    entities.MATERIEL_SUPPLIER.legacyAliases = ['FOURNISSEUR'];
    delete entities.FOURNISSEUR;
  }

  const spec = {
    version: 1,
    actionProfiles: DEFAULT_PROFILES,
    modules: seed.modules || {},
    actions: seed.actions || {},
    entities,
    roleMirrors: [
      { sourceModule: 'INVENTAIR', sourceEntity: 'ARTICLESEC', targetModule: 'INVENTAIR', targetEntity: 'ARTICLE' },
      { sourceModule: 'FINANCE', sourceEntity: 'WASTESALE', targetModule: 'PRODUCTION', targetEntity: 'WASTE' },
      { sourceModule: 'PRODUCTION', sourceEntity: 'STORAGEUNIT', targetModule: 'PRODUCTION', targetEntity: 'OILCONTAINER' },
    ],
  };

  fs.mkdirSync(path.dirname(SPEC_PATH), { recursive: true });
  fs.writeFileSync(SPEC_PATH, `${JSON.stringify(spec, null, 2)}\n`, 'utf8');
  console.log(`Extracted ${Object.keys(entities).length} entities -> ${SPEC_PATH}`);
}

function loadSpec() {
  if (!fs.existsSync(SPEC_PATH)) {
    throw new Error(`Missing ${SPEC_PATH}. Run with --extract first.`);
  }
  return JSON.parse(fs.readFileSync(SPEC_PATH, 'utf8'));
}

function resolveProfiles(spec) {
  return { ...DEFAULT_PROFILES, ...(spec.actionProfiles || {}) };
}

function resolveEntityPermissions(entityDef, profiles) {
  let permissions;
  if (Array.isArray(entityDef.permissions) && entityDef.permissions.length) {
    permissions = [...entityDef.permissions];
  } else if (entityDef.profile) {
    permissions = profiles[entityDef.profile];
    if (!permissions) {
      throw new Error(`Unknown profile "${entityDef.profile}"`);
    }
    permissions = [...permissions];
  } else {
    throw new Error('Entity must define "profile" or "permissions"');
  }
  if (Array.isArray(entityDef.addActions)) {
    permissions.push(...entityDef.addActions);
  }
  if (Array.isArray(entityDef.removeActions)) {
    const remove = new Set(entityDef.removeActions.map((a) => a.toUpperCase()));
    permissions = permissions.filter((a) => !remove.has(a.toUpperCase()));
  }
  return [...new Set(permissions.map((p) => p.toUpperCase()).filter(Boolean))].sort();
}

function buildSeedPayload(spec) {
  const profiles = resolveProfiles(spec);
  const entities = {};
  const security_entities = {};

  for (const [name, def] of Object.entries(spec.entities || {})) {
    const permissions = resolveEntityPermissions(def, profiles);
    const payload = {
      description: def.description || name,
      module: def.module,
      permissions,
    };
    const section = def.section === 'security_entities' ? security_entities : entities;
    section[name] = payload;

    for (const alias of def.legacyAliases || []) {
      section[alias] = {
        description: `Legacy alias — migrated to ${name}`,
        module: def.module,
        permissions,
      };
    }
  }

  return {
    entities,
    security_entities,
    modules: spec.modules || {},
    actions: spec.actions || {},
  };
}

function validateSpec(spec, javaActions) {
  const profiles = resolveProfiles(spec);
  const errors = [];
  const warnings = [];

  for (const [profileName, actions] of Object.entries(profiles)) {
    for (const action of actions) {
      if (!javaActions.has(action)) {
        warnings.push(`Profile ${profileName}: action ${action} not in Java Action enum`);
      }
    }
  }

  for (const [name, def] of Object.entries(spec.entities || {})) {
    try {
      const perms = resolveEntityPermissions(def, profiles);
      if (!def.module) {
        errors.push(`${name}: missing module`);
      }
      if (!MODULE_ORDER.includes(def.module)) {
        errors.push(`${name}: unknown module ${def.module}`);
      }
      for (const action of perms) {
        if (!javaActions.has(action)) {
          warnings.push(`${name}: action ${action} not in Java Action enum`);
        }
      }
    } catch (e) {
      errors.push(`${name}: ${e.message}`);
    }
  }

  return { errors, warnings };
}

function generateTypeScript(spec) {
  const byModule = {};
  for (const [name, def] of Object.entries(spec.entities || {})) {
    if (def.legacyAliases && name !== 'MATERIEL_SUPPLIER') {
      // skip pure alias targets duplicated under legacy keys
    }
    const mod = def.module;
    if (!byModule[mod]) {
      byModule[mod] = [];
    }
    byModule[mod].push(name);
  }

  const lines = [
    '// AUTO-GENERATED by oosm/scripts/sync-permissions.cjs — do not edit manually',
    '// Re-export from permissions.ts or import entity keys from here.',
    '',
  ];

  for (const mod of MODULE_ORDER) {
    const enumName = MODULE_TO_FE_ENUM[mod];
    if (!enumName || !byModule[mod]?.length) {
      continue;
    }
    const keys = [...new Set(byModule[mod])].sort();
    lines.push(`export enum ${enumName} {`);
    for (const key of keys) {
      const prop = /^[A-Z][A-Z0-9_]*$/.test(key) ? key : `'${key}'`;
      lines.push(`  ${/^[A-Z]/.test(key) ? key : `'${key}'`} = '${key}',`);
    }
    lines.push('}');
    lines.push('');
  }

  const allActions = new Set();
  const profiles = resolveProfiles(spec);
  for (const def of Object.values(spec.entities || {})) {
    resolveEntityPermissions(def, profiles).forEach((a) => allActions.add(a));
  }
  lines.push('/** Actions referenced by the permission catalog (subset of Action enum). */');
  lines.push('export enum CatalogAction {');
  [...allActions].sort().forEach((a) => lines.push(`  ${a} = '${a}',`));
  lines.push('}');
  lines.push('');

  fs.mkdirSync(path.dirname(GENERATED_TS), { recursive: true });
  fs.writeFileSync(GENERATED_TS, lines.join('\n'), 'utf8');
  console.log(`Generated ${GENERATED_TS}`);
}

function patchLegacySql(seedPayload) {
  if (!fs.existsSync(LEGACY_SQL)) {
    console.warn(`Legacy SQL not found: ${LEGACY_SQL}`);
    return;
  }
  const sql = fs.readFileSync(LEGACY_SQL, 'utf8');
  const marker = 'seed_permissions_from_json($$';
  const start = sql.indexOf(marker);
  const end = sql.indexOf('$$::jsonb)', start);
  if (start === -1 || end === -1) {
    console.warn('Could not patch legacy SQL seed block');
    return;
  }
  const before = sql.slice(0, start + marker.length);
  const after = sql.slice(end);
  const json = JSON.stringify(seedPayload, null, 2);
  const header = `-- NOTE: seed JSON block below is regenerated from permissions-spec.json via: node oosm/scripts/sync-permissions.cjs\n`;
  const updated = header + before + json + after;
  fs.writeFileSync(LEGACY_SQL, updated, 'utf8');
  console.log(`Patched seed JSON in ${LEGACY_SQL}`);
}

function writeReport(spec, validation) {
  const profiles = resolveProfiles(spec);
  const entityCount = Object.keys(spec.entities || {}).length;
  let permissionCount = 0;
  for (const def of Object.values(spec.entities || {})) {
    permissionCount += resolveEntityPermissions(def, profiles).length;
    for (const alias of def.legacyAliases || []) {
      permissionCount += resolveEntityPermissions(def, profiles).length;
    }
  }

  console.log('\n--- Permission catalog report ---');
  console.log(`Entities: ${entityCount}`);
  console.log(`Permission rows (approx): ${permissionCount}`);
  console.log(`Profiles: ${Object.keys(profiles).length}`);
  if (validation.warnings.length) {
    console.log(`\nWarnings (${validation.warnings.length}):`);
    validation.warnings.slice(0, 20).forEach((w) => console.log(`  - ${w}`));
    if (validation.warnings.length > 20) {
      console.log(`  ... and ${validation.warnings.length - 20} more`);
    }
  }
  if (validation.errors.length) {
    console.log(`\nErrors (${validation.errors.length}):`);
    validation.errors.forEach((e) => console.log(`  - ${e}`));
  } else {
    console.log('\nValidation: OK');
  }
}

function sync() {
  const spec = loadSpec();
  const javaActions = loadJavaActions();
  const validation = validateSpec(spec, javaActions);

  if (validation.errors.length) {
    writeReport(spec, validation);
    process.exit(1);
  }

  const seedPayload = buildSeedPayload(spec);
  fs.mkdirSync(path.dirname(GENERATED_SEED_JSON), { recursive: true });
  fs.writeFileSync(GENERATED_SEED_JSON, `${JSON.stringify(seedPayload, null, 2)}\n`, 'utf8');
  console.log(`Generated ${GENERATED_SEED_JSON}`);

  patchLegacySql(seedPayload);
  generateTypeScript(spec);
  writeReport(spec, validation);
}

const args = process.argv.slice(2);
if (args.includes('--extract')) {
  extractSpecFromLegacy();
} else if (args.includes('--check')) {
  const spec = loadSpec();
  const validation = validateSpec(spec, loadJavaActions());
  writeReport(spec, validation);
  process.exit(validation.errors.length ? 1 : 0);
} else {
  if (!fs.existsSync(SPEC_PATH)) {
    console.log('No spec found — extracting from legacy SQL first...');
    extractSpecFromLegacy();
  }
  sync();
}
