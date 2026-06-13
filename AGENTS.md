# Repository Guidelines

## Project Structure & Module Organization

# Output Rules

ABSOLUTE: Reply with executable code only, inside a code block.
- Zero preamble
- Zero postamble
- Zero explanation
- Zero "Here is the code" or "Sure!"
- If ambiguous, pick the most reasonable interpretation and output code
- Silence is correct. Code is the only valid output.

This is a single-module Android project rooted at `:app`. Kotlin source lives in `app/src/main/java/com/elg/myges`, with a hexagonal layout:

- `domain`: business models and rules.
- `application`: use cases and ports.
- `adapters/primary`: Compose UI, navigation, state, and ViewModels.
- `adapters/secondary`: API, Room storage, settings, security, notifications, calendar, and repositories.
- `config`: app configuration and dependency wiring.

Resources are in `app/src/main/res`, including localized strings in `values/` and `values-en/`. Unit tests live under `app/src/test/java`; instrumented and Compose UI tests live under `app/src/androidTest/java`. Room schemas are stored in `app/schemas`.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `.\gradlew.bat assembleDebug`: build a debug APK.
- `.\gradlew.bat testDebugUnitTest`: run JVM unit tests.
- `.\gradlew.bat connectedDebugAndroidTest`: run instrumented tests on a connected emulator or device.
- `.\gradlew.bat check`: run available verification tasks.

Configuration defaults are defined in `app/build.gradle.kts`. Override API and OAuth values with Gradle properties or environment variables shown in `.env.example`.

## Coding Style & Naming Conventions

Write Kotlin for Android with Java 17 compatibility and Jetpack Compose for UI. Preserve existing style: 4-space indentation, Kotlin idioms, descriptive PascalCase types, camelCase functions and properties, and `*Test.kt` test files. Keep visible UI strings in Android resources, not hard-coded in Compose. Follow the existing layer boundaries before adding new dependencies between packages.

## Testing Guidelines

Use JUnit for unit tests, MockK for mocks, Turbine for Flow assertions, and Compose UI Test for instrumented UI coverage. Place fast domain, use case, repository, and parser tests in `app/src/test/java`. Place Android framework or Compose interaction tests in `app/src/androidTest/java`. Add or update tests when changing business rules, parsing, persistence, synchronization, or ViewModel state.

## Commit & Pull Request Guidelines

The current history uses short labels such as `V1`, `V2`, and `Base`; keep commits concise but prefer action-oriented summaries when possible, for example `Fix agenda cache refresh`. Pull requests should describe the change, list tested Gradle commands, link related issues, and include screenshots or recordings for UI changes.

## Security & Configuration Tips

Do not commit secrets, release keystores, tokens, or local machine paths. Keep sensitive runtime values in environment variables, Gradle properties, or ignored local files. Avoid logging personal academic data, OAuth tokens, or API responses containing student information.
