package com.pawga.mynearbyapplication.ui.main

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pawga.mynearbyapplication.R
import com.pawga.mynearbyapplication.domain.State

class MainViewModel : ViewModel() {

    private val _stateLiveData = MutableLiveData<State>(State.RequiredPermissions)
    val stateLiveData: LiveData<State> =_stateLiveData

    fun setStatus(state: State) {
        _stateLiveData.value = state
    }

    fun findOpponents() {
        _stateLiveData.value = State.Ready
    }

    fun disconnect() {
        _stateLiveData.value = State.RequiredOpponent
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel() as T
        }
    }
}