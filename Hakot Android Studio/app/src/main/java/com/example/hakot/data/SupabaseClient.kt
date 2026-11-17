package com.example.hakot.data

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class SupabaseFetchResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val data: JSONArray?,
    val errorMessage: String?
)

data class SupabaseInsertResult(
    val isSuccess: Boolean,
    val statusCode: Int,
    val errorMessage: String?
)

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: String
)

data class RefreshSessionResult(
    val tokenResponse: AuthTokenResponse?,
    val isPermanentFailure: Boolean,
    val statusCode: Int
)

data class FetchProfileResult(
    val profile: JSONObject?,
    val isPermanentFailure: Boolean,
    val statusCode: Int
)

object SupabaseClient {

    const val SUPABASE_URL = "https://vyegsnkerrbdqacwegff.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_rd7efjfP3gHndcibiIVzHw_sshkqRxv"

    var connectionState: String by mutableStateOf("Checking...")
    var lastSyncTimestamp: String by mutableStateOf("Not Synced Yet")
    var gisLoadError: String by mutableStateOf("")

    private val uuidPattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    private fun isValidUuid(id: String): Boolean {
        if (id.isBlank()) return false
        return uuidPattern.matcher(id).matches()
    }

    private fun currentTimeString(): String {
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
    }

    private fun readErrorStream(conn: HttpURLConnection): String {
        return try {
            conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
        } catch (_: Exception) {
            "Failed to read error stream"
        }
    }

