package com.newit.school_guide.core.common

import android.os.Handler
import android.os.Looper

val UI_HANDLER = Handler(Looper.getMainLooper())
val UI_THREAD: Thread = Looper.getMainLooper().thread

fun uiThread(f: () -> Unit) {
    if (Thread.currentThread() !== UI_THREAD) {
        UI_HANDLER.post(f)
    }
    else {
        f()
    }
}