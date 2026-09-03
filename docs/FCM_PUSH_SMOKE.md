# FCM push smoke test

After deploying backend + frontend with the FCM send path:

## 1. Backend credentials (Admin → Settings → Notifications)

Set and save:

- `FCM_PROJECT_ID` — Firebase project id
- `FCM_CLIENT_EMAIL` — service account email
- `FCM_PRIVATE_KEY` — PEM private key (write-only secret)

Status for Notifications should become ready when all three are present.

Optional env overrides (same keys) work if set on Railway/Render before Admin Settings.

## 2. Frontend web config

Fill public Firebase web config in either:

- `osm-ms-fe/src/assets/firebase-config.json`, or
- `environment.ts` / `environment.prod.ts` → `firebase: { apiKey, authDomain, projectId, messagingSenderId, appId, vapidKey }`

`vapidKey` is the Web Push certificate key pair from Firebase Console → Project settings → Cloud Messaging.

Ensure `firebase-messaging-sw.js` is served at site root (already listed in `angular.json` assets).

## 3. Register a device token

1. Log in on HTTPS (or localhost).
2. Allow browser notification permission when prompted.
3. Confirm `POST /api/security/user/register-device` with `{ userId, token }` succeeds.
4. Confirm `oosmuser.fcm_token` is populated for that user.

## 4. Admin test push

1. Admin → Settings → Notifications → **Send test notification**.
2. Paste the FCM device token from the register-device request (or from the DB).
3. Expect success and a browser push.

Missing server credentials return `FCM_NOT_CONFIGURED`.

## 5. Domain event push

Trigger a flow with `pushEnabled` (e.g. leave approval) and confirm:

- in-app `user_notification` row is still created
- FCM delivery reaches the registered browser

## DB note

If upgrading an existing DB that still has `one_signal_player_id`, run:

`app/src/main/resources/db/fcm-token-rename.sql`
