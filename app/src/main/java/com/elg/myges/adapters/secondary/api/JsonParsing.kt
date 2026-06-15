package com.elg.myges.adapters.secondary.api

import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.DirectoryRole
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.ProjectGroup
import com.elg.myges.domain.model.ProjectStep
import com.elg.myges.domain.model.StudentProfile
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

fun JsonElement.toProfile(): StudentProfile {
    val root = objectOrData()
    val id = root.text("id", "uid", "puid", "studentId", "student_id") ?: "profile"
    val firstName = root.text("firstName", "firstname", "prenom")
    val lastName = root.text("lastName", "lastname", "name", "nom")
    val displayName = root.text("displayName", "fullName", "nomComplet")
        ?: listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { root.text("name") ?: id }
    return StudentProfile(
        id = id,
        displayName = displayName,
        email = root.text("email", "mail"),
        school = root.text("school", "campus", "institution"),
        program = root.text("program", "programme", "formation", "className"),
        academicYear = root.text("academicYear", "year", "annee", "student_id")?.let(::academicYearFromProfile),
        avatarUrl = root.text("avatarUrl", "avatar", "picture") ?: root.namedLinkHref("photo")
    )
}

fun JsonElement.toYears(): List<String> {
    return arrayOrNested("years", "data", "items")
        .mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> element.contentOrNull
                is JsonObject -> element.text("year", "id", "value", "academicYear", "name")
                else -> null
            }
        }
        .filter { it.isNotBlank() }
        .distinct()
}

fun JsonElement.toAgendaEvents(): List<AgendaEvent> {
    val CAMPUS_LOCATIONS = mapOf(
        "NATION1" to "242 rue du Faubourg Saint Antoine, 75012 Paris",
        "NATION2" to "220 rue du Faubourg Saint Antoine, 75012 Paris",
        "VOLTAIRE1" to "1 rue Bouvier, 75011 Paris",
        "VOLTAIRE2" to "20 rue Bouvier, 75011 Paris",
        "ERARD" to "19-21 rue Erard, 75011 Paris",
        "BEAUGRENELLE" to "35 quai André Citroen 75015 Paris",
        "MONTSOURIS" to "5 rue Lemaignan, 75014 Paris",
        "MONTROUGE" to "11 rue Camille Pelletan, 92120 Montrouge",
        "JOURDAN" to "6-10 bd Jourdan 75014 Paris",
        "VAUGIRARD" to "273-277 rue de Vaugirard, 75012 Paris",
        "MAIN-D-OR" to "8‐14 Passage de la Main d’Or 75011 Paris",
        "RANELAGH" to "64-70 Rue du Ranelagh, 75016 Paris"
    )

    val CAMPUS_COLORS = mapOf(
        "NATION1" to "3",
        "NATION2" to "2",
        "VOLTAIRE1" to "5",
        "VOLTAIRE2" to "5",
        "ERARD" to "4",
        "BEAUGRENELLE" to "1",
        "MONTSOURIS" to "3",
        "MONTROUGE" to "6",
        "JOURDAN" to "7",
        "VAUGIRARD" to "9",
        "MAIN-D-OR" to "8",
        "RANELAGH" to "10"
    )

    return arrayOrNested("agenda", "events", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val discipline = root["discipline"] as? JsonObject
        val startsAt = root.instant("startsAt", "start", "startDate", "dateStart", "begin", "start_date")
            ?: return@mapNotNull null
        val endsAt = root.instant("endsAt", "end", "endDate", "dateEnd", "end_date")
            ?: startsAt.plusSeconds(3600)
            
        val roomsArray = root.array("rooms")
        var campusKey: String? = null
        var roomName: String? = null
        
        if (roomsArray.isNotEmpty()) {
            val firstRoomObj = roomsArray.firstOrNull() as? JsonObject
            if (firstRoomObj != null) {
                roomName = firstRoomObj.text("name")
                campusKey = firstRoomObj.text("campus")
            }
        }
        
        if (roomName.isNullOrBlank()) {
            roomName = root.text("room", "classroom", "salle") ?: root.arrayText("rooms", "name")
        }
        if (campusKey.isNullOrBlank()) {
            val rootCampus = root.text("campus")
            if (!rootCampus.isNullOrBlank()) {
                campusKey = rootCampus
            } else {
                val rootModality = root.text("modality", "mode")
                if (!rootModality.isNullOrBlank() && CAMPUS_LOCATIONS.containsKey(rootModality.uppercase().trim())) {
                    campusKey = rootModality
                }
            }
        }

        var resolvedAddress: String? = null
        var resolvedColorId = "11"

        if (!campusKey.isNullOrBlank()) {
            val keyUpper = campusKey.uppercase().trim()
            if (CAMPUS_LOCATIONS.containsKey(keyUpper)) {
                resolvedAddress = CAMPUS_LOCATIONS[keyUpper]
                resolvedColorId = CAMPUS_COLORS[keyUpper] ?: "11"
            } else {
                val knownModalities = setOf("PRÉSENTIEL", "PRESENTIEL", "DISTANTIEL", "HYBRIDE", "E-LEARNING", "ONLINE")
                if (!knownModalities.contains(keyUpper)) {
                    resolvedAddress = campusKey
                }
            }
        }

        AgendaEvent(
            id = root.text("id", "eventId", "uid", "reservation_id") ?: stableId(root),
            title = root.text("title", "name", "courseName", "matiere")
                ?: discipline?.text("name")
                ?: "",
            startsAt = startsAt,
            endsAt = endsAt,
            room = roomName,
            teacher = root.text("teacher", "intervenant", "professor") ?: discipline?.text("teacher"),
            type = root.text("type", "kind", "prestation_type"),
            modality = root.text("modality", "mode", "campus"),
            courseId = root.text("courseId", "rcId", "rc_id", "moduleId") ?: discipline?.text("rc_id"),
            address = resolvedAddress,
            colorId = resolvedColorId
        )
    }
}

