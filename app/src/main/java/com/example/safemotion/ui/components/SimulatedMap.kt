package com.example.safemotion.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.safemotion.R
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.data.model.RiskZone

/**Mapa simulado: una imagen de fondo con los marcadores de las zonas encima.*/
@Composable
fun SimulatedMap(
    zones: List<RiskZone>,
    selectedZoneId: String?,
    onZoneClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.map_background),
            contentDescription = stringResource(R.string.risk_map_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        zones.forEach { zone ->
            RiskMarker(
                category = zone.category,
                selected = zone.id == selectedZoneId,
                onClick = { onZoneClick(zone.id) },
                modifier = Modifier.align(BiasAlignment(zone.mapX, zone.mapY))
            )
        }
    }
}

@Composable
private fun RiskMarker(
    category: RiskCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(48.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(category.markerColor().copy(alpha = 0.3f), CircleShape)
            )
        }
        RiskCategoryBadge(
            category = category,
            size = if (selected) 32.dp else 28.dp,
            modifier = if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier
        )
    }
}