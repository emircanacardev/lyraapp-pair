package com.turkcell.lyraapp.ui.premium.success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentSuccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planType: String = savedStateHandle["planType"] ?: "recurring"

    private val _uiState = MutableStateFlow(
        PaymentSuccessUiState(
            membershipLabel = if (planType == "recurring") "Premium · Aylık" else "Premium · 30 gün"
        )
    )
    val uiState: StateFlow<PaymentSuccessUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentSuccessEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentSuccessEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PaymentSuccessIntent) {
        when (intent) {
            is PaymentSuccessIntent.StartListeningClicked -> {
                viewModelScope.launch {
                    _effect.send(PaymentSuccessEffect.NavigateToHome)
                }
            }
        }
    }
}
