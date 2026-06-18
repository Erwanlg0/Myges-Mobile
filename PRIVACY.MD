# Privacy Policy — Studly

_Effective date: 2026-06-18_

Studly ("the app") is an **independent, unofficial** Android client for MyGES.
It is **not affiliated with, endorsed by, or operated by** Réseau GES, MyGES,
Kordis, or Skolae. This policy describes exactly what the app does with your
data. It is intentionally specific: where this policy and the app disagree, the
app's actual behaviour (visible in the open-source code) prevails.

**Data controller:** Erwan (project author) — erwan.luce.guedon@gmail.com
**Source code:** https://github.com/Erwanlg0/Myges-Mobile

## Short version

- The app shows **your own MyGES data** by talking directly to the Kordis API
  with your account.
- That data is stored **only on your device**, in an **encrypted** database, and
  your session token is stored **encrypted** via the Android Keystore.
- **No analytics, no advertising, no tracking, no profiling, no AI**, and **no
  developer-operated server.** Your academic data is never sent to the author or
  to any third party.

## What the app accesses

When you sign in, the app reads the following from your MyGES/Kordis account, to
display it to you:

- Profile: name, email, school, programme, academic year, avatar.
- Schedule, grades, absences, courses, syllabi and documents, projects and
  practicals, news.

It also processes, only on your device:

- Your authentication token (to call the API on your behalf).
- Optional, locally-stored preferences such as grade simulations and settings.

## Where your data is stored

- **On your device only.** Academic data is cached in a local database
  encrypted with SQLCipher (AES-256). The key is generated on-device and
  protected by the Android Keystore.
- **Session token** is stored encrypted (AES/GCM via the Android Keystore) in
  private app storage.
- **Backups are disabled** (`allowBackup=false`); the database and preferences
  are excluded from Android cloud backup and device-to-device transfer.
- Downloaded documents are kept in the app's private cache and purged
  automatically.

## Network connections

The app connects only to:

- `api.kordis.fr` and `authentication.kordis.fr` — to authenticate and fetch
  **your** data. Traffic is TLS-only (cleartext disabled) with certificate
  pinning.
- Google Play services — only for the optional "in-app update" and "rate the
  app" features.

Calendar export and document opening happen locally through Android; no data is
sent to the author.

## Authentication

Sign-in uses MyGES/Kordis OAuth in your browser. The app never sees your MyGES
password — it only receives and stores an access token.

## Permissions

- **Calendar (read/write):** only to optionally sync your schedule to your
  device calendar, at your request.
- **Notifications:** to alert you about new grades, absences, schedule changes,
  documents, and deadline reminders.
- **Biometric:** to optionally lock/unlock the app.

You can deny or revoke any of these in Android settings; the related feature is
simply disabled.

## Crash diagnostics (optional, off by default)

The app includes an optional integration with the Sentry crash-reporting SDK.
It is **disabled unless a Sentry DSN is configured at build time** (it is empty
by default). When enabled by the publisher, crash diagnostics (stack traces,
device model, OS version, app version) are sent to Sentry to fix bugs; no
academic data is included. If you obtained the app from a store listing that
declares crash reporting, this is the feature concerned.

## Data retention and deletion

Because your data lives only on your device:

- **Sign out** or use **Clear cache** in the app to remove the local copy.
- **Uninstalling** the app removes all local data.
- The app does not control data held by MyGES/Kordis; to access, correct, or
  delete your data **at the source**, use MyGES or contact Réseau GES.

## Children

The app is not directed at children under 16. It does not knowingly collect data
from children.

## Your rights (GDPR / RGPD)

You may request access to, correction of, or deletion of any personal data the
author holds — in practice the author holds **none**, since all processing is
local and sourced live from Kordis. For source data, contact your school /
Réseau GES. For questions about the app, contact erwan.luce.guedon@gmail.com.

## Changes

This policy may be updated; the effective date above will change and the updated
version will be published in the project repository.

## Contact

erwan.luce.guedon@gmail.com
