# MyGES API Endpoints Test Responses

Ce fichier documente les réponses retournées par l'API de MyGES (`https://api.kordis.fr/`) pour chaque endpoint défini dans le README.

**Date du test** : 2026-06-15 18:01:30
**Bearer Token** : `dc499f6e...8e971f23`

## Table des matières

- [me/2024/absences](#me2024absences)
- [me/2024/annualDocuments](#me2024annualdocuments)
- [me/2024/classes](#me2024classes)
- [me/2024/courses](#me2024courses)
- [me/2024/grades](#me2024grades)
- [me/2024/practicals](#me2024practicals)
- [me/2024/projects](#me2024projects)
- [me/2024/students](#me2024students)
- [me/2024/teachers](#me2024teachers)
- [me/2025/absences](#me2025absences)
- [me/2025/annualDocuments](#me2025annualdocuments)
- [me/2025/classes](#me2025classes)
- [me/2025/courses](#me2025courses)
- [me/2025/grades](#me2025grades)
- [me/2025/practicals](#me2025practicals)
- [me/2025/projects](#me2025projects)
- [me/2025/students](#me2025students)
- [me/2025/teachers](#me2025teachers)
- [me/2026/absences](#me2026absences)
- [me/2026/annualDocuments](#me2026annualdocuments)
- [me/2026/classes](#me2026classes)
- [me/2026/courses](#me2026courses)
- [me/2026/grades](#me2026grades)
- [me/2026/practicals](#me2026practicals)
- [me/2026/projects](#me2026projects)
- [me/2026/students](#me2026students)
- [me/2026/teachers](#me2026teachers)
- [me/349780/files](#me349780files)
- [me/349780/files/602186](#me349780files602186)
- [me/349780/syllabus](#me349780syllabus)
- [me/agenda](#meagenda)
- [me/annualDocuments/596371](#meannualdocuments596371)
- [me/classes/23129/students/2026](#meclasses23129students2026)
- [me/courses/349780/practicals](#mecourses349780practicals)
- [me/courses/349780/projects](#mecourses349780projects)
- [me/courses/349780/projects/16834/groups/371279](#mecourses349780projects16834groups371279)
- [me/cvec](#mecvec)
- [me/internalrules](#meinternalrules)
- [me/minimumVersion](#meminimumversion)
- [me/news](#menews)
- [me/news/banners](#menewsbanners)
- [me/nextProjectSteps](#menextprojectsteps)
- [me/notificationsDelays](#menotificationsdelays)
- [me/notificationsDelays/{notificationTypeId}](#menotificationsdelaysnotificationtypeid)
- [me/partners](#mepartners)
- [me/profile](#meprofile)
- [me/projectFiles/15175](#meprojectfiles15175)
- [me/projectStepFiles/413810](#meprojectstepfiles413810)
- [me/projects/16834](#meprojects16834)
- [me/speedMeetingAppointments](#mespeedmeetingappointments)
- [me/suggestion](#mesuggestion)
- [me/trimesterYears](#metrimesteryears)
- [me/years](#meyears)

---

## me/2024/absences

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "date": 1745480700000,
      "course_name": "S2 - développement mobile android",
      "justified": true,
      "trimester": 22,
      "trimester_name": "Semestre 2",
      "type": "m",
      "year": 2024,
      "links": []
    },
    {
      "date": 1744184700000,
      "course_name": "S2 - modélisation uml 2",
      "justified": false,
      "trimester": 22,
      "trimester_name": "Semestre 2",
      "type": "m",
      "year": 2024,
      "links": []
    },
    "... truncated list, total 19 items"
  ],
  "links": []
}
```

---

## me/2024/annualDocuments

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "category": "ACTIVITES ASSOCIATIVES",
      "document_id": 544602,
      "extension": ".docx",
      "filename": "Scg5sIxLDn.docx",
      "last_update": 1766498823503,
      "name": "Liste des associations 2025 - 2026",
      "school_id": 7,
      "school_name": "ESGI",
      "links": []
    },
    {
      "category": "ACTIVITES ASSOCIATIVES",
      "document_id": 490722,
      "extension": ".pdf",
      "filename": "BnMrH5cng7.pdf",
      "last_update": 1738340267490,
      "name": "Présentation OPEN JANVIER 2025/2026",
      "school_id": 7,
      "school_name": "ESGI",
      "links": []
    },
    "... truncated list, total 76 items"
  ],
  "links": []
}
```

---

## me/2024/classes

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "name": "Classe 2",
      "description": "Classe 2",
      "promotion": "2ESGIi",
      "school": "ESGI",
      "year": 2024,
      "trimester": 21,
      "puid": 1185,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/classes/1185"
        },
        {
          "rel": "students",
          "href": "https://api.kordis.fr/me/classes/1185/students"
        }
      ]
    },
    {
      "name": "Classe 2",
      "description": "Classe 2",
      "promotion": "2ESGIi",
      "school": "ESGI",
      "year": 2024,
      "trimester": 22,
      "puid": 1185,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/classes/1185"
        },
        {
          "rel": "students",
          "href": "https://api.kordis.fr/me/classes/1185/students"
        }
      ]
    }
  ],
  "links": []
}
```

---

## me/2024/courses

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "coef": 2.0,
      "ects": 2.0,
      "name": "S1- anglais 2 : informatique, expression orale et écrite",
      "teacher": "Mme DELESTRE",
      "trimester": "Semestre 1",
      "year": 2024,
      "links": [],
      "has_documents": true,
      "has_grades": true,
      "nb_students": 16,
      "rc_id": 273738,
      "school_id": 7,
      "student_group_id": 30115,
      "student_group_name": "2 ESGI - Initial Anglais G3",
      "syllabus_id": 41640,
      "teacher_id": 6331,
      "trimester_id": 21
    },
    {
      "coef": 2.0,
      "ects": 2.0,
      "name": "S2 - anglais 2 : informatique, expression orale et écrite",
      "teacher": "Mme DELESTRE",
      "trimester": "Semestre 2",
      "year": 2024,
      "links": [],
      "has_documents": true,
      "has_grades": true,
      "nb_students": 16,
      "rc_id": 273957,
      "school_id": 7,
      "student_group_id": 30936,
      "student_group_name": "2 ESGI - Initial Anglais G3",
      "syllabus_id": 41640,
      "teacher_id": 6331,
      "trimester_id": 22
    },
    "... truncated list, total 29 items"
  ],
  "links": []
}
```

---

## me/2024/grades

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "course": "S1 - projet annuel",
      "code": "",
      "grades": [],
      "bonus": 0.0,
      "exam": null,
      "average": null,
      "trimester": 21,
      "trimester_name": "Semestre 1",
      "year": 2024,
      "rc_id": 302289,
      "ects": "N.C.",
      "coef": "N.C.",
      "teacher_civility": "M.",
      "teacher_first_name": "Frédéric",
      "teacher_last_name": "SANANES",
      "absences": 0,
      "lates": 0,
      "letter_mark": "F",
      "ccaverage": 0.0,
      "links": []
    },
    {
      "course": "S2 - journée thématique (al)",
      "code": "",
      "grades": [],
      "bonus": 0.0,
      "exam": null,
      "average": null,
      "trimester": 22,
      "trimester_name": "Semestre 2",
      "year": 2024,
      "rc_id": 308445,
      "ects": "N.C.",
      "coef": "N.C.",
      "teacher_civility": "M.",
      "teacher_first_name": "Vincent",
      "teacher_last_name": "MILANO",
      "absences": 0,
      "lates": 0,
      "letter_mark": "F",
      "ccaverage": 0.0,
      "links": []
    },
    "... truncated list, total 29 items"
  ],
  "links": []
}
```

---

## me/2024/practicals

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "project_id": 19370,
      "teacher_id": 1000006426,
      "author": "KIFFER Raphael",
      "name": "Exam - Cloud - 2i2",
      "update_date": 1746610614824,
      "update_user": "KIFFER Raphael",
      "course_name": "S2 - introduction au cloud",
      "discipline_id": 143696,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 426248,
          "project_id": 19370,
          "subject_id": 29429,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "VAUCHER",
              "firstname": "Edouard",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 521407
            }
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 426249,
          "project_id": 19370,
          "subject_id": 29429,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "NAOTHAWORN",
              "firstname": "Hugo",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 476668
            }
          ]
        },
        "... truncated list, total 27 items"
      ],
      "steps": [
        {
          "psp_id": 28848,
          "psp_type": "Rendu final",
          "psp_desc": "Rendu",
          "psp_limit_date": 1746615600000,
          "pro_id": 19370,
          "psp_number": 1,
          "files": [
            {
              "psf_id": 451737,
              "psf_desc": "",
              "psf_begin_upload": 1746616658851,
              "psf_end_upload": 1746616679974,
              "psf_file": "EXhInYjusakyZI1FzPlO.txt",
              "psf_role_user": "student",
              "psf_file_size": 100,
              "psf_file_hash": "1066465ef8ead5a19983ae34fbb9d1d2",
              "psf_file_type": ".txt",
              "psp_id": 28848,
              "pgr_id": 426270,
              "u_id": 532733,
              "psf_name": "links",
              "links": []
            },
            {
              "psf_id": 451736,
              "psf_desc": "",
              "psf_begin_upload": 1746616451354,
              "psf_end_upload": 1746616469352,
              "psf_file": "SVIC5two10UVsvnygFMQ.docx",
              "psf_role_user": "student",
              "psf_file_size": 414382,
              "psf_file_hash": "19d9fa59084b3d52ea92cf7cd7962f4c",
              "psf_file_type": ".docx",
              "psp_id": 28848,
              "pgr_id": 426270,
              "u_id": 532733,
              "psf_name": "exo-2-2",
              "links": []
            },
            "... truncated list, total 47 items"
          ],
          "links": []
        }
      ],
      "project_files": [
        {
          "links": [],
          "pf_id": 17550,
          "pf_title": "Sujet",
          "pf_file": "JgnUgKm6qEtp47URD8nn.pdf",
          "pf_crea_date": 1746575811617,
          "pro_id": 19370
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 1738525,
          "pgl_author": "KIFFER Raphael",
          "pgl_role_user": "teacher",
          "pgl_describe": "KIFFER Raphael a ajouté LUCE--GUEDON Erwan au Groupe 17",
          "pgl_date": 1746576092972,
          "pgl_type_action": "join",
          "user_id": 1000006426,
          "pgr_id": 426264
        },
        {
          "links": [],
          "pgl_id": 1738749,
          "pgl_author": "LUCE--GUEDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUEDON Erwan a téléchargé le livrable Rendu Erwan 51084",
          "pgl_date": 1746620716658,
          "pgl_type_action": "downloadStepFile",
          "user_id": 479546,
          "pgr_id": 426264
        },
        "... truncated list, total 5 items"
      ],
      "is_draft": false,
      "project_type_id": 2,
      "project_computing_tools": "",
      "project_create_date": 1746568544479,
      "project_detail_plan": "",
      "project_hearing_presentation": null,
      "project_max_student_group": 1,
      "project_min_student_group": 1,
      "project_personal_work": 1,
      "project_presentation_duration": 0,
      "project_ref_books": "",
      "project_teaching_goals": "Le but de ce TP est de réaliser plusieurs exercices avec pour objectif final de mettre en place des applications ou services dans le Cloud. Ce TP est noté et nécessitera de votre part une connaissance et une compréhension du cours existant ainsi que des TPs guidés effectués en classe.",
      "project_type_group": "Imposé",
      "project_type_presentation": null,
      "project_type_presentation_details": null,
      "project_type_subject": "Imposé",
      "rc_id": 304536,
      "trimester_id": 22,
      "year": 2024,
      "links": []
    },
    {
      "project_id": 18913,
      "teacher_id": 1000044399,
      "author": "LARRIEU LACOSTE Noé",
      "name": "TP docker-compose",
      "update_date": 1743580323550,
      "update_user": "LARRIEU LACOSTE Noé",
      "course_name": "S2 - conteneurisation docker (bases)",
      "discipline_id": 143698,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 417497,
          "project_id": 18913,
          "subject_id": 28819,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "PRISSET",
              "firstname": "Noah",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 1",
              "links": [],
              "u_id": 483187
            }
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 417498,
          "project_id": 18913,
          "subject_id": 28819,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "DE SOUZA",
              "firstname": "Darril",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 1",
              "links": [],
              "u_id": 490264
            }
          ]
        },
        "... truncated list, total 50 items"
      ],
      "steps": null,
      "project_files": [
        {
          "links": [],
          "pf_id": 17217,
          "pf_title": "Ressources",
          "pf_file": "qHaWekSheky9DZYwSuY9.zip",
          "pf_crea_date": 1743586386959,
          "pro_id": 18913
        },
        {
          "links": [],
          "pf_id": 17218,
          "pf_title": "Sujet",
          "pf_file": "8D5Dq851MCiJ2wly2OZU.pdf",
          "pf_crea_date": 1743586356005,
          "pro_id": 18913
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 1711264,
          "pgl_author": "LARRIEU LACOSTE Noé",
          "pgl_role_user": "teacher",
          "pgl_describe": "LARRIEU LACOSTE Noé a ajouté LUCE--GUEDON Erwan au Groupe 47",
          "pgl_date": 1743586451954,
          "pgl_type_action": "join",
          "user_id": 1000044399,
          "pgr_id": 417543
        }
      ],
      "is_draft": false,
      "project_type_id": 2,
      "project_computing_tools": "",
      "project_create_date": 1743579200458,
      "project_detail_plan": "",
      "project_hearing_presentation": null,
      "project_max_student_group": 1,
      "project_min_student_group": 1,
      "project_personal_work": 1,
      "project_presentation_duration": 0,
      "project_ref_books": "",
      "project_teaching_goals": "Maîtriser docker compos à travers le développement d'une stack",
      "project_type_group": "Imposé",
      "project_type_presentation": null,
      "project_type_presentation_details": null,
      "project_type_subject": "Imposé",
      "rc_id": 304537,
      "trimester_id": 22,
      "year": 2024,
      "links": []
    },
    "... truncated list, total 10 items"
  ],
  "links": []
}
```

---

## me/2024/projects

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "project_id": 19176,
      "teacher_id": 5635,
      "author": "NEVEU Nicolas",
      "name": "Linux 2 - Shell et DNS 2i2",
      "update_date": 1745252653143,
      "update_user": "NEVEU Nicolas",
      "course_name": "S2 - linux administration",
      "discipline_id": 143689,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": 1747665600000,
          "project_group_id": 422165,
          "project_id": 19176,
          "subject_id": 29180,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "KHATCHATRIAN",
              "firstname": "Menoua",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 490791
            },
            {
              "name": "MANARIN",
              "firstname": "Lucie",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 462876
            },
            "... truncated list, total 3 items"
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": 1747672800000,
          "project_group_id": 422166,
          "project_id": 19176,
          "subject_id": 29180,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "LEJEUNE",
              "firstname": "Thomas",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 617010
            },
            {
              "name": "MAHIEUX",
              "firstname": "Barthélémy",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 501272
            },
            "... truncated list, total 3 items"
          ]
        },
        "... truncated list, total 9 items"
      ],
      "steps": [
        {
          "psp_id": 28575,
          "psp_type": "Rendu final",
          "psp_desc": "Upload des scripts et des fichiers de configuration du DNS",
          "psp_limit_date": 1747639800000,
          "pro_id": 19176,
          "psp_number": 1,
          "files": [
            {
              "psf_id": 454459,
              "psf_desc": "",
              "psf_begin_upload": 1747638253494,
              "psf_end_upload": 1747638270663,
              "psf_file": "sqUoNDmjjMh0INpUp79V.sh",
              "psf_role_user": "student",
              "psf_file_size": 856,
              "psf_file_hash": "2f51c2995620e0a5779c80bfb368520f",
              "psf_file_type": ".sh",
              "psp_id": 28575,
              "pgr_id": 422173,
              "u_id": 476668,
              "psf_name": "check suid",
              "links": []
            },
            {
              "psf_id": 454458,
              "psf_desc": "",
              "psf_begin_upload": 1747638226950,
              "psf_end_upload": 1747638244312,
              "psf_file": "d26TTR2V16g7z4hCU2W5.sh",
              "psf_role_user": "student",
              "psf_file_size": 913,
              "psf_file_hash": "24cf7478beae2e3b84e8320520c7e489",
              "psf_file_type": ".sh",
              "psp_id": 28575,
              "pgr_id": 422173,
              "u_id": 476668,
              "psf_name": "config sudo",
              "links": []
            },
            "... truncated list, total 12 items"
          ],
          "links": []
        }
      ],
      "project_files": [
        {
          "links": [],
          "pf_id": 17411,
          "pf_title": "2i - Linux - Projet - 2025-05",
          "pf_file": "VMosAsE4gXuMh2xqdCn1.pdf",
          "pf_crea_date": 1745259749773,
          "pro_id": 19176
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 1727275,
          "pgl_author": "BAR OR Orone",
          "pgl_role_user": "student",
          "pgl_describe": "BAR OR Orone a rejoint le groupe",
          "pgl_date": 1745330105258,
          "pgl_type_action": "join",
          "user_id": 515715,
          "pgr_id": 422169
        },
        {
          "links": [],
          "pgl_id": 1727407,
          "pgl_author": "DIALLO Maimounatou",
          "pgl_role_user": "student",
          "pgl_describe": "DIALLO Maimounatou a rejoint le groupe",
          "pgl_date": 1745334895080,
          "pgl_type_action": "join",
          "user_id": 566947,
          "pgr_id": 422169
        },
        "... truncated list, total 8 items"
      ],
      "is_draft": false,
      "project_type_id": 1,
      "project_computing_tools": "Distribution Debian",
      "project_create_date": 1745252637518,
      "project_detail_plan": "voir pièce jointe",
      "project_hearing_presentation": "A huis clos",
      "project_max_student_group": 3,
      "project_min_student_group": 3,
      "project_personal_work": 15,
      "project_presentation_duration": 15,
      "project_ref_books": "Cours de Linux Administration",
      "project_teaching_goals": "Démontrer la bonne compréhension des concepts vus en cours",
      "project_type_group": "Libre",
      "project_type_presentation": "Démonstration",
      "project_type_presentation_details": "",
      "project_type_subject": "Imposé",
      "rc_id": 304531,
      "trimester_id": 22,
      "year": 2024,
      "links": []
    },
    {
      "project_id": 19038,
      "teacher_id": 7864,
      "author": "DENIER Olivier",
      "name": "Examen Java - 2i2",
      "update_date": 1744796368607,
      "update_user": "DENIER Olivier",
      "course_name": "S2 - programmation orientée objet et langage java",
      "discipline_id": 143695,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 419453,
          "project_id": 19038,
          "subject_id": 28984,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "KHATCHATRIAN",
              "firstname": "Menoua",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 490791
            }
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 419454,
          "project_id": 19038,
          "subject_id": 28984,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "KAKISWA",
              "firstname": "Samuel",
              "promotion": "2ESGIi",
              "option": "",
              "classe": "Classe 2",
              "links": [],
              "u_id": 540288
            }
          ]
        },
        "... truncated list, total 27 items"
      ],
      "steps": [
        {
          "psp_id": 28471,
          "psp_type": "Rendu final",
          "psp_desc": "",
          "psp_limit_date": 1744801200000,
          "pro_id": 19038,
          "psp_number": 1,
          "files": [
            {
              "psf_id": 448303,
              "psf_desc": "",
              "psf_begin_upload": 1744801415740,
              "psf_end_upload": 1744801491933,
              "psf_file": "7KAH9DQvMZC0uYuMorp7.zip",
              "psf_role_user": "student",
              "psf_file_size": 14836,
              "psf_file_hash": "c881251a440a292950745cf819f413ae",
              "psf_file_type": ".zip",
              "psp_id": 28471,
              "pgr_id": 419464,
              "u_id": 566947,
              "psf_name": "tp",
              "links": []
            },
            {
              "psf_id": 448302,
              "psf_desc": "",
              "psf_begin_upload": 1744801350908,
              "psf_end_upload": 1744801368465,
              "psf_file": "UifvTpZZKq4ZF7sHdlQA.zip",
              "psf_role_user": "student",
              "psf_file_size": 2923,
              "psf_file_hash": "20f33191e9371222d02a78d290bf741e",
              "psf_file_type": ".zip",
              "psp_id": 28471,
              "pgr_id": 419466,
              "u_id": 590198,
              "psf_name": "tp",
              "links": []
            },
            "... truncated list, total 26 items"
          ],
          "links": []
        }
      ],
      "project_files": [
        {
          "links": [],
          "pf_id": 17336,
          "pf_title": "Système d'alarme",
          "pf_file": "l4spGYD9r1xb5S6Pdbaf.pdf",
          "pf_crea_date": 1744719429186,
          "pro_id": 19038
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 1720325,
          "pgl_author": "DENIER Olivier",
          "pgl_role_user": "teacher",
          "pgl_describe": "DENIER Olivier a ajouté LUCE--GUEDON Erwan au Groupe 20",
          "pgl_date": 1744550071205,
          "pgl_type_action": "join",
          "user_id": 7864,
          "pgr_id": 419472
        },
        {
          "links": [],
          "pgl_id": 1724141,
          "pgl_author": "LUCE--GUEDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUEDON Erwan a téléchargé le livrable Erwan Luce--Guédon - Examen Java - 2i2",
          "pgl_date": 1744807448763,
          "pgl_type_action": "downloadStepFile",
          "user_id": 479546,
          "pgr_id": 419472
        },
        "... truncated list, total 4 items"
      ],
      "is_draft": false,
      "project_type_id": 1,
      "project_computing_tools": "",
      "project_create_date": 1744542825436,
      "project_detail_plan": "Réalisation d'une application Java",
      "project_hearing_presentation": "",
      "project_max_student_group": 1,
      "project_min_student_group": 1,
      "project_personal_work": 10,
      "project_presentation_duration": 90,
      "project_ref_books": "",
      "project_teaching_goals": "Examen machine",
      "project_type_group": "Imposé",
      "project_type_presentation": "",
      "project_type_presentation_details": "",
      "project_type_subject": "Imposé",
      "rc_id": 304535,
      "trimester_id": 22,
      "year": 2024,
      "links": []
    },
    "... truncated list, total 17 items"
  ],
  "links": []
}
```

---

## me/2024/students

**HTTP Status**: `418`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 1,
  "version": "1",
  "exception": "I'm a teapot",
  "result": {
    "timestamp": 1781539266699,
    "status": 418,
    "error": "I'm a teapot",
    "message": "Cette ressource n'est pas utilisable par ce rôle utilisateur",
    "path": "https://api.kordis.fr:443/me/2024/students"
  }
}
```

---

## me/2024/teachers

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "uid": 6331,
      "firstname": "Patricia",
      "lastname": "DELESTRE",
      "email": "pdelestr@myges.fr",
      "civility": null,
      "profile_type": null,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/teachers/6331"
        },
        {
          "rel": "photo",
          "href": "https://ges-dl.kordis.fr/public/8Wv8aAnjekRz7bqxp-dI2OxbPLarotMO"
        }
      ]
    },
    {
      "uid": 1000001086,
      "firstname": "Philippe",
      "lastname": "TISSOT",
      "email": "ptissot@myges.fr",
      "civility": null,
      "profile_type": null,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/teachers/1000001086"
        },
        {
          "rel": "photo",
          "href": "https://ges-dl.kordis.fr/public/8Wv8aAnjekRz7bqxp-dI2NJ4jKDIUYyqs2zVn5EDR7s"
        }
      ]
    },
    "... truncated list, total 24 items"
  ],
  "links": []
}
```

---

## me/2025/absences

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "date": 1768546800000,
      "course_name": "T2 - théorie des langages et compilation",
      "justified": false,
      "trimester": 22,
      "trimester_name": "Semestre 2",
      "type": "m",
      "year": 2025,
      "links": []
    },
    {
      "date": 1761291900000,
      "course_name": "T1 - product building et low code",
      "justified": false,
      "trimester": 21,
      "trimester_name": "Semestre 1",
      "type": "m",
      "year": 2025,
      "links": []
    },
    "... truncated list, total 3 items"
  ],
  "links": []
}
```

---

## me/2025/annualDocuments

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "category": "ACTIVITES ASSOCIATIVES",
      "document_id": 540503,
      "extension": ".docx",
      "filename": "BVPK8abzSG.docx",
      "last_update": 1780646154279,
      "name": "Fiche d'identité création Associations / Laboratoires",
      "school_id": 7,
      "school_name": "ESGI",
      "links": []
    },
    {
      "category": "ACTIVITES ASSOCIATIVES",
      "document_id": 541170,
      "extension": ".pdf",
      "filename": "DMdTtBKraC.pdf",
      "last_update": 1780646061111,
      "name": "REPERTOIRE ASSOCIATIF DE L’ESGI 25-26",
      "school_id": 7,
      "school_name": "ESGI",
      "links": []
    },
    "... truncated list, total 36 items"
  ],
  "links": []
}
```

---

## me/2025/classes

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "name": "3ESGI AL CL A ALT RO",
      "description": "3ESGI AL Classe 1 ALT RO",
      "promotion": "BCH_SE3_000_ALT",
      "school": "ESGI",
      "year": 2025,
      "trimester": 73,
      "puid": 23129,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/classes/23129"
        },
        {
          "rel": "students",
          "href": "https://api.kordis.fr/me/classes/23129/students"
        }
      ]
    },
    {
      "name": "3ESGI AL CL A ALT RO",
      "description": "3ESGI AL Classe 1 ALT RO",
      "promotion": "BCH_SE3_000_ALT",
      "school": "ESGI",
      "year": 2025,
      "trimester": 22,
      "puid": 23129,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/classes/23129"
        },
        {
          "rel": "students",
          "href": "https://api.kordis.fr/me/classes/23129/students"
        }
      ]
    },
    "... truncated list, total 3 items"
  ],
  "links": []
}
```

---

## me/2025/courses

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "coef": 3.0,
      "ects": 3.0,
      "name": "T1 - algorithmique avancée : listes, tris et arbres",
      "teacher": "M. RAYNAL",
      "trimester": "Semestre 1",
      "year": 2025,
      "links": [],
      "has_documents": true,
      "has_grades": true,
      "nb_students": 22,
      "rc_id": 328195,
      "school_id": 7,
      "student_group_id": 41038,
      "student_group_name": "3 ESGI - T1 - AL 1 - Paris",
      "syllabus_id": 51504,
      "teacher_id": 1000000485,
      "trimester_id": 21
    },
    {
      "coef": 2.0,
      "ects": 2.0,
      "name": "B1 - nosql, application aux graphes avec graphql",
      "teacher": "M. CARLIER",
      "trimester": "Semestre 1",
      "year": 2025,
      "links": [],
      "has_documents": true,
      "has_grades": true,
      "nb_students": 22,
      "rc_id": 328196,
      "school_id": 7,
      "student_group_id": 41038,
      "student_group_name": "3 ESGI - T1 - AL 1 - Paris",
      "syllabus_id": 52072,
      "teacher_id": 1000023677,
      "trimester_id": 21
    },
    "... truncated list, total 28 items"
  ],
  "links": []
}
```

---

## me/2025/grades

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "course": "T1 - programme open lab",
      "code": "",
      "grades": [],
      "bonus": 0.0,
      "exam": null,
      "average": null,
      "trimester": 21,
      "trimester_name": "Semestre 1",
      "year": 2025,
      "rc_id": 338004,
      "ects": "N.C.",
      "coef": "N.C.",
      "teacher_civility": "Mme",
      "teacher_first_name": "Johanna",
      "teacher_last_name": "FILIOL",
      "absences": 0,
      "lates": 0,
      "letter_mark": "F",
      "ccaverage": 0.0,
      "links": []
    },
    {
      "course": "T2 - elearning",
      "code": "",
      "grades": [],
      "bonus": 0.0,
      "exam": null,
      "average": null,
      "trimester": 22,
      "trimester_name": "Semestre 2",
      "year": 2025,
      "rc_id": 351859,
      "ects": "N.C.",
      "coef": "N.C.",
      "teacher_civility": "Mlle",
      "teacher_first_name": "Lidia",
      "teacher_last_name": "RAMOS",
      "absences": 0,
      "lates": 0,
      "letter_mark": "F",
      "ccaverage": 0.0,
      "links": []
    },
    "... truncated list, total 29 items"
  ],
  "links": []
}
```

---

## me/2025/practicals

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "project_id": 23271,
      "teacher_id": 1000002852,
      "author": "DOMINGUES Thomas",
      "name": "Examen final - Docker",
      "update_date": 1778144802312,
      "update_user": "DOMINGUES Thomas",
      "course_name": "T2 - outils de développement et conteneurisation avancée",
      "discipline_id": 199977,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 507612,
          "project_id": 23271,
          "subject_id": 34003,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "BLACKETT",
              "firstname": "Lenny",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 466858
            }
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 507613,
          "project_id": 23271,
          "subject_id": 34003,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "LAVAL",
              "firstname": "Malo",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 466859
            }
          ]
        },
        "... truncated list, total 24 items"
      ],
      "steps": [
        {
          "psp_id": 34459,
          "psp_type": "Rendu final",
          "psp_desc": "Rendu du projet",
          "psp_limit_date": 1778160600000,
          "pro_id": 23271,
          "psp_number": 1,
          "files": [
            {
              "psf_id": 535977,
              "psf_desc": "",
              "psf_begin_upload": 1778160631401,
              "psf_end_upload": 1778160681771,
              "psf_file": "rmYA4N5nSVDaq8s48rtP.zip",
              "psf_role_user": "student",
              "psf_file_size": 78719345,
              "psf_file_hash": "7f17e1974b7720ff6ce2abe6e1c61c7c",
              "psf_file_type": ".zip",
              "psp_id": 34459,
              "pgr_id": 507631,
              "u_id": 462871,
              "psf_name": "rendu-v2",
              "links": []
            },
            {
              "psf_id": 535976,
              "psf_desc": "",
              "psf_begin_upload": 1778160653995,
              "psf_end_upload": 1778160671702,
              "psf_file": "SI3ApwJCD2DhNwcVdevI.zip",
              "psf_role_user": "student",
              "psf_file_size": 138695,
              "psf_file_hash": "b01493ace75c61f59904cf290bc0dc6d",
              "psf_file_type": ".zip",
              "psp_id": 34459,
              "pgr_id": 507625,
              "u_id": 491629,
              "psf_name": "Exam Tom Georgin",
              "links": []
            },
            "... truncated list, total 24 items"
          ],
          "links": []
        }
      ],
      "project_files": null,
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 2056138,
          "pgl_author": "DOMINGUES Thomas",
          "pgl_role_user": "teacher",
          "pgl_describe": "DOMINGUES Thomas a ajouté LUCE--GUÉDON Erwan au Groupe 17",
          "pgl_date": 1778152017912,
          "pgl_type_action": "join",
          "user_id": 1000002852,
          "pgr_id": 507628
        },
        {
          "links": [],
          "pgl_id": 2056463,
          "pgl_author": "LUCE--GUÉDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUÉDON Erwan a supprimé le livrable TP-Erwan-Luce--Guédon-3AL1",
          "pgl_date": 1778167772927,
          "pgl_type_action": "deleteStepFile",
          "user_id": 479546,
          "pgr_id": 507628
        },
        "... truncated list, total 6 items"
      ],
      "is_draft": false,
      "project_type_id": 2,
      "project_computing_tools": "git\r\ndocker",
      "project_create_date": 1778144802312,
      "project_detail_plan": "",
      "project_hearing_presentation": null,
      "project_max_student_group": 1,
      "project_min_student_group": 1,
      "project_personal_work": 1,
      "project_presentation_duration": 0,
      "project_ref_books": "",
      "project_teaching_goals": "Conteneuriser une application distribuée avec Docker.",
      "project_type_group": "Imposé",
      "project_type_presentation": null,
      "project_type_presentation_details": null,
      "project_type_subject": "Imposé",
      "rc_id": 342274,
      "trimester_id": 22,
      "year": 2025,
      "links": []
    },
    {
      "project_id": 22661,
      "teacher_id": 1000042467,
      "author": "MACHAVOINE Rémy",
      "name": "API REST",
      "update_date": 1773306458788,
      "update_user": "MACHAVOINE Rémy",
      "course_name": "T2 - api avec nodejs",
      "discipline_id": 188667,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 496745,
          "project_id": 22661,
          "subject_id": 33330,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "LUCE--GUÉDON",
              "firstname": "Erwan",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 479546
            }
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 496746,
          "project_id": 22661,
          "subject_id": 33330,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "ADJIRI",
              "firstname": "Mohamed Racim",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 640922
            }
          ]
        },
        "... truncated list, total 46 items"
      ],
      "steps": [
        {
          "psp_id": 33536,
          "psp_type": "Rendu final",
          "psp_desc": "Rendu Final",
          "psp_limit_date": 1773310800000,
          "pro_id": 22661,
          "psp_number": 1,
          "files": [
            {
              "psf_id": 523077,
              "psf_desc": "",
              "psf_begin_upload": 1773317507676,
              "psf_end_upload": 1773317546754,
              "psf_file": "8iDlfrpXakZSl0G1VEEk.md",
              "psf_role_user": "student",
              "psf_file_size": 2966,
              "psf_file_hash": "9ec35e5b4d401c31cd59cfbbe273cca1",
              "psf_file_type": ".md",
              "psp_id": 33536,
              "pgr_id": 496753,
              "u_id": 466858,
              "psf_name": "Readme",
              "links": []
            },
            {
              "psf_id": 523038,
              "psf_desc": "",
              "psf_begin_upload": 1773311711022,
              "psf_end_upload": 1773313811292,
              "psf_file": "oPJmDAgD4Gux1s64ES4u.zip",
              "psf_role_user": "student",
              "psf_file_size": 25375843,
              "psf_file_hash": "99749a249a9f9b9bef5efd92d3901473",
              "psf_file_type": ".zip",
              "psp_id": 33536,
              "pgr_id": 496769,
              "u_id": 491629,
              "psf_name": "CC Tom Georgin",
              "links": []
            },
            "... truncated list, total 48 items"
          ],
          "links": []
        }
      ],
      "project_files": [
        {
          "links": [],
          "pf_id": 20564,
          "pf_title": "Sujet",
          "pf_file": "N1p568yLpHxBo8wzUD7I.pdf",
          "pf_crea_date": 1773310055854,
          "pro_id": 22661
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 2008046,
          "pgl_author": "LUCE--GUÉDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUÉDON Erwan a rejoint le groupe",
          "pgl_date": 1773309233730,
          "pgl_type_action": "join",
          "user_id": 479546,
          "pgr_id": 496745
        },
        {
          "links": [],
          "pgl_id": 2008363,
          "pgl_author": "LUCE--GUÉDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUÉDON Erwan a envoyé le livrable TP-Erwan-Luce--Guédon-3AL1",
          "pgl_date": 1773314939283,
          "pgl_type_action": "uploadStepFile",
          "user_id": 479546,
          "pgr_id": 496745
        },
        "... truncated list, total 4 items"
      ],
      "is_draft": false,
      "project_type_id": 2,
      "project_computing_tools": "",
      "project_create_date": 1773305580939,
      "project_detail_plan": "",
      "project_hearing_presentation": null,
      "project_max_student_group": 1,
      "project_min_student_group": 1,
      "project_personal_work": 1,
      "project_presentation_duration": 0,
      "project_ref_books": "",
      "project_teaching_goals": "Créer une API REST. Pour plus de détails regarder la pièce jointe.",
      "project_type_group": "Libre",
      "project_type_presentation": null,
      "project_type_presentation_details": null,
      "project_type_subject": "Imposé",
      "rc_id": 340617,
      "trimester_id": 22,
      "year": 2025,
      "links": []
    },
    "... truncated list, total 7 items"
  ],
  "links": []
}
```

---

## me/2025/projects

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "project_id": 23680,
      "teacher_id": 1000048036,
      "author": "MOLINS Marie",
      "name": "Design Thinking",
      "update_date": 1781518350689,
      "update_user": "MOLINS Marie",
      "course_name": "T3 - design thinking",
      "discipline_id": 209440,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 1",
          "date_presentation": null,
          "project_group_id": 515080,
          "project_id": 23680,
          "subject_id": 34464,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "LUCE--GUÉDON",
              "firstname": "Erwan",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 479546
            },
            {
              "name": "PLA",
              "firstname": "Gabriel",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 483595
            },
            "... truncated list, total 4 items"
          ]
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": null,
          "project_group_id": 515081,
          "project_id": 23680,
          "subject_id": 34464,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "BLACKETT",
              "firstname": "Lenny",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 466858
            },
            {
              "name": "GARCIA PADRON",
              "firstname": "Sarah Victoria del Carmen",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 460740
            },
            "... truncated list, total 5 items"
          ]
        },
        "... truncated list, total 6 items"
      ],
      "steps": [
        {
          "psp_id": 35080,
          "psp_type": "Etape intermédiaire",
          "psp_desc": "Lancement projet et constitution des groupes",
          "psp_limit_date": 1780386300000,
          "pro_id": 23680,
          "psp_number": 1,
          "files": null,
          "links": []
        }
      ],
      "project_files": null,
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 2083671,
          "pgl_author": "NOURRY Chloé",
          "pgl_role_user": "administration",
          "pgl_describe": "NOURRY Chloé a affecté LUCE--GUÉDON Erwan au groupe",
          "pgl_date": 1781523650922,
          "pgl_type_action": "join",
          "user_id": 210003171,
          "pgr_id": 515080
        },
        {
          "links": [],
          "pgl_id": 2083673,
          "pgl_author": "NOURRY Chloé",
          "pgl_role_user": "administration",
          "pgl_describe": "NOURRY Chloé a affecté PLA Gabriel au groupe",
          "pgl_date": 1781523662119,
          "pgl_type_action": "join",
          "user_id": 210003171,
          "pgr_id": 515080
        },
        "... truncated list, total 4 items"
      ],
      "is_draft": false,
      "project_type_id": 1,
      "project_computing_tools": "Figma, Figjam",
      "project_create_date": 1781275028530,
      "project_detail_plan": "Mettre en application les étapes du DESIGN THINKING vues en cours : \r\n\r\n→ Activité d'application\r\n→ Phase d’observation en DT : commencez par vous interroger : à votre avis, quels sont les utilisateur·ices\r\naffecté·es par cette défaillance ? Ici votre tâche est de cartographier les parties prenantes :\r\n→ Les patient·es (qui ne sont pas remboursés ou dont le dossier médical est incomplet).\r\n→ Les équipes d’analystes de données (frustration, impuissance, démission).\r\n→ Les utilisateur·ices métier (qui ont fini par se plaindre).\r\n→ La direction (qui ne savait rien).\r\n→ Les DBA (arrogance ? surcharge ? déni ?).\r\nQuestion à se poser : pourquoi les alertes de l'analyste ont-elles été ignorées alors que celleux des utilisateur·ices\r\nmétier ont été entendus ?\r\nQuel est le sentiment d'insécurité pour un·e patient·e dont on a perdu 40% des données de santé ?\r\n→ Rédigez votre réponse en une demi-page d’écriture sans IA (1000 signes)\r\n→ Définir votre problématique de design : formulez le vrai problème : la base de données ne s’est pas « cassée\r\ntoute seule ».\r\n→ Rédigez une phrase « Comment pourrions-nous... » qui capture le cœur du dysfonctionnement.\r\n→ Voici les pistes que j’attends dans votre formulation :\r\n→ Le problème organisationnel (l’absence de boucle de retour\r\n→ Feedback loop), la culture du silence, ou l'absence de monitoring orienté \"métier\".\r\n→ Idéer vos solutions techniques : proposez 3 pistes de solutions variées.\r\n→ Piste technique : monitoring automatique ? tests d'intégrité des données en temps réel ?\r\n→ Piste processus : réunions obligatoires de validation des données ? Procédures d'escalade claires ?\r\n→ Piste humaine / culturelle : canal d'alerte anonyme ? Valorisation de la parole des analystes junior ?\r\nChangement de gouvernance des DBA ?\r\n→ Consigne : annotez toutes vos idées en groupe et choisissez ensuite la meilleure. Expliquez et justifiez\r\nvotre choix\r\n→ Prototyper votre concept de la solution : à partir de votre choix collectif de solution, imaginez un tableau de\r\nbord de santé des données ou une procédure d'urgence data.\r\n→ Décrivez comment cela fonctionne au quotidien : qui reçoit l'alerte ? Que se passe-t-il si un·e analyste dit\r\n\"il manque des données\" ?\r\n→ Quel est le protocole des 24 premières heures ?\r\n→ Votre objectif : montrer comment on rend la donnée \"visible\" et \"urgente\" pour tout le monde.\r\n→ Tester vos compromis et les risques pris : c'est l'étape cruciale pour ce cas. Identifiez les compromis (trade-\r\noffs) de votre solution.\r\nUn exemple concret : plus d'alertes = risque de fausses alertes critiques et fatigue des équipes. Si vous indiquez\r\ntrop de transparence totale dans votre solution de procédure d’urgence ou dans votre tableau de bord des\r\ndonnées, vous risquez un vent de panique interne ou de responsabilité légale immédiate.\r\n→ Comment allez-vous tester votre nouveau processus sans attendre la prochaine catastrophe ?\r\n→ Quel serait votre protocole pour réaliser une simulation de crise, audit à blanc, etc.\r\n→ Comment vous assurer que la culture de \"l'écoute\" est réellement installée et pas juste sur le papier ?\r\n→ Vous rendrez ce travail en équipe sous format Figma Slides. Pour vos solutions, utilisez Figjam pour formuler\r\nvos schémas et les commenter. Votre document Figma Slide devra comprendre :\r\n→ 1 page de présentation du contexte (-2 points si copié-collé de l’énoncé)\r\n→ 1 page de rédaction pour la phase d’observation // OBSERVER\r\n→ 1 page de définition de la problématique // DÉFINIR\r\n→ 1 page d’idéation de vos solutions et votre choix final // CONCEVOIR\r\n→ 1 page de prototype sous format Figjam inclus dans la présentation // PROTOTYPER\r\n→ 1 page de mentions des compris et risques pris // TEST\r\n→ 1 page de protocole de simulation en cas de crise.\r\n→ Vous présenterez vos slides Figma à la classe en fin d’atelier.\r\n→ L’oral durera 10-15 min par groupe : 5 min de présentation, 5-10min de questions et feedback collectif.",
      "project_hearing_presentation": "Devant la promotion",
      "project_max_student_group": 6,
      "project_min_student_group": 4,
      "project_personal_work": 3,
      "project_presentation_duration": 15,
      "project_ref_books": "→ Change by Design: How Design Thinking Transforms Organizations and Inspires Innovation. Tim Brown, New York: Harper Business.\r\n→ Wicked Problems in Design Thinking, Design Issues, Richard Buchanan",
      "project_teaching_goals": "Soutenance de projet",
      "project_type_group": "Imposé",
      "project_type_presentation": "Présentation / PowerPoint",
      "project_type_presentation_details": "",
      "project_type_subject": "Imposé",
      "rc_id": 349780,
      "trimester_id": 73,
      "year": 2025,
      "links": []
    },
    {
      "project_id": 22843,
      "teacher_id": 1000074019,
      "author": "RUHLMANN Stéphane",
      "name": "Projet final ReactJS",
      "update_date": 1775827681186,
      "update_user": "RUHLMANN Stéphane",
      "course_name": "T2 - développement frontend avec react",
      "discipline_id": 197746,
      "groups": [
        {
          "links": [],
          "group_name": "Groupe 13",
          "date_presentation": null,
          "project_group_id": 499912,
          "project_id": 22843,
          "subject_id": 33544,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": null
        },
        {
          "links": [],
          "group_name": "Groupe 2",
          "date_presentation": 1778067000000,
          "project_group_id": 499901,
          "project_id": 22843,
          "subject_id": 33544,
          "subject_validated": true,
          "teacher_comment": null,
          "teacher_intern_comment": null,
          "project_group_students": [
            {
              "name": "GRADOS",
              "firstname": "Maxence",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 605951
            },
            {
              "name": "MOUNKASSA",
              "firstname": "Thomas",
              "promotion": "BCH_SE3_000_ALT",
              "option": "",
              "classe": "3ESGI AL CL A ALT RO",
              "links": [],
              "u_id": 462871
            },
            "... truncated list, total 3 items"
          ]
        },
        "... truncated list, total 16 items"
      ],
      "steps": null,
      "project_files": [
        {
          "links": [],
          "pf_id": 20788,
          "pf_title": "Consignes du projet",
          "pf_file": "7ms3lZlkrnxIEvD0Ekt3.md",
          "pf_crea_date": 1774653214127,
          "pro_id": 22843
        }
      ],
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 2022450,
          "pgl_author": "THIBAUT Rémy",
          "pgl_role_user": "student",
          "pgl_describe": "THIBAUT Rémy a rejoint le groupe",
          "pgl_date": 1774781764154,
          "pgl_type_action": "join",
          "user_id": 462082,
          "pgr_id": 499900
        },
        {
          "links": [],
          "pgl_id": 2022452,
          "pgl_author": "LUCE--GUÉDON Erwan",
          "pgl_role_user": "student",
          "pgl_describe": "LUCE--GUÉDON Erwan a rejoint le groupe",
          "pgl_date": 1774784326053,
          "pgl_type_action": "join",
          "user_id": 479546,
          "pgr_id": 499900
        },
        "... truncated list, total 3 items"
      ],
      "is_draft": false,
      "project_type_id": 1,
      "project_computing_tools": "Git\r\nGithub\r\nNodeJS v22+\r\nReactJS\r\nVite",
      "project_create_date": 1774559309409,
      "project_detail_plan": "Projet en groupe de 3 étudiants, réalisé sur ~5 semaines en autonomie. Les étudiants développent un clone simplifié de Vinted (marketplace de vêtements d'occasion) en React + TypeScript, à partir d'un scaffold fourni incluant une API Express locale, le routing et TanStack Query pré-configurés.                                                                                            \r\n                                                                   \r\n- Périmètre obligatoire : catalogue avec recherche/filtres/tri, page détail article, formulaire de création d'annonce avec validation, page \"mes annonces\" avec suppression, système de favoris.\r\n                                                                                                                                       \r\n- Fonctionnalités au choix (2 minimum parmi 4) : brouillon automatique (localStorage), édition d'annonce, tests composants (Vitest), design responsive.                                                                             \r\n                                                                                                                                       \r\n- Rendu : repository GitHub fonctionnel. Soutenance : ~15 min par groupe avec questions individuelles. Ressources libres (IA incluse), mais le code doit être explicable par chaque membre.",
      "project_hearing_presentation": "A huis clos",
      "project_max_student_group": 3,
      "project_min_student_group": 3,
      "project_personal_work": 8,
      "project_presentation_duration": 15,
      "project_ref_books": "",
      "project_teaching_goals": "Développer une application frontend React reproduisant les fonctionnalités clés de Vinted : catalogue avec recherche/filtres/tri, consultation et publication d'annonces, gestion des favoris. Le projet mobilise les concepts vus en cours (composants, état, hooks, appels API, routing) dans un contexte applicatif réaliste avec une API locale fournie.",
      "project_type_group": "Imposé",
      "project_type_presentation": "Démonstration",
      "project_type_presentation_details": "",
      "project_type_subject": "Imposé",
      "rc_id": 340610,
      "trimester_id": 22,
      "year": 2025,
      "links": []
    },
    "... truncated list, total 9 items"
  ],
  "links": []
}
```

