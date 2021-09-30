package com.newit.school_guide.feat.model

import java.util.*

data class EventModel(var id : Long,var title :String,var type : Int,var timeStart:Date? = null,var timeEnd: Date?=null,var allDay: Int?=0)
