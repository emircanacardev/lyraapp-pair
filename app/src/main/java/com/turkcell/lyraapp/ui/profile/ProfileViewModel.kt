package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

/**
 * Profil ekranının MVI ViewModel sınıfı.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileEffect> = _effect.receiveAsFlow()

    init {
        observeTheme()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ThemeChanged -> {
                viewModelScope.launch {
                    themePreferencesRepository.setDarkTheme(intent.isDark)
                }
            }
            is ProfileIntent.SettingsClicked -> {
                viewModelScope.launch {
                    _effect.send(ProfileEffect.ShowSettingsMessage)
                }
            }
            is ProfileIntent.SettingItemClicked -> {
                viewModelScope.launch {
                    _effect.send(ProfileEffect.ShowSettingsMessage)
                }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferencesRepository.isDarkTheme.collect { isDark ->
                _uiState.update { current ->
                    current.copy(isDarkTheme = isDark ?: false)
                }
            }
        }
    }
}