---

## me/2025/students

**HTTP Status**: `418`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 1,
  "version": "1",
  "exception": "I'm a teapot",
  "result": {
    "timestamp": 1781539253543,
    "status": 418,
    "error": "I'm a teapot",
    "message": "Cette ressource n'est pas utilisable par ce rôle utilisateur",
    "path": "https://api.kordis.fr:443/me/2025/students"
  }
}
```

---

## me/2025/teachers

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "uid": 4727,
      "firstname": "Frédéric",
      "lastname": "SANANES",
      "email": "sananes@myges.fr",
      "civility": null,
      "profile_type": null,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/teachers/4727"
        },
        {
          "rel": "photo",
          "href": "https://ges-dl.kordis.fr/public/8Wv8aAnjekRz7bqxp-dI2NMk0qrLSKmI"
        }
      ]
    },
    {
      "uid": 1000072455,
      "firstname": "Ilhan",
      "lastname": "IFERGANE",
      "email": "iifergane@myges.fr",
      "civility": null,
      "profile_type": null,
      "links": [
        {
          "rel": "self",
          "href": "https://api.kordis.fr/me/teachers/1000072455"
        },
        {
          "rel": "photo",
          "href": "https://ges-dl.kordis.fr/public/8Wv8aAnjekRz7bqxp-dI2K9EPi4O4DKxpXQfYId9-qU"
        }
      ]
    },
    "... truncated list, total 22 items"
  ],
  "links": []
}
```

