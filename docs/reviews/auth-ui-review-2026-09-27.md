# Authentication flow and UI review

Reviewed 27 September 2026 against frontend commit `3f32f3f` and backend commit `86e7365`. This is a review and implementation specification; application code is unchanged.

## Scope and evidence

Reviewed login, first-login password creation, forgotten password, code verification, password reset, profile password changes, permission denial, and session refresh/logout. Traced Angular components, routing, guards, interceptors, translations, and Java response contracts.

Rendered the existing production bundle in isolated Chromium contexts with synthetic credentials and mocked HTTP responses. Checked French and Arabic at desktop width and reset/denied states at 390px. Blocked external resources; font/icon appearance in these captures is therefore not reliable evidence of production defects. Service workers were blocked for the final state reproductions. No real reset email was sent and no account password was changed. English dictionaries were inspected, but the English screens were not exercised in this review.

## Findings, ordered by priority

### 1. P1: Access Denied's logout action does not log out

`F:/osm-ms-fe/src/app/theme/layouts/access-denied/access-denied.component.html:23` routes directly to `/auth/login` instead of calling the component's existing `logout()` method. The login guard then redirects the authenticated user back into the application.

Reproduced: clicking “Se déconnecter” opened `/dashboard/overview`; the session token remained present. This breaks the user's explicit expectation that they have signed out.

Change: wire genuine logout; provide a primary dashboard action and a clear administrator-help explanation. Keep permission denial distinct from a locked account and an expired session. Do not display internal permission strings to ordinary users.

### 2. P1: Initial passwords are visible by default

`F:/osm-ms-fe/src/app/auth/update-password/update-password.component.ts:33` initializes both visibility flags to false while the template maps false to `type="text"`. Both new-password inputs rendered as text in French and Arabic. The separate `hideNewPassword`/`hideConfirmPassword` variables are unused by the template.

Change: mask both fields initially; use one pair of flags; give reveal controls localized accessible names and pressed states. Display password requirements before submission.

### 3. P1: Authentication failures can show no message, or leave login busy indefinitely

`F:/osm-ms-fe/src/app/auth/login/login.component.ts:138` stores the raw OAuth response, but `errorText` at line 46 only reads `message`. OAuth responses use `error` and `error_description`; `{error:"access_denied"}` rendered no error alert in the mocked browser check.

The successful-login path at line 154 also waits for `refreshSession().next()` or `.error()`. `F:/osm-ms-fe/src/app/auth/services/authentication.service.ts:298` catches refresh failures and returns `EMPTY`; several earlier exits do likewise. Completion without a value invokes neither handler, so the loading flag can remain true with credentials already persisted. This second issue is established by code tracing, not a browser reproduction.

Change: map known error codes into localized messages with a guaranteed fallback; finish loading on every completion path. Give session initialization an explicit result and retry behavior instead of silently completing. Do not render arbitrary backend objects or technical OAuth descriptions.

### 4. P1: Reset password mismatch silently disables submission

`F:/osm-ms-fe/src/app/auth/reset-confirm/reset-confirm.component.ts:41` puts mismatch on the form group. The Material field at `reset-confirm.component.html:92` uses the default control error state; the individual confirmation control can remain valid.

Reproduced with two different valid-length passwords: submit disabled, zero visible `mat-error` messages. Users cannot tell what must change.

Change: use a group-aware error-state matcher or an associated visible inline error, preserve control errors, and ensure keyboard/screen-reader users receive the same explanation.

### 5. P2: Translations are incomplete and error handling depends on the selected language

`FORGOT_PASSWORD.SUBTITLE` and `RESET_PASSWORD.SUBTITLE` are absent in all three dictionaries. Both raw keys appeared on French and Arabic screens. Reset validation helpers at `reset-confirm.component.ts:117` return English sentences. French login also says “Mot de pass”. Several messages are translated once with `instant`, so changing language can leave an already-visible message in the old language.

The `typeof err.error === translate.instant('AUTO.STRING')` checks in forgot/reset components compare JavaScript's literal `string` with `chaîne` in French and `خيط` in Arabic. Error parsing therefore behaves differently by locale.

Change: use ordinary type checks; store message keys and parameters; translate at render time. Cover all auth states with a shared FR/EN/AR namespace and a missing-key check.

### 6. P2: Password recovery has no clear recovery from failure

The reset component stores an `identifier` for resend but implements no resend action. There is no way back to code entry after moving to password entry, including when the code expires before password submission. An invalid initial-password context is only discovered on submit (`update-password.component.ts:116`) and has no sign-in action on that screen.

The reset form accepts 4–8 characters, while the backend generates six-digit codes. The email includes both a code and a link, but the request button only describes sending a link. The done state still uses the new-password heading. Return-to-login anchors in forgot/reset have click handlers without `href` or `routerLink`, leaving them outside normal keyboard link behavior.

Change: show explicit steps, six-digit instructions, resend/restart actions, expired-code recovery, and a dedicated completion state. Validate missing onboarding context when entering the screen. Implement actual links. Preserve no plaintext password in durable browser storage.

### 7. P2: Password policy differs across entry points

