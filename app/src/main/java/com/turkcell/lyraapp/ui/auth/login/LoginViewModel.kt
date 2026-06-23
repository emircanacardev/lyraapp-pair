package com.turkcell.lyraapp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.AuthRepository
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

import com.turkcell.lyraapp.data.auth.SessionManager

/**
 * Login ekranının MVI ViewModel'i.
 *
 * Tek giriş noktası [onIntent]'tir. Durum [uiState] üzerinden gözlemlenir; tek seferlik
 * olaylar [effect] kanalından akar.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            is LoginIntent.VerificationCodeChanged -> updateForm { it.copy(verificationCode = intent.value) }
            is LoginIntent.BackToPhoneInput -> updateForm { it.copy(isOtpSent = false, verificationCode = "") }
            is LoginIntent.Submit -> submit()
            is LoginIntent.RegisterClicked -> viewModelScope.launch { _effect.send(LoginEffect.NavigateToRegister) }
        }
    }

    /** Form alanını günceller ve giriş butonunun aktifliğini yeniden türetir. */
    private fun updateForm(transform: (LoginUiState) -> LoginUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isActionEnabled = updated.isFormValid())
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (!state.isActionEnabled || state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (!state.isOtpSent) {
                // OTP İstek Aşaması
                val formattedPhone = "+90" + state.phoneNumber.replace("\\s".toRegex(), "")
                val result = authRepository.requestOtp(formattedPhone)
                _uiState.update { it.copy(isLoading = false) }

                result
                    .onSuccess {
                        _uiState.update { it.copy(isOtpSent = true) }
                        updateForm { it } // Buton aktifliğini yeniden hesapla
                        _effect.send(LoginEffect.ShowMessage("Doğrulama kodu telefonunuza gönderildi."))
                    }
                    .onFailure { error ->
                        _effect.send(LoginEffect.ShowError(error.message ?: "Kod gönderme başarısız."))
                    }
            } else {
                // OTP Doğrulama Aşaması
                val formattedPhone = "+90" + state.phoneNumber.replace("\\s".toRegex(), "")
                val result = authRepository.verifyOtp(formattedPhone, state.verificationCode)
                _uiState.update { it.copy(isLoading = false) }

                result
                    .onSuccess { session ->
                        android.util.Log.d("LoginViewModel", "Saving tokens. access: ${session.accessToken}, refresh: ${session.refreshToken}")
                        sessionManager.setAccessToken(session.accessToken)
                        sessionManager.setRefreshToken(session.refreshToken)
                        if (session.firstTime) {
                            _effect.send(LoginEffect.NavigateToProfileComplete)
                        } else {
                            _effect.send(LoginEffect.NavigateToHome)
                        }
                    }
                    .onFailure { error ->
                        val errorMessage = if (error is retrofit2.HttpException && error.code() == 401) {
                            "SMS kodu yanlış."
                        } else {
                            error.message ?: "Doğrulama başarısız."
                        }
                        _effect.send(LoginEffect.ShowError(errorMessage))
                    }
            }
        }
    }
}

/** Giriş butonunun aktif olması için form geçerlilik kontrolü. */
private fun LoginUiState.isFormValid(): Boolean {
    return if (!isOtpSent) {
        phoneNumber.trim().length >= 10
    } else {
        verificationCode.trim().length >= 6
    }
}