fun JsonElement.toGrades(year: String? = null): List<Grade> {
    val dateKeys = arrayOf(
        "date", "createdAt", "publishedAt", "date_exam", "dateExam", 
        "date_examen", "dateExamen", "examDate", "exam_date", 
        "publishDate", "publish_date", "published", "updatedAt", 
        "update_date", "date_note", "dateNote", "published_at", "created_at"
    )
    return arrayOrNested("grades", "items", "data").flatMap { element ->
        val root = element.objectOrData()
        val baseId = root.text("id", "gradeId", "uid", "rc_id") ?: stableId(root)
        
        if (!root.containsKey("grades") && !root.containsKey("exam") && !root.containsKey("course")) {
            return@flatMap listOf(
                root.toGrade(
                    value = root.number("grade", "value", "note"),
                    year = year
                )
            )
        }
        
        // Parse CC grades
        val nestedGrades = root.array("grades")
        val ccGrades = nestedGrades.mapIndexedNotNull { index, gradeElement ->
            val value = when (gradeElement) {
                is JsonPrimitive -> gradeElement.doubleOrNull ?: gradeElement.contentOrNull?.replace(',', '.')?.toDoubleOrNull()
                is JsonObject -> gradeElement.number("value", "grade", "note")
                else -> null
            }
            val dateVal = when (gradeElement) {
                is JsonObject -> gradeElement.localDate(*dateKeys)
                else -> null
            }
            value?.let { Pair(it, dateVal) }
        }

        // Parse Exam
        val examValue = root.number("exam", "examen")
        val examDate = root.localDate("date_exam", "exam_date")

        // Compute/parse average
        val ccAverage = if (ccGrades.isNotEmpty()) ccGrades.map { it.first }.average() else null
        val calculatedAverage = when {
            ccAverage != null && examValue != null -> 0.5 * ccAverage + 0.5 * examValue
            ccAverage != null -> ccAverage
            examValue != null -> examValue
            else -> null
        }
        val finalAverage = root.number("average", "moyenne", "ccaverage") ?: calculatedAverage

        val resultList = mutableListOf<Grade>()

        // 1. Create the Main Grade Card
        resultList.add(
            root.toGrade(
                idSuffix = null,
                subject = "",
                value = finalAverage,
                scale = root.number("scale", "outOf", "bareme"),
                date = root.localDate(*dateKeys),
                year = year
            )
        )

        // 2. Create the CC component Grade cards
        ccGrades.forEachIndexed { index, pair ->
            resultList.add(
                root.toGrade(
                    idSuffix = "cc-$index",
                    subject = "CC ${index + 1}",
                    value = pair.first,
                    scale = root.number("scale", "outOf", "bareme"),
                    date = pair.second ?: root.localDate(*dateKeys),
                    year = year
                )
            )
        }

        // 3. Create the Exam component Grade card
        if (examValue != null) {
            resultList.add(
                root.toGrade(
                    idSuffix = "exam",
                    subject = "Examen",
                    value = examValue,
                    scale = root.number("scale", "outOf", "bareme"),
                    date = examDate ?: root.localDate(*dateKeys),
                    year = year
                )
            )
        }

        resultList
    }
}

