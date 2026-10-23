package com.example.safemotion.data.model

enum class RiskCategory { LIGHTING, INSECURITY, BAD_ROAD, HEAVY_TRAFFIC }

data class RiskEvidence(
    val id: String,
    val capturedAt: String,
    val photoPath: String? = null
)

data class RiskZone(
    val id: String,
    val category: RiskCategory,
    val place: String,
    val description: String,
    val reportedAt: String,
    val reporter: String,
    val confirmations: Int,
    val evidences: List<RiskEvidence>,
    val latitude: Double,
    val longitude: Double
)