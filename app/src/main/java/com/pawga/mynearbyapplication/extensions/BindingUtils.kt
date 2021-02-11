package com.pawga.mynearbyapplication.extensions

import android.view.View
import androidx.databinding.BindingAdapter
import com.pawga.mynearbyapplication.R
import com.pawga.mynearbyapplication.domain.State

/**
 * Created by sivannikov
 */

@BindingAdapter("nearbyEnable")
fun View.setNearbyEnable(status: State) {
    isEnabled = isEnable(this, status)
}

private fun isEnable(view: View, status: State): Boolean {
    return when (view.id) {
        R.id.find_opponents -> { status == State.RequiredOpponent }
        R.id.disconnect, R.id.message, R.id.send -> { status == State.Connected }
        else -> true
    }
}