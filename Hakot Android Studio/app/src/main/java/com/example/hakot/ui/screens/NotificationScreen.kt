package com.example.hakot.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.ScheduleItem
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.SafetyOrange
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import java.util.Locale

@Composable
fun NotificationScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var globalNotif by remember { mutableStateOf(HakotRepository.getGlobalNotificationsEnabled(context)) }
    var showSettingsDropdown by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableStateOf(0) }

    var notif10pm by remember { mutableStateOf(HakotRepository.getNotifSetting(context, "10pm_before", true)) }
    var notif1hour by remember { mutableStateOf(HakotRepository.getNotifSetting(context, "1hour_before", true)) }
    var notifActive by remember { mutableStateOf(HakotRepository.getNotifSetting(context, "truck_active", true)) }
    var notifComplete by remember { mutableStateOf(HakotRepository.getNotifSetting(context, "collection_complete", true)) }

    var notifTimeStr by remember { mutableStateOf(HakotRepository.getNotifTimeString(context, "10pm_time", "10:00 PM")) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            globalNotif = true
            HakotRepository.setGlobalNotificationsEnabled(context, true)
            Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val enabledSchedules = remember(refreshKey, HakotRepository.schedules.size, globalNotif) {
        if (!globalNotif) emptyList()
        else {
            HakotRepository.schedules.filter { HakotRepository.isScheduleNotificationEnabled(context, it.id) }
        }
    }

    Surface(
        color = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = SafetyOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Schedule Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextOnDarkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Toggle Card with Settings Button
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Push Notifications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showSettingsDropdown = !showSettingsDropdown },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (showSettingsDropdown) EcoGreenPrimary else TextOnDarkSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Switch(
                                checked = globalNotif,
                                onCheckedChange = {
                                    if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        globalNotif = it
                                        HakotRepository.setGlobalNotificationsEnabled(context, it)
                                        Toast.makeText(context, if (it) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = EcoGreenPrimary
                                )
                            )
                        }
                    }

                    // Dropdown Settings List of 4 Toggle Buttons
                    AnimatedVisibility(visible = showSettingsDropdown) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Notification Triggers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenLight
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. Day before (editable time, default 10pm)
                            NotificationSettingRow(
                                title = "Day before schedule at $notifTimeStr",
                                checked = notif10pm,
                                onCheckedChange = {
                                    notif10pm = it
                                    HakotRepository.setNotifSetting(context, "10pm_before", it)
                                },
                                timeText = notifTimeStr,
                                onTimeClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val hour12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                                            val amPm = if (hourOfDay < 12) "AM" else "PM"
                                            val formatted = String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm)
                                            notifTimeStr = formatted
                                            HakotRepository.setNotifTimeString(context, "10pm_time", formatted)
                                            Toast.makeText(context, "Reminder time set to $formatted", Toast.LENGTH_SHORT).show()
                                        },
                                        22, 0, false
                                    ).show()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. 1 hour before
                            NotificationSettingRow(
                                title = "1 hour before schedule",
                                checked = notif1hour,
                                onCheckedChange = {
                                    notif1hour = it
                                    HakotRepository.setNotifSetting(context, "1hour_before", it)
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Truck active / start / hold
                            NotificationSettingRow(
                                title = "Notif if truck is now active or press start collection / Hold collection",
                                checked = notifActive,
                                onCheckedChange = {
                                    notifActive = it
                                    HakotRepository.setNotifSetting(context, "truck_active", it)
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Collection complete
                            NotificationSettingRow(
                                title = "Notif if collection is complete",
                                checked = notifComplete,
                                onCheckedChange = {
                                    notifComplete = it
                                    HakotRepository.setNotifSetting(context, "collection_complete", it)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enabled Schedules (${enabledSchedules.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EcoGreenLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!globalNotif) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Notifications are currently turned off.\nEnable the master switch above to view alerts.",
                        fontSize = 13.sp,
                        color = TextOnDarkSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (enabledSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No schedules currently have notifications enabled.\n\nTap the bell icon on any schedule card in Home or Schedules to add it to your notification list.",
                        fontSize = 13.sp,
                        color = TextOnDarkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(enabledSchedules) { sch ->
                        NotificationScheduleCardItem(
                            schedule = sch,
                            onToggleOff = {
                                HakotRepository.toggleScheduleNotification(context, sch.id)
                                refreshKey++
                                Toast.makeText(context, "Removed ${sch.routeName} from notifications", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    timeText: String? = null,
    onTimeClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                color = TextOnDarkPrimary
            )
            if (timeText != null && onTimeClick != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    onClick = onTimeClick,
                    color = DarkSurfaceCard,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Change Time 🕒 ($timeText)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier.scale(0.75f),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = EcoGreenPrimary
                )
            )
        }
    }
}

@Composable
fun NotificationScheduleCardItem(
    schedule: ScheduleItem,
    onToggleOff: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schedule.routeName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onToggleOff,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Disable Notification",
                        tint = SafetyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Barangay: ${schedule.barangay} • Date: ${schedule.date}",
                fontSize = 11.5.sp,
                color = TextOnDarkSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Time: ${schedule.startTime} – ${schedule.endTime}",
                fontSize = 11.5.sp,
                color = EcoGreenLight,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
