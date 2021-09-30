package com.newit.school_guide.feat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Utils
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class SettingViewModel : ViewModel() {
    val settings = MutableLiveData<Resource<Objects>>()
    fun notifySetting(setting : Int){
        viewModelScope.launch {
            settings.postValue(Resource.loading(null))
            try {
                val res = network().postNotifySetting(CommonSharedPreferences.getInstance().getString(Constants.FCM_TOKEN),setting)
                if (res.statusCode == 1) {
                    settings.postValue(Resource.success(null))
                }else{
                    settings.postValue(Resource.error(res.statusCode))
                }
            }catch (e : Exception){
                settings.postValue(Resource.error(Utils.getErrorCode(e)))
            }
        }
    }
}