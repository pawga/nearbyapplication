package com.pawga.mynearbyapplication.ui.main

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pawga.mynearbyapplication.R
import com.pawga.mynearbyapplication.domain.State
import com.pawga.mynearbyapplication.extensions.exhaustive
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val _stateLiveData = MutableLiveData<State>(State.RequiredPermissions)
    val stateLiveData: LiveData<State> =_stateLiveData

    fun setStatus(state: State) {
        _stateLiveData.value = state
    }

    fun isEnable(view: View, status: State): Boolean {
        return when (view.id) {
            R.id.find_opponents -> { status == State.RequiredOpponent }
            R.id.disconnect, R.id.message, R.id.send -> { status == State.Ready }
            else -> true
        }
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel() as T
        }
    }
}