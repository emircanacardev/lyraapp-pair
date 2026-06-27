package com.turkcell.lyraapp.ui.premium.success

data class PaymentSuccessUiState(
    val membershipLabel: String = "Premium · 30 gün",
)

sealed interface PaymentSuccessIntent {
    data object StartListeningClicked : PaymentSuccessIntent
}

sealed interface PaymentSuccessEffect {
    data object NavigateToHome : PaymentSuccessEffect
}
