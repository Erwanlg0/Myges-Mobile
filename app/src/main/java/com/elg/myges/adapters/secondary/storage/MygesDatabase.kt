package com.elg.myges.adapters.secondary.storage

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
        ProjectStepEntity::class,
        PracticalEntity::class,
        AcademicDocumentEntity::class,
        NewsEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class MygesDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
