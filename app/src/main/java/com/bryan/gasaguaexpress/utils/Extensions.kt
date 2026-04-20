package com.bryan.gasaguaexpress.utils

import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadImage(url: String) {
    val uri = url.toUri().buildUpon().scheme("https").build()
    Glide.with(context)
        .load(uri)
        .into(this)
}