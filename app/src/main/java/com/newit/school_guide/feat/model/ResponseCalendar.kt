package com.newit.school_guide.feat.model

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class ResponseCalendar(
    @SerializedName("result")
    val statusCode: Int,
    @SerializedName("data")
    val data: ArrayList<Schedule>,
    @SerializedName("delete")
    val dataDelete: ArrayList<Schedule>
)