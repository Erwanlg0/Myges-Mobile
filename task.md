# Suivi des Tâches - Application Android MyGES

Ce document présente l'ensemble des chantiers techniques et fonctionnels à réaliser pour l'application MyGES Mobile. Toutes les tâches ci-dessous sont à accomplir ou à consolider.

## Dernière reprise vérifiée

- Build validé : `cmd.exe /c gradlew.bat :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:assembleRelease --no-daemon`
- Résultat : `BUILD SUCCESSFUL in 5m 52s` (`125 actionable tasks`)
- APK release généré : `app/build/outputs/apk/release/app-release-unsigned.apk`
- État notable : l'APK release reste non signé tant qu'aucun keystore production n'est fourni via `MYGES_RELEASE_STORE_FILE`, `MYGES_RELEASE_STORE_PASSWORD`, `MYGES_RELEASE_KEY_ALIAS`, `MYGES_RELEASE_KEY_PASSWORD`.
- Dernier lot réalisé : stratégie de session J+5/J+7 avec invalidation 401, agenda strict 4 semaines, parsing aligné sur les payloads live Kordis (`result`, `start_date`, `end_date`, notes numériques imbriquées), R8/ProGuard activé sur la release, suppression du keepRules vide généré par défaut, nettoyage du cache avec réinitialisation de `lastSyncAt`, exclusions backup/transfer explicites.

## 1. Architecture, Injection de Dépendances & Sécurité Système

- `[x]` Consolider la configuration initiale du projet Android avec Gradle Kotlin DSL (fichiers `build.gradle.kts` racine et module `:app`)
- `[x]` Finaliser la centralisation et la gestion des dépendances via Gradle Version Catalog (`gradle/libs.versions.toml`)
- `[x]` Valider l'implémentation de Dagger Hilt pour l'injection de dépendances (`@HiltAndroidApp`, `DependencyModule` et `PortBindingModule`)
- `[x]` Garantir la bonne liaison des interfaces Ports avec leurs implémentations Adaptateurs secondaires via Hilt
- `[x]` Intégrer SQLCipher pour le chiffrement de la base de données Room (sécurisation impérative de l'ensemble des données académiques et personnelles hautement sensibles)
- `[x]` Mettre en œuvre le SSL Pinning (épinglage de clé publique) sur le client Retrofit pour prémunir l'application des attaques de type Man-In-The-Middle (MITM)
- `[x]` Configurer et tester les règles ProGuard/R8 pour l'obfuscation et la réduction du code (en veillant à préserver les classes sérialisables, de base de données Room et d'injection Hilt)
- `[x]` Configurer un fichier de sécurité réseau (`network_security_config.xml`) restrictif pour interdire tout flux HTTP non chiffré
- `[x]` Mettre en place un système de rotation automatique des clés de chiffrement de l'Android Keystore
- `[x]` Mettre en œuvre une politique de masquage de logs en production (logs filtrés) empêchant toute écriture de jeton d'accès ou de données sensibles dans le Logcat (ex. intégration de Timber avec un arbre de release vide)
- `[ ]` Évaluer l'opportunité d'une migration future vers Kotlin Multiplatform (KMP) pour isoler la logique métier dans un module partagé

## 2. Authentification, Gestion de Session & Cycle de Vie des Jetons

- `[x]` Consolider la définition du Port `SessionRepository` pour la gestion réactive et persistante de la session utilisateur
- `[x]` Réviser l'adaptateur secondaire `SecureSessionStore` utilisant l'Android Keystore et le chiffrement AES/GCM/NoPadding sur SharedPreferences
- `[x]` Valider l'intercepteur OkHttp pour l'insertion automatique du jeton d'accès d'authentification (`Authorization: <token Kordis brut>`) et du `User-Agent` personnalisé
- `[x]` Compléter le support du déverrouillage biométrique (`BiometricPrompt` et stockage sécurisé des états)
- `[x]` Valider les cas d'utilisation `CompleteOAuthLoginUseCase` pour valider et enregistrer la session après le flux OAuth
- `[x]` Valider les cas d'utilisation `ObserveSessionUseCase` et `ObserveLockedBiometricSessionUseCase` pour suivre l'état de la session
- `[x]` Valider le cas d'utilisation `UnlockWithBiometricsUseCase` pour le déverrouillage de la session
- `[x]` Valider le cas d'utilisation `LogoutUseCase` pour la déconnexion et l'annulation des synchronisations en arrière-plan
- `[x]` Implémenter le mécanisme complet de rafraîchissement automatique des jetons (Token Refresh) :
  - `[x]` Configurer le `refreshToken` et l'expiration (`expiresAt`) dans le modèle de domaine `Session`
  - `[x]` Déclencher une nouvelle authentification OAuth proactive après 5 jours, avant l'expiration nominale à 7 jours
  - `[x]` Implémenter un interceptor OkHttp qui invalide la session et force une réauthentification immédiate en cas d'erreur `401 Unauthorized`

