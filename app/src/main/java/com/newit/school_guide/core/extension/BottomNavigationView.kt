package com.newit.school_guide.core.extension

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.newit.school_guide.R

fun BottomNavigationView.showBadge(number: String, itemId: Int) {
    removeBadgeCustom(itemId)
    val itemView: BottomNavigationItemView = this.findViewById(itemId)
    val badge: View = LayoutInflater.from(context).inflate(R.layout.layout_badge, this, false)

    val text: TextView = badge.findViewById(R.id.badge_text_view)
    text.text = number

    val badgeLayout: FrameLayout.LayoutParams = FrameLayout.LayoutParams(badge?.layoutParams).apply {
        gravity = Gravity.RIGHT
        leftMargin = resources.getDimension(R.dimen.badge_left_margin).toInt()
    }
    itemView.addView(badge,badgeLayout)
//    var badge = this.getOrCreateBadge(itemId).apply {
//        backgroundColor = ContextCompat.getColor(context, R.color.red)
//    }
//    badge.isVisible = true
}

fun BottomNavigationView.removeBadgeCustom(itemId: Int) {
    val itemView: BottomNavigationItemView = this.findViewById(itemId)
    if (itemView.childCount === 3) {
        itemView.removeViewAt(2)
    }
}