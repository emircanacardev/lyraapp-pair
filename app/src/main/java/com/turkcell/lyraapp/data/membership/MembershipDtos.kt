package com.turkcell.lyraapp.data.membership

import kotlinx.serialization.Serializable

@Serializable
data class MembershipDto(
    val planId: String,
    val type: String,
    val status: String,
    val autoRenew: Boolean,
    val startedAt: String,
    val expiresAt: String,
)

@Serializable
data class MembershipPlanDto(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val priceKurus: Int,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val autoRenew: Boolean,
)

@Serializable
data class MembershipPlansEnvelope(
    val data: List<MembershipPlanDto>
)

@Serializable
data class CheckoutRequest(
    val plan: String,
    val card: CardRequest,
)

@Serializable
data class CardRequest(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null,
)

@Serializable
data class CheckoutResponseEnvelope(
    val data: CheckoutResponseDto
)

@Serializable
data class CheckoutResponseDto(
    val payment: PaymentDto,
    val membership: MembershipDto,
)

@Serializable
data class PaymentDto(
    val transactionId: String,
    val amountKurus: Int,
    val currency: String,
)
