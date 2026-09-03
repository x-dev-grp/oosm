# Secure Administration Settings Plan

**Version:** 1.1  
**Last updated:** 2026-07-07  
**Status:** Approved for implementation

## Objective

Build an Administration UI and backend settings system that lets authorized administrators manage application configuration without exposing infrastructure credentials or creating unsafe runtime behavior.

The UI must support operational settings such as mail, frontend URLs, notification settings, QR URLs, feature toggles, and selected integration config. It must not expose database credentials or other infrastructure bootstrap secrets.

## Scope

### In scope (v1)

```text
Global application settings (single-tenant).
Mail (Resend), notifications (FCM), QR, CORS, frontend URL, logging/diagnostics.
Write-only secret rotation with AES-GCM encryption at rest.
Audit log for all change attempts.
Env fallback for backward compatibility with Render deployments.
```

### Out of scope (v1)

```text
Per-tenant or per-company settings (no tenant_id on app_setting in v1).
Editing Render environment variables from the app.
Managing database credentials or JWT signing material from the UI.
Automatic Resend domain verification on save (deferred to v1.1 — see Mail Validation).
Distributed settings admin across multiple workspaces.
```

### Future consideration (v2+)

```text
tenant_id on app_setting for multi-company branding and integration config.
Encryption key versioning with automated re-encryption.
Hard validation against Resend Domains API on MAIL_FROM_ADDRESS save.
```

## Non-Negotiable Security Rules

1. The frontend must never read, display, or directly edit Render environment variables.
2. The frontend must never receive secret values after they are saved.
3. Secret values must be write-only from the UI.
4. Every settings change must be audited (success and failure).
5. Every setting must have validation before persistence.
6. Every setting must be permission-protected.
7. Every setting must declare whether it is runtime reloadable or restart-required.
8. Database credentials must remain outside the application settings UI.
9. JWT signing material must remain outside ordinary settings management.
10. The system must fail closed: missing or invalid config disables the dependent feature instead of using unsafe defaults.
11. The application must never call the Render API for ordinary settings updates.
12. Bootstrap secrets and one-time boot flags must not be editable after successful first startup.

## Semantics

### `required` flag

`required` means **required for the dependent feature**, not required for application startup.

Examples:

```text
RESEND_API_KEY required=true  -> mail cannot send without it; app still boots.
MAIL_FROM_ADDRESS required=true -> mail cannot send without it; app still boots.
DB_URL not in registry -> always infrastructure; app fails boot if missing.
```

### `configured` flag (API responses for secrets)

```text
configured=true  -> a non-empty value exists in DB (encrypted) or env fallback.
configured=false -> no value; feature using this secret is disabled.
value            -> always null for sensitive settings.
```

### Source precedence

Runtime config reads use this order:

```text
1. Database app_setting value (or encrypted_value for secrets)
2. Process environment variable (Render env)
3. application.yml default
```

For secrets specifically:

```text
1. Database encrypted secret (decrypted in memory only)
2. Environment variable
3. missing -> feature disabled, never a hardcoded default
```

This keeps existing deployments working while allowing gradual migration into admin-managed config.

## Configuration Classification

### Forbidden In Admin UI

These must stay in Render or equivalent infrastructure secret storage. They are not registered in `AppSettingDefinitionRegistry`.

```text
DB_URL
DB_USER
DB_PASS
APP_SETTINGS_ENCRYPTION_KEY
JWT_SECRET
SERVER_PORT
PORT
PGHOST
PGPORT
PGDATABASE
PGUSER
PGPASSWORD
RENDER_EXTERNAL_URL
INTERNAL_BASE_URL
JWK_SET_URI
OAUTH2_CLIENT_ID
SECURITY_BOOTSTRAP_PASSWORD
```

Rationale:

```text
Database credentials control persistence.
Encryption key protects stored secrets at rest.
JWT secret and OAuth client ID control authentication trust.
Server, port, and platform-derived URLs require infrastructure-level control.
Bootstrap password is a one-time boot secret, never UI-managed.
```

### Read-Only Diagnostics (visible, never editable)

Shown in admin diagnostics after first successful startup. Values come from env or boot-time state only.

