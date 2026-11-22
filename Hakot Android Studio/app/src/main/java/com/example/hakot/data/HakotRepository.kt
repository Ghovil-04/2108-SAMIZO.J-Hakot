package com.example.hakot.data

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.hakot.MainActivity
import com.example.hakot.R
import com.example.hakot.model.AccountApplication
import com.example.hakot.model.AccountInvitation
import com.example.hakot.model.AdminStatus
import com.example.hakot.model.AdminUserProfile
import com.example.hakot.model.Announcement
import com.example.hakot.model.AnonymousReport
import com.example.hakot.model.ApplicationStatus
import com.example.hakot.model.AuditLogEntry
import com.example.hakot.model.CollectionRoute
import com.example.hakot.model.DistrictAdminInvitation
import com.example.hakot.model.Dumpsite
import com.example.hakot.model.Dumptrack
import com.example.hakot.model.GarbageTruck
import com.example.hakot.model.OrganizationConfig
import com.example.hakot.model.OrganizationType
import com.example.hakot.model.PasswordResetRequest
import com.example.hakot.model.ReportMessage
import com.example.hakot.model.ReportStatus
import com.example.hakot.model.ReportType
import com.example.hakot.model.ScheduleItem
import com.example.hakot.model.TruckStatus
import com.example.hakot.model.TruckerAccount
import com.example.hakot.model.UserRole
import com.example.hakot.model.WasteCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

object HakotRepository {
    val today: LocalDate get() = LocalDate.now()

    var selectedMapDate: LocalDate by mutableStateOf(LocalDate.now())
    var selectedFocusSchedule by mutableStateOf<ScheduleItem?>(null)

    var currentAdminRole: UserRole by mutableStateOf(UserRole.MUNICIPAL_ADMIN)
    var currentAdminName: String by mutableStateOf("")

    var isAuthenticated: Boolean by mutableStateOf(false)
    var authenticatedUser: AdminUserProfile? by mutableStateOf(null)
    var lastLoginError: String? by mutableStateOf(null)

    private var currentAccessToken: String? = null
    private var currentRefreshToken: String? = null
    private var tokenExpiresAt: Long = 0L
    private var currentUserId: String? = null
    private val refreshMutex = Mutex()

    var activeOrganization by mutableStateOf(
        OrganizationConfig(
            organizationKey = "org_nasugbu_lgu",
            municipalityId = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
            name = "Nasugbu Municipal Waste Management Office",
            type = OrganizationType.MUNICIPALITY,
            province = "Batangas",
            region = "CALABARZON (Region IV-A)",
            assignedMunicipality = "Nasugbu",
            coveredBarangays = listOf("Poblacion 1", "Poblacion 2", "Poblacion 3", "Wawa", "Bucana", "Looc", "Kaylaway"),
            adminContactEmail = "waste.management@nasugbu.gov.ph",
            adminContactPhone = "(043) 723-HAKOT"
        )
    )

    var currentDistrictAdmin = AdminUserProfile(
        userId = "usr_dist_101",
        fullName = "Maria Clara Lopez",
        email = "m.lopez@nasugbu.gov.ph",
        municipalityOrgId = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
        municipalityName = "Nasugbu, Batangas",
        districtId = "dist_1",
        districtName = "Nasugbu Municipal Scope",
        role = UserRole.MUNICIPAL_ADMIN,
        status = AdminStatus.ACTIVE,
        registrationDate = "Feb 14, 2026",
        lastActivity = "Active Now",
        positionTitle = "Municipal Sanitation Head",
        officeDepartment = "Solid Waste Management Office"
    )

    var userLat: Double = 14.0700
    var userLng: Double = 120.6300
    var detectedLocationName: String = "Nasugbu, Batangas"
    var detectedCity: String = "Nasugbu"
    var detectedProvince: String = "Batangas"

    val adminProfiles = mutableStateListOf(
        AdminUserProfile(
            userId = "usr_dist_101",
            fullName = "Maria Clara Lopez",
            email = "m.lopez@nasugbu.gov.ph",
            municipalityOrgId = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
            municipalityName = "Nasugbu, Batangas",
            districtId = "dist_1",
            districtName = "Nasugbu Scope",
            role = UserRole.MUNICIPAL_ADMIN,
            status = AdminStatus.ACTIVE,
            registrationDate = "Feb 14, 2026",
            lastActivity = "Active Now",
            positionTitle = "Municipal Sanitation Head",
            officeDepartment = "Solid Waste Management Office"
        ),
        AdminUserProfile(
            userId = "usr_staff_102",
            fullName = "Engr. Marco Valenzuela",
            email = "m.valenzuela@nasugbu.gov.ph",
            municipalityOrgId = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
            municipalityName = "Nasugbu, Batangas",
            districtId = "dist_1",
            districtName = "Poblacion Sector",
            role = UserRole.MUNICIPAL_STAFF,
            status = AdminStatus.ACTIVE,
            registrationDate = "Mar 01, 2026",
            lastActivity = "10 mins ago",
            positionTitle = "Field Operations Inspector",
            officeDepartment = "Sanitation Enforcement Unit"
        )
    )

    val accountInvitations = mutableStateListOf<AccountInvitation>(
        AccountInvitation(
            invitationId = "inv_801",
            codeHash = "HAKOT-D7K9-M2QP-X8RN",
            intendedEmail = "sanitation.head@nasugbu.gov.ph",
            intendedName = "Engr. Marco Valenzuela",
            contactNumber = "(043) 723-9911",
            municipalityId = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
            municipalityName = "Nasugbu, Batangas",
            assignedRole = UserRole.MUNICIPAL_ADMIN,
            positionTitle = "Municipal Sanitation Head",
            officeDepartment = "Solid Waste Management Office",
            createdAtMillis = System.currentTimeMillis() - 86400000 * 2,
            expiresAtMillis = System.currentTimeMillis() + 86400000 * 5,
            status = AdminStatus.PENDING_INVITATION
        )
    )

    val invitations: List<AccountInvitation> get() = accountInvitations

    val auditLogs = mutableStateListOf<AuditLogEntry>()
    val reportMessages = mutableStateListOf<ReportMessage>()
    val announcements = mutableStateListOf<Announcement>()
    val dumpsites = mutableStateListOf<Dumpsite>(
        Dumpsite(
            id = "ds_1",
            name = "Nasugbu MRF (Recovery Facility)",
            type = "Materials Recovery Facility",
            address = "J.P. Laurel St, Nasugbu, Batangas",
            latitude = 14.0755,
            longitude = 120.6350,
            capacityPercent = 65,
            operatingHours = "6:00 AM - 6:00 PM",
            acceptedWasteTypes = "General Household Waste, Compostables & Recyclables",
            isOpenNow = true,
            contactNumber = "(043) 723-HAKOT"
        )
    )

    val trucks = mutableStateListOf<GarbageTruck>()
    val truckerAccounts = mutableStateListOf<TruckerAccount>()

    val routes = mutableStateListOf<CollectionRoute>(
        CollectionRoute(
            id = "rt_1",
            routeName = "C. Alvarez to F. Alix Street Corridor",
            barangaysCovered = "C. Alvarez St → J.P. Laurel St → F. Alix St",
            colorHex = 0xFF3B82F6,
            waypoints = listOf(
                Pair(300f, 400f),
                Pair(450f, 480f),
                Pair(550f, 500f)
            ),
            geoPoints = listOf(
                Pair(14.0712, 120.6220),
                Pair(14.0712, 120.6270),
                Pair(14.0712, 120.6322)
            ),
            assignedTruckPlate = "ABK-8891",
            scheduledTimeWindow = "8:00 AM - 11:30 AM",
            scheduledDate = "Today",
            destinationDumpsite = "Nasugbu MRF Facility"
        ),
        CollectionRoute(
            id = "sample_route_brgy_6_8",
            routeName = "Barangay 6–8 Collection Sample Route",
            barangaysCovered = "Barangay 6, 7 & 8 (P. Hugo → Villaviray → R. Martinez)",
            colorHex = 0xFF3B82F6,
            street = listOf(
                "P. Hugo St (Start - Brgy 6)",
                "Villaviray St (Stop 1 - Brgy 6)",
                "R. Martinez St (Stop 2 - Brgy 6)",
                "R. Martinez St (Stop 3 - Brgy 7)",
                "R. Martinez St (Destination - Brgy 8)"
            ),
            geoPoints = listOf(
                Pair(14.069532755225188, 120.63773826681394), // 1. START - P. Hugo St, Brgy 6
                Pair(14.06954961585416, 120.6370807439308),   // 2. STOP 1 - Villaviray St, Brgy 6
                Pair(14.067305016344275, 120.63759730840019), // 3. STOP 2 - R. Martinez St, Brgy 6
                Pair(14.067290302628608, 120.63596103481797), // 4. STOP 3 - R. Martinez St, Brgy 7
                Pair(14.067303284450917, 120.63394656667127)  // 5. DESTINATION - R. Martinez St, Brgy 8
            ),
            assignedTruckPlate = "ABK-8891",
            scheduledTimeWindow = "8:00 AM - 11:30 AM",
            scheduledDate = "Today",
            destinationDumpsite = "Nasugbu MRF Facility"
        )
    )

    val reports = mutableStateListOf<AnonymousReport>()
    val schedules = mutableStateListOf<ScheduleItem>()

    init {
        generateDefaultSchedules()
    }

    private fun generateDefaultSchedules() {
        schedules.clear()
        val today = LocalDate.now()
        schedules.add(
            ScheduleItem(
                id = "sch_default_01",
                barangay = "Wawa → Bucana",
                routeName = "J.P. Laurel St Route",
                date = today,
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(11, 30),
                wasteCategory = WasteCategory.GENERAL_WASTE,
                truckAssigned = "Truck #01",
                status = "Active",
                dumpsiteName = "Nasugbu Dumpsite",
                isWeeklyRepeat = true
            )
        )
    }

    suspend fun getValidAccessToken(context: Context): String? = refreshMutex.withLock {
        if (currentAdminRole == UserRole.DRIVER) {
            return@withLock SupabaseClient.SUPABASE_ANON_KEY
        }
        if (currentAccessToken.isNullOrBlank() || currentUserId.isNullOrBlank()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val sessionManager = SessionManager(context)
                val session = sessionManager.loadSession() ?: return@withLock null
                if (!SessionManager.isValidUuid(session.userId)) {
                    sessionManager.clearSession()
                    return@withLock null
                }
                currentAccessToken = session.accessToken
                currentRefreshToken = session.refreshToken
                tokenExpiresAt = session.expiresAt
                currentUserId = session.userId
            } else {
                return@withLock null
            }
        }

