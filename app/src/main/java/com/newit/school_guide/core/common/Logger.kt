package com.newit.school_guide.core.common

import android.util.Log
import com.newit.school_guide.BuildConfig
import java.util.regex.Pattern

object Logger {
    private const val PREFIX_TAG = "school_guide"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(getTag(), message)
        }
    }

    fun v(message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(getTag(), message)
        }
    }

    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(getTag(), message)
        }
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(getTag(), message)
        }
    }

    fun e(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(PREFIX_TAG, message)
        }
    }

    fun e(message: String, t: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(PREFIX_TAG, message, t)
        }
    }

    private fun getTag(): String? {
        val elm: StackTraceElement = Throwable().stackTrace[2] ?: return PREFIX_TAG

        val pkgPattern: Pattern = Pattern.compile(".*\\.")
        val className: String = pkgPattern.matcher(elm.className).replaceAll("")
        val methodName: String = elm.methodName
        val line: Int = elm.lineNumber

        return "$PREFIX_TAG|$className#$methodName($line)"
    }
}