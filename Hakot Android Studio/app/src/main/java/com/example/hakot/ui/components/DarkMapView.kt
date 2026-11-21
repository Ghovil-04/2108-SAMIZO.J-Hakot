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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.example.hakot.data.GisManager
import com.example.hakot.data.HakotRepository
import com.example.hakot.data.GisRoadSegmentFeature
import com.example.hakot.data.OsrmRoutingHelper
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenPrimary
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
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.osmdroid.util.GeoPoint

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
    selectedRoadIds: List<String> = emptyList(),
    autoCenterSinglePoint: Boolean = false,
    showDefaultSchedulesWhenEmpty: Boolean = true,
    userLocationPoint: GeoPoint? = null,
    truckerLocations: List<GeoPoint> = emptyList(),
    truckerFocusPoint: GeoPoint? = null,
    truckerFocusTrigger: Int = 0,
    completedRouteSegments: List<List<GeoPoint>> = emptyList(),
    searchFocusPoint: GeoPoint? = null,
    searchFocusTrigger: Int = 0
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

    // Continuous traveling shimmer animation timer (30ms, 100 smooth steps, zero-lag performance)
    LaunchedEffect(isStyleLoaded, fetchedRoutePoints) {
        if (isStyleLoaded) {
            while (true) {
                delay(30)
                flowAnimPhase = (flowAnimPhase + 1) % 100
            }
        }
    }

    LaunchedEffect(HakotRepository.selectedMapDate, HakotRepository.selectedFocusSchedule, HakotRepository.schedules.size, customRoutePoints, customRouteSegments, barangayRoads, selectedRoadIds, showDefaultSchedulesWhenEmpty) {
        val focusSch = HakotRepository.selectedFocusSchedule
        val segmentsToUse = if (customRoutePoints.isNotEmpty() && customRouteSegments.isEmpty()) {
            emptyList()
        } else if (customRouteSegments.isNotEmpty()) {
            customRouteSegments
        } else if (showDefaultSchedulesWhenEmpty) {
            val todayDate = HakotRepository.selectedMapDate
            val todaySchedules = HakotRepository.schedules.filter { sch ->
                !sch.isDisabled && (sch.date == todayDate || HakotRepository.isScheduleActiveOnDate(sch, todayDate))
            }
            val allSegs = mutableListOf<List<GeoPoint>>()
            todaySchedules.forEach { sch ->
                val schSegs = mutableListOf<List<GeoPoint>>()
                if (sch.orderedSegments.isNotEmpty()) {
                    sch.orderedSegments.forEach { seg ->
                        val baseSeg = GisManager.roadSegments.find { it.id == seg.id } ?: seg
                        val rawCoords = when {
                            seg.clippedCoordinates.isNotEmpty() -> seg.clippedCoordinates
                            seg.lineStringCoordinates.isNotEmpty() -> seg.lineStringCoordinates
                            baseSeg.clippedCoordinates.isNotEmpty() -> baseSeg.clippedCoordinates
                            baseSeg.lineStringCoordinates.isNotEmpty() -> baseSeg.lineStringCoordinates
                            else -> emptyList()
                        }
                        val coords = if (seg.isReversed) rawCoords.reversed() else rawCoords
                        val gpList = coords.mapNotNull { (lng, lat) ->
                            if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                        }
                        if (gpList.size >= 2) schSegs.add(gpList)
                    }
                }
                if (schSegs.isEmpty()) {
                    val r = HakotRepository.routes.find { it.routeName.equals(sch.routeName, ignoreCase = true) }
                    val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                        if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                    } ?: emptyList()
                    if (rPts.size >= 2) {
                        schSegs.add(rPts)
                    } else {
                        val bgyRoads = GisManager.getRoadSegmentsInBarangay(sch.barangay)
                        bgyRoads.forEach { seg ->
                            val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                            val points = rawCoords.mapNotNull { (lng, lat) ->
                                if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                            }
                            if (points.size >= 2) schSegs.add(points)
                        }
                    }
                }
                allSegs.addAll(schSegs)
            }
            allSegs
        } else {
            emptyList()
        }
        fetchedRouteSegments = segmentsToUse
        val pointsToRender = if (customRoutePoints.isNotEmpty()) {
            customRoutePoints
        } else if (focusSch != null) {
            val focusPts = focusSch.orderedSegments.flatMap { seg ->
                val coords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                coords.mapNotNull { (lng, lat) ->
                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                }
            }
            if (focusPts.isNotEmpty()) focusPts else segmentsToUse.flatten()
        } else if (segmentsToUse.isNotEmpty()) {
            segmentsToUse.flatten()
        } else {
            emptyList()
        }
        fetchedRoutePoints = pointsToRender
    }

    // 1. Road Network Overlay (Runs ONLY when road data changes or flowPhase animates)
    LaunchedEffect(isStyleLoaded, barangayRoads, selectedRoadIds, flowAnimPhase) {
        if (isStyleLoaded) {
            mapLibreMapInstance?.let { maplibreMap ->
                setupMapLibreRoadNetworkOverlay(maplibreMap, barangayRoads, selectedRoadIds, flowAnimPhase)
            }
        }
    }

    // 2. Camera Bounds Easing (Runs when selectedFocusSchedule changes or initial route points load)
    LaunchedEffect(isStyleLoaded, HakotRepository.selectedFocusSchedule?.id) {
        if (isStyleLoaded) {
            mapLibreMapInstance?.let { maplibreMap ->
                try {
                    val focusSch = HakotRepository.selectedFocusSchedule
                    val targetPts = if (focusSch != null) {
                        val focusPts = focusSch.orderedSegments.flatMap { seg ->
                            val coords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                            coords.mapNotNull { (lng, lat) ->
                                if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                            }
                        }
                        if (focusPts.isNotEmpty()) focusPts else {
                            val r = HakotRepository.routes.find { it.routeName.equals(focusSch.routeName, ignoreCase = true) }
                            val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                                if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                            } ?: emptyList()
                            if (rPts.isNotEmpty()) rPts else {
                                GisManager.getRoadSegmentsInBarangay(focusSch.barangay).flatMap { seg ->
                                    val coords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                                    coords.mapNotNull { (lng, lat) ->
                                        if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                                    }
                                }
                            }
                        }
                    } else {
                        fetchedRoutePoints
                    }

                    val validPts = targetPts.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
                    if (validPts.size == 1) {
                        val pt = validPts[0]
                        maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pt.latitude, pt.longitude), 16.0), 500)
                    } else if (validPts.size >= 2) {
                        val bounds = LatLngBounds.Builder().apply {
                            validPts.forEach { pt ->
                                include(LatLng(pt.latitude, pt.longitude))
                            }
                        }.build()
                        maplibreMap.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80), 600)
                    }
                } catch (e: Exception) {
                    Log.e("HAKOT_ROUTE", "Error easing camera to route bounds", e)
                }
            }
        }
    }

    // 2b. Camera Easing to Specific Trucker GPS Location when Trucker Island is clicked
    LaunchedEffect(isStyleLoaded, truckerFocusPoint, truckerFocusTrigger) {
        if (isStyleLoaded && truckerFocusPoint != null && truckerFocusPoint.longitude.isFinite() && truckerFocusPoint.latitude.isFinite()) {
            mapLibreMapInstance?.let { maplibreMap ->
                maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(truckerFocusPoint.latitude, truckerFocusPoint.longitude), 16.0), 500)
            }
        }
    }

    // 2c. Camera Easing to Search Suggestion GPS Location
    LaunchedEffect(isStyleLoaded, searchFocusPoint, searchFocusTrigger) {
        if (isStyleLoaded && searchFocusPoint != null && searchFocusPoint.longitude.isFinite() && searchFocusPoint.latitude.isFinite()) {
            mapLibreMapInstance?.let { maplibreMap ->
                maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(searchFocusPoint.latitude, searchFocusPoint.longitude), 16.0), 600)
            }
        }
    }

    // 3. High-Performance Route Gradient Animation (Runs on animation ticks ONLY for fast property update)
    LaunchedEffect(isStyleLoaded, fetchedRouteSegments, fetchedRoutePoints, flowAnimPhase, HakotRepository.selectedFocusSchedule) {
        if (isStyleLoaded) {
            mapLibreMapInstance?.let { maplibreMap ->
                setupMapLibreRouteOverlay(maplibreMap, fetchedRouteSegments, fetchedRoutePoints, flowAnimPhase)
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
                            setupMapLibreCompletedRouteOverlay(maplibreMap, completedRouteSegments)
                            setupMapLibreUserLocationOverlay(maplibreMap, userLocationPoint)
                            setupMapLibreTruckerOverlay(maplibreMap, truckerLocations)
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
                        setupMapLibreCompletedRouteOverlay(maplibreMap, completedRouteSegments)
                        setupMapLibreUserLocationOverlay(maplibreMap, userLocationPoint)
                        setupMapLibreTruckerOverlay(maplibreMap, truckerLocations)
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

        // Floating Recenter Map Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                onClick = {
                    mapLibreMapInstance?.let { maplibreMap ->
                        if (userLocationPoint != null && userLocationPoint.longitude.isFinite() && userLocationPoint.latitude.isFinite()) {
                            maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLocationPoint.latitude, userLocationPoint.longitude), 15.0), 400)
                        } else if (customRoutePoints.size == 1) {
                            val pt = customRoutePoints[0]
                            maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pt.latitude, pt.longitude), 15.0), 400)
                        } else {
                            val validPts = fetchedRoutePoints.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
                            if (validPts.isNotEmpty()) {
                                if (validPts.size == 1) {
                                    val pt = validPts[0]
                                    maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pt.latitude, pt.longitude), 15.0), 400)
                                } else {
                                    val bounds = LatLngBounds.Builder().apply {
                                        validPts.forEach { include(LatLng(it.latitude, it.longitude)) }
                                    }.build()
                                    maplibreMap.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60), 400)
                                }
                            } else {
                                maplibreMap.easeCamera(CameraUpdateFactory.newLatLngZoom(nasugbuCenter, 15.0), 400)
                            }
                        }
                    }
                },
                shape = CircleShape,
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, DarkBorder),
                shadowElevation = 6.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
    val style = maplibreMap.style ?: return

    val sourceId = "hakot-route-source"
    val layerId = "hakot-route-layer"
    val flowLayerId = "hakot-route-flow-layer"

    val focusSourceId = "hakot-focus-route-source"
    val focusCasingLayerId = "hakot-focus-casing-layer"
    val focusLayerId = "hakot-focus-line-layer"

    try {
        val p1 = (flowPhase % 100) / 100.0
        val width = 0.10

        val numStops = 20
        val stopsList = ArrayList<Expression.Stop>(numStops + 1)
        for (i in 0..numStops) {
            val progress = i.toDouble() / numStops
            val minDist = Math.min(
                Math.abs(progress - p1),
                Math.min(Math.abs(progress - (p1 - 1.0)), Math.abs(progress - (p1 + 1.0)))
            )
            val colorHex = if (minDist < width) {
                val factor = 1.0 - (minDist / width)
                if (factor > 0.5) "#FBBF24" else "#FED7AA"
            } else {
                "#F28C28"
            }
            stopsList.add(Expression.stop(progress, Expression.color(android.graphics.Color.parseColor(colorHex))))
        }

        val gradientExpr = Expression.interpolate(
            Expression.linear(),
            Expression.lineProgress(),
            *stopsList.toTypedArray()
        )

        // 1. Build Base Routes Features (all routes for today)
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

        // 2. Build Focus Schedule Features for black border casing when a schedule card is clicked
        val focusSch = HakotRepository.selectedFocusSchedule
        val focusFeatures = mutableListOf<Feature>()
        if (focusSch != null) {
            val focusSegs = mutableListOf<List<GeoPoint>>()
            if (focusSch.orderedSegments.isNotEmpty()) {
                focusSch.orderedSegments.forEach { seg ->
                    val baseSeg = GisManager.roadSegments.find { it.id == seg.id } ?: seg
                    val rawCoords = when {
                        seg.clippedCoordinates.isNotEmpty() -> seg.clippedCoordinates
                        seg.lineStringCoordinates.isNotEmpty() -> seg.lineStringCoordinates
                        baseSeg.clippedCoordinates.isNotEmpty() -> baseSeg.clippedCoordinates
                        baseSeg.lineStringCoordinates.isNotEmpty() -> baseSeg.lineStringCoordinates
                        else -> emptyList()
                    }
                    val coords = if (seg.isReversed) rawCoords.reversed() else rawCoords
                    val pts = coords.mapNotNull { (lng, lat) ->
                        if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                    }
                    if (pts.size >= 2) focusSegs.add(pts)
                }
            }
            if (focusSegs.isEmpty()) {
                val r = HakotRepository.routes.find { it.routeName.equals(focusSch.routeName, ignoreCase = true) }
                val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                } ?: emptyList()
                if (rPts.size >= 2) {
                    focusSegs.add(rPts)
                } else {
                    val bgyRoads = GisManager.getRoadSegmentsInBarangay(focusSch.barangay)
                    bgyRoads.forEach { seg ->
                        val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                        val pts = rawCoords.mapNotNull { (lng, lat) ->
                            if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                        }
                        if (pts.size >= 2) focusSegs.add(pts)
                    }
                }
            }
            focusSegs.forEach { segPts ->
                val line = LineString.fromLngLats(segPts.map { Point.fromLngLat(it.longitude, it.latitude) })
                focusFeatures.add(Feature.fromGeometry(line))
            }
        }

        // Update Base Source & Layer
        val existingLayer = style.getLayer(layerId) as? LineLayer
        val existingSource = style.getSource(sourceId) as? GeoJsonSource

        if (existingLayer != null && existingSource != null) {
            existingSource.setGeoJson(FeatureCollection.fromFeatures(lineFeatures))
            existingLayer.setProperties(PropertyFactory.lineGradient(gradientExpr))
        } else {
            if (style.getLayer(flowLayerId) != null) style.removeLayer(flowLayerId)
            if (style.getLayer(layerId) != null) style.removeLayer(layerId)
            if (style.getSource(sourceId) != null) style.removeSource(sourceId)

            if (lineFeatures.isNotEmpty()) {
                val featureCollection = FeatureCollection.fromFeatures(lineFeatures)
                style.addSource(GeoJsonSource(sourceId, featureCollection, GeoJsonOptions().withLineMetrics(true)))

                if (isSinglePoint) {
                    val circleLayer = CircleLayer(layerId, sourceId).apply {
                        setProperties(
                            PropertyFactory.circleRadius(18f),
                            PropertyFactory.circleColor(android.graphics.Color.parseColor("#8B4513")), // Warm Brown Color
                            PropertyFactory.circleStrokeWidth(5f),
                            PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
                        )
                    }
                    style.addLayer(circleLayer)
                } else {
                    val lineLayer = LineLayer(layerId, sourceId).apply {
                        setProperties(
                            PropertyFactory.lineGradient(gradientExpr),
                            PropertyFactory.lineWidth(5.5f),
                            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                        )
                    }
                    style.addLayer(lineLayer)
                }
            }
        }

        // 3. Update or Add Focus Casing (Black Border) & Focus Highlight Layer
        var focusSource = style.getSource(focusSourceId) as? GeoJsonSource
        if (focusSource == null) {
            focusSource = GeoJsonSource(focusSourceId, FeatureCollection.fromFeatures(focusFeatures), GeoJsonOptions().withLineMetrics(true))
            style.addSource(focusSource)
        } else {
            focusSource.setGeoJson(FeatureCollection.fromFeatures(focusFeatures))
        }

        var focusCasing = style.getLayer(focusCasingLayerId) as? LineLayer
        if (focusCasing == null) {
            focusCasing = LineLayer(focusCasingLayerId, focusSourceId).apply {
                setProperties(
                    PropertyFactory.lineColor(android.graphics.Color.BLACK),
                    PropertyFactory.lineWidth(11f),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                )
            }
            style.addLayer(focusCasing)
        }

        var focusLayer = style.getLayer(focusLayerId) as? LineLayer
        if (focusLayer == null) {
            focusLayer = LineLayer(focusLayerId, focusSourceId).apply {
                setProperties(
                    PropertyFactory.lineGradient(gradientExpr),
                    PropertyFactory.lineWidth(6.5f),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                )
            }
            style.addLayer(focusLayer)
        } else {
            focusLayer.setProperties(PropertyFactory.lineGradient(gradientExpr))
        }
    } catch (e: Exception) {
        Log.e("HAKOT_ROUTE", "Error setting up MapLibre route overlay", e)
    }
}

