package com.pawga.mynearbyapplication.domain

/**
 * Created by sivannikov
 */

sealed class State {
    object Connected : State()
    object Finding : State()
    object RequiredPermissions : State()
    object RequiredOpponent : State()
    object NonPermissions : State()
    data class Error(val data: String) : State()
    //data class ReceivedData(val data: String) : State()
}