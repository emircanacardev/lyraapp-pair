package com.turkcell.lyraapp.ui.auth.complete

data class ProfileCompleteUiState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val isLoading: Boolean = false,
    val isSubmitEnabled: Boolean = false,
)

sealed interface ProfileCompleteIntent {
    data class FirstNameChanged(val value: String) : ProfileCompleteIntent
    data class LastNameChanged(val value: String) : ProfileCompleteIntent
    data class BirthDateChanged(val value: String) : ProfileCompleteIntent
    data object Submit : ProfileCompleteIntent
    data object BackToLogin : ProfileCompleteIntent
}

sealed interface ProfileCompleteEffect {
    data object NavigateToHome : ProfileCompleteEffect
    data object NavigateToLogin : ProfileCompleteEffect
    data class ShowError(val message: String) : ProfileCompleteEffect
}