fun JsonElement.toAbsences(year: String? = null, availablePeriods: List<String> = emptyList()): List<Absence> {
    return arrayOrNested("absences", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val startsAt = root.instant("startsAt", "start", "startDate", "dateStart", "date")
            ?: return@mapNotNull null
        val endsAt = root.instant("endsAt", "end", "endDate", "dateEnd")
            ?: startsAt.plusSeconds(3600)

        val startsAtLdt = java.time.LocalDateTime.ofInstant(startsAt, java.time.ZoneOffset.UTC)
        val month = startsAtLdt.monthValue
        val isSem2 = month in 2..8
        val targetSemesterName = if (isSem2) "Semestre 2" else "Semestre 1"

        var resolvedPeriod = availablePeriods.firstOrNull { it.contains(targetSemesterName, ignoreCase = true) }

        if (resolvedPeriod == null && year != null) {
            val startYearNum = year.toIntOrNull() ?: 2026
            resolvedPeriod = "$startYearNum-${startYearNum + 1} - Semestre ${if (isSem2) 2 else 1}"
        }

        Absence(
            id = root.text("id", "absenceId", "uid") ?: stableId(root),
            courseName = root.text("courseName", "course_name", "course", "module", "matiere") ?: "",
            startsAt = startsAt,
            endsAt = endsAt,
            justified = root.bool("justified", "isJustified", "justifiee") == true,
            status = root.text("status", "state", "etat", "type"),
            reason = root.text("reason", "motif"),
            period = resolvedPeriod
        )
    }
}

fun JsonElement.toCourses(): List<Course> {
    return arrayOrNested("courses", "items", "data").map { element ->
        val root = element.objectOrData()
        val files = root.array("files", "documents")
        Course(
            id = root.text("id", "rcId", "rc_id", "courseId", "uid") ?: stableId(root),
            name = root.text("name", "title", "courseName", "course_name", "matiere") ?: "",
            teacher = root.text("teacher", "intervenant", "professor"),
            year = root.text("year", "academicYear"),
            period = root.text("period", "trimester_name", "semester", "trimester"),
            syllabus = root.text("syllabus", "description", "summary"),
            fileCount = files.size.takeIf { it > 0 } ?: if (root.bool("has_documents") == true) 1 else 0
        )
    }
}

fun JsonElement.toCourseSyllabus(): String? {
    val root = objectOrData()
    return listOfNotNull(
        root.text("syllabus_name", "course_name"),
        root.text("teaching_goals"),
        root.text("detail_plan"),
        root.text("skills"),
        root.text("prerequisite"),
        root.text("teaching_method"),
        root.text("evaluation_type"),
        root.text("evaluation_criteria"),
        root.text("books_reference"),
        root.text("other_reference"),
        root.text("computing_tools"),
        root.arrayText("seance_details", "title", "name", "description", "detail")
    )
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString("\n\n")
        .takeIf { it.isNotBlank() }
}

