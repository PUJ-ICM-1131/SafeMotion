package com.example.safemotion.data.model

// Un punto geográfico propio de la app, para no depender de las clases de osmdroid fuera del mapa.
data class MapPoint(
    val latitude: Double,
    val longitude: Double
)