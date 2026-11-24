package com.example.hakot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.Announcement
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    isAdminMode: Boolean = false,
    selectedTab: HakotTab = HakotTab.MAP,
    onTabSelected: (HakotTab) -> Unit = {},
    onSelectSuggestion: ((String, Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAnnouncementOverlay by remember { mutableStateOf(false) }

    val announcementsList = HakotRepository.announcements

    val tickerText = remember(announcementsList.size, announcementsList.firstOrNull()?.id) {
        if (announcementsList.isNotEmpty()) {
            "📢 " + announcementsList.joinToString("     •     📢 ") { item ->
                "[${item.category.uppercase()}] ${item.title}: ${item.message}"
            }
        } else {
            "📢 Welcome to Hakot Municipal Portal! Tap here to view waste collection announcements and community updates."
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = DarkSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3-Line Menu Bar Tab on Left (Replaces Logo)
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Announcement Bar Container (No Search icon, clicking opens overlay)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .clickable {
                            showAnnouncementOverlay = true
                        },
                    shape = RoundedCornerShape(17.dp),
                    color = DarkSurfaceCard,
                    border = BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Announcements",
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            RightToLeftTicker(
                                text = tickerText,
                                textStyle = TextStyle(
                                    color = TextOnDarkPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Clickable Notification Bell Icon on Far Right
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextOnDarkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Overlay displaying all announcement text when clicked
    if (showAnnouncementOverlay) {
        AnnouncementOverlayDialog(
            announcements = announcementsList,
            onDismiss = { showAnnouncementOverlay = false }
        )
    }
}

@Composable
fun RightToLeftTicker(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(color = TextOnDarkPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
) {
    var containerWidth by remember { mutableIntStateOf(300) }

    val infiniteTransition = rememberInfiniteTransition(label = "rightToLeftTicker")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = containerWidth.toFloat().coerceAtLeast(250f),
        targetValue = -900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .onGloballyPositioned { containerWidth = it.size.width },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.offset { IntOffset(offsetX.toInt(), 0) }
        )
    }
}

@Composable
fun AnnouncementOverlayDialog(
    announcements: List<Announcement>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceCard,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Municipal Announcements",
                        color = EcoGreenLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                if (announcements.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = TextOnDarkMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active municipal announcements.",
                            color = TextOnDarkMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(announcements) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = BorderStroke(1.dp, if (item.isPinned) EcoGreenPrimary else DarkBorder),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val catBg = when (item.category.lowercase()) {
                                                "urgent" -> HazardousRed.copy(alpha = 0.2f)
                                                "schedule change" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                else -> EcoGreenPrimary.copy(alpha = 0.2f)
                                            }
                                            val catTxt = when (item.category.lowercase()) {
                                                "urgent" -> HazardousRed
                                                "schedule change" -> Color(0xFFFF9800)
                                                else -> EcoGreenPrimary
                                            }
                                            Surface(
                                                color = catBg,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.category.uppercase(),
                                                    color = catTxt,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (item.isPinned) {
                                                Surface(
                                                    color = EcoGreenPrimary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "📌 PINNED",
                                                        color = EcoGreenPrimary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        val formattedDate = remember(item.timestampMillis) {
                                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.timestampMillis))
                                        }
                                        Text(
                                            text = formattedDate,
                                            fontSize = 10.sp,
                                            color = TextOnDarkMuted
                                        )
                                    }

                                    Text(
                                        text = item.title,
                                        color = TextOnDarkPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = item.message,
                                        color = TextOnDarkSecondary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