---

## me/2026/absences

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/annualDocuments

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [],
  "links": []
}
```

---

## me/2026/classes

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/courses

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/grades

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/practicals

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/projects

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/2026/students

**HTTP Status**: `418`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 1,
  "version": "1",
  "exception": "I'm a teapot",
  "result": {
    "timestamp": 1781539250758,
    "status": 418,
    "error": "I'm a teapot",
    "message": "Cette ressource n'est pas utilisable par ce rôle utilisateur",
    "path": "https://api.kordis.fr:443/me/2026/students"
  }
}
```

---

## me/2026/teachers

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/349780/files

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "oc_id": 602186,
      "label": "SUPPORT DE COURS",
      "extension": ".pdf",
      "type": "cours",
      "category": "",
      "file": null,
      "teacher_name": null,
      "update_date": 1780307102229,
      "links": [
        {
          "rel": "url",
          "href": "https://ges-dl.kordis.fr/private/CQPFEkvJKsW5PcL030-kQg5n9PDAWTGqXaUOpHl1sqM"
        }
      ]
    }
  ],
  "links": []
}
```

---

## me/349780/files/602186

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
[Binary Content (application/pdf), Size: 9201411 bytes]
```

---

## me/349780/syllabus

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "allowed_docs": false,
      "asso_firstname": null,
      "asso_lastname": null,
      "books_reference": "→ Change by Design: How Design\r\nThinking Transforms\r\nOrganizations and Inspires\r\nInnovation. Tim Brown, New York:\r\nHarper Business.\r\n→ Design Patterns Pool Studio Design\r\nPatterns / Pool Studio → Diagramme de déploiement\r\n→ Wicked Problems in Design\r\nThinking, Design Issues, Richard\r\nBuchanan",
      "code": "N.C",
      "coef": 2.0,
      "computing_tools": "Figma",
      "control_types": [
        {
          "links": [],
          "evaluation_type_id": 2,
          "evaluation_label": "Contrôle Continu"
        }
      ],
      "course_name": "T3 - design thinking",
      "create_date": 1779353904465,
      "cr_id": 209440,
      "crossknowledge_reference": "→ MVC (Modèle-Vue-Contrôleur)\nInnowise Meilleurs Patterns d'Architecture Logicielle | Innowise\n→ UML pour les formalisations relationnelles d'un système\n→ Change by Design: How Design Thinking Transforms Organizations and Inspires\nInnovation. Tim Brown, New York:\nHarper Business.\n→ Design Patterns Pool Studio Design\nPatterns / Pool Studio → Diagramme de déploiement\n→ Wicked Problems in Design\nThinking, Design Issues, Richard\nBuchanan",
      "cyberlibris_reference": "",
      "detail_plan": "→ Définition générale du design thinking\r\n→ Rappel historique et acteur·ices clés\r\n→ Phases de développement des 5 piliers du design thinking\r\n→ Les 5 étapes du processus de design thinking appliqués à l'architecture logicielle\r\n→ Phase d'observation\r\n→ Phase de définition\r\n→ Phase d'idéation\r\n→ Phase de prototypage\r\n→ Phase de tests\r\n→ Activité d'application\r\n→ Phase d’observation en DT : commencez par vous interroger : à votre avis, quels sont les utilisateur·ices\r\naffecté·es par cette défaillance ? Ici votre tâche est de cartographier les parties prenantes :\r\n→ Les patient·es (qui ne sont pas remboursés ou dont le dossier médical est incomplet).\r\n→ Les équipes d’analystes de données (frustration, impuissance, démission).\r\n→ Les utilisateur·ices métier (qui ont fini par se plaindre).\r\n→ La direction (qui ne savait rien).\r\n→ Les DBA (arrogance ? surcharge ? déni ?).\r\nQuestion à se poser : pourquoi les alertes de l'analyste ont-elles été ignorées alors que celleux des utilisateur·ices\r\nmétier ont été entendus ?\r\nQuel est le sentiment d'insécurité pour un·e patient·e dont on a perdu 40% des données de santé ?\r\n→ Rédigez votre réponse en une demi-page d’écriture sans IA (1000 signes)\r\n→ Définir votre problématique de design : formulez le vrai problème : la base de données ne s’est pas « cassée\r\ntoute seule ».\r\n→ Rédigez une phrase « Comment pourrions-nous... » qui capture le coeur du dysfonctionnement.\r\n→ Voici les pistes que j’attends dans votre formulation :\r\n→ Le problème organisationnel (l’absence de boucle de retour\r\n→ Feedback loop), la culture du silence, ou l'absence de monitoring orienté \"métier\".\r\n→ Idéer vos solutions techniques : proposez 3 pistes de solutions variées.\r\n→ Piste technique : monitoring automatique ? tests d'intégrité des données en temps réel ?\r\n→ Piste processus : réunions obligatoires de validation des données ? Procédures d'escalade claires ?\r\n→ Piste humaine / culturelle : canal d'alerte anonyme ? Valorisation de la parole des analystes junior ?\r\nChangement de gouvernance des DBA ?\r\n→ Consigne : annotez toutes vos idées en groupe et choisissez ensuite la meilleure. Expliquez et justifiez\r\nvotre choix\r\n→ Prototyper votre concept de la solution : à partir de votre choix collectif de solution, imaginez un tableau de\r\nbord de santé des données ou une procédure d'urgence data.\r\n→ Décrivez comment cela fonctionne au quotidien : qui reçoit l'alerte ? Que se passe-t-il si un·e analyste dit\r\n\"il manque des données\" ?\r\n→ Quel est le protocole des 24 premières heures ?\r\n→ Votre objectif : montrer comment on rend la donnée \"visible\" et \"urgente\" pour tout le monde.\r\n→ Tester vos compromis et les risques pris : c'est l'étape cruciale pour ce cas. Identifiez les compromis (tradeoffs)\r\nde votre solution.\r\nUn exemple concret : plus d'alertes = risque de fausses alertes critiques et fatigue des équipes. Si vous indiquez\r\ntrop de transparence totale dans votre solution de procédure d’urgence ou dans votre tableau de bord des\r\ndonnées, vous risquez un vent de panique interne ou de responsabilité légale immédiate.\r\n→ Comment allez-vous tester votre nouveau processus sans attendre la prochaine catastrophe ?\r\n→ Quel serait votre protocole pour réaliser une simulation de crise, audit à blanc, etc.\r\n→ Comment vous assurer que la culture de \"l'écoute\" est réellement installée et pas juste sur le papier ?\r\n→ Vous rendrez ce travail en équipe sous format Figma Slides. Pour vos solutions, utilisez Figjam pour formuler\r\nvos schémas et les commenter. Votre document Figma Slide devra comprendre :\r\n→ 1 page de présentation du contexte (-2 points si copié-collé de l’énoncé)\r\n→ 1 page de rédaction pour la phase d’observation // OBSERVER\r\n→ 1 page de définition de la problématique // DÉFINIR\r\n→ 1 page d’idéation de vos solutions et votre choix final // CONCEVOIR\r\n→ 1 page de prototype sous format Figjam inclus dans la présentation // PROTOTYPER\r\n→ 1 page de mentions des compris et risques pris // TEST\r\n→ 1 page de protocole de simulation en cas de crise.\r\n→ Vous présenterez vos slides Figma à la classe en fin d’atelier.\r\n→ L’oral durera 10-15 min par groupe : 5 min de présentation, 5-10min de questions et feedback collectif.",
      "ects": 2.0,
      "evaluation_criteria": "Auditer la solution en analysant la visibilité de la solution et du contenu, en recueillant les avis des utilisateurs, en créant les vues tenant compte des besoins des utilisateurs et tenant compte des règles d’accessibilité numérique,\nde conception universelle et d’écoconception afin de formuler des préconisations d’amélioration de l'expérience\nutilisateur.\n\nImplémenter des solutions d'optimisation du code en utilisant des patrons de conception (Design Pattern), en\nappliquant des règles d'optimisation de la complexité algorithmique, en minimisant la mémoire utilisée par la\nsolution logicielle ou applicative pour réduire les problématiques de conception.\n\nParamétrer les composants applicatifs ou logiciels d’après la solution et le support système actuels, en utilisant\nles modèles d’intégration inhérents aux composants logiciels/matériels, en tenant compte du code/programme,\nde l’interface utilisateur et en élaborant les procédures d’installation et de mises à jour associées afin d’optimiser\nla solution logicielle ou applicative.",
      "evaluation_type": "Contrôle Continu",
      "exam_duration": 3,
      "formation": "BCH_SE3_000_ALT - 3 ESGI  - T3 - AL 1 - Paris ",
      "hours_per_week": 15,
      "nextyear": 2026,
      "other_reference": "",
      "personal_work": 5,
      "prerequisite": "Avoir installé Figma Desktop ou la version web sur son ordinateur afin de produire des design lisibles pour les standards professionnels.",
      "promo_id": 7,
      "rc_id": 349780,
      "resp_email": "mmolins@myges.fr",
      "resp_peda_firstname": "Frédéric",
      "resp_peda_lastname": "SANANES",
      "school_id": 7,
      "seance_details": [
        {
          "number": 1,
          "content": "→ Définition générale du design thinking\n",
          "homework": null,
          "ref": null,
          "eval": null,
          "links": [],
          "syl_id": 62227,
          "cont_id": 334277
        },
        {
          "number": 2,
          "content": "→ Rappel historique et acteur·ices clés\n",
          "homework": null,
          "ref": null,
          "eval": null,
          "links": [],
          "syl_id": 62227,
          "cont_id": 334278
        },
        "... truncated list, total 5 items"
      ],
      "skills": [
        {
          "links": [],
          "syl_id": 62227,
          "comp_label": "RNCP36469BC01 - Analyser et définir la stratégie du système d’information",
          "ti_label": "RNCP36469 - Expert en ingénierie du développement et en architecture logicielle",
          "comp_id": 964
        }
      ],
      "syllabus_id": 62227,
      "syllabus_name": "DESIGN THINKING (3AL)",
      "teacher_firstname": "Marie",
      "teacher_id": 1000048036,
      "teacher_lastname": "MOLINS",
      "teaching_goals": "À la fin du cours, les étudiant·es seront capables comprendre les enjeux du design thinking appliqué à l'architecture logicielle, intégrer la notion de prototypage propre à leur profession et se positionner par rapport à\nun \"wicked problem\" en design.",
      "teaching_method": "Pédagogie par projet\r\nControle continu",
      "tri_describe": "Semestre 3",
      "tri_name": "S3",
      "validation": true,
      "year": 2025,
      "links": []
    }
  ],
  "links": []
}
```

---

## me/agenda

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "reservation_id": 2255425,
      "rooms": [
        {
          "links": [],
          "room_id": 63,
          "name": "A06",
          "floor": "RDC",
          "campus": "NATION1",
          "color": "#FF6D01",
          "latitude": "48.8491790",
          "longitude": "2.3897950"
        },
        {
          "links": [],
          "room_id": 64,
          "name": "A07",
          "floor": "RDC",
          "campus": "NATION1",
          "color": "#FF6D01",
          "latitude": "48.8491790",
          "longitude": "2.3897950"
        }
      ],
      "type": "Autre",
      "modality": "",
      "author": 3194,
      "create_date": null,
      "start_date": 1758555000000,
      "end_date": 1758560400000,
      "state": "PUBLISHED",
      "comment": null,
      "classes": null,
      "name": "T1 - c et algorithmes (challenge)",
      "discipline": {
        "coef": null,
        "ects": null,
        "name": null,
        "teacher": " ",
        "trimester": null,
        "year": null,
        "links": [],
        "has_documents": null,
        "has_grades": null,
        "nb_students": 0,
        "rc_id": null,
        "school_id": null,
        "student_group_id": null,
        "student_group_name": null,
        "syllabus_id": null,
        "teacher_id": null,
        "trimester_id": null
      },
      "teacher": " ",
      "promotion": "",
      "prestation_type": 2,
      "is_electronic_signature": false,
      "links": []
    },
    {
      "reservation_id": 2132551,
      "rooms": [
        {
          "links": [],
          "room_id": 21,
          "name": "Salle 12",
          "floor": "1er étage",
          "campus": "ERARD",
          "color": "#FFF56F",
          "latitude": "48.8461620",
          "longitude": "2.3856650"
        }
      ],
      "type": "Cours",
      "modality": "Présentiel",
      "author": 210000556,
      "create_date": null,
      "start_date": 1765544400000,
      "end_date": 1765549800000,
      "state": "PUBLISHED",
      "comment": "tp machine ",
      "classes": null,
      "name": "T1 - langage java avancé : poo, lambda, flux, fxml",
      "discipline": {
        "coef": null,
        "ects": null,
        "name": "T1 - langage java avancé : poo, lambda, flux, fxml",
        "teacher": "M. ERNOT",
        "trimester": "Semestre 1",
        "year": 2025,
        "links": [],
        "has_documents": null,
        "has_grades": null,
        "nb_students": 22,
        "rc_id": 328197,
        "school_id": 7,
        "student_group_id": 41038,
        "student_group_name": "3 ESGI - T1 - AL 1 - Paris",
        "syllabus_id": null,
        "teacher_id": 1000005843,
        "trimester_id": 21
      },
      "teacher": "M. ERNOT",
      "promotion": "",
      "prestation_type": 2,
      "is_electronic_signature": false,
      "links": []
    },
    "... truncated list, total 317 items"
  ],
  "links": []
}
```

