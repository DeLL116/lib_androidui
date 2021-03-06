package com.nochino.support.androidui

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

abstract class AndroidUIApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initLeakCanary()
        initTimber()
    }

    /**
     * Initializes the Leak Canary library. Informs implementations of memory leaks.
     * See https://github.com/square/leakcanary
     */
    private fun initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

    private fun initTimber() {
        // TODO :: WIP :: Create Crash Reporting Tree
        Timber.plant(Timber.DebugTree())
    }
}