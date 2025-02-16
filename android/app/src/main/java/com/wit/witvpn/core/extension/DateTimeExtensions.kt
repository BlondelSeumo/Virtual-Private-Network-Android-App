package com.wit.witvpn.core.extension

import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

const val FULL_DATE_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
const val PATTERN_DATE_NOTIFICATION = "dd/MM/yyyy hh:ss"

fun Calendar.toStringWithPattern(pattern: String = "HH:mm dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return sdf.format(this.time)
}

fun Date.toStringWithPattern(pattern: String = "HH:mm dd/MM/yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return sdf.format(time)
}

fun Int.theMonth(): String {
    val monthNames = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    return monthNames[this]
}

fun String.toDateWithPattern(pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): Date {
    val sdf = SimpleDateFormat(pattern, Locale.US)
    return try {
        sdf.parse(this) ?: Date()
    } catch (ex: Exception) {
        Date()
    }
}

fun String.toCalendar(): Calendar {
    return Calendar.getInstance()
        .apply {
            time = this@toCalendar.toDateWithPattern()
        }
}

fun Date.toTimeAgo(): String? {
    var convTime: String? = null
    val prefix = ""
    val suffix = "trước"
    try {
        val nowTime = Date()
        val dateDiff = nowTime.time - this.time
        val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
        val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
        when {
            second < 60 -> {
                convTime = "$second giây $suffix"
            }
            minute < 60 -> {
                convTime = "$minute phút $suffix"
            }
            hour < 24 -> {
                convTime = "$hour giờ $suffix"
            }
            day >= 7 -> {
                convTime = when {
                    day > 360 -> {
                        (day / 360).toString() + " năm " + suffix
                    }
                    day > 30 -> {
                        (day / 30).toString() + " tháng " + suffix
                    }
                    else -> {
                        (day / 7).toString() + " tuần " + suffix
                    }
                }
            }
            day < 7 -> {
                convTime = "$day ngày $suffix"
            }
        }
    } catch (e: ParseException) {
        Timber.e(e)
    }
    return convTime
}

fun Date.daysBetween(): Long {
    val nowTime = Date()
    val dateDiff = nowTime.time - this.time
    return TimeUnit.MILLISECONDS.toDays(dateDiff)
}

fun Long.hourMinuteSecond(): String {
    val hours = TimeUnit.SECONDS.toHours(this) % 24
    val minute = TimeUnit.SECONDS.toMinutes(this) % 60
    val second = TimeUnit.SECONDS.toSeconds(this) % 60
    return String.format("%02d:%02d:%02d", hours, minute, second)
}