---

## me/annualDocuments/596371

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
[Binary Content (application/pdf), Size: 430304 bytes]
```

---

## me/classes/23129/students/2026

**HTTP Status**: `418`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 1,
  "version": "1",
  "exception": "I'm a teapot",
  "result": {
    "timestamp": 1781539285460,
    "status": 418,
    "error": "I'm a teapot",
    "message": "Cette ressource n'est pas utilisable par ce rôle utilisateur",
    "path": "https://api.kordis.fr:443/me/classes/23129/students/2026"
  }
}
```

---

## me/courses/349780/practicals

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/courses/349780/projects

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "project_id": 23680,
      "teacher_id": 1000048036,
      "author": "MOLINS Marie",
      "name": "Design Thinking",
      "update_date": 1781518350689,
      "update_user": "MOLINS Marie",
      "course_name": "T3 - design thinking",
      "discipline_id": 209440,
      "groups": null,
      "steps": [
        {
          "psp_id": 35080,
          "psp_type": "Etape intermédiaire",
          "psp_desc": "Lancement projet et constitution des groupes",
          "psp_limit_date": 1780386300000,
          "pro_id": 23680,
          "psp_number": 1,
          "files": null,
          "links": []
        }
      ],
      "project_files": null,
      "project_group_logs": [
        {
          "links": [],
          "pgl_id": 2083671,
          "pgl_author": "NOURRY Chloé",
          "pgl_role_user": "administration",
          "pgl_describe": "NOURRY Chloé a affecté LUCE--GUÉDON Erwan au groupe",
          "pgl_date": 1781523650922,
          "pgl_type_action": "join",
          "user_id": 210003171,
          "pgr_id": 515080
        },
        {
          "links": [],
          "pgl_id": 2083673,
          "pgl_author": "NOURRY Chloé",
          "pgl_role_user": "administration",
          "pgl_describe": "NOURRY Chloé a affecté PLA Gabriel au groupe",
          "pgl_date": 1781523662119,
          "pgl_type_action": "join",
          "user_id": 210003171,
          "pgr_id": 515080
        },
        "... truncated list, total 4 items"
      ],
      "is_draft": false,
      "project_type_id": 1,
      "project_computing_tools": "Figma, Figjam",
      "project_create_date": 1781275028530,
      "project_detail_plan": "Mettre en application les étapes du DESIGN THINKING vues en cours : \r\n\r\n→ Activité d'application\r\n→ Phase d’observation en DT : commencez par vous interroger : à votre avis, quels sont les utilisateur·ices\r\naffecté·es par cette défaillance ? Ici votre tâche est de cartographier les parties prenantes :\r\n→ Les patient·es (qui ne sont pas remboursés ou dont le dossier médical est incomplet).\r\n→ Les équipes d’analystes de données (frustration, impuissance, démission).\r\n→ Les utilisateur·ices métier (qui ont fini par se plaindre).\r\n→ La direction (qui ne savait rien).\r\n→ Les DBA (arrogance ? surcharge ? déni ?).\r\nQuestion à se poser : pourquoi les alertes de l'analyste ont-elles été ignorées alors que celleux des utilisateur·ices\r\nmétier ont été entendus ?\r\nQuel est le sentiment d'insécurité pour un·e patient·e dont on a perdu 40% des données de santé ?\r\n→ Rédigez votre réponse en une demi-page d’écriture sans IA (1000 signes)\r\n→ Définir votre problématique de design : formulez le vrai problème : la base de données ne s’est pas « cassée\r\ntoute seule ».\r\n→ Rédigez une phrase « Comment pourrions-nous... » qui capture le cœur du dysfonctionnement.\r\n→ Voici les pistes que j’attends dans votre formulation :\r\n→ Le problème organisationnel (l’absence de boucle de retour\r\n→ Feedback loop), la culture du silence, ou l'absence de monitoring orienté \"métier\".\r\n→ Idéer vos solutions techniques : proposez 3 pistes de solutions variées.\r\n→ Piste technique : monitoring automatique ? tests d'intégrité des données en temps réel ?\r\n→ Piste processus : réunions obligatoires de validation des données ? Procédures d'escalade claires ?\r\n→ Piste humaine / culturelle : canal d'alerte anonyme ? Valorisation de la parole des analystes junior ?\r\nChangement de gouvernance des DBA ?\r\n→ Consigne : annotez toutes vos idées en groupe et choisissez ensuite la meilleure. Expliquez et justifiez\r\nvotre choix\r\n→ Prototyper votre concept de la solution : à partir de votre choix collectif de solution, imaginez un tableau de\r\nbord de santé des données ou une procédure d'urgence data.\r\n→ Décrivez comment cela fonctionne au quotidien : qui reçoit l'alerte ? Que se passe-t-il si un·e analyste dit\r\n\"il manque des données\" ?\r\n→ Quel est le protocole des 24 premières heures ?\r\n→ Votre objectif : montrer comment on rend la donnée \"visible\" et \"urgente\" pour tout le monde.\r\n→ Tester vos compromis et les risques pris : c'est l'étape cruciale pour ce cas. Identifiez les compromis (trade-\r\noffs) de votre solution.\r\nUn exemple concret : plus d'alertes = risque de fausses alertes critiques et fatigue des équipes. Si vous indiquez\r\ntrop de transparence totale dans votre solution de procédure d’urgence ou dans votre tableau de bord des\r\ndonnées, vous risquez un vent de panique interne ou de responsabilité légale immédiate.\r\n→ Comment allez-vous tester votre nouveau processus sans attendre la prochaine catastrophe ?\r\n→ Quel serait votre protocole pour réaliser une simulation de crise, audit à blanc, etc.\r\n→ Comment vous assurer que la culture de \"l'écoute\" est réellement installée et pas juste sur le papier ?\r\n→ Vous rendrez ce travail en équipe sous format Figma Slides. Pour vos solutions, utilisez Figjam pour formuler\r\nvos schémas et les commenter. Votre document Figma Slide devra comprendre :\r\n→ 1 page de présentation du contexte (-2 points si copié-collé de l’énoncé)\r\n→ 1 page de rédaction pour la phase d’observation // OBSERVER\r\n→ 1 page de définition de la problématique // DÉFINIR\r\n→ 1 page d’idéation de vos solutions et votre choix final // CONCEVOIR\r\n→ 1 page de prototype sous format Figjam inclus dans la présentation // PROTOTYPER\r\n→ 1 page de mentions des compris et risques pris // TEST\r\n→ 1 page de protocole de simulation en cas de crise.\r\n→ Vous présenterez vos slides Figma à la classe en fin d’atelier.\r\n→ L’oral durera 10-15 min par groupe : 5 min de présentation, 5-10min de questions et feedback collectif.",
      "project_hearing_presentation": "Devant la promotion",
      "project_max_student_group": 6,
      "project_min_student_group": 4,
      "project_personal_work": 3,
      "project_presentation_duration": 15,
      "project_ref_books": "→ Change by Design: How Design Thinking Transforms Organizations and Inspires Innovation. Tim Brown, New York: Harper Business.\r\n→ Wicked Problems in Design Thinking, Design Issues, Richard Buchanan",
      "project_teaching_goals": "Soutenance de projet",
      "project_type_group": "Imposé",
      "project_type_presentation": "Présentation / PowerPoint",
      "project_type_presentation_details": "",
      "project_type_subject": "Imposé",
      "rc_id": 349780,
      "trimester_id": 73,
      "year": 2025,
      "links": []
    }
  ],
  "links": []
}
```

---

## me/courses/349780/projects/16834/groups/371279

**HTTP Status**: `405`

**Content-Type**: ``

**Response Excerpt / JSON Structure**:
```json

