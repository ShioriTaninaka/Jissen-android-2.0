package com.newit.school_guide.core.api

import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.newit.school_guide.feat.model.Info
import com.newit.school_guide.feat.model.ResponseCalendar
import com.newit.school_guide.feat.model.Schedule
import retrofit2.http.*
import java.util.*

interface Api {
    @GET("apis/topinfos")
    suspend fun getTopInfos(): DataResponse<ArrayList<Info>>

    @GET("apis/infos")
    suspend fun getInfos(@Query("token") token: String): DataResponse<ArrayList<Info>>

    @GET("apis/calendar")
    suspend fun getSchedule(): ResponseCalendar

    @POST("apis/token")
    @FormUrlEncoded
    suspend fun postToken(@Field("token") token: String,@Field("type") type: Int = 2): DataResponse<Objects> // type 1: iOS , 2 : Android

    @POST("apis/readed")
    @FormUrlEncoded
    suspend fun postReadInfo(@Field("token") token: String,@Field("info_id") type: Int): DataResponse<Objects>

    @POST("apis/notifysetting")
    @FormUrlEncoded
    suspend fun postNotifySetting(@Field("token") token: String,@Field("setting") setting: Int = 1): DataResponse<Objects> // 1 : on , 2 : off
}