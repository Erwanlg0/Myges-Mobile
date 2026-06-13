package com.elg.myges.adapters.secondary.api

import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
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
        avatarUrl = root.text("avatarUrl", "avatar", "picture")
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
    return arrayOrNested("agenda", "events", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val discipline = root["discipline"] as? JsonObject
        val startsAt = root.instant("startsAt", "start", "startDate", "dateStart", "begin", "start_date")
            ?: return@mapNotNull null
        val endsAt = root.instant("endsAt", "end", "endDate", "dateEnd", "end_date")
            ?: startsAt.plusSeconds(3600)
        AgendaEvent(
            id = root.text("id", "eventId", "uid", "reservation_id") ?: stableId(root),
            title = root.text("title", "name", "courseName", "matiere")
                ?: discipline?.text("name")
                ?: "",
            startsAt = startsAt,
            endsAt = endsAt,
            room = root.text("room", "classroom", "salle") ?: root.arrayText("rooms", "name"),
            teacher = root.text("teacher", "intervenant", "professor") ?: discipline?.text("teacher"),
            type = root.text("type", "kind"),
            modality = root.text("modality", "mode", "campus"),
            courseId = root.text("courseId", "rcId", "rc_id", "moduleId") ?: discipline?.text("rc_id")
        )
    }
}

fun JsonElement.toGrades(): List<Grade> {
    return arrayOrNested("grades", "items", "data").flatMap { element ->
        val root = element.objectOrData()
        val nestedGrades = root.array("grades")
        if (nestedGrades.isNotEmpty()) {
            nestedGrades.mapIndexedNotNull { index, gradeElement ->
                val gradeRoot = gradeElement as? JsonObject
                val value = gradeRoot?.number("value", "grade", "note") ?: gradeElement.numberOrNull()
                value?.let {
                    root.toGrade(
                        idSuffix = "grade-$index",
                        subject = gradeRoot?.text("subject", "title", "name", "evaluation"),
                        value = it,
                        scale = gradeRoot?.number("scale", "outOf", "bareme"),
                        date = gradeRoot?.localDate("date", "createdAt", "publishedAt")
                    )
                }
            }
        } else {
            val value = root.number("value", "grade", "note", "exam", "average", "ccaverage")
            value?.let { listOf(root.toGrade(value = it)) }.orEmpty()
        }
    }
}

