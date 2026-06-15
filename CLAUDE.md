# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

Use the cheapest command that validates the change. Validation order:

```bash
./gradlew :app:compileDebugKotlin --quiet
./gradlew :app:testDebugUnitTest --quiet
./gradlew :app:assembleDebug --quiet
./gradlew :app:lintDebug --quiet
```

Run a single test class:
```bash
./gradlew :app:testDebugUnitTest --quiet --tests "com.elg.myges.adapters.secondary.api.JsonParsingTest"
```

Run instrumented tests (device/emulator required):
```bash
./gradlew :app:connectedDebugAndroidTest --quiet
```

Release APK (requires keystore env vars — see below):
```bash
./gradlew :app:assembleRelease --quiet
```

**Never run** `clean` as a default fix. Never run bare `./gradlew build` or `./gradlew test`.

## Architecture

Hexagonal (ports & adapters) layered architecture. Single `:app` module under `app/src/main/java/com/elg/myges/`:

```
domain/model/       — pure Kotlin data classes, no Android APIs
application/
  ports/Ports.kt    — interfaces: SessionRepository, StudentDataRepository, SettingsRepository,
                      NetworkMonitor, CalendarSyncPort, NotificationScheduler
  usecase/          — one class per use case, injected via @Inject constructor
adapters/primary/
  ui/               — Jetpack Compose screens and reusable components
  viewmodel/        — ViewModels consuming use cases, emitting UiState flows
  state/UiState.kt  — sealed UI state types
adapters/secondary/
  api/              — Retrofit service + OkHttp interceptors + JSON parsing
  repository/       — OfflineFirstStudentDataRepository, MygesSessionRepository
  storage/          — Room database (SQLCipher-encrypted), DAOs, entity mappers
  settings/         — Jetpack DataStore preferences
  security/         — Android Keystore AES/GCM session encryption
  notification/     — WorkManager worker, notification channels, local alerts
  calendar/         — CalendarContract sync adapter
config/
  DependencyModule  — Hilt @Provides + @Binds wiring all port interfaces to adapters
```

**Dependency rule**: `domain` must not import Android APIs or adapters. Business logic stays in `domain`/`application`. UI in `adapters/primary`. Infrastructure in `adapters/secondary`.

## Key Wiring

- **DI**: Dagger Hilt (`@HiltAndroidApp` on `MygesApplication`). `PortBindingModule` binds interfaces; `DependencyModule` provides singletons.
- **API**: Retrofit + Kotlinx Serialization. Auth via `MygesAuthInterceptor` (raw Kordis token + User-Agent). Retry via `ApiRetryInterceptor` (exponential backoff for 429/503).
- **Database**: Room with SQLCipher. Passphrase from Android Keystore (`DatabasePassphraseStore`). Schema exports to `app/schemas/`.
- **Session**: OAuth token stored encrypted in SharedPreferences via `SecureSessionStore` (AES/GCM). Session refresh triggered proactively at J+5; 401 invalidates immediately.
- **Background sync**: `StudentSyncWorker` via WorkManager, every 6 hours, network-constrained.
- **Navigation**: `NavHost` with `ModalNavigationDrawer` in `MygesApp.kt`.

## Build Config / Secrets

Build config fields read from Gradle properties or environment variables:

| Env var | Default |
|---|---|
| `MYGES_API_BASE_URL` | `https://api.kordis.fr/` |
| `KORDIS_OAUTH_AUTHORIZE_URL` | *(Kordis OAuth URL)* |
| `KORDIS_OAUTH_REDIRECT_URI` | `comreseaugesskolae:/oauth2redirect` |
| `MYGES_USER_AGENT` | `MyGES Android` |
| `MYGES_RELEASE_STORE_FILE` | *(required for signed release)* |
| `MYGES_RELEASE_STORE_PASSWORD` | |
| `MYGES_RELEASE_KEY_ALIAS` | |
| `MYGES_RELEASE_KEY_PASSWORD` | |

## Testing Stack

- Unit tests: JUnit + MockK + Turbine (for Flow assertions)
- Instrumented tests: Compose UI Test + Espresso
- Add tests when touching: ViewModels, repositories, use cases, JSON parsing, persistence, sync logic

## Code Conventions

- Kotlin idiomatic style, Java 21, 4-space indent
- All UI strings in `res/values/strings.xml` (FR) and `res/values-en/strings.xml` (EN) — no hardcoded text
- Room schema changes require a migration and bump `MygesDatabase` version
- Completion output format (when a task is done): `STATUS: SUCCESS` followed by ≤3 bullet points, ≤12 words each
