package com.elg.studly.adapters.secondary.storage

import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.DirectoryRole
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectGroup
import com.elg.studly.domain.model.ProjectStep
import com.elg.studly.domain.model.StudentProfile
import java.time.Instant
import java.time.LocalDate

fun StudentProfile.toEntity() = StudentProfileEntity(
    id = id,
    displayName = displayName,
    email = email,
    school = school,
    program = program,
    academicYear = academicYear,
    avatarUrl = avatarUrl
)

fun StudentProfileEntity.toDomain() = StudentProfile(
    id = id,
    displayName = displayName,
    email = email,
    school = school,
    program = program,
    academicYear = academicYear,
    avatarUrl = avatarUrl
)

fun AgendaEvent.toEntity() = AgendaEventEntity(
    id = id,
    title = title,
    startsAtEpochMillis = startsAt.toEpochMilli(),
    endsAtEpochMillis = endsAt.toEpochMilli(),
    room = room,
    teacher = teacher,
    type = type,
    modality = modality,
    courseId = courseId,
    address = address,
    colorId = colorId
)

fun AgendaEventEntity.toDomain() = AgendaEvent(
    id = id,
    title = title,
    startsAt = Instant.ofEpochMilli(startsAtEpochMillis),
    endsAt = Instant.ofEpochMilli(endsAtEpochMillis),
    room = room,
    teacher = teacher,
    type = type,
    modality = modality,
    courseId = courseId,
    address = address,
    colorId = colorId
)

fun Grade.toEntity() = GradeEntity(
    id = id,
    courseName = courseName,
    subject = subject,
    value = value,
    scale = scale,
    coefficient = coefficient,
    average = average,
    dateIso = date?.toString(),
    period = period
)

fun GradeEntity.toDomain() = Grade(
    id = id,
    courseName = courseName,
    subject = subject,
    value = value,
    scale = scale,
    coefficient = coefficient,
    average = average,
    date = dateIso?.let { LocalDate.parse(it) },
    period = period
)

fun Absence.toEntity() = AbsenceEntity(
    id = id,
    courseName = courseName,
    startsAtEpochMillis = startsAt.toEpochMilli(),
    endsAtEpochMillis = endsAt.toEpochMilli(),
    justified = justified,
    status = status,
    reason = reason,
    period = period
)

fun AbsenceEntity.toDomain() = Absence(
    id = id,
    courseName = courseName,
    startsAt = Instant.ofEpochMilli(startsAtEpochMillis),
    endsAt = Instant.ofEpochMilli(endsAtEpochMillis),
    justified = justified,
    status = status,
    reason = reason,
    period = period
)

fun Course.toEntity() = CourseEntity(
    id = id,
    name = name,
    teacher = teacher,
    year = year,
    period = period,
    syllabus = syllabus,
    fileCount = fileCount,
    location = location
)

fun CourseEntity.toDomain() = Course(
    id = id,
    name = name,
    teacher = teacher,
    year = year,
    period = period,
    syllabus = syllabus,
    fileCount = fileCount,
    location = location
)

fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    courseName = courseName,
    groupName = groupName,
    status = status,
    deadlineEpochMillis = deadline?.toEpochMilli(),
    fileCount = fileCount,
    year = year,
    courseId = courseId,
    startsAtEpochMillis = startsAt?.toEpochMilli()
)

fun Project.toGroupEntities() = groups.map { group ->
    ProjectGroupEntity(
        projectId = id,
        id = group.id,
        name = group.name,
        students = group.students.joinToString("\n"),
        isMine = group.isMine
    )
}

fun Project.toStepEntities() = steps.map { step ->
    ProjectStepEntity(
        projectId = id,
        id = step.id,
        title = step.title,
        deadlineEpochMillis = step.deadline?.toEpochMilli(),
        status = step.status
    )
}

