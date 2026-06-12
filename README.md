# Application Android MyGES

Ce dépôt décrit les fondations fonctionnelles et techniques d'une application Android native MyGES. L'application consomme les endpoints MyGES existants, expose une expérience mobile centrée sur l'étudiant et conserve une architecture claire pour faciliter la maintenance, les tests et l'évolution.

## Objectifs

- Consulter l'agenda, les notes, les absences, les cours, les documents et les projets.
- Centraliser les informations utiles au quotidien étudiant dans une application Android native.
- Utiliser les composants Android natifs, sans WebView ni interface web embarquée.
- Fonctionner correctement avec une gestion robuste du cache local et des états hors ligne.
- Isoler la logique métier du framework UI, du stockage local et des services externes.
- Préparer l'application à l'internationalisation dès le départ.

## Recommandations d'amélioration du README

- Clarifier le statut du document : il s'agit d'une spécification technique initiale, pas encore d'une documentation d'installation complète.
- Indiquer explicitement que le projet cible Android uniquement.
- Supprimer les options multi-plateformes et les références iOS, Flutter ou React Native si l'application doit être Android native.
- Préciser que l'application ne doit pas être une app web déguisée : pas de WebView pour les écrans principaux.
- Séparer les choix d'architecture, les fonctionnalités, l'API et l'i18n pour rendre la lecture plus directe.
- Ajouter une section `Installation` lorsque le projet contiendra le code source, les scripts Gradle et les dépendances.
- Ajouter une section `Configuration` pour documenter l'URL de base API, les secrets locaux, les build variants et les paramètres de debug.
- Ajouter une section `Tests` lorsque les conventions de tests unitaires, d'intégration et UI seront définies.
- Documenter explicitement les endpoints utilisés, les paramètres dynamiques et les usages principaux.

## Principes Android natifs

L'application doit être développée comme une application Android native, pas comme une interface web encapsulée.

- UI native avec Jetpack Compose ou vues Android natives.
- Navigation Android native, sans routage web.
- Accès aux fonctionnalités système via les APIs Android : stockage sécurisé, notifications, fichiers, calendrier, biométrie.
- Appels API via un client HTTP natif, pas via une page web chargée dans l'application.
- Cache local via une base embarquée Android, pas via `localStorage`, cookies web ou session de navigateur.
- Gestion des erreurs, loaders, états vides et hors ligne directement dans les écrans natifs.

Une WebView peut rester acceptable uniquement pour un cas très ponctuel, par exemple afficher un document HTML externe non reproductible nativement. Elle ne doit pas servir à rendre l'application principale.

## Architecture

L'application suit une architecture hexagonale adaptée à Android. La logique métier reste indépendante de l'interface, du stockage local, du client HTTP et des services natifs.

```text
app/src/main/java/
├── domain/                    # Entités, value objects et règles métier
├── application/               # Cas d'utilisation et ports
├── adapters/
│   ├── primary/               # UI Compose, navigation, ViewModels
│   └── secondary/             # API, stockage, notifications, services Android
└── config/                    # Configuration applicative
```

### Couches principales

- `domain` : modèles métier comme `Student`, `Grade`, `Absence`, `AgendaEvent`, `Project` ou `AcademicDocument`.
- `application` : orchestration des cas d'utilisation, par exemple l'authentification, la synchronisation, la récupération des notes ou la gestion des projets.
- `adapters/primary` : écrans Android, composants Compose, navigation et ViewModels.
- `adapters/secondary` : client API MyGES, cache local, stockage sécurisé, notifications et intégrations Android.

Cette séparation permet de tester les règles métier sans dépendre de l'UI Android ou des services externes.

## Stack technique recommandée

- Langage : Kotlin.
- UI : Jetpack Compose.
- Architecture UI : ViewModel avec `StateFlow`.
- Injection de dépendances : Hilt.
- Client HTTP : Retrofit et OkHttp.
- Sérialisation : Kotlinx Serialization ou Moshi.
- Cache local : Room.
- Préférences : DataStore.
- Stockage sécurisé : Android Keystore et EncryptedSharedPreferences.
- Tâches de fond : WorkManager.
- Notifications : Firebase Cloud Messaging et notifications locales Android.
- Fichiers : Storage Access Framework.
- Biométrie : AndroidX Biometric.
- Tests unitaires : JUnit, MockK, Turbine.
- Tests UI : Compose UI Test.