```text
SECURITY_BOOTSTRAP_ENABLED
SECURITY_BOOTSTRAP_USERNAME
SECURITY_BOOTSTRAP_EMAIL
SECURITY_BOOTSTRAP_PHONE
SECURITY_BOOTSTRAP_RESET_PASSWORD
PERMISSIONS_SYNC_ON_STARTUP
PRODUCTION_BOOTSTRAP_ENABLED
```

Rules:

```text
Display current effective value or "managed by infrastructure".
No PUT or rotate-secret endpoints for these keys.
Changes require Render env update + service restart.
Audit any attempted API access as PERMISSION_DENIED or NOT_EDITABLE.
```

### Write-Only Secret Settings

Managed only through `POST /rotate-secret`. Never returned by API responses.

```text
RESEND_API_KEY
FCM_PRIVATE_KEY
OAUTH2_CLIENT_SECRET
THIRD_PARTY_WEBHOOK_SECRET
FUTURE_API_KEYS
```

Returned API shape:

```json
{
  "key": "RESEND_API_KEY",
  "configured": true,
  "sensitive": true,
  "value": null,
  "lastUpdatedAt": "2026-07-07T16:00:00Z",
  "source": "DATABASE"
}
```

### Editable Non-Secret Settings

These can be read and updated in the admin UI:

```text
MAIL_ENABLED
MAIL_FROM_ADDRESS
MAIL_FROM_NAME
MAIL_SUPPORT_EMAIL
MAIL_DEBUG
FRONTEND_ENTRY_POINT
APP_CORS_ALLOWED_ORIGIN_PATTERNS
QR_BASE_URL
FCM_PROJECT_ID
SEARCH_DEBUG_ON_STARTUP
HEALTH_SHOW_DETAILS
LOG_LEVEL_WEB
LOG_LEVEL_REST
```

## Database Model

### app_setting

```sql
create table app_setting (
    id uuid primary key,
    setting_key varchar(150) not null unique,
    value text null,
    encrypted_value text null,
    encryption_key_id varchar(20) not null default 'v1',
    value_type varchar(50) not null,
    category varchar(80) not null,
    label varchar(150) not null,
    description text null,
    sensitive boolean not null default false,
    editable boolean not null default true,
    restart_required boolean not null default false,
    required_for_feature boolean not null default false,
    validation_regex text null,
    allowed_values text null,
    default_value text null,
    effective_source varchar(50) not null default 'DEFAULT',
    last_validated_at timestamp null,
    last_validation_status varchar(50) null,
    created_at timestamp not null,
    updated_at timestamp not null,
    updated_by uuid null
);

create index idx_app_setting_category on app_setting (category);
```

Notes:

```text
setting_key replaces ambiguous column name "key".
required_for_feature replaces "required" for clarity.
effective_source is computed at read time: DATABASE | ENV | DEFAULT.
encryption_key_id supports future key rotation (v1 uses only 'v1').
No tenant_id in v1 — global settings only.
```

### app_setting_meta

Lightweight global metadata for cross-instance cache coordination (v1.1 optional, recommended for multi-instance):

```sql
create table app_setting_meta (
    id varchar(50) primary key default 'global',
    settings_version bigint not null default 0,
    updated_at timestamp not null
);
```

Rules:

```text
Increment settings_version on every successful setting write.
Each instance polls settings_version every 30s (or on /reload).
When version changes, invalidate local cache and reload.
Single-instance Render free tier can skip polling and rely on local invalidation only.
```

### app_setting_audit

```sql
create table app_setting_audit (
    id uuid primary key,
    setting_key varchar(150) not null,
    action varchar(50) not null,
    old_value_masked text null,
    new_value_masked text null,
    changed_by uuid null,
    changed_by_username varchar(150) null,
    changed_at timestamp not null,
    ip_address varchar(100) null,
    user_agent text null,
    reason text null,
    success boolean not null,
    failure_reason text null
);

create index idx_app_setting_audit_key on app_setting_audit (setting_key);
create index idx_app_setting_audit_changed_at on app_setting_audit (changed_at desc);
```

Audit actions:

```text
UPDATE
ROTATE_SECRET
RELOAD
TEST_MAIL
TEST_NOTIFICATION
VALIDATION_FAILED
PERMISSION_DENIED
```

