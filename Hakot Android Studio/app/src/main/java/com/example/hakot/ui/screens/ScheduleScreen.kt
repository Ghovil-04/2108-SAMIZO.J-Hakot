package com.example.hakot.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hakot.R
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.ScheduleItem
import com.example.hakot.ui.components.DarkMapView
import org.osmdroid.util.GeoPoint
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.DarkSurfaceVariant
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.HazardousRedContainer
import com.example.hakot.ui.theme.SafetyOrange
import com.example.hakot.ui.theme.RecycleYellow
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleScreen(
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nowLocalDate = remember { LocalDate.now(ZoneId.of("Asia/Manila")) }
    val nowLocalTime = remember { LocalTime.now(ZoneId.of("Asia/Manila")) }

    var selectedDate by remember { mutableStateOf(nowLocalDate) }
    var isMonthlyView by remember { mutableStateOf(false) }
    var selectedScheduleDetail by remember { mutableStateOf<ScheduleItem?>(null) }

    LaunchedEffect(Unit) {
        HakotRepository.fetchSchedulesFromSupabase(context)
    }

    // Update map selected date when schedule date changes
    LaunchedEffect(selectedDate) {
        HakotRepository.selectedMapDate = selectedDate
    }

    val activeCurrentSchedule = if (selectedDate == nowLocalDate) {
        HakotRepository.schedules.find { sch ->
            HakotRepository.isScheduleActiveOnDate(sch, selectedDate) && !nowLocalTime.isBefore(sch.startTime) && !nowLocalTime.isAfter(sch.endTime)
        }
    } else null

    val schedulesForDate = HakotRepository.schedules.filter { sch ->
        val matchesDate = HakotRepository.isScheduleActiveOnDate(sch, selectedDate)
        val matchesSearch = searchQuery.isEmpty() ||
            sch.barangay.contains(searchQuery, ignoreCase = true) ||
            sch.routeName.contains(searchQuery, ignoreCase = true) ||
            sch.wasteCategory.displayName.contains(searchQuery, ignoreCase = true)

        matchesDate && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Screen Title Header: Title & Time Clock on the Same Compact Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Schedule",
                    tint = EcoGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hakot Collection Schedule",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Real System Date & Time Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Clock",
                        tint = EcoGreenLight,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = nowLocalTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Working Time Status Banner
        if (activeCurrentSchedule != null) {
            Surface(
                color = EcoGreenContainer,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EcoGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EcoGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "⚡ ACTIVE WORKING TIME COLLECTION NOW",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenLight
                        )
                        Text(
                            text = "${activeCurrentSchedule.routeName} (${activeCurrentSchedule.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${activeCurrentSchedule.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))})",
                            fontSize = 10.sp,
                            color = TextOnDarkPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = TextOnDarkSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedDate == nowLocalDate) "Next Working Window: Today 8:00 AM - 11:30 AM" else "Collection Schedules for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        fontSize = 11.sp,
                        color = TextOnDarkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Interactive Calendar Date Picker Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isMonthlyView) "31-Day Calendar View:" else "Weekly Calendar View:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )
            Surface(
                onClick = { isMonthlyView = !isMonthlyView },
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = if (isMonthlyView) "Switch to 7-Day" else "Switch to 31-Day",
                    fontSize = 9.sp,
                    color = EcoGreenLight,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val daysRange = if (isMonthlyView) (0..30) else (-2..4)
        val daysWindow = remember(nowLocalDate, isMonthlyView) {
            daysRange.map { nowLocalDate.plusDays(it.toLong()) }
        }

        // Compact Date Selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(daysWindow) { date ->
                val isSelected = date == selectedDate
                val isToday = date == nowLocalDate

                val dayOfWeekStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                val dayOfMonthStr = date.dayOfMonth.toString()

                Surface(
                    onClick = { selectedDate = date },
                    color = if (isSelected) EcoGreenPrimary else DarkSurfaceCard,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isToday) EcoGreenLight else DarkBorder
                    ),
                    modifier = Modifier.width(48.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isToday) {
                            Text(
                                text = "TODAY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.Black else EcoGreenLight
                            )
                        } else {
                            Text(
                                text = dayOfWeekStr.uppercase(),
                                fontSize = 8.5.sp,
                                color = if (isSelected) Color.Black else TextOnDarkSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayOfMonthStr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextOnDarkPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Collection Schedules List (${schedulesForDate.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EcoGreenLight
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Schedule List for Selected Date
        if (schedulesForDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "No Schedule",
                        tint = TextOnDarkMuted,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No collection scheduled for this date",
                        fontSize = 12.5.sp,
                        color = TextOnDarkSecondary
                    )
                    Text(
                        text = "Regular street corridors run Mon-Sat 8:00 AM - 5:00 PM",
                        fontSize = 10.5.sp,
                        color = TextOnDarkMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(schedulesForDate) { sch ->
                    ScheduleCardItem(
                        schedule = sch,
                        onClick = {
                            HakotRepository.selectedMapDate = sch.date
                            selectedScheduleDetail = sch
                        }
                    )
                }
            }
        }
    }

    selectedScheduleDetail?.let { sch ->
        ScheduleDetailModal(schedule = sch, onDismiss = { selectedScheduleDetail = null })
    }
}

@Composable
fun ScheduleCardItem(schedule: ScheduleItem, onClick: () -> Unit = {}) {
    val truckName = schedule.truckAssigned.takeIf { it.isNotBlank() }?.replace("(null)", "")?.trim()?.ifBlank { "No assigned truck yet" } ?: "No assigned truck yet"
    val displayStatus = HakotRepository.getDisplayStatus(schedule.status)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Route Name on Left & Status Pill on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.routeName,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))

                val context = LocalContext.current
                var isNotifOn by remember(schedule.id) {
                    mutableStateOf(HakotRepository.isScheduleNotificationEnabled(context, schedule.id))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            tint = if (isNotifOn) SafetyOrange else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Subtle Red Pill for MISSED status, clean neutral for others
                    if (displayStatus.uppercase() == "MISSED") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HazardousRedContainer)
                                .border(1.dp, HazardousRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MISSED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HazardousRed
                            )
                        }
                    } else {
                        val statusColor = when {
                            displayStatus.equals("ACTIVE", true) || displayStatus.equals("COMPLETED", true) || displayStatus.equals("FINISH", true) || displayStatus.equals("DONE", true) -> EcoGreenLight
                            displayStatus.equals("UPCOMING", true) || displayStatus.equals("DRAFT", true) || displayStatus.equals("SCHEDULED", true) -> TextOnDarkSecondary
                            displayStatus.equals("MISSED", true) -> HazardousRed
                            displayStatus.equals("ON_HOLD", true) || displayStatus.equals("HOLD", true) -> RecycleYellow
                            else -> TextOnDarkSecondary
                        }
                        Text(
                            text = displayStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Barangay
            Text(
                text = schedule.barangay,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EcoGreenLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(5.dp))

            // Time Window Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = TextOnDarkPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${schedule.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${schedule.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Smaller, Less Prominent Truck Assignment Info Container
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_truck),
                        contentDescription = "Truck",
                        tint = TextOnDarkMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Assigned: $truckName",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextOnDarkSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleDetailModal(schedule: ScheduleItem, onDismiss: () -> Unit) {
    val truckName = schedule.truckAssigned.takeIf { it.isNotBlank() }?.replace("(null)", "")?.trim()?.ifBlank { "No assigned truck yet" } ?: "No assigned truck yet"
    val displayStatus = HakotRepository.getDisplayStatus(schedule.status)

    val routeSegmentsList = remember(schedule) {
        if (schedule.orderedSegments.isNotEmpty()) {
            schedule.orderedSegments.mapNotNull { seg ->
                val coords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                val pts = coords.mapNotNull { (lng, lat) ->
                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                }
                if (pts.size >= 2) pts else null
            }
        } else {
            val routeMatch = HakotRepository.routes.find { it.routeName.equals(schedule.routeName, ignoreCase = true) }
            val routePts = routeMatch?.geoPoints?.mapNotNull { (lng, lat) ->
                if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
            } ?: emptyList()
            if (routePts.size >= 2) listOf(routePts) else emptyList()
        }
    }
    val routePoints = remember(routeSegmentsList) { routeSegmentsList.flatten() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceCard,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collection Schedule Details",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Small Map Preview Box showing the schedule's specific route corridor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    DarkMapView(
                        modifier = Modifier.fillMaxSize(),
                        customRoutePoints = routePoints,
                        customRouteSegments = routeSegmentsList,
                        showDefaultSchedulesWhenEmpty = false
                    )
                }

                Column {
                    Text(text = "Route Name", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(text = schedule.routeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                }

                HorizontalDivider(color = DarkBorder)

                Column {
                    Text(text = "Assigned Barangay / Area", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(text = schedule.barangay, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoGreenLight)
                }

                Column {
                    Text(text = "Collection Date", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(text = schedule.date.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextOnDarkPrimary)
                }

                Column {
                    Text(text = "Time Window", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(
                        text = "${schedule.startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${schedule.endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafetyOrange
                    )
                }

                Column {
                    Text(text = "Assigned Truck", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(text = truckName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                }

                Column {
                    Text(text = "Status", fontSize = 10.sp, color = TextOnDarkSecondary)
                    val statusColor = when {
                        displayStatus.equals("ACTIVE", true) || displayStatus.equals("COMPLETED", true) || displayStatus.equals("FINISH", true) || displayStatus.equals("DONE", true) -> EcoGreenLight
                        displayStatus.equals("UPCOMING", true) || displayStatus.equals("DRAFT", true) || displayStatus.equals("SCHEDULED", true) -> TextOnDarkSecondary
                        displayStatus.equals("MISSED", true) -> HazardousRed
                        displayStatus.equals("ON_HOLD", true) || displayStatus.equals("HOLD", true) -> RecycleYellow
                        else -> TextOnDarkSecondary
                    }
                    Text(text = displayStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }

                if (schedule.dumpsiteName.isNotBlank()) {
                    Column {
                        Text(text = "Destination Dumpsite", fontSize = 10.sp, color = TextOnDarkSecondary)
                        Text(text = schedule.dumpsiteName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    )
}
