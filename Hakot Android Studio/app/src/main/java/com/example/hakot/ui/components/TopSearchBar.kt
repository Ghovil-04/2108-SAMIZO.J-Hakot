package com.example.hakot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hakot.R
import com.example.hakot.data.GisManager
import com.example.hakot.data.HakotRepository
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary

@Composable
fun TopSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    isAdminMode: Boolean = false,
    onSelectSuggestion: ((String, Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isSuggestionsVisible by remember { mutableStateOf(false) }

    val activeLocation = HakotRepository.selectedMunicipalityName?.substringBefore(",")?.trim()
        ?: HakotRepository.activeOrganization.assignedMunicipality.substringBefore(",").trim().ifBlank { "Nasugbu" }

    val suggestions = remember(query) {
        if (query.isBlank()) emptyList()
        else {
            val list = mutableListOf<Triple<String, Double, Double>>()
            // 1. Municipalities & Provinces
            HakotRepository.municipalities.filter {
                it.name.contains(query, ignoreCase = true) || it.province.contains(query, ignoreCase = true)
            }.forEach {
                list.add(Triple(it.name, it.centerLat, it.centerLng))
            }
            // 2. Streets
            GisManager.roadSegments.filter { it.segmentName.contains(query, ignoreCase = true) }.take(5).forEach { road ->
                val coords = if (road.clippedCoordinates.isNotEmpty()) road.clippedCoordinates else road.lineStringCoordinates
                if (coords.isNotEmpty()) {
                    val avgLat = coords.map { it.second }.filter { it.isFinite() }.average()
                    val avgLon = coords.map { it.first }.filter { it.isFinite() }.average()
                    if (avgLat.isFinite() && avgLon.isFinite()) {
                        list.add(Triple(road.segmentName, avgLat, avgLon))
                    }
                }
            }
            // 3. Routes
            HakotRepository.routes.filter { it.routeName.contains(query, ignoreCase = true) }.forEach { r ->
                if (r.geoPoints.isNotEmpty()) {
                    val avgLat = r.geoPoints.map { it.second }.filter { it.isFinite() }.average()
                    val avgLon = r.geoPoints.map { it.first }.filter { it.isFinite() }.average()
                    if (avgLat.isFinite() && avgLon.isFinite()) {
                        list.add(Triple(r.routeName, avgLat, avgLon))
                    }
                }
            }
            // 4. Dumpsites
            HakotRepository.dumpsites.filter { it.name.contains(query, ignoreCase = true) }.forEach { d ->
                if (d.latitude.isFinite() && d.longitude.isFinite()) {
                    list.add(Triple(d.name, d.latitude, d.longitude))
                }
            }
            list.distinctBy { it.first }.take(6)
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
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Hakot Logo on Left
                Image(
                    painter = painterResource(id = R.drawable.hakot_logo),
                    contentDescription = "Hakot Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Search Bar Input Field in sleek shorter container
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceCard,
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search in $activeLocation...",
                                    style = TextStyle(
                                        color = TextOnDarkMuted,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = {
                                    onQueryChange(it)
                                    isSuggestionsVisible = it.isNotBlank()
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextOnDarkPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(EcoGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onQueryChange("")
                                    isSuggestionsVisible = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextOnDarkSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextOnDarkSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Clickable Notification Bell Icon on Right
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

        if (isSuggestionsVisible && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkBorder),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    suggestions.forEach { (name, lat, lon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onQueryChange(name)
                                    isSuggestionsVisible = false
                                    onSelectSuggestion?.invoke(name, lat, lon)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextOnDarkPrimary
                            )
                        }
                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