### app_setting_definition

Use a **code-based registry** as the source of truth, not a mutable DB table.

Each setting definition must include:

```text
key
category
label
description
valueType
sensitive
editable
requiredForFeature
restartRequired
validationRule
defaultFallback
permissionRequired
reloadable
```

## Value Types

Supported types:

```text
STRING
BOOLEAN
INTEGER
URL
EMAIL
CSV
JSON
SECRET
ENUM
```

Validation examples:

```text
MAIL_FROM_ADDRESS: EMAIL (+ domain suffix allowlist in v1; Resend API check in v1.1)
MAIL_ENABLED: BOOLEAN
FRONTEND_ENTRY_POINT: URL (https only in production)
APP_CORS_ALLOWED_ORIGIN_PATTERNS: CSV of origin patterns; wildcard * rejected
LOG_LEVEL_WEB: ENUM [TRACE, DEBUG, INFO, WARN, ERROR]
LOG_LEVEL_REST: ENUM [TRACE, DEBUG, INFO, WARN, ERROR]
HEALTH_SHOW_DETAILS: ENUM [never, when_authorized, always]
RESEND_API_KEY: SECRET; prefix must be "re_" when non-empty
OAUTH2_CLIENT_SECRET: SECRET; min length 16 when non-empty
```

Invalid examples (must be rejected):

```text
MAIL_FROM_ADDRESS=xdev.pro
MAIL_ENABLED=maybe
HEALTH_SHOW_DETAILS=true
LOG_LEVEL_WEB=VERBOSE
APP_CORS_ALLOWED_ORIGIN_PATTERNS=*
FRONTEND_ENTRY_POINT=javascript:alert(1)
```

## Backend Components

### AppSettingsService

Responsibilities:

```text
Read settings with env and default fallback.
Validate updates against definition registry.
Encrypt and decrypt sensitive values.
Mask values for API responses.
Write audit records for all attempts.
Expose typed getters for runtime consumers.
Refresh in-memory cache.
Report missing feature-required settings in /status.
Track effective source per setting (DATABASE | ENV | DEFAULT).
```

Interface:

```java
public interface AppSettingsService {
    String getString(String key);
    String getString(String key, String fallback);
    boolean getBoolean(String key, boolean fallback);
    int getInt(String key, int fallback);
    Optional<String> getSecret(String key);
    List<AdminSettingCategoryDto> listForAdmin();
    AdminSettingDto getForAdmin(String key);
    AdminSettingDto update(String key, UpdateSettingRequest request, UserContext user);
    AdminSettingDto rotateSecret(String key, RotateSecretRequest request, UserContext user);
    void reload();
    AdminSettingsStatusDto getStatus();
}
```

### AppSettingDefinitionRegistry

Code-owned registry containing all valid keys.

Rules:

```text
Unknown keys cannot be created from the UI.
Sensitive classification cannot be changed from the UI.
Validation rules cannot be changed from the UI.
Restart-required classification cannot be changed from the UI.
Read-only diagnostic keys are registered but marked editable=false.
```

### AppSettingEncryptionService

Use **AES-256-GCM** with a Render-only master key:

```text
APP_SETTINGS_ENCRYPTION_KEY
```

Rules:

```text
Key must be at least 256 bits (32 bytes, base64-encoded in env).
Never store plaintext secrets in app_setting.value.
Use app_setting.encrypted_value for secrets only.
Use unique nonce per encryption (12 bytes).
Include setting_key as additional authenticated data (AAD).
Store encryption_key_id='v1' on every encrypted row.
Decrypt only in service layer; never log decrypted values.
```

Encrypted payload format (stored in `encrypted_value`):

```text
v1:<base64(nonce)>:<base64(ciphertext+tag)>
```

### Encryption Key Rotation (v1 procedure)

v1 does not support live key rotation. Document this operational procedure:

```text
1. Set new APP_SETTINGS_ENCRYPTION_KEY in Render (label it v2 when versioning is added).
2. Re-enter all secrets via admin UI rotate-secret (old ciphertext is unreadable).
3. Deploy/restart the service.
4. Verify /api/admin/settings/status shows all secrets configured.
```

v2 (future): support `encryption_key_id` v1/v2 with dual-decrypt during migration window.

