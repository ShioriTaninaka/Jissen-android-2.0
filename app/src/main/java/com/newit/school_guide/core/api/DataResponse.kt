package com.newit.school_guide.core.api

import com.google.gson.annotations.SerializedName

data class DataResponse<out T>(
    @SerializedName("result")
    val statusCode: Int,
    @SerializedName("badges")
    val badges: Int,
    @SerializedName("count_unread")
    val countUnread: Int,
    @SerializedName("data")
    val data: T

)