package com.newit.school_guide.core.base

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.newit.school_guide.core.common.DialogUtils
import com.newit.school_guide.core.common.Logger
import com.newit.school_guide.core.extension.addFragment
import com.newit.school_guide.core.extension.popback
import com.newit.school_guide.core.extension.replaceFragment

open abstract class BaseActivity : AppCompatActivity() {

    open interface OnFinishInputText {
        fun onFinish(viewID: Int)
    }

    private var listener: OnFinishInputText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(getLayout())
        setupView()
        initial()

    }

    abstract fun initial()
    abstract fun getLayout(): Int
    abstract fun setupView()

    open fun setOnFinishInputTextListener(onFinishInputText: OnFinishInputText?) {
        this.listener = onFinishInputText
    }

    protected fun gotoFragment(fragment: Fragment, rootFrame: Int, isAddToBackStack: Boolean = false) {
        addFragment(fragment, rootFrame, isAddToBackStack)
    }

    fun replaceFrag(fragment: Fragment, rootFrame: Int, isAddToBackStack: Boolean = false) {
        replaceFragment(fragment, rootFrame)
    }

    fun removeFrag(fragment: Fragment) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }

    fun backToPrev() {
        popback()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus
        val ret = super.dispatchTouchEvent(event)

        if (view is EditText) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = event.rawX + view.left - location[0]
            val y = event.rawY + view.top - location[1]
            if (event.action === MotionEvent.ACTION_DOWN && (x < view.left || x >= view.right || y < view.top || y > view.bottom)) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window.currentFocus!!.windowToken, 0)
                if (listener != null) {
                    listener!!.onFinish(view.id)
                }
                view.clearFocus()
            }
        }
        return ret
    }

    open fun getVisibleFragment(): Fragment? {
        val fragmentManager = this@BaseActivity.supportFragmentManager
        val fragments = fragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible)
                    return fragment
            }
        }
        return null
    }

    open fun showActivity(t: Class<*>?, bundle: Bundle? = null) {
        val intent = Intent(this, t)
        intent.putExtra("extra", bundle)
        startActivity(intent)
//        overridePendingTransition(R.anim.fragment_slide_right_enter, R.anim.fragment_slide_left_exit)
    }

    fun showLoading() {
        DialogUtils.showLoadingDialog(this)
    }

    fun hideLoading() {
        DialogUtils.dismiss()
    }

    fun resetBadgeCounterOfPushMessages() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.cancelAll()
            Logger.d("resetBadgeCounterOfPushMessages")
        }
    }
}