### AppSettingAuditService

Every attempted change writes an audit event.

Audit values:

```text
Non-secret old/new values: store masked or truncated values (max 500 chars).
Secret old/new values: store <configured>, <empty>, or <rotated> only.
Failed validation: store failure reason, never the rejected secret.
Permission denied: store attempted key and actor when available.
```

### Runtime Consumer Migration

Today, modules use `@Value` injection (e.g. `OosmMailProperties`). Migration pattern:

#### Phase A — introduce AppSettingsService (Phase 1)

```text
AppSettingsService reads DB -> env -> default.
Existing @Value beans unchanged; env fallback preserves current behavior.
```

#### Phase B — dynamic consumer wrappers (Phase 2, mail first)

Replace static `@Value` readers with beans that delegate to `AppSettingsService` on each call:

```java
@Component
public class DynamicMailSettings {
    private final AppSettingsService settings;

    public boolean isEnabled() {
        return settings.getBoolean("MAIL_ENABLED", true);
    }

    public Optional<String> getApiKey() {
        return settings.getSecret("RESEND_API_KEY");
    }

    public String getFromAddress() {
        return settings.getString("MAIL_FROM_ADDRESS", "");
    }
}
```

`MailServiceImpl` and `OosmMailComposer` inject `DynamicMailSettings` instead of `OosmMailProperties`.

#### Phase C — migrate remaining consumers per rollout phase

```text
Phase 4: notifications, QR
Phase 5: CORS, frontend URL, logging
Do not migrate forbidden/read-only keys.
```

#### What stays env-only forever

```text
DB_*, JWT_SECRET, APP_SETTINGS_ENCRYPTION_KEY, PORT, RENDER_EXTERNAL_URL
```

## API Design

Base path:

```text
/api/admin/settings
```

All endpoints require authentication. Permission checks are enforced per endpoint and per setting.

Endpoints:

```http
GET    /api/admin/settings
GET    /api/admin/settings/{key}
PUT    /api/admin/settings/{key}
POST   /api/admin/settings/{key}/rotate-secret
POST   /api/admin/settings/reload
POST   /api/admin/settings/mail/test
GET    /api/admin/settings/audit
GET    /api/admin/settings/status
```

### GET /api/admin/settings

Returns grouped settings. Secret values are always null.

```json
{
  "categories": [
    {
      "key": "MAIL",
      "label": "Mail",
      "settings": [
        {
          "key": "MAIL_FROM_ADDRESS",
          "value": "noreply@x-dev.pro",
          "configured": true,
          "sensitive": false,
          "restartRequired": false,
          "reloadable": true,
          "editable": true,
          "valueType": "EMAIL",
          "source": "DATABASE",
          "lastUpdatedAt": "2026-07-07T16:00:00Z",
          "lastUpdatedBy": "oosmAdmin"
        },
        {
          "key": "RESEND_API_KEY",
          "value": null,
          "configured": true,
          "sensitive": true,
          "restartRequired": false,
          "reloadable": true,
          "editable": true,
          "valueType": "SECRET",
          "source": "ENV"
        }
      ]
    }
  ]
}
```

### PUT /api/admin/settings/{key}

Request:

```json
{
  "value": "noreply@x-dev.pro",
  "reason": "Use verified Resend sender domain"
}
```

Rules:

```text
Reject if key is unknown (404).
Reject if key is sensitive (400 — use rotate-secret).
Reject if key is not editable (403).
Reject if user lacks permission (403).
Reject if validation fails (400).
Reject if restart-required and confirmRestart != true (409 with restartRequired flag).
Write audit on success and failure.
Invalidate local cache; bump settings_version if meta table exists.
```

### POST /api/admin/settings/{key}/rotate-secret

Request:

```json
{
  "value": "re_xxxxxxxxx",
  "reason": "Rotate Resend API key"
}
```

Response:

```json
{
  "key": "RESEND_API_KEY",
  "configured": true,
  "sensitive": true,
  "value": null,
  "source": "DATABASE"
}
```

Rules:

```text
Only works for SECRET type settings.
Never echoes the submitted value.
Validate prefix/length before encrypt.
Encrypt before save; plain value column must remain null.
Audit as ROTATE_SECRET.
Rate limit: 10 rotations per user per day.
```

