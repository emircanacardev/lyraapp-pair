package com.turkcell.lyraapp.ui.auth.complete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.SessionManager
import com.turkcell.lyraapp.data.auth.TokenStorage
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

@HiltViewModel
class ProfileCompleteViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCompleteUiState())
    val uiState: StateFlow<ProfileCompleteUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProfileCompleteEffect>(Channel.BUFFERED)
    val effect: Flow<ProfileCompleteEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: ProfileCompleteIntent) {
        when (intent) {
            is ProfileCompleteIntent.FirstNameChanged -> updateForm { it.copy(firstName = intent.value) }
            is ProfileCompleteIntent.LastNameChanged -> updateForm { it.copy(lastName = intent.value) }
            is ProfileCompleteIntent.BirthDateChanged -> updateForm { it.copy(birthDate = intent.value) }
            is ProfileCompleteIntent.BackToLogin -> backToLogin()
            is ProfileCompleteIntent.Submit -> submit()
        }
    }

    private fun updateForm(transform: (ProfileCompleteUiState) -> ProfileCompleteUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isSubmitEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.updateProfile(
                firstName = state.firstName,
                lastName = state.lastName,
                birthDate = state.birthDate
            )
            _uiState.update { it.copy(isLoading = false) }

            result
                .onSuccess {
                    _effect.send(ProfileCompleteEffect.NavigateToHome)
                }
                .onFailure { error ->
                    _effect.send(ProfileCompleteEffect.ShowError(error.message ?: "Profil güncellenemedi."))
                }
        }
    }

    private fun backToLogin() {
        viewModelScope.launch {
            sessionManager.clear()
            tokenStorage.clearTokens()
            _effect.send(ProfileCompleteEffect.NavigateToLogin)
        }
    }
}

private fun ProfileCompleteUiState.isFormValid(): Boolean {
    return firstName.isNotBlank() && lastName.isNotBlank() && birthDate.trim().length >= 10
}
