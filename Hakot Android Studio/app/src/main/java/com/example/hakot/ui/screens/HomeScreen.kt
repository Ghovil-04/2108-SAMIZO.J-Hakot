package com.example.hakot.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.ReportType
import com.example.hakot.model.ScheduleItem
import com.example.hakot.model.WasteCategory
import com.example.hakot.ui.components.DarkMapView
import com.example.hakot.ui.components.TopSearchBar
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.DarkSurfaceVariant
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.HazardousRedContainer
import com.example.hakot.ui.theme.RecycleYellow
import com.example.hakot.ui.theme.RecycleYellowContainer
import com.example.hakot.ui.theme.ResidualCyan
import com.example.hakot.ui.theme.ResidualCyanContainer
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    onMenuClick: () -> Unit = {},
    isAdminMode: Boolean = false,
    initialShowReportDialog: Boolean = false,
    onReportDialogDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            Log.d("HAKOT_HOME", "Home screen opened - Fetching all launch data from Supabase...")
            HakotRepository.restoreSession(context)
            HakotRepository.fetchSchedulesFromSupabase(context)
            HakotRepository.fetchTrucksFromSupabase(context)
            HakotRepository.fetchDumpsitesFromSupabase(context)
            HakotRepository.fetchAnonymousReportsFromSupabase()
            Log.d("HAKOT_HOME", "Fetch all launch data on home succeeded. Schedules count=${HakotRepository.schedules.size}")
        } catch (e: Exception) {
            Log.e("HAKOT_HOME", "Error fetching launch data on home screen", e)
        }
    }

    var showReportDialog by remember(initialShowReportDialog) { mutableStateOf(initialShowReportDialog) }
    var selectedReportType by remember { mutableStateOf(ReportType.MISSED_COLLECTION) }
    var isMapInteracting by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Full-Width Top Header Box covering the entire top
            TopSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onMenuClick = onMenuClick,
                isAdminMode = isAdminMode
            )

            // Map Container in standing rectangle format enclosed by a thick edge border and side margins
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(4.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                DarkMapView(
                    searchQuery = searchQuery,
                    onReportClick = { showReportDialog = true },
                    onMapInteractionStateChange = { interacting ->
                        isMapInteracting = interacting
                    }
                )
            }

            // Fixed Non-Draggable Bottom Box for Today's Schedules (Box shape, no round top edges)
            Surface(
                color = DarkSurface,
                shape = RectangleShape,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header: Today's Schedules
                    val todayDate = LocalDate.now()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Schedules",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )
                        Text(
                            text = todayDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EcoGreenPrimary
                        )
                    }

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
                            CollectionStatusCardItem(
                                schedule = displaySchedules.firstOrNull(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> { // 2 or more schedules: swipable horizontal list (LazyRow)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(displaySchedules) { sch ->
                                    CollectionStatusCardItem(
                                        schedule = sch,
                                        modifier = Modifier.width(300.dp)
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
                onDismiss = {
                    showReportDialog = false
                    onReportDialogDismiss()
                },
                onSubmitSuccess = { refCode ->
                    showReportDialog = false
                    onReportDialogDismiss()
                }
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
                    text = "Report Waste Issue Anonymously",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(EcoGreenContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("100% Anonymous", fontSize = 10.sp, color = EcoGreenLight)
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

            Box(modifier = Modifier.fillMaxWidth()) {
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

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(DarkSurfaceCard)
                ) {
                    ReportType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = type.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (type == selectedType) HazardousRed else TextOnDarkPrimary
                                    )
                                    Text(
                                        text = type.description,
                                        fontSize = 10.sp,
                                        color = TextOnDarkSecondary
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = type.title,
                                    tint = if (type == selectedType) HazardousRed else TextOnDarkMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                selectedType = type
                                isCategoryDropdownExpanded = false
                            }
                        )
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
    onClick: () -> Unit = {}
) {
    val truckName = schedule?.truckAssigned?.takeIf { it.isNotBlank() } ?: "Truck #01"
    val assignedArea = schedule?.barangay?.takeIf { it.isNotBlank() } ?: "Wawa → Bucana"
    val scheduleTime = if (schedule != null) "${schedule.startTime} – ${schedule.endTime}" else "08:00 – 11:30 AM"

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Collection status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collection status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary
                )
                if (schedule != null && schedule.routeName.isNotBlank()) {
                    Text(
                        text = schedule.routeName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EcoGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Truck Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_truck),
                    contentDescription = "Truck",
                    tint = EcoGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = truckName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Assigned Area Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned area",
                    fontSize = 13.sp,
                    color = TextOnDarkSecondary
                )
                Text(
                    text = assignedArea,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnDarkPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Schedule Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    fontSize = 13.sp,
                    color = TextOnDarkSecondary
                )
                Text(
                    text = scheduleTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextOnDarkPrimary
                )
            }
        }
    }
}

@Composable
fun EmptyCollectionStatusCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_truck),
                contentDescription = "No Schedule",
                tint = TextOnDarkMuted,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Collection Schedule Today",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Check the Schedules tab for upcoming municipal waste collection routes.",
                fontSize = 12.sp,
                color = TextOnDarkSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
