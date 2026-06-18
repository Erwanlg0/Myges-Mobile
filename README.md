# Studly — Android client for MyGES

Studly is a native Android app that gives students a fast, offline-first view of
their MyGES data: schedule, grades, absences, courses, documents, projects and
news.

> **Unofficial project.** Studly is independent and **not affiliated with,
> endorsed by, or operated by** Réseau GES, MyGES, Kordis or Skolae. It talks to
> the Kordis API with your own account. Trademarks belong to their owners. Use at
> your own discretion and in accordance with your school's terms.

## Features

- **Dashboard** — profile, next class, latest grades, recent absences, upcoming
  deadlines, sync status.
- **Schedule** — day / week / list views, course details, optional sync to the
  device calendar, `.ics` export.
- **Grades & absences** — filter by year/period, coefficients and averages,
  local "what-if" grade simulation.
- **Courses, projects, practicals** — syllabi, files, groups, steps, deadlines.
- **Documents** — open/download annual and course documents.
- **Notifications** — new grade, absence, schedule change, document, deadline
  reminders (local, after background sync).
- **Offline-first** — encrypted local cache, works without network.
- French and English, light/dark, Material You dynamic color, biometric lock.

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Hilt · Retrofit + OkHttp +
kotlinx.serialization · Room + SQLCipher · DataStore · WorkManager · Coil ·
AndroidX Biometric · Sentry (optional, off by default) · Play in-app
update/review.

## Architecture

Hexagonal (ports & adapters); business logic is independent of UI, storage and
network.

```text
app/src/main/java/com/elg/studly/
├── domain/model/         # pure Kotlin models + business rules
├── application/          # ports (interfaces) + use cases
├── adapters/primary/     # Compose UI, navigation, ViewModels
├── adapters/secondary/   # API, Room/SQLCipher storage, security, notifications, calendar
└── config/               # Hilt wiring, app config, certificate pins
```

The MyGES endpoints consumed are declared in
[`MyGesApiService.kt`](app/src/main/java/com/elg/studly/adapters/secondary/api/MyGesApiService.kt).

## Build

```bash
./gradlew :app:compileDebugKotlin   # fastest check
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:assembleRelease      # signed release (needs keystore env vars)
```

Requirements: JDK 21, Android SDK (compileSdk 37, minSdk 26).

## Configuration

Build-config values come from Gradle properties or environment variables:

| Variable                       | Default                                                                                  |
| ------------------------------ | ---------------------------------------------------------------------------------------- |
| `MYGES_API_BASE_URL`           | `https://api.kordis.fr/`                                                                  |
| `KORDIS_OAUTH_AUTHORIZE_URL`   | Kordis OAuth authorize URL                                                                |
| `KORDIS_OAUTH_REDIRECT_URI`    | `comreseaugesskolae:/oauth2redirect`                                                      |
| `MYGES_USER_AGENT`             | `MyGES Android`                                                                           |
| `MYGES_SENTRY_DSN`             | _(empty — crash reporting disabled unless set)_                                          |
| `MYGES_RELEASE_STORE_FILE`     | _(required for a signed release)_                                                         |
| `MYGES_RELEASE_STORE_PASSWORD` |                                                                                          |
| `MYGES_RELEASE_KEY_ALIAS`      |                                                                                          |
| `MYGES_RELEASE_KEY_PASSWORD`   |                                                                                          |

## Security

- Local database encrypted with SQLCipher (AES-256); key held in the Android
  Keystore (AES-256-GCM).
- OAuth session token stored encrypted; the access token is only ever sent to
  the Kordis API host.
- TLS-only (cleartext disabled) with certificate pinning; backups disabled.

See [PRIVACY.MD](PRIVACY.MD) for what data is processed and stored.

## Tests

Unit: JUnit, MockK, Turbine. UI/instrumented: Compose UI Test, Espresso. A
parity test enforces matching French/English string keys.

## Publishing notes (Play Store)

When filling the Play **Data safety** form, declare it to match the app's real
behaviour:

- **Data collected/shared with third parties:** none (no analytics, no ads).
  Crash diagnostics are sent to Sentry **only if** `MYGES_SENTRY_DSN` is set —
  declare "Crash logs" in that case.
- **Personal/academic data** (identity, grades, schedule, etc.) is processed and
  stored **on-device only**.
- **Security:** data is encrypted in transit and at rest; users can request
  deletion (sign out / uninstall / clear cache).
- Provide the hosted **privacy policy URL** and include the **unofficial**
  disclaimer in the listing.

## License

[MIT](LICENSE) © 2026 Erwan (Erwanlg0). Third-party components and their
licenses are listed in [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md).
