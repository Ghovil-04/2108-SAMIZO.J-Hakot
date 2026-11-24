package com.example.hakot.ui.screens

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import com.example.hakot.ui.components.DarkMapView
import com.example.hakot.model.Route
import com.example.hakot.model.Dumptrack
import com.example.hakot.model.Dumpsite
import androidx.compose.material3.CircularProgressIndicator
import com.example.hakot.model.ScheduleItem
import com.example.hakot.model.WasteCategory
import com.example.hakot.data.GisManager
import com.example.hakot.data.GisRoadSegmentFeature
import org.osmdroid.util.GeoPoint
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.hakot.data.SupabaseClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import com.example.hakot.ui.theme.RecycleYellowContainer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.hakot.ui.components.DumpTruckLogo
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.AccountApplication
import com.example.hakot.model.AccountInvitation
import com.example.hakot.model.AdminStatus
import com.example.hakot.model.AdminUserProfile
import com.example.hakot.model.Announcement
import com.example.hakot.model.AnonymousReport
import com.example.hakot.model.ApplicationStatus
import com.example.hakot.model.ReportStatus
import com.example.hakot.model.TruckerAccount
import com.example.hakot.model.UserRole
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.foundation.horizontalScroll
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.DarkSurfaceVariant
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
import com.example.hakot.ui.theme.SafetyOrange
import com.example.hakot.ui.theme.RecycleYellow
import com.example.hakot.ui.theme.TextOnDarkMuted
import com.example.hakot.ui.theme.TextOnDarkPrimary
import com.example.hakot.ui.theme.TextOnDarkSecondary
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    searchQuery: String = "",
    onExitAdminMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var showRedeemModal by remember { mutableStateOf(false) }
    var showCreateAccountModal by remember { mutableStateOf(false) }
    var showRegistrationScreen by remember { mutableStateOf(false) }
    var showActivationScreen by remember { mutableStateOf(false) }

    var liveClockString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            liveClockString = sdf.format(Date())
            delay(1000)
        }
    }

    val isAuthenticated = HakotRepository.isAuthenticated
    val userRole = HakotRepository.currentAdminRole

    if (showRegistrationScreen) {
        AccountRegistrationScreen(
            onBackToLogin = { showRegistrationScreen = false }
        )
    } else if (showActivationScreen) {
        AccountActivationScreen(
            onBackToLogin = { showActivationScreen = false }
        )
    } else if (!isAuthenticated) {
        AdminLoginScreen(
            onOpenRedeemModal = { showRedeemModal = true },
            onOpenRegistration = { showRegistrationScreen = true },
            onOpenActivation = { showActivationScreen = true },
            onExitAdminPortal = onExitAdminMode
        )
    } else {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        if (userRole == UserRole.DRIVER) {
            DriverDashboardView(onSignOut = {
                scope.launch {
                    HakotRepository.logout(context)
                }
            })
        } else {
            val adminTabs = if (userRole == UserRole.SYSTEM_OWNER) {
                listOf(
                    "Accounts",
                    "Applications",
                    "Reset Requests"
                )
            } else {
                listOf(
                    "District Overview",
                    "Account Management",
                    "Announcements",
                    "Collection Schedule",
                    "Schedule Audit",
                    "Dumpsites",
                    "Fleet Trucks",
                    "Citizen Reports",
                    "Audit History"
                )
            }

            if (selectedAdminTab >= adminTabs.size) {
                selectedAdminTab = 0
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                // Top Admin Banner with Sign Out Icon Button (Thinned header)
                Surface(
                    color = EcoGreenContainer,
                    border = BorderStroke(1.dp, EcoGreenPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "District Admin",
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MUNICIPAL SANITATION PORTAL",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDarkPrimary
                                )
                                val dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                                val dateText = LocalDate.now().format(dateFmt)
                                Text(
                                    text = "$dateText • 🕒 $liveClockString",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoGreenLight
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    HakotRepository.logout(context)
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceCard)
                                .border(1.dp, HazardousRed.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = HazardousRed,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Admin Tab Selector Row
                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = DarkSurface,
                    contentColor = EcoGreenPrimary,
                    edgePadding = 16.dp
                ) {
                    adminTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedAdminTab == index,
                            onClick = { selectedAdminTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Admin Tab Content Views
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (userRole == UserRole.SYSTEM_OWNER) {
                        when (selectedAdminTab) {
                            0 -> AdminAccountManagementView(
                                onOpenCreateModal = { showCreateAccountModal = true },
                                onNavigateToApplications = { selectedAdminTab = 1 },
                                onNavigateToResetRequests = { selectedAdminTab = 2 }
                            )
                            1 -> SystemOwnerApplicationsReviewView(
                                onApproved = { selectedAdminTab = 0 }
                            )
                            2 -> SystemOwnerPasswordResetRequestsView()
                            else -> AdminAccountManagementView(
                                onOpenCreateModal = { showCreateAccountModal = true },
                                onNavigateToApplications = { selectedAdminTab = 1 },
                                onNavigateToResetRequests = { selectedAdminTab = 2 }
                            )
                        }
                    } else {
                        when (selectedAdminTab) {
                            0 -> DistrictOverviewView()
                            1 -> AdminAccountManagementView(
                                onOpenCreateModal = { showCreateAccountModal = true }
                            )
                            2 -> AdminAnnouncementsView()
                            3 -> CollectionScheduleDashboardView(onNavigateToDumpsites = { selectedAdminTab = 5 })
                            4 -> AdminScheduleAuditView()
                            5 -> AdminDumpsitesView()
                            6 -> AdminTrucksView()
                            7 -> AdminReportsView()
                            8 -> AdminAuditLogsView()
                            else -> DistrictOverviewView()
                        }
                    }
                }
            }
        }
    }

    if (showRedeemModal) {
        RedeemInvitationModal(
            onDismiss = { showRedeemModal = false },
            onRedeemed = { showRedeemModal = false }
        )
    }

    if (showCreateAccountModal) {
        CreateAccountModal(
            onDismiss = { showCreateAccountModal = false },
            onCreated = { showCreateAccountModal = false }
        )
    }
}



@Composable
fun SystemOwnerApplicationsReviewView(
    onApproved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isProcessingDecision by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var selectedApplication by remember { mutableStateOf<AccountApplication?>(null) }
    var inspectCertPath by remember { mutableStateOf<String?>(null) }

    val applications = HakotRepository.accountApplications

    fun refreshApplications() {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            val res = HakotRepository.fetchAccountApplications()
            isLoading = false
            if (!res.first) {
                errorMessage = res.second
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshApplications()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACCOUNT APPLICATIONS REVIEW",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                Text(
                    text = "Review and approve/reject pending municipal self-registration requests. (System Owner Only)",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary
                )
            }
            IconButton(
                enabled = !isLoading && !isProcessingDecision,
                onClick = { refreshApplications() }
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = EcoGreenLight)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, HazardousRed, RoundedCornerShape(10.dp))
            ) {
                Text(it, color = HazardousRed, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        successMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(10.dp))
            ) {
                Text(it, color = EcoGreenLight, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EcoGreenPrimary)
            }
        } else if (applications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Mail, contentDescription = "Empty", tint = TextOnDarkSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pending account applications found.", fontSize = 13.sp, color = TextOnDarkSecondary)
                }
            }
        } else {
            val expandedApplicationIds = remember { mutableStateMapOf<String, Boolean>() }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(applications) { app ->
                    val isExpanded = expandedApplicationIds[app.applicationId] == true
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isExpanded) EcoGreenPrimary else DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isProcessingDecision) {
                                        expandedApplicationIds[app.applicationId] = !isExpanded
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(app.fullName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isExpanded) "▲" else "▼", fontSize = 12.sp, color = EcoGreenLight)
                                }
                                Surface(
                                    color = when (app.status) {
                                        ApplicationStatus.PENDING -> RecycleYellowContainer
                                        ApplicationStatus.APPROVED -> EcoGreenContainer
                                        ApplicationStatus.REJECTED -> HazardousRed.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        expandedApplicationIds[app.applicationId] = !isExpanded
                                    }
                                ) {
                                    Text(
                                        text = app.status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (app.status) {
                                            ApplicationStatus.PENDING -> RecycleYellow
                                            ApplicationStatus.APPROVED -> EcoGreenLight
                                            ApplicationStatus.REJECTED -> HazardousRed
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            if (app.organizationName.isNotBlank()) {
                                Text("Organization: ${app.organizationName} (${app.organizationType})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            }
                            Text("Requested Role: ${app.requestedRole.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text("Municipality: ${app.municipality}", fontSize = 11.sp, color = TextOnDarkSecondary)

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Detailed Application Information:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Organization Name: ${app.organizationName.ifBlank { "N/A" }}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Organization Type: ${app.organizationType}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Requested Role: ${app.requestedRole.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                                Text("• Municipality Scope: ${app.municipality}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Administrator Full Name: ${app.fullName}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Administrator Email: ${app.email}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Contact Number: ${app.contactNumber}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Office / Department: ${app.officeDepartment} (${app.officialPosition})", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Application Status: ${app.status.label}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                val dateFormatted = try {
                                    DateFormat.format("MMM dd, yyyy • hh:mm a",
                                        Date(app.createdAtMillis)
                                    ).toString()
                                } catch (_: Exception) { "N/A" }
                                Text("• Submission Date: $dateFormatted", fontSize = 11.sp, color = TextOnDarkSecondary)

                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                Text("Admin: ${app.fullName} • Email: ${app.email}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("Contact: ${app.contactNumber} • Office: ${app.officeDepartment}", fontSize = 11.sp, color = TextOnDarkSecondary)
                            }

                            if (app.certificatePath.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                TextButton(
                                    onClick = { inspectCertPath = app.certificatePath },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = "Cert", tint = EcoGreenPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Certificate of Operation", fontSize = 11.sp, color = EcoGreenPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (app.status == ApplicationStatus.PENDING) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        enabled = !isProcessingDecision,
                                        onClick = {
                                            Log.d("HAKOT_APPROVAL_DEBUG", "Reject button clicked for application ID: ${app.applicationId}")
                                            scope.launch {
                                                isProcessingDecision = true
                                                selectedApplication = app
                                                errorMessage = null
                                                successMessage = null
                                                val res = HakotRepository.updateAccountApplicationStatus(app.applicationId, ApplicationStatus.REJECTED)
                                                isProcessingDecision = false
                                                selectedApplication = null
                                                if (res.first) {
                                                    HakotRepository.fetchAccountApplications()
                                                    successMessage = "Application rejected and removed successfully."
                                                    Toast.makeText(context, "Application rejected and removed successfully.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    errorMessage = res.second
                                                    Toast.makeText(context, "Rejection failed: ${res.second}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HazardousRed.copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        if (isProcessingDecision && selectedApplication?.applicationId == app.applicationId) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("Reject", color = HazardousRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        enabled = !isProcessingDecision,
                                        onClick = {
                                            Log.d("HAKOT_APPROVAL_DEBUG", "1. Approve button clicked for application ID: ${app.applicationId}, Org: ${app.organizationName}")
                                            selectedApplication = app
                                            Log.d("HAKOT_APPROVAL_DEBUG", "2. Application ID retrieved: ${app.applicationId}")
                                            scope.launch {
                                                isProcessingDecision = true
                                                errorMessage = null
                                                successMessage = null
                                                Log.d("HAKOT_APPROVAL_DEBUG", "3. Approval function called (updateAccountApplicationStatus)")
                                                val res = HakotRepository.updateAccountApplicationStatus(app.applicationId, ApplicationStatus.APPROVED)
                                                isProcessingDecision = false
                                                if (res.first) {
                                                    HakotRepository.fetchAccountApplications()
                                                    successMessage = "Application approved successfully and account activated."
                                                    Log.d("HAKOT_APPROVAL_DEBUG", "8. UI updated: success message displayed")
                                                    Toast.makeText(context, "Application approved successfully!", Toast.LENGTH_SHORT).show()
                                                    selectedApplication = null
                                                    onApproved()
                                                } else {
                                                    errorMessage = res.second
                                                    Toast.makeText(context, "Approval failed: ${res.second}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        if (isProcessingDecision && selectedApplication?.applicationId == app.applicationId) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approving...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("Approve", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (inspectCertPath != null) {
        val path = inspectCertPath!!
        val isImage = path.lowercase().endsWith(".png") || path.lowercase().endsWith(".jpg") || path.lowercase().endsWith(".jpeg")
        val isPdf = path.lowercase().endsWith(".pdf")

        var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
        var isLoadingImage by remember(path) { mutableStateOf(isImage) }
        var loadError by remember(path) { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        LaunchedEffect(path) {
            if (isImage) {
                withContext(Dispatchers.IO) {
                    try {
                        val token = HakotRepository.getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
                        val conn = openStorageConnection(path, token)
                        val bmp = BitmapFactory.decodeStream(conn.inputStream)
                        if (bmp != null) {
                            bitmap = bmp
                        } else {
                            loadError = "Unable to decode image from storage."
                        }
                        conn.disconnect()
                    } catch (e: Exception) {
                        loadError = "Failed to load image: ${e.message}"
                    } finally {
                        isLoadingImage = false
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { inspectCertPath = null },
            title = { Text("Certificate of Operation Preview") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Storage Path: $path", fontSize = 11.sp, color = TextOnDarkSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isImage && bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Certificate Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (isImage && isLoadingImage) {
                        CircularProgressIndicator(color = EcoGreenPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                    } else if (loadError != null) {
                        Text(loadError ?: "Error loading image", color = HazardousRed, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    var isDownloadingFile by remember { mutableStateOf(false) }
                    var fileError by remember { mutableStateOf<String?>(null) }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Document",
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPdf) "PDF Document Certificate" else "Certificate Document File",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        fileError?.let {
                            Text(it, fontSize = 11.sp, color = HazardousRed, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Button(
                            enabled = !isDownloadingFile,
                            onClick = {
                                scope.launch {
                                    isDownloadingFile = true
                                    fileError = null
                                    try {
                                        val token = HakotRepository.getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
                                        val downloadedFile: File = withContext(Dispatchers.IO) {
                                            val conn = openStorageConnection(path, token)
                                            val code = conn.responseCode
                                            if (code !in 200..299) {
                                                val errStr = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { null }
                                                conn.disconnect()
                                                throw Exception("HTTP $code: ${errStr ?: "Server returned error"}")
                                            }

                                            val ext = path.substringAfterLast('.', "dat")
                                            val file = File(context.cacheDir, "certificate_preview_${System.currentTimeMillis()}.$ext")
                                            file.outputStream().use { out ->
                                                conn.inputStream.copyTo(out)
                                            }
                                            conn.disconnect()

                                            if (!file.exists() || file.length() == 0L) {
                                                file.delete()
                                                throw Exception("Downloaded file is empty or incomplete.")
                                            }
                                            file
                                        }

                                        val mimeType = when {
                                            path.lowercase().endsWith(".pdf") -> "application/pdf"
                                            path.lowercase().endsWith(".png") -> "image/png"
                                            path.lowercase().endsWith(".jpg") || path.lowercase().endsWith(".jpeg") -> "image/jpeg"
                                            else -> "*/*"
                                        }

                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            downloadedFile
                                        )

                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(contentUri, mimeType)
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        fileError = "Failed to download/open file: ${e.message}"
                                    } finally {
                                        isDownloadingFile = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isDownloadingFile) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Opening...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    text = if (isPdf) "Open PDF Document" else "Open / View Certificate File",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { inspectCertPath = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }
}

@Composable
fun SystemOwnerPasswordResetRequestsView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val resetRequests = HakotRepository.passwordResetRequests

    fun refreshRequests() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val (ok, msg) = HakotRepository.fetchPasswordResetRequests(context)
            isLoading = false
            if (!ok) {
                errorMessage = msg
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RESET PASSWORD REQUESTS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                Text(
                    text = "Review reset password applicants and mark as resolved",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary
                )
            }

            IconButton(onClick = { refreshRequests() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = EcoGreenPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        errorMessage?.let { err ->
            Text(err, color = HazardousRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EcoGreenPrimary)
            }
        } else if (resetRequests.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending reset password requests found.",
                        fontSize = 13.sp,
                        color = TextOnDarkMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(resetRequests) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = req.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDarkPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = "Email",
                                        tint = EcoGreenLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = req.email,
                                        fontSize = 13.sp,
                                        color = TextOnDarkSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Requested: ${req.createdAtDisplay}",
                                    fontSize = 11.sp,
                                    color = TextOnDarkMuted
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val (ok, msg) = HakotRepository.resolvePasswordResetRequest(req.id, context)
                                        if (ok) {
                                            Toast.makeText(context, "Password reset request resolved and deleted.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Resolved", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginScreen(
    onOpenRegistration: () -> Unit,
    onExitAdminPortal: () -> Unit,
    onOpenRedeemModal: (() -> Unit)? = null,
    onOpenActivation: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showResetPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }
    var submittedEmailDisplay by remember { mutableStateOf("") }
    var resetNameInput by rememberSaveable { mutableStateOf("") }
    var resetGmailInput by rememberSaveable { mutableStateOf("") }
    var isSubmittingReset by remember { mutableStateOf(false) }
    var resetError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DumpTruckLogo()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HAKOT PORTAL LOGIN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                Text(
                    text = "Enter your credentials to access your assigned role",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Mail, contentDescription = "Email", tint = TextOnDarkSecondary) },
                    placeholder = { Text("user@hakot.gov.ph", color = TextOnDarkMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = TextOnDarkSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = TextOnDarkSecondary
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    placeholder = { Text("••••••••", color = TextOnDarkMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        resetNameInput = ""
                        resetGmailInput = emailInput.ifBlank { "" }
                        resetError = null
                        showResetPasswordDialog = true
                    }) {
                        Text("Reset Password", fontSize = 11.sp, color = EcoGreenLight)
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = HazardousRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val success = HakotRepository.login(context, emailInput, passwordInput)
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = HakotRepository.lastLoginError ?: "Invalid credentials, inactive account, or network error."
                                }
                            }
                        } else {
                            errorMessage = "Please enter email and password."
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Sign In",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onOpenRegistration) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Register", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Applicant? Register for Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onExitAdminPortal) {
                    Text("Return to Public Citizen Mode", fontSize = 12.sp, color = TextOnDarkMuted)
                }
            }
        }

        if (showResetPasswordDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = true) { /* Consume background click */ },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, EcoGreenPrimary, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Request Password Reset",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenLight
                        )
                        Text(
                            text = "Enter your full name and registered Gmail to submit a password reset request.",
                            fontSize = 12.sp,
                            color = TextOnDarkSecondary
                        )
                        OutlinedTextField(
                            value = resetNameInput,
                            onValueChange = { resetNameInput = it; resetError = null },
                            label = { Text("Full Name") },
                            placeholder = { Text("Juan Dela Cruz", color = TextOnDarkMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoGreenPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextOnDarkPrimary,
                                unfocusedTextColor = TextOnDarkPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = resetGmailInput,
                            onValueChange = { resetGmailInput = it; resetError = null },
                            label = { Text("Gmail / Email") },
                            placeholder = { Text("user@gmail.com", color = TextOnDarkMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EcoGreenPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextOnDarkPrimary,
                                unfocusedTextColor = TextOnDarkPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        resetError?.let { err ->
                            Text(err, color = HazardousRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showResetPasswordDialog = false }) {
                                Text("Cancel", color = TextOnDarkSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (resetNameInput.isBlank() || resetGmailInput.isBlank()) {
                                        resetError = "Please enter both your name and Gmail."
                                    } else {
                                        scope.launch {
                                            isSubmittingReset = true
                                            val (success, msg) = HakotRepository.submitPasswordResetRequest(resetNameInput, resetGmailInput, context)
                                            isSubmittingReset = false
                                            if (success) {
                                                submittedEmailDisplay = resetGmailInput.trim()
                                                resetNameInput = ""
                                                resetGmailInput = ""
                                                resetError = null
                                                showResetPasswordDialog = false
                                                showResetSuccessDialog = true
                                            } else {
                                                resetError = msg
                                            }
                                        }
                                    }
                                },
                                enabled = !isSubmittingReset,
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isSubmittingReset) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Text("Submit Request", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showResetSuccessDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = true) { /* Consume background click */ },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .border(1.dp, EcoGreenPrimary, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Request Submitted!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenLight,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Your password reset request for $submittedEmailDisplay has been submitted successfully.\n\nThe System Owner will review your request and reset your account credentials.",
                            fontSize = 12.sp,
                            color = TextOnDarkSecondary,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { showResetSuccessDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "OK",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun DriverDashboardView(onSignOut: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoadingSchedules by remember { mutableStateOf(true) }
    var showGpsBroadcastModal by remember { mutableStateOf(false) }
    var selectedScheduleDetail by remember { mutableStateOf<ScheduleItem?>(null) }
    var scheduleToStartFromAssignedConfirm by remember { mutableStateOf<ScheduleItem?>(null) }
    var scheduleToCompleteConfirm by remember { mutableStateOf<ScheduleItem?>(null) }
    var isSharingLocation by remember { mutableStateOf(false) }
    var showSharingConfirmDialog by remember { mutableStateOf(false) }
    var broadcastLiveLat by remember { mutableStateOf(14.0725) }
    var broadcastLiveLon by remember { mutableStateOf(120.6335) }
    val driver = HakotRepository.authenticatedUser

    val driverId = driver?.userId ?: "default_driver"
    val sharedPrefs = remember(context, driverId) {
        context.getSharedPreferences("hakot_driver_prefs_$driverId", Context.MODE_PRIVATE)
    }
    var hiddenCompletedFromDayViewIds by remember(driverId) {
        mutableStateOf(sharedPrefs.getStringSet("dismissed_schedules", emptySet()) ?: emptySet())
    }

    LaunchedEffect(Unit) {
        isLoadingSchedules = true
        if (driver?.municipalityOrgId?.isNotBlank() == true) {
            HakotRepository.activeOrganization = HakotRepository.activeOrganization.copy(municipalityId = driver.municipalityOrgId)
        }
        HakotRepository.fetchTrucksFromSupabase(context)
        HakotRepository.fetchSchedulesFromSupabase(context)
        isLoadingSchedules = false

        // Connect to actual device/emulator GPS location
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val loc = try {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: SecurityException) {
                    null
                }
                if (loc != null) {
                    broadcastLiveLat = loc.latitude
                    broadcastLiveLon = loc.longitude
                }

                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        1f
                    ) { location ->
                        broadcastLiveLat = location.latitude
                        broadcastLiveLon = location.longitude
                    }
                } catch (_: SecurityException) {}
            }
        } catch (e: Exception) {
            Log.e("HAKOT_GPS", "Error connecting to device location manager", e)
        }
    }

    var selectedTruckInUse by remember(driver, HakotRepository.trucks.size) {
        mutableStateOf(
            HakotRepository.trucks.find { 
                it.driverName.equals(driver?.fullName, ignoreCase = true) || 
                it.assignedDriverId == driver?.userId || 
                it.driverName.contains(driver?.fullName ?: "", ignoreCase = true)
            } ?: HakotRepository.trucks.firstOrNull()
        )
    }

    var showTruckDropdown by remember { mutableStateOf(false) }
    var driverSubTab by remember { mutableIntStateOf(0) } // 0=All, 1=Unassigned, 2=Assigned, 3=Active, 4=Completed, 5=Missed
    val zoneId = ZoneId.of("Asia/Manila")
    var selectedWeekStart by remember { mutableStateOf(LocalDate.now(zoneId).with(DayOfWeek.MONDAY)) }
    var selectedDay by remember { mutableStateOf<LocalDate?>(LocalDate.now(zoneId)) }

    val daysOfWeek = (0..6).map { selectedWeekStart.plusDays(it.toLong()) }
    val today = LocalDate.now(zoneId)

    val allDriverSchedules = HakotRepository.schedules.filter { sch ->
        sch.operational
    }.sortedWith(compareBy({ it.date }, { it.startTime }))

    val unassignedSchedules = allDriverSchedules.filter { 
        val st = it.status.uppercase()
        (st == "DRAFT" || st == "SCHEDULED" || st.isBlank() || it.truckAssigned.contains("No assigned", ignoreCase = true) || it.truckAssigned.contains("Unassigned", ignoreCase = true)) && st != "ASSIGNED"
    }
    val assignedSchedules = allDriverSchedules.filter { 
        it.status.equals("ASSIGNED", ignoreCase = true)
    }
    val activeDriverSchedules = allDriverSchedules.filter { 
        val st = it.status.uppercase()
        st == "ACTIVE" || st == "ON_HOLD" || st == "HOLD" || st == "ON HOLD"
    }
    val completedDriverSchedules = allDriverSchedules.filter { 
        val st = it.status.uppercase()
        st == "COMPLETED" || st == "DONE" || st == "COMPLETE"
    }
    val missedDriverSchedules = allDriverSchedules.filter { 
        val st = it.status.uppercase()
        st == "MISSED" || (it.date.isBefore(today) && !completedDriverSchedules.contains(it) && !it.isWeeklyRepeat)
    }

    val currentFilterTitle = when (driverSubTab) {
        1 -> "DRAFT"
        2 -> "ASSIGNED"
        3 -> "ACTIVE"
        4 -> "COMPLETED"
        5 -> "MISSED"
        else -> if (selectedDay != null) "FOR $selectedDay" else "ALL SCHEDULES"
    }

       val targetDriverDay = selectedDay ?: today
    val driverSchedules = allDriverSchedules.filter { sch ->
        val matchesDay = if (driverSubTab != 0) {
            true
        } else {
            HakotRepository.isScheduleActiveOnDate(sch, targetDriverDay) && !hiddenCompletedFromDayViewIds.contains(sch.id)
        }
        val matchesTab = when (driverSubTab) {
            1 -> unassignedSchedules.contains(sch)
            2 -> assignedSchedules.contains(sch)
            3 -> activeDriverSchedules.contains(sch)
            4 -> completedDriverSchedules.contains(sch)
            5 -> missedDriverSchedules.contains(sch)
            else -> true
        }
        matchesDay && matchesTab
    }.filterNot { sch ->
        sch.isWeeklyRepeat && allDriverSchedules.any { other ->
            !other.isWeeklyRepeat &&
            other.date == targetDriverDay &&
            (other.routeName == "${sch.routeName} ($targetDriverDay)" || other.routeName == sch.routeName || other.routeName.startsWith(sch.routeName))
        }
    }

    val unassignedCount = unassignedSchedules.size
    val assignedCount = assignedSchedules.size
    val activeCount = activeDriverSchedules.size
    val completedCount = completedDriverSchedules.size
    val missedCount = missedDriverSchedules.size

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        // Floating Mini Island Live GPS Notification Banner
        if (isSharingLocation) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HazardousRed, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(HazardousRed, CircleShape)
                            )
                            Column {
                                Text(
                                    text = "🔴 LIVE GPS LOCATION BROADCASTING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HazardousRed,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Lat: ${String.format("%.4f", broadcastLiveLat)}° N • Lon: ${String.format("%.4f", broadcastLiveLon)}° E",
                                    fontSize = 10.sp,
                                    color = TextOnDarkSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    isSharingLocation = false
                                    if (driver?.userId != null) {
                                        HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, false, context)
                                    }
                                    Toast.makeText(context, "Stopped live GPS location sharing", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Stop Sharing",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // Compact Sleek Header Island
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TRUCK DRIVER PORTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text(
                                text = driver?.fullName ?: "Driver",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnDarkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val hasAssignedOrActiveSchedule = HakotRepository.schedules.any { sch ->
                            sch.operational && (sch.status.equals("ASSIGNED", ignoreCase = true) || sch.status.equals("ACTIVE", ignoreCase = true))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { showGpsBroadcastModal = true },
                                enabled = hasAssignedOrActiveSchedule,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EcoGreenPrimary,
                                    disabledContainerColor = DarkSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Broadcast GPS",
                                    color = if (hasAssignedOrActiveSchedule) Color.Black else TextOnDarkSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Button(
                                onClick = onSignOut,
                                colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Sign Out",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Vehicle Dropdown Selector
                    Box {
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, EcoGreenPrimary),
                            modifier = Modifier.fillMaxWidth().clickable { showTruckDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Vehicle: ${selectedTruckInUse?.truckCode ?: "Select Truck"} (${selectedTruckInUse?.plateNumber ?: "N/A"})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDarkPrimary
                                )
                                Text("▼", fontSize = 10.sp, color = EcoGreenLight)
                            }
                        }

                        DropdownMenu(
                            expanded = showTruckDropdown,
                            onDismissRequest = { showTruckDropdown = false }
                        ) {
                            HakotRepository.trucks.forEach { tr ->
                                DropdownMenuItem(
                                    text = { Text("${tr.truckCode} (${tr.plateNumber})", fontSize = 11.sp) },
                                    onClick = {
                                        selectedTruckInUse = tr
                                        showTruckDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Status Metrics Bar (Unassigned, Assigned, Active, Completed, Missed)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MetricCard(
                    title = "Draft",
                    value = "$unassignedCount",
                    color = TextOnDarkPrimary,
                    modifier = Modifier.weight(1f).clickable { driverSubTab = if (driverSubTab == 1) 0 else 1 }
                )
                MetricCard(
                    title = "Assigned",
                    value = "$assignedCount",
                    color = EcoGreenLight,
                    modifier = Modifier.weight(1f).clickable { driverSubTab = if (driverSubTab == 2) 0 else 2 }
                )
                MetricCard(
                    title = "Active",
                    value = "$activeCount",
                    color = EcoGreenPrimary,
                    modifier = Modifier.weight(1f).clickable { driverSubTab = if (driverSubTab == 3) 0 else 3 }
                )
                MetricCard(
                    title = "Completed",
                    value = "$completedCount",
                    color = EcoGreenLight,
                    modifier = Modifier.weight(1f).clickable { driverSubTab = if (driverSubTab == 4) 0 else 4 }
                )
                MetricCard(
                    title = "Missed",
                    value = "$missedCount",
                    color = HazardousRed,
                    modifier = Modifier.weight(1f).clickable { driverSubTab = if (driverSubTab == 5) 0 else 5 }
                )
            }
        }

        // WEEKLY CALENDAR WIDGET FOR TRUCKER (Pic 3)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedWeekStart = selectedWeekStart.minusWeeks(1) }) {
                            Text("◀ Prev Week", fontSize = 11.sp, color = EcoGreenLight)
                        }
                        Text("Week of ${selectedWeekStart}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                        TextButton(onClick = { selectedWeekStart = selectedWeekStart.plusWeeks(1) }) {
                            Text("Next Week ▶", fontSize = 11.sp, color = EcoGreenLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        daysOfWeek.forEach { day ->
                            val isSelected = selectedDay == day
                            val hasSchedules = HakotRepository.schedules.any {
                                it.operational && HakotRepository.isScheduleActiveOnDate(it, day)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EcoGreenPrimary else if (hasSchedules) EcoGreenContainer else DarkSurface)
                                    .clickable { 
                                        selectedDay = if (isSelected) null else day 
                                        driverSubTab = 0
                                    }
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(day.dayOfWeek.name.take(3), fontSize = 10.sp, color = if (isSelected) Color.Black else TextOnDarkSecondary)
                                Text("${day.dayOfMonth}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else TextOnDarkPrimary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("DRIVER COLLECTION SCHEDULES — VIEWING $currentFilterTitle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkMuted)
        }

        // Schedules List or Loading State
        if (isLoadingSchedules) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = EcoGreenPrimary, strokeWidth = 2.dp)
                        Text("Loading assigned collection schedules...", fontSize = 11.sp, color = TextOnDarkSecondary)
                    }
                }
            }
        } else if (driverSchedules.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No $currentFilterTitle collection schedules found.", fontSize = 12.sp, color = TextOnDarkSecondary, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(driverSchedules) { sch ->
                val targetDateStr = (selectedDay ?: LocalDate.now()).toString()
                val effectiveStatus = if (sch.isWeeklyRepeat) sch.dateStatuses[targetDateStr] ?: "DRAFT" else sch.status
                val isActive = effectiveStatus.equals("ACTIVE", ignoreCase = true)
                val isOnHold = effectiveStatus.equals("ON_HOLD", ignoreCase = true) || effectiveStatus.equals("HOLD", ignoreCase = true)
                val isCompleted = effectiveStatus.equals("COMPLETED", ignoreCase = true) || effectiveStatus.equals("DONE", ignoreCase = true) || sch.completedDates.contains(targetDateStr)

                val isUnassigned = sch.truckAssigned.isBlank() || 
                    sch.truckAssigned.contains("No assigned", ignoreCase = true) || 
                    sch.truckAssigned.contains("Unassigned", ignoreCase = true) ||
                    sch.truckAssigned.contains("Dump Truck", ignoreCase = true)

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedScheduleDetail = sch }
                        .border(1.dp, if (isActive) EcoGreenPrimary else if (isOnHold) RecycleYellow else DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sch.routeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            
                            // Status Badge
                            Surface(
                                color = if (isActive) EcoGreenContainer else if (isOnHold) DarkSurface else DarkSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isActive) EcoGreenPrimary else if (isOnHold) RecycleYellow else TextOnDarkSecondary)
                            ) {
                                Text(
                                    text = sch.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) EcoGreenLight else if (isOnHold) RecycleYellow else if (isCompleted) EcoGreenPrimary else TextOnDarkSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Barangay: ${sch.barangay}", fontSize = 11.sp, color = EcoGreenLight)
                        Text("Date: ${sch.date} • Time: ${sch.startTime} - ${sch.endTime}", fontSize = 10.sp, color = TextOnDarkSecondary)
                        val truckText = if (isUnassigned) "No assigned truck yet" else sch.truckAssigned
                        Text("Assigned Truck: $truckText", fontSize = 10.sp, color = TextOnDarkSecondary)

                        val assignedDriverName = if (sch.truckAssigned.contains("Driver:", ignoreCase = true)) {
                            sch.truckAssigned.substringAfter("Driver:").trim()
                        } else ""

                        val isAssignedToSomeoneElse = assignedDriverName.isNotBlank() &&
                            !assignedDriverName.equals("Unassigned", ignoreCase = true) &&
                            !assignedDriverName.equals("No assigned", ignoreCase = true) &&
                            driver?.fullName != null &&
                            !assignedDriverName.equals(driver.fullName, ignoreCase = true)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAssignedToSomeoneElse) {
                                Text("Assigned to $assignedDriverName", fontSize = 10.sp, color = RecycleYellow, fontWeight = FontWeight.Bold)
                            } else if (isUnassigned || sch.status.equals("DRAFT", ignoreCase = true)) {
                                // Assign Myself Button (DRAFT -> ASSIGNED or Clone Instance for Weekly)
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val truckInfo = if (selectedTruckInUse != null) {
                                                "${selectedTruckInUse?.plateNumber} (${selectedTruckInUse?.truckCode}) - Driver: ${driver?.fullName ?: "Driver"}"
                                            } else "Driver: ${driver?.fullName ?: "Driver"}"

                                            if (sch.isWeeklyRepeat && selectedDay != null) {
                                                // Create a single-date clone instance in Supabase!
                                                val targetDate = selectedDay!!
                                                val cloneUuid = UUID.randomUUID().toString()
                                                val cloneRouteName = "${sch.routeName} ($targetDate)"
                                                val clonedSchedule = sch.copy(
                                                    id = cloneUuid,
                                                    routeName = cloneRouteName,
                                                    date = targetDate,
                                                    isWeeklyRepeat = false,
                                                    status = "ASSIGNED",
                                                    truckAssigned = truckInfo
                                                )
                                                val saveRes = HakotRepository.saveScheduledRouteToSupabase(clonedSchedule, context)
                                                if (saveRes.first) {
                                                    HakotRepository.fetchSchedulesFromSupabase(context)
                                                    Toast.makeText(context, "Assigned to route for $targetDate! Created instance $cloneRouteName.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Error creating instance: ${saveRes.second}", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                val updated = sch.copy(status = "ASSIGNED", truckAssigned = truckInfo)
                                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                                if (success) {
                                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                                    if (idx != -1) {
                                                        HakotRepository.schedules[idx] = updated
                                                        val temp = HakotRepository.schedules.toList()
                                                        HakotRepository.schedules.clear()
                                                        HakotRepository.schedules.addAll(temp)
                                                    }
                                                    Toast.makeText(context, "Schedule assigned! Status is now ASSIGNED.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Assign Myself", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else if (sch.status.equals("ASSIGNED", ignoreCase = true) || effectiveStatus.equals("ASSIGNED", ignoreCase = true)) {
                                Button(
                                    onClick = { scheduleToStartFromAssignedConfirm = sch },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Start Collection", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (!sch.isWeeklyRepeat && sch.routeName.contains("(")) {
                                                // Delete the cloned instance from DB so it reverts to master template!
                                                val success = HakotRepository.deleteScheduleFromSupabase(sch.id, context)
                                                if (success) {
                                                    HakotRepository.schedules.removeIf { it.id == sch.id }
                                                    Toast.makeText(context, "Unassigned. Reverted to master weekly schedule.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                val updated = sch.copy(status = "DRAFT", truckAssigned = "No assigned truck yet")
                                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                                if (success) {
                                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                                    if (idx != -1) {
                                                        HakotRepository.schedules[idx] = updated
                                                        val temp = HakotRepository.schedules.toList()
                                                        HakotRepository.schedules.clear()
                                                        HakotRepository.schedules.addAll(temp)
                                                    }
                                                    Toast.makeText(context, "Unassigned successfully. Status is back to DRAFT.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Unassign Myself", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Toggle Start / Hold Button
                                Button(
                                    onClick = {
                                        if (isActive) {
                                            scope.launch {
                                                val updated = sch.copy(status = "ON_HOLD")
                                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                                if (success) {
                                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                                    if (idx != -1) {
                                                        HakotRepository.schedules[idx] = updated
                                                        val temp = HakotRepository.schedules.toList()
                                                        HakotRepository.schedules.clear()
                                                        HakotRepository.schedules.addAll(temp)
                                                    }
                                                    Toast.makeText(context, "Status updated to ON_HOLD", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } else {
                                            scheduleToStartFromAssignedConfirm = sch
                                        }
                                    },
                                    enabled = !isCompleted,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isActive) RecycleYellow else EcoGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (isActive) "Hold Collection" else "Start Collection", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Complete / Done Button
                                Button(
                                    onClick = { scheduleToCompleteConfirm = sch },
                                    enabled = !isCompleted,
                                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (isCompleted) "Completed" else "Complete Collection", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                if (isCompleted && driverSubTab == 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    TextButton(
                                        onClick = {
                                            val newSet = hiddenCompletedFromDayViewIds + sch.id
                                            hiddenCompletedFromDayViewIds = newSet
                                            sharedPrefs.edit().putStringSet("dismissed_schedules", newSet).apply()
                                            Toast.makeText(context, "Hidden from day view (still visible in Completed filter)", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("Hide ✕", fontSize = 10.sp, color = TextOnDarkSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    scheduleToStartFromAssignedConfirm?.let { sch ->
        AlertDialog(
            onDismissRequest = { scheduleToStartFromAssignedConfirm = null },
            title = { Text("Start Collection & Live Location", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to start collection for route '${sch.routeName}'? Would you also like to start live GPS location broadcasting for this route?", color = TextOnDarkSecondary) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val updated = sch.copy(status = "ACTIVE")
                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                if (success) {
                                    isSharingLocation = true
                                    if (driver?.userId != null) {
                                        HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, true, context)
                                    }
                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                    if (idx != -1) {
                                        HakotRepository.schedules[idx] = updated
                                        val temp = HakotRepository.schedules.toList()
                                        HakotRepository.schedules.clear()
                                        HakotRepository.schedules.addAll(temp)
                                    }
                                    Toast.makeText(context, "Started collection & live location broadcasting!", Toast.LENGTH_SHORT).show()
                                }
                                scheduleToStartFromAssignedConfirm = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start with Live Location", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val updated = sch.copy(status = "ACTIVE")
                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                if (success) {
                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                    if (idx != -1) {
                                        HakotRepository.schedules[idx] = updated
                                        val temp = HakotRepository.schedules.toList()
                                        HakotRepository.schedules.clear()
                                        HakotRepository.schedules.addAll(temp)
                                    }
                                    Toast.makeText(context, "Started collection (Location sharing off)", Toast.LENGTH_SHORT).show()
                                }
                                scheduleToStartFromAssignedConfirm = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start without Live Location", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { scheduleToStartFromAssignedConfirm = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextOnDarkSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {},
            containerColor = DarkSurfaceCard
        )
    }

    scheduleToCompleteConfirm?.let { sch ->
        val targetDateStr = (selectedDay ?: LocalDate.now()).toString()
        AlertDialog(
            onDismissRequest = { scheduleToCompleteConfirm = null },
            title = { Text("Confirm Complete Collection", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to mark collection route '${sch.routeName}' as completed for $targetDateStr?", color = TextOnDarkSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val baseRouteName = sch.routeName.substringBefore(" (").trim()
                            HakotRepository.markWeeklyDateCompletedLocally(baseRouteName, targetDateStr)
                            HakotRepository.markWeeklyDateCompletedLocally(sch.routeName, targetDateStr)

                            val parentMaster = HakotRepository.schedules.find { it.isWeeklyRepeat && (it.routeName == baseRouteName || it.routeName.equals(baseRouteName, ignoreCase = true)) }
                            if (parentMaster != null && !parentMaster.completedDates.contains(targetDateStr)) {
                                val updatedParent = parentMaster.copy(completedDates = (parentMaster.completedDates + targetDateStr).distinct())
                                HakotRepository.updateScheduleInSupabase(updatedParent, context)
                            }

                            val newCompletedDates = (sch.completedDates + targetDateStr).distinct()
                            val updated = sch.copy(status = "COMPLETED", completedDates = newCompletedDates)
                            val success = HakotRepository.updateScheduleInSupabase(updated, context)
                            if (success) {
                                val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                if (idx != -1) {
                                    HakotRepository.schedules[idx] = updated
                                    val temp = HakotRepository.schedules.toList()
                                    HakotRepository.schedules.clear()
                                    HakotRepository.schedules.addAll(temp)
                                }
                                val remainingActive = HakotRepository.schedules.filter { s ->
                                    s.id != sch.id && (s.status == "ACTIVE" || s.status == "ON_HOLD" || s.status == "IN_PROGRESS") &&
                                    (s.truckAssigned.contains(driver?.fullName ?: "", ignoreCase = true) || s.truckAssigned.contains(driver?.email ?: "", ignoreCase = true))
                                }
                                if (remainingActive.isEmpty()) {
                                    isSharingLocation = false
                                    if (driver?.userId != null) {
                                        HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, false, context)
                                    }
                                }
                                Toast.makeText(context, "Collection marked as COMPLETED for $targetDateStr!", Toast.LENGTH_SHORT).show()
                            }
                            scheduleToCompleteConfirm = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Confirm & Complete", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToCompleteConfirm = null }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    var syncCountdown by remember { mutableIntStateOf(30) }

    if (showGpsBroadcastModal) {
        // 30-second periodic database sync and live countdown when sharing is active
        LaunchedEffect(isSharingLocation) {
            if (isSharingLocation) {
                syncCountdown = 30
            }
            while (isSharingLocation) {
                delay(1_000L)
                if (syncCountdown > 1) {
                    syncCountdown--
                } else {
                    syncCountdown = 30
                    if (driver?.userId != null) {
                        HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, true, context)
                        Log.d("HAKOT_GPS", "30s Periodic location recalculate & sync: lat=$broadcastLiveLat, lon=$broadcastLiveLon")
                    }
                }
            }
        }

        val currentGpsPoint = GeoPoint(broadcastLiveLat, broadcastLiveLon)
        val currentGpsList = listOf(currentGpsPoint)

        Dialog(
            onDismissRequest = { showGpsBroadcastModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(12.dp),
                color = DarkBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, EcoGreenPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("LIVE GPS BROADCAST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text("Phone Current Location (Live)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                        }
                        IconButton(onClick = { showGpsBroadcastModal = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        DarkMapView(
                            modifier = Modifier.fillMaxSize(),
                            customRoutePoints = currentGpsList,
                            autoCenterSinglePoint = true
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, if (isSharingLocation) HazardousRed else EcoGreenPrimary, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Vehicle: ${selectedTruckInUse?.truckCode ?: "Truck"} (${selectedTruckInUse?.plateNumber ?: "N/A"})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            Text("Operator: ${driver?.fullName ?: "Driver"}", fontSize = 11.sp, color = TextOnDarkSecondary)
                            Text(
                                text = if (isSharingLocation) "🔴 Status: GPS Active & Broadcasting Live (Next sync in ${syncCountdown}s)" else "🟢 Status: GPS Active & Ready (Not Sharing)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSharingLocation) HazardousRed else EcoGreenLight
                            )
                            Text("Latitude: ${String.format("%.4f", broadcastLiveLat)}° N • Longitude: ${String.format("%.4f", broadcastLiveLon)}° E", fontSize = 11.sp, color = TextOnDarkPrimary)
                            Text("Location Source: Live Device/Emulator GPS", fontSize = 10.sp, color = TextOnDarkSecondary)
                        }
                    }

                    Button(
                        onClick = {
                            if (isSharingLocation) {
                                scope.launch {
                                    isSharingLocation = false
                                    if (driver?.userId != null) {
                                        HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, false, context)
                                    }
                                    Toast.makeText(context, "Stopped sharing live location", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                showSharingConfirmDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSharingLocation) HazardousRed else EcoGreenPrimary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            if (isSharingLocation) "Stop Sharing Location" else "Start Sharing Location",
                            color = if (isSharingLocation) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showGpsBroadcastModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Location View", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showSharingConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSharingConfirmDialog = false },
            title = { Text("Confirm Start Sharing GPS", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to start sharing your live GPS coordinates with the database?", color = TextOnDarkSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSharingLocation = true
                            if (driver?.userId != null) {
                                HakotRepository.updateTruckerLocationInSupabase(driver.userId, broadcastLiveLat, broadcastLiveLon, true, context)
                            }
                            Toast.makeText(context, "Started sharing live GPS coordinates to database!", Toast.LENGTH_SHORT).show()
                            showSharingConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Confirm & Start", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSharingConfirmDialog = false }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    selectedScheduleDetail?.let { sch ->
        val routeSegmentsList = remember(sch, GisManager.roadSegments) {
            sch.orderedSegments.mapNotNull { seg ->
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
                if (points.size >= 2) points else null
            }
        }
        val routeGeoPoints = remember(routeSegmentsList) { routeSegmentsList.flatten() }

        Dialog(
            onDismissRequest = { selectedScheduleDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                color = DarkBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sch.routeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text("Barangay: ${sch.barangay} • Date: ${sch.date}", fontSize = 11.sp, color = TextOnDarkSecondary)
                        }
                        IconButton(onClick = { selectedScheduleDetail = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        DarkMapView(
                            modifier = Modifier.fillMaxSize(),
                            customRoutePoints = routeGeoPoints,
                            customRouteSegments = routeSegmentsList
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("SCHEDULE & ROUTE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        Text("Collection Date: ${sch.date}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("Time Window: ${sch.startTime} - ${sch.endTime}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        
                        val truckDisplay = if (sch.truckAssigned.isBlank() || sch.truckAssigned.contains("Unassigned", ignoreCase = true) || sch.truckAssigned.contains("No assigned", ignoreCase = true) || sch.truckAssigned.contains("Dump Truck", ignoreCase = true)) "No assigned truck yet" else sch.truckAssigned
                        Text("Assigned Truck: $truckDisplay", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("Assigned Dumpsite: ${sch.dumpsiteName}", fontSize = 12.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
                        Text("Status: ${sch.status}", fontSize = 12.sp, color = TextOnDarkPrimary)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("ROUTE CORRIDOR SEGMENTS (${sch.orderedSegments.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)

                        if (sch.orderedSegments.isEmpty()) {
                            Text("No segment details stored.", fontSize = 11.sp, color = TextOnDarkSecondary)
                        } else {
                            sch.orderedSegments.forEachIndexed { idx, seg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${idx + 1}. ${seg.segmentName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                            Text("Type: ${seg.roadType}", fontSize = 10.sp, color = TextOnDarkSecondary)
                                        }
                                        Text(
                                            if (seg.isReversed) "Reversed" else "Forward",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoGreenLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = TextOnDarkMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DistrictOverviewView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val admin = HakotRepository.currentDistrictAdmin
    val org = HakotRepository.activeOrganization
    val realName = HakotRepository.authenticatedUser?.fullName?.ifBlank { HakotRepository.currentAdminName }?.ifBlank { admin.fullName }

    var selectedMapDate by remember { mutableStateOf(LocalDate.now(ZoneId.systemDefault())) }

    LaunchedEffect(Unit) {
        scope.launch {
            HakotRepository.fetchSchedulesFromSupabase(context)
            HakotRepository.fetchTrucksFromSupabase(context)
        }
    }

    val activeSchedules = HakotRepository.schedules.count { sch ->
        !sch.isDisabled && (sch.status.equals("ACTIVE", ignoreCase = true) || sch.status.equals("ACTIVE_ON_ROUTE", ignoreCase = true))
    }
    val completedSchedules = HakotRepository.schedules.count { it.status.equals("COMPLETED", true) || it.completedDates.isNotEmpty() }
    val missedSchedules = HakotRepository.schedules.count { it.status.equals("MISSED", true) || !it.operational }

    val resolvedMunicipality = admin.municipalityName.ifBlank { org.assignedMunicipality }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account Information",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcoGreenContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(admin.status.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Name: $realName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary
                    )
                    Text(
                        text = "Municipality: $resolvedMunicipality",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EcoGreenLight
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(title = "Active Schedules", value = "$activeSchedules", color = EcoGreenPrimary, modifier = Modifier.weight(1f))
                MetricCard(title = "Completed Schedules", value = "$completedSchedules", color = EcoGreenLight, modifier = Modifier.weight(1f))
                MetricCard(title = "Missed Schedules", value = "$missedSchedules", color = HazardousRed, modifier = Modifier.weight(1f))
            }
        }

        item {
            val today = LocalDate.now(ZoneId.systemDefault())
            val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
            val isTodaySelected = selectedMapDate == today
            val mapTitle = if (isTodaySelected) "TODAY'S COLLECTION ROUTES MAP" else "COLLECTION ROUTES MAP (${selectedMapDate.format(dateFmt).uppercase()})"

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mapTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextOnDarkMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            ) {
                val activeSchedulesForDate = HakotRepository.schedules.filter { sch ->
                    !sch.isDisabled && (sch.date == selectedMapDate || HakotRepository.isScheduleActiveOnDate(sch, selectedMapDate))
                }
                val routeSegmentsList = mutableListOf<List<GeoPoint>>()
                activeSchedulesForDate.forEach { schedule ->
                    val segsList = mutableListOf<List<GeoPoint>>()
                    if (schedule.orderedSegments.isNotEmpty()) {
                        schedule.orderedSegments.forEach { seg ->
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
                        val r = HakotRepository.routes.find { it.routeName.equals(schedule.routeName, ignoreCase = true) }
                        val rPts = r?.geoPoints?.mapNotNull { (lng, lat) ->
                            if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                        } ?: emptyList()
                        if (rPts.size >= 2) {
                            segsList.add(rPts)
                        } else {
                            val bgyRoads = GisManager.getRoadSegmentsInBarangay(schedule.barangay)
                            bgyRoads.forEach { seg ->
                                val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
                                val points = rawCoords.mapNotNull { (lng, lat) ->
                                    if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
                                }
                                if (points.size >= 2) segsList.add(points)
                            }
                        }
                    }
                    routeSegmentsList.addAll(segsList)
                }
                val routePoints = routeSegmentsList.flatten()

                DarkMapView(
                    modifier = Modifier.fillMaxSize(),
                    customRoutePoints = routePoints,
                    customRouteSegments = routeSegmentsList,
                    showDefaultSchedulesWhenEmpty = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Select Scheduled Day to View Routes:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            val allNext7Days = (0..6).map { today.plusDays(it.toLong()) }
            val daysWithSchedulesList = remember(HakotRepository.schedules.size) {
                val filtered = allNext7Days.filter { d ->
                    HakotRepository.schedules.any { sch ->
                        !sch.isDisabled && (sch.date == d || HakotRepository.isScheduleActiveOnDate(sch, d))
                    }
                }
                filtered.ifEmpty { listOf(today) }
            }

            LaunchedEffect(daysWithSchedulesList, selectedMapDate) {
                HakotRepository.selectedFocusSchedule = null
                HakotRepository.selectedMapDate = selectedMapDate
                if (daysWithSchedulesList.isNotEmpty() && !daysWithSchedulesList.contains(selectedMapDate)) {
                    selectedMapDate = daysWithSchedulesList.first()
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(38.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(daysWithSchedulesList) { dateItem ->
                        val isSelected = selectedMapDate == dateItem
                        val label = if (dateItem == today) "Today" else dateItem.format(dateFmt)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedMapDate = dateItem
                                HakotRepository.selectedMapDate = dateItem
                                HakotRepository.selectedFocusSchedule = null
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EcoGreenContainer,
                                selectedLabelColor = EcoGreenLight,
                                containerColor = DarkSurfaceCard,
                                labelColor = TextOnDarkSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun AdminAccountManagementView(
    onOpenCreateModal: () -> Unit,
    onNavigateToApplications: () -> Unit = {},
    onNavigateToResetRequests: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<AdminStatus?>(null) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    var selectedAccountForDetails by remember { mutableStateOf<AdminUserProfile?>(null) }
    var selectedAccountForDelete by remember { mutableStateOf<AdminUserProfile?>(null) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var selectedInvitationForDetails by remember { mutableStateOf<AccountInvitation?>(null) }

    LaunchedEffect(Unit) {
        scope.launch { HakotRepository.fetchAdminProfiles(context) }
        scope.launch { HakotRepository.fetchAccountApplications() }
        scope.launch { HakotRepository.fetchPasswordResetRequests(context) }
    }

    val currentAdmin = HakotRepository.currentDistrictAdmin
    val municipalProfiles = HakotRepository.adminProfiles.filter { prof ->
        prof.role != UserRole.SYSTEM_OWNER && (
            HakotRepository.currentAdminRole == UserRole.SYSTEM_OWNER ||
            (prof.municipalityOrgId.isNotBlank() && prof.municipalityOrgId == currentAdmin.municipalityOrgId) ||
            (prof.municipalityName.isNotBlank() && prof.municipalityName.equals(currentAdmin.municipalityName, ignoreCase = true)) ||
            currentAdmin.municipalityOrgId.isBlank()
        )
    }

    val totalAdmins = municipalProfiles.size
    val activeCount = municipalProfiles.count { it.status == AdminStatus.ACTIVE }
    val pendingCount = HakotRepository.accountInvitations.count { it.status == AdminStatus.PENDING_INVITATION }
    val suspendedCount = municipalProfiles.count { it.status == AdminStatus.SUSPENDED }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                if (HakotRepository.currentAdminRole == UserRole.SYSTEM_OWNER) {
                    val applicantsCount = HakotRepository.accountApplications.size
                    val activeAccountsCount = HakotRepository.adminProfiles.count { it.status == AdminStatus.ACTIVE && it.role != UserRole.SYSTEM_OWNER }
                    val resetRequestsCount = HakotRepository.passwordResetRequests.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            onClick = onNavigateToApplications,
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Applicants", fontSize = 11.sp, color = TextOnDarkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$applicantsCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RecycleYellow)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Active Accounts", fontSize = 11.sp, color = TextOnDarkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$activeAccountsCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            }
                        }

                        Card(
                            onClick = onNavigateToResetRequests,
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Reset Requests", fontSize = 11.sp, color = TextOnDarkSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$resetRequestsCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HazardousRed)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(title = "Total Admins", value = "$totalAdmins", color = EcoGreenPrimary, modifier = Modifier.weight(1f))
                        MetricCard(title = "Active Accounts", value = "$activeCount", color = EcoGreenLight, modifier = Modifier.weight(1f))
                        MetricCard(title = "Pending Invites", value = "$pendingCount", color = RecycleYellow, modifier = Modifier.weight(1f))
                        MetricCard(title = "Suspended", value = "$suspendedCount", color = HazardousRed, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search accounts by name, email, or position...", color = TextOnDarkMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextOnDarkSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val filteredProfiles = municipalProfiles.filter { prof ->
                val matchesSearch = searchQuery.isBlank() ||
                        prof.fullName.contains(searchQuery, ignoreCase = true) ||
                        prof.email.contains(searchQuery, ignoreCase = true) ||
                        prof.positionTitle.contains(searchQuery, ignoreCase = true) ||
                        prof.officeDepartment.contains(searchQuery, ignoreCase = true)

                val matchesRole = selectedRoleFilter == null || prof.role == selectedRoleFilter
                val matchesStatus = selectedStatusFilter == null || prof.status == selectedStatusFilter

                matchesSearch && matchesRole && matchesStatus
            }

            if (filteredProfiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No administrator accounts match your criteria.",
                            fontSize = 12.sp,
                            color = TextOnDarkMuted
                        )
                    }
                }
            } else {
                items(filteredProfiles) { prof ->
                    AccountCardItem(
                        profile = prof,
                        onClick = { selectedAccountForDetails = prof },
                        onDelete = { selectedAccountForDelete = prof }
                    )
                }
            }
        }

        if (selectedAccountForDetails != null) {
            val currentProfile = selectedAccountForDetails!!
            AccountDetailsModal(
                profile = currentProfile,
                onDismiss = { selectedAccountForDetails = null },
                onDeleteRequest = {
                    selectedAccountForDetails = null
                    selectedAccountForDelete = currentProfile
                }
            )
        }

        if (selectedAccountForDelete != null) {
            val targetProf = selectedAccountForDelete!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = true) { selectedAccountForDelete = null },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .border(1.dp, HazardousRed, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) { /* Consume clicks */ }
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Confirm Account Deletion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HazardousRed
                        )

                        Text(
                            text = "Are you sure you want to permanently delete the administrator account for ${targetProf.fullName} (${targetProf.email})?\n\nThis action will permanently remove the account from the database and cannot be undone.",
                            fontSize = 12.sp,
                            color = TextOnDarkSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { selectedAccountForDelete = null },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("No, Cancel", color = TextOnDarkSecondary, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        isDeletingAccount = true
                                        val (ok, msg) = HakotRepository.deleteAdminProfile(targetProf.userId, targetProf.email, context)
                                        isDeletingAccount = false
                                        selectedAccountForDelete = null
                                        if (ok) {
                                            Toast.makeText(context, "Account deleted successfully from database.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error deleting account: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isDeletingAccount,
                                colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                if (isDeletingAccount) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCardItem(
    profile: AdminUserProfile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EcoGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "User Avatar",
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.fullName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (profile.status) {
                                        AdminStatus.ACTIVE -> EcoGreenContainer
                                        AdminStatus.SUSPENDED -> Color(0xFF450A0A)
                                        else -> DarkSurfaceVariant
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = profile.status.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (profile.status) {
                                    AdminStatus.ACTIVE -> EcoGreenLight
                                    AdminStatus.SUSPENDED -> HazardousRed
                                    else -> TextOnDarkMuted
                                }
                            )
                        }
                    }

                    Text(
                        text = "${profile.role.label} • ${profile.positionTitle}",
                        fontSize = 11.sp,
                        color = EcoGreenLight
                    )
                    Text(
                        text = "${profile.email} • ${profile.municipalityName}",
                        fontSize = 10.sp,
                        color = TextOnDarkSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("View Full Details", fontSize = 11.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



@Composable
fun InvitationCardItem(
    invitation: AccountInvitation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mail,
                    contentDescription = "Invite",
                    tint = RecycleYellow,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = invitation.intendedName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDarkPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3A2E00))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = invitation.status.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = RecycleYellow
                        )
                    }
                }

                Text(
                    text = "Role: ${invitation.assignedRole.label} • ${invitation.positionTitle}",
                    fontSize = 11.sp,
                    color = RecycleYellow
                )
                Text(
                    text = "Recipient: ${invitation.intendedEmail} • ${invitation.municipalityName}",
                    fontSize = 10.sp,
                    color = TextOnDarkSecondary
                )
            }
        }
    }
}



// ----------------------------------------------------------------------------
// CREATE ACCOUNT INVITATION FORM MODAL
// ----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountModal(
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = HakotRepository.authenticatedUser ?: HakotRepository.currentDistrictAdmin
    val callerRole = currentUser.role

    var fullNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var positionInput by remember { mutableStateOf("") }
    var departmentInput by remember { mutableStateOf("") }
    var selectedRole by remember {
        mutableStateOf(
            if (callerRole == UserRole.SYSTEM_OWNER) UserRole.MUNICIPAL_ADMIN else UserRole.MUNICIPAL_STAFF
        )
    }
    var municipalityInput by remember { mutableStateOf(currentUser.municipalityName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Invite Municipal Account via Email",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )
            Text(
                text = "Send an email invitation. The recipient sets up their password upon account activation.",
                fontSize = 11.sp,
                color = TextOnDarkSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Select Role to Assign:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (callerRole == UserRole.SYSTEM_OWNER) {
                    FilterChip(
                        selected = selectedRole == UserRole.MUNICIPAL_ADMIN,
                        onClick = { selectedRole = UserRole.MUNICIPAL_ADMIN },
                        label = { Text("Municipal Admin", fontSize = 11.sp) }
                    )
                }

                if (callerRole == UserRole.SYSTEM_OWNER || callerRole == UserRole.MUNICIPAL_ADMIN || callerRole == UserRole.DISTRICT_ADMIN) {
                    FilterChip(
                        selected = selectedRole == UserRole.MUNICIPAL_STAFF,
                        onClick = { selectedRole = UserRole.MUNICIPAL_STAFF },
                        label = { Text("Municipal Staff", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.DRIVER,
                        onClick = { selectedRole = UserRole.DRIVER },
                        label = { Text("Driver / Operator", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = fullNameInput,
                onValueChange = { fullNameInput = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Contact Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = municipalityInput,
                onValueChange = { if (callerRole == UserRole.SYSTEM_OWNER) municipalityInput = it },
                label = { Text("Municipality Scope") },
                enabled = callerRole == UserRole.SYSTEM_OWNER,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = positionInput,
                onValueChange = { positionInput = it },
                label = { Text("Official Position / Designation") },
                placeholder = { Text("e.g. Sanitation Inspector / Field Admin") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = departmentInput,
                onValueChange = { departmentInput = it },
                label = { Text("Office or Department") },
                placeholder = { Text("e.g. Solid Waste Management Office") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, color = HazardousRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (fullNameInput.isBlank() || emailInput.isBlank() || positionInput.isBlank()) {
                        errorMessage = "Please complete full name, email, and official position."
                        return@Button
                    }

                    HakotRepository.createAccountInvitation(
                        intendedName = fullNameInput.trim(),
                        intendedEmail = emailInput.trim(),
                        contactNumber = phoneInput.trim(),
                        municipalityId = HakotRepository.activeOrganization.municipalityId,
                        municipalityName = municipalityInput.trim(),
                        assignedRole = selectedRole,
                        positionTitle = positionInput.trim(),
                        officeDepartment = departmentInput.trim()
                    )

                    Toast.makeText(context, "Email invitation sent to ${emailInput.trim()}! (Backend Contract Pending)", Toast.LENGTH_LONG).show()
                    onCreated()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Mail, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SEND EMAIL INVITATION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// ACCOUNT DETAILS MODAL SHEET
// ----------------------------------------------------------------------------

@Composable
fun AccountDetailsModal(
    profile: AdminUserProfile,
    onDismiss: () -> Unit,
    onDeleteRequest: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = true) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, EcoGreenPrimary, RoundedCornerShape(20.dp))
                .clickable(enabled = false) { /* Consume clicks */ }
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account Details & Permissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkSecondary)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Full Name: ${profile.fullName}", fontSize = 14.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
                        Text("Email: ${profile.email}", fontSize = 12.sp, color = TextOnDarkSecondary)
                        Text("Role: ${profile.role.label}", fontSize = 12.sp, color = EcoGreenLight, fontWeight = FontWeight.SemiBold)
                        Text("Position: ${profile.positionTitle}", fontSize = 12.sp, color = TextOnDarkSecondary)
                        Text("Department: ${profile.officeDepartment}", fontSize = 12.sp, color = TextOnDarkSecondary)
                        Text("Municipality: ${profile.municipalityName}", fontSize = 12.sp, color = TextOnDarkSecondary)
                        Text("District Scope: ${profile.districtName}", fontSize = 12.sp, color = TextOnDarkSecondary)
                        Text("Registration Date: ${profile.registrationDate}", fontSize = 12.sp, color = TextOnDarkMuted)
                        Text("Status: ${profile.status.label}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (profile.status == AdminStatus.ACTIVE) EcoGreenLight else HazardousRed)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (HakotRepository.currentAdminRole == UserRole.SYSTEM_OWNER) {
                    Button(
                        onClick = { onDeleteRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DELETE ACCOUNT", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Close Details", fontSize = 13.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRoutesView() {
    CollectionScheduleDashboardView()
}

@Composable
fun MapRoutePlannerScreen(
    initialDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onNavigateToDumpsites: () -> Unit = {},
    onSaveScheduledRoute: (String, String, String, LocalDate, LocalTime, LocalTime, String, Boolean, LocalDate?, List<GisRoadSegmentFeature>, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGisLoading by remember { mutableStateOf(!GisManager.isLoaded) }
    var selectedDumpsiteName by remember { mutableStateOf(HakotRepository.dumpsites.firstOrNull()?.name ?: "no dumpsite") }

    LaunchedEffect(Unit) {
        if (!GisManager.isLoaded) {
            isGisLoading = true
            GisManager.loadGisData()
            isGisLoading = false
        }
        try {
            HakotRepository.fetchDumpsitesFromSupabase(context)
            if (HakotRepository.dumpsites.isNotEmpty()) {
                selectedDumpsiteName = HakotRepository.dumpsites.first().name
            } else {
                selectedDumpsiteName = "no dumpsite"
            }
        } catch (e: Exception) {
            Log.e("HAKOT_ADMIN", "Error fetching dumpsites in route planner", e)
            selectedDumpsiteName = "no dumpsite"
        }
    }

    val barangayList = remember(GisManager.barangays.size, isGisLoading) {
        if (GisManager.barangays.isNotEmpty()) GisManager.barangays.map { it.name }
        else listOf("Aga", "Bucana", "Caluangan", "Kaylaway", "Wawa", "Barangay 1", "Barangay 2", "Barangay 6")
    }

    var routeNameInput by remember { mutableStateOf("") }
    var scheduleNameInput by remember { mutableStateOf("") }
    var selectedBarangay by remember { mutableStateOf("Barangay 6") }
    var selectedRoadSegmentIds by remember { mutableStateOf(listOf<String>()) }
    var bgySearchQuery by remember { mutableStateOf("") }
    var roadSearchQuery by remember { mutableStateOf("") }
    var showReviewDialog by remember { mutableStateOf(false) }

    var collectionDateInput by remember(initialDate) { mutableStateOf(initialDate.toString()) }
    var startTimeInput by remember { mutableStateOf("08:00") }
    var endTimeInput by remember { mutableStateOf("11:30") }
    var assignedTruckInput by remember { mutableStateOf(HakotRepository.trucks.firstOrNull()?.let { "${it.plateNumber} (${it.truckCode})" } ?: "ABK-8891 (Dump Truck A)") }

    val filteredBarangays = remember(bgySearchQuery, barangayList) {
        if (bgySearchQuery.isBlank()) barangayList
        else barangayList.filter { it.contains(bgySearchQuery, ignoreCase = true) }
    }

    val availableRoads = remember(selectedBarangay, GisManager.isLoaded, isGisLoading) {
        GisManager.getRoadSegmentsInBarangay(selectedBarangay)
    }

    val filteredAvailableRoads = remember(roadSearchQuery, availableRoads) {
        if (roadSearchQuery.isBlank()) availableRoads
        else availableRoads.filter { it.segmentName.contains(roadSearchQuery, ignoreCase = true) || it.roadType.contains(roadSearchQuery, ignoreCase = true) }
    }

    val selectedGisSegments = remember(selectedRoadSegmentIds, availableRoads, GisManager.isLoaded) {
        selectedRoadSegmentIds.mapNotNull { id ->
            availableRoads.find { it.id == id } ?: GisManager.roadSegments.find { it.id == id }
        }
    }

    var flipStateTrigger by remember { mutableStateOf(0) }

    val routeSegmentsList = remember(selectedGisSegments, flipStateTrigger, selectedGisSegments.map { "${it.id}_${it.isReversed}" }) {
        selectedGisSegments.mapNotNull { seg ->
            val rawCoords = if (seg.clippedCoordinates.isNotEmpty()) seg.clippedCoordinates else seg.lineStringCoordinates
            val coords = if (seg.isReversed) rawCoords.reversed() else rawCoords
            val points = coords.mapNotNull { (lng, lat) ->
                if (lng.isFinite() && lat.isFinite() && lng in -180.0..180.0 && lat in -90.0..90.0) GeoPoint(lat, lng) else null
            }
            if (points.size >= 2) points else null
        }
    }
    val routeGeoPoints = remember(routeSegmentsList) { routeSegmentsList.flatten() }

    if (isGisLoading && !GisManager.isLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = EcoGreenPrimary)
                Text("Loading 42 Nasugbu Barangays & 1,579 PostGIS Road Segments...", fontSize = 12.sp, color = TextOnDarkPrimary)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            // Top Toolbar Surface
            Surface(
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PLAN NEW ROUTE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        Text("$selectedBarangay • Selected roads: ${selectedGisSegments.size}", fontSize = 10.sp, color = TextOnDarkPrimary)
                    }
                    val isRouteSelected = selectedGisSegments.isNotEmpty()
                    Button(
                        onClick = {
                            if (!isRouteSelected) {
                                Toast.makeText(context, "Please select at least one route corridor / street first.", Toast.LENGTH_SHORT).show()
                            } else {
                                showReviewDialog = true
                            }
                        },
                        enabled = isRouteSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EcoGreenPrimary,
                            disabledContainerColor = DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Finish Route",
                            color = if (isRouteSelected) Color.Black else TextOnDarkMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Standing Rectangle Map Container Box (matching 3rd pick design)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(4.dp, DarkBorder, RoundedCornerShape(14.dp))
            ) {
                DarkMapView(
                    modifier = Modifier.fillMaxSize(),
                    customRoutePoints = routeGeoPoints,
                    customRouteSegments = routeSegmentsList,
                    barangayRoads = availableRoads,
                    selectedRoadIds = selectedRoadSegmentIds,
                    showDefaultSchedulesWhenEmpty = false
                )
            }

            // Bottom Selection Panel with 2 Columns: Barangay Column & Street Column (each with own search & scrollable list)
            Surface(
                color = DarkSurfaceCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Route Corridors", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        Text("Selected: ${selectedRoadSegmentIds.size} roads", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                    }

                    if (selectedGisSegments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            itemsIndexed(selectedGisSegments) { idx, seg ->
                                Surface(
                                    color = DarkSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, EcoGreenPrimary)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("${idx + 1}. ${seg.segmentName.take(14)}...", fontSize = 10.sp, color = TextOnDarkPrimary)
                                        TextButton(
                                            onClick = {
                                                seg.isReversed = !seg.isReversed
                                                flipStateTrigger++
                                            },
                                            contentPadding = PaddingValues(2.dp),
                                            modifier = Modifier.size(50.dp, 24.dp)
                                        ) {
                                            Text(if (seg.isReversed) "Rev ⇄" else "Fwd ⇄", fontSize = 9.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Two Columns: Left = Barangay, Right = Street
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left Column: Barangay
                        Column(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .background(DarkSurface, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("1. Barangay", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            OutlinedTextField(
                                value = bgySearchQuery,
                                onValueChange = { bgySearchQuery = it },
                                placeholder = { Text("Search...", fontSize = 10.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoGreenPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextOnDarkPrimary,
                                    unfocusedTextColor = TextOnDarkPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredBarangays) { bgyName ->
                                    val isSelected = selectedBarangay == bgyName
                                    Surface(
                                        color = if (isSelected) EcoGreenContainer else DarkSurfaceCard,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, if (isSelected) EcoGreenPrimary else DarkBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (selectedBarangay != bgyName) {
                                                    selectedBarangay = bgyName
                                                    selectedRoadSegmentIds = emptyList()
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = bgyName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EcoGreenLight else TextOnDarkPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: Street
                        Column(
                            modifier = Modifier
                                .weight(0.58f)
                                .fillMaxHeight()
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .background(DarkSurface, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("2. Streets (${filteredAvailableRoads.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            OutlinedTextField(
                                value = roadSearchQuery,
                                onValueChange = { roadSearchQuery = it },
                                placeholder = { Text("Search streets...", fontSize = 10.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EcoGreenPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextOnDarkPrimary,
                                    unfocusedTextColor = TextOnDarkPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            )
                            if (filteredAvailableRoads.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No roads found.", fontSize = 10.sp, color = TextOnDarkSecondary)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(filteredAvailableRoads) { road ->
                                        val isChecked = selectedRoadSegmentIds.contains(road.id)
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = if (isChecked) EcoGreenContainer.copy(alpha = 0.5f) else DarkSurfaceCard),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedRoadSegmentIds = if (isChecked) selectedRoadSegmentIds - road.id else selectedRoadSegmentIds + road.id
                                                }
                                                .border(1.dp, if (isChecked) EcoGreenPrimary else DarkBorder, RoundedCornerShape(6.dp))
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(road.segmentName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, maxLines = 1)
                                                    Text(road.roadType, fontSize = 9.sp, color = TextOnDarkSecondary, maxLines = 1)
                                                }
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        selectedRoadSegmentIds = if (checked) selectedRoadSegmentIds + road.id else selectedRoadSegmentIds - road.id
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    if (showReviewDialog) {
        var scheduleType by remember { mutableStateOf("SPECIFIC_DATE") }
        var selectedDayOfWeek by remember { mutableStateOf(DayOfWeek.MONDAY) }
        val daysOfWeekList = listOf(
            DayOfWeek.MONDAY to "Monday",
            DayOfWeek.TUESDAY to "Tuesday",
            DayOfWeek.WEDNESDAY to "Wednesday",
            DayOfWeek.THURSDAY to "Thursday",
            DayOfWeek.FRIDAY to "Friday",
            DayOfWeek.SATURDAY to "Saturday",
            DayOfWeek.SUNDAY to "Sunday"
        )
        var repeatEndDate by remember { mutableStateOf(LocalDate.now().plusMonths(3).toString()) }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Route & Schedule Details (Database Save)", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = routeNameInput,
                        onValueChange = { routeNameInput = it },
                        label = { Text("Route & Schedule Name (Required)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Barangay: $selectedBarangay (${selectedGisSegments.size} segments)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Assigned Dumpsite / MRF Facility:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    if (HakotRepository.dumpsites.isEmpty()) {
                        Button(
                            onClick = {
                                showReviewDialog = false
                                onDismiss()
                                onNavigateToDumpsites()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("+ Add Dumpsite First (Go to Dumpsites Tab)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        var dumpsiteDropdownExpanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .clickable { dumpsiteDropdownExpanded = true }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏛️ $selectedDumpsiteName", fontSize = 12.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TextOnDarkSecondary)
                            }
                            DropdownMenu(
                                expanded = dumpsiteDropdownExpanded,
                                onDismissRequest = { dumpsiteDropdownExpanded = false }
                            ) {
                                HakotRepository.dumpsites.forEach { ds ->
                                    DropdownMenuItem(
                                        text = { Text(ds.name, fontSize = 12.sp) },
                                        onClick = {
                                            selectedDumpsiteName = ds.name
                                            dumpsiteDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Schedule Frequency:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = scheduleType == "SPECIFIC_DATE", onClick = { scheduleType = "SPECIFIC_DATE" })
                        Text("Specific Date", fontSize = 11.sp, color = TextOnDarkPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = scheduleType == "DAY_OF_WEEK", onClick = { scheduleType = "DAY_OF_WEEK" })
                        Text("Day of Week (Weekly)", fontSize = 11.sp, color = TextOnDarkPrimary)
                    }

                    if (scheduleType == "SPECIFIC_DATE") {
                        OutlinedTextField(
                            value = collectionDateInput,
                            onValueChange = { collectionDateInput = it },
                            label = { Text("Collection Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Select Day of Week:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkSecondary)
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(daysOfWeekList) { (dow, label) ->
                                    FilterChip(
                                        selected = selectedDayOfWeek == dow,
                                        onClick = {
                                            selectedDayOfWeek = dow
                                            var target = LocalDate.now()
                                            while (target.dayOfWeek != dow) {
                                                target = target.plusDays(1)
                                            }
                                            collectionDateInput = target.toString()
                                        },
                                        label = { Text(label.take(3), fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight, containerColor = DarkSurface, labelColor = TextOnDarkSecondary)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = repeatEndDate,
                            onValueChange = { repeatEndDate = it },
                            label = { Text("Recurrence End Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTimeInput,
                            onValueChange = { startTimeInput = it },
                            label = { Text("Start Time (HH:MM)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTimeInput,
                            onValueChange = { endTimeInput = it },
                            label = { Text("End Time (HH:MM)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Route Travel Sequence & Traversal Direction:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        selectedGisSegments.forEachIndexed { idx, seg ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${idx + 1}. ${seg.segmentName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                        Text("Direction: ${if (seg.isReversed) "Reversed (◀)" else "Forward (➔)"}", fontSize = 9.sp, color = EcoGreenLight)
                                    }
                                    TextButton(
                                        onClick = {
                                            seg.isReversed = !seg.isReversed
                                        },
                                        contentPadding = PaddingValues(2.dp)
                                    ) {
                                        Text("⇄ Flip", fontSize = 10.sp, color = EcoGreenLight)
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
                        if (selectedGisSegments.isNotEmpty()) {
                            if (routeNameInput.isBlank()) {
                                routeNameInput = "$selectedBarangay Route"
                            }
                            scheduleNameInput = routeNameInput
                            try {
                                val start = LocalTime.parse(startTimeInput.trim())
                                val end = LocalTime.parse(endTimeInput.trim())
                                if (!end.isAfter(start)) return@Button

                                val repeatWeekly: Boolean = (scheduleType == "DAY_OF_WEEK")
                                val date: LocalDate = if (repeatWeekly) {
                                    var target = LocalDate.now()
                                    while (target.dayOfWeek != selectedDayOfWeek) {
                                        target = target.plusDays(1)
                                    }
                                    target
                                } else {
                                    try {
                                        LocalDate.parse(collectionDateInput.trim())
                                    } catch (_: Exception) {
                                        LocalDate.now()
                                    }
                                }
                                val endRecur: LocalDate? = if (repeatWeekly) {
                                    try { LocalDate.parse(repeatEndDate.trim()) } catch (_: Exception) { date.plusMonths(3) }
                                } else null

                                onSaveScheduledRoute(
                                    routeNameInput.trim(),
                                    scheduleNameInput.trim(),
                                    selectedBarangay,
                                    date,
                                    start,
                                    end,
                                    "No assigned truck yet",
                                    repeatWeekly,
                                    endRecur,
                                    selectedGisSegments,
                                    selectedDumpsiteName
                                )
                                scope.launch {
                                    HakotRepository.fetchSchedulesFromSupabase(context)
                                }
                                showReviewDialog = false
                                onDismiss()
                            } catch (e: Exception) {
                                Log.e("HAKOT_PLANNER", "Save schedule error", e)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Confirm and Save Schedule", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Back", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }
    }
}



@Composable
fun CollectionScheduleDashboardView(
    onNavigateToDumpsites: () -> Unit = {}
) {
    val context = LocalContext.current
    val zoneId = ZoneId.of("Asia/Manila")
    var showSchedulePlanner by remember { mutableStateOf(false) }
    var selectedScheduleDetail by remember { mutableStateOf<ScheduleItem?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleItem?>(null) }
    var isLoadingSchedules by remember { mutableStateOf(false) }
    var selectedWeekStart by remember { mutableStateOf(LocalDate.now(zoneId).with(DayOfWeek.MONDAY)) }
    var adminSelectedDay by remember { mutableStateOf<LocalDate?>(null) }
    var adminScheduleSubTab by remember { mutableIntStateOf(0) } // 0=All/Day, 1=Draft, 2=Assigned, 3=Active, 4=Completed, 5=Missed
    var successMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit, showSchedulePlanner) {
        if (!showSchedulePlanner) {
            isLoadingSchedules = true
            HakotRepository.fetchSchedulesFromSupabase(context)
            isLoadingSchedules = false
        }
    }

    val daysOfWeek = (0..6).map { selectedWeekStart.plusDays(it.toLong()) }
    val now = LocalDateTime.now(ZoneId.of("Asia/Manila"))
    val today = LocalDate.now(ZoneId.of("Asia/Manila"))

    val allAdminSchedules = HakotRepository.schedules.sortedWith(compareBy({ it.date }, { it.startTime }))

    val adminDrafts = allAdminSchedules.filter { 
        val st = it.status.uppercase()
        (st == "DRAFT" || st == "SCHEDULED" || st.isBlank() || it.truckAssigned.contains("No assigned", ignoreCase = true)) && st != "ASSIGNED"
    }
    val adminAssigned = allAdminSchedules.filter { it.status.equals("ASSIGNED", ignoreCase = true) }
    val adminActive = allAdminSchedules.filter { 
        val st = it.status.uppercase()
        st == "ACTIVE" || st == "ON_HOLD" || st == "HOLD" || st == "ON HOLD"
    }
    val adminCompleted = allAdminSchedules.filter { 
        val st = it.status.uppercase()
        st == "COMPLETED" || st == "DONE" || st == "COMPLETE"
    }
    val adminMissed = allAdminSchedules.filter { 
        val st = it.status.uppercase()
        st == "MISSED" || (it.date.isBefore(today) && !adminCompleted.contains(it) && !it.isWeeklyRepeat)
    }

    val adminFilterTitle = when (adminScheduleSubTab) {
        1 -> "DRAFT"
        2 -> "ASSIGNED"
        3 -> "ACTIVE"
        4 -> "COMPLETED"
        5 -> "MISSED"
        else -> if (adminSelectedDay != null) "FOR $adminSelectedDay" else "ALL SCHEDULES"
    }

    val targetAdminDay = adminSelectedDay ?: today
    val schedulesForSelectedDay = allAdminSchedules.filter { sch ->
        val matchesDay = if (adminScheduleSubTab != 0) true else HakotRepository.isScheduleActiveOnDate(sch, targetAdminDay)
        val matchesTab = when (adminScheduleSubTab) {
            1 -> adminDrafts.contains(sch)
            2 -> adminAssigned.contains(sch)
            3 -> adminActive.contains(sch)
            4 -> adminCompleted.contains(sch)
            5 -> adminMissed.contains(sch)
            else -> true
        }
        matchesDay && matchesTab
    }.filterNot { sch ->
        sch.isWeeklyRepeat && adminScheduleSubTab == 0 && allAdminSchedules.any { other ->
            !other.isWeeklyRepeat &&
            other.date == targetAdminDay &&
            (other.routeName == "${sch.routeName} ($targetAdminDay)" || other.routeName == sch.routeName || other.routeName.startsWith(sch.routeName))
        }
    }

    if (showSchedulePlanner) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            MapRoutePlannerScreen(
                initialDate = adminSelectedDay ?: LocalDate.now(),
                onDismiss = { showSchedulePlanner = false },
                onNavigateToDumpsites = {
                    showSchedulePlanner = false
                    onNavigateToDumpsites()
                },
                onSaveScheduledRoute = { routeName, scheduleName, barangayName, date, startTime, endTime, truck, repeatWeekly, repeatEnd, segments, dumpsiteName ->
                    val newSchedule = ScheduleItem(
                        id = "sch_${System.currentTimeMillis()}",
                        barangay = barangayName,
                        routeName = routeName,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        wasteCategory = WasteCategory.GENERAL_WASTE,
                        truckAssigned = if (truck.isBlank() || truck.contains("Dump Truck", ignoreCase = true)) "No assigned truck yet" else truck,
                        status = "DRAFT",
                        isWeeklyRepeat = repeatWeekly,
                        recurrenceEndDate = repeatEnd,
                        isDisabled = false,
                        orderedSegments = segments,
                        dumpsiteName = dumpsiteName
                    )

                    scope.launch {
                        val schedRes = HakotRepository.saveScheduledRouteToSupabase(newSchedule, context)
                        if (schedRes.first) {
                            adminSelectedDay = date
                            selectedWeekStart = date.with(DayOfWeek.MONDAY)
                            HakotRepository.fetchSchedulesFromSupabase(context)
                            successMsg = "Success: Scheduled route '$routeName' saved to table 'Collection_Scheds' for $date!"
                            Toast.makeText(context, "Schedule '$routeName' saved successfully to Supabase!", Toast.LENGTH_LONG).show()
                            showSchedulePlanner = false
                        } else {
                            successMsg = "Database Schedule Error: ${schedRes.second}"
                            Toast.makeText(context, "Save Failed: ${schedRes.second}", Toast.LENGTH_LONG).show()
                            Log.e("HAKOT_SAVE", "Failed to save to Collection_Scheds: ${schedRes.second}")
                        }
                    }
                }
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Collection Schedule & Routes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                Text("Nasugbu Municipality • Timezone: Asia/Manila", fontSize = 11.sp, color = TextOnDarkSecondary)
            }
            Button(
                onClick = { showSchedulePlanner = true },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Plan a Schedule", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        successMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(10.dp))
            ) {
                Text(it, color = EcoGreenLight, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Interactive Status Metrics Bar (Draft, Assigned, Active, Completed, Missed)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MetricCard(
                title = "Draft",
                value = "${adminDrafts.size}",
                color = TextOnDarkPrimary,
                modifier = Modifier.weight(1f).clickable { adminScheduleSubTab = if (adminScheduleSubTab == 1) 0 else 1 }
            )
            MetricCard(
                title = "Assigned",
                value = "${adminAssigned.size}",
                color = EcoGreenLight,
                modifier = Modifier.weight(1f).clickable { adminScheduleSubTab = if (adminScheduleSubTab == 2) 0 else 2 }
            )
            MetricCard(
                title = "Active",
                value = "${adminActive.size}",
                color = EcoGreenPrimary,
                modifier = Modifier.weight(1f).clickable { adminScheduleSubTab = if (adminScheduleSubTab == 3) 0 else 3 }
            )
            MetricCard(
                title = "Completed",
                value = "${adminCompleted.size}",
                color = EcoGreenLight,
                modifier = Modifier.weight(1f).clickable { adminScheduleSubTab = if (adminScheduleSubTab == 4) 0 else 4 }
            )
            MetricCard(
                title = "Missed",
                value = "${adminMissed.size}",
                color = HazardousRed,
                modifier = Modifier.weight(1f).clickable { adminScheduleSubTab = if (adminScheduleSubTab == 5) 0 else 5 }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // WEEKLY CALENDAR WIDGET
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedWeekStart = selectedWeekStart.minusWeeks(1) }) {
                        Text("◀ Prev Week", fontSize = 11.sp, color = EcoGreenLight)
                    }
                    Text("Week of ${selectedWeekStart}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                    TextButton(onClick = { selectedWeekStart = selectedWeekStart.plusWeeks(1) }) {
                        Text("Next Week ▶", fontSize = 11.sp, color = EcoGreenLight)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    daysOfWeek.forEach { day ->
                        val isSelected = day == adminSelectedDay
                        val hasSchedules = HakotRepository.schedules.any {
                            HakotRepository.isScheduleActiveOnDate(it, day)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EcoGreenPrimary else if (hasSchedules) EcoGreenContainer else DarkSurface)
                                .clickable { 
                                    adminSelectedDay = if (adminSelectedDay == day) null else day
                                    adminScheduleSubTab = 0
                                }
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(day.dayOfWeek.name.take(3), fontSize = 10.sp, color = if (isSelected) Color.Black else TextOnDarkSecondary)
                            Text("${day.dayOfMonth}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else TextOnDarkPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Schedule List — VIEWING $adminFilterTitle (${schedulesForSelectedDay.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
        Spacer(modifier = Modifier.height(6.dp))

        if (isLoadingSchedules) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EcoGreenPrimary)
            }
        } else if (schedulesForSelectedDay.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No $adminFilterTitle collection schedules found.", fontSize = 12.sp, color = TextOnDarkSecondary, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                schedulesForSelectedDay.forEach { sch ->
                    val dbStatus = sch.status.uppercase()
                    val statusColor = when (dbStatus) {
                        "ACTIVE" -> EcoGreenLight
                        "ON_HOLD", "HOLD" -> RecycleYellow
                        "COMPLETED", "DONE", "COMPLETE" -> EcoGreenPrimary
                        "ASSIGNED" -> EcoGreenLight
                        else -> TextOnDarkSecondary
                    }

                    val scope = rememberCoroutineScope()
                    val cardAlpha = if (sch.isDisabled) 0.4f else 1f

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard.copy(alpha = cardAlpha)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                Log.d("HAKOT_ROUTE", "CLICK_SCHEDULE: Selected schedule id=${sch.id}, routeName=${sch.routeName}, orderedSegments.size=${sch.orderedSegments.size}")
                                selectedScheduleDetail = sch 
                            }
                            .border(1.dp, if (sch.isDisabled) TextOnDarkSecondary.copy(alpha = 0.3f) else if (dbStatus == "ACTIVE") EcoGreenPrimary else DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sch.routeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (sch.isDisabled) TextOnDarkSecondary else TextOnDarkPrimary)
                                Surface(
                                    color = if (sch.isDisabled) DarkSurface else if (dbStatus == "ACTIVE") EcoGreenContainer else if (dbStatus == "ON_HOLD") RecycleYellowContainer else DarkSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (sch.isDisabled) TextOnDarkSecondary else statusColor)
                                ) {
                                    Text(
                                        text = if (sch.isDisabled) "Disabled" else sch.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sch.isDisabled) TextOnDarkSecondary else statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📍 Barangay Scope: ${sch.barangay}", fontSize = 12.sp, color = if (sch.isDisabled) TextOnDarkSecondary else EcoGreenLight)
                            Text("🗓️ Date: ${sch.date} • Time: ${sch.startTime} - ${sch.endTime}", fontSize = 11.sp, color = TextOnDarkSecondary)
                            Text("🚛 Assigned Truck: ${sch.truckAssigned}", fontSize = 11.sp, color = TextOnDarkSecondary)
                            Text("🏛️ Dumpsite: ${sch.dumpsiteName}", fontSize = 11.sp, color = EcoGreenLight)
                            if (sch.isWeeklyRepeat) {
                                Text("🔄 Repeats Weekly until ${sch.recurrenceEndDate}", fontSize = 10.sp, color = if (sch.isDisabled) TextOnDarkSecondary else EcoGreenLight)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Switch(
                                        checked = sch.operational,
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                val updated = sch.copy(operational = isChecked)
                                                val success = HakotRepository.updateScheduleInSupabase(updated, context)
                                                if (success) {
                                                    val idx = HakotRepository.schedules.indexOfFirst { it.id == sch.id }
                                                    if (idx != -1) {
                                                        HakotRepository.schedules[idx] = updated
                                                    }
                                                } else {
                                                    successMsg = "Failed to update schedule status in Supabase."
                                                }
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EcoGreenPrimary,
                                            checkedTrackColor = EcoGreenContainer,
                                            uncheckedThumbColor = TextOnDarkSecondary,
                                            uncheckedTrackColor = DarkSurface
                                        )
                                    )
                                    Text(if (sch.operational) "Operational" else "Not Operational", fontSize = 11.sp, color = if (sch.operational) EcoGreenLight else TextOnDarkSecondary, fontWeight = FontWeight.Bold)
                                }

                                val canCancel = dbStatus == "DRAFT" || dbStatus == "SCHEDULED" || dbStatus == "ASSIGNED" || dbStatus.isBlank()
                                if (canCancel) {
                                    TextButton(onClick = {
                                        scheduleToDelete = sch
                                    }) {
                                        Text("Cancel Schedule", fontSize = 11.sp, color = HazardousRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    scheduleToDelete?.let { schToDel ->
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Confirm Schedule Cancellation", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel and permanently delete route '${schToDel.routeName}' for ${schToDel.date} from the Supabase database?", color = TextOnDarkSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = HakotRepository.deleteScheduleFromSupabase(schToDel.id, context)
                            if (success) {
                                successMsg = "Successfully cancelled and deleted schedule '${schToDel.routeName}'."
                            } else {
                                successMsg = "Failed to delete schedule from Supabase."
                            }
                            scheduleToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Keep", color = TextOnDarkPrimary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    selectedScheduleDetail?.let { sch ->
        val routeSegmentsList = remember(sch, GisManager.roadSegments) {
            sch.orderedSegments.mapNotNull { seg ->
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
                if (points.size >= 2) points else null
            }
        }
        val routeGeoPoints = remember(routeSegmentsList) { routeSegmentsList.flatten() }

        Log.d("HAKOT_ROUTE", "AFTER_REMEMBER: scheduleId=${sch.id}, routeGeoPoints.size=${routeGeoPoints.size}, segmentsCount=${routeSegmentsList.size}")

        Dialog(
            onDismissRequest = { selectedScheduleDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                color = DarkBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sch.routeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text("Barangay: ${sch.barangay} • Date: ${sch.date}", fontSize = 11.sp, color = TextOnDarkSecondary)
                        }
                        IconButton(onClick = { selectedScheduleDetail = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Log.d("HAKOT_ROUTE", "BEFORE_DARKMAPVIEW: customRoutePoints.size=${routeGeoPoints.size}, customRouteSegments.size=${routeSegmentsList.size}")
                        DarkMapView(
                            modifier = Modifier.fillMaxSize(),
                            customRoutePoints = routeGeoPoints,
                            customRouteSegments = routeSegmentsList
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("SCHEDULE & ROUTE DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        Text("Collection Date: ${sch.date}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("Time Window: ${sch.startTime} - ${sch.endTime}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        val truckDisplay = if (sch.truckAssigned.isBlank() || sch.truckAssigned.contains("Unassigned", ignoreCase = true) || sch.truckAssigned.contains("No assigned", ignoreCase = true) || sch.truckAssigned.contains("Dump Truck", ignoreCase = true)) "No assigned truck yet" else sch.truckAssigned
                        Text("Assigned Truck: $truckDisplay", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("Assigned Dumpsite: ${sch.dumpsiteName}", fontSize = 12.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
                        Text("Status: ${sch.status}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        if (sch.isWeeklyRepeat) {
                            Text("🔄 Weekly Recurrence: Active until ${sch.recurrenceEndDate}", fontSize = 11.sp, color = EcoGreenLight)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("ROUTE CORRIDOR SEGMENTS (${sch.orderedSegments.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)

                        if (sch.orderedSegments.isEmpty()) {
                            Text("No segment details stored.", fontSize = 11.sp, color = TextOnDarkSecondary)
                        } else {
                            sch.orderedSegments.forEachIndexed { idx, seg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${idx + 1}. ${seg.segmentName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                            Text("Type: ${seg.roadType}", fontSize = 10.sp, color = TextOnDarkSecondary)
                                        }
                                        Text(
                                            if (seg.isReversed) "Reversed ◀" else "Forward ➔",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoGreenLight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun PlanScheduleDialog(
    onDismiss: () -> Unit,
    onSaveSchedule: (String, String, LocalDate, LocalTime, LocalTime, String, Boolean, LocalDate?) -> Unit
) {
    val savedRoutes = HakotRepository.routes
    var selectedRoute by remember { mutableStateOf(savedRoutes.firstOrNull()) }
    var scheduleType by remember { mutableStateOf("SPECIFIC_DATE") } // "SPECIFIC_DATE" or "DAY_OF_WEEK"
    var collectionDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var selectedDayOfWeek by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("11:30") }
    var assignedTruck by remember { mutableStateOf(HakotRepository.trucks.firstOrNull()?.let { "${it.plateNumber} (${it.truckCode})" } ?: "ABK-8891 (Dump Truck A)") }
    var repeatEndDate by remember { mutableStateOf(LocalDate.now().plusMonths(3).toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val daysOfWeekList = listOf(
        DayOfWeek.MONDAY to "Monday",
        DayOfWeek.TUESDAY to "Tuesday",
        DayOfWeek.WEDNESDAY to "Wednesday",
        DayOfWeek.THURSDAY to "Thursday",
        DayOfWeek.FRIDAY to "Friday",
        DayOfWeek.SATURDAY to "Saturday",
        DayOfWeek.SUNDAY to "Sunday"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan a Schedule (Date or Day of Week + Time)", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMsg?.let {
                    Surface(color = HazardousRed.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                        Text(it, fontSize = 11.sp, color = HazardousRed, modifier = Modifier.padding(6.dp))
                    }
                }

                Text("Select Saved Route:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                if (savedRoutes.isEmpty()) {
                    Text("No saved routes found. Please create a route first.", fontSize = 11.sp, color = HazardousRed)
                } else {
                    savedRoutes.forEach { rt ->
                        val isSelected = selectedRoute?.id == rt.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedRoute = rt }
                                .fillMaxWidth()
                                .background(if (isSelected) EcoGreenContainer.copy(alpha = 0.4f) else Color.Transparent)
                                .padding(4.dp)
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedRoute = rt })
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(rt.routeName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                Text("📍 ${rt.barangaysCovered}", fontSize = 10.sp, color = TextOnDarkSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Schedule Frequency Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = scheduleType == "SPECIFIC_DATE", onClick = { scheduleType = "SPECIFIC_DATE" })
                    Text("Specific Date", fontSize = 11.sp, color = TextOnDarkPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(selected = scheduleType == "DAY_OF_WEEK", onClick = { scheduleType = "DAY_OF_WEEK" })
                    Text("Day of Week (Weekly)", fontSize = 11.sp, color = TextOnDarkPrimary)
                }

                if (scheduleType == "SPECIFIC_DATE") {
                    OutlinedTextField(
                        value = collectionDate,
                        onValueChange = { collectionDate = it },
                        label = { Text("Collection Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Select Day of Week:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkSecondary)
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(daysOfWeekList) { (dow, label) ->
                                FilterChip(
                                    selected = selectedDayOfWeek == dow,
                                    onClick = { selectedDayOfWeek = dow },
                                    label = { Text(label.take(3), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight, containerColor = DarkSurface, labelColor = TextOnDarkSecondary)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = repeatEndDate,
                        onValueChange = { repeatEndDate = it },
                        label = { Text("Recurrence End Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRoute == null) {
                        errorMsg = "Please select a saved route."
                        return@Button
                    }
                    try {
                        val start = LocalTime.parse(startTime.trim())
                        val end = LocalTime.parse(endTime.trim())

                        if (!end.isAfter(start)) {
                            errorMsg = "End time must be later than start time."
                            return@Button
                        }

                        val date: LocalDate
                        val repeatWeekly: Boolean
                        val endRecur: LocalDate?

                        if (scheduleType == "SPECIFIC_DATE") {
                            date = LocalDate.parse(collectionDate.trim())
                            if (date.isBefore(LocalDate.now())) {
                                errorMsg = "Collection date cannot be in the past."
                                return@Button
                            }
                            repeatWeekly = false
                            endRecur = null
                        } else {
                            var d = LocalDate.now()
                            while (d.dayOfWeek != selectedDayOfWeek) {
                                d = d.plusDays(1)
                            }
                            date = d
                            repeatWeekly = true
                            endRecur = LocalDate.parse(repeatEndDate.trim())
                            if (endRecur.isBefore(date)) {
                                errorMsg = "Recurrence end date must be after start date."
                                return@Button
                            }
                        }

                        onSaveSchedule(
                            selectedRoute!!.routeName,
                            selectedRoute!!.routeName,
                            date,
                            start,
                            end,
                            assignedTruck,
                            repeatWeekly,
                            endRecur
                        )
                    } catch (e: Exception) {
                        errorMsg = "Invalid date/time format (use YYYY-MM-DD and HH:MM)."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
            ) {
                Text("Confirm and Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextOnDarkSecondary)
            }
        },
        containerColor = DarkSurfaceCard
    )
}

@Composable
fun AdminTrucksView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fleetSubTab by remember { mutableIntStateOf(0) } // 0 = Trucks, 1 = Truckers

    var showAddTruckDialog by remember { mutableStateOf(false) }
    var showAddTruckerDialog by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    // Truck inputs (2 fields)
    var truckCodeInput by remember { mutableStateOf("Hakot Truck #${HakotRepository.trucks.size + 1}") }
    var plateNumberInput by remember { mutableStateOf("XYZ-${(1000..9999).random()}") }

    // Trucker account inputs (3 fields)
    var truckerFullNameInput by remember { mutableStateOf("") }
    var truckerUsernameInput by remember { mutableStateOf("") }
    var truckerPasswordInput by remember { mutableStateOf("") }

    var selectedTruckForDelete by remember { mutableStateOf<Dumptrack?>(null) }
    var selectedTruckerForDelete by remember { mutableStateOf<TruckerAccount?>(null) }

    val municipalitySlug = HakotRepository.activeOrganization.assignedMunicipality
        .substringBefore(",")
        .lowercase()
        .replace(" ", "")
        .replace(Regex("[^a-z0-9]"), "")
        .let { if (it.contains("nasugbu")) "nasugbu" else it }
        .ifBlank { "nasugbu" }

    LaunchedEffect(Unit) {
        HakotRepository.fetchTrucksFromSupabase(context)
        HakotRepository.fetchTruckerAccountsFromSupabase(context)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Fleet & Drivers Management", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                Text("Manage municipal collection trucks and trucker accounts", fontSize = 11.sp, color = TextOnDarkSecondary)
            }
            if (fleetSubTab == 0) {
                Button(
                    onClick = { showAddTruckDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Add New Truck", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { showAddTruckerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Add New Driver Account", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sub-tabs row (Trucks vs Truckers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { fleetSubTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (fleetSubTab == 0) EcoGreenPrimary else DarkSurfaceCard
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "🚛 Trucks (${HakotRepository.trucks.size})",
                    color = if (fleetSubTab == 0) Color.Black else TextOnDarkPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { fleetSubTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (fleetSubTab == 1) EcoGreenPrimary else DarkSurfaceCard
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "👤 Truckers (${HakotRepository.truckerAccounts.size})",
                    color = if (fleetSubTab == 1) Color.Black else TextOnDarkPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        successMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(10.dp))
            ) {
                Text(it, color = EcoGreenLight, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }

        if (fleetSubTab == 0) {
            // Trucks List
            if (HakotRepository.trucks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No fleet trucks registered yet. Click 'Add New Truck' to add one.", fontSize = 12.sp, color = TextOnDarkSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(HakotRepository.trucks) { tr ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tr.truckCode} (${tr.plateNumber})",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnDarkPrimary
                                    )
                                    Surface(
                                        color = EcoGreenContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, EcoGreenPrimary)
                                    ) {
                                        Text(
                                            text = "Active",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoGreenLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Plate: ${tr.plateNumber} | Code: ${tr.truckCode}", fontSize = 12.sp, color = TextOnDarkSecondary)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { selectedTruckForDelete = tr }
                                    ) {
                                        Text("Delete Truck ✕", fontSize = 10.sp, color = HazardousRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Truckers List
            if (HakotRepository.truckerAccounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No driver accounts registered yet. Click 'Add New Driver Account' to add one.", fontSize = 12.sp, color = TextOnDarkSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(HakotRepository.truckerAccounts) { acc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.fullName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnDarkPrimary
                                    )
                                    Surface(
                                        color = EcoGreenContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, EcoGreenPrimary)
                                    ) {
                                        Text(
                                            text = acc.role.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EcoGreenLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📧 Email: ${acc.email}", fontSize = 12.sp, color = EcoGreenLight)
                                Text("👤 Username: ${acc.username}", fontSize = 11.sp, color = TextOnDarkSecondary)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { selectedTruckerForDelete = acc }
                                    ) {
                                        Text("Delete Driver ✕", fontSize = 10.sp, color = HazardousRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Truck Dialog
    if (selectedTruckForDelete != null) {
        val trToDelete = selectedTruckForDelete!!
        AlertDialog(
            onDismissRequest = { selectedTruckForDelete = null },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Confirm Truck Deletion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HazardousRed
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete fleet truck '${trToDelete.truckCode}' (${trToDelete.plateNumber})?\n\nThis action cannot be undone.",
                    fontSize = 12.sp,
                    color = TextOnDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = HakotRepository.deleteTruckFromSupabase(trToDelete.id, context)
                            if (success) {
                                HakotRepository.trucks.removeIf { it.id == trToDelete.id }
                                Toast.makeText(context, "Truck deleted successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error deleting truck", Toast.LENGTH_SHORT).show()
                            }
                            selectedTruckForDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTruckForDelete = null }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            }
        )
    }

    // Delete Trucker Account Dialog
    if (selectedTruckerForDelete != null) {
        val accToDelete = selectedTruckerForDelete!!
        AlertDialog(
            onDismissRequest = { selectedTruckerForDelete = null },
            containerColor = DarkSurfaceCard,
            title = {
                Text(
                    text = "Confirm Driver Account Deletion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HazardousRed
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete driver account '${accToDelete.fullName}' (${accToDelete.email})?\n\nThis action cannot be undone.",
                    fontSize = 12.sp,
                    color = TextOnDarkSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val success = HakotRepository.deleteTruckerAccountFromSupabase(accToDelete.id, context)
                            if (success) {
                                Toast.makeText(context, "Driver account deleted successfully.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error deleting driver account", Toast.LENGTH_SHORT).show()
                            }
                            selectedTruckerForDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTruckerForDelete = null }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            }
        )
    }

    // Add New Truck Dialog (2 fields asked: truck name or code, plate number)
    if (showAddTruckDialog) {
        AlertDialog(
            onDismissRequest = { showAddTruckDialog = false },
            title = { Text("Register New Fleet Truck", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = truckCodeInput,
                        onValueChange = { truckCodeInput = it },
                        label = { Text("Truck Name or Code (e.g. Hakot #4)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = plateNumberInput,
                        onValueChange = { plateNumberInput = it },
                        label = { Text("Plate Number (e.g. ABC-1234)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plateNumberInput.isNotBlank() && truckCodeInput.isNotBlank()) {
                            scope.launch {
                                val newTruck = Dumptrack(
                                    id = UUID.randomUUID().toString(),
                                    truckCode = truckCodeInput.trim(),
                                    plateNumber = plateNumberInput.trim(),
                                    driverName = "Unassigned Driver",
                                    assignedRoute = "Collection Route",
                                    destinationDumpsite = "Nasugbu MRF Facility",
                                    latitude = 14.0725,
                                    longitude = 120.6335
                                )
                                val res = HakotRepository.saveTruckToSupabase(newTruck, context)
                                if (res.first) {
                                    successMsg = "Successfully registered truck '${newTruck.truckCode}'!"
                                    Toast.makeText(context, "Truck saved successfully to database!", Toast.LENGTH_SHORT).show()
                                    showAddTruckDialog = false
                                } else {
                                    successMsg = "Error saving truck: ${res.second}"
                                    Toast.makeText(context, "Error: ${res.second}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTruckDialog = false }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }

    // Add New Driver Account Dialog (3 fields asked: Full name, name with .{municipality}@hakot.driver.ph, password)
    if (showAddTruckerDialog) {
        val generatedEmail = if (truckerUsernameInput.isNotBlank()) {
            "${truckerUsernameInput.trim().lowercase().replace(" ", "")}.$municipalitySlug@hakot.driver.ph"
        } else {
            "name.$municipalitySlug@hakot.driver.ph"
        }

        AlertDialog(
            onDismissRequest = { showAddTruckerDialog = false },
            title = { Text("Register New Driver Account", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = truckerFullNameInput,
                        onValueChange = { truckerFullNameInput = it },
                        label = { Text("Full Name (e.g. Juan Dela Cruz)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = truckerUsernameInput,
                        onValueChange = { truckerUsernameInput = it },
                        label = { Text("Name / Username (e.g. juan)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Generated Email/Username: $generatedEmail",
                        fontSize = 11.sp,
                        color = EcoGreenLight
                    )
                    OutlinedTextField(
                        value = truckerPasswordInput,
                        onValueChange = { truckerPasswordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (truckerFullNameInput.isNotBlank() && truckerUsernameInput.isNotBlank() && truckerPasswordInput.isNotBlank()) {
                            scope.launch {
                                val newAccount = TruckerAccount(
                                    id = UUID.randomUUID().toString(),
                                    municipalityId = HakotRepository.activeOrganization.municipalityId,
                                    truckId = null,
                                    fullName = truckerFullNameInput.trim(),
                                    username = truckerUsernameInput.trim().lowercase().replace(" ", ""),
                                    email = generatedEmail,
                                    password = truckerPasswordInput.trim(),
                                    role = "driver",
                                    status = "active"
                                )
                                val res = HakotRepository.saveTruckerAccountToSupabase(newAccount, context)
                                if (res.first) {
                                    successMsg = "Successfully registered driver account '${newAccount.fullName}'!"
                                    Toast.makeText(context, "Driver account saved successfully!", Toast.LENGTH_SHORT).show()
                                    showAddTruckerDialog = false
                                } else {
                                    successMsg = "Error saving driver account: ${res.second}"
                                    Toast.makeText(context, "Error: ${res.second}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTruckerDialog = false }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }
}



@Composable
fun AdminReportsView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        HakotRepository.fetchAnonymousReportsFromSupabase()
    }

    val reportsList = HakotRepository.reports

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Citizen Waste Reports Management", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
            Text("Total Submitted Reports (${reportsList.size})", fontSize = 11.sp, color = TextOnDarkSecondary)
        }

        if (reportsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No reports found.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )
                        Text(
                            text = "No citizen waste reports currently in database.",
                            fontSize = 11.sp,
                            color = TextOnDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(reportsList) { rep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = rep.reportType.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnDarkPrimary
                                    )
                                    Text(
                                        text = "Ref: ${rep.referenceNumber}",
                                        fontSize = 9.5.sp,
                                        color = TextOnDarkMuted
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            val success = HakotRepository.resolveCitizenReport(rep.id, context)
                                            if (success) {
                                                Toast.makeText(context, "Report resolved & deleted from database.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to resolve report.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Resolve ✕", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "📍 ${rep.locationName}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EcoGreenLight
                            )
                            if (rep.description.isNotBlank()) {
                                Text(
                                    text = rep.description,
                                    fontSize = 11.sp,
                                    color = TextOnDarkSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val timeFormatted = try {
                                val instant = Instant.ofEpochMilli(rep.timestampMillis)
                                val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                                zdt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a"))
                            } catch (e: Exception) {
                                "Submitted recently"
                            }
                            Text(text = timeFormatted, fontSize = 9.5.sp, color = TextOnDarkMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportDetailDialog(
    report: AnonymousReport,
    onDismiss: () -> Unit,
    onStatusUpdated: (ReportStatus) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var currentStatus by remember { mutableStateOf(report.status) }

    val statusOptions = listOf(
        ReportStatus.SUBMITTED to "Pending",
        ReportStatus.UNDER_REVIEW to "Under Review",
        ReportStatus.RESOLVED to "Resolved",
        ReportStatus.REJECTED to "Rejected"
    )

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
                Column {
                    Text("Report Details", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Text("Ref: ${report.referenceNumber}", fontSize = 10.sp, color = TextOnDarkMuted)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextOnDarkPrimary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Report Type
                Column {
                    Text("Report Type", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(report.reportType.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                }

                HorizontalDivider(color = DarkBorder)

                // Location / Street
                Column {
                    Text("Location / Street / Barangay", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(report.locationName, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = EcoGreenLight)
                }

                // Description
                Column {
                    Text("Problem Description / Notes", fontSize = 10.sp, color = TextOnDarkSecondary)
                    Text(report.description.ifBlank { "No additional details provided." }, fontSize = 11.5.sp, color = TextOnDarkPrimary)
                }

                // Date & Time
                Column {
                    Text("Submitted Date & Time", fontSize = 10.sp, color = TextOnDarkSecondary)
                    val timeFormatted = try {
                        val instant = Instant.ofEpochMilli(report.timestampMillis)
                        val zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                        zdt.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy • hh:mm:ss a"))
                    } catch (e: Exception) {
                        "Submitted recently"
                    }
                    Text(timeFormatted, fontSize = 11.5.sp, color = TextOnDarkPrimary)
                }

                HorizontalDivider(color = DarkBorder)

                // Change Report Status
                Text("Manage Report Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isDropdownExpanded = !isDropdownExpanded },
                        color = DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EcoGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: ${statusOptions.find { it.first == currentStatus }?.second ?: currentStatus.label}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                            Icon(imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = EcoGreenLight, modifier = Modifier.size(18.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(DarkSurfaceCard)
                    ) {
                        statusOptions.forEach { (st, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 12.sp, fontWeight = if (st == currentStatus) FontWeight.Bold else FontWeight.Medium, color = if (st == currentStatus) EcoGreenLight else TextOnDarkPrimary) },
                                onClick = {
                                    currentStatus = st
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStatusUpdated(currentStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Save Status", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    )
}



@Composable
fun AdminAuditLogsView() {
    var auditSubTab by remember { mutableIntStateOf(0) } // 0 = Admins Audit History, 1 = Truckers Activity

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Audit History", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
            Text("🕒 Audit logs automatically expire and delete after 15 days", fontSize = 10.sp, color = RecycleYellow)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = auditSubTab == 0,
                onClick = { auditSubTab = 0 },
                label = { Text("Admins Audit History", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight, containerColor = DarkSurfaceCard, labelColor = TextOnDarkSecondary)
            )
            FilterChip(
                selected = auditSubTab == 1,
                onClick = { auditSubTab = 1 },
                label = { Text("Truckers Activity", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight, containerColor = DarkSurfaceCard, labelColor = TextOnDarkSecondary)
            )
        }

        val filteredLogs = HakotRepository.auditLogs.filter { log ->
            val isTruckerActivity = log.action.contains("Trucker", ignoreCase = true) || log.action.contains("GPS", ignoreCase = true) || log.action.contains("Driver", ignoreCase = true)
            if (auditSubTab == 1) isTruckerActivity else !isTruckerActivity
        }

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Audit history list is empty.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )
                        Text(
                            text = "No recorded actions found for this view.",
                            fontSize = 11.sp,
                            color = TextOnDarkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.actingUserEmail, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                                Text(log.targetRecord, fontSize = 11.sp, color = TextOnDarkMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.action, fontSize = 12.sp, color = TextOnDarkPrimary)
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemInvitationModal(
    onDismiss: () -> Unit,
    onRedeemed: () -> Unit
) {
    val context = LocalContext.current
    var codeInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text("Redeem District Invitation Code", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                label = { Text("Invitation Code (e.g. HAKOT-XXXX)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Your Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (codeInput.isNotBlank() && emailInput.isNotBlank()) {
                        val success = HakotRepository.redeemInvitation(codeInput, emailInput)
                        if (success) {
                            Toast.makeText(context, "Invitation Redeemed Successfully!", Toast.LENGTH_SHORT).show()
                            onRedeemed()
                        } else {
                            Toast.makeText(context, "Invalid or expired invitation code.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Redeem Code", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountRegistrationScreen(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var orgNameInput by remember { mutableStateOf("") }
    var municipalityInput by remember { mutableStateOf("Nasugbu, Batangas") }
    var orgTypeInput by remember { mutableStateOf("Government / Municipal Office") }
    var fullNameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var contactInput by remember { mutableStateOf("") }
    var officeInput by remember { mutableStateOf("Municipal Sanitation Office") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var certUri by remember { mutableStateOf<Uri?>(null) }
    var certFileName by remember { mutableStateOf<String?>(null) }

    val certLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            certUri = uri
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.use {
                if (it.moveToFirst() && nameIndex != null && nameIndex >= 0) {
                    certFileName = it.getString(nameIndex)
                }
            }
            if (certFileName == null) {
                certFileName = uri.lastPathSegment ?: "Certificate_Document"
            }
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = onBackToLogin) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("← Back to Login", fontSize = 11.sp, color = EcoGreenLight)
                    }
                }

                Text(
                    text = "ORGANIZATION ACCESS APPLICATION",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                Text(
                    text = "Provide organization details and certificate of operation for System Owner review.",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSubmitted) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = EcoGreenLight, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Application Submitted Successfully!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your organization account application has been submitted and is currently PENDING System Owner review. You will receive an email activation code once approved.",
                                fontSize = 12.sp,
                                color = TextOnDarkPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackToLogin,
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("Return to Login", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = orgNameInput,
                        onValueChange = { orgNameInput = it; errorMessage = null },
                        label = { Text("Organization Name (Required)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = municipalityInput,
                        onValueChange = { municipalityInput = it; errorMessage = null },
                        label = { Text("Municipality / LGU Scope (Required)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Organization Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = orgTypeInput == "Government / Municipal Office",
                            onClick = { orgTypeInput = "Government / Municipal Office" },
                            label = { Text("Government / Municipal Office", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight)
                        )
                        FilterChip(
                            selected = orgTypeInput == "Private Company",
                            onClick = { orgTypeInput = "Private Company" },
                            label = { Text("Private Company", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it; errorMessage = null },
                        label = { Text("Administrator Full Name (Required)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; errorMessage = null },
                        label = { Text("Administrator Email Address (Login & Activation)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = { contactInput = it; errorMessage = null },
                        label = { Text("Contact Number (Mobile / Landline)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = officeInput,
                        onValueChange = { officeInput = it; errorMessage = null },
                        label = { Text("Office / Department") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = null },
                        label = { Text("Password (min 8 characters)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = TextOnDarkSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = TextOnDarkSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it; errorMessage = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password", tint = TextOnDarkSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = TextOnDarkSecondary
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EcoGreenPrimary, unfocusedBorderColor = DarkBorder, focusedTextColor = TextOnDarkPrimary, unfocusedTextColor = TextOnDarkPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Certificate of Operation Upload
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Mail, contentDescription = "Upload", tint = EcoGreenPrimary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Upload Certificate of Operation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Drag and drop an image here, paste a copied image, or browse files.",
                                fontSize = 11.sp,
                                color = TextOnDarkSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Supported formats: JPG, JPEG, PNG, PDF • Max file size: 5MB",
                                fontSize = 10.sp,
                                color = TextOnDarkMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (certFileName != null) {
                                Surface(
                                    color = EcoGreenContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, EcoGreenPrimary)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Selected", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = certFileName ?: "",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextOnDarkPrimary,
                                                maxLines = 1
                                            )
                                        }
                                        TextButton(onClick = { certUri = null; certFileName = null }) {
                                            Text("Remove", fontSize = 11.sp, color = HazardousRed)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { certLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = "Browse", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse Files", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val item = clip.getItemAt(0)
                                                val uri = item.uri
                                                if (uri != null) {
                                                    certUri = uri
                                                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                                                    val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                                    cursor?.use {
                                                        if (it.moveToFirst() && nameIndex != null && nameIndex >= 0) {
                                                            certFileName = it.getString(nameIndex)
                                                        }
                                                    }
                                                    if (certFileName == null) {
                                                        certFileName = uri.lastPathSegment ?: "Pasted_Certificate.png"
                                                    }
                                                    Toast.makeText(context, "Image pasted from clipboard successfully!", Toast.LENGTH_SHORT).show()
                                                } else if (item.text != null) {
                                                    Toast.makeText(context, "Clipboard contains text, not an image/file.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "No valid image found in clipboard.", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty.", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Paste failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenContainer),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = "Paste", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Paste Image", color = EcoGreenLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = HazardousRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            Log.d("HAKOT_SUBMIT_UI", "Submit organization account application clicked.")
                            if (orgNameInput.isBlank() || municipalityInput.isBlank() || fullNameInput.isBlank() || emailInput.isBlank() || contactInput.isBlank() || officeInput.isBlank() || passwordInput.isBlank() || confirmPasswordInput.isBlank()) {
                                errorMessage = "Please fill in all required registration fields, including passwords."
                            } else if (passwordInput.length < 8) {
                                errorMessage = "Password must be at least 8 characters long."
                            } else if (passwordInput != confirmPasswordInput) {
                                errorMessage = "Passwords do not match."
                            } else if (certUri == null) {
                                errorMessage = "Certificate of Operation upload is required."
                            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()) {
                                errorMessage = "Please enter a valid administrator email address."
                            } else {
                                isSubmitting = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(certUri!!)
                                        val bytes = inputStream?.readBytes()
                                        inputStream?.close()

                                        if (bytes == null || bytes.isEmpty()) {
                                            isSubmitting = false
                                            errorMessage = "Failed to read certificate file."
                                            return@launch
                                        }

                                        if (bytes.size > 5 * 1024 * 1024) {
                                            isSubmitting = false
                                            errorMessage = "Certificate file size exceeds 5MB limit."
                                            return@launch
                                        }

                                        val fileName = certFileName ?: "certificate.pdf"
                                        val ext = fileName.substringAfterLast('.', "").lowercase()
                                        val detectedMime = context.contentResolver.getType(certUri!!)

                                        val mimeType = when {
                                            ext == "pdf" -> "application/pdf"
                                            ext == "png" -> "image/png"
                                            ext == "jpg" || ext == "jpeg" -> "image/jpeg"
                                            !detectedMime.isNullOrBlank() && detectedMime != "application/octet-stream" -> detectedMime
                                            else -> "application/octet-stream"
                                        }

                                        val first16Hex = bytes.take(16).joinToString(" ") { String.format("%02X", it) }
                                        Log.d("HAKOT_PDF_INSPECT", "URI: $certUri | Name: $fileName | Resolved MIME: $mimeType | ContentResolver MIME: $detectedMime | Size: ${bytes.size} bytes | Header Hex: $first16Hex")

                                        val token = HakotRepository.getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
                                        val uploadRes = SupabaseClient.uploadCertificate(bytes, fileName, mimeType, token)
                                        if (!uploadRes.first) {
                                            isSubmitting = false
                                            errorMessage = "Certificate upload failed: ${uploadRes.second}"
                                            return@launch
                                        }

                                        val certPath = uploadRes.second

                                        val result = HakotRepository.submitAccountApplication(
                                            fullName = fullNameInput.trim(),
                                            email = emailInput.trim(),
                                            contactNumber = contactInput.trim(),
                                            municipalityId = HakotRepository.activeOrganization.municipalityId,
                                            officeDepartment = officeInput.trim(),
                                            organizationName = orgNameInput.trim(),
                                            organizationType = orgTypeInput,
                                            certificatePath = certPath,
                                            password = passwordInput
                                        )

                                        isSubmitting = false
                                        if (result.first) {
                                            isSubmitted = true
                                        } else {
                                            errorMessage = result.second
                                        }
                                    } catch (e: Exception) {
                                        isSubmitting = false
                                        errorMessage = "Submission exception: ${e.message}"
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.5.dp)
                        } else {
                            Text(
                                text = "SUBMIT ORGANIZATION APPLICATION",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountActivationScreen(
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var isCodeVerified by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TextButton(onClick = onBackToLogin) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("← Back to Login", fontSize = 11.sp, color = EcoGreenLight)
                    }
                }

                DumpTruckLogo()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ACTIVATE HAKOT ACCOUNT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenLight
                )
                Text(
                    text = "Enter your approved email and the activation code sent to your inbox.",
                    fontSize = 11.sp,
                    color = TextOnDarkSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it; errorMessage = null },
                    label = { Text("Registered Email Address") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Mail, contentDescription = "Email", tint = TextOnDarkSecondary) },
                    singleLine = true,
                    enabled = !isCodeVerified,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it; errorMessage = null },
                    label = { Text("Activation Code (6 digits)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = "Code", tint = TextOnDarkSecondary) },
                    singleLine = true,
                    enabled = !isCodeVerified,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        if (emailInput.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val (success, msg) = HakotRepository.resendActivationCode(emailInput)
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "New activation code sent to email.", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = msg
                                }
                            }
                        } else {
                            errorMessage = "Please enter your email address to resend code."
                        }
                    }) {
                        Text("Resend Code", fontSize = 11.sp, color = EcoGreenLight)
                    }

                    if (!isCodeVerified) {
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank() && codeInput.isNotBlank()) {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        val (success, msg) = HakotRepository.verifyActivationCode(emailInput, codeInput)
                                        isLoading = false
                                        if (success) {
                                            isCodeVerified = true
                                            Toast.makeText(context, "Code verified! Please set your password.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                } else {
                                    errorMessage = "Please enter both email and code."
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text("Verify Code", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (isCodeVerified) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Code Verified Successfully. Set Your Password:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it; errorMessage = null },
                        label = { Text("New Password (min 6 chars)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Password", tint = TextOnDarkSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = TextOnDarkSecondary
                                )
                            }
                        },
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it; errorMessage = null },
                        label = { Text("Confirm New Password") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Confirm Password", tint = TextOnDarkSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = TextOnDarkSecondary
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newPasswordInput.length >= 6 && newPasswordInput == confirmPasswordInput) {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    val (success, msg) = HakotRepository.activateAccountWithPassword(emailInput, codeInput, newPasswordInput)
                                    isLoading = false
                                    if (success) {
                                        successMessage = "Account activated successfully! You can now sign in."
                                        Toast.makeText(context, "Account activated successfully!", Toast.LENGTH_LONG).show()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else if (newPasswordInput.length < 6) {
                                errorMessage = "Password must be at least 6 characters long."
                            } else {
                                errorMessage = "Passwords do not match."
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = RecycleYellow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            Text("Set Password & Activate", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = HazardousRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }

                successMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = EcoGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onBackToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Proceed to Login", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDumpsitesView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAddDumpsiteModal by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var latInput by remember { mutableStateOf("14.0700") }
    var lonInput by remember { mutableStateOf("120.6300") }
    var isOperationalInput by remember { mutableStateOf(true) }
    var typeInput by remember { mutableStateOf("MRF / Disposal Facility") }
    var addressInput by remember { mutableStateOf("") }
    var acceptedWasteInput by remember { mutableStateOf("General Household Waste, Compostables & Recyclables") }
    var hoursInput by remember { mutableStateOf("6:00 AM - 6:00 PM") }
    var contactInput by remember { mutableStateOf("(043) 723-HAKOT") }

    LaunchedEffect(Unit) {
        HakotRepository.fetchDumpsitesFromSupabase(context)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Municipal Dumpsites & MRF Facilities", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Text("Manage municipal disposal sites and material recovery facilities", fontSize = 11.sp, color = TextOnDarkSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showAddDumpsiteModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dumpsite", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (HakotRepository.dumpsites.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EcoGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("No dumpsites registered in the database.", fontSize = 12.sp, color = TextOnDarkSecondary)
                    Button(
                        onClick = { showAddDumpsiteModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ ADD DUMPSITE / MRF FACILITY", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            HakotRepository.dumpsites.forEach { ds ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ds.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            Surface(
                                color = if (ds.status) EcoGreenContainer else HazardousRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (ds.status) EcoGreenPrimary else HazardousRed)
                            ) {
                                Text(
                                    text = if (ds.status) "OPERATIONAL" else "CLOSED / NON-OPERATIONAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ds.status) EcoGreenLight else HazardousRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("📍 Address: ${ds.address}", fontSize = 11.sp, color = EcoGreenLight)
                        Text("🌐 Coordinates (lat, lon): ${ds.latitude}, ${ds.longitude}", fontSize = 10.sp, color = TextOnDarkSecondary)
                        Text("🏛️ Type: ${ds.type} • Hours: ${ds.operatingHours}", fontSize = 10.sp, color = TextOnDarkSecondary)
                        Text("♻️ Accepted: ${ds.acceptedWasteTypes}", fontSize = 10.sp, color = TextOnDarkSecondary)
                        Text("📞 Contact: ${ds.contactNumber}", fontSize = 10.sp, color = TextOnDarkSecondary)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        val success = HakotRepository.deleteDumpsiteFromSupabase(ds.id, context)
                                        if (success) {
                                            HakotRepository.dumpsites.removeIf { it.id == ds.id }
                                            Toast.makeText(context, "Dumpsite deleted from DB", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            ) {
                                Text("Delete Dumpsite ✕", fontSize = 10.sp, color = HazardousRed)
                            }
                        }
                    }
                }
            }
        }
    }

    var selectedDsLat by remember { mutableStateOf(14.0700) }
    var selectedDsLon by remember { mutableStateOf(120.6300) }

    if (showAddDumpsiteModal) {
        AlertDialog(
            onDismissRequest = { showAddDumpsiteModal = false },
            title = { Text("Register New Dumpsite", color = EcoGreenLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Facility Name (Required)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address / Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📍 Tap Map to Pinpoint Facility Coordinates:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight
                    )
                    Text(
                        text = "Coordinates: ${String.format(Locale.US, "%.4f", selectedDsLat)}, ${String.format(Locale.US, "%.4f", selectedDsLon)}",
                        fontSize = 10.sp,
                        color = TextOnDarkSecondary
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, EcoGreenPrimary, RoundedCornerShape(10.dp))
                    ) {
                        DarkMapView(
                            modifier = Modifier.fillMaxSize(),
                            customRoutePoints = listOf(GeoPoint(selectedDsLat, selectedDsLon)),
                            onMapClick = { latLng ->
                                selectedDsLat = latLng.latitude
                                selectedDsLon = latLng.longitude
                                latInput = latLng.latitude.toString()
                                lonInput = latLng.longitude.toString()
                            }
                        )
                    }

                    OutlinedTextField(
                        value = latInput,
                        onValueChange = {
                            latInput = it
                            it.toDoubleOrNull()?.let { v -> selectedDsLat = v }
                        },
                        label = { Text("Latitude (lat)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lonInput,
                        onValueChange = {
                            lonInput = it
                            it.toDoubleOrNull()?.let { v -> selectedDsLon = v }
                        },
                        label = { Text("Longitude (lon)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Operational Status:", fontSize = 12.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isOperationalInput,
                            onCheckedChange = { isOperationalInput = it }
                        )
                    }
                    OutlinedTextField(
                        value = typeInput,
                        onValueChange = { typeInput = it },
                        label = { Text("Facility Type (e.g. MRF)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = acceptedWasteInput,
                        onValueChange = { acceptedWasteInput = it },
                        label = { Text("Accepted Waste Types") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { hoursInput = it },
                        label = { Text("Operating Hours") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = { contactInput = it },
                        label = { Text("Contact Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            scope.launch {
                                val latVal = latInput.toDoubleOrNull() ?: selectedDsLat
                                val lonVal = lonInput.toDoubleOrNull() ?: selectedDsLon
                                val newDs = Dumpsite(
                                    id = UUID.randomUUID().toString(),
                                    name = nameInput.trim(),
                                    latitude = latVal,
                                    longitude = lonVal,
                                    status = isOperationalInput,
                                    type = typeInput.trim(),
                                    address = addressInput.trim(),
                                    capacityPercent = 50,
                                    operatingHours = hoursInput.trim(),
                                    acceptedWasteTypes = acceptedWasteInput.trim(),
                                    isOpenNow = isOperationalInput,
                                    contactNumber = contactInput.trim()
                                )
                                val success = HakotRepository.saveDumpsiteToSupabase(newDs, context)
                                if (success) {
                                    HakotRepository.dumpsites.add(newDs)
                                    Toast.makeText(context, "Dumpsite registered successfully!", Toast.LENGTH_SHORT).show()
                                    showAddDumpsiteModal = false
                                    nameInput = ""
                                    addressInput = ""
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Save Dumpsite", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDumpsiteModal = false }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            },
            containerColor = DarkSurfaceCard
        )
    }
}

@Composable
fun AdminScheduleAuditView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var auditSubTab by remember { mutableIntStateOf(0) }
    var selectedCompletedIds by remember { mutableStateOf(setOf<String>()) }
    var selectedMissedIds by remember { mutableStateOf(setOf<String>()) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val allSchedules = HakotRepository.schedules

    val completedList = allSchedules.filter { 
        val st = it.status.uppercase()
        st == "COMPLETED" || st == "DONE" || st == "COMPLETE"
    }

    val missedList = allSchedules.filter { 
        val st = it.status.uppercase()
        st == "MISSED" || (it.date.isBefore(today) && st != "COMPLETED" && st != "DONE" && st != "COMPLETE" && !it.isWeeklyRepeat)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Schedule Audit & Resolution", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                Text("Audit completed schedules and resolve missed collection routes", fontSize = 11.sp, color = TextOnDarkSecondary)
            }
        }

        successMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(10.dp))
            ) {
                Text(it, color = EcoGreenLight, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = auditSubTab == 0,
                onClick = { auditSubTab = 0 },
                label = { Text("Completed Schedules (${completedList.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EcoGreenContainer, selectedLabelColor = EcoGreenLight, containerColor = DarkSurfaceCard, labelColor = TextOnDarkSecondary)
            )
            FilterChip(
                selected = auditSubTab == 1,
                onClick = { auditSubTab = 1 },
                label = { Text("Missed Schedules (${missedList.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HazardousRed.copy(alpha = 0.2f), selectedLabelColor = HazardousRed, containerColor = DarkSurfaceCard, labelColor = TextOnDarkSecondary)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (auditSubTab == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = completedList.isNotEmpty() && selectedCompletedIds.size == completedList.size,
                        onCheckedChange = { checked ->
                            selectedCompletedIds = if (checked) completedList.map { it.id }.toSet() else emptySet()
                        }
                    )
                    Text("Select All (${completedList.size})", fontSize = 11.sp, color = TextOnDarkPrimary)
                }

                Button(
                    onClick = {
                        if (selectedCompletedIds.isNotEmpty()) {
                            scope.launch {
                                var count = 0
                                selectedCompletedIds.forEach { id ->
                                    val success = HakotRepository.deleteScheduleFromSupabase(id, context)
                                    if (success) count++
                                }
                                successMsg = "Successfully approved and permanently deleted $count completed schedule(s) from DB."
                                selectedCompletedIds = emptySet()
                            }
                        }
                    },
                    enabled = selectedCompletedIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Approve Selected (${selectedCompletedIds.size})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            if (completedList.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("No completed schedules found for audit.", fontSize = 11.sp, color = TextOnDarkSecondary, modifier = Modifier.padding(16.dp))
                }
            } else {
                completedList.forEach { sch ->
                    val isSelected = selectedCompletedIds.contains(sch.id)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, if (isSelected) EcoGreenPrimary else DarkBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedCompletedIds = if (checked) selectedCompletedIds + sch.id else selectedCompletedIds - sch.id
                                    }
                                )
                                Column {
                                    Text(sch.routeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                    Text("Barangay: ${sch.barangay} • Date: ${sch.date}", fontSize = 10.sp, color = EcoGreenLight)
                                    Text("Assigned: ${sch.truckAssigned}", fontSize = 10.sp, color = TextOnDarkSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = HakotRepository.deleteScheduleFromSupabase(sch.id, context)
                                        if (success) {
                                            successMsg = "Approved & permanently deleted route '${sch.routeName}' from DB."
                                        } else {
                                            successMsg = "Failed to delete schedule."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Approve ✕", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = missedList.isNotEmpty() && selectedMissedIds.size == missedList.size,
                        onCheckedChange = { checked ->
                            selectedMissedIds = if (checked) missedList.map { it.id }.toSet() else emptySet()
                        }
                    )
                    Text("Select All (${missedList.size})", fontSize = 11.sp, color = TextOnDarkPrimary)
                }

                Button(
                    onClick = {
                        if (selectedMissedIds.isNotEmpty()) {
                            scope.launch {
                                var count = 0
                                selectedMissedIds.forEach { id ->
                                    val success = HakotRepository.deleteScheduleFromSupabase(id, context)
                                    if (success) count++
                                }
                                successMsg = "Successfully resolved and permanently deleted $count missed schedule(s) from DB."
                                selectedMissedIds = emptySet()
                            }
                        }
                    },
                    enabled = selectedMissedIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Resolve Selected (${selectedMissedIds.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            if (missedList.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("No missed schedules found.", fontSize = 11.sp, color = TextOnDarkSecondary, modifier = Modifier.padding(16.dp))
                }
            } else {
                missedList.forEach { sch ->
                    val isSelected = selectedMissedIds.contains(sch.id)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, if (isSelected) HazardousRed else DarkBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedMissedIds = if (checked) selectedMissedIds + sch.id else selectedMissedIds - sch.id
                                    }
                                )
                                Column {
                                    Text(sch.routeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                    Text("Barangay: ${sch.barangay} • Date: ${sch.date}", fontSize = 10.sp, color = HazardousRed)
                                    Text("Assigned: ${sch.truckAssigned}", fontSize = 10.sp, color = TextOnDarkSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = HakotRepository.deleteScheduleFromSupabase(sch.id, context)
                                        if (success) {
                                            successMsg = "Resolved & permanently deleted missed route '${sch.routeName}' from DB."
                                        } else {
                                            successMsg = "Failed to resolve schedule."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Resolve ✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnnouncementsView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAddModal by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("General") }
    var isPinnedInput by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }

    val categories = listOf("General", "Urgent", "Schedule Change", "Maintenance", "Community Alert")

    LaunchedEffect(Unit) {
        HakotRepository.fetchAnnouncementsFromSupabase(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Municipal Announcements",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Publish announcements and running ticker updates to citizen mobile screens",
                        fontSize = 11.sp,
                        color = TextOnDarkSecondary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Announcement", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (successMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenPrimary.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = successMsg!!,
                    color = EcoGreenPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (errorMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HazardousRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, HazardousRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMsg!!,
                    color = HazardousRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Active Announcements List or Centered Empty State
        if (HakotRepository.announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, EcoGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = EcoGreenPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No announcements published yet.",
                            fontSize = 13.sp,
                            color = TextOnDarkSecondary,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CREATE ANNOUNCEMENT",
                                color = Color.Black,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            HakotRepository.announcements.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (item.isPinned) EcoGreenPrimary else DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Category Tag
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
                                    shape = RoundedCornerShape(6.dp)
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
                                        shape = RoundedCornerShape(6.dp)
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

                            IconButton(
                                onClick = { announcementToDelete = item },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = HazardousRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDarkPrimary
                        )

                        Text(
                            text = item.message,
                            fontSize = 12.sp,
                            color = TextOnDarkSecondary,
                            lineHeight = 16.sp
                        )

                        val formattedDate = remember(item.timestampMillis) {
                            SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(
                                Date(item.timestampMillis)
                            )
                        }
                        Text(
                            text = "Published: $formattedDate",
                            fontSize = 10.sp,
                            color = TextOnDarkMuted
                        )
                    }
                }
            }
        }
    }

    // Modal to Add Announcement
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            containerColor = DarkSurfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Announcement", color = EcoGreenLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Announcement Title", fontSize = 11.sp) },
                        placeholder = { Text("e.g. Garbage Collection Delay in Barangay 4", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = EcoGreenPrimary,
                            unfocusedLabelColor = TextOnDarkMuted,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text("Announcement Message / Details", fontSize = 11.sp) },
                        placeholder = { Text("Write full notice details here...", fontSize = 11.sp) },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcoGreenPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = EcoGreenPrimary,
                            unfocusedLabelColor = TextOnDarkMuted,
                            focusedTextColor = TextOnDarkPrimary,
                            unfocusedTextColor = TextOnDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category:", fontSize = 11.sp, color = TextOnDarkSecondary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = categoryInput == cat,
                                onClick = { categoryInput = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoGreenPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextOnDarkSecondary
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPinnedInput = !isPinnedInput }
                    ) {
                        Checkbox(
                            checked = isPinnedInput,
                            onCheckedChange = { isPinnedInput = it },
                            colors = CheckboxDefaults.colors(checkedColor = EcoGreenPrimary)
                        )
                        Text("Pin this announcement to top", fontSize = 11.sp, color = TextOnDarkPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isBlank() || messageInput.isBlank()) {
                            Toast.makeText(context, "Please fill in both title and message", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val newAnno = Announcement(
                                id = UUID.randomUUID().toString(),
                                title = titleInput.trim(),
                                message = messageInput.trim(),
                                category = categoryInput,
                                isPinned = isPinnedInput,
                                timestampMillis = System.currentTimeMillis()
                            )
                            val res = HakotRepository.saveAnnouncementToSupabase(newAnno, context)
                            isSubmitting = false
                            if (res.first) {
                                successMsg = "Announcement published successfully!"
                                errorMsg = null
                                showAddModal = false
                                titleInput = ""
                                messageInput = ""
                            } else {
                                successMsg = res.second
                                showAddModal = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Publish", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal = false }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            }
        )
    }

    // Delete confirmation dialog
    if (announcementToDelete != null) {
        val target = announcementToDelete!!
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            containerColor = DarkSurfaceCard,
            title = { Text("Delete Announcement", color = HazardousRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' from database?", color = TextOnDarkPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            HakotRepository.deleteAnnouncementFromSupabase(target.id, context)
                            announcementToDelete = null
                            Toast.makeText(context, "Announcement deleted.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel", color = TextOnDarkSecondary)
                }
            }
        )
    }
}

private fun openStorageConnection(p: String, tok: String): HttpURLConnection {
    val cleanPath = p.removePrefix("certificates/").removePrefix("/").trim()
    val urls = listOf(
        "${SupabaseClient.SUPABASE_URL}/storage/v1/object/authenticated/certificates/$cleanPath",
        "${SupabaseClient.SUPABASE_URL}/storage/v1/object/public/certificates/$cleanPath",
        "${SupabaseClient.SUPABASE_URL}/storage/v1/object/authenticated/$p",
        "${SupabaseClient.SUPABASE_URL}/storage/v1/object/public/$p"
    )
    var lastConn: HttpURLConnection? = null
    for (urlStr in urls) {
        try {
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                if (tok.isNotBlank() && tok != SupabaseClient.SUPABASE_ANON_KEY) {
                    setRequestProperty("Authorization", "Bearer $tok")
                }
                connectTimeout = 15000
                readTimeout = 15000
            }
            val code = conn.responseCode
            if (code in 200..299) {
                return conn
            } else {
                lastConn?.disconnect()
                lastConn = conn
            }
        } catch (_: Exception) {}
    }
    return lastConn ?: throw Exception("Object not found in storage")
}



