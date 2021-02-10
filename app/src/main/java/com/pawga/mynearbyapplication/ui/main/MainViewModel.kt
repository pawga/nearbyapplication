package com.pawga.mynearbyapplication.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pawga.mynearbyapplication.domain.State

class MainViewModel : ViewModel() {

    private val _stateLiveData = MutableLiveData<State>(State.RequiredPermissions)
    val stateLiveData: LiveData<State> =_stateLiveData

    fun setStatus(state: State) {
        _stateLiveData.value = state
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel() as T
        }
    }
}