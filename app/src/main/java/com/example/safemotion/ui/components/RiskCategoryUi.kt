package com.example.safemotion.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.safemotion.R
import com.example.safemotion.data.model.RiskCategory
import com.example.safemotion.ui.theme.RiskBadRoadColor
import com.example.safemotion.ui.theme.RiskHeavyTrafficColor
import com.example.safemotion.ui.theme.RiskInsecurityColor
import com.example.safemotion.ui.theme.RiskLightingColor

@StringRes
fun RiskCategory.labelRes(): Int = when (this) {
    RiskCategory.LIGHTING -> R.string.risk_category_lighting
    RiskCategory.INSECURITY -> R.string.risk_category_insecurity
    RiskCategory.BAD_ROAD -> R.string.risk_category_bad_road
    RiskCategory.HEAVY_TRAFFIC -> R.string.risk_category_heavy_traffic
}

fun RiskCategory.icon(): ImageVector = when (this) {
    RiskCategory.LIGHTING -> Icons.Default.Lightbulb
    RiskCategory.INSECURITY -> Icons.Default.GppMaybe
    RiskCategory.BAD_ROAD -> Icons.Default.Construction
    RiskCategory.HEAVY_TRAFFIC -> Icons.Default.Traffic
}

fun RiskCategory.markerColor(): Color = when (this) {
    RiskCategory.LIGHTING -> RiskLightingColor
    RiskCategory.INSECURITY -> RiskInsecurityColor
    RiskCategory.BAD_ROAD -> RiskBadRoadColor
    RiskCategory.HEAVY_TRAFFIC -> RiskHeavyTrafficColor
}