package com.pawga.mynearbyapplication

import android.app.Application
import android.content.Context
import toothpick.config.Module

/**
 * Created by sivannikov
 */

class AppModule(application: Application) : Module() {

    private val context: Context = application.applicationContext

    init {
        bind(Application::class.java)
            .toInstance(application)
        bind(Context::class.java)
            .toInstance(context)
    }
}