package com.wit.witvpn.core.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Created by Furuichi on 25/09/2023
 */
object AppConstant {
    object Firebase {
        const val SERVERS = "Servers"
        const val USERS = "users"
        const val ERROR = "errors"
        const val ANONYMOUS = "anonymous"
        const val CONFIGS = "configs"
    }

    object Prefs {
        val vpnServiceId = stringPreferencesKey("vpnServiceId")
        val isRefuseRateApp = booleanPreferencesKey("isRefuseRateApp")
    }
}