## Fonctionnalités mobiles prévues

### Authentification

- Connexion à l'espace MyGES.
- Stockage sécurisé des jetons d'authentification.
- Support possible de la biométrie après une première connexion réussie.
- Gestion explicite des erreurs de session, d'expiration et de réseau.

### Tableau de bord

- Résumé du profil étudiant.
- Prochain cours.
- Dernières notes.
- Absences récentes.
- Projets et échéances importantes.
- État de synchronisation et de cache.

### Agenda

- Consultation des événements de cours.
- Affichage par jour, semaine ou liste.
- Détails d'un cours : horaire, salle, intervenant, type et modalité.
- Synchronisation optionnelle avec le calendrier Android.

### Notes et absences

- Consultation des notes par année, période ou matière.
- Affichage des coefficients et moyennes lorsque les données nécessaires sont disponibles.
- Liste des absences avec statut de justification.
- Accès aux informations utiles pour suivre l'assiduité.

### Cours, projets et travaux pratiques

- Liste des cours par année.
- Accès aux syllabus et fichiers associés.
- Liste des projets et travaux pratiques.
- Consultation des groupes, étapes, fichiers et livrables.
- Suivi des prochaines échéances de projet.

### Documents

- Accès aux documents annuels.
- Téléchargement et ouverture de fichiers via les intents Android.
- Prévisualisation locale lorsque le type de fichier est supporté.

### Notifications

- Notifications locales ou push pour les changements importants.
- Exemples : nouvelle note, absence enregistrée, changement d'agenda, rappel de rendu.
- Préférences configurables par type de notification lorsque l'API le permet.

## Internationalisation (i18n)

L'application doit être conçue pour supporter plusieurs langues sans modifier le code applicatif.

### Principes

- Aucune chaîne visible ne doit être codée en dur dans les écrans, composants ou messages d'erreur.
- Toutes les chaînes UI doivent être déclarées dans les ressources Android.
- Les clés de traduction doivent être stables, explicites et organisées par domaine fonctionnel.
- Les dates, heures, nombres et montants doivent être formatés selon la locale active.
- La langue par défaut doit être le français.
- L'application doit pouvoir utiliser la langue du système, avec une option de surcharge dans les paramètres si nécessaire.

### Structure recommandée

```text
app/src/main/res/
├── values/
│   └── strings.xml
└── values-en/
    └── strings.xml
```

Exemple :

```xml
<resources>
    <string name="auth_login_title">Connexion</string>
    <string name="dashboard_next_course">Prochain cours</string>
    <string name="grades_title">Notes</string>
    <string name="absences_title">Absences</string>
    <string name="projects_deadline">Échéance</string>
    <string name="error_network">Connexion impossible. Vérifiez votre réseau.</string>
</resources>
```

### Points d'attention

- Utiliser les pluriels Android pour les absences, notifications, documents et échéances.
- Éviter de concaténer des chaînes traduites dans le code.
- Tester les écrans avec des libellés plus longs que le français.
- Prévoir les traductions des notifications locales et des messages hors ligne.
- Formater les dates avec `java.time` et la locale active.

## API MyGES

Les endpoints ci-dessous sont les routes MyGES utilisées par l'application. Ils sont documentés ici sous forme de chemins relatifs ; l'URL de base doit être configurée par environnement.

### Profil et configuration

| Endpoint | Usage |
| --- | --- |
| `me/profile` | Récupération du profil étudiant |
| `me/minimumVersion` | Version minimale requise de l'application |
| `me/years` | Liste des années disponibles |
| `me/trimesterYears` | Années et périodes académiques |
| `me/cvec` | Informations CVEC |
| `me/internalrules` | Règlement intérieur |
| `me/partners` | Partenaires |
| `me/suggestion` | Envoi ou récupération de suggestions selon le contrat API |

### Agenda, cours et scolarité

| Endpoint | Usage |
| --- | --- |
| `me/agenda` | Agenda de l'étudiant |
| `me/{year}/courses` | Cours d'une année |
| `me/{year}/classes` | Classes d'une année |
| `me/{year}/students` | Étudiants d'une année |
| `me/{year}/teachers` | Enseignants d'une année |
| `me/classes/{puid}/students/{year}` | Étudiants d'une classe pour une année |
| `me/{rcId}/syllabus` | Syllabus d'un cours |

