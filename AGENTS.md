# Repository Guidelines

## Output Rules

ABSOLUTE: Reply with executable code only, inside a code block.

* Zero preamble
* Zero postamble
* Zero explanation
* Zero "Here is the code" or "Sure!"
* If ambiguous, pick the most reasonable interpretation and output code
* Silence is correct. Code is the only valid output

---

## Android / Gradle Rules

Never run:

```bash
./gradlew build
./gradlew clean
./gradlew clean build
./gradlew test
./gradlew connectedCheck
```

unless explicitly requested.

Prefer:

```bash
./gradlew :app:assembleDebug --quiet
./gradlew :app:testDebugUnitTest --quiet
./gradlew :app:compileDebugKotlin --quiet
./gradlew :app:lintDebug --quiet
```

Use the cheapest command that validates the change.

Validation order:

```bash
./gradlew :app:compileDebugKotlin --quiet
./gradlew :app:testDebugUnitTest --quiet
./gradlew :app:assembleDebug --quiet
./gradlew :app:lintDebug --quiet
```

Never use `clean` as a default fix.

---

## Command Output Rules

Never paste complete Gradle logs.

Return only:

* failing task
* exception type
* root cause
* relevant stack trace excerpt (max 30 lines)
* proposed fix
* next command

When a command fails:

* stop at the first useful error
* do not dump the full output
* do not retry the same command repeatedly

Use:

```bash
--quiet
```

by default.

Use:

```bash
--stacktrace
```

only if the root cause is not visible.

---

## File Reading Rules

Always search before reading files.

Prefer:

```bash
rg "keyword" app/src
```

over reading directories.

Never read entire files larger than 500 lines.

Read only the relevant section.

Avoid:

```text
.gradle
build
app/build
.idea
*.apk
*.aab
*.jar
```

unless explicitly required.

---

## Token Optimization Rules

Keep answers short.

Do not:

* explain obvious code
* summarize entire files
* dump generated code
* dump logs
* dump dependency trees

Return only the files that need modification.

Prefer minimal diffs.

Prefer targeted edits.

Do not read unrelated files.

Do not inspect the whole repository when a search is sufficient.

---

## Architecture

Project root:

```text
:app
```

Source:

```text
app/src/main/java/com/elg/myges
```

Layers:

```text
domain
application
adapters/primary
adapters/secondary
config
```

Rules:

* domain must not depend on Android APIs
* domain must not depend on adapters
* business logic belongs in domain/application
* Compose UI belongs in adapters/primary
* repositories, Room, APIs and services belong in adapters/secondary

Respect existing architecture.

---

## Kotlin Rules

* Kotlin idiomatic style
* Java 21 compatibility
* Jetpack Compose UI
* 4 spaces indentation
* PascalCase classes
* camelCase functions
* strings in resources
* avoid hardcoded UI text

---

## Testing

Use:

* JUnit
* MockK
* Turbine
* Compose UI Test

Prefer targeted tests:

```bash
./gradlew :app:testDebugUnitTest --quiet
```

Run instrumented tests only when necessary:

```bash
./gradlew :app:connectedDebugAndroidTest --quiet
```

Add tests when modifying:

* business rules
* repositories
* ViewModels
* synchronization
* parsing
* persistence

---

## Security

Never commit:

* API keys
* OAuth secrets
* tokens
* keystores
* local paths

Never log:

* personal student data
* OAuth tokens
* sensitive API responses

Use environment variables or Gradle properties for secrets.

---

## Preferred Workflow

1. Search first with ripgrep.
2. Read only necessary files.
3. Make the smallest possible change.
4. Compile targeted module.
5. Run targeted tests.
6. Return only the modified code.
7. Keep output minimal.


## Completion Output Rules

When the task is complete, output ONLY:

```text
STATUS: SUCCESS

<max 3 bullet points>
```

Example:

```text
STATUS: SUCCESS

- Added project progress calculation
- Updated project UI
- Added unit tests
```

Maximum output:

- 3 bullet points
- 12 words per bullet point
- No file names unless explicitly requested
- No code snippets
- No command output
- No test output
- No explanations
- No implementation details
- No metrics
- No reasoning
- No change statistics

Never output:

- "Implemented..."
- "Verified..."
- "I updated..."
- "I tested..."
- "The application now..."
- lists of modified files
- build results
- test summaries

If blocked, output ONLY:

```text
STATUS: FAILED

<single blocking reason>
```