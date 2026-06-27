package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.SessionManager
import com.turkcell.lyraapp.data.auth.TokenStorage
import com.turkcell.lyraapp.data.theme.ThemePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        observeTheme()
        loadUserProfile()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ThemeChanged -> {
                viewModelScope.launch {
                    themePreferencesRepository.setDarkTheme(intent.isDark)
                }
            }
            is ProfileIntent.SettingsClicked -> {
                viewModelScope.launch { _effect.send(ProfileEffect.ShowSettingsMessage) }
            }
            is ProfileIntent.SettingItemClicked -> {
                viewModelScope.launch { _effect.send(ProfileEffect.ShowSettingsMessage) }
            }
            is ProfileIntent.LogoutClicked -> logout()
            is ProfileIntent.PremiumCardClicked -> {
                val state = _uiState.value
                if (!state.hasPremium) {
                    viewModelScope.launch { _effect.send(ProfileEffect.NavigateToPremium) }
                } else if (state.premiumDaysLeft <= 3) {
                    _uiState.update { it.copy(showRenewalDialog = true) }
                } else {
                    viewModelScope.launch { _effect.send(ProfileEffect.NavigateToPremium) }
                }
            }
            is ProfileIntent.RenewalDialogDismissed -> {
                _uiState.update { it.copy(showRenewalDialog = false) }
            }
            is ProfileIntent.RenewWithSubscriptionClicked -> {
                _uiState.update { it.copy(showRenewalDialog = false) }
                viewModelScope.launch { _effect.send(ProfileEffect.NavigateToPremium) }
            }
            is ProfileIntent.RenewOneTimeClicked -> {
                _uiState.update { it.copy(showRenewalDialog = false) }
                viewModelScope.launch { _effect.send(ProfileEffect.NavigateToPremium) }
            }
        }
    }

    private fun logout() {
        val refreshToken = sessionManager.getRefreshToken()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (refreshToken != null) {
                authRepository.logout(refreshToken)
            }
            sessionManager.clear()
            tokenStorage.clearTokens()
            _uiState.update { it.copy(isLoading = false) }
            _effect.send(ProfileEffect.NavigateToLogin)
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.getCurrentUser()
            _uiState.update { it.copy(isLoading = false) }

            result.onSuccess { user ->
                val fullName = "${user.firstName.orEmpty()} ${user.lastName.orEmpty()}".trim()
                val initials = listOfNotNull(
                    user.firstName?.firstOrNull()?.uppercaseChar(),
                    user.lastName?.firstOrNull()?.uppercaseChar()
                ).joinToString("")
                val handle = "@" + (user.firstName.orEmpty() + user.lastName.orEmpty())
                    .lowercase()
                    .replace("\\s".toRegex(), "")

                val membership = user.membership
                val hasPremium = membership?.status == "active"
                val daysLeft = if (hasPremium && membership != null) {
                    runCatching {
                        val expires = OffsetDateTime.parse(membership.expiresAt)
                        val now = OffsetDateTime.now()
                        ChronoUnit.DAYS.between(now, expires).toInt().coerceAtLeast(0)
                    }.getOrDefault(0)
                } else 0

                _uiState.update { current ->
                    current.copy(
                        name = if (fullName.isNotBlank()) fullName else "Kullanıcı",
                        initials = if (initials.isNotBlank()) initials else "?",
                        handle = if (handle.length > 1) handle else "@kullanici",
                        hasPremium = hasPremium,
                        premiumDaysLeft = daysLeft,
                        showRenewalDialog = hasPremium && daysLeft <= 3,
                    )
                }
            }.onFailure {
                _uiState.update { current ->
                    current.copy(name = "Hata oluştu", initials = "!", handle = "@hata")
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferencesRepository.isDarkTheme.collect { isDark ->
                _uiState.update { current -> current.copy(isDarkTheme = isDark ?: false) }
            }
        }
    }
}
