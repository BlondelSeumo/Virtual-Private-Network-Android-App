package com.wit.witvpn.core.util

import timber.log.Timber

class AppDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format("#Timber: #%s", Thread.currentThread().name)
    }
}