### POST /api/admin/settings/mail/test

Request:

```json
{
  "to": "admin@example.com"
}
```

Response:

```json
{
  "success": true,
  "provider": "RESEND",
  "messageId": "a1b2c3d4-..."
}
```

Rules:

```text
Requires ADMIN_SETTINGS_TEST.
Recipient must be a valid email; no CC/BCC/subject override from client.
Sends a fixed "OOSM mail test" template using current effective settings.
Rate limit: 5 requests per user per hour.
Audit recipient, success/failure, and messageId when present.
Does not expose API key or from-address in error messages.
```

### GET /api/admin/settings/status

Diagnostics for admins. No secret values.

```json
{
  "settingsVersion": 42,
  "lastReloadAt": "2026-07-07T16:05:00Z",
  "sourceCounts": { "DATABASE": 8, "ENV": 3, "DEFAULT": 2 },
  "features": {
    "mail": {
      "enabled": true,
      "configured": true,
      "provider": "RESEND",
      "missingKeys": []
    }
  },
  "missingFeatureRequired": [],
  "invalidSettings": []
}
```

## Rate Limiting

Implement with in-memory bucket per user (v1). Return `429 Too Many Requests` when exceeded.

| Action | Limit |
|--------|-------|
| `POST .../rotate-secret` | 10 / user / day |
| `POST .../mail/test` | 5 / user / hour |
| `POST .../notifications/test` (Phase 4) | 5 / user / hour |
| `PUT .../{key}` (any setting) | 60 / user / hour |
| `POST .../reload` | 10 / user / hour |

## Permissions

Add permission actions (seed on startup when permission table is empty):

```text
ADMIN_SETTINGS_READ
ADMIN_SETTINGS_UPDATE
ADMIN_SETTINGS_SECRET_ROTATE
ADMIN_SETTINGS_AUDIT_READ
ADMIN_SETTINGS_RELOAD
ADMIN_SETTINGS_TEST
```

Default access:

```text
OOSMADMIN: all ADMIN_SETTINGS_* permissions
Other admin roles: ADMIN_SETTINGS_READ only unless explicitly granted
Regular users: no access
```

Per-setting permission overrides (in definition registry):

```text
Default non-secret update: ADMIN_SETTINGS_UPDATE
Default secret rotate: ADMIN_SETTINGS_SECRET_ROTATE
Audit read: ADMIN_SETTINGS_AUDIT_READ
Reload: ADMIN_SETTINGS_RELOAD
Test actions: ADMIN_SETTINGS_TEST
```

Backend enforcement:

```text
Use method-level @PreAuthorize or equivalent permission checks.
Do not rely on frontend route hiding.
Return 403 before validation when permission is missing (and audit PERMISSION_DENIED).
```

## Frontend Administration UI

### Navigation

```text
Administration -> Settings
```

Sections:

```text
Mail
Frontend and CORS
Notifications
QR
Boot Diagnostics (read-only)
Logging and Diagnostics
Audit Log
```

Remove editable "Security Bootstrap" section — replaced by read-only Boot Diagnostics.

### UI Rules

```text
Never display secret values.
Show configured/missing badge for secrets.
Use "Rotate secret" dialog for secrets; clear input after submit.
Require typed confirmation for restart-required changes.
Show validation errors client-side where possible; always show backend errors.
Show last updated by, timestamp, and effective source (DB / env / default).
Link to filtered audit log per setting.
Disable controls when user lacks permission (hide rotate button, not just disable API).
Never persist secrets in localStorage, sessionStorage, or URL query params.
```

### Mail Settings UI

Fields:

```text
MAIL_ENABLED: toggle
MAIL_FROM_ADDRESS: email input
MAIL_FROM_NAME: text input
MAIL_SUPPORT_EMAIL: email input
RESEND_API_KEY: configured/missing badge + rotate button
Mail delivery status: enabled/disabled/misconfigured (from /status)
Test email: send test message (recipient input, default to current user email)
```

### Mail Validation

**v1 (Phase 2):**

