package com.newit.school_guide.core.common


import android.preference.PreferenceManager
import com.newit.school_guide.core.base.BaseApplication


class CommonSharedPreferences {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance())
    private val KEY_ACCOUNT = "account"
    private val KEY_LESSON_TRIAL = "lesson_trial"

    companion object {
        private val INSTANCE = CommonSharedPreferences()
        fun getInstance() = INSTANCE
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        if (!sharedPreferences.contains(key))
            defaultValue
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getFloat(key: String): Float {
        return sharedPreferences.getFloat(key, 1.0f)
    }

    fun putFloat(key: String, value: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    fun getBoolean(key: String): Boolean =
        sharedPreferences.getBoolean(key, false)

    fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int): Int = sharedPreferences.getInt(key, defaultValue)

    fun getInt(key: String): Int = sharedPreferences.getInt(key, -1)

    fun putInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getLong(key: String, defaultValue: Long): Long =
        sharedPreferences.getLong(key, defaultValue)

    fun putLong(key: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun putString(key: String, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String =
        sharedPreferences.getString(key, defaultValue)!!

    fun remove(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

//    fun saveAccount(account : Account){
//        val json = Gson().toJson(account)
//        putString(KEY_ACCOUNT,json)
//    }
//
//    fun getAccount(): Account?{
//        val json = getString(KEY_ACCOUNT)
//        return Gson().fromJson(json,Account::class.java)
//    }

    fun removeAccount() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_ACCOUNT)
        editor.apply()
    }
}