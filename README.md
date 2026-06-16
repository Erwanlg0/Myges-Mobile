# Studly Android Application

This repository describes the functional and technical foundations of a native Android application for MyGES. The application consumes existing MyGES endpoints, provides a student-focused mobile experience, and maintains a clear architecture to simplify maintenance, testing, and future development.

## Objectives

* View schedules, grades, absences, courses, documents, and projects.
* Centralize useful student information in a native Android application.
* Use native Android components without WebView or embedded web interfaces.
* Provide reliable local caching and offline state management.
* Isolate business logic from the UI framework, local storage, and external services.
* Prepare the application for internationalization from the start.

## README Improvement Recommendations

* Clarify the status of this document: it is an initial technical specification, not yet a complete installation guide.
* Explicitly state that the project targets Android only.
* Remove multi-platform options and references to iOS, Flutter, or React Native if the application remains Android native.
* Clearly specify that the application must not be a web application in disguise: no WebView for core screens.
* Separate architecture choices, features, API documentation, and i18n to improve readability.
* Add an `Installation` section once source code, Gradle scripts, and dependencies are available.
* Add a `Configuration` section to document the API base URL, local secrets, build variants, and debug settings.
* Add a `Tests` section once testing conventions have been defined.
* Explicitly document the endpoints used, dynamic parameters, and their primary purposes.

## Native Android Principles

The application must be developed as a native Android application, not as a wrapped web interface.

* Native UI using Jetpack Compose or Android Views.
* Native Android navigation without web routing.
* Access to platform capabilities through Android APIs: secure storage, notifications, files, calendar, biometrics.
* API calls through a native HTTP client, not through a web page loaded inside the application.
* Local cache through an embedded Android database, not through `localStorage`, browser cookies, or web sessions.
* Error states, loading states, empty states, and offline states handled directly by native screens.

A WebView may be acceptable only for very specific use cases, such as displaying an external HTML document that cannot reasonably be reproduced natively. It must not be used to render the main application experience.

## Architecture

The application follows a hexagonal architecture adapted for Android. Business logic remains independent from the UI, local storage, HTTP client, and Android services.

```text
app/src/main/java/
├── domain/                    # Entities, value objects, and business rules
├── application/               # Use cases and ports
├── adapters/
│   ├── primary/               # Compose UI, navigation, ViewModels
│   └── secondary/             # API, storage, notifications, Android services
└── config/                    # Application configuration
```

### Main Layers

* `domain`: business models such as `Student`, `Grade`, `Absence`, `AgendaEvent`, `Project`, or `AcademicDocument`.
* `application`: use case orchestration, including authentication, synchronization, grade retrieval, and project management.
* `adapters/primary`: Android screens, Compose components, navigation, and ViewModels.
* `adapters/secondary`: MyGES API client, local cache, secure storage, notifications, and Android integrations.

This separation allows business rules to be tested independently from Android UI and external services.

## Recommended Technology Stack

* Language: Kotlin.
* UI: Jetpack Compose.
* UI Architecture: ViewModel with `StateFlow`.
* Dependency Injection: Hilt.
* HTTP Client: Retrofit and OkHttp.
* Serialization: Kotlinx Serialization or Moshi.
* Local Cache: Room.
* Preferences: DataStore.
* Secure Storage: Android Keystore and EncryptedSharedPreferences.
* Background Work: WorkManager.
* Notifications: Firebase Cloud Messaging and Android local notifications.
* Files: Storage Access Framework.
* Biometrics: AndroidX Biometric.
* Unit Testing: JUnit, MockK, Turbine.
* UI Testing: Compose UI Test.

## Planned Mobile Features

### Authentication

* Login to MyGES.
* Secure storage of authentication tokens.
* Optional biometric authentication after a successful first login.
* Explicit handling of session expiration, authentication, and network errors.

### Dashboard

* Student profile summary.
* Upcoming course.
* Latest grades.
* Recent absences.
* Projects and important deadlines.
* Synchronization and cache status.

### Schedule

* Course event consultation.
* Day, week, and list views.
* Course details including schedule, room, instructor, type, and modality.
* Optional synchronization with Android Calendar.

### Grades and Absences

* View grades by academic year, period, or subject.
* Display coefficients and averages when the required data is available.
* View absences with justification status.
* Access information useful for attendance tracking.

### Courses, Projects, and Practicals

* List of courses by academic year.
* Access to syllabi and associated files.
* List of projects and practical work.
* View groups, steps, files, and deliverables.
* Track upcoming project deadlines.

### Documents

* Access annual academic documents.
* Download and open files through Android intents.
* Local preview when supported by the file type.

### Notifications

* Local or push notifications for important changes.
* Examples: new grade, recorded absence, schedule update, submission reminder.
* Configurable notification preferences when supported by the API.

## Internationalization (i18n)

The application must support multiple languages without requiring code changes.

### Principles

* No user-facing string should be hardcoded in screens, components, or error messages.
* All UI strings must be defined in Android resources.
* Translation keys should be stable, explicit, and organized by functional domain.
* Dates, times, numbers, and currencies should be formatted according to the active locale.
* French should be the default language.
* The application should support the system language, with an optional language override in settings if needed.

### Recommended Structure

```text
app/src/main/res/
├── values/
│   └── strings.xml
└── values-en/
    └── strings.xml
```

Example:

