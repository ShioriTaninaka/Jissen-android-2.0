package com.newit.school_guide.feat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import kotlinx.coroutines.launch

class WebviewViewModel : ViewModel() {
    fun postReadInfo(id : Int){
        viewModelScope.launch {
            val res = network().postReadInfo(CommonSharedPreferences.getInstance().getString(Constants.FCM_TOKEN),id)
            BaseApplication.getInstance()?.isReaded = true
        }
    }
}