```text
MAIL_FROM_ADDRESS: valid EMAIL format.
Optional domain suffix allowlist configured in registry (e.g. x-dev.pro, onrender.com).
MAIL_SUPPORT_EMAIL: valid EMAIL format.
RESEND_API_KEY: non-empty secrets must start with "re_".
```

**v1.1 (future):**

```text
On save or test, call Resend Domains API to verify sender domain is verified.
Surface clear error: "Domain not verified in Resend."
```

## Mail System Migration

Current Resend config resolves as:

```text
RESEND_API_KEY      -> encrypted DB secret | RESEND_API_KEY env | missing
MAIL_FROM_ADDRESS   -> DB | MAIL_FROM_ADDRESS env | empty
MAIL_FROM_NAME      -> DB | MAIL_FROM_NAME env | OOSM
MAIL_SUPPORT_EMAIL  -> DB | MAIL_SUPPORT_EMAIL env | MAIL_FROM_ADDRESS
MAIL_ENABLED        -> DB | MAIL_ENABLED env | true
```

Delivery enabled only when:

```text
MAIL_ENABLED=true
RESEND_API_KEY is present
MAIL_FROM_ADDRESS is present
```

If not enabled:

```text
Log INFO with missing key names (never log secret values).
Do not throw for optional notifications (welcome email, password reset).
Throw MailDeliveryException only when caller requires guaranteed delivery.
When mail disabled, UserService continues returning initialPassword in API response.
```

## Runtime Reload Strategy

### Cache

```text
In-memory cache per instance.
TTL: 30 seconds (safety net).
Immediate invalidation after successful PUT, rotate-secret, or /reload.
Optional: poll app_setting_meta.settings_version every 30s for multi-instance.
```

### Reloadable settings (no restart)

```text
MAIL_ENABLED
MAIL_FROM_ADDRESS
MAIL_FROM_NAME
MAIL_SUPPORT_EMAIL
RESEND_API_KEY
FCM_PRIVATE_KEY
FCM_PROJECT_ID
QR_BASE_URL
MAIL_DEBUG
SEARCH_DEBUG_ON_STARTUP
LOG_LEVEL_WEB (requires LoggingSystem refresh implementation)
LOG_LEVEL_REST (requires LoggingSystem refresh implementation)
```

### Restart-required settings

```text
APP_CORS_ALLOWED_ORIGIN_PATTERNS (until CorsConfigurationSource is dynamic)
FRONTEND_ENTRY_POINT (used in security redirect and mail template URLs at bean init in v1)
OAUTH2_CLIENT_SECRET (OAuth client registration bean)
HIBERNATE_DDL_AUTO
All forbidden infrastructure keys
```

Phase 5 decision: implement dynamic `CorsConfigurationSource` reading from `AppSettingsService` to move CORS to reloadable. Until then, `restartRequired=true` with UI confirmation dialog.

## Secret Handling

```text
Secret fields are write-only.
Secret fields use encrypted_value only; value column must remain null.
API responses: value=null, configured=true|false.
Logs never include secret values (use structured logging with redaction).
Audit never includes secret values.
Validation error messages never echo the submitted secret.
Frontend clears secret input immediately after successful rotate.
Backend zeroes char[]/String references where practical after encrypt (best effort).
```

## Render Integration Policy

### Allowed

```text
Read fallback values from process environment at runtime.
Document required Render env vars in RENDER_DEPLOYMENT.md.
Expose configured/missing status in /api/admin/settings/status.
Keep RESEND_API_KEY in Render as bootstrap fallback until DB secret is set.
```

### Forbidden

```text
Calling Render API from backend for settings updates.
Storing Render API token in the app.
Browser calling Render API.
Showing Render env secret values in admin UI or API.
Syncing DB settings back to Render automatically.
```

### Render env after Phase 6 (target minimal surface)

```text
DB_URL
DB_USER
DB_PASS
APP_SETTINGS_ENCRYPTION_KEY
JWT_SECRET
PORT (platform-provided)
Optional bootstrap fallbacks until admin UI is configured:
  RESEND_API_KEY
  MAIL_FROM_ADDRESS
  FRONTEND_ENTRY_POINT
```

## Observability

`GET /api/admin/settings/status` and actuator info (no secrets):