```xml
<resources>
    <string name="auth_login_title">Login</string>
    <string name="dashboard_next_course">Next Course</string>
    <string name="grades_title">Grades</string>
    <string name="absences_title">Absences</string>
    <string name="projects_deadline">Deadline</string>
    <string name="error_network">Unable to connect. Please check your network.</string>
</resources>
```

### Important Considerations

* Use Android plurals for absences, notifications, documents, and deadlines.
* Avoid concatenating translated strings in code.
* Test screens with labels longer than the French equivalents.
* Provide translations for local notifications and offline messages.
* Format dates using `java.time` and the active locale.

## MyGES API

The endpoints below represent the MyGES routes used by the application. They are documented as relative paths; the base URL must be configured per environment.

### Profile and Configuration

| Endpoint            | Purpose                                                  |
| ------------------- | -------------------------------------------------------- |
| `me/profile`        | Retrieve student profile                                 |
| `me/minimumVersion` | Minimum required application version                     |
| `me/years`          | Available academic years                                 |
| `me/trimesterYears` | Academic years and periods                               |
| `me/cvec`           | CVEC information                                         |
| `me/internalrules`  | Internal regulations                                     |
| `me/partners`       | Partners                                                 |
| `me/suggestion`     | Submit or retrieve suggestions depending on API contract |

### Schedule, Courses, and Academic Data

| Endpoint                            | Purpose                              |
| ----------------------------------- | ------------------------------------ |
| `me/agenda`                         | Student schedule                     |
| `me/{year}/courses`                 | Courses for a given year             |
| `me/{year}/classes`                 | Classes for a given year             |
| `me/{year}/students`                | Students for a given year            |
| `me/{year}/teachers`                | Teachers for a given year            |
| `me/classes/{puid}/students/{year}` | Students of a class for a given year |
| `me/{rcId}/syllabus`                | Course syllabus                      |

### Grades, Absences, and Documents

| Endpoint                    | Purpose                             |
| --------------------------- | ----------------------------------- |
| `me/{year}/grades`          | Grades for a given year             |
| `me/{year}/absences`        | Absences for a given year           |
| `me/{year}/annualDocuments` | Annual documents for a given year   |
| `me/annualDocuments/{id}`   | Annual document details or download |

### Course Files

| Endpoint                 | Purpose                         |
| ------------------------ | ------------------------------- |
| `me/{rcId}/files`        | Files associated with a course  |
| `me/{rcId}/files/{ocId}` | Course file details or download |

### Projects and Practicals

| Endpoint                                                         | Purpose                       |
| ---------------------------------------------------------------- | ----------------------------- |
| `me/{year}/projects`                                             | Projects for a given year     |
| `me/projects/{projectId}`                                        | Project details               |
| `me/nextProjectSteps`                                            | Upcoming project steps        |
| `me/projectFiles/{pfId}`                                         | Project file                  |
| `me/projectStepFiles/{psfId}`                                    | Project step file             |
| `me/courses/{rcId}/projects`                                     | Projects linked to a course   |
| `me/courses/{rcId}/projects/{projectId}/groups/{projectGroupId}` | Project group                 |
| `me/{year}/practicals`                                           | Practicals for a given year   |
| `me/courses/{rcId}/practicals`                                   | Practicals linked to a course |

### News and Notifications

| Endpoint                                      | Purpose                                |
| --------------------------------------------- | -------------------------------------- |
| `me/news`                                     | News                                   |
| `me/news/banners`                             | News banners                           |
| `me/notificationsDelays`                      | Configurable notification delays       |
| `me/notificationsDelays/{notificationTypeId}` | Notification delay for a specific type |
| `me/speedMeetingAppointments`                 | Speed meeting appointments             |

### Dynamic Parameters

* `{year}`: academic year.
* `{rcId}`: course or course resource identifier.
* `{puid}`: class or population identifier according to the MyGES API contract.
* `{id}`: annual document identifier.
* `{ocId}`: course file identifier.
* `{projectId}`: project identifier.
* `{projectGroupId}`: project group identifier.
* `{pfId}`: project file identifier.
* `{psfId}`: project step file identifier.
* `{notificationTypeId}`: notification type identifier.

## Synchronization and Cache

The application must remain usable under varying network conditions.

* Critical data must be cached locally after retrieval.
* Screens must display synchronization status: up to date, synchronizing, offline, or error.
* API errors must be converted into meaningful application-level errors.
* Tokens must be stored exclusively in secure Android storage.
* Sensitive data must never be logged.
* Periodic synchronization must use WorkManager and respect Android battery and network constraints.

## Recommended README Structure

Once source code becomes available, the README should follow this structure:

```text
1. Overview
2. Features
3. Android Technology Stack
4. Installation
5. Configuration
6. Architecture
7. Internationalization
8. MyGES API
9. Tests
10. Android Build
11. Technical Notes
```

## Important Technical Notes

* Listed endpoints must be validated against the actual API contract, including HTTP methods, query parameters, pagination, response formats, and error codes.
* Download routes must specify whether they return binaries, signed URLs, or metadata.
* Session refresh strategy must be defined before API integration.
* Push notifications generally require a backend or third-party service; an Android application alone cannot always detect changes in real time without periodic synchronization.
* Android background tasks are constrained by Doze, App Standby, and manufacturer battery policies.
* Grade averages should only be calculated locally when coefficients and grading rules are reliable.
* Academic and personal data must be treated as sensitive information through secure storage, filtered logging, and minimal permissions.
* The application must not depend on an embedded MyGES web session to render its primary screens.
