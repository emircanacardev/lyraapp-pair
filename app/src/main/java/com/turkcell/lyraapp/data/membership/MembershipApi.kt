package com.turkcell.lyraapp.data.membership

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MembershipApi {

    @GET("api/v1/memberships/plans")
    suspend fun getPlans(): MembershipPlansEnvelope

    @POST("api/v1/memberships/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): CheckoutResponseEnvelope
}