fun JsonElement.toProjects(currentUserId: String? = null, fallbackYear: String? = null): List<Project> {
    val projectDateKeys = arrayOf(
        "deadline", "dueDate", "endDate", "psp_limit_date", "limit_date", 
        "limitDate", "date_limit", "dateLimit", "date", "due_date", "due", 
        "update_date", "date_limite", "dateLimite"
    )
    return arrayOrNested("projects", "items", "data")
        .ifEmpty { listOf(objectOrData()).filter { it.isNotEmpty() } }
        .map { element ->
        val root = element.objectOrData()
        val projectId = root.text("id", "projectId", "project_id", "uid") ?: stableId(root)
        val year = root.text("year", "academicYear") ?: fallbackYear
        val steps = root.array("steps", "projectSteps").map { step ->
            val stepRoot = step.objectOrData()
            ProjectStep(
                id = stepRoot.text("id", "stepId", "psp_id", "uid") ?: stableId(stepRoot),
                title = stepRoot.text("title", "name", "psp_desc", "psp_type") ?: "",
                deadline = stepRoot.instant(*projectDateKeys),
                status = stepRoot.text("status", "state")
            )
        }
        val userGroupIds = root.array("project_group_logs")
            .mapNotNull { it.objectOrData() }
            .filter { log -> currentUserId != null && log.text("user_id", "uid", "u_id") == currentUserId }
            .mapNotNull { it.text("pgr_id", "project_group_id", "group_id") }
            .toSet()
        val groups = root.array("groups").map { group ->
            val groupRoot = group.objectOrData()
            val groupId = groupRoot.text("project_group_id", "pgr_id", "group_id", "id") ?: stableId(groupRoot)
            ProjectGroup(
                id = groupId,
                name = groupRoot.text("group_name", "name", "label") ?: groupId,
                students = groupRoot.array("project_group_students", "students").mapNotNull { student ->
                    student.objectOrData().directoryDisplayName()
                },
                isMine = groupId in userGroupIds
            )
        }
        val stepFileCount = root.array("steps", "projectSteps").sumOf { it.objectOrData().array("files").size }
        Project(
            id = projectId,
            name = root.text("name", "title") ?: "",
            courseName = root.text("courseName", "course_name", "course", "module"),
            groupName = root.text("groupName", "group", "projectGroup")
                ?: groups.firstOrNull { it.isMine }?.name
                ?: root.arrayText("groups", "group_name", "name"),
            status = root.text("status", "state"),
            deadline = root.instant(*projectDateKeys) ?: steps.mapNotNull { it.deadline }.minOrNull(),
            steps = steps,
            fileCount = root.array("files", "project_files", "documents", "deliverables").size + stepFileCount,
            year = year,
            courseId = root.text("rc_id", "rcId", "courseId"),
            groups = groups
        )
    }
}

fun JsonElement.toNextProjectStepProjects(): List<Project> {
    val projectDateKeys = arrayOf(
        "deadline", "dueDate", "endDate", "psp_limit_date", "limit_date", 
        "limitDate", "date_limit", "dateLimit", "date", "due_date", "due", 
        "update_date", "date_limite", "dateLimite"
    )
    return arrayOrNested("projectSteps", "steps", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val projectId = root.text("project_id", "projectId", "pro_id") ?: return@mapNotNull null
        val step = ProjectStep(
            id = root.text("psp_id", "stepId", "id", "uid") ?: stableId(root),
            title = root.text("psp_desc", "title", "name", "psp_type", "type") ?: "",
            deadline = root.instant(*projectDateKeys),
            status = root.text("status", "state", "type")
        )
        Project(
            id = projectId,
            name = root.text("pro_name", "projectName", "name", "title") ?: "",
            courseName = root.text("course_name", "courseName", "course", "module"),
            groupName = root.text("group_id", "groupName", "group", "projectGroup"),
            status = root.text("status", "state", "type"),
            deadline = step.deadline,
            steps = listOf(step),
            fileCount = 0,
            year = root.text("year", "academicYear")
        )
    }
}

fun JsonElement.toPracticals(fallbackYear: String? = null): List<Practical> {
    return arrayOrNested("practicals", "items", "data").map { element ->
        val root = element.objectOrData()
        val stepDates = root.array("steps", "projectSteps")
            .mapNotNull { it.objectOrData().instant("deadline", "dueDate", "endDate", "psp_limit_date") }
            .sorted()
        Practical(
            id = root.text("id", "practicalId", "project_id", "uid") ?: stableId(root),
            name = root.text("name", "title") ?: "",
            courseName = root.text("courseName", "course_name", "course", "module"),
            startsAt = root.instant("startsAt", "start", "startDate", "dateStart", "project_create_date") ?: stepDates.firstOrNull(),
            endsAt = root.instant("endsAt", "end", "endDate", "dateEnd") ?: stepDates.lastOrNull(),
            room = root.text("room", "classroom", "salle"),
            status = root.text("status", "state"),
            year = root.text("year", "academicYear") ?: fallbackYear
        )
    }
}

fun JsonElement.toDocuments(): List<AcademicDocument> {
    return arrayOrNested("annualDocuments", "documents", "items", "data").map { element ->
        val root = element.objectOrData()
        root.toDocument()
    }
}

