package com.foddy.app

import android.app.Application
import com.foddy.app.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FoddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        }
    }
}
