package com.example.hakot.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.ReportType
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.HazardousRedContainer
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import kotlinx.coroutines.launch

@Composable
fun ReportScreen(
    searchQuery: String = "",
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(ReportType.MISSED_COLLECTION) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var locationInput by remember { mutableStateOf("J.P. Laurel Street, Nasugbu") }
    var descriptionInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submittedRefCode by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = "Report",
                    tint = HazardousRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Waste Issue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDarkPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(EcoGreenContainer)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("100% Anonymous", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "No personal details or profile required. Your report alerts the sanitation team.",
            fontSize = 12.sp,
            color = TextOnDarkSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (submittedRefCode != null) {
            Surface(
                color = DarkSurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, EcoGreenPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Report Submitted Successfully!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = submittedRefCode!!,
                        fontSize = 12.sp,
                        color = TextOnDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            submittedRefCode = null
                            descriptionInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit Another Report", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Category Dropdown
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

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(12.dp))

            // Details / Notes Input
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
                placeholder = { Text("e.g. Uncollected garbage left at corner since morning...", fontSize = 12.sp, color = TextOnDarkMuted) },
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

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage!!,
                    color = HazardousRed,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (locationInput.isBlank()) {
                        errorMessage = "Please enter location or street name."
                        return@Button
                    }
                    isSubmitting = true
                    errorMessage = null

                    scope.launch {
                        val result = HakotRepository.addAnonymousReport(
                            type = selectedType,
                            locationName = locationInput.trim(),
                            description = descriptionInput.trim()
                        )
                        isSubmitting = false
                        if (result.first) {
                            Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                            submittedRefCode = result.second
                        } else {
                            errorMessage = result.second
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Submitting Report...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "🚩 SUBMIT ANONYMOUS REPORT",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
