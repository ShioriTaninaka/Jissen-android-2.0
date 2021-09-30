package com.newit.school_guide.feat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.feat.model.Info
import kotlinx.coroutines.launch

class InfomationViewModel : ViewModel(){
    val infomations = MutableLiveData<Resource<ArrayList<Info>>>()
    var data = ArrayList<Info>()

    fun getInfomations(isShowLoading :Boolean = true){
        viewModelScope.launch {
            if(isShowLoading){
                infomations.postValue(Resource.loading(null))
            }
            try {
                val resPost = network().getInfos(CommonSharedPreferences.getInstance().getString(Constants.FCM_TOKEN))
                if (resPost.statusCode == 1) {
                    data.addAll(resPost.data)
                    BaseApplication.getInstance()?.infoList = ArrayList()
                    BaseApplication.getInstance()?.infoList?.addAll(resPost.data)
                    BaseApplication.getInstance()?.countUnread = resPost.countUnread
                    BaseApplication.getInstance()?.isReaded = false
                    infomations.postValue(Resource.success(data))
                }else{
                    infomations.postValue(Resource.error(resPost.statusCode))
                }
            } catch (e: Exception) {
                infomations.postValue(Resource.error(Utils.getErrorCode(e)))
            }
        }
    }

    fun resetData(){
        data.clear()
    }
}