```

---

## me/cvec

**HTTP Status**: `405`

**Content-Type**: ``

**Response Excerpt / JSON Structure**:
```json

```

---

## me/internalrules

**HTTP Status**: `405`

**Content-Type**: ``

**Response Excerpt / JSON Structure**:
```json

```

---

## me/minimumVersion

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "type": "skolae_app_version",
    "label": "Version minimum requise de l'app mobile Skolae",
    "value": "3.5.0"
  }
}
```

---

## me/news

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "content": [
      {
        "ne_id": 4947,
        "title": "Tout comprendre sur la technologie durable",
        "author": "ESGI",
        "summary": null,
        "text": null,
        "html": "<p><span style=\"font-size: medium;\">La technologie qui alimente les entreprises&nbsp;: appareils des salari&eacute;s, centres de donn&eacute;es et r&eacute;seaux informatiques, a, aujourd&rsquo;hui une empreinte carbone &eacute;lev&eacute;e. Avec la croissance exponentielle des donn&eacute;es et de l'IA, ainsi que les progr&egrave;s technologiques, de plus en plus de ressources sont n&eacute;cessaires pour alimenter l&rsquo;entreprise.</span></p>\n<p><span style=\"font-size: medium;\">La technologie durable &eacute;merge donc comme une r&eacute;ponse cruciale aux d&eacute;fis environnementaux contemporains.</span></p>\n<p><span style=\"font-size: medium;\">&nbsp;</span></p>\n<h2><span style=\"font-size: medium;\"><strong>Principes de bases de la technologie durable</strong></span></h2>\n<p><span style=\"font-size: medium;\">La technologie durable englobe des solutions ayant pour but de r&eacute;duire l&rsquo;impact environnemental d&rsquo;une infrastructure informatique tout au long de son cycle de vie.</span></p>\n<p><span style=\"font-size: medium;\">Le but est d&rsquo;utiliser la technologie de mani&egrave;re &agrave; r&eacute;duire son impact sur l&rsquo;environnement et d&rsquo;assurer sa durabilit&eacute; sur le long terme.</span></p>\n<p><span style=\"font-size: medium;\">La technologie durable fait intervenir des strat&eacute;gies et des pratiques qui visent &agrave; r&eacute;duire la consommation d&rsquo;&eacute;nergie, &agrave; minimiser les d&eacute;chets et &agrave; promouvoir une prise de d&eacute;cision respectueuse de l&rsquo;environnement au sein du secteur IT.</span></p>\n<p><span style=\"font-size: medium;\">&nbsp;</span></p>\n<p><span style=\"font-size: medium;\"><img title=\"Article Technologie Durable\" src=\"https://www.esgi.fr/ecole-informatique/MA-Tech-durable.png\" alt=\"Technologie Durable\" width=\"912\" height=\"630\" /></span></p>\n<p>&nbsp;</p>\n<h2><span style=\"font-size: medium;\"><strong>Comment cela se concr&eacute;tise ?</strong></span></h2>\n<p><span style=\"font-size: medium;\">Les centres de donn&eacute;es et les &eacute;quipements informatiques consomment d'&eacute;normes quantit&eacute;s d'&eacute;nergie. La mise en &oelig;uvre de pratiques visant &agrave; optimiser l'efficacit&eacute; &eacute;nerg&eacute;tique, telles que la virtualisation des serveurs, la gestion intelligente de l'alimentation et l'utilisation de sources d'&eacute;nergie renouvelable, permet de r&eacute;duire cette consommation et, par cons&eacute;quent, l'empreinte carbone de l'industrie.</span></p>\n<p><span style=\"font-size: medium;\">Certaines entreprises du secteur IT cherchent &agrave; concevoir des produits informatiques avec des mat&eacute;riaux recyclables, durables et &eacute;co&eacute;nerg&eacute;tiques afin de r&eacute;duire les d&eacute;chets &eacute;lectroniques et l&rsquo;utilisation de ressources non renouvelables. De plus, la conception modulaire permet de prolonger la dur&eacute;e de vie des appareils en facilitant les mises &agrave; niveau et les r&eacute;parations.</span></p>\n<p><span style=\"font-size: medium;\">Et bien s&ucirc;r, impossible de ne pas parler du sujet du recyclage et de la gestion des d&eacute;chets. La r&eacute;cup&eacute;ration des composants &eacute;lectroniques et leur recyclage appropri&eacute; sont essentiels pour r&eacute;duire l'impact environnemental de l'informatique. Les programmes de recyclage et les initiatives visant &agrave; promouvoir la r&eacute;utilisation des &eacute;quipements contribuent &agrave; limiter la quantit&eacute; de d&eacute;chets &eacute;lectroniques envoy&eacute;s en d&eacute;charge.</span></p>\n<p><span style=\"font-size: medium;\"><strong>&nbsp;</strong></span></p>\n<h2><span style=\"font-size: medium;\"><strong>Pourquoi la technologie durable est importante&nbsp;?</strong></span></h2>\n<p><span style=\"font-size: medium;\">Chaque ann&eacute;e, la fabrication et la destruction des objets technologiques contribuent largement aux &eacute;missions de gaz &agrave; effet de serre. En optant pour des technologies durables, leur impact environnemental se voit r&eacute;duit.</span></p>\n<p><span style=\"font-size: medium;\">Cela nous permet &eacute;galement de revoir notre rapport avec les innovations actuelles, telles que l&rsquo;IoT, l&rsquo;IA et le Cloud, en s&rsquo;en servant comme d&rsquo;un outil afin d'aider &agrave; relever les d&eacute;fis environnementaux et sociaux d&rsquo;aujourd&rsquo;hui et de demain.</span></p>\n<p><span style=\"font-size: medium;\">Gr&acirc;ce &agrave; la technologie durable, les entreprises ont la possibilit&eacute; de r&eacute;inventer leurs mod&egrave;les &eacute;conomiques pour &eacute;quilibrer leurs performances financi&egrave;res et les objectifs durabilit&eacute;.&nbsp;</span></p>\n<p>&nbsp;</p>\n<div class=\"OutlineElement Ltr SCXW138226291 BCX0\">\n<p class=\"Paragraph SCXW138226291 BCX0\"><span style=\"font-size: medium;\"><span class=\"TextRun Highlight SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">De</span><span class=\"NormalTextRun SCXW138226291 BCX0\">&nbsp;nombreuses</span></span><span class=\"TextRun SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\">&nbsp;urgences&nbsp;</span><span class=\"TextRun Highlight SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">climatique</span><span class=\"NormalTextRun SCXW138226291 BCX0\">s</span></span><span class=\"TextRun SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">&nbsp;ont &eacute;t&eacute; mises en lumi&egrave;re ces derni&egrave;res ann&eacute;es. Une charte num&eacute;rique a donc &eacute;t&eacute; tout naturellement mise en place.&nbsp;</span></span></span><span style=\"font-size: medium;\"><span class=\"TextRun SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">Cette&nbsp;</span><span class=\"NormalTextRun SCXW138226291 BCX0\">charte&nbsp;</span></span><span class=\"TextRun Highlight SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">se&nbsp;</span><span class=\"NormalTextRun SCXW138226291 BCX0\">divise</span></span><span class=\"TextRun SCXW138226291 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW138226291 BCX0\">&nbsp;en&nbsp;</span><span class=\"NormalTextRun SCXW138226291 BCX0\">cinq grands engagements pour un num&eacute;rique responsable. Le premier engagement &eacute;tant en faveur de l'environnement, le but &eacute;tant d'aider &agrave; optimiser les outils num&eacute;riques pour limiter leurs impacts et leur consommation d&eacute;j&agrave; existantes.</span></span></span></p>\n<div class=\"OutlineElement Ltr SCXW138226291 BCX0\">&nbsp;</div>\n</div>\n<p><span style=\"font-size: medium;\"><a href=\"https://www.esgi.fr/\">L'ESGI </a>est fier d'avoir r&eacute;cemment sign&eacute; cette charte et a pr&eacute;vu des actions concr&egrave;tes pour sensibiliser ses &eacute;tudiants et contribuer &agrave; l'avenir de la technologie durable.</span></p>",
        "date": 1715778479682,
        "update_date": 1715780480776,
        "links": [
          {
            "rel": "self",
            "href": "https://api.kordis.fr/me/news/4947"
          },
          {
            "rel": "url",
            "href": "https://www.myges.fr/#/actualites/4947/15052024-tout-comprendre-sur-la-technologie-durable"
          },
          "... truncated list, total 3 items"
        ]
      },
      {
        "ne_id": 4940,
        "title": "Quelles études choisir pour travailler dans le développement mobile ?",
        "author": "ESGI",
        "summary": null,
        "text": null,
        "html": "<div>\n<p><span style=\"font-size: medium;\">&Agrave; l&rsquo;&egrave;re de la r&eacute;volution technologique, la fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s de l'ESGI &eacute;merge comme une r&eacute;ponse innovante et pertinente aux d&eacute;fis contemporains. Cette fili&egrave;re, en constante mutation, explore les multiples facettes de la convergence entre la mobilit&eacute; et les objets connect&eacute;s, offrant ainsi des opportunit&eacute;s de formation et des d&eacute;bouch&eacute;s professionnels dans des domaines aussi vari&eacute;s que passionnants.</span></p>\n<h2><span style=\"font-size: medium;\">Qu&rsquo;est-ce que la fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s ?</span></h2>\n<p><span style=\"font-size: medium;\">La fili&egrave;re <a href=\"https://www.esgi.fr/programmes/mobilite-objets-connectes.html\">Mobilit&eacute;s et Objets Connect&eacute;s</a> de l'<a href=\"esgi.fr\">ESGI</a> se concentre sur l'&eacute;tude et le d&eacute;veloppement des technologies li&eacute;es &agrave; la mobilit&eacute; et aux objets connect&eacute;s. Elle englobe un large &eacute;ventail de sujets, tels que le d&eacute;veloppement d'applications mobiles, la conception de dispositifs IoT (Internet des Objets), la gestion des donn&eacute;es g&eacute;n&eacute;r&eacute;es par ces dispositifs, la s&eacute;curit&eacute; des r&eacute;seaux et des syst&egrave;mes, ainsi que les implications soci&eacute;tales et &eacute;thiques de ces technologies &eacute;mergentes.</span></p>\n<p><span style=\"font-size: medium;\">L&rsquo;importance de la fili&egrave;re MOC dans une tendance &agrave; l&rsquo;informatisation des m&eacute;tiers</span></p>\n<p><span style=\"font-size: medium;\">&Agrave; une &eacute;poque o&ugrave; la num&eacute;risation et l'automatisation transforment profond&eacute;ment de nombreux secteurs d'activit&eacute;, la fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s rev&ecirc;t une importance cruciale. Les entreprises cherchent de plus en plus &agrave; int&eacute;grer des solutions innovantes bas&eacute;es sur la mobilit&eacute; et les objets connect&eacute;s pour optimiser leurs processus, am&eacute;liorer l'exp&eacute;rience utilisateur et rester comp&eacute;titives sur le march&eacute;. Ainsi, la ma&icirc;trise des comp&eacute;tences offertes par cette fili&egrave;re devient un atout majeur pour les professionnels d&eacute;sireux de s'adapter aux &eacute;volutions technologiques et de r&eacute;pondre aux besoins changeants des entreprises.</span></p>\n<p><span style=\"font-size: medium;\"><img src=\"https://www.esgi.fr/ecole-informatique/IOT-MA.png\" alt=\"\" width=\"912\" height=\"630\" /></span></p>\n<h2><span style=\"font-size: medium;\">Quels sont les d&eacute;bouch&eacute;s et les opportunit&eacute;s de carri&egrave;re ?</span></h2>\n<p><span style=\"font-size: medium;\">La fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s offre une multitude de d&eacute;bouch&eacute;s professionnels passionnants. Les dipl&ocirc;m&eacute;.e.s peuvent envisager des carri&egrave;res dans des domaines tels que le d&eacute;veloppement d'applications mobiles, l'ing&eacute;nierie IoT, la cybers&eacute;curit&eacute;, la gestion des donn&eacute;es, la conception de produits connect&eacute;s, la consultation en technologie num&eacute;rique, etc. Les secteurs d'activit&eacute; concern&eacute;s sont &eacute;galement vari&eacute;s, allant de la sant&eacute; &agrave; l'industrie en passant par les transports, le commerce de d&eacute;tail, l'&eacute;nergie et bien d'autres encore. Cette diversit&eacute; de possibilit&eacute;s t&eacute;moigne de la polyvalence et de la pertinence des comp&eacute;tences acquises dans le cadre de cette fili&egrave;re.</span></p>\n<h2><span style=\"font-size: medium;\">Les atouts de la fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s de l&rsquo;ESGI</span></h2>\n<p><span style=\"font-size: medium;\">L'ESGI offre un environnement d'apprentissage dynamique et ax&eacute; sur la pratique, permettant aux &eacute;tudiant.e.s de d&eacute;velopper des comp&eacute;tences concr&egrave;tes et directement applicables dans le monde professionnel. Gr&acirc;ce &agrave; des programmes de formation innovants, des projets pratiques et des partenariats avec des entreprises leaders dans le domaine des technologies de l'information, les &eacute;tudiant.e.s de la fili&egrave;re Mobilit&eacute;s et Objets Connect&eacute;s b&eacute;n&eacute;ficient d'une exp&eacute;rience &eacute;ducative enrichissante et ax&eacute;e sur l'employabilit&eacute;. De plus, l'ESGI encourage l'esprit d'innovation et d'entrepreneuriat, offrant aux &eacute;tudiant.e.s l'opportunit&eacute; de d&eacute;velopper leurs propres projets et de transformer leurs id&eacute;es en entreprises prosp&egrave;res.</span></p>\n<p><span style=\"font-size: medium;\">La fili&egrave;re <a href=\"https://www.esgi.fr/programmes/mobilite-objets-connectes.html\">Mobilit&eacute;s et Objets Connect&eacute;s</a> de l'<a href=\"esgi.fr\">ESGI</a> repr&eacute;sente une r&eacute;ponse visionnaire aux d&eacute;fis de l'&egrave;re num&eacute;rique. En formant les futurs professionnels &agrave; la convergence entre la mobilit&eacute; et les objets connect&eacute;s, cette fili&egrave;re joue un r&ocirc;le essentiel dans la pr&eacute;paration des &eacute;tudiant.e.s aux m&eacute;tiers de demain. Avec des d&eacute;bouch&eacute;s vari&eacute;s, des opportunit&eacute;s de carri&egrave;re passionnantes et une formation de qualit&eacute;, elle constitue un choix judicieux pour ceux qui souhaitent s'engager dans une carri&egrave;re prometteuse au c&oelig;ur de l'innovation technologique.</span></p>\n</div>",
        "date": 1713951462443,
        "update_date": 1713955369750,
        "links": [
          {
            "rel": "self",
            "href": "https://api.kordis.fr/me/news/4940"
          },
          {
            "rel": "url",
            "href": "https://www.myges.fr/#/actualites/4940/24042024-quelles-études-choisir-pour-travailler-dans-le développement-mobile"
          },
          "... truncated list, total 3 items"
        ]
      },
      "... truncated list, total 20 items"
    ],
    "pageable": {
      "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
      },
      "page_size": 20,
      "page_number": 0,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "total_pages": 29,
    "total_elements": 566,
    "last": false,
    "number": 0,
    "size": 20,
    "number_of_elements": 20,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "first": true,
    "empty": false
  },
  "links": []
}
```

---

## me/news/banners

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "content": [
      {
        "ba_id": 3158,
        "display_order": 1,
        "title": "Soirée de désintégration",
        "description": null,
        "author": "BDE SKOLAE",
        "html": "<div class=\"kvgmc6g5 cxmmr5t8 oygrvhab hcukyx3x c1et5uql\">\n<div class=\"kvgmc6g5 cxmmr5t8 oygrvhab hcukyx3x c1et5uql\">\n<p>L&rsquo;ann&eacute;e s&rsquo;ach&egrave;ve, et il est temps de la c&eacute;l&eacute;brer comme il se doit.<br />Rejoins-nous pour une soir&eacute;e plac&eacute;e sous le signe du partage, de la musique et des derniers moments pass&eacute;s ensemble.<br />Une belle occasion de se retrouver une derni&egrave;re fois avant de finir l&rsquo;ann&eacute;e.</p>\n<p>&nbsp;</p>\n<p>La soir&eacute;e du vendredi 19 juin 2026 sera &agrave; La Nuit, de 23h &agrave; 5h !</p>\n<p>Prix de la place (une conso incluse) :</p>\n<div class=\"ListContainerWrapper SCXW245473493 BCX0\">\n<p class=\"Paragraph SCXW245473493 BCX0 OutlineElement Ltr SCXW245473493 BCX0\"><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SpellingErrorV2Themed SCXW245473493 BCX0\">Early</span><span class=\"NormalTextRun SCXW245473493 BCX0\"> ticket</span></span><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW245473493 BCX0\"> jusqu'</span><span class=\"NormalTextRun SCXW245473493 BCX0\">au 31 Mai</span><span class=\"NormalTextRun SCXW245473493 BCX0\">23h</span><span class=\"NormalTextRun SCXW245473493 BCX0\">00</span><span class=\"NormalTextRun SCXW245473493 BCX0\">: </span></span><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\">12,99&euro;</span><span class=\"EOP SCXW245473493 BCX0\" data-ccp-props=\"{}\">&nbsp;</span></p>\n</div>\n<div class=\"ListContainerWrapper SCXW245473493 BCX0\">\n<p class=\"Paragraph SCXW245473493 BCX0 OutlineElement Ltr SCXW245473493 BCX0\"><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\">Regular ticket</span><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\"><span class=\"NormalTextRun SCXW245473493 BCX0\">&agrave;</span><span class=\"NormalTextRun SCXW245473493 BCX0\"> partir du 19 Juin</span><span class=\"NormalTextRun SCXW245473493 BCX0\">23h01</span><span class=\"NormalTextRun SCXW245473493 BCX0\">: </span></span><span class=\"TextRun SCXW245473493 BCX0\" lang=\"FR-FR\" xml:lang=\"FR-FR\" data-contrast=\"auto\">14,99&euro;</span><span class=\"EOP SCXW245473493 BCX0\" data-ccp-props=\"{}\">&nbsp;</span></p>\n</div>\n<p><br />Soir&eacute;e ouverte aux externes !</p>\n<p>- Lieu :</p>\n<p>La Nuit<br />8 Boulevard de la Madeleine, 75009 PARIS</p>\n<p><br />Carte d'identit&eacute; obligatoire, <strong><span>soir&eacute;e interdite aux mineurs</span></strong>. La direction se r&eacute;serve le droit d'entr&eacute;e pour les personnes en &eacute;tat d'&eacute;bri&eacute;t&eacute; !</p>\n<p><strong><span>Aucun remboursement possible</span></strong></p>\n</div>\n</div>\n<p>&nbsp;https://my.weezevent.com/soiree-de-desintegration-2026?</p>",
        "image": "https://ges-dl.kordis.fr/public/dEkj-aOcIw6rQVF8JlH0kyQJDzuqbRDQ",
        "begin_date": 1781082000000,
        "end_date": 1781906100000,
        "url": null,
        "links": [
          {
            "rel": "photo",
            "href": "https://ges-dl.kordis.fr/public/dEkj-aOcIw6rQVF8JlH0kyQJDzuqbRDQ"
          }
        ]
      },
      {
        "ba_id": 3148,
        "display_order": 1,
        "title": "Mindbreak CT",
        "description": null,
        "author": "Astrid Beaucourt",
        "html": null,
        "image": "https://ges-dl.kordis.fr/public/dEkj-aOcIw6rQVF8JlH0k5t0jRrxa1yJ",
        "begin_date": 1780351200000,
        "end_date": 1783029600000,
        "url": "https://discord.com/invite/H8uZmCtsWn",
        "links": [
          {
            "rel": "photo",
            "href": "https://ges-dl.kordis.fr/public/dEkj-aOcIw6rQVF8JlH0k5t0jRrxa1yJ"
          }
        ]
      },
      "... truncated list, total 4 items"
    ],
    "pageable": {
      "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
      },
      "page_size": 20,
      "page_number": 0,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "total_pages": 1,
    "total_elements": 4,
    "last": true,
    "number": 0,
    "size": 20,
    "number_of_elements": 4,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "first": true,
    "empty": false
  },
  "links": []
}
```

