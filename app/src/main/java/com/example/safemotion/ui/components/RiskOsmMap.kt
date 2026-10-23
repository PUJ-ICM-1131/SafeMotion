package com.example.safemotion.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.safemotion.R
import com.example.safemotion.data.model.MapPoint
import com.example.safemotion.data.model.RiskZone
import com.example.safemotion.ui.theme.Indigo
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

private val BogotaCenter = MapPoint(latitude = 4.6765, longitude = -74.0600)

// OpenStreetMap exige que cada app se identifique con un nombre propio y descriptivo.
private const val OsmUserAgent = "SafeMotion/0.2 (app academica Android, Pontificia Universidad Javeriana)"


// API key gratuita de MapTiler.
private const val MapTilerKey = "9iRoHwbvd5ReyrwkqfoT"

// Cuadros del mapa de MapTiler (estilo Streets), con datos de OpenStreetMap.
private object MapTilerStreets : OnlineTileSourceBase(
    "MapTilerStreets",
    0,
    19,
    256,
    ".png",
    arrayOf("https://api.maptiler.com/maps/streets-v2/256/"),
    "© MapTiler © OpenStreetMap contributors"
) {
    // MapTiler necesita la key al final de la URL: .../zoom/x/y.png?key=...
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) +
                imageFilenameEnding() +
                "?key=" + MapTilerKey
}

/**
 * Mapa real de OpenStreetMap (osmdroid) con un marcador por zona de riesgo
 * y, si se conoce, la ubicación actual del usuario.
 */
@Composable
fun RiskOsmMap(
    zones: List<RiskZone>,
    selectedZoneId: String?,
    onZoneClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: MapPoint? = null,
    initialCenter: MapPoint = BogotaCenter
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val myLocationLabel = stringResource(R.string.map_my_location)

    // El MapView se crea una sola vez y sobrevive a las recomposiciones.
    val mapView = remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = OsmUserAgent
        }
        MapView(context).apply {
            setTileSource(MapTilerStreets)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(13.0)
            controller.setCenter(GeoPoint(initialCenter.latitude, initialCenter.longitude))
        }
    }

    // El mapa se pausa y reanuda con el ciclo de vida de la pantalla.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Al seleccionar una zona, el mapa se mueve hasta ella.
    LaunchedEffect(selectedZoneId) {
        zones.firstOrNull { it.id == selectedZoneId }?.let { zone ->
            mapView.controller.animateTo(GeoPoint(zone.latitude, zone.longitude))
        }
    }

    // Cuando llega la ubicación del usuario, el mapa se centra en ella.
    LaunchedEffect(userLocation) {
        userLocation?.let { mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude)) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()
            // Créditos obligatorios de OpenStreetMap en la esquina del mapa.
            map.overlays.add(CopyrightOverlay(map.context))
            zones.forEach { zone ->
                val marker = Marker(map).apply {
                    position = GeoPoint(zone.latitude, zone.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = pinIcon(map.context, zone.category.markerColor())
                    alpha = if (selectedZoneId == null || zone.id == selectedZoneId) 1f else 0.5f
                    setOnMarkerClickListener { _, _ ->
                        onZoneClick(zone.id)
                        true
                    }
                }
                map.overlays.add(marker)
            }
            userLocation?.let { location ->
                val me = Marker(map).apply {
                    position = GeoPoint(location.latitude, location.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = pinIcon(map.context, Indigo)
                    title = myLocationLabel
                }
                map.overlays.add(me)
            }
            map.invalidate()
        }
    )
}

// Copia el pin y lo pinta del color indicado.
private fun pinIcon(context: Context, color: Color) =
    ContextCompat.getDrawable(context, R.drawable.ic_map_pin)
        ?.mutate()
        ?.apply { setTint(color.toArgb()) }