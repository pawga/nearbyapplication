package com.pawga.mynearbyapplication

import android.app.Application
import androidx.multidex.MultiDex
import com.pawga.common.extensions.ApplicationScope
import timber.log.Timber
import toothpick.configuration.Configuration
import toothpick.ktp.KTP

/**
 * Created by sivannikov on 11.02.21 16:53
 */

class App  : Application() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        initializeTimber()
        initializeToothpick()
    }


    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeToothpick() {

        if (BuildConfig.DEBUG) {
            KTP.setConfiguration(Configuration.forDevelopment())
            KTP.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        }

        KTP.openRootScope()
            .openSubScope(ApplicationScope::class.java)
            .installModules(AppModule(this))
            .inject(this)
    }
}