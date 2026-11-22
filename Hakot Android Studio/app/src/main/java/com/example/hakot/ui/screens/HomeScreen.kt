package com.example.hakot.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log
import com.example.hakot.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.hakot.data.GisManager
import com.example.hakot.data.HakotRepository
import com.example.hakot.data.HakotRepository.MunicipalityItem
import com.example.hakot.model.ReportType
import com.example.hakot.model.ScheduleItem
import com.example.hakot.model.WasteCategory
import com.example.hakot.ui.components.DarkMapView
import com.example.hakot.ui.components.TopSearchBar
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.DarkSurfaceVariant
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.SafetyOrange
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.HazardousRedContainer
import com.example.hakot.ui.theme.RecycleYellow
import com.example.hakot.ui.theme.RecycleYellowContainer
import com.example.hakot.ui.theme.ResidualCyan
import com.example.hakot.ui.theme.ResidualCyanContainer
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    isAdminMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userLat by remember { mutableStateOf(14.0700) }
    var userLon by remember { mutableStateOf(120.6300) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    userLat = loc.latitude
                    userLon = loc.longitude
                }
            } catch (_: SecurityException) {}
        }
    }

    LaunchedEffect(Unit) {
        try {
            HakotRepository.selectedFocusSchedule = null
            HakotRepository.selectedMapDate = LocalDate.now()
            Log.d("HAKOT_HOME", "Home screen opened - Fetching all launch data from Supabase...")
            HakotRepository.restoreSession(context)
            HakotRepository.fetchSchedulesFromSupabase(context)
            HakotRepository.fetchTrucksFromSupabase(context)
            HakotRepository.fetchTruckerAccountsFromSupabase(context)
            HakotRepository.fetchDumpsitesFromSupabase(context)
            HakotRepository.fetchAnonymousReportsFromSupabase()
            HakotRepository.fetchBarangaysAndStreetsForMunicipality(HakotRepository.activeOrganization.municipalityId, context)

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    userLat = loc.latitude
                    userLon = loc.longitude
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            Log.d("HAKOT_HOME", "Fetch all launch data on home succeeded. Schedules count=${HakotRepository.schedules.size}")

            val globalNotif = HakotRepository.getGlobalNotificationsEnabled(context)
            val enabledList = HakotRepository.schedules.filter { HakotRepository.isScheduleNotificationEnabled(context, it.id) }
            if (globalNotif && enabledList.isNotEmpty()) {
                HakotRepository.showSystemNotification(
                    context,
                    "🔔 Hakot Active Notifications",
                    "Monitoring ${enabledList.size} schedule(s) for collection."
                )
            }
        } catch (e: Exception) {
            Log.e("HAKOT_HOME", "Error fetching launch data on home screen", e)
        }
    }

    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReportType by remember { mutableStateOf(ReportType.MISSED_COLLECTION) }
    var isMapInteracting by remember { mutableStateOf(false) }

    val todayDate = LocalDate.now()
    val todaySchedules = remember(HakotRepository.schedules.size, todayDate) {
        HakotRepository.schedules.filter { sch ->
            !sch.isDisabled && (sch.date == todayDate || HakotRepository.isScheduleActiveOnDate(sch, todayDate))
        }
    }
    val activeRouteSegments = remember(todaySchedules) {
        val list = mutableListOf<List<GeoPoint>>()
        todaySchedules.filter { it.status != "COMPLETED" }.forEach { sch ->
            val segsList = mutableListOf<List<GeoPoint>>()
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
                    val points = coords.mapNotNull { (lng, lat) ->
                        if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                    }
                    if (points.size >= 2) segsList.add(points)
                }
            }
            if (segsList.isEmpty()) {
                val r = HakotRepository.routes.find { it.routeName.equals(sch.routeName, ignoreCase = true) }
                val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                } ?: emptyList()
                if (rPts.size >= 2) {
                    segsList.add(rPts)
                } else {
                    val bgyRoads = GisManager.getRoadSegmentsInBarangay(sch.barangay)
                    bgyRoads.forEach { seg ->
                        val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                        val points = rawCoords.mapNotNull { (lng, lat) ->
                            if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                        }
                        if (points.size >= 2) segsList.add(points)
                    }
                }
            }
            list.addAll(segsList)
        }
        list
    }

    val completedRouteSegments = remember(todaySchedules) {
        val list = mutableListOf<List<GeoPoint>>()
        todaySchedules.filter { it.status == "COMPLETED" }.forEach { sch ->
            val segsList = mutableListOf<List<GeoPoint>>()
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
                    val points = coords.mapNotNull { (lng, lat) ->
                        if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                    }
                    if (points.size >= 2) segsList.add(points)
                }
            }
            if (segsList.isEmpty()) {
                val r = HakotRepository.routes.find { it.routeName.equals(sch.routeName, ignoreCase = true) }
                val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                } ?: emptyList()
                if (rPts.size >= 2) {
                    segsList.add(rPts)
                } else {
                    val bgyRoads = GisManager.getRoadSegmentsInBarangay(sch.barangay)
                    bgyRoads.forEach { seg ->
                        val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                        val points = rawCoords.mapNotNull { (lng, lat) ->
                            if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                        }
                        if (points.size >= 2) segsList.add(points)
                    }
                }
            }
            list.addAll(segsList)
        }
        list
    }

    val activeTruckerLocations = remember(HakotRepository.truckerAccounts.size, HakotRepository.activeOrganization.municipalityId) {
        HakotRepository.truckerAccounts.filter { trucker ->
            trucker.municipalityId == HakotRepository.activeOrganization.municipalityId &&
            trucker.isSharingLocation &&
            trucker.latitude != 0.0 && trucker.longitude != 0.0 &&
            trucker.latitude.isFinite() && trucker.longitude.isFinite() &&
            trucker.latitude in -90.0..90.0 && trucker.longitude in -180.0..180.0
        }.map { GeoPoint(it.latitude, it.longitude) }
    }

    var focusedTruckerPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var truckerFocusTrigger by remember { mutableStateOf(0) }
    var focusedSearchPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var searchFocusTrigger by remember { mutableStateOf(0) }

    fun focusTruckerGps(sch: ScheduleItem?) {
        if (sch != null) {
            val assignedDriverName = sch.truckAssigned.substringAfter("Driver:").trim()
            val matchedTrucker = HakotRepository.truckerAccounts.find { trucker ->
                (assignedDriverName.isNotBlank() && trucker.fullName.contains(assignedDriverName, ignoreCase = true)) ||
                sch.truckAssigned.contains(trucker.fullName, ignoreCase = true) ||
                (trucker.username.isNotBlank() && sch.truckAssigned.contains(trucker.username, ignoreCase = true))
            }
            val matchedTruck = HakotRepository.trucks.find { truck ->
                sch.truckAssigned.contains(truck.plateNumber, ignoreCase = true) ||
                (truck.driverName.isNotBlank() && sch.truckAssigned.contains(truck.driverName, ignoreCase = true))
            }
            val isSharing = matchedTrucker?.isSharingLocation == true || matchedTruck?.isOnline == true
            val lat = matchedTrucker?.latitude.takeIf { it != null && it != 0.0 }
                ?: matchedTruck?.latitude.takeIf { it != null && it != 0.0 } ?: 0.0
            val lon = matchedTrucker?.longitude.takeIf { it != null && it != 0.0 }
                ?: matchedTruck?.longitude.takeIf { it != null && it != 0.0 } ?: 0.0

            if (isSharing && lat != 0.0 && lon != 0.0) {
                focusedTruckerPoint = GeoPoint(lat, lon)
                truckerFocusTrigger++
            } else {
                Toast.makeText(context, "Trucker GPS is offline", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Trucker GPS is offline", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Full-Width Top Header Box covering the entire top
            TopSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick,
                isAdminMode = isAdminMode,
                onSelectSuggestion = { _, lat, lon ->
                    focusedSearchPoint = GeoPoint(lat, lon)
                    searchFocusTrigger++
                    onQueryChange("")
                }
            )

            // Map Container in standing rectangle format with reduced height and consistent 14.dp rounded corners
            Box(
                modifier = Modifier
                    .weight(0.92f) // Slightly reduced height (~8%)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp)) // Consistent 14.dp rounded corners matching content cards
                    .border(4.dp, DarkBorder, RoundedCornerShape(14.dp))
            ) {
                DarkMapView(
                    searchQuery = searchQuery,
                    customRoutePoints = activeRouteSegments.flatten(),
                    customRouteSegments = activeRouteSegments,
                    completedRouteSegments = completedRouteSegments,
                    autoCenterSinglePoint = true,
                    userLocationPoint = GeoPoint(userLat, userLon),
                    truckerLocations = activeTruckerLocations,
                    truckerFocusPoint = focusedTruckerPoint,
                    truckerFocusTrigger = truckerFocusTrigger,
                    searchFocusPoint = focusedSearchPoint,
                    searchFocusTrigger = searchFocusTrigger,
                    onReportClick = { showReportDialog = true },
                    onMapInteractionStateChange = { interacting ->
                        isMapInteracting = interacting
                    }
                )
            }

            // Spacing between Map and Schedule section
            Spacer(modifier = Modifier.height(10.dp))

            // Fixed Non-Draggable Bottom Box for Today's Schedules (Consistent margins & spacing)
            Surface(
                color = DarkSurface,
                shape = RectangleShape,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Header: Today's Schedules
                    val todayDate = LocalDate.now()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Schedules",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )
                        Text(
                            text = todayDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                            fontSize = 10.sp, // Reduced font size of date slightly
                            fontWeight = FontWeight.SemiBold,
                            color = EcoGreenPrimary
                        )
                    }

                    // Add more spacing between heading and collection card
                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter strictly for today's schedules from database public.schedules (no fallback to other dates)
                    val todaySchedules = HakotRepository.schedules.filter { sch ->
                        HakotRepository.isScheduleActiveOnDate(sch, todayDate)
                    }.sortedBy { it.startTime }

                    val displaySchedules: List<ScheduleItem> = todaySchedules

                    when {
                        displaySchedules.isEmpty() -> {
                            EmptyCollectionStatusCard(modifier = Modifier.fillMaxWidth())
                        }
                        displaySchedules.size == 1 -> {
                            val sch = displaySchedules.firstOrNull()
                            CollectionStatusCardItem(
                                schedule = sch,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                                onTruckIslandClick = { focusTruckerGps(sch) }
                            )
                        }
                        else -> { // 2 or more schedules: swipable horizontal list (LazyRow)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(end = 12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(displaySchedules) { sch ->
                                    CollectionStatusCardItem(
                                        schedule = sch,
                                        modifier = Modifier.width(260.dp),
                                        onClick = {},
                                        onTruckIslandClick = { focusTruckerGps(sch) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Anonymous Report Sheet / Dialog Modal
        if (showReportDialog) {
            AnonymousReportModal(
                initialType = selectedReportType,
                onDismiss = { showReportDialog = false },
                onSubmitSuccess = { showReportDialog = false }
            )
        }
    }
}

@Composable
fun QuickReportChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = HazardousRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextOnDarkPrimary
            )
        }
    }
}

@Composable
fun WasteCategoryGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WasteCategoryItem(
            name = "General Waste (All Trash Accepted)",
            desc = "All household waste, food scraps, plastics, paper & general garbage collected together",
            color = EcoGreenPrimary,
            bgColor = EcoGreenContainer
        )
        WasteCategoryItem(
            name = "Recyclables & Dry Trash",
            desc = "Plastics, cardboard, bottles & dry packaging accepted during regular pass",
            color = RecycleYellow,
            bgColor = RecycleYellowContainer
        )
        WasteCategoryItem(
            name = "Bulky & Large Household Trash",
            desc = "Furniture, worn items & large household waste accepted on scheduled routes",
            color = ResidualCyan,
            bgColor = ResidualCyanContainer
        )
        WasteCategoryItem(
            name = "Hazardous & E-Waste",
            desc = "Batteries, electronics, bulbs & chemical containers",
            color = HazardousRed,
            bgColor = HazardousRedContainer
        )
    }
}

