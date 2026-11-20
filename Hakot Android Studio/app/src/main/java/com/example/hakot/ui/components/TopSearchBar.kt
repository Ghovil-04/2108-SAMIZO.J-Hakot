package com.example.hakot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    modifier: Modifier = Modifier
) {
    var isSuggestionsVisible by remember { mutableStateOf(false) }

    Surface(
        color = DarkSurface,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp)
                .height(42.dp),
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

            // Search Bar Input Field in sleek slim container
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
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
                                text = if (isAdminMode) "Search routes, trucks..." else "Search collection route or dumpsite...",
                                style = TextStyle(
                                    color = TextOnDarkMuted,
                                    fontSize = 12.sp,
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
                                fontSize = 12.sp,
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
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextOnDarkSecondary,
                                modifier = Modifier.size(15.dp)
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
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (isAdminMode) EcoGreenPrimary else TextOnDarkPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