---

## me/nextProjectSteps

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "course_name": "T2 - projet annuel",
      "group_id": 490013,
      "pro_id": 22276,
      "pro_name": "Connected Neighbours (3AL1)",
      "psp_desc": "Démonstration du client web  et au moins 95 % de l'application totale réalisé\r\nFournir :\r\n* Reprendre les dossiers en intégrant les modifications vues pendant la séance précédente, pour finaliser le dossier fonctionnel et technique complet\r\n* Etat d'avancement (Trello)\r\n* Client Web React administration terminée\r\n* Micro-langage d'interrogation sur documents MongoDB\r\n\r\n\r\nréunion : 01/07 3AL1 et 02/07 3AL2",
      "psp_id": 32953,
      "psp_limit_date": 1782683940000,
      "psp_number": 5,
      "psp_type": "Etape intermédiaire",
      "type": "project",
      "links": []
    },
    {
      "course_name": "T2 - projet annuel",
      "group_id": 490013,
      "pro_id": 22276,
      "pro_name": "Connected Neighbours (3AL1)",
      "psp_desc": "Intégralité des sources (nettoyés des traces de débogage), des classes et des exécutables de l'application (si existants)\r\nIntégralité des fichiers du front/back : react et Node.js, multimedia (images, vidéo, sons) + driver BDD\r\nBase de données sous la forme d'un fichier texte à importer (prévoir plusieurs jeux d'essais, dont un vide)\r\nDossier technique complet et dossier utilisation sur plateforme,\r\nInstaller automatique à prévoir\r\nDocument de synthèse sur la réalisation du projet, comportant une explication sur la démarche de réalisation suivie, une explication précise sur le travail effectué par chacun, et une analyse critique et objective du projet.\r\n\r\nTous les documents ci-dessus devront être postés sur la plate-forme de projets et imprimés en un exemplaire.  \r\n\r\nREMARQUES :\r\n* TOUS LES DOSSIERS DOIVENT ETRE BONNE QUALITE ET REUNIS EN UN SEUL DOCUMENT POUR LE JURY EXTERIEUR\r\n* LES FICHIERS DOIVENT ETRE POSTES SUR LA PLATE-FORME DE PROJETS UN JOUR AVANT LA SOUTENANCE (MEME SI UN ACCES A UN COMPTE GITHUBA ETE FOURNI)\r\n* L'APPLICATION JAVA DOIT ETRE LIVREE SOUS LA FORME D'UN FICHIER JAR AUTO-EXECUTABLE\r\n* LES APPLICATIONS DOIVENT ETRE CONTENEURISEES\r\n\r\nUN PROJET NON DEPLOYÉ NE SERA PAS CORRIGÉ\r\n\r\nLa soutenance aura normalement lieu la semaine du 20 juillet\r\n\r\n",
      "psp_id": 32951,
      "psp_limit_date": 1784498340000,
      "psp_number": 6,
      "psp_type": "Rendu final",
      "type": "project",
      "links": []
    }
  ],
  "links": []
}
```

---

## me/notificationsDelays

**HTTP Status**: `204`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json

```

