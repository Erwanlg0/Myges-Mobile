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
    val id = root.text("id", "uid", "puid", "studentId") ?: "profile"
    val firstName = root.text("firstName", "firstname", "prenom")
    val lastName = root.text("lastName", "lastname", "nom")
    val displayName = root.text("displayName", "fullName", "name", "nomComplet")
        ?: listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { id }
    return StudentProfile(
        id = id,
        displayName = displayName,
        email = root.text("email", "mail"),
        school = root.text("school", "campus", "institution"),
        program = root.text("program", "programme", "formation", "className"),
        academicYear = root.text("academicYear", "year", "annee"),
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
        val startsAt = root.instant("startsAt", "start", "startDate", "dateStart")
            ?: return@mapNotNull null
        val endsAt = root.instant("endsAt", "end", "endDate", "dateEnd")
            ?: startsAt.plusSeconds(3600)
        Absence(
            id = root.text("id", "absenceId", "uid") ?: stableId(root),
            courseName = root.text("courseName", "course", "module", "matiere") ?: "",
            startsAt = startsAt,
            endsAt = endsAt,
            justified = root.bool("justified", "isJustified", "justifiee") == true,
            status = root.text("status", "state", "etat"),
            reason = root.text("reason", "motif")
        )
    }
}

fun JsonElement.toCourses(): List<Course> {
    return arrayOrNested("courses", "items", "data").map { element ->
        val root = element.objectOrData()
        val files = root.array("files", "documents")
        Course(
            id = root.text("id", "rcId", "courseId", "uid") ?: stableId(root),
            name = root.text("name", "title", "courseName", "matiere") ?: "",
            teacher = root.text("teacher", "intervenant", "professor"),
            year = root.text("year", "academicYear"),
            period = root.text("period", "trimester", "semester"),
            syllabus = root.text("syllabus", "description", "summary"),
            fileCount = files.size
        )
    }
}

fun JsonElement.toProjects(): List<Project> {
    return arrayOrNested("projects", "items", "data").map { element ->
        val root = element.objectOrData()
        val steps = root.array("steps", "projectSteps").map { step ->
            val stepRoot = step.objectOrData()
            ProjectStep(
                id = stepRoot.text("id", "stepId", "uid") ?: stableId(stepRoot),
                title = stepRoot.text("title", "name") ?: "",
                deadline = stepRoot.instant("deadline", "dueDate", "endDate"),
                status = stepRoot.text("status", "state")
            )
        }
        Project(
            id = root.text("id", "projectId", "uid") ?: stableId(root),
            name = root.text("name", "title") ?: "",
            courseName = root.text("courseName", "course", "module"),
            groupName = root.text("groupName", "group", "projectGroup"),
            status = root.text("status", "state"),
            deadline = root.instant("deadline", "dueDate", "endDate"),
            steps = steps,
            fileCount = root.array("files", "documents", "deliverables").size
        )
    }
}

fun JsonElement.toPracticals(): List<Practical> {
    return arrayOrNested("practicals", "items", "data").map { element ->
        val root = element.objectOrData()
        Practical(
            id = root.text("id", "practicalId", "uid") ?: stableId(root),
            name = root.text("name", "title") ?: "",
            courseName = root.text("courseName", "course", "module"),
            startsAt = root.instant("startsAt", "start", "startDate", "dateStart"),
            endsAt = root.instant("endsAt", "end", "endDate", "dateEnd"),
            room = root.text("room", "classroom", "salle"),
            status = root.text("status", "state")
        )
    }
}

fun JsonElement.toDocuments(): List<AcademicDocument> {
    return arrayOrNested("annualDocuments", "documents", "items", "data").map { element ->
        val root = element.objectOrData()
        val title = root.text("title", "name", "label") ?: ""
        AcademicDocument(
            id = root.text("id", "documentId", "uid") ?: stableId(root),
            title = title,
            category = root.text("category", "type"),
            year = root.text("year", "academicYear"),
            mimeType = root.text("mimeType", "contentType"),
            fileName = root.text("fileName", "filename") ?: title.ifBlank { "document" },
            downloadUrl = root.text("downloadUrl", "url", "href"),
            updatedAt = root.instant("updatedAt", "date", "createdAt")
        )
    }
}

fun JsonElement.toNews(): List<NewsItem> {
    return arrayOrNested("news", "banners", "items", "data").map { element ->
        val root = element.objectOrData()
        NewsItem(
            id = root.text("id", "newsId", "uid") ?: stableId(root),
            title = root.text("title", "name") ?: "",
            body = root.text("body", "content", "description"),
            publishedAt = root.instant("publishedAt", "date", "createdAt")
        )
    }
}

private fun JsonElement.objectOrData(): JsonObject {
    val root = this as? JsonObject ?: return JsonObject(emptyMap())
    return (root["data"] as? JsonObject) ?: root
}

private fun JsonElement.arrayOrNested(vararg keys: String): List<JsonElement> {
    if (this is JsonArray) return this.toList()
    val root = this as? JsonObject ?: return emptyList()
    (keys.toList() + listOf("result", "data", "items", "results")).distinct().forEach { key ->
        val value = root[key]
        if (value is JsonArray) return value.toList()
        if (value is JsonObject) {
            val nested = value.arrayOrNested("items", "data", "results")
            if (nested.isNotEmpty()) return nested
        }
    }
    return root.values.firstOrNull { it is JsonArray }?.jsonArray?.toList().orEmpty()
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
