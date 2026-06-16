package com.elg.studly.adapters.secondary.storage

internal data class EntitySyncPlan<T>(
    val upserts: List<T>,
    val deletes: List<T>
)

internal fun <T, K> entitySyncPlan(
    current: List<T>,
    incoming: List<T>,
    key: (T) -> K
): EntitySyncPlan<T> {
    val currentByKey = current.associateBy(key)
    val incomingByKey = incoming.associateBy(key)
    return EntitySyncPlan(
        upserts = incoming.filter { entity -> currentByKey[key(entity)] != entity },
        deletes = current.filter { entity -> key(entity) !in incomingByKey }
    )
}
