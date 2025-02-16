package com.wit.witvpn.core.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Patterns
import com.wit.witvpn.R

/**
 * Created by Furuichi on 25/09/2023
 */
object Util {

    fun getDeviceInfo(context: Context): Map<String, String> {
        return mapOf(
            "imei" to Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            "model" to Build.MODEL,
            "os_code" to "${Build.VERSION.SDK_INT}",
            "os_version" to Build.VERSION.RELEASE,
            "product" to Build.PRODUCT,
            "manufacturer" to Build.MANUFACTURER,
            "hardware" to Build.HARDWARE,
            "display_version" to Build.DISPLAY
        )
    }

    fun getResId(input: String?): Int? {
        if (input == null) return null

        val inputTmp = input.lowercase()

        var result = inputTmp.toResId()
        if (result == -1) {
            result = "ic_$inputTmp".toResId()
        }

        if (result == -1) {
            result = "ic_${inputTmp.subSequence(0, 1)}".toResId()
        }
        return result
    }

}

private fun String.toResId(): Int {
    return try {
        val clazz = R.drawable::class.java
        val field = clazz.getDeclaredField(this)
        field.getInt(field)
    } catch (exception: Exception) {
        -1
    }
}

fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()
