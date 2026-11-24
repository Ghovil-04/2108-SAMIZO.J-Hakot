package com.example.hakot.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hakot.R
import com.example.hakot.data.HakotRepository
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary

@Composable
fun HakotDrawerContent(
    isAdminMode: Boolean,
    onToggleAdminMode: (Boolean) -> Unit,
    onChangeMunicipality: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showHelpDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        HakotRepository.fetchMunicipalitiesFromSupabase(context)
    }

    Surface(
        color = DarkSurface,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hakot_logo),
                    contentDescription = "Hakot Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "HAKOT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary
                    )
                    Text(
                        text = if (isAdminMode) "Admin Access Portal" else "Anonymous Citizen Mode",
                        fontSize = 12.sp,
                        color = if (isAdminMode) EcoGreenPrimary else TextOnDarkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Settings Section
            Text(
                text = "SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextOnDarkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            // System Administration Item in Settings
            DrawerItemRow(
                icon = Icons.Default.AdminPanelSettings,
                title = "System Administration",
                subtitle = if (isAdminMode) "Active (Tap to exit)" else "System & District Admin Portal",
                onClick = {
                    onToggleAdminMode(!isAdminMode)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Help and Support Section
            Text(
                text = "HELP & SUPPORT",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextOnDarkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            DrawerItemRow(
                icon = Icons.Default.HelpOutline,
                title = "Sanitation FAQ & Waste Management",
                subtitle = "Bin rules and waste management info",
                onClick = { showHelpDialog = true }
            )

            DrawerItemRow(
                icon = Icons.Default.PhoneInTalk,
                title = "LGU Waste Office & Contact Info",
                subtitle = "HakotOfficial@gmail.com",
                onClick = { showContactDialog = true }
            )

            DrawerItemRow(
                icon = Icons.Default.Security,
                title = "Privacy & Anonymous Guarantee",
                subtitle = "Why and how the app is anonymous",
                onClick = { showPrivacyDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DarkBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Exit App Button
            DrawerItemRow(
                icon = Icons.Default.ExitToApp,
                title = "Exit App",
                subtitle = "Close Hakot application",
                onClick = {
                    (context as? Activity)?.finishAffinity()
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            val currentMunId = HakotRepository.activeOrganization.municipalityId.takeIf { it.isNotBlank() }
                ?: HakotRepository.municipalities.firstOrNull()?.id
                ?: "cb394eb7-a8b7-45f2-a314-3926baeab31f"

            Text(
                text = "Hakot App v1.0.0 • ID: $currentMunId • LGU System",
                fontSize = 9.sp,
                color = TextOnDarkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Sanitation FAQ Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Waste Management & FAQ", color = EcoGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("1. Proper Waste Segregation:", fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, fontSize = 13.sp)
                    Text("• Biodegradable: Food scraps, leaves, and garden waste for composting.\n• Non-Biodegradable / Residual: Plastics, wrappers, and non-recyclable packaging.\n• Recyclables: Clean paper, cardboard, glass bottles, and metal cans.\n• Hazardous: Batteries, chemicals, and electronic waste.", color = TextOnDarkSecondary, fontSize = 11.5.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Collection Guidelines:", fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, fontSize = 13.sp)
                    Text("• Place garbage bins or secure bags outside your residence on scheduled collection mornings.\n• Ensure waste is tightly secured to prevent stray animals from scattering garbage.", color = TextOnDarkSecondary, fontSize = 11.5.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Got it", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // LGU Waste Office & Contact Info Dialog
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LGU Waste Office & Support", color = EcoGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("For inquiries, sanitation assistance, or reporting collection issues, contact the municipal waste office through:", color = TextOnDarkSecondary, fontSize = 12.sp)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gmail:", color = TextOnDarkMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("HakotOfficial@gmail.com", color = EcoGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hotline / Number:", color = TextOnDarkMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("0999 500 3244", color = EcoGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showContactDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Privacy & Anonymous Guarantee Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Privacy & Anonymous Guarantee", color = EcoGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Why Hakot is Anonymous:", fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, fontSize = 13.sp)
                    Text("Hakot is built on community trust and public sanitation reporting. We believe every citizen should be able to report overflowing bins, illegal dumping, or request assistance without fear of exposure, login friction, or personal data tracking.", color = TextOnDarkSecondary, fontSize = 11.5.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("How We Protect Your Privacy:", fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, fontSize = 13.sp)
                    Text("• No Account Login Required: Citizens use the app instantly without signing up or providing personal credentials.\n• No Personal Tracking: Reports and interactions do not store or link your personal identity, phone numbers, or emails to public reports.\n• Secure Municipal Data: Only essential location and waste report data are processed by the local municipality sanitation department to dispatch collection trucks.", color = TextOnDarkSecondary, fontSize = 11.5.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Understood", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun DrawerItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TextOnDarkSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnDarkPrimary
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = TextOnDarkSecondary
                        )
                    }
                }
            }
            trailing?.invoke()
        }
    }
}
