package com.elg.studly.adapters.secondary.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class EntitySyncPlanTest {
    @Test
    fun unchangedEntitiesAreNeitherUpsertedNorDeleted() {
        val current = listOf(TestEntity("1", "same"))
        val incoming = listOf(TestEntity("1", "same"))

        val plan = entitySyncPlan(current, incoming, TestEntity::id)

        assertEquals(emptyList<TestEntity>(), plan.upserts)
        assertEquals(emptyList<TestEntity>(), plan.deletes)
    }

    @Test
    fun changedEntitiesAreUpserted() {
        val current = listOf(TestEntity("1", "old"))
        val incoming = listOf(TestEntity("1", "new"))

        val plan = entitySyncPlan(current, incoming, TestEntity::id)

        assertEquals(listOf(TestEntity("1", "new")), plan.upserts)
        assertEquals(emptyList<TestEntity>(), plan.deletes)
    }

    @Test
    fun missingEntitiesAreDeletedAndNewEntitiesAreUpserted() {
        val current = listOf(TestEntity("1", "old"), TestEntity("2", "stale"))
        val incoming = listOf(TestEntity("1", "old"), TestEntity("3", "new"))

        val plan = entitySyncPlan(current, incoming, TestEntity::id)

        assertEquals(listOf(TestEntity("3", "new")), plan.upserts)
        assertEquals(listOf(TestEntity("2", "stale")), plan.deletes)
    }

    private data class TestEntity(
        val id: String,
        val value: String
    )
}