fun ProjectEntity.toDomain(
    steps: List<ProjectStepEntity>,
    groups: List<ProjectGroupEntity> = emptyList()
) = Project(
    id = id,
    name = name,
    courseName = courseName,
    groupName = groupName,
    status = status,
    deadline = deadlineEpochMillis?.let { Instant.ofEpochMilli(it) },
    steps = steps.map { it.toDomain() },
    fileCount = fileCount,
    year = year,
    courseId = courseId,
    groups = groups.map { it.toDomain() },
    startsAt = startsAtEpochMillis?.let { Instant.ofEpochMilli(it) }
)

fun ProjectGroupEntity.toDomain() = ProjectGroup(
    id = id,
    name = name,
    students = students.lines().filter { it.isNotBlank() },
    isMine = isMine
)

fun ProjectStepEntity.toDomain() = ProjectStep(
    id = id,
    title = title,
    deadline = deadlineEpochMillis?.let { Instant.ofEpochMilli(it) },
    status = status
)

fun Practical.toEntity() = PracticalEntity(
    id = id,
    name = name,
    courseName = courseName,
    startsAtEpochMillis = startsAt?.toEpochMilli(),
    endsAtEpochMillis = endsAt?.toEpochMilli(),
    room = room,
    status = status,
    year = year,
    courseId = courseId
)

fun PracticalEntity.toDomain(
    steps: List<ProjectStepEntity> = emptyList(),
    groups: List<ProjectGroupEntity> = emptyList()
) = Practical(
    id = id,
    name = name,
    courseName = courseName,
    startsAt = startsAtEpochMillis?.let { Instant.ofEpochMilli(it) },
    endsAt = endsAtEpochMillis?.let { Instant.ofEpochMilli(it) },
    room = room,
    status = status,
    year = year,
    courseId = courseId,
    steps = steps.map { it.toDomain() },
    groups = groups.map { it.toDomain() }
)

fun Practical.toGroupEntities() = groups.map { group ->
    ProjectGroupEntity(
        projectId = id,
        id = group.id,
        name = group.name,
        students = group.students.joinToString("\n"),
        isMine = group.isMine
    )
}

fun Practical.toStepEntities() = steps.map { step ->
    ProjectStepEntity(
        projectId = id,
        id = step.id,
        title = step.title,
        deadlineEpochMillis = step.deadline?.toEpochMilli(),
        status = step.status
    )
}

fun AcademicDocument.toEntity() = AcademicDocumentEntity(
    id = id,
    title = title,
    category = category,
    year = year,
    mimeType = mimeType,
    fileName = fileName,
    downloadUrl = downloadUrl,
    updatedAtEpochMillis = updatedAt?.toEpochMilli(),
    ownerId = ownerId,
    groupId = groupId,
    inlineContent = inlineContent
)

fun AcademicDocumentEntity.toDomain() = AcademicDocument(
    id = id,
    title = title,
    category = category,
    year = year,
    mimeType = mimeType,
    fileName = fileName,
    downloadUrl = downloadUrl,
    updatedAt = updatedAtEpochMillis?.let { Instant.ofEpochMilli(it) },
    ownerId = ownerId,
    groupId = groupId,
    inlineContent = inlineContent
)

fun DirectoryPerson.toEntity() = DirectoryPersonEntity(
    id = id,
    displayName = displayName,
    email = email,
    role = role.name,
    year = year,
    groupName = groupName,
    avatarUrl = avatarUrl
)

fun DirectoryPersonEntity.toDomain() = DirectoryPerson(
    id = id,
    displayName = displayName,
    email = email,
    role = runCatching { DirectoryRole.valueOf(role) }.getOrDefault(DirectoryRole.Student),
    year = year,
    groupName = groupName,
    avatarUrl = avatarUrl
)

fun NewsItem.toEntity() = NewsEntity(
    id = id,
    title = title,
    body = body,
    publishedAtEpochMillis = publishedAt?.toEpochMilli()
)

fun NewsEntity.toDomain() = NewsItem(
    id = id,
    title = title,
    body = body,
    publishedAt = publishedAtEpochMillis?.let { Instant.ofEpochMilli(it) }
)