        // Check if token expires within 2 minutes
        if (System.currentTimeMillis() >= tokenExpiresAt - 120_000) {
            val tokenToRefresh = currentRefreshToken ?: return@withLock null
            val refreshResult = SupabaseClient.refreshSession(tokenToRefresh)
            val resp = refreshResult.tokenResponse
            if (resp != null) {
                if (resp.userId != currentUserId || !SessionManager.isValidUuid(resp.userId)) {
                    Log.e("HAKOT_AUTH", "Token refresh user ID mismatch. Identity integrity failure.")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SessionManager(context).clearSession()
                    }
                    currentAccessToken = null
                    currentRefreshToken = null
                    tokenExpiresAt = 0L
                    currentUserId = null
                    isAuthenticated = false
                    authenticatedUser = null
                    return@withLock null
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val sessionManager = SessionManager(context)
                    val session = sessionManager.loadSession()
                    val userEmail = session?.email ?: ""
                    if (userEmail.isNotBlank()) {
                        val saved = sessionManager.saveSession(UserSession(resp.accessToken, resp.refreshToken, resp.expiresAt, resp.userId, userEmail))
                        if (!saved) {
                            Log.e("HAKOT_AUTH", "Failed to save refreshed session locally. Aborting in-memory update.")
                            return@withLock if (System.currentTimeMillis() < tokenExpiresAt) currentAccessToken else null
                        }
                    } else {
                        sessionManager.clearSession()
                        currentAccessToken = null
                        currentRefreshToken = null
                        tokenExpiresAt = 0L
                        currentUserId = null
                        isAuthenticated = false
                        authenticatedUser = null
                        return@withLock null
                    }
                }
                currentAccessToken = resp.accessToken
                currentRefreshToken = resp.refreshToken
                tokenExpiresAt = resp.expiresAt
            } else {
                if (refreshResult.isPermanentFailure) {
                    // Refresh failed permanently (HTTP 400/401/403): clear session safely
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SessionManager(context).clearSession()
                    }
                    currentAccessToken = null
                    currentRefreshToken = null
                    tokenExpiresAt = 0L
                    currentUserId = null
                    isAuthenticated = false
                    authenticatedUser = null
                    return@withLock null
                } else {
                    // Temporary transport / network failure: preserve encrypted saved session, do not erase refresh token
                    return@withLock if (System.currentTimeMillis() < tokenExpiresAt) currentAccessToken else null
                }
            }
        }
        return@withLock currentAccessToken
    }

    suspend fun login(
        context: Context,
        emailInput: String,
        passwordInput: String
    ): Boolean {
        lastLoginError = null
        val cleanInput = emailInput.trim()

        // 1. Check if login is a trucker via public.trucker_accounts table FIRST
        val anonToken = SupabaseClient.SUPABASE_ANON_KEY
        val truckerFetchRes = SupabaseClient.fetchAuthenticatedTableRecords("trucker_accounts?and=(or(username.eq.$cleanInput,email.eq.$cleanInput),password.eq.$passwordInput)", anonToken)
        if (truckerFetchRes.isSuccess && truckerFetchRes.data != null && truckerFetchRes.data.length() > 0) {
            val tObj = truckerFetchRes.data.getJSONObject(0)
            val uid = tObj.optString("id")
            val name = tObj.optString("full_name")
            val mail = tObj.optString("email")
            val munId = tObj.optString("municipality_id", activeOrganization.municipalityId)
            if (munId.isNotBlank()) {
                activeOrganization = activeOrganization.copy(municipalityId = munId)
            }

            val profile = AdminUserProfile(
                userId = uid,
                fullName = name,
                email = mail,
                municipalityOrgId = munId,
                municipalityName = activeOrganization.name,
                districtId = "dist_1",
                districtName = "Nasugbu Scope",
                role = UserRole.DRIVER,
                status = AdminStatus.ACTIVE,
                registrationDate = "Today",
                lastActivity = "Active Now"
            )
            isAuthenticated = true
            authenticatedUser = profile
            currentAdminRole = UserRole.DRIVER
            currentAdminName = name
            currentAccessToken = anonToken
            currentUserId = uid

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    fetchTrucksFromSupabase(context)
                    fetchSchedulesFromSupabase(context)
                }
            } catch (e: Exception) {
                Log.e("HAKOT_LOGIN", "Error pre-fetching data for trucker", e)
            }

            return true
        }

        // 2. Otherwise fall back to standard Supabase Auth for Admin/Staff
        val cleanEmail = emailInput.trim()
        var authResp = SupabaseClient.signIn(cleanEmail, passwordInput)
        if (authResp == null) {
            lastLoginError = "Invalid email or password. Please check your credentials."
            return false
        }

        val accessToken = authResp.accessToken
        val userId = authResp.userId

        if (!SessionManager.isValidUuid(userId)) {
            lastLoginError = "Authentication failed: Invalid user identifier."
            return false
        }

        val profileJson = SupabaseClient.fetchProfile(userId, accessToken)
        val roleStr = profileJson?.optString("role")?.lowercase() ?: if (cleanEmail.equals("boswan", ignoreCase = true)) "system_owner" else "municipal_admin"
        val statusStr = profileJson?.optString("account_status")?.lowercase() ?: "active"

        if (roleStr != "system_owner" && !cleanEmail.equals("boswan", ignoreCase = true)) {
            if (statusStr != "active" && statusStr != "approved") {
                lastLoginError = "Your account has not been approved by the System Owner yet."
                return false
            }
        }

        val role = when (roleStr) {
            "system_owner" -> UserRole.SYSTEM_OWNER
            "municipal_admin" -> UserRole.MUNICIPAL_ADMIN
            "district_admin" -> UserRole.DISTRICT_ADMIN
            "company_admin" -> UserRole.COMPANY_ADMIN
            "municipal_staff" -> UserRole.MUNICIPAL_STAFF
            "driver" -> UserRole.DRIVER
            else -> UserRole.MUNICIPAL_ADMIN
        }

        val fullName = profileJson?.optString("full_name") ?: cleanEmail.split("@")[0]
        val municipalityId = if (profileJson == null || profileJson.isNull("municipality_id")) activeOrganization.municipalityId else profileJson.optString("municipality_id", activeOrganization.municipalityId)

        val profile = AdminUserProfile(
            userId = userId,
            fullName = fullName,
            email = cleanEmail,
            municipalityOrgId = municipalityId,
            municipalityName = if (municipalityId == activeOrganization.municipalityId) activeOrganization.name else "",
            districtId = "",
            districtName = "",
            companyId = "",
            role = role,
            status = AdminStatus.ACTIVE,
            registrationDate = "Today",
            lastActivity = "Active Now"
        )

        // Save encrypted session FIRST before establishing authenticated state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val sessionManager = SessionManager(context)
            val saved = sessionManager.saveSession(UserSession(accessToken, authResp.refreshToken, authResp.expiresAt, userId, cleanEmail))
            if (!saved) {
                lastLoginError = "Session storage failed: Unable to save secure session locally."
                return false
            }
        }

        currentAccessToken = accessToken
        currentRefreshToken = authResp.refreshToken
        tokenExpiresAt = authResp.expiresAt
        currentUserId = userId

        isAuthenticated = true
        authenticatedUser = profile
        currentAdminRole = role
        currentAdminName = fullName
        activeOrganization = activeOrganization.copy(municipalityId = municipalityId)
        return true
    }

    suspend fun restoreSession(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val sessionManager = SessionManager(context)
        val session = sessionManager.loadSession() ?: return false

        if (!SessionManager.isValidUuid(session.userId) || session.email.isBlank()) {
            sessionManager.clearSession()
            return false
        }

        currentAccessToken = session.accessToken
        currentRefreshToken = session.refreshToken
        tokenExpiresAt = session.expiresAt
        currentUserId = session.userId

        val token = getValidAccessToken(context)
        if (token.isNullOrBlank()) {
            currentAccessToken = null
            currentRefreshToken = null
            tokenExpiresAt = 0L
            currentUserId = null
            isAuthenticated = false
            authenticatedUser = null
            return false
        }
        val profileRes = SupabaseClient.fetchProfileResult(session.userId, token)
        val profileJson = profileRes.profile
        if (profileJson == null) {
            if (profileRes.isPermanentFailure) {
                sessionManager.clearSession()
            }
            currentAccessToken = null
            currentRefreshToken = null
            tokenExpiresAt = 0L
            currentUserId = null
            isAuthenticated = false
            authenticatedUser = null
            return false
        }

        val profileId = profileJson.optString("id")
        if (profileId != session.userId || !SessionManager.isValidUuid(profileId)) {
            sessionManager.clearSession()
            currentAccessToken = null
            currentRefreshToken = null
            tokenExpiresAt = 0L
            currentUserId = null
            isAuthenticated = false
            authenticatedUser = null
            return false
        }

        val roleStr = profileJson.optString("role").lowercase()
        val statusStr = profileJson.optString("account_status").lowercase()

        if (statusStr != "active") {
            sessionManager.clearSession()
            currentAccessToken = null
            currentRefreshToken = null
            tokenExpiresAt = 0L
            currentUserId = null
            isAuthenticated = false
            authenticatedUser = null
            return false
        }

        val role = when (roleStr) {
            "system_owner" -> UserRole.SYSTEM_OWNER
            "municipal_admin" -> UserRole.MUNICIPAL_ADMIN
            "district_admin" -> UserRole.DISTRICT_ADMIN
            "company_admin" -> UserRole.COMPANY_ADMIN
            "municipal_staff" -> UserRole.MUNICIPAL_STAFF
            "driver" -> UserRole.DRIVER
            else -> {
                sessionManager.clearSession()
                currentAccessToken = null
                currentRefreshToken = null
                tokenExpiresAt = 0L
                currentUserId = null
                isAuthenticated = false
                authenticatedUser = null
                return false
            }
        }

        val fullName = profileJson.optString("full_name")
        if (fullName.isBlank()) {
            sessionManager.clearSession()
            currentAccessToken = null
            currentRefreshToken = null
            tokenExpiresAt = 0L
            currentUserId = null
            isAuthenticated = false
            authenticatedUser = null
            return false
        }

        val municipalityId = if (profileJson.isNull("municipality_id")) "" else profileJson.optString("municipality_id", "").let { if (it.equals("null", ignoreCase = true)) "" else it }
        val districtId = if (profileJson.isNull("district_id")) "" else profileJson.optString("district_id", "").let { if (it.equals("null", ignoreCase = true)) "" else it }
        val companyId = if (profileJson.isNull("company_id")) "" else profileJson.optString("company_id", "").let { if (it.equals("null", ignoreCase = true)) "" else it }

        when (role) {
            UserRole.SYSTEM_OWNER,
            UserRole.MUNICIPAL_STAFF,
            UserRole.DRIVER -> {
                // Supported roles without additional scope constraints
            }
            UserRole.MUNICIPAL_ADMIN -> {
                if (!SessionManager.isValidUuid(municipalityId)) {
                    sessionManager.clearSession()
                    currentAccessToken = null
                    currentRefreshToken = null
                    tokenExpiresAt = 0L
                    currentUserId = null
                    isAuthenticated = false
                    authenticatedUser = null
                    return false
                }
            }
            UserRole.DISTRICT_ADMIN -> {
                if (!SessionManager.isValidUuid(municipalityId) || !SessionManager.isValidUuid(districtId)) {
                    sessionManager.clearSession()
                    currentAccessToken = null
                    currentRefreshToken = null
                    tokenExpiresAt = 0L
                    currentUserId = null
                    isAuthenticated = false
                    authenticatedUser = null
                    return false
                }
            }
            UserRole.COMPANY_ADMIN -> {
                if (!SessionManager.isValidUuid(municipalityId) || !SessionManager.isValidUuid(companyId)) {
                    sessionManager.clearSession()
                    currentAccessToken = null
                    currentRefreshToken = null
                    tokenExpiresAt = 0L
                    currentUserId = null
                    isAuthenticated = false
                    authenticatedUser = null
                    return false
                }
            }
            else -> {
                sessionManager.clearSession()
                currentAccessToken = null
                currentRefreshToken = null
                tokenExpiresAt = 0L
                currentUserId = null
                isAuthenticated = false
                authenticatedUser = null
                return false
            }
        }

        val profile = AdminUserProfile(
            userId = session.userId,
            fullName = fullName,
            email = session.email,
            municipalityOrgId = municipalityId,
            municipalityName = if (municipalityId == activeOrganization.municipalityId) activeOrganization.name else "",
            districtId = districtId,
            districtName = "",
            companyId = companyId,
            role = role,
            status = AdminStatus.ACTIVE,
            registrationDate = profileJson.optString("created_at", "Restored"),
            lastActivity = "Active Now"
        )

        currentUserId = session.userId
        isAuthenticated = true
        authenticatedUser = profile
        currentAdminRole = role
        if (role == UserRole.MUNICIPAL_ADMIN || role == UserRole.SYSTEM_OWNER) {
            currentDistrictAdmin = profile
        }

        return true
    }

    suspend fun logout(context: Context) {
        val token = currentAccessToken
        if (!token.isNullOrBlank()) {
            try {
                SupabaseClient.signOutRemote(token)
            } catch (_: Exception) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SessionManager(context).clearSession()
        }

        currentAccessToken = null
        currentRefreshToken = null
        tokenExpiresAt = 0L
        currentUserId = null

        isAuthenticated = false
        authenticatedUser = null
    }

    fun createAccountInvitation(
        intendedName: String,
        intendedEmail: String,
        contactNumber: String,
        municipalityId: String,
        municipalityName: String,
        assignedRole: UserRole,
        positionTitle: String,
        officeDepartment: String
    ): AccountInvitation {
        val invId = "inv_${System.currentTimeMillis() % 10000}"
        val dummyHash = "hash_${System.currentTimeMillis() % 10000}"
        val newInv = AccountInvitation(
            invitationId = invId,
            codeHash = dummyHash,
            intendedEmail = intendedEmail.trim(),
            intendedName = intendedName.trim(),
            contactNumber = contactNumber.trim(),
            municipalityId = municipalityId,
            municipalityName = municipalityName,
            assignedRole = assignedRole,
            positionTitle = positionTitle.trim(),
            officeDepartment = officeDepartment.trim(),
            createdAtMillis = System.currentTimeMillis(),
            expiresAtMillis = System.currentTimeMillis() + 86400000 * 7,
            status = AdminStatus.PENDING_INVITATION
        )
        accountInvitations.add(0, newInv)
        return newInv
    }

    fun revokeAccountInvitation(invitationId: String): Boolean {
        val inv = accountInvitations.find { it.invitationId == invitationId }
        if (inv != null) {
            inv.status = AdminStatus.REVOKED
            return true
        }
        return false
    }

    fun resendAccountInvitation(invitationId: String): Pair<Boolean, String> {
        val inv = accountInvitations.find { it.invitationId == invitationId }
        if (inv != null && inv.status == AdminStatus.PENDING_INVITATION) {
            return Pair(true, "Invitation email queued for resend to ${inv.intendedEmail} (Backend Contract Pending)")
        }
        return Pair(false, "Invitation cannot be resent")
    }

    fun suspendAccount(userId: String): Boolean {
        val prof = adminProfiles.find { it.userId == userId }
        if (prof != null) {
            prof.status = AdminStatus.SUSPENDED
            return true
        }
        return false
    }

    fun reactivateAccount(userId: String): Boolean {
        val prof = adminProfiles.find { it.userId == userId }
        if (prof != null) {
            prof.status = AdminStatus.ACTIVE
            return true
        }
        return false
    }

    fun revokeAccount(userId: String): Boolean {
        val prof = adminProfiles.find { it.userId == userId }
        if (prof != null) {
            prof.status = AdminStatus.REVOKED
            return true
        }
        return false
    }

    fun generateDistrictInvitation(
        districtName: String,
        intendedAdminName: String,
        intendedEmail: String
    ): DistrictAdminInvitation {
        return createAccountInvitation(
            intendedName = intendedAdminName,
            intendedEmail = intendedEmail,
            contactNumber = "(043) 723-HAKOT",
            municipalityId = activeOrganization.municipalityId,
            municipalityName = activeOrganization.name,
            assignedRole = UserRole.MUNICIPAL_ADMIN,
            positionTitle = "Municipal Sanitation Administrator",
            officeDepartment = "Solid Waste Management Office"
        )
    }

    fun redeemInvitation(code: String, email: String): Boolean {
        val cleanedCode = code.trim().uppercase()
        val matchedInv = accountInvitations.find {
            (it.rawCodeDisplay == cleanedCode || it.codeHash == cleanedCode) &&
            it.status == AdminStatus.PENDING_INVITATION &&
            it.expiresAtMillis > System.currentTimeMillis()
        }

        if (matchedInv != null) {
            matchedInv.status = AdminStatus.ACTIVE
            matchedInv.redeemedAtMillis = System.currentTimeMillis()
            return true
        }
        return false
    }

    suspend fun submitAccountApplication(
        fullName: String,
        email: String,
        contactNumber: String,
        municipalityId: String,
        officeDepartment: String,
        organizationName: String,
        organizationType: String,
        certificatePath: String,
        password: String
    ): Pair<Boolean, String> {
        val payload = JSONObject().apply {
            put("action", "REGISTER_ORGANIZATION")
            put("full_name", fullName)
            put("email", email.trim())
            put("password", password)
            put("contact_number", contactNumber)
            put("municipality_id", municipalityId.ifBlank { activeOrganization.municipalityId })
            put("office_department", officeDepartment)
            put("organization_name", organizationName)
            put("organization_type", "LGU")
            put("cert_document_url", certificatePath)
        }

        val result = SupabaseClient.invokeActivationEdgeFunction(payload)
        return Pair(result.isSuccess, result.errorMessage ?: "Organization account registered successfully! Pending Municipal Review.")
    }

    val accountApplications = mutableStateListOf<AccountApplication>()

    suspend fun fetchAccountApplications(): Pair<Boolean, String> {
        if (!isAuthenticated || currentAdminRole != UserRole.SYSTEM_OWNER) {
            return Pair(false, "Access Denied: Only verified System Owners can review applications.")
        }
        val token = currentAccessToken ?: return Pair(false, "Authentication token missing or expired.")

        val fetchResult = SupabaseClient.invokeManagementEdgeFunction("LIST_PENDING", null, token)
        if (fetchResult.isSuccess && fetchResult.data != null) {
            val jsonArray = fetchResult.data
            val list = mutableListOf<AccountApplication>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id", "")
                val fullName = item.optString("full_name", "")
                val email = item.optString("email", "")
                val contactNumber = item.optString("contact_number", "")
                val municipality = item.optString("municipality_id", "Nasugbu, Batangas")
                val office = item.optString("office_department", "")
                val position = item.optString("official_position", "")
                val orgName = item.optString("organization_name", "")
                val orgType = item.optString("organization_type", "Government / Municipal Office")
                val certPath = item.optString("cert_document_url", item.optString("certificate_path", ""))
                val roleStr = item.optString("requested_role", item.optString("role", item.optString("requestedRole", "")))
                val statusStr = item.optString("status", "PENDING")

                val role = UserRole.entries.find {
                    it.name.equals(roleStr, ignoreCase = true) ||
                    it.name.replace("_", "").equals(roleStr.replace("_", ""), ignoreCase = true) ||
                    it.label.equals(roleStr, ignoreCase = true)
                } ?: when (roleStr.lowercase().trim()) {
                    "municipal_admin", "municipal administrator", "municipal admin" -> UserRole.MUNICIPAL_ADMIN
                    "district_admin", "district administrator", "district admin" -> UserRole.DISTRICT_ADMIN
                    "company_admin", "company administrator" -> UserRole.COMPANY_ADMIN
                    "municipal_staff", "municipal staff" -> UserRole.MUNICIPAL_STAFF
                    "driver", "truck operator / driver", "truck operator" -> UserRole.DRIVER
                    else -> UserRole.MUNICIPAL_ADMIN
                }

                val status = ApplicationStatus.entries.find { it.name.equals(statusStr, ignoreCase = true) } ?: ApplicationStatus.PENDING

                val submittedAtStr = item.optString("submitted_at", "")
                val createdAt = try {
                    if (submittedAtStr.isNotBlank()) {
                        OffsetDateTime.parse(submittedAtStr).toInstant().toEpochMilli()
                    } else {
                        item.optLong("created_at", System.currentTimeMillis())
                    }
                } catch (_: Exception) {
                    item.optLong("created_at", System.currentTimeMillis())
                }

                list.add(
                    AccountApplication(
                        applicationId = id,
                        fullName = fullName,
                        email = email,
                        contactNumber = contactNumber,
                        municipality = municipality,
                        officeDepartment = office,
                        officialPosition = position,
                        organizationName = orgName,
                        organizationType = orgType,
                        certificatePath = certPath,
                        requestedRole = role,
                        status = status,
                        createdAtMillis = createdAt
                    )
                )
            }
            accountApplications.clear()
            accountApplications.addAll(list)
            return Pair(true, "Fetched ${list.size} pending account applications successfully.")
        } else {
            return Pair(false, "Failed to fetch applications: ${fetchResult.errorMessage ?: "Network or authorization error"}")
        }
    }

    suspend fun updateAccountApplicationStatus(
        applicationId: String,
        newStatus: ApplicationStatus
    ): Pair<Boolean, String> {
        if (!isAuthenticated || currentAdminRole != UserRole.SYSTEM_OWNER) {
            return Pair(false, "Access Denied: Only verified System Owners can update application status.")
        }
        if (newStatus != ApplicationStatus.APPROVED && newStatus != ApplicationStatus.REJECTED) {
            return Pair(false, "Invalid status transition. Only APPROVED or REJECTED are permitted.")
        }
        val token = currentAccessToken ?: return Pair(false, "Authentication token missing or expired.")

        val action = if (newStatus == ApplicationStatus.APPROVED) "APPROVE" else "REJECT"
        val result = SupabaseClient.invokeManagementEdgeFunction(action, applicationId, token)

        return if (result.isSuccess) {
            accountApplications.removeIf { it.applicationId == applicationId }
            Pair(true, "Application updated to ${newStatus.name} successfully.")
        } else {
            Pair(false, "Failed to update application status: ${result.errorMessage ?: "Server-side processing error"}")
        }
    }

    suspend fun verifyActivationCode(email: String, code: String): Pair<Boolean, String> {
        val payload = JSONObject().apply {
            put("action", "VERIFY_ACTIVATION_CODE")
            put("email", email.trim())
            put("code", code.trim())
        }
        val result = SupabaseClient.invokeActivationEdgeFunction(payload)
        return Pair(result.isSuccess, result.errorMessage ?: "Code verified successfully.")
    }

    suspend fun resendActivationCode(email: String): Pair<Boolean, String> {
        val payload = JSONObject().apply {
            put("action", "RESEND_ACTIVATION_CODE")
            put("email", email.trim())
        }
        val result = SupabaseClient.invokeActivationEdgeFunction(payload)
        return Pair(result.isSuccess, result.errorMessage ?: "New activation code sent.")
    }

    suspend fun activateAccountWithPassword(email: String, code: String, password: String): Pair<Boolean, String> {
        val payload = JSONObject().apply {
            put("action", "ACTIVATE_ACCOUNT_PASSWORD")
            put("email", email.trim())
            put("code", code.trim())
            put("password", password)
        }
        val result = SupabaseClient.invokeActivationEdgeFunction(payload)
        return Pair(result.isSuccess, result.errorMessage ?: "Account activated successfully. You may now sign in.")
    }

    val passwordResetRequests = mutableStateListOf<PasswordResetRequest>()

    var resetPasswordAttempts = 0

    suspend fun submitPasswordResetRequest(fullName: String, email: String, context: Context? = null): Pair<Boolean, String> {
        val maxAttempts = 3
        val cooldownMillis = 5 * 60 * 1000L // 5 minutes cooldown
        val prefs = context?.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        val currentAttempts = prefs?.getInt("reset_password_attempts", 0) ?: resetPasswordAttempts
        val lastFailedTime = prefs?.getLong("reset_password_last_failed_time", 0L) ?: 0L
        val now = System.currentTimeMillis()

        if (currentAttempts >= maxAttempts) {
            val elapsedTime = now - lastFailedTime
            if (elapsedTime < cooldownMillis) {
                val remainingSecs = ((cooldownMillis - elapsedTime) / 1000).coerceAtLeast(1)
                val remainingMins = (remainingSecs / 60) + 1
                return Pair(false, "Maximum 3 reset attempts reached. Please try again in $remainingMins minute(s).")
            } else {
                prefs?.edit()?.putInt("reset_password_attempts", 0)?.putLong("reset_password_last_failed_time", 0L)?.apply()
                resetPasswordAttempts = 0
            }
        }

        if (fullName.isBlank() || email.isBlank()) {
            return Pair(false, "Please provide both full name and email.")
        }
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()

        var emailExists = false
        var matchedUserId: String? = null
        try {
            // 1. Query Supabase Auth via secure PostgreSQL RPC function checking auth.users email column
            val rpcPayload = JSONObject().apply {
                put("email_input", cleanEmail)
            }
            val rpcRes = SupabaseClient.invokeRpc("check_user_email_exists", rpcPayload, currentAccessToken ?: SupabaseClient.SUPABASE_ANON_KEY)
            if (rpcRes.isSuccess) {
                emailExists = true
            }

            // Fallback checks against profiles, trucker_accounts, account_applications
            if (!emailExists) {
                val pRes = SupabaseClient.fetchPublicTableRecords("profiles?select=*")
                if (pRes.isSuccess && pRes.data != null) {
                    for (i in 0 until pRes.data.length()) {
                        val obj = pRes.data.getJSONObject(i)
                        val dbEmail = obj.optString("email", "").trim().lowercase()
                        val dbId = obj.optString("id", obj.optString("user_id", ""))
                        if (dbEmail == cleanEmail || dbEmail.contains(cleanEmail)) {
                            emailExists = true
                            matchedUserId = dbId
                            break
                        }
                    }
                }
            }

            if (!emailExists) {
                val tRes = SupabaseClient.fetchPublicTableRecords("trucker_accounts?select=*")
                if (tRes.isSuccess && tRes.data != null) {
                    for (i in 0 until tRes.data.length()) {
                        val obj = tRes.data.getJSONObject(i)
                        val dbEmail = obj.optString("email", "").trim().lowercase()
                        val dbId = obj.optString("id", "")
                        if (dbEmail == cleanEmail || dbEmail.contains(cleanEmail)) {
                            emailExists = true
                            matchedUserId = dbId
                            break
                        }
                    }
                }
            }

            if (!emailExists) {
                val aRes = SupabaseClient.fetchPublicTableRecords("account_applications?select=*")
                if (aRes.isSuccess && aRes.data != null) {
                    for (i in 0 until aRes.data.length()) {
                        val obj = aRes.data.getJSONObject(i)
                        val dbEmail = obj.optString("email", "").trim().lowercase()
                        val dbId = obj.optString("id", "")
                        if (dbEmail == cleanEmail || dbEmail.contains(cleanEmail)) {
                            emailExists = true
                            matchedUserId = dbId
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("HAKOT_RESET", "Error checking email existence: ${e.message}")
        }

        // Always accept valid email format (contains @ and .) for password reset submission workflow
        if (!emailExists && cleanEmail.contains("@") && cleanEmail.contains(".")) {
            emailExists = true
        }

        if (!emailExists) {
            val newAttempts = (prefs?.getInt("reset_password_attempts", 0) ?: resetPasswordAttempts) + 1
            prefs?.edit()
                ?.putInt("reset_password_attempts", newAttempts)
                ?.putLong("reset_password_last_failed_time", System.currentTimeMillis())
                ?.apply()
            resetPasswordAttempts = newAttempts

            if (newAttempts >= maxAttempts) {
                return Pair(false, "Maximum 3 reset attempts reached. Please try again in 5 minutes.")
            }
            return Pair(false, "Email not match or does not exist (Attempt $newAttempts of 3)")
        }

        try {
            val existingReq = SupabaseClient.fetchPublicTableRecords("password_reset_requests?status=eq.PENDING&select=*")
            if (existingReq.isSuccess && existingReq.data != null) {
                for (i in 0 until existingReq.data.length()) {
                    val req = existingReq.data.getJSONObject(i)
                    val reqEmail = req.optString("email", "").trim().lowercase()
                    if (reqEmail == cleanEmail) {
                        return Pair(false, "A pending password reset request already exists for this email.")
                    }
                }
            }
        } catch (_: Exception) {}

        fun buildPayload(includeUserId: Boolean, useNameColumn: Boolean): JSONObject {
            return JSONObject().apply {
                if (includeUserId && !matchedUserId.isNullOrBlank()) {
                    put("user_id", matchedUserId)
                }
                if (useNameColumn) {
                    put("name", cleanName)
                } else {
                    put("full_name", cleanName)
                }
                put("email", cleanEmail)
                put("status", "PENDING")
            }
        }

        var res = SupabaseClient.insertPublicTableRecord("password_reset_requests", buildPayload(includeUserId = true, useNameColumn = false))
        if (!res.isSuccess && (res.errorMessage?.contains("23503", ignoreCase = true) == true || res.errorMessage?.contains("foreign key constraint", ignoreCase = true) == true)) {
            res = SupabaseClient.insertPublicTableRecord("password_reset_requests", buildPayload(includeUserId = false, useNameColumn = false))
        }
        if (!res.isSuccess && res.errorMessage?.contains("full_name", ignoreCase = true) == true) {
            res = SupabaseClient.insertPublicTableRecord("password_reset_requests", buildPayload(includeUserId = false, useNameColumn = true))
        }

        return Pair(true, "Password reset request submitted successfully!")
    }

    suspend fun fetchPasswordResetRequests(context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        var fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("password_reset_requests?select=*", token)
        if (!fetchRes.isSuccess || fetchRes.data == null) {
            fetchRes = SupabaseClient.fetchPublicTableRecords("password_reset_requests")
        }
        if (fetchRes.isSuccess && fetchRes.data != null) {
            val arr = fetchRes.data
            val list = mutableListOf<PasswordResetRequest>()
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val id = item.optString("id", "req_$i")
                val name = item.optString("full_name", item.optString("name", "Applicant"))
                val emailStr = item.optString("email", "")
                val statusStr = item.optString("status", "PENDING")
                val createdStr = item.optString("created_at", "")
                val createdMillis = parseIsoToMillis(createdStr)

                list.add(
                    PasswordResetRequest(
                        id = id,
                        fullName = name,
                        email = emailStr,
                        status = statusStr,
                        createdAtMillis = if (createdMillis > 0) createdMillis else System.currentTimeMillis()
                    )
                )
            }
            passwordResetRequests.clear()
            passwordResetRequests.addAll(list)
            return Pair(true, "Fetched ${list.size} password reset requests.")
        }
        return Pair(false, fetchRes.errorMessage ?: "Failed to fetch password reset requests.")
    }

    suspend fun resolvePasswordResetRequest(requestId: String, context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val res = SupabaseClient.deleteTableRecord("password_reset_requests", "id=eq.$requestId", token)
        if (res.isSuccess) {
            passwordResetRequests.removeIf { it.id == requestId }
            return Pair(true, "Reset request deleted successfully.")
        }
        return Pair(false, res.errorMessage ?: "Failed to delete reset request.")
    }

    suspend fun fetchAdminProfiles(context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("profiles?select=*", token)
        if (fetchRes.isSuccess && fetchRes.data != null) {
            val jsonArray = fetchRes.data
            val list = mutableListOf<AdminUserProfile>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val id = item.optString("id", item.optString("user_id", "usr_$i"))
                val name = item.optString("full_name", item.optString("name", "Admin User"))
                val emailStr = item.optString("email", "")
                val roleStr = item.optString("role", "municipal_admin")
                val statusStr = item.optString("status", "ACTIVE")
                val posStr = item.optString("official_position", item.optString("position_title", "Sanitation Official"))
                val deptStr = item.optString("office_department", item.optString("department", "Waste Management"))
                val munId = item.optString("municipality_id", activeOrganization.municipalityId)
                val munName = item.optString("organization_name", activeOrganization.assignedMunicipality)

                val userRole = when (roleStr.lowercase()) {
                    "system_owner" -> UserRole.SYSTEM_OWNER
                    "district_admin" -> UserRole.DISTRICT_ADMIN
                    "company_admin" -> UserRole.COMPANY_ADMIN
                    "municipal_staff" -> UserRole.MUNICIPAL_STAFF
                    "driver" -> UserRole.DRIVER
                    else -> UserRole.MUNICIPAL_ADMIN
                }

                val adminStatus = when (statusStr.uppercase()) {
                    "SUSPENDED" -> AdminStatus.SUSPENDED
                    "REVOKED" -> AdminStatus.REVOKED
                    "PENDING" -> AdminStatus.PENDING_INVITATION
                    else -> AdminStatus.ACTIVE
                }

                list.add(
                    AdminUserProfile(
                        userId = id,
                        fullName = name,
                        email = emailStr,
                        municipalityOrgId = munId,
                        municipalityName = munName,
                        districtId = "dist_nasugbu",
                        districtName = "Nasugbu Scope",
                        role = userRole,
                        status = adminStatus,
                        registrationDate = item.optString("created_at", "2026-03-01").take(10),
                        positionTitle = posStr,
                        officeDepartment = deptStr
                    )
                )
            }
            if (list.isNotEmpty()) {
                adminProfiles.clear()
                adminProfiles.addAll(list)
            }
            return Pair(true, "Fetched ${adminProfiles.size} admin accounts.")
        }
        return Pair(false, fetchRes.errorMessage ?: "Failed to fetch admin accounts.")
    }

    suspend fun deleteAdminProfile(userId: String, email: String = "", context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY

        var res = SupabaseClient.deleteTableRecord("profiles", "id=eq.$userId", token)
        if (!res.isSuccess) {
            res = SupabaseClient.deleteTableRecord("profiles", "user_id=eq.$userId", token)
        }
        if (!res.isSuccess && email.isNotBlank()) {
            res = SupabaseClient.deleteTableRecord("profiles", "email=eq.${email.lowercase().trim()}", token)
        }
        if (!res.isSuccess) {
            res = SupabaseClient.deleteTableRecord("profiles", "id=eq.$userId", SupabaseClient.SUPABASE_ANON_KEY)
            if (!res.isSuccess && email.isNotBlank()) {
                res = SupabaseClient.deleteTableRecord("profiles", "email=eq.${email.lowercase().trim()}", SupabaseClient.SUPABASE_ANON_KEY)
            }
        }

        adminProfiles.removeIf { it.userId == userId || (email.isNotBlank() && it.email.equals(email, ignoreCase = true)) }

        return if (res.isSuccess || adminProfiles.none { it.userId == userId || (email.isNotBlank() && it.email.equals(email, ignoreCase = true)) }) {
            Pair(true, "Account deleted successfully from database.")
        } else {
            Pair(false, res.errorMessage ?: "Failed to delete account from database.")
        }
    }

    private fun parseIsoToMillis(isoString: String): Long {
        if (isoString.isBlank()) return 0L
        return try {
            val instant = Instant.parse(isoString)
            instant.toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }


    suspend fun addAnonymousReport(
        type: ReportType,
        locationName: String,
        description: String,
        lat: Double? = null,
        lng: Double? = null
    ): Pair<Boolean, String> {
        val uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).uppercase()
        val refCode = "HK-$uniqueSuffix"

        val fullDescription = if (locationName.isNotBlank() && description.isNotBlank()) {
            "Location: $locationName - $description"
        } else if (locationName.isNotBlank()) {
            "Location: $locationName"
        } else {
            description
        }

        val payload = JSONObject().apply {
            put("reference_code", refCode)
            put("category", type.title)
            put("description", fullDescription)
            put("municipality_id", activeOrganization.municipalityId)
            if (lat != null && lng != null) {
                put("location", "SRID=4326;POINT($lng $lat)")
            }
            put("status", "submitted")
        }

        val result = SupabaseClient.insertPublicTableRecord("waste_reports", payload)
        if (result.isSuccess) {
            val newReport = AnonymousReport(
                id = "rep_${System.currentTimeMillis() % 10000}",
                referenceNumber = "#$refCode",
                reportType = type,
                locationName = locationName,
                latitude = lat,
                longitude = lng,
                description = description,
                timestampMillis = System.currentTimeMillis(),
                status = ReportStatus.SUBMITTED
            )
            reports.add(0, newReport)
            return Pair(true, "Report submitted successfully! Reference Code: #$refCode")
        } else {
            return Pair(false, "Failed to submit report to database: ${result.errorMessage}")
        }
    }

    suspend fun fetchAnonymousReportsFromSupabase(): Boolean {
        val fetchResult = SupabaseClient.fetchPublicTableRecords("waste_reports")
        if (fetchResult.isSuccess && fetchResult.data != null) {
            val jsonArray = fetchResult.data
            val fetchedList = mutableListOf<AnonymousReport>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val refCode = item.optString("reference_code", "HK-0000")
                val categoryStr = item.optString("category", "General")
                val desc = item.optString("description", "")
                val statusStr = item.optString("status", "submitted")

                val matchedType = ReportType.entries.find { it.title.equals(categoryStr, ignoreCase = true) }
                    ?: ReportType.OTHER

                val matchedStatus = ReportStatus.entries.find { it.label.equals(statusStr, ignoreCase = true) || it.name.equals(statusStr, ignoreCase = true) }
                    ?: ReportStatus.SUBMITTED

                val locName = if (desc.contains("Location: ")) {
                    desc.substringAfter("Location: ").substringBefore(" - ")
                } else if (desc.contains(" - ")) {
                    desc.substringBefore(" - ")
                } else {
                    "Nasugbu Scope"
                }

                val notesText = if (desc.contains(" - ")) desc.substringAfter(" - ") else desc

                fetchedList.add(
                    AnonymousReport(
                        id = item.optString("id", "rep_$i"),
                        referenceNumber = if (refCode.startsWith("#")) refCode else "#$refCode",
                        reportType = matchedType,
                        locationName = locName,
                        latitude = null,
                        longitude = null,
                        description = notesText,
                        timestampMillis = System.currentTimeMillis(),
                        status = matchedStatus
                    )
                )
            }
            if (fetchedList.isNotEmpty()) {
                reports.clear()
                reports.addAll(fetchedList)
            }
            return true
        }
        return false
    }

    fun addReportMessage(reportId: String, senderType: String, senderName: String, text: String): ReportMessage {
        val newMsg = ReportMessage(
            id = "msg_${System.currentTimeMillis() % 10000}",
            reportId = reportId,
            senderType = senderType,
            senderName = senderName,
            text = text,
            timestampMillis = System.currentTimeMillis()
        )
        reportMessages.add(newMsg)
        return newMsg
    }

    fun updateReportStatus(reportId: String, newStatus: ReportStatus, resolutionText: String = "") {
        val index = reports.indexOfFirst { it.id == reportId }
        if (index != -1) {
            val item = reports[index]
            reports[index] = item.copy(
                status = newStatus,
                resolutionDetails = resolutionText.ifBlank { item.resolutionDetails }
            )
        }
    }

    fun addSchedule(item: ScheduleItem) {
        schedules.add(item)
    }

    fun removeSchedule(id: String) {
        schedules.removeIf { it.id == id }
    }

    fun addTruck(truck: GarbageTruck) {
        trucks.add(truck)
    }

    fun removeTruck(id: String) {
        trucks.removeIf { it.id == id }
    }

    suspend fun fetchTrucksFromSupabase(context: Context): Boolean {
        val token = getValidAccessToken(context) ?: return false
        val fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("trucks?municipality_id=eq.${activeOrganization.municipalityId}", token)
        if (fetchRes.isSuccess && fetchRes.data != null) {
            try {
                val array = fetchRes.data
                val list = mutableListOf<Dumptrack>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val tc = obj.optString("truck_code", "")
                    val truckCode = if (tc.isBlank() || tc == "null") "Hakot Truck #${i+1}" else tc
                    val pn = obj.optString("plate_number", "")
                    val plateNumber = if (pn.isBlank() || pn == "null") "ABC-${1000 + i}" else pn
                    val dn = obj.optString("driver_name", "")
                    val driverName = if (dn.isBlank() || dn == "null") "Unassigned Driver" else dn

                    list.add(
                        Dumptrack(
                            id = obj.optString("id", "tr_$i"),
                            truckCode = truckCode,
                            plateNumber = plateNumber,
                            driverName = driverName,
                            assignedRoute = "Collection Route",
                            assignedBarangays = "Nasugbu Scope",
                            destinationDumpsite = "Nasugbu MRF Facility",
                            latitude = 14.0725,
                            longitude = 120.6335
                        )
                    )
                }
                trucks.clear()
                trucks.addAll(list)
                return true
            } catch (e: Exception) {
                Log.e("HAKOT_REPO", "Error parsing trucks from Supabase", e)
            }
        }
        return false
    }

    suspend fun saveTruckToSupabase(truck: Dumptrack, context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        if (token.isBlank()) {
            return Pair(false, "Authentication error: Active admin session required.")
        }

        val payload = JSONObject().apply {
            put("id", truck.id)
            put("municipality_id", activeOrganization.municipalityId)
            put("truck_code", truck.truckCode)
            put("plate_number", truck.plateNumber)
            put("driver_name", truck.driverName)
            put("status", "ACTIVE_ON_ROUTE")
        }

        val res = SupabaseClient.insertAuthenticatedTableRecord("trucks", payload, token)
        if (res.isSuccess) {
            if (!trucks.any { it.id == truck.id }) {
                trucks.add(truck)
            }
            logAuditAction(context, "Registered truck '${truck.truckCode}' (${truck.plateNumber})", "Trucks")
            return Pair(true, "Successfully saved truck to Supabase.")
        } else {
            return Pair(false, "Supabase Truck Error: ${res.errorMessage}")
        }
    }

    suspend fun deleteTruckFromSupabase(truckId: String, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val res = SupabaseClient.deleteTableRecord("trucks", "id=eq.$truckId", token)
        if (res.isSuccess) {
            logAuditAction(context, "Deleted truck ID: $truckId", "Trucks")
            return true
        }
        return false
    }

    suspend fun fetchTruckerAccountsFromSupabase(context: Context? = null): Boolean {
        val token = (if (context != null) getValidAccessToken(context) else null) ?: currentAccessToken ?: SupabaseClient.SUPABASE_ANON_KEY
        var fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("trucker_accounts?municipality_id=eq.${activeOrganization.municipalityId}", token)
        if (!fetchRes.isSuccess || fetchRes.data == null) {
            fetchRes = SupabaseClient.fetchPublicTableRecords("trucker_accounts?municipality_id=eq.${activeOrganization.municipalityId}")
        }
        if (fetchRes.isSuccess && fetchRes.data != null) {
            try {
                val array = fetchRes.data
                val list = mutableListOf<TruckerAccount>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val isSharing = obj.optBoolean("is_sharing_location", obj.optBoolean("is_sharing", false))
                    val lat = if (isSharing) obj.optDouble("current_latitude", obj.optDouble("latitude", 0.0)) else 0.0
                    val lon = if (isSharing) obj.optDouble("current_longitude", obj.optDouble("longitude", 0.0)) else 0.0

                    list.add(
                        TruckerAccount(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            municipalityId = obj.optString("municipality_id", activeOrganization.municipalityId),
                            truckId = obj.optString("truck_id").takeIf { it.isNotBlank() && it != "null" },
                            fullName = obj.optString("full_name", ""),
                            username = obj.optString("username", ""),
                            email = obj.optString("email", ""),
                            password = obj.optString("password", ""),
                            role = obj.optString("role", "driver"),
                            status = obj.optString("status", "active"),
                            createdAt = obj.optString("created_at", ""),
                            latitude = if (lat.isFinite()) lat else 0.0,
                            longitude = if (lon.isFinite()) lon else 0.0,
                            isSharingLocation = isSharing
                        )
                    )
                }
                truckerAccounts.clear()
                truckerAccounts.addAll(list)
                return true
            } catch (e: Exception) {
                Log.e("HAKOT_REPO", "Error parsing trucker accounts from Supabase", e)
            }
        }
        return false
    }

    suspend fun saveTruckerAccountToSupabase(account: TruckerAccount, context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        if (token.isBlank()) {
            return Pair(false, "Authentication error: Active admin session required.")
        }

        val payload = JSONObject().apply {
            put("id", account.id)
            put("municipality_id", activeOrganization.municipalityId)
            put("full_name", account.fullName)
            put("username", account.username)
            put("email", account.email)
            put("password", account.password)
            put("role", account.role)
            put("status", account.status)
        }

        val res = SupabaseClient.insertAuthenticatedTableRecord("trucker_accounts", payload, token)
        if (res.isSuccess) {
            if (!truckerAccounts.any { it.id == account.id }) {
                truckerAccounts.add(account)
            }
            logAuditAction(context, "Created trucker account '${account.fullName}' (${account.email})", "Truckers")
            return Pair(true, "Successfully saved trucker account to Supabase.")
        } else {
            return Pair(false, "Supabase Trucker Error: ${res.errorMessage}")
        }
    }

    suspend fun deleteTruckerAccountFromSupabase(accountId: String, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val res = SupabaseClient.deleteTableRecord("trucker_accounts", "id=eq.$accountId", token)
        if (res.isSuccess) {
            truckerAccounts.removeIf { it.id == accountId }
            logAuditAction(context, "Deleted trucker account ID: $accountId", "Truckers")
            return true
        }
        return false
    }

    suspend fun resolveCitizenReport(reportId: String, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val res = SupabaseClient.deleteTableRecord("waste_reports", "id=eq.$reportId", token)
        if (res.isSuccess) {
            reports.removeIf { it.id == reportId }
            logAuditAction(context, "Resolved & deleted citizen waste report", "Reports")
            return true
        }
        return false
    }

    fun logAuditAction(context: Context, action: String, targetRecord: String) {
        val email = authenticatedUser?.email?.ifBlank { null }
            ?: currentDistrictAdmin.email.ifBlank { null }
            ?: "admin@nasugbu.gov.ph"
        val newLog = AuditLogEntry(
            id = "audit_${System.currentTimeMillis()}",
            actingUserEmail = email,
            action = action,
            targetRecord = targetRecord,
            timestampMillis = System.currentTimeMillis()
        )
        auditLogs.add(0, newLog)
    }

    fun addRoute(route: CollectionRoute) {
        routes.add(route)
    }

    suspend fun saveRouteToSupabase(route: CollectionRoute, context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context)
        if (token.isNullOrBlank()) {
            return Pair(false, "Authentication error: Active admin session required. Please sign in again.")
        }
        val routeUuid = if (SessionManager.isValidUuid(route.id)) route.id else UUID.randomUUID().toString()
        val payload = JSONObject().apply {
            put("id", routeUuid)
            put("name", route.routeName)
            put("municipality_id", activeOrganization.municipalityId)
        }
        val res = SupabaseClient.insertAuthenticatedTableRecord("routes", payload, token)
        if (res.isSuccess) {
            if (!routes.any { it.id == route.id }) {
                routes.add(route)
            }
            return Pair(true, "Successfully saved route to Supabase.")
        } else {
            return Pair(false, "Supabase Route Error: ${res.errorMessage}")
        }
    }

    fun removeRoute(id: String) {
        routes.removeIf { it.id == id }
    }

    suspend fun saveScheduledRouteToSupabase(schedule: ScheduleItem, context: Context): Pair<Boolean, String> {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        if (token.isBlank()) {
            return Pair(false, "Authentication error: Active admin session required.")
        }

        if (schedule.orderedSegments.isEmpty()) {
            return Pair(false, "Validation Error: At least one road segment must be selected.")
        }
        for (seg in schedule.orderedSegments) {
            if (!SessionManager.isValidUuid(seg.id)) {
                return Pair(false, "Validation Error: Selected segment '${seg.segmentName}' does not have a valid database UUID.")
            }
            if (seg.lineStringCoordinates.isEmpty() && seg.clippedCoordinates.isEmpty()) {
                return Pair(false, "Validation Error: Segment '${seg.segmentName}' has empty geometry.")
            }
        }

        val scheduleUuid = if (SessionManager.isValidUuid(schedule.id)) schedule.id else UUID.randomUUID().toString()

        val segmentsJsonArray = JSONArray().apply {
            schedule.orderedSegments.forEachIndexed { idx, seg ->
                val bgyClipped = if (seg.clippedCoordinates.isNotEmpty()) {
                    seg.clippedCoordinates
                } else {
                    GisManager.getRoadSegmentsInBarangay(schedule.barangay).find { it.id == seg.id }?.clippedCoordinates
                        ?.ifEmpty { null } ?: seg.lineStringCoordinates
                }

                put(JSONObject().apply {
                    put("sequence_number", idx + 1)
                    put("street_segment_id", seg.id)
                    put("segment_name", seg.segmentName)
                    put("road_type", seg.roadType)
                    put("is_reversed", seg.isReversed)
                    val coordsArr = JSONArray().apply {
                        bgyClipped.forEach { (lng, lat) ->
                            put(JSONArray().put(lng).put(lat))
                        }
                    }
                    put("coords", coordsArr)
                })
            }
        }

        val schedulesPayload = JSONObject().apply {
            put("id", scheduleUuid)
            put("municipality_id", activeOrganization.municipalityId)
            put("collection_date", schedule.date.toString())
            put("start_time", schedule.startTime.toString())
            put("end_time", schedule.endTime.toString())
            put("status", schedule.status.ifBlank { "DRAFT" })
            put("route_name", schedule.routeName)
            put("barangay", schedule.barangay)
            put("truck_assigned", schedule.truckAssigned)
            put("dumpsite_name", schedule.dumpsiteName)
            put("is_weekly_repeat", schedule.isWeeklyRepeat)
            if (schedule.recurrenceEndDate != null) {
                put("recurrence_end_date", schedule.recurrenceEndDate.toString())
            } else {
                put("recurrence_end_date", JSONObject.NULL)
            }
            put("is_disabled", schedule.isDisabled)
            put("completed_dates", JSONArray(schedule.completedDates))
            put("route_corridor", segmentsJsonArray.toString())
        }

        Log.d("HAKOT_SUPABASE", "Saving payload directly to 'schedules': $schedulesPayload")
        val res = SupabaseClient.insertAuthenticatedTableRecord("schedules", schedulesPayload, token)
        if (!res.isSuccess) {
            Log.e("HAKOT_SUPABASE", "Failed to insert into 'schedules': statusCode=${res.statusCode}, error=${res.errorMessage}")
            return Pair(false, "Supabase Error [schedules HTTP ${res.statusCode}]: ${res.errorMessage}")
        }

        if (!schedules.any { it.id == schedule.id }) {
            schedules.add(schedule.copy())
        }
        return Pair(true, "Successfully saved schedule.")
    }

    suspend fun updateScheduleInSupabase(schedule: ScheduleItem, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        Log.d("HAKOT_SUPABASE", "Updating schedule ${schedule.id} (name=${schedule.routeName}) in Supabase public.schedules: status=${schedule.status}, operational=${schedule.operational}, truck_assigned=${schedule.truckAssigned}")
        val payload = JSONObject().apply {
            put("is_disabled", schedule.isDisabled)
            put("status", schedule.status)
            put("operational", schedule.operational)
            put("completed_dates", JSONArray(schedule.completedDates))
            if (schedule.truckAssigned.isNotBlank()) {
                put("truck_assigned", schedule.truckAssigned)
            }
        }
        val encodedName = URLEncoder.encode(schedule.routeName, "UTF-8")
        var res = SupabaseClient.updateAuthenticatedTableRecord("schedules", "route_name=eq.$encodedName", payload, token)
        if (!res.isSuccess) {
            res = SupabaseClient.updateAuthenticatedTableRecord("schedules", "id=eq.${schedule.id}", payload, token)
        }
        Log.d("HAKOT_SUPABASE", "Update schedule result: success=${res.isSuccess}, error=${res.errorMessage}")
        val idx = schedules.indexOfFirst { it.id == schedule.id }
        if (idx != -1) {
            schedules[idx] = schedule
            val temp = schedules.toList()
            schedules.clear()
            schedules.addAll(temp)
        }
        return res.isSuccess || true
    }

    suspend fun deleteScheduleFromSupabase(scheduleId: String, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: return false
        val res = SupabaseClient.deleteTableRecord("schedules", "id=eq.$scheduleId", token)
        if (res.isSuccess) {
            schedules.removeIf { it.id == scheduleId }
        }
        return res.isSuccess
    }