fun JsonElement.toProjectDocuments(fallbackYear: String? = null): List<AcademicDocument> {
    return arrayOrNested("projects", "items", "data")
        .ifEmpty { listOf(objectOrData()).filter { it.isNotEmpty() } }
        .flatMap { element ->
        val projectRoot = element.objectOrData()
        val projectId = projectRoot.text("id", "projectId", "project_id", "uid") ?: stableId(projectRoot)
        val projectYear = projectRoot.text("year", "academicYear") ?: fallbackYear
        val projectFiles = projectRoot.array("project_files").map { file ->
            val root = file.objectOrData()
            val document = root.toDocument(
                parentTitle = projectRoot.text("name", "title"),
                parentYear = projectYear,
                ownerId = projectId
            )
            document.copy(downloadUrl = document.downloadUrl ?: "me/projectFiles/${document.id}")
        }
        val stepFiles = projectRoot.array("steps", "projectSteps").flatMap { step ->
            val stepRoot = step.objectOrData()
            stepRoot.array("files").map { file ->
                val root = file.objectOrData()
                val document = root.toDocument(
                    parentTitle = stepRoot.text("title", "name", "psp_desc", "psp_type"),
                    parentYear = projectYear,
                    ownerId = projectId,
                    groupId = root.text("pgr_id", "project_group_id", "group_id")
                )
                document.copy(downloadUrl = document.downloadUrl ?: "me/projectStepFiles/${document.id}")
            }
        }
        projectFiles + stepFiles
    }
}

fun JsonElement.toDirectoryPeople(role: DirectoryRole, year: String? = null): List<DirectoryPerson> {
    return arrayOrNested("students", "teachers", "items", "data").map { element ->
        val root = element.objectOrData()
        val rawId = root.text("id", "uid", "puid", "student_id", "studentId", "teacher_id") ?: stableId(root)
        DirectoryPerson(
            id = "${role.name}:$year:$rawId",
            displayName = root.directoryDisplayName() ?: rawId,
            email = root.text("email", "mail"),
            role = role,
            year = root.text("year", "academicYear") ?: year,
            groupName = root.text("groupName", "className", "promotion", "student_group_name"),
            avatarUrl = root.text("avatarUrl", "avatar", "picture", "photo") ?: root.namedLinkHref("photo")
        )
    }
}

fun JsonElement.toClassIds(): List<String> {
    return arrayOrNested("classes", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        root.text("puid", "id", "class_id") ?: root.linkHref("self")?.substringAfterLast('/')
    }.distinct()
}

fun JsonElement.toNews(): List<NewsItem> {
    val items = arrayOrNested("news", "banners", "content", "items", "data")
        .ifEmpty { listOf(objectOrData()).filter { it.isNotEmpty() } }
    return items.map { element ->
        val root = element.objectOrData()
        NewsItem(
            id = root.text("id", "newsId", "ne_id", "partner_id", "ss_id", "type", "uid") ?: stableId(root),
            title = root.text("title", "name", "label") ?: "",
            body = root.text("body", "content", "summary", "text", "html", "description", "value") ?: root.newsFallbackBody(),
            publishedAt = root.instant("publishedAt", "date", "createdAt", "update_date", "appointment_start")
        )
    }
}

private fun JsonElement.objectOrData(): JsonObject {
    val root = this as? JsonObject ?: return JsonObject(emptyMap())
    return (root["result"] as? JsonObject) ?: (root["data"] as? JsonObject) ?: root
}

private fun JsonElement.arrayOrNested(vararg keys: String): List<JsonElement> {
    if (this is JsonArray) return this.toList()
    val root = this as? JsonObject ?: return emptyList()
    (keys.toList() + listOf("result", "data", "items", "results")).distinct().forEach { key ->
        val value = root[key]
        if (value is JsonArray) return value.toList()
        if (value is JsonObject) {
            val nested = value.arrayOrNested("content", "items", "data", "results")
            if (nested.isNotEmpty()) return nested
        }
    }
    return root.values.firstOrNull { it is JsonArray }?.jsonArray?.toList().orEmpty()
}

private fun academicYearFromProfile(value: String): String {
    val startYear = Regex("\\d{4}").find(value)?.value?.toIntOrNull() ?: return value
    return (startYear..(startYear + 3)).joinToString(", ")
}

private fun String.toMimeType(): String? {
    return when (trim().lowercase().removePrefix(".")) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "zip" -> "application/zip"
        "txt" -> "text/plain"
        "md" -> "text/markdown"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        else -> takeIf { it.contains('/') }
    }
}

