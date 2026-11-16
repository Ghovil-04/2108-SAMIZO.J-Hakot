package com.example.hakot.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hakot.ui.components.DarkMapView
import com.example.hakot.model.Route
import com.example.hakot.model.Dumptrack
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
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.hakot.DumpTruckLogo
import com.example.hakot.data.HakotRepository
import com.example.hakot.model.AccountApplication
import com.example.hakot.model.AccountInvitation
import com.example.hakot.model.AdminStatus
import com.example.hakot.model.AdminUserProfile
import com.example.hakot.model.AnonymousReport
import com.example.hakot.model.ApplicationStatus
import com.example.hakot.model.ReportStatus
import com.example.hakot.model.UserRole
import com.example.hakot.ui.theme.DarkBackground
import com.example.hakot.ui.theme.DarkBorder
import com.example.hakot.ui.theme.DarkSurface
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.DarkSurfaceVariant
import com.example.hakot.ui.theme.EcoGreenContainer
import com.example.hakot.ui.theme.EcoGreenLight
import com.example.hakot.ui.theme.EcoGreenPrimary
import com.example.hakot.ui.theme.HazardousRed
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
import java.time.LocalDateTime
import java.time.ZoneId
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
                    "District Overview",
                    "Account Management",
                    "Account Applications",
                    "Collection Schedule",
                    "Fleet Trucks",
                    "Citizen Reports",
                    "Audit History"
                )
            } else {
                listOf(
                    "District Overview",
                    "Account Management",
                    "Collection Schedule",
                    "Fleet Trucks",
                    "Citizen Reports",
                    "Audit History"
                )
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                // Top Admin Banner with Sign Out
                Surface(
                    color = EcoGreenContainer,
                    border = BorderStroke(1.dp, EcoGreenPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "District Admin",
                                tint = EcoGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MUNICIPAL SANITATION PORTAL",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDarkPrimary
                                )
                                Text(
                                    text = HakotRepository.currentDistrictAdmin.districtName,
                                    fontSize = 11.sp,
                                    color = EcoGreenLight
                                )
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    HakotRepository.logout(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = HazardousRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign Out", fontSize = 11.sp, color = TextOnDarkPrimary)
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
                    when (selectedAdminTab) {
                        0 -> DistrictOverviewView(
                            onRedeemCode = { showRedeemModal = true }
                        )
                        1 -> AdminAccountManagementView(
                            onOpenCreateModal = { showCreateAccountModal = true }
                        )
                        2 -> if (userRole == UserRole.SYSTEM_OWNER) {
                            SystemOwnerApplicationsReviewView()
                        } else {
                            CollectionScheduleDashboardView()
                        }
                        3 -> if (userRole == UserRole.SYSTEM_OWNER) {
                            CollectionScheduleDashboardView()
                        } else {
                            AdminTrucksView()
                        }
                        4 -> if (userRole == UserRole.SYSTEM_OWNER) {
                            AdminTrucksView()
                        } else {
                            AdminReportsView()
                        }
                        5 -> if (userRole == UserRole.SYSTEM_OWNER) {
                            AdminReportsView()
                        } else {
                            AdminAuditLogsView()
                        }
                        6 -> if (userRole == UserRole.SYSTEM_OWNER) {
                            AdminAuditLogsView()
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
fun SystemOwnerApplicationsReviewView() {
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
                            Text("Municipality: ${app.municipality}", fontSize = 11.sp, color = TextOnDarkSecondary)

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("Detailed Application Information:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Organization Name: ${app.organizationName.ifBlank { "N/A" }}", fontSize = 11.sp, color = TextOnDarkSecondary)
                                Text("• Organization Type: ${app.organizationType}", fontSize = 11.sp, color = TextOnDarkSecondary)
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
                                Text("• Requested Role: ${app.requestedRole.label}", fontSize = 11.sp, color = TextOnDarkSecondary)

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
        val certUrl = "${SupabaseClient.SUPABASE_URL}/storage/v1/object/authenticated/$path"
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
                        val conn = (URL(certUrl).openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                            setRequestProperty("Authorization", "Bearer $token")
                            connectTimeout = 15000
                            readTimeout = 15000
                        }
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

                    if (isImage) {
                        if (isLoadingImage) {
                            CircularProgressIndicator(color = EcoGreenPrimary, modifier = Modifier.size(36.dp))
                        } else if (loadError != null) {
                            Text(loadError ?: "Error loading image", color = HazardousRed, fontSize = 12.sp)
                        } else if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Certificate Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    } else if (isPdf) {
                        var isDownloadingPdf by remember { mutableStateOf(false) }
                        var pdfError by remember { mutableStateOf<String?>(null) }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = "PDF", tint = EcoGreenPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("PDF Document Certificate", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                            Spacer(modifier = Modifier.height(6.dp))

                            pdfError?.let {
                                Text(it, fontSize = 11.sp, color = HazardousRed)
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Button(
                                enabled = !isDownloadingPdf,
                                onClick = {
                                    scope.launch {
                                        isDownloadingPdf = true
                                        pdfError = null
                                        try {
                                            val token = HakotRepository.getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
                                            val downloadedFile: File = withContext(Dispatchers.IO) {
                                                val conn = (URL(certUrl).openConnection() as HttpURLConnection).apply {
                                                    requestMethod = "GET"
                                                    setRequestProperty("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                                                    setRequestProperty("Authorization", "Bearer $token")
                                                    connectTimeout = 20000
                                                    readTimeout = 20000
                                                }
                                                val code = conn.responseCode
                                                if (code !in 200..299) {
                                                    val errStr = try { conn.errorStream?.bufferedReader()?.use { it.readText() } } catch (_: Exception) { null }
                                                    conn.disconnect()
                                                    throw Exception("HTTP $code: ${errStr ?: "Server returned error"}")
                                                }

                                                val file = File(context.cacheDir, "certificate_preview.pdf")
                                                file.outputStream().use { out ->
                                                    conn.inputStream.copyTo(out)
                                                }
                                                conn.disconnect()

                                                if (!file.exists() || file.length() < 10) {
                                                    file.delete()
                                                    throw Exception("Downloaded PDF file is empty or incomplete.")
                                                }

                                                val headerBytes = ByteArray(4)
                                                file.inputStream().use { stream ->
                                                    stream.read(headerBytes)
                                                }
                                                val headerStr = String(headerBytes, Charsets.US_ASCII)
                                                if (headerStr != "%PDF") {
                                                    file.delete()
                                                    throw Exception("Downloaded file is not a valid PDF document.")
                                                }

                                                file
                                            }

                                            val contentUri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                downloadedFile
                                            )

                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(contentUri, "application/pdf")
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            pdfError = "Failed to download/open PDF: ${e.message}"
                                        } finally {
                                            isDownloadingPdf = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isDownloadingPdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Downloading...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Open PDF Document", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Text("Document format not previewable directly.", fontSize = 12.sp, color = TextOnDarkSecondary)
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
fun AdminLoginScreen(
    onOpenRedeemModal: () -> Unit,
    onOpenRegistration: () -> Unit,
    onOpenActivation: () -> Unit,
    onExitAdminPortal: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        Toast.makeText(context, "Password reset: Please contact your system administrator or check email instructions.", Toast.LENGTH_LONG).show()
                    }) {
                        Text("Forgot Password?", fontSize = 11.sp, color = EcoGreenLight)
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

                TextButton(onClick = onOpenRedeemModal) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Code", tint = EcoGreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Redeem Invitation Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onOpenRegistration) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Register", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Applicant? Register for Access", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onOpenActivation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Activate", tint = EcoGreenLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Have an Activation Code? Activate Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onExitAdminPortal) {
                    Text("Return to Public Citizen Mode", fontSize = 12.sp, color = TextOnDarkMuted)
                }
            }
        }
    }
}



@Composable
fun DriverDashboardView(onSignOut: () -> Unit) {
    val driver = HakotRepository.authenticatedUser
    val truck = HakotRepository.trucks.firstOrNull()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, EcoGreenPrimary, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TRUCK DRIVER OPERATOR PORTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                            Text(driver?.fullName ?: "Mario Santos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                        }

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sign Out", fontSize = 11.sp, color = TextOnDarkPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("🚚 Assigned Vehicle: ${truck?.truckCode} (${truck?.plateNumber})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                    Text("📍 Route: ${truck?.assignedRoute}", fontSize = 12.sp, color = EcoGreenLight)
                    Text("🏁 Destination: ${truck?.destinationDumpsite}", fontSize = 12.sp, color = TextOnDarkSecondary)
                }
            }
        }

        item {
            Text("TODAY'S DRIVER COLLECTION SCHEDULE WINDOW", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextOnDarkMuted)
        }

        items(HakotRepository.schedules) { sch ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(sch.routeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                    Text("📍 ${sch.barangay}", fontSize = 12.sp, color = EcoGreenLight)
                    Text("Time Window: 8:00 AM - 11:30 AM • Status: ${sch.status}", fontSize = 11.sp, color = TextOnDarkSecondary)
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
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = TextOnDarkMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun DistrictOverviewView(
    onRedeemCode: () -> Unit
) {
    val admin = HakotRepository.currentDistrictAdmin
    val org = HakotRepository.activeOrganization

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = admin.fullName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextOnDarkPrimary
                            )
                            Text(
                                text = admin.email,
                                fontSize = 12.sp,
                                color = TextOnDarkSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(EcoGreenContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(admin.status.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🏛️ Assigned Municipality: ${org.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnDarkPrimary
                    )
                    Text(
                        text = "📍 Scope: ${admin.districtName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
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
                MetricCard(title = "Barangays", value = "${org.coveredBarangays.size} Covered", color = EcoGreenPrimary, modifier = Modifier.weight(1f))
                MetricCard(title = "Assigned Trucks", value = "${HakotRepository.trucks.size} Trucks", color = RecycleYellow, modifier = Modifier.weight(1f))
                MetricCard(title = "Active Routes", value = "${HakotRepository.routes.size} Corridors", color = EcoGreenLight, modifier = Modifier.weight(1f))
            }
        }

        item {
            Button(
                onClick = onRedeemCode,
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Key",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Redeem Invitation Code", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "AUTHORIZED DISTRICT COLLECTION CORRIDORS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextOnDarkMuted
            )
        }

        items(HakotRepository.routes) { route ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(route.routeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                    Text("📍 ${route.barangaysCovered}", fontSize = 11.sp, color = TextOnDarkSecondary)
                }
            }
        }
    }
}



@Composable
fun AdminAccountManagementView(
    onOpenCreateModal: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<AdminStatus?>(null) }
    var selectedSubTab by remember { mutableIntStateOf(0) }

    var selectedAccountForDetails by remember { mutableStateOf<AdminUserProfile?>(null) }
    var selectedInvitationForDetails by remember { mutableStateOf<AccountInvitation?>(null) }

    val totalAdmins = HakotRepository.adminProfiles.count {
        it.role == UserRole.MUNICIPAL_ADMIN || it.role == UserRole.DISTRICT_ADMIN || it.role == UserRole.SYSTEM_OWNER
    }
    val activeCount = HakotRepository.adminProfiles.count { it.status == AdminStatus.ACTIVE }
    val pendingCount = HakotRepository.accountInvitations.count { it.status == AdminStatus.PENDING_INVITATION }
    val suspendedCount = HakotRepository.adminProfiles.count { it.status == AdminStatus.SUSPENDED }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
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

        item {
            Card(
                onClick = onOpenCreateModal,
                colors = CardDefaults.cardColors(containerColor = EcoGreenContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EcoGreenPrimary, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Invite Account",
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "+ INVITE MUNICIPAL ACCOUNT VIA EMAIL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreenLight
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedSubTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSubTab == 0) EcoGreenPrimary else DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Active Accounts (${HakotRepository.adminProfiles.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == 0) Color.Black else TextOnDarkPrimary
                        )
                    }

                    Button(
                        onClick = { selectedSubTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSubTab == 1) EcoGreenPrimary else DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Pending Invites (${HakotRepository.accountInvitations.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSubTab == 1) Color.Black else TextOnDarkPrimary
                        )
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            val filteredProfiles = HakotRepository.adminProfiles.filter { prof ->
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
                        onClick = { selectedAccountForDetails = prof }
                    )
                }
            }
        } else {
            val filteredInvitations = HakotRepository.accountInvitations.filter { inv ->
                searchQuery.isBlank() ||
                        inv.intendedName.contains(searchQuery, ignoreCase = true) ||
                        inv.intendedEmail.contains(searchQuery, ignoreCase = true) ||
                        inv.positionTitle.contains(searchQuery, ignoreCase = true)
            }

            if (filteredInvitations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending email invitations found.",
                            fontSize = 12.sp,
                            color = TextOnDarkMuted
                        )
                    }
                }
            } else {
                items(filteredInvitations) { inv ->
                    InvitationCardItem(
                        invitation = inv,
                        onClick = { selectedInvitationForDetails = inv }
                    )
                }
            }
        }
    }

    if (selectedAccountForDetails != null) {
        AccountDetailsModal(
            profile = selectedAccountForDetails!!,
            onDismiss = { selectedAccountForDetails = null }
        )
    }

    if (selectedInvitationForDetails != null) {
        InvitationDetailsModal(
            invitation = selectedInvitationForDetails!!,
            onDismiss = { selectedInvitationForDetails = null }
        )
    }
}

@Composable
fun AccountCardItem(
    profile: AdminUserProfile,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsModal(
    profile: AdminUserProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
                text = "Account Details & Permissions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Full Name: ${profile.fullName}", fontSize = 14.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
            Text("Email: ${profile.email}", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Role: ${profile.role.label}", fontSize = 12.sp, color = EcoGreenLight)
            Text("Municipality: ${profile.municipalityName}", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Position: ${profile.positionTitle} (${profile.officeDepartment})", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Status: ${profile.status.label}", fontSize = 12.sp, color = if (profile.status == AdminStatus.ACTIVE) EcoGreenLight else HazardousRed)

            Spacer(modifier = Modifier.height(20.dp))

            Text("Account Actions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextOnDarkMuted)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (profile.status == AdminStatus.ACTIVE) {
                    Button(
                        onClick = {
                            HakotRepository.suspendAccount(profile.userId)
                            Toast.makeText(context, "Account Suspended (Backend Pending)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Suspend", fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            HakotRepository.reactivateAccount(profile.userId)
                            Toast.makeText(context, "Account Reactivated (Backend Pending)", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reactivate", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        HakotRepository.revokeAccount(profile.userId)
                        Toast.makeText(context, "Account Revoked (Backend Pending)", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Revoke", fontSize = 12.sp, color = HazardousRed)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------------------------------------------------------------------
// INVITATION DETAILS MODAL SHEET
// ----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationDetailsModal(
    invitation: AccountInvitation,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
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
                text = "Pending Email Invitation Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDarkPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Intended Recipient: ${invitation.intendedName}", fontSize = 14.sp, color = TextOnDarkPrimary, fontWeight = FontWeight.Bold)
            Text("Email: ${invitation.intendedEmail}", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Contact Phone: ${invitation.contactNumber}", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Assigned Role: ${invitation.assignedRole.label}", fontSize = 12.sp, color = RecycleYellow)
            Text("Municipality: ${invitation.municipalityName}", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Position: ${invitation.positionTitle} (${invitation.officeDepartment})", fontSize = 12.sp, color = TextOnDarkSecondary)
            Text("Invitation Code Display: ${invitation.rawCodeDisplay}", fontSize = 12.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
            Text("Status: ${invitation.status.label}", fontSize = 12.sp, color = RecycleYellow)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val res = HakotRepository.resendAccountInvitation(invitation.invitationId)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resend Email", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        HakotRepository.revokeAccountInvitation(invitation.invitationId)
                        Toast.makeText(context, "Invitation Revoked (Backend Pending)", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HazardousRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Revoke Invite", fontSize = 12.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
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
    onSaveScheduledRoute: (String, String, String, LocalDate, LocalTime, LocalTime, String, Boolean, LocalDate?, List<GisRoadSegmentFeature>) -> Unit
) {
    var isGisLoading by remember { mutableStateOf(!GisManager.isLoaded) }

    LaunchedEffect(Unit) {
        if (!GisManager.isLoaded) {
            isGisLoading = true
            GisManager.loadGisData()
            isGisLoading = false
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
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            DarkMapView(
                modifier = Modifier.fillMaxSize(),
                customRoutePoints = routeGeoPoints,
                customRouteSegments = routeSegmentsList,
                barangayRoads = availableRoads,
                selectedRoadIds = selectedRoadSegmentIds
            )

        // Top Toolbar
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
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
                Button(
                    onClick = {
                        if (selectedGisSegments.isNotEmpty()) {
                            showReviewDialog = true
                        }
                    },
                    enabled = selectedGisSegments.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Finish Route", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Selection Panel with Searchable Barangay Chips & Available Roads Checklist
        Surface(
            color = DarkSurfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1. Select Barangay (${GisManager.barangays.size} Available):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    Text("Selected: ${selectedRoadSegmentIds.size} roads", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                }

                OutlinedTextField(
                    value = bgySearchQuery,
                    onValueChange = { bgySearchQuery = it },
                    label = { Text("Search barangay (e.g. Barangay 6, Aga)...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredBarangays) { bgyName ->
                            val isSelected = selectedBarangay == bgyName
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (selectedBarangay != bgyName) {
                                        selectedBarangay = bgyName
                                        selectedRoadSegmentIds = emptyList()
                                    }
                                },
                                label = { Text(bgyName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoGreenContainer,
                                    selectedLabelColor = EcoGreenLight,
                                    containerColor = DarkSurface,
                                    labelColor = TextOnDarkSecondary
                                )
                            )
                        }
                    }
                }

                if (selectedGisSegments.isNotEmpty()) {
                    Text("Selected Roads Sequence & Direction (Flip to reverse):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
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
                                    Text("${idx + 1}. ${seg.segmentName.take(16)}...", fontSize = 10.sp, color = TextOnDarkPrimary)
                                    TextButton(
                                        onClick = {
                                            seg.isReversed = !seg.isReversed
                                            flipStateTrigger++
                                        },
                                        contentPadding = PaddingValues(2.dp),
                                        modifier = Modifier.size(54.dp, 24.dp)
                                    ) {
                                        Text(if (seg.isReversed) "Rev ⇄" else "Fwd ⇄", fontSize = 9.sp, color = EcoGreenLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Text("2. Available Roads in $selectedBarangay (${filteredAvailableRoads.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)

                OutlinedTextField(
                    value = roadSearchQuery,
                    onValueChange = { roadSearchQuery = it },
                    label = { Text("Search streets in $selectedBarangay...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreenPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextOnDarkPrimary,
                        unfocusedTextColor = TextOnDarkPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    if (filteredAvailableRoads.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No road segments found in $selectedBarangay.", fontSize = 11.sp, color = TextOnDarkSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            items(filteredAvailableRoads) { road ->
                                val isChecked = selectedRoadSegmentIds.contains(road.id)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (isChecked) EcoGreenContainer.copy(alpha = 0.4f) else DarkSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRoadSegmentIds = if (isChecked) selectedRoadSegmentIds - road.id else selectedRoadSegmentIds + road.id
                                        }
                                        .border(1.dp, if (isChecked) EcoGreenPrimary else DarkBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(road.segmentName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
                                            Text("Type: ${road.roadType}", fontSize = 9.sp, color = TextOnDarkSecondary)
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

                    Text("Assigned Truck: $assignedTruckInput", fontSize = 11.sp, color = TextOnDarkSecondary)

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

                                val date: LocalDate = try {
                                    LocalDate.parse(collectionDateInput.trim())
                                } catch (_: Exception) {
                                    LocalDate.now()
                                }
                                val repeatWeekly: Boolean = (scheduleType == "DAY_OF_WEEK")
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
                                    assignedTruckInput,
                                    repeatWeekly,
                                    endRecur,
                                    selectedGisSegments
                                )
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
fun CollectionScheduleDashboardView() {
    val context = LocalContext.current
    val zoneId = ZoneId.of("Asia/Manila")
    var showSchedulePlanner by remember { mutableStateOf(false) }
    var selectedScheduleDetail by remember { mutableStateOf<ScheduleItem?>(null) }
    var scheduleToDelete by remember { mutableStateOf<ScheduleItem?>(null) }
    var isLoadingSchedules by remember { mutableStateOf(false) }
    var selectedWeekStart by remember { mutableStateOf(LocalDate.now(zoneId).with(DayOfWeek.MONDAY)) }
    var selectedDay by remember { mutableStateOf(LocalDate.now(zoneId)) }
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

    val schedulesForSelectedDay = HakotRepository.schedules.filter {
        HakotRepository.isScheduleActiveOnDate(it, selectedDay)
    }

    if (showSchedulePlanner) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            MapRoutePlannerScreen(
                initialDate = selectedDay,
                onDismiss = { showSchedulePlanner = false },
                onSaveScheduledRoute = { routeName, scheduleName, barangayName, date, startTime, endTime, truck, repeatWeekly, repeatEnd, segments ->
                    val newSchedule = ScheduleItem(
                        id = "sch_${System.currentTimeMillis()}",
                        barangay = barangayName,
                        routeName = routeName,
                        date = date,
                        startTime = startTime,
                        endTime = endTime,
                        wasteCategory = WasteCategory.GENERAL_WASTE,
                        truckAssigned = truck,
                        status = "Scheduled",
                        isWeeklyRepeat = repeatWeekly,
                        recurrenceEndDate = repeatEnd,
                        isDisabled = false,
                        orderedSegments = segments
                    )

                    scope.launch {
                        val schedRes = HakotRepository.saveScheduledRouteToSupabase(newSchedule, context)
                        if (schedRes.first) {
                            selectedDay = date
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    daysOfWeek.forEach { day ->
                        val isSelected = day == selectedDay
                        val hasSchedules = HakotRepository.schedules.any {
                            HakotRepository.isScheduleActiveOnDate(it, day)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EcoGreenPrimary else if (hasSchedules) EcoGreenContainer else DarkSurface)
                                .clickable { selectedDay = day }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(day.dayOfWeek.name.take(3), fontSize = 10.sp, color = if (isSelected) Color.Black else TextOnDarkSecondary)
                            Text("${day.dayOfMonth}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else TextOnDarkPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text("Scheduled Routes for $selectedDay (${schedulesForSelectedDay.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextOnDarkPrimary)
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No collection routes scheduled for this day.", fontSize = 11.sp, color = TextOnDarkSecondary, modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                schedulesForSelectedDay.forEach { sch ->
                    val isToday = sch.date == today
                    val statusLabel = when {
                        sch.isDisabled -> "Disabled"
                        sch.date.isAfter(today) -> "Upcoming"
                        isToday && now.toLocalTime().isBefore(sch.startTime) -> "Upcoming"
                        isToday && !now.toLocalTime().isAfter(sch.endTime) -> "Active"
                        else -> "Completed / Expired"
                    }

                    val statusColor = when (statusLabel) {
                        "Active" -> EcoGreenLight
                        "Upcoming" -> EcoGreenLight
                        "Disabled" -> HazardousRed
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
                            .border(1.dp, if (sch.isDisabled) TextOnDarkSecondary.copy(alpha = 0.3f) else if (statusLabel == "Active") EcoGreenPrimary else DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sch.routeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (sch.isDisabled) TextOnDarkSecondary else TextOnDarkPrimary)
                                Surface(
                                    color = if (sch.isDisabled) DarkSurface else if (statusLabel == "Active") EcoGreenContainer else DarkSurface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (sch.isDisabled) TextOnDarkSecondary else statusColor)
                                ) {
                                    Text(
                                        text = if (sch.isDisabled) "Inactive / Pale" else statusLabel,
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
                                        checked = !sch.isDisabled,
                                        onCheckedChange = { isChecked ->
                                            scope.launch {
                                                val updated = sch.copy(isDisabled = !isChecked)
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
                                    Text(if (sch.isDisabled) "Inactive" else "Active", fontSize = 11.sp, color = if (sch.isDisabled) TextOnDarkSecondary else EcoGreenLight, fontWeight = FontWeight.Bold)
                                }

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
                        Text("🗓️ Collection Date: ${sch.date}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("⏰ Time Window: ${sch.startTime} - ${sch.endTime}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("🚛 Assigned Truck: ${sch.truckAssigned}", fontSize = 12.sp, color = TextOnDarkPrimary)
                        Text("📊 Status: ${sch.status}", fontSize = 12.sp, color = TextOnDarkPrimary)
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
    var showAddTruckDialog by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    var truckCodeInput by remember { mutableStateOf("Hakot Truck #${HakotRepository.trucks.size + 1}") }
    var plateNumberInput by remember { mutableStateOf("XYZ-${(1000..9999).random()}") }
    var driverNameInput by remember { mutableStateOf("") }
    var assignedRouteInput by remember { mutableStateOf("Nasugbu Main Route") }
    var destinationInput by remember { mutableStateOf("Nasugbu MRF Facility") }

    LaunchedEffect(Unit) {
        HakotRepository.fetchTrucksFromSupabase(context)
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
            Column {
                Text("Fleet Trucks Management", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                Text("Manage municipal collection trucks & drivers", fontSize = 11.sp, color = TextOnDarkSecondary)
            }
            Button(
                onClick = { showAddTruckDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Add New Truck", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        if (HakotRepository.trucks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                            Text("👤 Driver: ${tr.driverName}", fontSize = 12.sp, color = EcoGreenLight)
                            Text("📍 Assigned Route: ${tr.assignedRoute}", fontSize = 11.sp, color = TextOnDarkSecondary)
                            Text("🏁 Destination: ${tr.destinationDumpsite}", fontSize = 11.sp, color = TextOnDarkSecondary)
                        }
                    }
                }
            }
        }
    }

    if (showAddTruckDialog) {
        AlertDialog(
            onDismissRequest = { showAddTruckDialog = false },
            title = { Text("Register New Fleet Truck", color = TextOnDarkPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = truckCodeInput,
                        onValueChange = { truckCodeInput = it },
                        label = { Text("Truck Code / Name (e.g. Hakot #4)") },
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
                    OutlinedTextField(
                        value = driverNameInput,
                        onValueChange = { driverNameInput = it },
                        label = { Text("Assigned Driver Name") },
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
                        if (plateNumberInput.isNotBlank() && driverNameInput.isNotBlank()) {
                            scope.launch {
                                val newTruck = Dumptrack(
                                    id = "tr_${System.currentTimeMillis()}",
                                    truckCode = truckCodeInput.trim().ifBlank { "Hakot Truck" },
                                    plateNumber = plateNumberInput.trim(),
                                    driverName = driverNameInput.trim(),
                                    assignedRoute = "Collection Route",
                                    destinationDumpsite = "Nasugbu MRF Facility",
                                    latitude = 14.0725,
                                    longitude = 120.6335
                                )
                                val res = HakotRepository.saveTruckToSupabase(newTruck, context)
                                if (res.first) {
                                    successMsg = "Successfully registered truck '${newTruck.truckCode}' to Supabase!"
                                    showAddTruckDialog = false
                                } else {
                                    successMsg = "Error saving truck: ${res.second}"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Save to Database", color = Color.Black, fontWeight = FontWeight.Bold)
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
}



@Composable
fun AdminReportsView() {
    var selectedReportForChat by remember { mutableStateOf<AnonymousReport?>(null) }

    LaunchedEffect(Unit) {
        HakotRepository.fetchAnonymousReportsFromSupabase()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(HakotRepository.reports) { rep ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedReportForChat = rep }
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rep.referenceNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenLight
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rep.reportType.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HazardousRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcoGreenContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(rep.status.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoGreenLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "📍 ${rep.locationName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnDarkPrimary
                    )
                    Text(
                        text = rep.description,
                        fontSize = 11.sp,
                        color = TextOnDarkSecondary
                    )
                }
            }
        }
    }
}



@Composable
fun AdminAuditLogsView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(HakotRepository.auditLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
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
                                color = TextOnDarkSecondary,
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.5.dp)
                        } else {
                            Text("SUBMIT ORGANIZATION APPLICATION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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