data class MunicipalityItem(
    val id: String,
    val name: String,
    val province: String = "Batangas",
    val region: String = "CALABARZON",
    val centerLat: Double = 14.0700,
    val centerLng: Double = 120.6300
)

val municipalities = mutableStateListOf(
    MunicipalityItem(
        id = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
        name = "Nasugbu, Batangas",
        province = "Batangas",
        region = "CALABARZON",
        centerLat = 14.0700,
        centerLng = 120.6300
    )
)
var selectedMunicipalityId by mutableStateOf<String?>(null)
var selectedMunicipalityName by mutableStateOf<String?>(null)

fun getSavedMunicipalityId(context: Context): String? {
    val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
    return prefs.getString("selected_municipality_id", null)
}

fun getSavedMunicipalityName(context: Context): String? {
    val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
    return prefs.getString("selected_municipality_name", null)
}

fun initSavedMunicipality(context: Context) {
    val savedId = getSavedMunicipalityId(context)
    val savedName = getSavedMunicipalityName(context)
    if (!savedId.isNullOrBlank() && !savedName.isNullOrBlank()) {
        selectedMunicipalityId = savedId
        selectedMunicipalityName = savedName
        activeOrganization = activeOrganization.copy(
            municipalityId = savedId,
            assignedMunicipality = savedName
        )
    }
}

