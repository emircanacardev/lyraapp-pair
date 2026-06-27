package com.turkcell.lyraapp.ui.premium.plan

import com.turkcell.lyraapp.data.membership.MembershipPlanDto

data class PremiumUiState(
    val isLoading: Boolean = false,
    val plans: List<MembershipPlanDto> = emptyList(),
    val selectedPlanId: String = "",
    val errorMessage: String? = null,
)

sealed interface PremiumIntent {
    data class PlanSelected(val planId: String) : PremiumIntent
    data object ContinueClicked : PremiumIntent
    data object BackClicked : PremiumIntent
}

sealed interface PremiumEffect {
    data class NavigateToPayment(
        val planId: String,
        val planType: String,
        val planName: String,
        val planPrice: Int,
    ) : PremiumEffect
    data object NavigateBack : PremiumEffect
}