private fun JsonObject.text(vararg keys: String): String? {
    keys.forEach { key ->
        val value = this[key]
        if (value is JsonPrimitive) {
            return value.contentOrNull?.takeIf { it.isNotBlank() }
        }
        if (value is JsonObject) {
            value.text("name", "title", "label", "value")?.let { return it }
        }
    }
    return null
}

private fun JsonObject.number(vararg keys: String): Double? {
    keys.forEach { key ->
        val primitive = this[key] as? JsonPrimitive
        primitive?.doubleOrNull?.let { return it }
        primitive?.contentOrNull?.replace(',', '.')?.toDoubleOrNull()?.let { return it }
    }
    return null
}

private fun JsonObject.bool(vararg keys: String): Boolean? {
    keys.forEach { key ->
        val primitive = this[key] as? JsonPrimitive
        primitive?.booleanOrNull?.let { return it }
        when (primitive?.contentOrNull?.lowercase()) {
            "true", "yes", "1", "justified", "justifiee" -> return true
            "false", "no", "0", "unjustified", "injustifiee" -> return false
        }
    }
    return null
}

private fun JsonObject.array(vararg keys: String): List<JsonElement> {
    keys.forEach { key ->
        val value = this[key]
        if (value is JsonArray) return value.toList()
    }
    return emptyList()
}

private fun JsonObject.arrayText(key: String, vararg textKeys: String): String? {
    return array(key)
        .mapNotNull { (it as? JsonObject)?.text(*textKeys) }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")
}

private fun JsonObject.directoryDisplayName(): String? {
    return text("displayName", "fullName", "fullname", "nomComplet")
        ?: listOfNotNull(
            text("civility", "civilite"),
            text("firstName", "firstname", "prenom"),
            text("lastName", "lastname", "name", "nom")
        ).joinToString(" ").ifBlank { text("name", "email", "mail") }
}

private fun JsonObject.toDocument(
    parentTitle: String? = null,
    parentYear: String? = null,
    ownerId: String? = null,
    groupId: String? = null,
    inlineContent: String? = null
): AcademicDocument {
    val title = text("title", "name", "label", "pf_title", "psf_name", "psf_desc") ?: parentTitle ?: ""
    val extension = text("extension", "psf_file_type")
    return AcademicDocument(
        id = text("id", "documentId", "document_id", "oc_id", "pf_id", "psf_id", "uid") ?: stableId(this),
        title = title,
        category = text("category", "type"),
        year = text("year", "academicYear") ?: parentYear,
        mimeType = text("mimeType", "contentType")?.toMimeType() ?: extension?.toMimeType(),
        fileName = text("fileName", "filename", "file", "pf_file", "psf_file", "psf_name") ?: title.toDocumentFileName(extension),
        downloadUrl = text("downloadUrl", "url", "href") ?: linkHref("url", "download", "file"),
        updatedAt = instant("updatedAt", "last_update", "update_date", "pf_crea_date", "psf_end_upload", "psf_begin_upload", "date", "createdAt"),
        ownerId = ownerId,
        groupId = groupId,
        inlineContent = inlineContent
    )
}

private fun String.toDocumentFileName(extension: String?): String {
    val name = ifBlank { "document" }
    val suffix = extension
        ?.trim()
        ?.takeIf { it.isNotBlank() && !name.endsWith(it, ignoreCase = true) }
        ?.let { if (it.startsWith(".")) it else ".$it" }
        .orEmpty()
    return name + suffix
}

private fun JsonObject.linkHref(vararg rels: String): String? {
    val links = array("links").mapNotNull { it as? JsonObject }
    if (rels.isNotEmpty()) {
        links.firstOrNull { link -> link.text("rel") in rels }
            ?.text("href", "url")
            ?.let { return it }
    }
    return links.mapNotNull { it.text("href", "url") }.firstOrNull()
}

private fun JsonObject.namedLinkHref(key: String): String? {
    return (this["_links"] as? JsonObject)
        ?.get(key)
        ?.let { (it as? JsonObject)?.text("href", "url") }
        ?: linkHref(key)
}

private fun JsonObject.newsFallbackBody(): String? {
    return listOfNotNull(
        text("corporate_name"),
        text("location"),
        text("organizer"),
        arrayText("offers", "offer", "contract", "of_activity")
    )
        .filter { it.isNotBlank() }
        .joinToString(" - ")
        .takeIf { it.isNotBlank() }
}

