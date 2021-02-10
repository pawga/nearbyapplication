package com.pawga.mynearbyapplication.ui.main

import com.pawga.mynearbyapplication.domain.State

/**
 * Created by sivannikov
 */
interface MainView {
    fun render(state: State)
}