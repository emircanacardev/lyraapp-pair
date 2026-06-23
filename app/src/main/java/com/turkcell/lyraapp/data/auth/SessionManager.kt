package com.turkcell.lyraapp.data.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun getAccessToken(): String? = accessToken
    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun getRefreshToken(): String? = refreshToken
    fun setRefreshToken(token: String?) {
        refreshToken = token
    }

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
