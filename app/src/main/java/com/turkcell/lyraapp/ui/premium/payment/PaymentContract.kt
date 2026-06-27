package com.turkcell.lyraapp.ui.premium.payment

data class PaymentUiState(
    val isLoading: Boolean = false,
    val planId: String = "",
    val planType: String = "",
    val planName: String = "",
    val planPrice: Int = 0,
    val cardNumber: String = "",
    val cardHolder: String = "",
    val expMonth: String = "",
    val expYear: String = "",
    val cvc: String = "",
    val isPayEnabled: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PaymentIntent {
    data class CardNumberChanged(val value: String) : PaymentIntent
    data class CardHolderChanged(val value: String) : PaymentIntent
    data class ExpMonthChanged(val value: String) : PaymentIntent
    data class ExpYearChanged(val value: String) : PaymentIntent
    data class CvcChanged(val value: String) : PaymentIntent
    data object PayClicked : PaymentIntent
    data object BackClicked : PaymentIntent
}

sealed interface PaymentEffect {
    data object NavigateToSuccess : PaymentEffect
    data class ShowError(val message: String) : PaymentEffect
    data object NavigateBack : PaymentEffect
}
