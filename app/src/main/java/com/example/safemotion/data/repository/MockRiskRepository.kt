package com.example.safemotion.data.repository

import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.model.RiskEvidence
import com.example.safemotion.data.model.RiskZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MockRiskRepository {
    const val CURRENT_LOCATION = "Parque Simón Bolívar, entrada Carrera 60"

    private val initialZones = listOf(
        RiskZone(
            id = "z1",
            category = RiskCategory.LIGHTING,
            place = "Calle 116 entre Carrera 15 y 19",
            description = "Tres postes apagados en la mitad de la cuadra. De 5 a 6 a. m. se ve muy oscuro y el andén es angosto.",
            reportedAt = "14 sep 2026",
            reporter = "Daniel O.",
            confirmations = 12,
            evidences = listOf(RiskEvidence("e1", "5:41 a. m."), RiskEvidence("e2", "5:43 a. m.")),
            mapX = -0.61f,
            mapY = -0.31f
        ),
        RiskZone(
            id = "z2",
            category = RiskCategory.INSECURITY,
            place = "Ciclovía de la Calle 80 con Carrera 68",
            description = "Intentaron robarle el celular a una ciclista al amanecer. Casi no hay vigilancia en el tramo.",
            reportedAt = "12 sep 2026",
            reporter = "Laura G.",
            confirmations = 8,
            evidences = listOf(RiskEvidence("e3", "5:52 a. m."), RiskEvidence("e4", "5:55 a. m.")),
            mapX = 0.23f,
            mapY = -0.41f
        ),
        RiskZone(
            id = "z3",
            category = RiskCategory.INSECURITY,
            place = "Sendero norte del Parque El Virrey",
            description = "Grupo de personas sospechosas rondando el sendero después de las 8 p. m.",
            reportedAt = "10 sep 2026",
            reporter = "Camilo R.",
            confirmations = 5,
            evidences = listOf(RiskEvidence("e5", "8:14 p. m.")),
            mapX = 0.72f,
            mapY = -0.13f
        ),
        RiskZone(
            id = "z4",
            category = RiskCategory.BAD_ROAD,
            place = "Carrera 15 con Calle 100",
            description = "Hueco grande en el carril derecho. Es muy peligroso para las bicicletas, sobre todo con poca luz.",
            reportedAt = "9 sep 2026",
            reporter = "Sofía C.",
            confirmations = 15,
            evidences = listOf(RiskEvidence("e6", "6:02 a. m."), RiskEvidence("e7", "6:03 a. m.")),
            mapX = -0.28f,
            mapY = 0.12f
        ),
        RiskZone(
            id = "z5",
            category = RiskCategory.HEAVY_TRAFFIC,
            place = "Autopista Norte con Calle 127",
            description = "Mucho tráfico pesado y buses sin espacio para los ciclistas entre las 6 y las 8 a. m.",
            reportedAt = "8 sep 2026",
            reporter = "Andrés P.",
            confirmations = 9,
            evidences = listOf(RiskEvidence("e8", "6:30 a. m.")),
            mapX = 0.54f,
            mapY = 0.37f
        ),
        RiskZone(
            id = "z6",
            category = RiskCategory.LIGHTING,
            place = "Carrera 7 con Calle 72",
            description = "Zona muy oscura porque los árboles tapan las luminarias.",
            reportedAt = "6 sep 2026",
            reporter = "Juliana M.",
            confirmations = 7,
            evidences = listOf(RiskEvidence("e9", "5:20 a. m.")),
            mapX = 0.03f,
            mapY = 0.51f
        )
    )

    private val freeSpots = listOf(
        -0.35f to -0.55f,
        0.45f to 0.05f,
        -0.65f to 0.40f,
        0.10f to -0.20f,
        0.70f to 0.60f
    )

    private val _zones = MutableStateFlow(initialZones)
    val zones = _zones.asStateFlow()

    private val _confirmedZoneIds = MutableStateFlow<Set<String>>(emptySet())
    val confirmedZoneIds = _confirmedZoneIds.asStateFlow()

    private var lastReportedZoneId: String? = null

    fun addReport(category: RiskCategory, description: String, place: String): RiskZone {
        val newReports = _zones.value.size - initialZones.size
        val spot = freeSpots[newReports % freeSpots.size]
        val zone = RiskZone(
            id = "z${_zones.value.size + 1}",
            category = category,
            place = place,
            description = description,
            reportedAt = "Hoy",
            reporter = currentReporter(),
            confirmations = 0,
            evidences = listOf(RiskEvidence("e${_zones.value.size + 10}", "Ahora")),
            mapX = spot.first,
            mapY = spot.second
        )
        _zones.value = _zones.value + zone
        lastReportedZoneId = zone.id
        return zone
    }

    fun confirm(zoneId: String) {
        if (zoneId in _confirmedZoneIds.value) return
        _confirmedZoneIds.value = _confirmedZoneIds.value + zoneId
        _zones.update { zones ->
            zones.map { if (it.id == zoneId) it.copy(confirmations = it.confirmations + 1) else it }
        }
    }

    fun consumeLastReportedZoneId(): String? {
        val id = lastReportedZoneId
        lastReportedZoneId = null
        return id
    }

    private fun currentReporter(): String {
        val parts = MockAuthRepository.currentUser?.name.orEmpty()
            .split(" ")
            .filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts.first()} ${parts.last().first()}."
            parts.size == 1 -> parts.first()
            else -> "Mariana R."
        }
    }
}