### Notes, absences et documents

| Endpoint | Usage |
| --- | --- |
| `me/{year}/grades` | Notes d'une année |
| `me/{year}/absences` | Absences d'une année |
| `me/{year}/annualDocuments` | Documents annuels d'une année |
| `me/annualDocuments/{id}` | Détail ou téléchargement d'un document annuel |

### Fichiers de cours

| Endpoint | Usage |
| --- | --- |
| `me/{rcId}/files` | Fichiers associés à un cours |
| `me/{rcId}/files/{ocId}` | Détail ou téléchargement d'un fichier de cours |

### Projets et travaux pratiques

| Endpoint | Usage |
| --- | --- |
| `me/{year}/projects` | Projets d'une année |
| `me/projects/{projectId}` | Détail d'un projet |
| `me/nextProjectSteps` | Prochaines étapes de projet |
| `me/projectFiles/{pfId}` | Fichier associé à un projet |
| `me/projectStepFiles/{psfId}` | Fichier associé à une étape de projet |
| `me/courses/{rcId}/projects` | Projets associés à un cours |
| `me/courses/{rcId}/projects/{projectId}/groups/{projectGroupId}` | Groupe d'un projet |
| `me/{year}/practicals` | Travaux pratiques d'une année |
| `me/courses/{rcId}/practicals` | Travaux pratiques associés à un cours |

### Actualités et notifications

| Endpoint | Usage |
| --- | --- |
| `me/news` | Actualités |
| `me/news/banners` | Bannières d'actualité |
| `me/notificationsDelays` | Délais de notification configurables |
| `me/notificationsDelays/{notificationTypeId}` | Délai d'un type de notification |
| `me/speedMeetingAppointments` | Rendez-vous speed meeting |

### Paramètres dynamiques

- `{year}` : année académique.
- `{rcId}` : identifiant de cours ou de ressource cours.
- `{puid}` : identifiant de classe ou de population selon le contrat MyGES.
- `{id}` : identifiant de document annuel.
- `{ocId}` : identifiant de fichier de cours.
- `{projectId}` : identifiant de projet.
- `{projectGroupId}` : identifiant de groupe de projet.
- `{pfId}` : identifiant de fichier projet.
- `{psfId}` : identifiant de fichier d'étape de projet.
- `{notificationTypeId}` : identifiant de type de notification.

## Synchronisation et cache

L'application doit être utilisable dans des conditions réseau variables.

- Les données critiques doivent être mises en cache localement après récupération.
- Les écrans doivent afficher l'état de synchronisation : à jour, en cours, hors ligne ou en erreur.
- Les erreurs API doivent être transformées en erreurs applicatives compréhensibles.
- Les jetons doivent être stockés uniquement dans le stockage sécurisé Android.
- Les données sensibles ne doivent pas être journalisées.
- Les synchronisations périodiques doivent passer par WorkManager et respecter les contraintes batterie/réseau Android.

## Structure README recommandée

Lorsque le projet contiendra le code source, le README devrait suivre cette structure :

```text
1. Présentation
2. Fonctionnalités
3. Stack technique Android
4. Installation
5. Configuration
6. Architecture
7. Internationalisation
8. API MyGES
9. Tests
10. Build Android
11. Remarques techniques
```

## Remarques techniques importantes

- Les endpoints listés doivent être validés avec le contrat API réel : méthodes HTTP, paramètres de requête, pagination, formats de réponse et codes d'erreur.
- Les routes de téléchargement doivent préciser si elles retournent un binaire, une URL signée ou des métadonnées.
- La stratégie de rafraîchissement de session doit être définie avant l'intégration du client API.
- Les notifications push nécessitent généralement un backend ou un service tiers ; une application Android seule ne peut pas toujours détecter les changements en temps réel sans synchronisation périodique.
- Les tâches de fond Android sont contraintes par Doze, App Standby et les politiques batterie constructeur.
- Les calculs de moyennes ne doivent être effectués localement que si les coefficients et règles de calcul sont fiables.
- Les données académiques et personnelles doivent être traitées comme sensibles : stockage sécurisé, logs filtrés et permissions minimales.
- L'application ne doit pas dépendre d'une session web MyGES embarquée pour afficher les écrans principaux.
