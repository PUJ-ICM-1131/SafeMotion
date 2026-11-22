package com.example.safemotion.notifications

class NewDocumentTracker(private val startedAtMillis: Long) {
    private val seen = mutableMapOf<String, Long?>()

    fun newIds(documents: Map<String, Long?>): List<String> {
        val newIds = mutableListOf<String>()
        for ((id, createdAtMillis) in documents) {
            val known = seen.containsKey(id)
            val previousTime = seen[id]
            if (!known || previousTime != createdAtMillis) {
                seen[id] = createdAtMillis
                if (createdAtMillis != null && createdAtMillis >= startedAtMillis) {
                    newIds += id
                }
            }
        }
        return newIds
    }
}