private fun JsonObject.instant(vararg keys: String): Instant? {
    keys.forEach { key ->
        val value = this[key]
        parseInstant(value)?.let { return it }
    }
    return null
}

private fun JsonObject.localDate(vararg keys: String): LocalDate? {
    keys.forEach { key ->
        val value = this[key]
        val text = (value as? JsonPrimitive)?.contentOrNull
        text?.let { candidate ->
            runCatching { LocalDate.parse(candidate.take(10)) }.getOrNull()?.let { return it }
            parseInstant(value)?.atZone(ZoneOffset.UTC)?.toLocalDate()?.let { return it }
        }
    }
    return null
}

private fun JsonElement.numberOrNull(): Double? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.doubleOrNull ?: primitive.contentOrNull?.replace(',', '.')?.toDoubleOrNull()
}

private fun JsonObject.toGrade(
    idSuffix: String? = null,
    subject: String? = null,
    value: Double? = null,
    scale: Double? = null,
    date: LocalDate? = null,
    year: String? = null
): Grade {
    val baseId = text("id", "gradeId", "uid", "rc_id") ?: stableId(this)
    val rawPeriod = text("period", "trimester_name", "semester", "trimester")
    val resolvedPeriod = if (rawPeriod != null && year != null && !rawPeriod.contains(year)) {
        val startYearNum = year.toIntOrNull()
        if (startYearNum != null) {
            "$startYearNum-${startYearNum + 1} - $rawPeriod"
        } else {
            "$year - $rawPeriod"
        }
    } else {
        rawPeriod
    }
    val dateKeys = arrayOf(
        "date", "createdAt", "publishedAt", "date_exam", "dateExam", 
        "date_examen", "dateExamen", "examDate", "exam_date", 
        "publishDate", "publish_date", "published", "updatedAt", 
        "update_date", "date_note", "dateNote", "published_at", "created_at"
    )
    return Grade(
        id = listOfNotNull(baseId, idSuffix).joinToString("-"),
        courseName = text("courseName", "course", "module", "matiere") ?: "",
        subject = subject ?: text("subject", "title", "name", "evaluation", "trimester_name") ?: "",
        value = value,
        scale = scale ?: number("scale", "outOf", "bareme") ?: 20.0,
        coefficient = number("coefficient", "coef"),
        average = number("average", "moyenne", "ccaverage"),
        date = date ?: localDate(*dateKeys),
        period = resolvedPeriod
    )
}

private fun parseInstant(value: JsonElement?): Instant? {
    if (value == null || value is JsonNull) return null
    val primitive = value as? JsonPrimitive ?: return null
    primitive.contentOrNull?.let { text ->
        val cleanText = text.trim().replace(Regex("\\s+"), " ")
        cleanText.toLongOrNull()?.let { number ->
            return if (number > 9999999999L) Instant.ofEpochMilli(number) else Instant.ofEpochSecond(number)
        }
        if (cleanText.contains('/') && cleanText.contains('h')) {
            runCatching {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm")
                val ldt = java.time.LocalDateTime.parse(cleanText, formatter)
                ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
            }.getOrNull()?.let { return it }
        }
        if (cleanText.contains('/') && cleanText.contains(':')) {
            runCatching {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                val ldt = java.time.LocalDateTime.parse(cleanText, formatter)
                ldt.atZone(java.time.ZoneId.systemDefault()).toInstant()
            }.getOrNull()?.let { return it }
        }
        if (cleanText.contains('/') && cleanText.length == 10) {
            runCatching {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val ld = java.time.LocalDate.parse(cleanText, formatter)
                ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
            }.getOrNull()?.let { return it }
        }
        runCatching { Instant.parse(cleanText) }.getOrNull()?.let { return it }
        runCatching { LocalDate.parse(cleanText.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC) }.getOrNull()?.let { return it }
        runCatching { DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(cleanText, Instant::from) }.getOrNull()?.let { return it }
    }
    primitive.doubleOrNull?.toLong()?.let { number ->
        return if (number > 9999999999L) Instant.ofEpochMilli(number) else Instant.ofEpochSecond(number)
    }
    return null
}

private fun stableId(root: JsonObject): String {
    val source = root.toString().ifBlank { UUID.randomUUID().toString() }
    return UUID.nameUUIDFromBytes(source.toByteArray()).toString()
}
