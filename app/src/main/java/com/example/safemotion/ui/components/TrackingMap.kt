package com.example.safemotion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.safemotion.data.model.TrackPoint

@Composable
fun TrackingMap(
    route: List<TrackPoint>,
    interrupted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE7E9F5))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFFCCD0DF)
            repeat(6) { index ->
                val horizontal = size.height * index / 5f
                val vertical = size.width * index / 5f
                drawLine(gridColor, Offset(0f, horizontal), Offset(size.width, horizontal), 2f)
                drawLine(gridColor, Offset(vertical, 0f), Offset(vertical, size.height), 2f)
            }
            if (route.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(route.first().x * size.width, route.first().y * size.height)
                    route.drop(1).forEach { point ->
                        lineTo(point.x * size.width, point.y * size.height)
                    }
                }
                val routeColor = if (interrupted) Color(0xFFBA1A1A) else Color(0xFF4A48D9)
                drawPath(path, routeColor, style = Stroke(width = 10f))
                val last = route.last()
                drawCircle(routeColor, 18f, Offset(last.x * size.width, last.y * size.height))
                drawCircle(Color.White, 7f, Offset(last.x * size.width, last.y * size.height))
            }
        }
    }
}
