package com.example.rateflow.network

import com.example.rateflow.model.Rating
import com.example.rateflow.model.RatingResponse
import retrofit2.Call
import retrofit2.http.*

interface RatingApi {

    @POST("/api/ratings/submit")
    @Headers("Content-Type: application/json")
    fun submitRating(@Body rating: Rating): Call<RatingResponse>

    @GET("/api/ratings/service/{serviceId}/stats")
    fun getRatingStats(@Path("serviceId") serviceId: Int): Call<Map<String, Any>>

    @GET("/api/ratings/service/{serviceId}/feedbacks")
    fun getFeedbacksByService(@Path("serviceId") serviceId: Int): Call<List<Rating>>

    @GET("/api/ratings/check/user/{userId}/service/{serviceId}")
    fun checkUserRating(
        @Path("userId") userId: Int,
        @Path("serviceId") serviceId: Int
    ): Call<Map<String, Any>>

    @GET("/api/ratings/user/{userId}")
    fun getUserRatings(@Path("userId") userId: Int): Call<List<Rating>>

    @DELETE("/api/ratings/delete/{ratingId}")
    fun deleteRating(@Path("ratingId") ratingId: Int): Call<Map<String, Any>>
}