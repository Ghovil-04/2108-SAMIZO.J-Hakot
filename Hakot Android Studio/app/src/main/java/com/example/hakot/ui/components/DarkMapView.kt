package com.example.hakot.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.hakot.data.HakotRepository
import com.example.hakot.data.GisRoadSegmentFeature
import com.example.hakot.data.OsrmRoutingHelper
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.osmdroid.util.GeoPoint
import java.time.LocalDate

data class SelectedMarkerInfo(
    val title: String,
    val subtitle: String,
    val type: String,
    val color: Color,
    val details: List<Pair<String, String>>
)

@SuppressLint("ClickableViewAccessibility")
@Composable
fun DarkMapView(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onReportClick: () -> Unit = {},
    onMapInteractionStateChange: (isInteracting: Boolean) -> Unit = {},
    customRoutePoints: List<GeoPoint> = emptyList(),
    customRouteSegments: List<List<GeoPoint>> = emptyList(),
    onMapClick: ((LatLng) -> Unit)? = null,
    barangayRoads: List<GisRoadSegmentFeature> = emptyList(),
    selectedRoadIds: List<String> = emptyList()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var idleJob by remember { mutableStateOf<Job?>(null) }

    remember(context) {
        MapLibre.getInstance(context.applicationContext)
        true
    }

    var selectedMarker by remember { mutableStateOf<SelectedMarkerInfo?>(null) }
    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }
    var fetchedRoutePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var fetchedRouteSegments by remember { mutableStateOf<List<List<GeoPoint>>>(emptyList()) }
    var flowAnimPhase by remember { mutableStateOf(0) }

    val nasugbuCenter = remember { LatLng(14.0700, 120.6300) }
    val mapView = remember { MapView(context) }

    // Continuous flowing line animation timer (300ms, 5 frames, reaching next dot)
    LaunchedEffect(isStyleLoaded, fetchedRoutePoints) {
        if (isStyleLoaded) {
            while (true) {
                delay(300)
                flowAnimPhase = (flowAnimPhase + 1) % 5
            }
        }
    }

    LaunchedEffect(HakotRepository.selectedMapDate, customRoutePoints, customRouteSegments, barangayRoads, selectedRoadIds) {
        fetchedRouteSegments = customRouteSegments
        val pointsToRender = if (customRoutePoints.isNotEmpty()) {
            customRoutePoints
        } else if (customRouteSegments.isNotEmpty()) {
            customRouteSegments.flatten()
        } else {
            Log.w("HAKOT_ROUTE", "WARNING: customRoutePoints is empty. No route line will be rendered; no fallback route substituted.")
            emptyList()
        }
        fetchedRoutePoints = pointsToRender
    }

    LaunchedEffect(isStyleLoaded, fetchedRoutePoints, fetchedRouteSegments, barangayRoads, selectedRoadIds, flowAnimPhase) {
        if (isStyleLoaded) {
            mapLibreMapInstance?.let { maplibreMap ->
                setupMapLibreRoadNetworkOverlay(maplibreMap, barangayRoads, selectedRoadIds)
                setupMapLibreRouteOverlay(maplibreMap, fetchedRouteSegments, fetchedRoutePoints, flowAnimPhase)
                if (fetchedRoutePoints.isNotEmpty()) {
                    try {
                        val validPts = fetchedRoutePoints.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
                        if (validPts.size == 1) {
                            val pt = validPts[0]
                            maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pt.latitude, pt.longitude), 16.0), 300)
                        } else if (validPts.size >= 2) {
                            val bounds = LatLngBounds.Builder().apply {
                                validPts.forEach { pt ->
                                    include(LatLng(pt.latitude, pt.longitude))
                                }
                            }.build()
                            maplibreMap.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60), 500)
                        }
                    } catch (e: Exception) {
                        Log.e("HAKOT_ROUTE", "Error easing camera to route bounds", e)
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF4F3EE))) {
        // MapLibre Native Android View with OpenFreeMap Public Style (Zero 403 Errors, Zero API Keys, Vector Quality)
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { maplibreMap ->
                        mapLibreMapInstance = maplibreMap

                        // Enable 3D Tilt, Rotation, Pinch Zoom & Panning
                        maplibreMap.uiSettings.isZoomGesturesEnabled = true
                        maplibreMap.uiSettings.isScrollGesturesEnabled = true
                        maplibreMap.uiSettings.isRotateGesturesEnabled = true
                        maplibreMap.uiSettings.isTiltGesturesEnabled = true
                        maplibreMap.uiSettings.isDoubleTapGesturesEnabled = true

                        if (onMapClick != null) {
                            maplibreMap.addOnMapClickListener { latLng ->
                                onMapClick(latLng)
                                true
                            }
                        }

                        // Set OpenFreeMap Public Style
                        maplibreMap.setStyle("https://tiles.openfreemap.org/styles/bright") { _ ->
                            Log.d("HAKOT_ROUTE", "STYLE_LOADED: MapLibre style callback invoked.")
                            isStyleLoaded = true
                            maplibreMap.cameraPosition = CameraPosition.Builder()
                                .target(nasugbuCenter)
                                .zoom(15.0)
                                .tilt(20.0)
                                .build()

                            setupMapLibreRoadNetworkOverlay(maplibreMap, barangayRoads, selectedRoadIds)
                            setupMapLibreRouteOverlay(maplibreMap, fetchedRouteSegments, fetchedRoutePoints, flowAnimPhase)
                        }

                        // Touch listener to disallow parent intercept and hide search bar during map gestures
                        setOnTouchListener { v, event ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                            when (event.actionMasked) {
                                MotionEvent.ACTION_DOWN,
                                MotionEvent.ACTION_MOVE,
                                MotionEvent.ACTION_POINTER_DOWN -> {
                                    idleJob?.cancel()
                                    onMapInteractionStateChange(true)
                                }
                                MotionEvent.ACTION_UP,
                                MotionEvent.ACTION_CANCEL,
                                MotionEvent.ACTION_POINTER_UP -> {
                                    v.performClick()
                                    idleJob?.cancel()
                                    idleJob = scope.launch {
                                        delay(1500)
                                        onMapInteractionStateChange(false)
                                    }
                                }
                            }
                            v.onTouchEvent(event)
                        }
                    }
                }
            },
            update = {
                mapLibreMapInstance?.let { maplibreMap ->
                    if (isStyleLoaded) {
                        setupMapLibreRoadNetworkOverlay(maplibreMap, barangayRoads, selectedRoadIds)
                        setupMapLibreRouteOverlay(maplibreMap, fetchedRouteSegments, fetchedRoutePoints, flowAnimPhase)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // MapLibre Lifecycle Management
        DisposableEffect(mapView) {
            mapView.onStart()
            mapView.onResume()
            onDispose {
                mapView.onPause()
                mapView.onStop()
                mapView.onDestroy()
            }
        }

        // Selected Route Details Overlay Card
        AnimatedVisibility(
            visible = selectedMarker != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedMarker?.let { info ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, info.color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(info.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = info.title,
                                    color = TextOnDarkPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { selectedMarker = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextOnDarkSecondary
                                )
                            }
                        }

                        Text(
                            text = info.subtitle,
                            color = TextOnDarkSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 18.dp, bottom = 8.dp)
                        )

                        info.details.forEach { (k, v) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(k, fontSize = 12.sp, color = TextOnDarkSecondary)
                                Text(v, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextOnDarkPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun setupMapLibreRouteOverlay(
    maplibreMap: MapLibreMap,
    segments: List<List<GeoPoint>>,
    points: List<GeoPoint>,
    flowPhase: Int = 0
) {
    val style = maplibreMap.style ?: run {
        Log.w("HAKOT_ROUTE", "MapLibre style is null when trying to setup route overlay.")
        return
    }

    val sourceId = "hakot-route-source"
    val layerId = "hakot-route-layer"
    val flowLayerId = "hakot-route-flow-layer"

    try {
        if (style.getLayer(flowLayerId) != null) style.removeLayer(flowLayerId)
        if (style.getLayer(layerId) != null) style.removeLayer(layerId)
        if (style.getSource(sourceId) != null) style.removeSource(sourceId)

        val lineFeatures = mutableListOf<Feature>()
        val isSinglePoint = segments.isEmpty() && points.size == 1

        if (segments.isNotEmpty()) {
            segments.forEach { segPts ->
                val valid = segPts.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
                if (valid.size >= 2) {
                    val line = LineString.fromLngLats(valid.map { Point.fromLngLat(it.longitude, it.latitude) })
                    lineFeatures.add(Feature.fromGeometry(line))
                }
            }
        } else if (points.isNotEmpty()) {
            val valid = points.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
            if (valid.size == 1) {
                val pt = valid[0]
                lineFeatures.add(Feature.fromGeometry(Point.fromLngLat(pt.longitude, pt.latitude)))
            } else if (valid.size >= 2) {
                val line = LineString.fromLngLats(valid.map { Point.fromLngLat(it.longitude, it.latitude) })
                lineFeatures.add(Feature.fromGeometry(line))
            }
        }

        if (lineFeatures.isNotEmpty()) {
            val featureCollection = FeatureCollection.fromFeatures(lineFeatures)

            style.addSource(GeoJsonSource(sourceId, featureCollection))

            if (isSinglePoint) {
                val circleLayer = CircleLayer(layerId, sourceId).apply {
                    setProperties(
                        PropertyFactory.circleRadius(16f),
                        PropertyFactory.circleColor(android.graphics.Color.parseColor("#10B981")),
                        PropertyFactory.circleStrokeWidth(4f),
                        PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
                    )
                }
                style.addLayer(circleLayer)
                Log.d("HAKOT_ROUTE", "Added current location point marker successfully!")
            } else {
                // 1. Royal Blue Route Base Line Layer (Slightly thicker: 7f)
                val lineLayer = LineLayer(layerId, sourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#1D4ED8")),
                        PropertyFactory.lineWidth(7f),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    )
                }
                style.addLayer(lineLayer)

                // 2. Animated Flowing Direction Stream Layer (5 frames, fewer dots reaching next dot)
                val dashArray = when (flowPhase) {
                    0 -> arrayOf(0.2f, 5.0f)
                    1 -> arrayOf(1.4f, 3.8f)
                    2 -> arrayOf(2.6f, 2.6f)
                    3 -> arrayOf(3.8f, 1.4f)
                    else -> arrayOf(5.0f, 0.2f)
                }

                val flowLayer = LineLayer(flowLayerId, sourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#60A5FA")),
                        PropertyFactory.lineWidth(4f),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineDasharray(dashArray)
                    )
                }
                style.addLayer(flowLayer)
                Log.d("HAKOT_ROUTE", "Added animated route overlay with ${lineFeatures.size} features successfully!")
            }
        } else {
            Log.w("HAKOT_ROUTE", "Route overlay skipped: no valid segment line features.")
        }
    } catch (e: Exception) {
        Log.e("HAKOT_ROUTE", "Error setting up MapLibre route overlay", e)
        e.printStackTrace()
    }
}

private fun setupMapLibreRoadNetworkOverlay(
    maplibreMap: MapLibreMap,
    roads: List<GisRoadSegmentFeature>,
    selectedIds: List<String>
) {
    val style = maplibreMap.style ?: return
    val roadsSourceId = "barangay-roads-source"
    val roadsLayerId = "barangay-roads-layer"
    val selectedSourceId = "barangay-selected-source"
    val selectedLayerId = "barangay-selected-layer"

    try {
        if (style.getLayer(selectedLayerId) != null) style.removeLayer(selectedLayerId)
        if (style.getSource(selectedSourceId) != null) style.removeSource(selectedSourceId)
        if (style.getLayer(roadsLayerId) != null) style.removeLayer(roadsLayerId)
        if (style.getSource(roadsSourceId) != null) style.removeSource(roadsSourceId)

        if (roads.isNotEmpty()) {
            val unselectedFeatures = roads.filter { !selectedIds.contains(it.id) }.map { road ->
                val line = LineString.fromLngLats(road.lineStringCoordinates.map { (lng, lat) -> Point.fromLngLat(lng, lat) })
                Feature.fromGeometry(line).apply {
                    addStringProperty("id", road.id)
                    addStringProperty("name", road.segmentName)
                }
            }
            if (unselectedFeatures.isNotEmpty()) {
                style.addSource(GeoJsonSource(roadsSourceId, FeatureCollection.fromFeatures(unselectedFeatures)))
                style.addLayer(LineLayer(roadsLayerId, roadsSourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#94A3B8")),
                        PropertyFactory.lineWidth(4f),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND)
                    )
                })
            }

            val selectedFeatures = roads.filter { selectedIds.contains(it.id) }.map { road ->
                val coords = if (road.clippedCoordinates.isNotEmpty()) road.clippedCoordinates else road.lineStringCoordinates
                val line = LineString.fromLngLats(coords.map { (lng, lat) -> Point.fromLngLat(lng, lat) })
                Feature.fromGeometry(line).apply {
                    addStringProperty("id", road.id)
                    addStringProperty("name", road.segmentName)
                }
            }
            if (selectedFeatures.isNotEmpty()) {
                style.addSource(GeoJsonSource(selectedSourceId, FeatureCollection.fromFeatures(selectedFeatures)))
                style.addLayer(LineLayer(selectedLayerId, selectedSourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#3B82F6")),
                        PropertyFactory.lineWidth(8f),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    )
                })

                try {
                    val boundsBuilder = LatLngBounds.Builder()
                    for (road in roads.filter { selectedIds.contains(it.id) }) {
                        val coords = if (road.clippedCoordinates.isNotEmpty()) road.clippedCoordinates else road.lineStringCoordinates
                        for (pt in coords) {
                            boundsBuilder.include(LatLng(pt.second, pt.first))
                        }
                    }
                    val bounds = boundsBuilder.build()
                    maplibreMap.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120), 1000)
                } catch (_: Exception) {}
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
