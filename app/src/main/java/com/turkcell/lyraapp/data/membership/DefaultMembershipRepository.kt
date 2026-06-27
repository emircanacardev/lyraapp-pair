package com.turkcell.lyraapp.data.membership

import javax.inject.Inject

class DefaultMembershipRepository @Inject constructor(
    private val membershipApi: MembershipApi,
) : MembershipRepository {

    override suspend fun getPlans(): Result<List<MembershipPlanDto>> = runCatching {
        membershipApi.getPlans().data
    }

    override suspend fun checkout(
        plan: String,
        card: CardRequest,
    ): Result<CheckoutResponseDto> = runCatching {
        membershipApi.checkout(CheckoutRequest(plan = plan, card = card)).data
    }
}
