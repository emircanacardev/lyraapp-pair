package com.turkcell.lyraapp.data.network

import com.turkcell.lyraapp.data.auth.AuthTokensEnvelope
import com.turkcell.lyraapp.data.auth.TokenRefreshRequest
import com.turkcell.lyraapp.data.auth.TokenStorage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val json: Json,
) : Authenticator {

    // Separate bare client — no interceptors, no authenticator (avoids circular dependency)
    private val refreshClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops: give up after 1 retry
        if (responseCount(response) >= 2) return null

        return runBlocking {
            val refreshToken = tokenStorage.getRefreshToken() ?: return@runBlocking null

            val body = json.encodeToString(TokenRefreshRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())

            val refreshRequest = Request.Builder()
                .url("https://streaming-api.halitkalayci.com/api/v1/auth/refresh")
                .post(body)
                .build()

            try {
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                if (!refreshResponse.isSuccessful) {
                    tokenStorage.clearTokens()
                    return@runBlocking null
                }

                val responseBody = refreshResponse.body?.string()
                    ?: run { tokenStorage.clearTokens(); return@runBlocking null }

                val tokens = json.decodeFromString<AuthTokensEnvelope>(responseBody).data
                tokenStorage.saveAccessToken(tokens.accessToken)
                tokenStorage.saveRefreshToken(tokens.refreshToken)

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokens.accessToken}")
                    .build()
            } catch (e: Exception) {
                tokenStorage.clearTokens()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var r = response.priorResponse
        while (r != null) { count++; r = r.priorResponse }
        return count
    }
}
