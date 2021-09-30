package com.newit.school_guide.feat.model


import com.google.gson.annotations.SerializedName

data class Info(
    @SerializedName("unread")
    var unread: Int, // 0: false , 1 : true
    @SerializedName("title")
    var title: String,
    @SerializedName("published")
    var published: String,
    @SerializedName("thumbnail")
    var thumbnail: String,
    @SerializedName("url")
    var url: String,
    @SerializedName("id")
    var id: Int
)