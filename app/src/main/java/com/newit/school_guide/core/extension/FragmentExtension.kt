package com.newit.school_guide.core.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.newit.school_guide.R


fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun FragmentActivity.addFragment(fragment: Fragment, rootId: Int, addToBackStack: Boolean = false) {
    if (addToBackStack) {
        supportFragmentManager.beginTransaction().add(rootId, fragment,fragment::class.java.simpleName).addToBackStack(fragment::class.java.simpleName).commit()
    } else {
        supportFragmentManager.inTransaction { add(rootId, fragment,fragment::class.java.simpleName) }
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, rootId: Int, addToBackStack: Boolean = false) {
    if (addToBackStack) {
        supportFragmentManager.beginTransaction()
//            .setCustomAnimations(R.anim.fragment_slide_left_enter, R.anim.fragment_slide_left_exit, R.anim.fragment_slide_right_enter, R.anim.fragment_slide_right_exit)
            .replace(rootId, fragment, fragment::class.java.simpleName).addToBackStack(fragment::class.java.simpleName).commit()
    } else {
        supportFragmentManager.inTransaction { replace(rootId, fragment, fragment::class.java.simpleName) }
    }
}

fun AppCompatActivity.popback() {
    if (supportFragmentManager.backStackEntryCount > 0) {
        if (supportFragmentManager.popBackStackImmediate()) {
//            Logger.d("Popback done")
        }
    } else {
//        Logger.d("Cannot back")
    }
}