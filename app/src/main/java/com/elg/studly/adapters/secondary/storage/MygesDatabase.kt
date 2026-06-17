package com.elg.studly.adapters.secondary.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentProfileEntity::class,
        AgendaEventEntity::class,
        GradeEntity::class,
        AbsenceEntity::class,
        CourseEntity::class,
        ProjectEntity::class,
        ProjectGroupEntity::class,
        ProjectStepEntity::class,
        PracticalEntity::class,
        AcademicDocumentEntity::class,
        DirectoryPersonEntity::class,
        NewsEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class MygesDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
