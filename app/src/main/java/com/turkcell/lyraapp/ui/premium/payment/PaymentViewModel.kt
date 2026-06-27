package com.turkcell.lyraapp.ui.premium.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.CardRequest
import com.turkcell.lyraapp.data.membership.MembershipRepository
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
class PaymentViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planId: String = savedStateHandle["planId"] ?: ""
    private val planType: String = savedStateHandle["planType"] ?: ""
    private val planName: String = savedStateHandle["planName"] ?: ""
    private val planPrice: Int = savedStateHandle["planPrice"] ?: 0

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            planId = planId,
            planType = planType,
            planName = planName,
            planPrice = planPrice,
        )
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.CardNumberChanged -> {
                val digits = intent.value.filter { it.isDigit() }.take(16)
                updateForm { it.copy(cardNumber = digits) }
            }
            is PaymentIntent.CardHolderChanged -> updateForm { it.copy(cardHolder = intent.value) }
            is PaymentIntent.ExpMonthChanged -> updateForm { it.copy(expMonth = intent.value) }
            is PaymentIntent.ExpYearChanged -> updateForm { it.copy(expYear = intent.value) }
            is PaymentIntent.CvcChanged -> updateForm { it.copy(cvc = intent.value) }
            is PaymentIntent.PayClicked -> pay()
            is PaymentIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(PaymentEffect.NavigateBack) }
            }
        }
    }

    private fun updateForm(transform: (PaymentUiState) -> PaymentUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            updated.copy(isPayEnabled = updated.isFormValid())
        }
    }

    private fun PaymentUiState.isFormValid(): Boolean {
        val rawNumber = cardNumber.replace(" ", "")
        val yearInt = expYear.toIntOrNull()
        val yearValid = yearInt != null && (yearInt >= 2024 || yearInt in 24..99)
        return rawNumber.length == 16 &&
            cardHolder.isNotBlank() &&
            expMonth.toIntOrNull()?.let { it in 1..12 } == true &&
            yearValid &&
            cvc.length in 3..4
    }

    private fun pay() {
        val state = _uiState.value
        if (!state.isPayEnabled || state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val expMonthInt = state.expMonth.toIntOrNull() ?: 0
            val rawYear = state.expYear.toIntOrNull() ?: 0
            val expYearInt = if (rawYear in 0..99) rawYear + 2000 else rawYear
            membershipRepository.checkout(
                plan = state.planType,
                card = CardRequest(
                    number = state.cardNumber,
                    expMonth = expMonthInt,
                    expYear = expYearInt,
                    cvc = state.cvc,
                    holderName = state.cardHolder.takeIf { it.isNotBlank() },
                )
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(PaymentEffect.NavigateToSuccess)
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                _effect.send(PaymentEffect.ShowError(error.message ?: "Ödeme başarısız."))
            }
        }
    }
}