Initial-password and profile forms require uppercase, lowercase, number, and special character. Reset requires only eight characters. Backend `UpdatePasswordDTO` enforces 8–128 characters, but the frontend forms omit the upper bound. Thus a password accepted during recovery can be rejected by another password screen, and long input is accepted by the UI then rejected by the API.

Change: define one product password policy, enforce it on the server, and reuse its validation/messages on all three forms. Preserve compatibility for existing passwords at login. The UI must explain reuse rejection separately from expired reset authorization when the server can reliably distinguish them.

### 8. P2: Permission denial, account locking, and session expiry are conflated

`F:/osm-ms-fe/src/app/interceptors/error.interceptor.ts:50` interprets every 403 body with `error: access_denied` as a locked account and logs out. Ordinary expired-session paths log out without a reason. A failed refresh with network status 0 also clears the session through the generic failure path. The server currently uses `access_denied` for multiple login conditions, so the client cannot infer a precise reason from that code alone.

Change: reserve account-lock messaging for an explicit trusted account-state code. Keep a normal permission failure local to the page/action. Display a specific session-expired message when refresh credentials are invalid, and distinguish temporary connectivity failures. Preserve a validated internal return route for sign-in recovery.

### 9. P2: Accessibility and theme behavior are inconsistent

Auth panels use fixed light backgrounds and blur (`authentication.scss:179`) rather than adapting their surface to dark mode. Reduced-motion rules omit the panel/card `auth-rise` animations at lines 111 and 242; there is no reduced-transparency override in this stylesheet. Initial/profile reveal buttons lack accessible labels. Loading buttons replace their label with an unlabeled spinner.

Change: use the application's theme tokens; opaque surfaces under reduced transparency; disable all auth entrance/decorative animations under reduced motion. Retain an accessible busy label, associate inline errors with inputs, and move focus to the next step or error summary. Check RTL, keyboard traversal, and 390px layouts after implementation. Arabic direction switching itself worked in the tested screens.

## Backend response work needed for truthful UI

- Use stable, documented error codes for credential rejection, password-update-required, invalid/expired reset authorization, password policy/reuse, session expiry, and temporary failures. Avoid parsing English exception text.
- Public reset requests currently return a user DTO for an existing account and a 400 for an unknown account (`UserController.java:54`, `UserService.java:599`). A generic “If an account exists…” screen alone cannot hide this response difference. A uniform public response and opaque recovery identifier require coordinated backend/frontend changes.
- Code expiry currently depends on `lastModifiedDate + 10 minutes` (`ConfirmationCode.java:22`), and failed attempts update that entity. Use an explicit issue/expiry timestamp before promising a fixed countdown in the UI.
- Verify attempt-limit persistence, consumption under concurrent requests, and session revocation after password changes with integration tests. These are backend review follow-ups, not established as correct by this UI review.

## Replacement French messages

| State | Message | Visible action |
|---|---|---|
| Rejected sign-in | Connexion impossible. Vérifiez vos identifiants et réessayez. | Se connecter / Mot de passe oublié ? |
| Expired session | Votre session a expiré. Reconnectez-vous pour continuer. | Se reconnecter |
| Permission denied | Vous n’avez pas accès à cette page. Contactez l’administrateur de votre organisation si cet accès est nécessaire. | Retour au tableau de bord / Se déconnecter |
| Confirmed locked account | Votre compte est verrouillé. Contactez l’administrateur de votre organisation. | Retour à la connexion |
| Reset request accepted | Si un compte correspond à cette adresse, vous recevrez un e-mail contenant un code de réinitialisation. | Saisir le code / Renvoyer un code |
| Code rejected | Ce code est incorrect ou n’est plus valide. Vérifiez le dernier e-mail reçu ou demandez un nouveau code. | Réessayer / Renvoyer un code |
| Password mismatch | Les mots de passe ne correspondent pas. | Inline below confirmation |
| Initial password required | Créez votre mot de passe pour terminer l’activation de votre compte. | Créer mon mot de passe |
| Recovery complete | Votre mot de passe a été modifié. Connectez-vous avec votre nouveau mot de passe. | Se connecter |
| Connectivity failure | Connexion au service impossible. Vérifiez votre connexion et réessayez. | Réessayer |

Use equivalent reviewed English and Arabic messages. Only show confirmed states; a timeout must not claim that the password was changed or that an account is locked.

## Implementation sequence and acceptance

1. Fix the four P1 defects, add guaranteed localized fallbacks, and cover logout, masked inputs, failed sign-in, empty refresh completion, and mismatch visibility with regression tests.
2. Complete the reset/onboarding state model and align password policy with the server. Add typed error contracts and explicit expiry. Do not disable backend authorization to make the UI proceed.
3. Apply shared auth layout, message, busy-state, theme, accessibility, and translation rules to login, recovery, onboarding, profile password changes, and Access Denied.
4. Verify desktop/mobile FR/EN/AR, keyboard-only navigation, RTL, dark/light, reduced motion/transparency, invalid/expired/consumed codes, resend, missing context, double submission, server/network failure, session expiry, and permission denial. Then run an end-to-end flow using a dedicated test account and controlled mail delivery; mocked browser checks do not establish email delivery or real token revocation.