private fun setupMapLibreRoadNetworkOverlay(
    maplibreMap: MapLibreMap,
    roads: List<GisRoadSegmentFeature>,
    selectedIds: List<String>,
    flowPhase: Int = 0
) {
    val style = maplibreMap.style ?: return
    val roadsSourceId = "barangay-roads-source"
    val roadsLayerId = "barangay-roads-layer"
    val selectedSourceId = "barangay-selected-source"
    val selectedLayerId = "barangay-selected-layer"

    try {
        val p1 = (flowPhase % 100) / 100.0
        val width = 0.10
        val numStops = 20
        val stopsList = ArrayList<Expression.Stop>(numStops + 1)
        for (i in 0..numStops) {
            val progress = i.toDouble() / numStops
            val minDist = Math.min(
                Math.abs(progress - p1),
                Math.min(Math.abs(progress - (p1 - 1.0)), Math.abs(progress - (p1 + 1.0)))
            )
            val colorHex = if (minDist < width) {
                val factor = 1.0 - (minDist / width)
                if (factor > 0.5) "#FBBF24" else "#FED7AA"
            } else {
                "#F28C28"
            }
            stopsList.add(Expression.stop(progress, Expression.color(android.graphics.Color.parseColor(colorHex))))
        }
        val gradientExpr = Expression.interpolate(
            Expression.linear(),
            Expression.lineProgress(),
            *stopsList.toTypedArray()
        )

        val unselectedFeatures = roads.filter { !selectedIds.contains(it.id) }.map { road ->
            val line = LineString.fromLngLats(road.lineStringCoordinates.map { (lng, lat) -> Point.fromLngLat(lng, lat) })
            Feature.fromGeometry(line).apply {
                addStringProperty("id", road.id)
                addStringProperty("name", road.segmentName)
            }
        }

        var roadsSource = style.getSource(roadsSourceId) as? GeoJsonSource
        if (roadsSource == null) {
            style.addSource(GeoJsonSource(roadsSourceId, FeatureCollection.fromFeatures(unselectedFeatures)))
            style.addLayer(LineLayer(roadsLayerId, roadsSourceId).apply {
                setProperties(
                    PropertyFactory.lineColor(android.graphics.Color.parseColor("#94A3B8")),
                    PropertyFactory.lineWidth(4f),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND)
                )
            })
        } else {
            roadsSource.setGeoJson(FeatureCollection.fromFeatures(unselectedFeatures))
        }

        val selectedFeatures = roads.filter { selectedIds.contains(it.id) }.map { road ->
            val coords = if (road.clippedCoordinates.isNotEmpty()) road.clippedCoordinates else road.lineStringCoordinates
            val line = LineString.fromLngLats(coords.map { (lng, lat) -> Point.fromLngLat(lng, lat) })
            Feature.fromGeometry(line).apply {
                addStringProperty("id", road.id)
                addStringProperty("name", road.segmentName)
            }
        }

        var selSource = style.getSource(selectedSourceId) as? GeoJsonSource
        if (selSource == null) {
            style.addSource(GeoJsonSource(selectedSourceId, FeatureCollection.fromFeatures(selectedFeatures), GeoJsonOptions().withLineMetrics(true)))
            style.addLayer(LineLayer(selectedLayerId, selectedSourceId).apply {
                setProperties(
                    PropertyFactory.lineGradient(gradientExpr),
                    PropertyFactory.lineWidth(8f),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                )
            })
        } else {
            selSource.setGeoJson(FeatureCollection.fromFeatures(selectedFeatures))
            val selLayer = style.getLayer(selectedLayerId) as? LineLayer
            selLayer?.setProperties(PropertyFactory.lineGradient(gradientExpr))
        }

        if (selectedFeatures.isNotEmpty()) {
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
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun setupMapLibreUserLocationOverlay(
    maplibreMap: MapLibreMap,
    userLoc: GeoPoint?
) {
    val style = maplibreMap.style ?: return
    val userSourceId = "hakot-user-location-source"
    val userLayerId = "hakot-user-location-layer"

    try {
        if (style.getLayer(userLayerId) != null) style.removeLayer(userLayerId)
        if (style.getSource(userSourceId) != null) style.removeSource(userSourceId)

        if (userLoc != null && userLoc.longitude.isFinite() && userLoc.latitude.isFinite()) {
            val point = Point.fromLngLat(userLoc.longitude, userLoc.latitude)
            val feature = Feature.fromGeometry(point)
            style.addSource(GeoJsonSource(userSourceId, FeatureCollection.fromFeatures(listOf(feature))))
            style.addLayer(
                CircleLayer(userLayerId, userSourceId).apply {
                    setProperties(
                        PropertyFactory.circleRadius(18f),
                        PropertyFactory.circleColor(android.graphics.Color.parseColor("#8B4513")), // Warm Brown Color
                        PropertyFactory.circleStrokeWidth(5f),
                        PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
                    )
                }
            )
        }
    } catch (e: Exception) {
        Log.e("HAKOT_ROUTE", "Error setting up user location overlay", e)
    }
}

private fun setupMapLibreTruckerOverlay(
    maplibreMap: MapLibreMap,
    truckerPts: List<GeoPoint>
) {
    val style = maplibreMap.style ?: return
    val truckerSourceId = "hakot-trucker-gps-source"
    val truckerLayerId = "hakot-trucker-gps-layer"

    try {
        if (style.getLayer(truckerLayerId) != null) style.removeLayer(truckerLayerId)
        if (style.getSource(truckerSourceId) != null) style.removeSource(truckerSourceId)

        val features = truckerPts.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }.map { pt ->
            Feature.fromGeometry(Point.fromLngLat(pt.longitude, pt.latitude))
        }

        if (features.isNotEmpty()) {
            style.addSource(GeoJsonSource(truckerSourceId, FeatureCollection.fromFeatures(features)))
            style.addLayer(
                CircleLayer(truckerLayerId, truckerSourceId).apply {
                    setProperties(
                        PropertyFactory.circleRadius(16f),
                        PropertyFactory.circleColor(android.graphics.Color.parseColor("#22C55E")), // Vibrant Emerald Green Marker for Active Truckers
                        PropertyFactory.circleStrokeWidth(4f),
                        PropertyFactory.circleStrokeColor(android.graphics.Color.WHITE)
                    )
                }
            )
        }
    } catch (e: Exception) {
        Log.e("HAKOT_ROUTE", "Error setting up trucker GPS overlay", e)
    }
}

private fun setupMapLibreCompletedRouteOverlay(
    maplibreMap: MapLibreMap,
    segments: List<List<GeoPoint>>
) {
    val style = maplibreMap.style ?: return
    val sourceId = "hakot-completed-route-source"
    val layerId = "hakot-completed-route-layer"

    try {
        if (style.getLayer(layerId) != null) style.removeLayer(layerId)
        if (style.getSource(sourceId) != null) style.removeSource(sourceId)

        val lineFeatures = mutableListOf<Feature>()
        segments.forEach { segPts ->
            val valid = segPts.filter { it.longitude.isFinite() && it.latitude.isFinite() && it.longitude in -180.0..180.0 && it.latitude in -90.0..90.0 }
            if (valid.size >= 2) {
                val line = LineString.fromLngLats(valid.map { Point.fromLngLat(it.longitude, it.latitude) })
                lineFeatures.add(Feature.fromGeometry(line))
            }
        }

        if (lineFeatures.isNotEmpty()) {
            style.addSource(GeoJsonSource(sourceId, FeatureCollection.fromFeatures(lineFeatures)))
            style.addLayer(
                LineLayer(layerId, sourceId).apply {
                    setProperties(
                        PropertyFactory.lineColor(android.graphics.Color.parseColor("#22C55E")), // Solid Green
                        PropertyFactory.lineWidth(4.5f), // Clean solid line matching user drawing
                        PropertyFactory.lineOpacity(1.0f), // Fully solid
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    )
                }
            )
        }
    } catch (e: Exception) {
        Log.e("HAKOT_ROUTE", "Error setting up completed route overlay", e)
    }
}