---

## me/notificationsDelays/{notificationTypeId}

**HTTP Status**: `N/A`

**Content-Type**: `N/A`

**Response Excerpt / JSON Structure**:
```json
No notificationTypeId available to test
```

---

## me/partners

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "partner_id": 1,
      "name": "Microsoft Campus",
      "small_image_url": "https://www.myges.fr/public/images/partner/partner_mscampus_120x120-grey.png",
      "medium_image_url": "https://www.myges.fr/public/images/partner/banner_mscampus.jpg",
      "link": "http://partenaires.reseau-ges.fr/mscampus",
      "content": "<div>Partenaire de la vie étudiante, Microsoft propose depuis une plateforme dédiée une offre campus program destinée aux étudiants :</div><ul><li>Des tarifs préférentiels sur le Pack Microsoft Office et sur une sélection de PC portables</li><li>Un libre accès aux logiciels Microsoft (OneNote, Project, Visio...)</li><li>Des formations gratuites aux technologies les plus récentes</li><li>Et bien d'autres avantages...</li></ul>",
      "links": []
    },
    {
      "partner_id": 3,
      "name": "Ornikar",
      "small_image_url": "https://www.myges.fr/public/images/partner/partner_ornikar_120x120-grey.png",
      "medium_image_url": "https://www.myges.fr/public/images/partner/banner_ornikar.jpg",
      "link": "https://www.ornikar.com/",
      "content": "<div>Ornikar est une auto-école en ligne qui révolutionne la formation au permis de conduire. Notre formation connectée, flexible, et <b>jusqu'à 46 % moins chère que les auto-écoles traditionnelles</b> a su séduire plus de 40 000 utilisateurs, ce qui fait d'Ornikar le leader de ce marché. Notre mission : faire du permis de conduire un plaisir !</div>",
      "links": []
    },
    "... truncated list, total 10 items"
  ],
  "links": []
}
```

---

## me/profile

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "uid": 479546,
    "student_id": "2023-ESGI-479546",
    "ine": "080602439AC",
    "civility": "M",
    "firstname": "Erwan",
    "name": "LUCE--GUÉDON",
    "maiden_name": null,
    "birthday": 1121983200000,
    "birthplace": "Paris",
    "birth_country": "France",
    "address1": "15 Rue des Jolis Monts ",
    "address2": null,
    "city": "Sartrouville",
    "zipcode": "78500",
    "country": "France",
    "telephone": null,
    "mobile": "0640125530",
    "email": "e.luceguedon@myskolae.fr",
    "nationality": "française",
    "personal_mail": "erwan.luce.guedon@gmail.com",
    "mailing": null,
    "emergency_contact": {
      "emergency_id": 479546,
      "type": null,
      "type_details": null,
      "firstname": null,
      "name": null,
      "telephone": null,
      "mobile": null,
      "work_phone": null
    },
    "get_need_to_be_removed_from_edusign": null,
    "_links": {
      "self": {
        "href": "https://api.kordis.fr/me/profile"
      },
      "years": {
        "href": "https://api.kordis.fr/me/years"
      },
      "agenda": {
        "href": "https://api.kordis.fr/me/agenda?start={start}&end={end}",
        "templated": true
      },
      "grades": {
        "href": "https://api.kordis.fr/me/{year}/grades",
        "templated": true
      },
      "classes": {
        "href": "https://api.kordis.fr/me/{year}/classes",
        "templated": true
      },
      "courses": {
        "href": "https://api.kordis.fr/me/{year}/courses",
        "templated": true
      },
      "teachers": {
        "href": "https://api.kordis.fr/me/{year}/teachers",
        "templated": true
      },
      "news": {
        "href": "https://api.kordis.fr/me/news"
      },
      "photo": {
        "href": "https://ges-dl.kordis.fr/public/dEkj-aOcIw52B9RsgY-op41XjDwLljS9CZSMwXEBz3M"
      }
    }
  }
}
```