```text
settingsVersion
sourceCounts: DATABASE / ENV / DEFAULT
missingFeatureRequired keys
invalidSettings keys
lastReloadAt
feature status: mail, notifications
configured secret count (not values)
```

Never include secret values in diagnostics, logs, or actuator output.

## Rollout Plan

### Phase 1: Backend Foundation

Deliverables:

```text
app_setting, app_setting_audit, app_setting_meta tables
AppSettingDefinitionRegistry
AppSettingsService with env fallback
AppSettingEncryptionService (AES-GCM v1)
AppSettingAuditService
Admin settings REST API (read, update, rotate, reload, status, audit)
ADMIN_SETTINGS_* permissions seeded
Unit tests: validation, masking, encryption, fallback order, permissions
```

Acceptance:

```text
Unknown keys rejected with 404.
Sensitive values never returned in JSON.
Audit row for every success and failure.
Env fallback works when DB row is absent.
```

### Phase 2: Mail Settings

Deliverables:

```text
DynamicMailSettings consumer (replaces OosmMailProperties for runtime reads)
Registry entries: MAIL_*, RESEND_API_KEY
POST /api/admin/settings/mail/test
Mail section in admin UI
Migrate MailServiceImpl to read from AppSettingsService
```

Acceptance:

```text
Mail sends via Resend using DB-stored settings.
Render env fallback still works when DB is empty.
Missing sender or API key disables delivery without boot failure.
Secret rotation never returns key in response.
Test email rate-limited and audited.
```

### Phase 3: Admin UI Generalization

Deliverables:

```text
Settings dashboard with grouped categories
Validation and error states
Restart-required confirmation dialog
Audit log viewer with filters (key, actor, date, success)
Permission-aware controls
Boot Diagnostics read-only section
```

Acceptance:

```text
Non-admin receives 403 on all write endpoints.
UI hides edit/rotate controls without permission.
Audit shows actor, key, masked values, result, reason.
```

### Phase 4: Notifications And QR

Deliverables:

```text
FCM_PROJECT_ID, FCM_PRIVATE_KEY
QR_BASE_URL
Dynamic notification settings consumer
Notification test endpoint (rate-limited)
```

Acceptance:

```text
FCM private key is write-only and encrypted.
QR URL validation rejects invalid URLs.
```

### Phase 5: CORS, Frontend, Diagnostics

Deliverables:

```text
FRONTEND_ENTRY_POINT (restartRequired=true in v1)
APP_CORS_ALLOWED_ORIGIN_PATTERNS
LOG_LEVEL_WEB, LOG_LEVEL_REST (with LoggingSystem refresh)
SEARCH_DEBUG_ON_STARTUP, HEALTH_SHOW_DETAILS
Optional: dynamic CorsConfigurationSource (moves CORS to reloadable)
```

Acceptance:

```text
CORS wildcard * rejected.
FRONTEND_ENTRY_POINT requires https in production validation.
Health detail setting cannot expose secrets publicly.
Logging refresh applied without restart when implemented.
```

### Phase 6: Reduce Render Env Surface

After DB settings are stable in production:

```text
Document that mail, QR, notifications, CORS can be removed from Render env.
Keep only infrastructure bootstrap vars (see Render Integration Policy).
Runbook: migrate env values into admin UI, verify /status, remove env keys, redeploy.
```

## Test Plan

### Unit Tests

```text
Definition registry rejects unknown keys.
Email, URL, enum, CSV validation.
Wildcard CORS rejected.
Sensitive values masked in DTOs.
Encrypt/decrypt round-trip with AAD.
Fallback order: DB > env > default.
Permission denied audited.
requiredForFeature does not block application context startup.
```

### Integration Tests

```text
GET /settings as admin returns non-secret values.
GET /settings as non-admin returns 403.
PUT updates DB, invalidates cache, writes audit.
PUT on secret key returns 400.
POST rotate-secret stores encrypted value; GET never returns it.
Mail test returns 503 when Resend not configured.
Mail test succeeds with mocked Resend client.
Rate limit returns 429 on excess test emails.
```

### Security Tests

```text
Secret never in JSON response body.
Secret never in logs (grep test with test key).
Secret never in audit old_value_masked / new_value_masked.
User without ADMIN_SETTINGS_SECRET_ROTATE gets 403 on rotate.
Read-only bootstrap keys return 403 on PUT.
CSRF and auth policies protect admin endpoints.
```

