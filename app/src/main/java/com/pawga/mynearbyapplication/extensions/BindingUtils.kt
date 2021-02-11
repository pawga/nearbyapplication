package com.pawga.mynearbyapplication.extensions

import android.view.View
import androidx.core.view.isVisible
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

@BindingAdapter("nearbyVisible")
fun View.setNearbyVisible(status: State) {
    visibility = if (isEnable(this, status)) View.VISIBLE  else View.GONE
}

private fun isEnable(view: View, status: State): Boolean {
    return when (view.id) {
        R.id.find -> { status == State.RequiredOpponent }
        R.id.stop_finding, R.id.progress_bar -> { status == State.Finding }
        R.id.disconnect, R.id.message, R.id.send -> { status == State.Connected }
        else -> true
    }
}