---

## me/projectFiles/15175

**HTTP Status**: `500`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "timestamp": 1781539288041,
    "status": 500,
    "error": "Internal Server Error",
    "message": "",
    "path": "/me/projectFiles/15175"
  },
  "links": []
}
```

---

## me/projectStepFiles/413810

**HTTP Status**: `400`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 1,
  "version": "1",
  "exception": "Bad Request",
  "result": {
    "timestamp": 1781539288214,
    "status": 400,
    "error": "Bad Request",
    "message": "Vous n'avez pas accès à cette ressource",
    "path": "https://api.kordis.fr:443/me/projectStepFiles/413810"
  }
}
```

---

## me/projects/16834

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": {
    "project_id": 16834,
    "teacher_id": 1000024307,
    "author": "BALASOUPRAMANIANE Amrta Devy",
    "name": "Data Mining Python - Classe 2 - 2024",
    "update_date": 1733438770331,
    "update_user": "BALASOUPRAMANIANE Amrta Devy",
    "course_name": "S1 - introduction au data mining en python",
    "discipline_id": 141202,
    "groups": [
      {
        "group_name": "Groupe 3",
        "date_presentation": null,
        "project_group_id": 371277,
        "project_id": 16834,
        "subject_id": 26211,
        "subject_validated": true,
        "teacher_comment": null,
        "teacher_intern_comment": null,
        "project_group_students": null
      },
      {
        "group_name": "Groupe 4",
        "date_presentation": null,
        "project_group_id": 371278,
        "project_id": 16834,
        "subject_id": 26211,
        "subject_validated": true,
        "teacher_comment": null,
        "teacher_intern_comment": null,
        "project_group_students": null
      },
      "... truncated list, total 14 items"
    ],
    "steps": [
      {
        "psp_id": 25445,
        "psp_type": "Etape intermédiaire",
        "psp_desc": "Choisir son Dataset\r\nFaire le choix entre l'implémentation d'un algorithme de classification ou d'un algorithme de régression.\r\nCommencement le nettoyage du Dataset",
        "psp_limit_date": 1701298740000,
        "pro_id": 16834,
        "psp_number": 1,
        "files": [
          {
            "psf_id": 422301,
            "psf_desc": "régression ",
            "psf_begin_upload": 1736205123825,
            "psf_end_upload": 1736205149640,
            "psf_file": "hvZefVUhPVIO3dxJQedb.ipynb",
            "psf_role_user": "student",
            "psf_file_size": 1673618,
            "psf_file_hash": "1a2208811f5c3e8b4f31176ac287cdeb",
            "psf_file_type": ".ipynb",
            "psp_id": 25445,
            "pgr_id": 371279,
            "u_id": 596768,
            "psf_name": "projet "
          },
          {
            "psf_id": 422230,
            "psf_desc": "",
            "psf_begin_upload": 1736194908729,
            "psf_end_upload": 1736194925400,
            "psf_file": "NsLfu50IrhS6oC9gWbI6.zip",
            "psf_role_user": "student",
            "psf_file_size": 482597,
            "psf_file_hash": "d57eed66c9d3fae8c18b22fc0c8ba7c1",
            "psf_file_type": ".zip",
            "psp_id": 25445,
            "pgr_id": 371286,
            "u_id": 479546,
            "psf_name": "Dataset_voiture_regression "
          },
          "... truncated list, total 4 items"
        ]
      },
      {
        "psp_id": 25444,
        "psp_type": "Rendu final",
        "psp_desc": "Rendu Final: Format zip",
        "psp_limit_date": 1704581940000,
        "pro_id": 16834,
        "psp_number": 2,
        "files": [
          {
            "psf_id": 423470,
            "psf_desc": "",
            "psf_begin_upload": 1736521082990,
            "psf_end_upload": 1736521114915,
            "psf_file": "W6vXJDb63ZyGqeY52kMa.ipynb",
            "psf_role_user": "student",
            "psf_file_size": 1589395,
            "psf_file_hash": "43faaeca10a06e3a0b0f44d1968e7a5b",
            "psf_file_type": ".ipynb",
            "psp_id": 25444,
            "pgr_id": 371276,
            "u_id": 395286,
            "psf_name": "Final absolue"
          },
          {
            "psf_id": 422528,
            "psf_desc": "implémentation d'une régression ",
            "psf_begin_upload": 1736307633546,
            "psf_end_upload": 1736307664556,
            "psf_file": "IxOLAZt3mPjMe05oYkfb.zip",
            "psf_role_user": "student",
            "psf_file_size": 425254,
            "psf_file_hash": "c9d7c47231124974301a5c7ebddbe9c0",
            "psf_file_type": ".zip",
            "psp_id": 25444,
            "pgr_id": 371279,
            "u_id": 596768,
            "psf_name": "Rendu projet"
          },
          "... truncated list, total 11 items"
        ]
      }
    ],
    "project_files": [
      {
        "pf_id": 15174,
        "pf_title": "Dataset sur la classification animale",
        "pf_file": "3YvN9YWc2tez98Jj95WR.zip",
        "pf_crea_date": 1697994501672,
        "pro_id": 16834
      },
      {
        "pf_id": 15175,
        "pf_title": "Dataset sur la classification des utilisateurs des réseaux sociaux",
        "pf_file": "CGCvuVZEcZko0fJIUf2p.zip",
        "pf_crea_date": 1697994586208,
        "pro_id": 16834
      },
      "... truncated list, total 5 items"
    ],
    "project_group_logs": [
      {
        "pgl_id": 1556090,
        "pgl_author": "MARTINEZ MAXIME",
        "pgl_role_user": "student",
        "pgl_describe": "MARTINEZ MAXIME a rejoint le groupe",
        "pgl_date": 1728651564838,
        "pgl_type_action": "join",
        "user_id": 482778,
        "pgr_id": 371286
      },
      {
        "pgl_id": 1556115,
        "pgl_author": "LEJEUNE Thomas",
        "pgl_role_user": "student",
        "pgl_describe": "LEJEUNE Thomas a rejoint le groupe",
        "pgl_date": 1728651880546,
        "pgl_type_action": "join",
        "user_id": 617010,
        "pgr_id": 371286
      },
      "... truncated list, total 13 items"
    ],
    "is_draft": false,
    "project_type_id": 1,
    "project_computing_tools": "Google Collabs ( en ligne )",
    "project_create_date": 1728591702926,
    "project_detail_plan": "Savoir implémenter un modèle de classification ou de régression en utilisant les techniques de Data mining vues en cours.",
    "project_hearing_presentation": "A huis clos",
    "project_max_student_group": 3,
    "project_min_student_group": 2,
    "project_personal_work": 6,
    "project_presentation_duration": 15,
    "project_ref_books": "-",
    "project_teaching_goals": "Savoir utiliser Python pour\r\n- manipuler un jeu de données ,\r\n- l'explorer \r\n- le nettoyer, \r\n- extraire de l'information en fonction du besoin en utilisation un modèle de Machine Learning\r\n",
    "project_type_group": "Libre",
    "project_type_presentation": "Présentation / PowerPoint|Démonstration",
    "project_type_presentation_details": "",
    "project_type_subject": "Imposé",
    "rc_id": 274192,
    "trimester_id": 21,
    "year": 2024
  }
}
```

---

## me/speedMeetingAppointments

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "appointment_end": 1687336800000,
      "appointment_start": 1687335600000,
      "corporate_name": "UNIVERSITÉ PARIS DESCARTES",
      "location": "ONLINE",
      "organizer": "ESGI",
      "ss_id": 133684,
      "title": "SPEED-MEETING SPECIAL 1ERE ET 2EME ANNEE",
      "offers": [
        {
          "contract": null,
          "corporate_name": "UNIVERSITÉ PARIS DESCARTES",
          "ge_id": null,
          "of_activity": null,
          "begin_date": null,
          "of_id": null,
          "of_region": null,
          "of_selection_active": null,
          "of_upd_date": null,
          "of_url": null,
          "offer": null,
          "rsf_id": 11644,
          "allowed_promo": true,
          "of_file_ext": null,
          "of_perdu": null,
          "of_pourvu": null,
          "fid": 82953,
          "links": []
        }
      ],
      "links": []
    },
    {
      "appointment_end": 1687340400000,
      "appointment_start": 1687339200000,
      "corporate_name": "KEMEO",
      "location": "ONLINE",
      "organizer": "ESGI",
      "ss_id": 134062,
      "title": "SPEED-MEETING SPECIAL 1ERE ET 2EME ANNEE",
      "offers": [
        {
          "contract": "Contrat d'apprentissage",
          "corporate_name": "KEMEO",
          "ge_id": 3028,
          "of_activity": "INFORMATIQUE",
          "begin_date": "Dès que possible...",
          "of_id": 147059,
          "of_region": "ILE DE FRANCE",
          "of_selection_active": true,
          "of_upd_date": 1721913530296,
          "of_url": null,
          "offer": "Développeur PHP h/f",
          "rsf_id": 11614,
          "allowed_promo": true,
          "of_file_ext": ".docx",
          "of_perdu": true,
          "of_pourvu": false,
          "fid": 92862,
          "links": []
        }
      ],
      "links": []
    },
    "... truncated list, total 9 items"
  ],
  "links": []
}
```

---

## me/suggestion

**HTTP Status**: `405`

**Content-Type**: ``

**Response Excerpt / JSON Structure**:
```json

```

---

## me/trimesterYears

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    {
      "school_name": "ESGI",
      "tri_describe": "Semestre 1",
      "tri_id": 21,
      "tri_name": "S1",
      "year": 2023,
      "links": [],
      "p_uid": 7
    },
    {
      "school_name": "ESGI",
      "tri_describe": "Semestre 2",
      "tri_id": 22,
      "tri_name": "S2",
      "year": 2023,
      "links": [],
      "p_uid": 7
    },
    "... truncated list, total 8 items"
  ],
  "links": []
}
```

---

## me/years

**HTTP Status**: `200`

**Content-Type**: `application/json`

**Response Excerpt / JSON Structure**:
```json
{
  "response_code": 0,
  "version": "1",
  "result": [
    2026,
    2025,
    "... truncated list, total 4 items"
  ],
  "links": []
}
```

---

