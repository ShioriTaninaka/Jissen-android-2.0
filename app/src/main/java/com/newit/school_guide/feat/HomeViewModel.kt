package com.newit.school_guide.feat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.newit.school_guide.core.api.Resource
import com.newit.school_guide.core.api.network
import com.newit.school_guide.core.base.BaseApplication
import com.newit.school_guide.core.common.CommonSharedPreferences
import com.newit.school_guide.core.common.Constants
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.core.common.Utils
import com.newit.school_guide.feat.model.Info
import kotlinx.coroutines.launch

class HomeViewModel : BaseViewModel() {
    val topInfos = MutableLiveData<Resource<ArrayList<Info>>>()

    var badges = 0

    fun getTopInfos() {
        viewModelScope.launch {
            topInfos.postValue(Resource.loading(null))
            try {
                val resPost = network().getTopInfos()
                if (resPost.statusCode == 1) {
                    topInfos.postValue(Resource.success(resPost.data))
                    badges = resPost.badges
                }
            } catch (e: Exception) {
                topInfos.postValue(Resource.error(Utils.getErrorCode(e)))
            }
        }
    }

    fun checkAndPostToken(){
        if(CommonSharedPreferences.getInstance().getBoolean(Constants.IS_NEW_TOKEN)){
            viewModelScope.launch {
                try {
                    val res = network().postToken(CommonSharedPreferences.getInstance().getString(Constants.FCM_TOKEN))
                    if (res.statusCode == 1) {
                        Logger.d("post_token success")
                        CommonSharedPreferences.getInstance().putBoolean(Constants.IS_NEW_TOKEN,false)
                    }
                } catch (e: Exception) {
                    Logger.d("post_token fails :" + e.message)
                }
            }
        }
    }

    var data = ArrayList<Info>()

    fun getInfomations(isShowLoading :Boolean = true){
        viewModelScope.launch {
//            if(isShowLoading){
//                infomations.postValue(Resource.loading(null))
//            }
            try {
                val resPost = network().getInfos(CommonSharedPreferences.getInstance().getString(Constants.FCM_TOKEN))
                if (resPost.statusCode == 1) {
//                    data.addAll(resPost.data)
                    BaseApplication.getInstance()?.infoList = ArrayList()
                    BaseApplication.getInstance()?.infoList?.addAll(resPost.data)
                    BaseApplication.getInstance()?.countUnread = resPost.countUnread
                    BaseApplication.getInstance()?.isReaded = false
//                    infomations.postValue(Resource.success(data))
                }else{
//                    infomations.postValue(Resource.error(resPost.statusCode))
                }
            } catch (e: Exception) {
//                infomations.postValue(Resource.error(Utils.getErrorCode(e)))
            }
        }
    }

    fun resetData(){
        data.clear()
    }
}