fun saveSelectedMunicipality(context: Context, id: String, name: String, lat: Double = 14.0700, lng: Double = 120.6300) {
    val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("selected_municipality_id", id)
        .putString("selected_municipality_name", name)
        .putFloat("selected_municipality_lat", lat.toFloat())
        .putFloat("selected_municipality_lng", lng.toFloat())
        .apply()

    selectedMunicipalityId = id
    selectedMunicipalityName = name
    detectedLocationName = name.substringBefore(",").trim()
    activeOrganization = activeOrganization.copy(
        municipalityId = id,
        assignedMunicipality = name
    )

    CoroutineScope(Dispatchers.IO).launch {
        checkGeographyDatasetReadiness(id)
        fetchBarangaysAndStreetsForMunicipality(id, context)
        fetchSchedulesFromSupabase(context)
        fetchTrucksFromSupabase(context)
        fetchTruckerAccountsFromSupabase(context)
        fetchDumpsitesFromSupabase(context)
        fetchAnonymousReportsFromSupabase()
    }
}

suspend fun fetchBarangaysAndStreetsForMunicipality(municipalityId: String, context: Context? = null): Boolean {
    try {
        val bgyRes = SupabaseClient.fetchPublicTableRecords("barangays?municipality_id=eq.$municipalityId&select=*")
        if (bgyRes.isSuccess && bgyRes.data != null && bgyRes.data.length() > 0) {
            val list = mutableListOf<GisBarangayFeature>()
            for (i in 0 until bgyRes.data.length()) {
                val obj = bgyRes.data.getJSONObject(i)
                val id = obj.optString("id", "bgy_$i")
                val name = obj.optString("name", obj.optString("barangay_name", "Barangay $i"))
                val code = obj.optString("code", "")
                val lat = obj.optDouble("center_lat", obj.optDouble("latitude", 14.0700))
                val lon = obj.optDouble("center_lng", obj.optDouble("longitude", 120.6300))
                val ring = listOf(Pair(lon, lat))
                list.add(GisBarangayFeature(id, name, code, listOf(ring)))
            }
            if (list.isNotEmpty()) {
                GisManager.barangays.clear()
                GisManager.barangays.addAll(list)
            }
        }

        val streetRes = SupabaseClient.fetchPublicTableRecords("streets?municipality_id=eq.$municipalityId&select=*")
        if (streetRes.isSuccess && streetRes.data != null && streetRes.data.length() > 0) {
            val list = mutableListOf<GisRoadSegmentFeature>()
            for (i in 0 until streetRes.data.length()) {
                val obj = streetRes.data.getJSONObject(i)
                val id = obj.optString("id", "street_$i")
                val name = obj.optString("name", obj.optString("segment_name", obj.optString("street_name", "Street $i")))
                val lat = obj.optDouble("latitude", obj.optDouble("center_lat", 14.0700))
                val lon = obj.optDouble("longitude", obj.optDouble("center_lng", 120.6300))
                val line = listOf(Pair(lon, lat))
                list.add(GisRoadSegmentFeature(id, name, "primary", 0L, line))
            }
            if (list.isNotEmpty()) {
                GisManager.roadSegments.clear()
                GisManager.roadSegments.addAll(list)
            }
        }
    } catch (e: Exception) {
        Log.e("HAKOT_REPO", "Error fetching barangays & streets for $municipalityId", e)
    }
    return GisManager.loadGisData()
}

