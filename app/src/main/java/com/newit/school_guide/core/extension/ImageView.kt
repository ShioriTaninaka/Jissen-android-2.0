package com.newit.school_guide.core.extension

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.newit.school_guide.BuildConfig
import com.newit.school_guide.R

fun ImageView.loadUrl(url: String?, placeholder: Int? = null) {
    val requestOption = RequestOptions()
    requestOption.placeholder(R.drawable.err_picture)
    placeholder?.let {
        requestOption.placeholder(placeholder)
    }
    requestOption.dontAnimate()
    Glide.with(context).load(url).apply(requestOption).into(this)
}