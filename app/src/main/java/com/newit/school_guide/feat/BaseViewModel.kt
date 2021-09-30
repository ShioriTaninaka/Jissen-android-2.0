package com.newit.school_guide.feat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.feat.model.ResponseCalendar
import com.newit.school_guide.feat.model.Schedule
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    val schedules = MutableLiveData<Resource<ResponseCalendar>>()
    fun getSchedules() {
        viewModelScope.launch {
            schedules.postValue(Resource.loading(null))
            try {
                val resPost = network().getSchedule()
                if (resPost.statusCode == 1) {
                    schedules.postValue(Resource.success(resPost))
                }else{
                    schedules.postValue(Resource.error(resPost.statusCode))
                }
            } catch (e: Exception) {
                schedules.postValue(Resource.error(Utils.getErrorCode(e)))
            }
        }
    }
}