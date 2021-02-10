package com.pawga.mynearbyapplication.domain

/**
 * Created by sivannikov
 */

sealed class State {
    object Ready : State()
    object Loading : State()
    data class Error(val data: String) : State()
    object RequiredPermissions : State()
    object RequiredOpponent : State()
    object NonPermissions : State()
}