fun JsonElement.toAbsences(): List<Absence> {
    return arrayOrNested("absences", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val startsAt = root.instant("startsAt", "start", "startDate", "dateStart", "date")
            ?: return@mapNotNull null
        val endsAt = root.instant("endsAt", "end", "endDate", "dateEnd")
            ?: startsAt.plusSeconds(3600)
        Absence(
            id = root.text("id", "absenceId", "uid") ?: stableId(root),
            courseName = root.text("courseName", "course_name", "course", "module", "matiere") ?: "",
            startsAt = startsAt,
            endsAt = endsAt,
            justified = root.bool("justified", "isJustified", "justifiee") == true,
            status = root.text("status", "state", "etat", "type"),
            reason = root.text("reason", "motif", "type")
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
            period = root.text("period", "trimester", "semester"),
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

fun JsonElement.toProjects(): List<Project> {
    return arrayOrNested("projects", "items", "data").map { element ->
        val root = element.objectOrData()
        val steps = root.array("steps", "projectSteps").map { step ->
            val stepRoot = step.objectOrData()
            ProjectStep(
                id = stepRoot.text("id", "stepId", "psp_id", "uid") ?: stableId(stepRoot),
                title = stepRoot.text("title", "name", "psp_desc", "psp_type") ?: "",
                deadline = stepRoot.instant("deadline", "dueDate", "endDate", "psp_limit_date"),
                status = stepRoot.text("status", "state")
            )
        }
        val stepFileCount = root.array("steps", "projectSteps").sumOf { it.objectOrData().array("files").size }
        Project(
            id = root.text("id", "projectId", "project_id", "uid") ?: stableId(root),
            name = root.text("name", "title") ?: "",
            courseName = root.text("courseName", "course_name", "course", "module"),
            groupName = root.text("groupName", "group", "projectGroup"),
            status = root.text("status", "state"),
            deadline = root.instant("deadline", "dueDate", "endDate", "update_date") ?: steps.mapNotNull { it.deadline }.minOrNull(),
            steps = steps,
            fileCount = root.array("files", "project_files", "documents", "deliverables").size + stepFileCount
        )
    }
}

fun JsonElement.toNextProjectStepProjects(): List<Project> {
    return arrayOrNested("projectSteps", "steps", "items", "data").mapNotNull { element ->
        val root = element.objectOrData()
        val projectId = root.text("project_id", "projectId", "pro_id") ?: return@mapNotNull null
        val step = ProjectStep(
            id = root.text("psp_id", "stepId", "id", "uid") ?: stableId(root),
            title = root.text("psp_desc", "title", "name", "psp_type", "type") ?: "",
            deadline = root.instant("psp_limit_date", "deadline", "dueDate", "endDate"),
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
            fileCount = 0
        )
    }
}

fun JsonElement.toPracticals(): List<Practical> {
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
            status = root.text("status", "state")
        )
    }
}

fun JsonElement.toDocuments(): List<AcademicDocument> {
    return arrayOrNested("annualDocuments", "documents", "items", "data").map { element ->
        val root = element.objectOrData()
        root.toDocument()
    }
}

fun JsonElement.toProjectDocuments(): List<AcademicDocument> {
    return arrayOrNested("projects", "items", "data").flatMap { element ->
        val projectRoot = element.objectOrData()
        val projectFiles = projectRoot.array("project_files").map { file ->
            val root = file.objectOrData()
            val document = root.toDocument(parentTitle = projectRoot.text("name", "title"), parentYear = projectRoot.text("year", "academicYear"))
            document.copy(downloadUrl = document.downloadUrl ?: "me/projectFiles/${document.id}")
        }
        val stepFiles = projectRoot.array("steps", "projectSteps").flatMap { step ->
            val stepRoot = step.objectOrData()
            stepRoot.array("files").map { file ->
                val root = file.objectOrData()
                val document = root.toDocument(parentTitle = stepRoot.text("title", "name", "psp_desc", "psp_type"), parentYear = projectRoot.text("year", "academicYear"))
                document.copy(downloadUrl = document.downloadUrl ?: "me/projectStepFiles/${document.id}")
            }
        }
        projectFiles + stepFiles
    }
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
    return Regex("\\d{4}").find(value)?.value ?: value
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

private fun JsonObject.toDocument(
    parentTitle: String? = null,
    parentYear: String? = null
): AcademicDocument {
    val title = text("title", "name", "label", "pf_title") ?: parentTitle ?: ""
    return AcademicDocument(
        id = text("id", "documentId", "document_id", "oc_id", "pf_id", "psf_id", "uid") ?: stableId(this),
        title = title,
        category = text("category", "type"),
        year = text("year", "academicYear") ?: parentYear,
        mimeType = text("mimeType", "contentType", "extension", "psf_file_type")?.toMimeType(),
        fileName = text("fileName", "filename", "file", "pf_file", "psf_file", "psf_name") ?: title.ifBlank { "document" },
        downloadUrl = text("downloadUrl", "url", "href") ?: linkHref(),
        updatedAt = instant("updatedAt", "last_update", "update_date", "pf_crea_date", "psf_end_upload", "psf_begin_upload", "date", "createdAt")
    )
}

private fun JsonObject.linkHref(): String? {
    return array("links")
        .mapNotNull { (it as? JsonObject)?.text("href", "url") }
        .firstOrNull()
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
    value: Double,
    scale: Double? = null,
    date: LocalDate? = null
): Grade {
    val baseId = text("id", "gradeId", "uid", "rc_id") ?: stableId(this)
    return Grade(
        id = listOfNotNull(baseId, idSuffix).joinToString("-"),
        courseName = text("courseName", "course", "module", "matiere") ?: "",
        subject = subject ?: text("subject", "title", "name", "evaluation", "trimester_name") ?: "",
        value = value,
        scale = scale ?: number("scale", "outOf", "bareme") ?: 20.0,
        coefficient = number("coefficient", "coef"),
        average = number("average", "moyenne", "ccaverage"),
        date = date ?: localDate("date", "createdAt", "publishedAt"),
        period = text("period", "trimester", "semester", "trimester_name")
    )
}

private fun parseInstant(value: JsonElement?): Instant? {
    if (value == null || value is JsonNull) return null
    val primitive = value as? JsonPrimitive ?: return null
    primitive.contentOrNull?.let { text ->
        text.toLongOrNull()?.let { number ->
            return if (number > 9999999999L) Instant.ofEpochMilli(number) else Instant.ofEpochSecond(number)
        }
        runCatching { Instant.parse(text) }.getOrNull()?.let { return it }
        runCatching { LocalDate.parse(text.take(10)).atStartOfDay().toInstant(ZoneOffset.UTC) }.getOrNull()?.let { return it }
        runCatching { DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(text, Instant::from) }.getOrNull()?.let { return it }
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
