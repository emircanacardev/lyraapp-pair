package com.turkcell.lyraapp.data.auth

interface TokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun saveAccessToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun clearTokens()
}
