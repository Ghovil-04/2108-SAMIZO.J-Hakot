package com.example.hakot.data

import android.content.Context
import android.util.Log
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.hakot.model.ReportMessage
import com.example.hakot.model.ReportStatus
import com.example.hakot.model.ReportType
import com.example.hakot.model.ScheduleItem
import com.example.hakot.model.TruckStatus
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

object HakotRepository {
    val today: LocalDate get() = LocalDate.now()

    var selectedMapDate: LocalDate by mutableStateOf(LocalDate.now())

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

    var activeOrganization = OrganizationConfig(
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
        val truckerFetchRes = SupabaseClient.fetchAuthenticatedTableRecords("trucker_accounts?or=(username.eq.$cleanInput,email.eq.$cleanInput)&password.eq.$passwordInput", anonToken)
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
            val profile = currentDistrictAdmin
            isAuthenticated = true
            authenticatedUser = profile
            currentAdminRole = profile.role
            currentAdminName = profile.fullName
            return true
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
        return Pair(result.isSuccess, result.errorMessage ?: "Organization account registered successfully! Pending System Owner review.")
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
                val roleStr = item.optString("requested_role", "")
                val statusStr = item.optString("status", "")

                val role = UserRole.entries.find { it.name.equals(roleStr, ignoreCase = true) } ?: continue
                val status = ApplicationStatus.entries.find { it.name.equals(statusStr, ignoreCase = true) } ?: continue

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
                    list.add(
                        Dumptrack(
                            id = obj.optString("id", "tr_$i"),
                            truckCode = obj.optString("truck_code", "Hakot Truck #${i+1}"),
                            plateNumber = obj.optString("plate_number", "ABK-8891"),
                            driverName = obj.optString("driver_name", "Unassigned Driver"),
                            assignedRoute = obj.optString("assigned_route", "Collection Route"),
                            assignedBarangays = obj.optString("assigned_barangays", "Nasugbu Scope"),
                            destinationDumpsite = obj.optString("destination_dumpsite", "Nasugbu MRF Facility"),
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
            return Pair(true, "Successfully saved truck to Supabase.")
        } else {
            return Pair(false, "Supabase Truck Error: ${res.errorMessage}")
        }
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
            put("is_weekly_repeat", schedule.isWeeklyRepeat)
            if (schedule.recurrenceEndDate != null) {
                put("recurrence_end_date", schedule.recurrenceEndDate.toString())
            } else {
                put("recurrence_end_date", JSONObject.NULL)
            }
            put("is_disabled", schedule.isDisabled)
            put("route_corridor", segmentsJsonArray.toString())
        }

        Log.d("HAKOT_SUPABASE", "Saving payload directly to 'schedules': $schedulesPayload")
        val res = SupabaseClient.insertAuthenticatedTableRecord("schedules", schedulesPayload, token)
        if (!res.isSuccess) {
            Log.e("HAKOT_SUPABASE", "Failed to insert into 'schedules': statusCode=${res.statusCode}, error=${res.errorMessage}")
            return Pair(false, "Supabase Error [schedules HTTP ${res.statusCode}]: ${res.errorMessage}")
        }

        // Insert ordered schedule_segments
        Log.d("HAKOT_ROUTE", "SCHEDULE_SEGMENTS: About to enter child insertion loop. orderedSegments.size = ${schedule.orderedSegments.size}")
        for ((idx, seg) in schedule.orderedSegments.withIndex()) {
            val segUuid = UUID.randomUUID().toString()
            val segPayload = JSONObject().apply {
                put("id", segUuid)
                put("schedule_id", scheduleUuid)
                put("street_segment_id", seg.id)
                put("sequence_number", idx + 1)
                put("is_reversed", seg.isReversed)
                put("progress_status", "not_started")
            }
            Log.d("HAKOT_ROUTE", "SCHEDULE_SEGMENTS: Requesting insert for segment #${idx + 1} (id=${seg.id}, name=${seg.segmentName}): $segPayload")
            val segRes = SupabaseClient.insertAuthenticatedTableRecord("schedule_segments", segPayload, token)
            Log.d("HAKOT_ROUTE", "SCHEDULE_SEGMENTS: Response for segment #${idx + 1}: success=${segRes.isSuccess}, statusCode=${segRes.statusCode}, error=${segRes.errorMessage}")
            if (!segRes.isSuccess) {
                Log.e("HAKOT_ROUTE", "SCHEDULE_SEGMENTS: FAILED to insert segment #${idx + 1} (${seg.id}): HTTP ${segRes.statusCode} - ${segRes.errorMessage}")
                return Pair(false, "Supabase Error [schedule_segments HTTP ${segRes.statusCode}]: ${segRes.errorMessage}")
            }
        }
        Log.d("HAKOT_ROUTE", "SCHEDULE_SEGMENTS: All ${schedule.orderedSegments.size} segments successfully inserted.")

        if (!schedules.any { it.id == schedule.id }) {
            schedules.add(schedule.copy())
        }
        return Pair(true, "Successfully saved to 'schedules' and 'schedule_segments'.")
    }

    suspend fun updateScheduleInSupabase(schedule: ScheduleItem, context: Context): Boolean {
        val token = getValidAccessToken(context) ?: SupabaseClient.SUPABASE_ANON_KEY
        Log.d("HAKOT_SUPABASE", "Updating schedule ${schedule.id} (name=${schedule.routeName}) in Supabase public.schedules: status=${schedule.status}, operational=${schedule.operational}, truck_assigned=${schedule.truckAssigned}")
        val payload = JSONObject().apply {
            put("is_disabled", schedule.isDisabled)
            put("status", schedule.status)
            put("operational", schedule.operational)
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

    fun isScheduleActiveOnDate(sch: ScheduleItem, targetDate: LocalDate): Boolean {
        if (sch.date == targetDate || sch.date.toString() == targetDate.toString()) return true
        if (sch.isWeeklyRepeat && !targetDate.isBefore(sch.date)) {
            val validEnd = sch.recurrenceEndDate == null || !targetDate.isAfter(sch.recurrenceEndDate)
            return validEnd && targetDate.dayOfWeek == sch.date.dayOfWeek
        }
        return false
    }

    suspend fun fetchSchedulesFromSupabase(context: Context): Boolean {
        val token = getValidAccessToken(context) ?: return false
        Log.d("HAKOT_ROUTE", "Fetching schedules from Supabase...")

        var fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("schedules?municipality_id=eq.${activeOrganization.municipalityId}", token)
        if (!fetchRes.isSuccess || fetchRes.data == null || fetchRes.data.length() == 0) {
            fetchRes = SupabaseClient.fetchAuthenticatedTableRecords("schedules?select=*", token)
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
                            orderedSegments = orderedSegs
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
}