## Initial Setting Definitions

```text
MAIL_ENABLED
  type=BOOLEAN, category=MAIL, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=false, default=true, permission=ADMIN_SETTINGS_UPDATE

MAIL_FROM_ADDRESS
  type=EMAIL, category=MAIL, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=true, permission=ADMIN_SETTINGS_UPDATE

MAIL_FROM_NAME
  type=STRING, category=MAIL, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=false, default=OOSM, permission=ADMIN_SETTINGS_UPDATE

MAIL_SUPPORT_EMAIL
  type=EMAIL, category=MAIL, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=false, permission=ADMIN_SETTINGS_UPDATE

RESEND_API_KEY
  type=SECRET, category=MAIL, sensitive=true, reloadable=true, restartRequired=false
  requiredForFeature=true, validationPrefix=re_, permission=ADMIN_SETTINGS_SECRET_ROTATE

FRONTEND_ENTRY_POINT
  type=URL, category=FRONTEND, sensitive=false, reloadable=false, restartRequired=true
  requiredForFeature=false, permission=ADMIN_SETTINGS_UPDATE
  validation=https-only in production profile

APP_CORS_ALLOWED_ORIGIN_PATTERNS
  type=CSV, category=SECURITY, sensitive=false, reloadable=false, restartRequired=true
  requiredForFeature=false, permission=ADMIN_SETTINGS_UPDATE
  validation=no-wildcard-star

QR_BASE_URL
  type=URL, category=QR, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=false, permission=ADMIN_SETTINGS_UPDATE

FCM_PROJECT_ID
  type=URL, category=NOTIFICATIONS, sensitive=false, reloadable=true, restartRequired=false
  requiredForFeature=false, default=(FCM HTTP v1 via service account)
  permission=ADMIN_SETTINGS_UPDATE

FCM_PRIVATE_KEY
  type=SECRET, category=NOTIFICATIONS, sensitive=true, reloadable=true, restartRequired=false
  requiredForFeature=true, permission=ADMIN_SETTINGS_SECRET_ROTATE

OAUTH2_CLIENT_SECRET
  type=SECRET, category=INTEGRATIONS, sensitive=true, reloadable=true, restartRequired=true
  requiredForFeature=true, permission=ADMIN_SETTINGS_SECRET_ROTATE
  note=Prefer Render env in v1; UI rotate available but requires restart

SEARCH_DEBUG_ON_STARTUP
  type=BOOLEAN, category=DIAGNOSTICS, sensitive=false, reloadable=true, restartRequired=false
  default=false, permission=ADMIN_SETTINGS_UPDATE

HEALTH_SHOW_DETAILS
  type=ENUM, category=DIAGNOSTICS, allowed=[never, when_authorized, always]
  sensitive=false, reloadable=true, restartRequired=false, default=never
  permission=ADMIN_SETTINGS_UPDATE

LOG_LEVEL_WEB
  type=ENUM, category=DIAGNOSTICS, allowed=[TRACE, DEBUG, INFO, WARN, ERROR]
  sensitive=false, reloadable=true, restartRequired=false, default=INFO
  permission=ADMIN_SETTINGS_UPDATE

LOG_LEVEL_REST
  type=ENUM, category=DIAGNOSTICS, allowed=[TRACE, DEBUG, INFO, WARN, ERROR]
  sensitive=false, reloadable=true, restartRequired=false, default=WARN
  permission=ADMIN_SETTINGS_UPDATE
```

## Definition Of Done

The feature is complete when:

```text
Admins can manage mail settings from the UI without editing Render env vars.
Secrets are write-only, encrypted at rest, and never returned by API.
All change attempts are audited with actor, reason, and masked values.
Unauthorized users cannot read or mutate settings via API.
Runtime mail reads from AppSettingsService (not static @Value).
Render env remains fallback and infrastructure bootstrap layer.
Mail test works from UI with rate limiting.
Boot diagnostics show read-only bootstrap state.
Backend compiles and all unit/integration/security tests pass.
RENDER_DEPLOYMENT.md lists remaining Render-only variables and encryption key setup.
```
