package com.wit.witvpn

import android.app.Application
import com.onesignal.OneSignal
import com.wit.witvpn.core.util.AppDebugTree
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by Furuichi on 25/09/2023
 */
@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(AppDebugTree())
        } else {
            Thread.setDefaultUncaughtExceptionHandler { _, e ->
                //TDB
                e.printStackTrace()
            }
        }

        // OneSignal Initialization
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }
}