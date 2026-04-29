package com.example.rateflow.model

import com.google.gson.annotations.SerializedName

data class Rating(
    @SerializedName("ratingId")
    var ratingId: Int? = null,

    @SerializedName("serviceId")
    var serviceId: Int? = null,

    @SerializedName("userId")
    var userId: Int? = null,

    @SerializedName("starRate")
    var starRate: Int? = null,

    @SerializedName("feedbackText")
    var feedbackText: String? = null,

    @SerializedName("userName")
    var userName: String? = null,

    @SerializedName("dateCreated")
    var dateCreated: String? = null
)