@Composable
fun WasteCategoryItem(
    name: String,
    desc: String,
    color: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor.copy(alpha = 0.4f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = TextOnDarkSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnonymousReportModal(
    initialType: ReportType,
    onDismiss: () -> Unit,
    onSubmitSuccess: (refCode: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(initialType) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var locationInput by remember { mutableStateOf("J.P. Laurel Street, Nasugbu") }
    var descriptionInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Report Waste Issue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EcoGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("100% Anonymous", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                }
            }

            Text(
                text = "No personal details or profile required. Your report alerts the sanitation team.",
                fontSize = 12.sp,
                color = TextOnDarkSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Compact Dropdown Category Selector
            Text("Issue Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { if (!isSubmitting) isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
                    color = HazardousRedContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HazardousRed, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = selectedType.title,
                                tint = HazardousRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = selectedType.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDarkPrimary
                                )
                                Text(
                                    text = selectedType.description,
                                    fontSize = 10.sp,
                                    color = TextOnDarkSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isCategoryDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown Arrow",
                            tint = TextOnDarkSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isCategoryDropdownExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = DarkSurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                ReportType.entries.forEachIndexed { index, type ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedType = type
                                                isCategoryDropdownExpanded = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ReportProblem,
                                            contentDescription = type.title,
                                            tint = if (type == selectedType) HazardousRed else TextOnDarkMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = type.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (type == selectedType) HazardousRed else TextOnDarkPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = type.description,
                                                fontSize = 10.sp,
                                                color = TextOnDarkSecondary
                                            )
                                        }
                                    }
                                    if (index < ReportType.entries.size - 1) {
                                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Input Field
            Text("Location / Street / Barangay:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
            OutlinedTextField(
                value = locationInput,
                onValueChange = {
                    locationInput = it
                    errorMessage = null
                },
                singleLine = true,
                enabled = !isSubmitting,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoGreenPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextOnDarkPrimary,
                    unfocusedTextColor = TextOnDarkPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Description Multiline Input Field
            Text("Details / Notes (Optional):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = {
                    descriptionInput = it
                    errorMessage = null
                },
                minLines = 3,
                maxLines = 5,
                enabled = !isSubmitting,
                placeholder = { Text("Describe the waste collection issue or concern in detail...", color = TextOnDarkMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoGreenPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextOnDarkPrimary,
                    unfocusedTextColor = TextOnDarkPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(err, color = HazardousRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    if (locationInput.isBlank()) {
                        errorMessage = "Please specify a location or street name."
                        return@Button
                    }
                    scope.launch {
                        isSubmitting = true
                        errorMessage = null
                        val result = HakotRepository.addAnonymousReport(
                            type = selectedType,
                            locationName = locationInput.trim(),
                            description = descriptionInput.trim()
                        )
                        isSubmitting = false
                        if (result.first) {
                            Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            onSubmitSuccess(result.second)
                        } else {
                            errorMessage = result.second
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Submitting to Supabase...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🚩 SUBMIT ANONYMOUS REPORT",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun CollectionStatusCardItem(
    schedule: ScheduleItem?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTruckIslandClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val rawTruckAssigned = schedule?.truckAssigned?.takeIf { it.isNotBlank() } ?: "No assigned truck yet"
    val platePart = rawTruckAssigned.substringBefore("(").substringBefore(" - Driver:").trim()
    val driverPart = rawTruckAssigned.substringAfter("Driver:").trim().takeIf { it.isNotBlank() }
        ?: "No driver"
    val truckName = if (rawTruckAssigned.contains("Driver:")) {
        "$platePart - $driverPart"
    } else {
        rawTruckAssigned
    }
    val assignedArea = schedule?.barangay?.takeIf { it.isNotBlank() } ?: "Wawa → Bucana"
    val scheduleTime = if (schedule != null) "${schedule.startTime} – ${schedule.endTime}" else "08:00 – 11:30 AM"
    val cardStatus = if (schedule != null) HakotRepository.getDisplayStatus(schedule.status) else "UPCOMING"

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier.clickable {
            if (schedule != null) {
                HakotRepository.selectedMapDate = schedule.date
                if (HakotRepository.selectedFocusSchedule?.id == schedule.id) {
                    HakotRepository.selectedFocusSchedule = null
                } else {
                    HakotRepository.selectedFocusSchedule = schedule
                }
            }
            onClick()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Status of the schedule & Notification Bell Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cardStatus,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafetyOrange
                )

                if (schedule != null) {
                    var isNotifOn by remember(schedule.id) {
                        mutableStateOf(HakotRepository.isScheduleNotificationEnabled(context, schedule.id))
                    }
                    IconButton(
                        onClick = {
                            val newState = HakotRepository.toggleScheduleNotification(context, schedule.id)
                            isNotifOn = newState
                            val msg = if (newState) "Notification enabled for ${schedule.routeName}" else "Notification disabled for ${schedule.routeName}"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Toggle Notification",
                            tint = if (isNotifOn) SafetyOrange else Color(0xFF94A3B8), // Pale white / muted gray-white
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visually Distinct Truck Assignment Status Box
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (onTruckIslandClick != null) {
                            onTruckIslandClick()
                        } else {
                            val assignedDriverName = schedule?.truckAssigned?.substringAfter("Driver:")?.trim() ?: ""
                            val matchedTrucker = HakotRepository.truckerAccounts.find { trucker ->
                                (assignedDriverName.isNotBlank() && trucker.fullName.contains(assignedDriverName, ignoreCase = true)) ||
                                (schedule?.truckAssigned?.contains(trucker.fullName, ignoreCase = true) == true) ||
                                (trucker.username.isNotBlank() && schedule?.truckAssigned?.contains(trucker.username, ignoreCase = true) == true)
                            }
                            val matchedTruck = HakotRepository.trucks.find { truck ->
                                schedule?.truckAssigned?.contains(truck.plateNumber, ignoreCase = true) == true ||
                                (truck.driverName.isNotBlank() && schedule?.truckAssigned?.contains(truck.driverName, ignoreCase = true) == true)
                            }
                            val lat = matchedTrucker?.latitude.takeIf { it != null && it != 0.0 }
                                ?: matchedTruck?.latitude.takeIf { it != null && it != 0.0 } ?: 0.0
                            val lon = matchedTrucker?.longitude.takeIf { it != null && it != 0.0 }
                                ?: matchedTruck?.longitude.takeIf { it != null && it != 0.0 } ?: 0.0

                            if (lat == 0.0 || lon == 0.0) {
                                Toast.makeText(context, "Trucker GPS is offline", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_truck),
                        contentDescription = "Truck",
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = truckName,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(6.dp))

            // Assigned Area Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned area",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary
                )
                Text(
                    text = assignedArea,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Schedule Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary
                )
                Text(
                    text = scheduleTime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionStatusCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_truck),
                contentDescription = "No Schedule",
                tint = TextOnDarkMuted,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Collection Schedule Today",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Check the Schedules tab for upcoming municipal waste collection routes.",
                fontSize = 11.sp,
                color = TextOnDarkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MunicipalitySelectionModal(
    onDismiss: () -> Unit,
    onMunicipalitySelected: (id: String, name: String, lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<MunicipalityItem?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingItem by remember { mutableStateOf<MunicipalityItem?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        HakotRepository.fetchMunicipalitiesFromSupabase(context)
        isLoading = false
    }

    val filteredList = remember(searchQuery, HakotRepository.municipalities.size) {
        if (searchQuery.isBlank()) HakotRepository.municipalities
        else HakotRepository.municipalities.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.province.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showConfirmDialog && pendingItem != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Confirm Municipality Change", color = EcoGreenLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text("Are you sure you want to change your active municipality to ${pendingItem?.name}?", color = TextOnDarkSecondary, fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingItem?.let {
                            onMunicipalitySelected(it.id, it.name, it.centerLat, it.centerLng)
                        }
                        showConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextOnDarkSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceCard,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Your Municipality",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextOnDarkSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose your municipality to access the correct waste collection schedules, announcements, and local reports.",
                    fontSize = 11.5.sp,
                    color = TextOnDarkSecondary
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your municipality...", fontSize = 12.sp, color = TextOnDarkMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EcoGreenPrimary, modifier = Modifier.size(28.dp))
                    }
                } else if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No municipality found matching \"$searchQuery\".",
                            fontSize = 12.sp,
                            color = TextOnDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredList) { item ->
                            val isSelected = selectedItem?.id == item.id || (selectedItem == null && HakotRepository.activeOrganization.municipalityId == item.id)
                            Surface(
                                color = if (isSelected) EcoGreenContainer else DarkSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) EcoGreenPrimary else DarkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedItem = item }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) EcoGreenLight else TextOnDarkPrimary
                                        )
                                        Text(
                                            text = "${item.province}, ${item.region}",
                                            fontSize = 10.sp,
                                            color = TextOnDarkSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Text("Selected ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val itemToSave = selectedItem ?: filteredList.firstOrNull()
                    if (itemToSave != null) {
                        pendingItem = itemToSave
                        showConfirmDialog = true
                    }
                },
                enabled = selectedItem != null || filteredList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {}
    )
}