## 3. Client API MyGES & Parsing Robuste

- `[x]` Valider le client Retrofit avec le convertisseur de sérialisation `Kotlinx Serialization`
- `[x]` Consolider la configuration tolérante de `Json` (ignorer les clés inconnues, autoriser le mode lenient, ignorer les valeurs nulles explicites)
- `[x]` Valider l'implémentation du parseur JSON flexible (`JsonParsing.kt`) prenant en charge des structures imbriquées et de multiples formats de date/heure (Epoch, ISO Offset, etc.)
- `[x]` Valider le support du téléchargement de fichiers binaires avec Retrofit `@Streaming` et copie vers le stockage local
- `[x]` Compléter et valider les endpoints de base existants dans `MyGesApiService` :
  - `[x]` `me/profile` (Profil étudiant)
  - `[x]` `me/years` (Liste des années académiques disponibles)
  - `[x]` `me/agenda` (Événements de l'agenda avec filtres de dates)
  - `[x]` `me/{year}/courses` (Liste des cours d'une année)
  - `[x]` `me/{year}/grades` (Notes obtenues pour une année)
  - `[x]` `me/{year}/absences` (Absences enregistrées pour une année)
  - `[x]` `me/{year}/annualDocuments` (Liste des documents annuels)
  - `[x]` `me/{year}/projects` (Liste des projets d'une année)
  - `[x]` `me/{year}/practicals` (Liste des travaux pratiques d'une année)
  - `[x]` `me/news` (Actualités générales)
  - `[x]` `me/news/banners` (Bannières d'actualité urgentes/importantes)
- `[ ]` Implémenter tous les endpoints restants de la spécification technique MyGES :
  - `[ ]` `me/minimumVersion` (Version minimale de l'application requise par le serveur)
  - `[ ]` `me/trimesterYears` (Années et périodes académiques associées)
  - `[ ]` `me/cvec` (Statut ou informations sur la CVEC)
  - `[ ]` `me/internalrules` (Règlement intérieur de l'établissement)
  - `[ ]` `me/partners` (Liste des partenaires académiques/professionnels)
  - `[ ]` `me/suggestion` (Envoi ou récupération de suggestions)
  - `[ ]` `me/{year}/classes` (Classes/promotions d'une spécifique)
  - `[ ]` `me/{year}/students` (Liste des étudiants d'une spécifique)
  - `[ ]` `me/{year}/teachers` (Liste des enseignants d'une spécifique)
  - `[ ]` `me/classes/{puid}/students/{year}` (Liste des étudiants d'une classe spécifique)
  - `[ ]` `me/{rcId}/syllabus` (Syllabus détaillé d'un cours spécifique)
  - `[ ]` `me/{rcId}/files` (Fichiers associés à un cours)
  - `[ ]` `me/{rcId}/files/{ocId}` (Détail et téléchargement d'un fichier de cours spécifique)
  - `[ ]` `me/projects/{projectId}` (Détail d'un projet spécifique)
  - `[ ]` `me/nextProjectSteps` (Prochaines étapes de rendu de projets)
  - `[ ]` `me/projectFiles/{pfId}` (Fichiers associés à un projet)
  - `[ ]` `me/projectStepFiles/{psfId}` (Fichiers d'une étape de projet)
  - `[ ]` `me/courses/{rcId}/projects` (Projets associés à un cours spécifique)
  - `[ ]` `me/courses/{rcId}/projects/{projectId}/groups/{projectGroupId}` (Groupe affecté à un projet)
  - `[ ]` `me/courses/{rcId}/practicals` (Travaux pratiques associés à un cours spécifique)
  - `[ ]` `me/notificationsDelays` (Délais configurables de notifications push)
  - `[ ]` `me/notificationsDelays/{notificationTypeId}` (Délai d'un type de notification spécifique)
  - `[ ]` `me/speedMeetingAppointments` (Rendez-vous speed meeting planifiés)

## 4. Persistance des Données & Stratégie Offline-First

- `[x]` Consolider la configuration de la base de données Room (`MygesDatabase`)
- `[x]` Définir et valider les schémas SQL sous forme d'entités Room (`Entities.kt`)
- `[x]` Valider le DAO Room (`StudentDao`) définissant les requêtes d'observation réactive et d'écriture transactionnelle
- `[x]` Valider les mappeurs de données (`EntityMappers.kt`) assurant la conversion entre les modèles de domaine et les entités Room
- `[x]` Consolider le dépôt offline-first `OfflineFirstStudentDataRepository` coordonnant l'observation des flux locaux, la synchronisation avec l'API distante et le téléchargement des documents
- `[x]` Optimiser l'algorithme de mise à jour des données dans Room (`replaceSyncedData()`) :
  - Actuellement, l'implémentation effectue une purge globale (DELETE ALL) suivie d'une insertion (UPSERT). Il convient d'adopter des requêtes de mise à jour incrémentale (comparaison d'entités par diff) afin de ne pas perturber les états d'affichage de l'interface et d'éviter les clignotements d'UI pour les données inchangées.
- `[x]` Valider la gestion du cycle de vie des fichiers téléchargés localement (`cacheDir/documents`) via un `FileProvider` sécurisé
- `[x]` Valider les cas d'utilisation associés : `RefreshStudentDataUseCase`, `ClearCacheUseCase`, `DownloadDocumentUseCase`

## 5. Synchronisation de l'Agenda & Calendrier Android

- `[x]` Définir et valider le Port `CalendarSyncPort` pour la synchronisation de l'agenda
- `[x]` Valider l'adaptateur secondaire `AndroidCalendarSyncAdapter` utilisant le `ContentResolver` et `CalendarContract`
- `[x]` Valider le nettoyage automatique des anciens événements synchronisés via un filtre sur la description contenant le préfixe de l'application
- `[x]` Générer une fenêtre d'agenda stricte de 4 semaines pour couvrir l'alternance 3 semaines entreprise / 1 semaine école
- `[x]` Optimiser l'algorithme de synchronisation du calendrier pour éviter de vider et de réinsérer intégralement l'agenda à chaque cycle de synchronisation (mise à jour incrémentale / diff d'événements)

## 6. Tâches en Arrière-Plan & Gestion Énergétique

- `[x]` Valider le Port `NetworkMonitor` et l'adaptateur `AndroidNetworkMonitor` émettant réactivement le statut de connectivité via les API système
- `[x]` Valider la planification de la synchronisation périodique via WorkManager (`StudentSyncWorker`) déclenchée toutes les 6 heures avec contrainte de réseau connecté
- `[x]` Optimiser la planification des tâches WorkManager pour tenir compte de l'état de charge de l'appareil (économiseur de batterie) et s'adapter aux restrictions de veille imposées par le système (Doze mode)

## 7. Système de Notifications Locales & Push

- `[x]` Valider le Port `NotificationScheduler` et l'adaptateur secondaire `AndroidNotificationScheduler`
- `[x]` Consolider la création du canal de notification natif `student` à l'initialisation de l'application
- `[x]` Valider le mécanisme de détection des nouveautés par comparaison d'IDs pour les éléments suivants :
  - `[x]` Notifications locales pour les nouvelles notes
  - `[x]` Notifications locales pour les nouvelles absences
  - `[x]` Notifications locales pour les modifications d'événements de l'agenda
  - `[x]` Notifications locales pour les échéances de projet proches
  - `[x]` Notifications locales pour l'ajout de nouveaux documents
- `[x]` Valider la notification d'échec de synchronisation en arrière-plan
- `[ ]` Implémenter des fonctionnalités avancées de notifications locales (actions directes, regroupement intelligent par matière)
- `[ ]` Mettre en place un système de notifications Push via Firebase Cloud Messaging (FCM) avec option de repli sur la synchronisation périodique locale

## 8. Préférences & Configuration Utilisateur

- `[x]` Valider le Port `SettingsRepository` pour persisté les options globales de l'application
- `[x]` Valider l'adaptateur secondaire `AppSettingsRepository` basé sur Jetpack DataStore Preferences
- `[x]` Assurer la gestion et la persistance des préférences suivantes :
  - `[x]` État d'activation de la synchronisation du calendrier
  - `[x]` Configuration granulaire des alertes par type (notes, absences, agenda, projets, documents)
  - `[x]` Langue configurée de l'application (Français, Anglais, Système)
  - `[x]` Date et heure de dernière synchronisation réussie

## 9. Interface Utilisateur & Design Premium (Jetpack Compose)

- `[x]` Valider la configuration Edge-to-Edge et la gestion des insets de fenêtre de `MainActivity`
- `[x]` Réviser le système de navigation principal via `NavHost` et le tiroir de navigation latéral (`ModalNavigationDrawer`)
- `[x]` Valider l'intégration du composant d'état d'UI `FeatureStateContent` gérant les états de chargement, d'erreur, hors ligne et rafraîchissement
- `[ ]` Refondre visuellement et enrichir l'ensemble des écrans pour un rendu "Premium" :
  - `[ ]` **Authentification (`AuthScreen`)** : Améliorer les transitions visuelles lors du basculement vers Custom Tabs et lors du déverrouillage biométrique.
  - `[ ]` **Tableau de Bord (`DashboardScreen`)** : Intégrer un design moderne avec des cartes en dégradés progressifs, des ombres douces et une structure claire.
  - `[ ]` **Agenda (`AgendaScreen`)** : Perfectionner l'ergonomie des vues Jour/Semaine, ajouter des indicateurs colorés par type de cours.
  - `[ ]` **Notes (`GradesScreen`)** : Mettre en valeur la moyenne générale et les coefficients, ajouter des graphiques simples de progression ou de répartition.
  - `[ ]` **Absences (`AbsencesScreen`)** : Rendre le récapitulatif des absences non justifiées plus impactant visuellement.
  - `[ ]` **Cours (`CoursesScreen`)** : Faciliter la consultation du syllabus avec une UI pliable et ergonomique.
  - `[ ]` **Projets (`ProjectsScreen`)** : Afficher un indicateur visuel de progression de type "Timeline" ou "Stepper" pour les étapes de rendu.
  - `[ ]` **Travaux Pratiques (`PracticalsScreen`)** : Structurer l'affichage par ordre chronologique avec repères visuels clairs.
  - `[ ]` **Documents (`DocumentsScreen`)** : Ajouter des indicateurs de statut de téléchargement en temps réel (loader, état local/distant).
  - `[ ]` **Actualités / News (`NotificationsScreen`)** : Optimiser le rendu des bannières d'actualité.
  - `[ ]` **Réglages (`SettingsScreen`)** : Harmoniser l'ensemble des commutateurs et actions.
- `[ ]` Intégrer des animations de transition de pages fluides (Shared Element Transitions, animations Compose personnalisées)
- `[ ]` Importer et configurer des polices de caractères Google Fonts (ex. Inter ou Outfit) pour remplacer la typographie système par défaut
- `[ ]` Ajouter des micro-interactions de retours haptiques et d'animations lors de l'activation des boutons ou des commutateurs
- `[x]` Prendre en charge le mode sombre et le mode clair natif de manière harmonieuse sans dépendre exclusivement de la palette dynamique Android 12+
- `[ ]` Concevoir des layouts adaptatifs pour assurer un affichage optimal sur tablettes, écrans pliables et mode paysage (colonnes partagées)
- `[x]` Intégrer l'API Splash Screen native d'Android 12+ pour une transition propre au lancement

## 10. Internationalisation (i18n) & Accessibilité (a11y)

- `[x]` Valider la séparation complète des chaînes de caractères de l'UI dans les fichiers de ressources (`res/values/strings.xml` et `res/values-en/strings.xml`)
- `[x]` Auditer le code pour garantir l'absence totale de chaînes de caractères codées en dur (hardcoded) dans le code Kotlin Compose
- `[x]` Garantir la bonne gestion des pluriels pour les compteurs d'absences dans toutes les langues
- `[x]` Améliorer l'accessibilité (a11y) : baliser sémantiquement les composants Compose pour TalkBack (contentDescription exhaustif, rôles d'éléments adaptés, fusion des sélecteurs)

## 11. Tests Globaux & Validation de Robustesse

- `[x]` Écrire des tests unitaires pour les ViewModels (`AuthViewModel`, `StudentViewModel`, `SettingsViewModel`)
- `[x]` Écrire des tests unitaires pour le dépôt `OfflineFirstStudentDataRepository`
- `[x]` Écrire des tests unitaires pour l'ensemble des mappeurs de données et utilitaires de parsing JSON
- `[ ]` Implémenter des tests d'intégration instrumentés pour la base de données Room (`StudentDaoTest`)
- `[ ]` Implémenter des tests d'intégration pour le magasin de session sécurisé (`SecureSessionStore`)
- `[ ]` Écrire des tests Compose UI instrumentés supplémentaires pour valider le comportement dynamique de chaque écran fonctionnel
- `[ ]` Configurer des tests d'interface de bout en bout (E2E) simulés à l'aide de MockWebServer pour tester l'intégralité du flux hors ligne et en ligne
- `[ ]` Mettre en place des tests de non-régression visuelle (Screenshot Testing) sur les écrans Compose
- `[ ]` Valider la robustesse de l'application face aux cas limites de l'API :
  - `[x]` Gestion du code HTTP `429 Too Many Requests` avec relance progressive (Exponential Backoff)
  - `[x]` Gestion du code HTTP `503 Service Unavailable` ou mode maintenance
  - `[x]` Reconnexion réseau automatique (re-déclenchement fluide du rafraîchissement d'UI dès que le réseau redevient disponible)

## 12. Améliorations UX Avancées, Intégrations Plateforme & Qualité

- `[x]` Configurer des `PendingIntent` avec deeplinks de navigation explicites dans `AndroidNotificationScheduler` :
  - Permettre à l'étudiant d'ouvrir directement l'écran concerné (ex. écran des Notes pour une nouvelle note, écran des Absences pour une absence) lors du clic sur une notification système.
- `[ ]` Intégrer la bibliothèque de chargement d'images Coil pour afficher l'avatar de l'étudiant :
  - Ajouter la dépendance Coil dans Gradle.
  - Charger l'URL `avatarUrl` du profil et l'afficher sous forme circulaire dans le Tableau de Bord (`DashboardScreen`) et l'en-tête du tiroir latéral (`ModalNavigationDrawer`).
- `[ ]` Mettre en œuvre une redirection globale de session expirée (`401 Unauthorized`) :
  - Intercepter les erreurs de token invalide/expiré et appeler automatiquement `LogoutUseCase` pour renvoyer proprement l'utilisateur vers le formulaire d'authentification, évitant ainsi un blocage d'UI avec une simple bannière d'erreur.
- `[ ]` Implémenter un écran/dialogue de détails pour l'Agenda :
  - Permettre le clic sur un événement de cours (`AgendaEventCard`) pour ouvrir un dialogue modal détaillant les horaires complets, l'intervenant, la salle de classe, les modalités (présentiel/distanciel), et d'éventuels liens ou syllabus associés.
- `[ ]` Implémenter un calculateur de moyennes générales et de GPA :
  - Concevoir un module (dans la couche domaine/métier) pour calculer de manière fiable les moyennes globales par semestre en fonction des coefficients et notes disponibles, tout en prévenant l'utilisateur de l'éventuelle inexactitude si les règles de l'établissement diffèrent.
- `[ ]` Intégrer les API Google Play Store de qualité :
  - `[ ]` **Google Play In-App Review API** : proposer discrètement à l'étudiant de noter l'application après une interaction réussie (ex. une synchronisation complète des notes).
  - `[ ]` **Google Play In-App Updates API** : détecter la présence d'une nouvelle version sur le store au lancement de l'application et proposer une mise à jour flexible ou immédiate.
- `[x]` Mettre en place un système de purge automatisée du cache des fichiers locaux :
  - Supprimer automatiquement les fichiers de documents stockés dans `cacheDir/documents` après une période d'inactivité ou une durée définie (ex. 30 jours) pour optimiser l'espace disque de l'appareil de l'utilisateur.
- `[ ]` Intégrer un indicateur de progression de téléchargement dans la carte des documents pour donner un feedback visuel précis lors de la récupération locale.
- `[ ]` Établir des retours tactiles (Haptic Feedback) lors des actions réussies ou d'erreurs (ex. déconnexion réussie, échec de synchronisation).
