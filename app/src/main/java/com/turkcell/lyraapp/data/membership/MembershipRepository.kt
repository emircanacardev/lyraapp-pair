package com.turkcell.lyraapp.data.membership

interface MembershipRepository {

    suspend fun getPlans(): Result<List<MembershipPlanDto>>

    suspend fun checkout(
        plan: String,
        card: CardRequest,
    ): Result<CheckoutResponseDto>
}