    private fun parseAuthResponse(jsonText: String): AuthTokenResponse? {
        try {
            val json = JSONObject(jsonText)
            val accessToken = json.optString("access_token")
            val refreshToken = json.optString("refresh_token")
            
            // Strict expires_in validation without type coercion from strings or fractional values
            if (!json.has("expires_in") || json.isNull("expires_in")) return null
            val expiresVal = json.get("expires_in")
            
            val expiresIn: Long = when (expiresVal) {
                is Int -> expiresVal.toLong()
                is Long -> expiresVal
                is Number -> {
                    val d = expiresVal.toDouble()
                    if (d % 1.0 != 0.0) return null // Reject fractional values
                    d.toLong()
                }
                else -> return null // Reject Strings, booleans, null, non-numeric values
            }

            if (expiresIn <= 0 || expiresIn > 604800) return null

            val userObj = json.optJSONObject("user")
            val userId = userObj?.optString("id") ?: ""

            if (accessToken.isBlank() || refreshToken.isBlank()) return null
            if (!isValidUuid(userId)) return null

            // Safe expiry timestamp calculation avoiding overflow
            val maxDelta = 604800_000L
            val safeDelta = minOf(expiresIn * 1000, maxDelta)
            val expiresAt = System.currentTimeMillis() + safeDelta

            return AuthTokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                userId = userId
            )
        } catch (_: Exception) {
            return null
        }
    }

    // ---------------------------------------------------------
    // SUPABASE AUTHENTICATION
    // ---------------------------------------------------------

    suspend fun signIn(emailInput: String, passwordInput: String): AuthTokenResponse? =
        withContext(Dispatchers.IO) {
            if (emailInput.isBlank() || passwordInput.isBlank()) return@withContext null
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/auth/v1/token?grant_type=password"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("email", emailInput)
                    put("password", passwordInput)
                }
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                if (code in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    return@withContext parseAuthResponse(jsonText)
                } else {
                    val errText = readErrorStream(connection)
                    Log.e("HAKOT_AUTH", "signIn failed with HTTP $code: $errText")
                }
            } catch (e: Exception) {
                Log.e("HAKOT_AUTH", "signIn exception: ${e.message}")
            } finally {
                connection?.disconnect()
            }
            null
        }

    suspend fun signUp(
        email: String,
        pass: String
    ): AuthTokenResponse? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$SUPABASE_URL/auth/v1/signup"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }
            val payload = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code in 200..299) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                return@withContext parseAuthResponse(jsonText)
            } else {
                val err = readErrorStream(connection)
                Log.e("HAKOT_AUTH", "signUp failed with HTTP $code: $err")
            }
        } catch (e: Exception) {
            Log.e("HAKOT_AUTH", "signUp exception: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        return@withContext null
    }

    suspend fun refreshSession(refreshTokenInput: String): RefreshSessionResult =
        withContext(Dispatchers.IO) {
            if (refreshTokenInput.isBlank()) {
                return@withContext RefreshSessionResult(null, true, 400)
            }
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/auth/v1/token?grant_type=refresh_token"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val payload = JSONObject().apply {
                    put("refresh_token", refreshTokenInput)
                }
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                if (code in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val tokenResp = parseAuthResponse(jsonText)
                    return@withContext RefreshSessionResult(tokenResp, tokenResp == null, code)
                } else {
                    val errText = readErrorStream(connection)
                    Log.e("HAKOT_AUTH", "refreshSession failed with HTTP $code: $errText")
                    val isPermanent = code in 400..403
                    return@withContext RefreshSessionResult(null, isPermanent, code)
                }
            } catch (e: Exception) {
                Log.e("HAKOT_AUTH", "refreshSession exception: ${e.message}")
                return@withContext RefreshSessionResult(null, false, -1)
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun signOutRemote(accessToken: String): Boolean =
        withContext(Dispatchers.IO) {
            if (accessToken.isBlank()) return@withContext false
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/auth/v1/logout"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                return@withContext connection.responseCode in 200..299
            } catch (_: Exception) {
                false
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun fetchProfileResult(userId: String, accessToken: String): FetchProfileResult =
        withContext(Dispatchers.IO) {
            if (userId.isBlank() || accessToken.isBlank() || !isValidUuid(userId)) {
                return@withContext FetchProfileResult(null, true, 400)
            }

            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/rest/v1/profiles?id=eq.$userId&select=*"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val code = connection.responseCode
                if (code in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val arr = JSONArray(jsonText)
                    if (arr.length() == 1) {
                        val profileObj = arr.getJSONObject(0)
                        val profileId = profileObj.optString("id")
                        if (profileId == userId && isValidUuid(profileId)) {
                            return@withContext FetchProfileResult(profileObj, false, code)
                        }
                    }
                }
                connection.disconnect()

                // Fallback to GET_PROFILE Edge Function
                var edgeConn: HttpURLConnection? = null
                try {
                    val edgeUrl = "$SUPABASE_URL/functions/v1/manage-account-applications"
                    edgeConn = (URL(edgeUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("apikey", SUPABASE_ANON_KEY)
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        connectTimeout = 5000
                        readTimeout = 5000
                    }
                    val payload = JSONObject().apply { put("action", "GET_PROFILE") }
                    edgeConn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                    val edgeCode = edgeConn.responseCode
                    if (edgeCode in 200..299) {
                        val edgeText = edgeConn.inputStream.bufferedReader().use { it.readText() }
                        val root = JSONObject(edgeText)
                        val prof = root.optJSONObject("profile")
                        if (prof != null) {
                            return@withContext FetchProfileResult(prof, false, 200)
                        }
                    }
                } catch (_: Exception) {}
                finally { edgeConn?.disconnect() }

                return@withContext FetchProfileResult(null, true, code)
            } catch (e: Exception) {
                Log.e("HAKOT_AUTH", "fetchProfile exception: ${e.message}")
                return@withContext FetchProfileResult(null, false, -1)
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun fetchProfile(userId: String, accessToken: String): JSONObject? {
        return fetchProfileResult(userId, accessToken).profile
    }

    // ---------------------------------------------------------
    // CHECK SUPABASE CONNECTION
    // ---------------------------------------------------------

    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        connectionState = "Synchronizing..."
        var connection: HttpURLConnection? = null

        try {
            val urlString = "$SUPABASE_URL/rest/v1/"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                connectTimeout = 4000
                readTimeout = 4000
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                connectionState = "Connected (Supabase Cloud HTTP 200)"
                lastSyncTimestamp = "Just now (${currentTimeString()})"
                true
            } else {
                connectionState = "Disconnected (HTTP $responseCode)"
                false
            }

        } catch (_: Exception) {
            connectionState = "Offline / Connection Error"
            false
        } finally {
            connection?.disconnect()
        }
    }

    // ---------------------------------------------------------
    // EXPLICIT PUBLIC ANONYMOUS FETCH
    // ---------------------------------------------------------

    suspend fun fetchPublicTableRecords(tableName: String): SupabaseFetchResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                val urlString = "$SUPABASE_URL/rest/v1/$tableName?select=*"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    SupabaseFetchResult(
                        isSuccess = true,
                        statusCode = responseCode,
                        data = JSONArray(jsonText),
                        errorMessage = null
                    )
                } else {
                    val errorText = readErrorStream(connection)
                    SupabaseFetchResult(
                        isSuccess = false,
                        statusCode = responseCode,
                        data = null,
                        errorMessage = "HTTP $responseCode: $errorText"
                    )
                }

            } catch (e: Exception) {
                SupabaseFetchResult(
                    isSuccess = false,
                    statusCode = -1,
                    data = null,
                    errorMessage = e.message ?: "Unknown fetch exception"
                )
            } finally {
                connection?.disconnect()
            }
        }

    // ---------------------------------------------------------
    // EXPLICIT PUBLIC ANONYMOUS INSERT (For Anonymous Reports)
    // ---------------------------------------------------------

    suspend fun insertPublicTableRecord(
        tableName: String,
        jsonPayload: JSONObject
    ): SupabaseInsertResult = withContext(Dispatchers.IO) {
        Log.d("HAKOT_SUPABASE_INSERT", "insertPublicTableRecord started for table: $tableName")
        var connection: HttpURLConnection? = null

        try {
            val urlString = "$SUPABASE_URL/rest/v1/$tableName"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                // Send publishable key via apikey header ONLY; do not send as Authorization: Bearer for anonymous requests
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            Log.d("HAKOT_SUPABASE_INSERT", "Executing HTTP POST request to $urlString")
            connection.outputStream.use { output ->
                output.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            Log.d("HAKOT_SUPABASE_INSERT", "Received HTTP response for $tableName: status code $responseCode")
            if (responseCode in 200..299) {
                SupabaseInsertResult(
                    isSuccess = true,
                    statusCode = responseCode,
                    errorMessage = null
                )
            } else {
                val errorText = readErrorStream(connection)
                SupabaseInsertResult(
                    isSuccess = false,
                    statusCode = responseCode,
                    errorMessage = "HTTP $responseCode: $errorText"
                )
            }

        } catch (e: Exception) {
            Log.e("HAKOT_SUPABASE_INSERT", "Exception during insertPublicTableRecord for $tableName: ${e.javaClass.simpleName} - ${e.message}")
            SupabaseInsertResult(
                isSuccess = false,
                statusCode = -1,
                errorMessage = e.message ?: "Unknown insert exception"
            )
        } finally {
            connection?.disconnect()
        }
    }

    // ---------------------------------------------------------
    // EXPLICIT AUTHENTICATED FETCH (Requires user JWT)
    // ---------------------------------------------------------

    suspend fun fetchAuthenticatedTableRecords(tableName: String, accessToken: String): SupabaseFetchResult =
        withContext(Dispatchers.IO) {
            if (accessToken.isBlank()) {
                Log.e("HAKOT_FETCH", "fetchAuthenticatedTableRecords failed: Missing access token for table $tableName")
                return@withContext SupabaseFetchResult(
                    isSuccess = false,
                    statusCode = 401,
                    data = null,
                    errorMessage = "Missing authenticated user access token"
                )
            }
            var connection: HttpURLConnection? = null

            try {
                val separator = if (tableName.contains("?")) "&" else "?"
                val urlString = "$SUPABASE_URL/rest/v1/$tableName${separator}select=*"
                Log.d("HAKOT_FETCH", "Fetching records from URL: $urlString")

                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 20000
                    readTimeout = 20000
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("HAKOT_FETCH", "Fetch SUCCESS (HTTP $responseCode) for $tableName. Raw JSON response: $jsonText")
                    SupabaseFetchResult(
                        isSuccess = true,
                        statusCode = responseCode,
                        data = JSONArray(jsonText),
                        errorMessage = null
                    )
                } else {
                    val errorText = readErrorStream(connection)
                    Log.e("HAKOT_FETCH", "Fetch FAILED (HTTP $responseCode) for $tableName: $errorText")
                    SupabaseFetchResult(
                        isSuccess = false,
                        statusCode = responseCode,
                        data = null,
                        errorMessage = "HTTP $responseCode: $errorText"
                    )
                }

            } catch (e: Exception) {
                Log.e("HAKOT_FETCH", "Fetch EXCEPTION for $tableName: ${e.message}", e)
                SupabaseFetchResult(
                    isSuccess = false,
                    statusCode = -1,
                    data = null,
                    errorMessage = e.message ?: "Unknown fetch exception"
                )
            } finally {
                connection?.disconnect()
            }
        }

    // ---------------------------------------------------------
    // EXPLICIT AUTHENTICATED INSERT (Requires user JWT)
    // ---------------------------------------------------------

    suspend fun insertAuthenticatedTableRecord(
        tableName: String,
        jsonPayload: JSONObject,
        accessToken: String
    ): SupabaseInsertResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext SupabaseInsertResult(
                isSuccess = false,
                statusCode = 401,
                errorMessage = "Missing authenticated user access token"
            )
        }
        var connection: HttpURLConnection? = null

        try {
            val urlString = "$SUPABASE_URL/rest/v1/$tableName"
            Log.d("HAKOT_SUPABASE", "Inserting into $urlString with payload: $jsonPayload")
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=merge-duplicates,return=representation")
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
            }

            connection.outputStream.use { output ->
                output.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = try {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } catch (_: Exception) { "" }
                Log.d("HAKOT_SUPABASE", "Insert SUCCESS (HTTP $responseCode) for $tableName. Response: $responseText")
                SupabaseInsertResult(
                    isSuccess = true,
                    statusCode = responseCode,
                    errorMessage = null
                )
            } else {
                val errorText = readErrorStream(connection)
                Log.e("HAKOT_SUPABASE", "Insert FAILED (HTTP $responseCode) for $tableName: $errorText")
                SupabaseInsertResult(
                    isSuccess = false,
                    statusCode = responseCode,
                    errorMessage = "HTTP $responseCode: $errorText"
                )
            }

        } catch (e: Exception) {
            Log.e("HAKOT_SUPABASE", "Insert EXCEPTION for $tableName", e)
            SupabaseInsertResult(
                isSuccess = false,
                statusCode = -1,
                errorMessage = e.message ?: "Unknown insert exception"
            )
        } finally {
            connection?.disconnect()
        }
    }

    // ---------------------------------------------------------
    // EXPLICIT AUTHENTICATED UPDATE (Requires user JWT & PATCH)
    // ---------------------------------------------------------

    suspend fun updateAuthenticatedTableRecord(
        tableName: String,
        queryFilter: String,
        jsonPayload: JSONObject,
        accessToken: String
    ): SupabaseInsertResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext SupabaseInsertResult(
                isSuccess = false,
                statusCode = 401,
                errorMessage = "Missing authenticated user access token"
            )
        }
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$SUPABASE_URL/rest/v1/$tableName?$queryFilter"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=representation")
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
            }
            connection.outputStream.use { output ->
                output.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = try {
                    connection.inputStream?.bufferedReader()?.use { it.readText() } ?: "[]"
                } catch (_: Exception) {
                    "[]"
                }
                val jsonArray = try { JSONArray(responseText) } catch (_: Exception) { JSONArray() }
                if (jsonArray.length() == 1) {
                    SupabaseInsertResult(true, responseCode, null)
                } else {
                    SupabaseInsertResult(false, responseCode, "Update failed: Server updated ${jsonArray.length()} rows instead of exactly 1.")
                }
            } else {
                val errorText = readErrorStream(connection)
                SupabaseInsertResult(false, responseCode, "HTTP $responseCode: $errorText")
            }
        } catch (e: Exception) {
            SupabaseInsertResult(false, -1, e.message ?: "Unknown update exception")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun deleteTableRecord(
        tableName: String,
        queryFilter: String,
        accessToken: String
    ): SupabaseInsertResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext SupabaseInsertResult(false, 401, "Missing access token")
        }
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$SUPABASE_URL/rest/v1/$tableName?$queryFilter"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Prefer", "return=minimal")
                connectTimeout = 20000
                readTimeout = 20000
            }
            val code = connection.responseCode
            if (code in 200..299) {
                SupabaseInsertResult(true, code, null)
            } else {
                val err = readErrorStream(connection)
                SupabaseInsertResult(false, code, "HTTP $code: $err")
            }
        } catch (e: Exception) {
            SupabaseInsertResult(false, -1, e.message ?: "Unknown delete exception")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun invokeRpc(
        rpcName: String,
        jsonPayload: JSONObject,
        accessToken: String
    ): SupabaseInsertResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext SupabaseInsertResult(false, 401, "Missing access token")
        }
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$SUPABASE_URL/rest/v1/rpc/$rpcName"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 20000
                readTimeout = 20000
            }
            connection.outputStream.use { output ->
                output.write(jsonPayload.toString().toByteArray(Charsets.UTF_8))
            }
            val code = connection.responseCode
            if (code in 200..299) {
                SupabaseInsertResult(true, code, null)
            } else {
                val err = readErrorStream(connection)
                SupabaseInsertResult(false, code, "HTTP $code: $err")
            }
        } catch (e: Exception) {
            SupabaseInsertResult(false, -1, e.message ?: "Unknown RPC exception")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun invokeManagementEdgeFunction(
        action: String,
        applicationId: String?,
        accessToken: String
    ): SupabaseFetchResult = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            return@withContext SupabaseFetchResult(
                isSuccess = false,
                statusCode = 401,
                data = null,
                errorMessage = "Missing authenticated user access token"
            )
        }
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$SUPABASE_URL/functions/v1/manage-account-applications"
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val payload = JSONObject().apply {
                put("action", action)
                if (!applicationId.isNullOrBlank()) {
                    put("applicationId", applicationId)
                }
            }

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val jsonText = try {
                if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    readErrorStream(connection)
                }
            } catch (_: Exception) { "" }

            if (responseCode in 200..299) {
                val rootObj = try { JSONObject(jsonText) } catch (_: Exception) { JSONObject() }
                val dataArray = rootObj.optJSONArray("data")
                SupabaseFetchResult(
                    isSuccess = true,
                    statusCode = responseCode,
                    data = dataArray ?: JSONArray(),
                    errorMessage = null
                )
            } else {
                val errMsg = try { JSONObject(jsonText).optString("error") } catch (_: Exception) { jsonText }
                SupabaseFetchResult(
                    isSuccess = false,
                    statusCode = responseCode,
                    data = null,
                    errorMessage = errMsg.ifBlank { "HTTP $responseCode" }
                )
            }
        } catch (e: Exception) {
            SupabaseFetchResult(
                isSuccess = false,
                statusCode = -1,
                data = null,
                errorMessage = e.message ?: "Unknown Edge Function exception"
            )
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun uploadCertificate(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        accessToken: String = ""
    ): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val tokenToUse = if (accessToken.isNotBlank() && accessToken != SUPABASE_ANON_KEY) accessToken else SUPABASE_ANON_KEY
                val base64Data = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
                val payload = JSONObject().apply {
                    put("action", "UPLOAD_CERTIFICATE")
                    put("fileName", fileName)
                    put("mimeType", mimeType)
                    put("fileBase64", base64Data)
                }

                val urlString = "$SUPABASE_URL/functions/v1/manage-account-applications"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $tokenToUse")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 20000
                    readTimeout = 20000
                }

                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val responseText = try {
                    if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        readErrorStream(connection)
                    }
                } catch (_: Exception) { "" }

                if (responseCode in 200..299) {
                    val root = try { JSONObject(responseText) } catch (_: Exception) { JSONObject() }
                    val certPath = root.optString("certificatePath", "")
                    if (certPath.isNotBlank()) {
                        Pair(true, certPath)
                    } else {
                        val err = root.optString("error", "Unknown Edge Function upload error")
                        Pair(false, "Upload stage [Edge Function]: $err")
                    }
                } else {
                    val errJson = try { JSONObject(responseText) } catch (_: Exception) { JSONObject() }
                    val errMsg = errJson.optString("error").ifBlank { responseText }
                    val detailedErr = "Edge Function POST /manage-account-applications failed: HTTP $responseCode - $errMsg"
                    Log.e("HAKOT_STORAGE", detailedErr)
                    Pair(false, detailedErr)
                }
            } catch (e: Exception) {
                val detailedErr = "Storage upload exception: ${e.message ?: "Unknown error"}"
                Log.e("HAKOT_STORAGE", detailedErr)
                Pair(false, detailedErr)
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun invokeActivationEdgeFunction(payload: JSONObject): SupabaseFetchResult =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/functions/v1/manage-account-applications"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                connection.outputStream.use { output ->
                    output.write(payload.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val jsonText = try {
                    if (responseCode in 200..299) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else {
                        readErrorStream(connection)
                    }
                } catch (_: Exception) { "" }

                if (responseCode in 200..299) {
                    val rootObj = try { JSONObject(jsonText) } catch (_: Exception) { JSONObject() }
                    SupabaseFetchResult(
                        isSuccess = true,
                        statusCode = responseCode,
                        data = rootObj.optJSONArray("data") ?: JSONArray(),
                        errorMessage = rootObj.optString("error").let { if (it.isBlank()) null else it }
                    )
                } else {
                    val errMsg = try { JSONObject(jsonText).optString("error") } catch (_: Exception) { jsonText }
                    SupabaseFetchResult(
                        isSuccess = false,
                        statusCode = responseCode,
                        data = null,
                        errorMessage = errMsg.ifBlank { "HTTP $responseCode" }
                    )
                }
            } catch (e: Exception) {
                SupabaseFetchResult(
                    isSuccess = false,
                    statusCode = -1,
                    data = null,
                    errorMessage = e.message ?: "Activation network exception"
                )
            } finally {
                connection?.disconnect()
            }
        }

    // ---------------------------------------------------------
    // FETCH NASUGBU GIS GEOJSON (Public RPC)
    // ---------------------------------------------------------

    suspend fun fetchNasugbuMapGeoJson(): Pair<String, String>? =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                gisLoadError = ""
                val urlString = "$SUPABASE_URL/rest/v1/rpc/get_nasugbu_map_geojson"

                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 60000
                }

                connection.outputStream.use { output ->
                    output.write("{}".toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    val errorText = readErrorStream(connection)
                    gisLoadError = "HTTP $responseCode: $errorText"
                    return@withContext null
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val barangays = json.getJSONObject("barangays").toString()
                val roads = json.getJSONObject("roads").toString()

                lastSyncTimestamp = "GIS Synced (${currentTimeString()})"
                Pair(barangays, roads)

            } catch (_: Exception) {
                gisLoadError = "Unknown GIS loading error"
                null
            } finally {
                connection?.disconnect()
            }
        }

    suspend fun fetchVerifiedRecord(userId: String, accessToken: String): JSONObject? =
        withContext(Dispatchers.IO) {
            if (userId.isBlank() || accessToken.isBlank() || !isValidUuid(userId)) return@withContext null
            var connection: HttpURLConnection? = null
            try {
                val urlString = "$SUPABASE_URL/rest/v1/verifieds?auth_user_id=eq.$userId&select=*"
                connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("apikey", SUPABASE_ANON_KEY)
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    connectTimeout = 20000
                    readTimeout = 20000
                }
                val code = connection.responseCode
                if (code in 200..299) {
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    val arr = JSONArray(jsonText)
                    if (arr.length() == 1) {
                        return@withContext arr.getJSONObject(0)
                    }
                }
            } catch (_: Exception) {}
            finally { connection?.disconnect() }
            return@withContext null
        }
}