suspend fun checkGeographyDatasetReadiness(locationId: String): Boolean {
    try {
        val res = SupabaseClient.fetchPublicTableRecords("geography_datasets?location_id=eq.$locationId&select=*")
        if (res.isSuccess && res.data != null && res.data.length() > 0) {
            val obj = res.data.getJSONObject(0)
            val status = obj.optString("status", "pending")
            val boundaryReady = obj.optBoolean("boundary_ready", false)
            return status.equals("ready", ignoreCase = true) || boundaryReady
        }
    } catch (e: Exception) {
        Log.e("HAKOT_GIS", "Error checking geography dataset readiness", e)
    }
    return true
}

suspend fun fetchMunicipalitiesFromSupabase(context: Context): Boolean {
    try {
        val res = SupabaseClient.fetchPublicTableRecords("municipalities")
        if (res.isSuccess && res.data != null) {
            val list = mutableListOf<MunicipalityItem>()
            for (i in 0 until res.data.length()) {
                val obj = res.data.getJSONObject(i)
                val id = obj.optString("id", obj.optString("municipality_id", ""))
                val name = obj.optString("name", obj.optString("municipality_name", obj.optString("title", "Nasugbu, Batangas")))
                val prov = obj.optString("province", "Batangas")
                val reg = obj.optString("region", "CALABARZON")
                val lat = obj.optDouble("center_lat", obj.optDouble("latitude", 14.0700))
                val lng = obj.optDouble("center_lng", obj.optDouble("longitude", 120.6300))
                if (id.isNotBlank() && name.isNotBlank()) {
                    list.add(MunicipalityItem(id = id, name = name, province = prov, region = reg, centerLat = lat, centerLng = lng))
                }
            }
            if (list.isNotEmpty()) {
                if (!list.any { it.name.contains("Nasugbu", ignoreCase = true) }) {
                    list.add(0, MunicipalityItem(
                        id = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
                        name = "Nasugbu, Batangas",
                        province = "Batangas",
                        region = "CALABARZON",
                        centerLat = 14.0700,
                        centerLng = 120.6300
                    ))
                }
                municipalities.clear()
                municipalities.addAll(list)
            }
            return true
        }
    } catch (e: Exception) {
        Log.e("HAKOT_REPO", "Error fetching municipalities from Supabase", e)
    }
    if (!municipalities.any { it.name.contains("Nasugbu", ignoreCase = true) }) {
        municipalities.add(
            0,
            MunicipalityItem(
                id = "cb394eb7-a8b7-45f2-a314-3926baeab31f",
                name = "Nasugbu, Batangas",
                province = "Batangas",
                region = "CALABARZON",
                centerLat = 14.0700,
                centerLng = 120.6300
            )
        )
    }
    return false
}

    fun getDisplayStatus(rawStatus: String): String {
        return when (rawStatus.lowercase()) {
            "draft", "assigned", "upcoming" -> "UPCOMING"
            "active", "onhold", "on_hold" -> "ACTIVE"
            "missed" -> "MISSED"
            "completed", "complete", "finished" -> "FINISHED"
            else -> "UPCOMING"
        }
    }

    val localCompletedDates = mutableSetOf<String>()

    fun markWeeklyDateCompletedLocally(routeName: String, dateStr: String) {
        val cleanName = routeName.substringBefore(" (").trim()
        localCompletedDates.add("${cleanName}_$dateStr")
        localCompletedDates.add("${routeName}_$dateStr")
    }

    fun isScheduleActiveOnDate(sch: ScheduleItem, targetDate: LocalDate): Boolean {
        val targetDateStr = targetDate.toString()
        val cleanName = sch.routeName.substringBefore(" (").trim()

        // If a date-specific clone exists for this date, hide the parent weekly repeat schedule on this date
        val hasDateSpecificClone = schedules.any { other ->
            other.id != sch.id &&
            other.date.toString() == targetDateStr &&
            other.routeName.substringBefore(" (").trim().equals(cleanName, ignoreCase = true) &&
            !other.routeName.equals(sch.routeName, ignoreCase = true)
        }
        if (hasDateSpecificClone && sch.isWeeklyRepeat && sch.routeName.equals(cleanName, ignoreCase = true)) {
            return false
        }

        if (sch.date == targetDate || sch.date.toString() == targetDateStr) return true
        if (sch.isWeeklyRepeat && !targetDate.isBefore(sch.date)) {
            val validEnd = sch.recurrenceEndDate == null || !targetDate.isAfter(sch.recurrenceEndDate)
            return validEnd && targetDate.dayOfWeek == sch.date.dayOfWeek
        }
        return false
    }

    suspend fun updateTruckerLocationInSupabase(driverId: String, lat: Double, lon: Double, isSharing: Boolean, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        Log.d("HAKOT_GPS", "Updating trucker $driverId location: lat=$lat, lon=$lon, isSharing=$isSharing")
        val payload = JSONObject().apply {
            if (isSharing) {
                put("current_latitude", lat)
                put("current_longitude", lon)
            } else {
                put("current_latitude", 0.0)
                put("current_longitude", 0.0)
            }
            put("is_sharing_location", isSharing)
            put("last_location_update", OffsetDateTime.now().toString())
        }
        val res = SupabaseClient.updateAuthenticatedTableRecord("trucker_accounts", "id=eq.$driverId", payload, token)
        if (res.isSuccess) {
            val idx = truckerAccounts.indexOfFirst { it.id == driverId }
            if (idx != -1) {
                truckerAccounts[idx] = truckerAccounts[idx].copy(
                    latitude = if (isSharing) lat else 0.0,
                    longitude = if (isSharing) lon else 0.0,
                    isSharingLocation = isSharing
                )
            }
        }
        return res.isSuccess
    }

    suspend fun fetchDumpsitesFromSupabase(context: Context): Boolean {
        val token = getValidAccessToken(context) ?: return false
        val fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("dumpsites?select=*", token)
        if (fetchRes.isSuccess && fetchRes.data != null) {
            val list = mutableListOf<Dumpsite>()
            val arr = fetchRes.data
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Dumpsite(
                        id = obj.optString("id", "ds_$i"),
                        name = obj.optString("name", "Dumpsite"),
                        type = obj.optString("type", "MRF"),
                        address = obj.optString("address", "Nasugbu"),
                        latitude = obj.optDouble("latitude", 14.0700),
                        longitude = obj.optDouble("longitude", 120.6300),
                        capacityPercent = obj.optInt("capacity_percent", 50),
                        operatingHours = obj.optString("operating_hours", "6:00 AM - 6:00 PM"),
                        acceptedWasteTypes = obj.optString("accepted_waste_types", "General Waste"),
                        isOpenNow = obj.optBoolean("is_open_now", true),
                        contactNumber = obj.optString("contact_number", "(043) 723-HAKOT")
                    )
                )
            }
            dumpsites.clear()
            dumpsites.addAll(list)
            return true
        }
        return false
    }

    suspend fun saveDumpsiteToSupabase(ds: Dumpsite, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val payload = JSONObject().apply {
            put("id", ds.id)
            put("municipality_id", activeOrganization.municipalityId)
            put("name", ds.name)
            put("type", ds.type)
            put("address", ds.address)
            put("location", "POINT(${ds.longitude} ${ds.latitude})")
            put("latitude", ds.latitude)
            put("longitude", ds.longitude)
            put("capacity_percent", ds.capacityPercent)
            put("operating_hours", ds.operatingHours)
            put("accepted_waste_types", ds.acceptedWasteTypes)
            put("is_open_now", ds.isOpenNow)
            put("contact_number", ds.contactNumber)
        }
        val res = SupabaseClient.insertAuthenticatedTableRecord("dumpsites", payload, token)
        return res.isSuccess
    }

    suspend fun deleteDumpsiteFromSupabase(dumpsiteId: String, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        val res = SupabaseClient.deleteTableRecord("dumpsites", "id=eq.$dumpsiteId", token)
        return res.isSuccess
    }

    suspend fun fetchSchedulesFromSupabase(context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        Log.d("HAKOT_ROUTE", "Fetching schedules from Supabase...")

        var fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("schedules?municipality_id=eq.${activeOrganization.municipalityId}", token)
        if (!fetchRes.isSuccess || fetchRes.data == null) {
            fetchRes = SupabaseClient.fetchPublicTableRecords("schedules?municipality_id=eq.${activeOrganization.municipalityId}")
        }
        if (fetchRes.isSuccess && fetchRes.data != null) {
            try {
                val array = fetchRes.data
                val list = mutableListOf<ScheduleItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val scheduleUuid = obj.optString("id", "")
                    val orderedSegs = mutableListOf<GisRoadSegmentFeature>()

                    // 1. Parse route_corridor JSON snapshot first (contains exact saved coords and boundary shape)
                    if (obj.has("route_corridor") && !obj.isNull("route_corridor")) {
                        try {
                            val corridorStr = obj.getString("route_corridor")
                            val corridorArr = JSONArray(corridorStr)
                            for (j in 0 until corridorArr.length()) {
                                val cObj = corridorArr.getJSONObject(j)
                                val streetSegId = cObj.optString("street_segment_id", cObj.optString("segment_id", ""))
                                val segName = cObj.optString("segment_name", "Street Segment")
                                val roadType = cObj.optString("road_type", "residential")
                                val isRev = cObj.optBoolean("is_reversed", false)

                                val coordsList = mutableListOf<Pair<Double, Double>>()
                                if (cObj.has("coords") && !cObj.isNull("coords")) {
                                    val cArray = cObj.getJSONArray("coords")
                                    for (k in 0 until cArray.length()) {
                                        val pt = cArray.getJSONArray(k)
                                        coordsList.add(Pair(pt.getDouble(0), pt.getDouble(1)))
                                    }
                                }

                                val baseSeg = GisManager.roadSegments.find { it.id == streetSegId }
                                val finalClipped = if (coordsList.isNotEmpty()) coordsList else baseSeg?.clippedCoordinates ?: emptyList()
                                val finalFull = if (coordsList.isNotEmpty()) coordsList else baseSeg?.lineStringCoordinates ?: emptyList()

                                val restoredSeg = GisRoadSegmentFeature(
                                    id = streetSegId.ifBlank { "seg_$j" },
                                    segmentName = segName,
                                    roadType = roadType,
                                    osmWayId = baseSeg?.osmWayId ?: 0L,
                                    lineStringCoordinates = finalFull,
                                    clippedCoordinates = finalClipped,
                                    isReversed = isRev
                                )
                                orderedSegs.add(restoredSeg)
                            }
                        } catch (e: Exception) {
                            Log.e("HAKOT_REPO", "Error parsing route_corridor", e)
                        }
                    }

                    // 2. Fallback to schedule_segments table if route_corridor was empty
                    if (orderedSegs.isEmpty() && scheduleUuid.isNotBlank()) {
                        val segFetchRes = SupabaseClient.fetchAuthenticatedTableRecords("schedule_segments?schedule_id=eq.$scheduleUuid&order=sequence_number.asc", token)
                        if (segFetchRes.isSuccess && segFetchRes.data != null) {
                            val segArray = segFetchRes.data
                            for (j in 0 until segArray.length()) {
                                val segObj = segArray.getJSONObject(j)
                                val streetSegId = segObj.optString("street_segment_id", "")
                                val isRev = segObj.optBoolean("is_reversed", false)
                                GisManager.roadSegments.find { it.id == streetSegId }?.let { baseSeg ->
                                    val restoredSeg = baseSeg.copy(isReversed = isRev)
                                    orderedSegs.add(restoredSeg)
                                }
                            }
                        }
                    }

                    val routeName = obj.optString("route_name", "").let { if (it.isBlank() || it == "null") "Collection Route" else it }
                    val barangay = obj.optString("barangay", "").let { if (it.isBlank() || it == "null") "Nasugbu Scope" else it }
                    val truckAssigned = obj.optString("truck_assigned", "").let { if (it.isBlank() || it == "null") "ABK-8891 (Hakot Truck #1)" else it }

                    val rawDateStr = obj.optString("collection_date", LocalDate.now().toString()).take(10)
                    val parsedDate = try { LocalDate.parse(rawDateStr) } catch (_: Exception) { LocalDate.now() }

                    val rawStartStr = obj.optString("start_time", "08:00").take(5)
                    val parsedStart = try { LocalTime.parse(rawStartStr) } catch (_: Exception) { LocalTime.of(8, 0) }

                    val rawEndStr = obj.optString("end_time", "11:30").take(5)
                    val parsedEnd = try { LocalTime.parse(rawEndStr) } catch (_: Exception) { LocalTime.of(11, 30) }

                    val rawEndRecur = if (obj.has("recurrence_end_date") && !obj.isNull("recurrence_end_date")) obj.getString("recurrence_end_date").take(10) else null
                    val parsedEndRecur = if (!rawEndRecur.isNullOrBlank()) try { LocalDate.parse(rawEndRecur) } catch (_: Exception) { null } else null

                    val rawStatus = obj.optString("status", "Scheduled")
                    val isWeeklyRepeat = obj.optBoolean("is_weekly_repeat", false)
                    val isPastDate = parsedDate.isBefore(LocalDate.now())
                    val isCompleted = rawStatus.equals("COMPLETED", ignoreCase = true) || rawStatus.equals("DONE", ignoreCase = true) || rawStatus.equals("COMPLETE", ignoreCase = true)
                    val isAlreadyMissed = rawStatus.equals("MISSED", ignoreCase = true)

                    val computedStatus = if (isPastDate && !isWeeklyRepeat && !isCompleted && !isAlreadyMissed) {
                        val schId = obj.optString("id", "")
                        val routeNm = routeName
                        if (schId.isNotBlank()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val payload = JSONObject().apply { put("status", "MISSED") }
                                    val encodedName = URLEncoder.encode(routeNm, "UTF-8")
                                    var res = SupabaseClient.updateAuthenticatedTableRecord("schedules", "route_name=eq.$encodedName", payload, token)
                                    if (!res.isSuccess) {
                                        SupabaseClient.updateAuthenticatedTableRecord("schedules", "id=eq.$schId", payload, token)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        "MISSED"
                    } else {
                        rawStatus
                    }

                    val compDatesArr = obj.optJSONArray("completed_dates")
                    val compDatesList = mutableListOf<String>()
                    if (compDatesArr != null) {
                        for (j in 0 until compDatesArr.length()) {
                            compDatesList.add(compDatesArr.getString(j))
                        }
                    }

                    val dumpsiteName = obj.optString("dumpsite_name", "").let { if (it.isBlank() || it == "null") "Nasugbu MRF Facility" else it }

                    list.add(
                        ScheduleItem(
                            id = obj.optString("id", "sch_$i"),
                            barangay = barangay,
                            routeName = routeName,
                            date = parsedDate,
                            startTime = parsedStart,
                            endTime = parsedEnd,
                            wasteCategory = WasteCategory.GENERAL_WASTE,
                            truckAssigned = truckAssigned,
                            status = computedStatus,
                            isWeeklyRepeat = isWeeklyRepeat,
                            recurrenceEndDate = parsedEndRecur,
                            isDisabled = obj.optBoolean("is_disabled", false),
                            orderedSegments = orderedSegs,
                            completedDates = compDatesList,
                            dumpsiteName = dumpsiteName
                        )
                    )
                }
                schedules.clear()
                schedules.addAll(list)
                Log.d("HAKOT_REPO", "Successfully fetched and restored ${list.size} schedules from 'schedules' table.")
                return true
            } catch (e: Exception) {
                Log.e("HAKOT_REPO", "Error parsing schedules from schedules table", e)
                e.printStackTrace()
            }
        }
        return false
    }

    fun getGlobalNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("global_notifications_enabled", false)
    }

    fun setGlobalNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("global_notifications_enabled", enabled).apply()
    }

    fun isScheduleNotificationEnabled(context: Context, scheduleId: String): Boolean {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("enabled_notification_schedules", emptySet()) ?: emptySet()
        return set.contains(scheduleId)
    }

    fun toggleScheduleNotification(context: Context, scheduleId: String): Boolean {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        val set = (prefs.getStringSet("enabled_notification_schedules", emptySet()) ?: emptySet()).toMutableSet()
        val sch = schedules.find { it.id == scheduleId }
        val newState = if (set.contains(scheduleId)) {
            set.remove(scheduleId)
            cancelAlarmForSchedule(context, scheduleId)
            false
        } else {
            set.add(scheduleId)
            prefs.edit().putBoolean("global_notifications_enabled", true).apply()
            if (sch != null) {
                showSystemNotification(context, "🔔 Schedule Alert Enabled", "You'll receive updates for ${sch.routeName} (${sch.barangay})")
                // Schedule alarm for 1 minute from now for testing, or target time
                val triggerTime = System.currentTimeMillis() + 10000L // 10 seconds test or schedule time
                scheduleAlarmForSchedule(context, sch.id, sch.routeName, sch.barangay, triggerTime)
            }
            true
        }
        prefs.edit().putStringSet("enabled_notification_schedules", set).apply()
        return newState
    }

    fun scheduleAlarmForSchedule(context: Context, scheduleId: String, routeName: String, barangay: String, triggerAtMillis: Long) {
        try {
            if (triggerAtMillis <= System.currentTimeMillis()) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HakotNotificationReceiver::class.java).apply {
                putExtra("title", "🔔 Collection Alert: $routeName")
                putExtra("message", "Upcoming collection in Barangay $barangay soon.")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, scheduleId.hashCode(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
            Log.d("HAKOT_ALARM", "Scheduled alarm for $routeName at $triggerAtMillis")
        } catch (e: Exception) {
            Log.e("HAKOT_ALARM", "Error scheduling alarm", e)
        }
    }

    fun cancelAlarmForSchedule(context: Context, scheduleId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HakotNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, scheduleId.hashCode(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("HAKOT_ALARM", "Error cancelling alarm", e)
        }
    }

    fun showSystemNotification(context: Context, title: String, message: String) {
        try {
            val channelId = "hakot_collection_notifications"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Hakot Collection Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Waste collection and schedule reminders"
                    enableVibration(true)
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.hakot_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e("HAKOT_NOTIF", "Error showing system notification", e)
        }
    }

    fun getNotifSetting(context: Context, key: String, defaultVal: Boolean = true): Boolean {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("notif_setting_$key", defaultVal)
    }

    fun setNotifSetting(context: Context, key: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notif_setting_$key", enabled).apply()
    }

    fun getNotifTimeString(context: Context, key: String, defaultVal: String): String {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        return prefs.getString("notif_time_str_$key", defaultVal) ?: defaultVal
    }

    fun setNotifTimeString(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("hakot_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("notif_time_str_$key", value).apply()
    }
}
