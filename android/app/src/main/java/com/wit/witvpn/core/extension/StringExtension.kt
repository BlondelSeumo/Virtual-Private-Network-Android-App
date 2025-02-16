package com.wit.witvpn.core.extension

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.wit.witvpn.core.util.CustomTypefaceSpan
import java.text.NumberFormat
import java.util.Currency
import kotlin.math.roundToInt

fun SpannableStringBuilder.setFontSpan(context: Context, @FontRes fontId: Int, start: Int, end: Int): SpannableStringBuilder {
    setSpan(
        CustomTypefaceSpan(ResourcesCompat.getFont(context, fontId)!!),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun SpannableStringBuilder.setFontSizeSpan(context: Context, @DimenRes fontSize: Int, start: Int, end: Int): SpannableStringBuilder {
    setSpan(
        AbsoluteSizeSpan(context.getDimension(fontSize).roundToInt()),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun SpannableStringBuilder.setColorSpan(context: Context, @ColorRes color: Int, start: Int, end: Int): SpannableStringBuilder {
    setSpan(
        ForegroundColorSpan(context.getColor(color)),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}


fun SpannableStringBuilder.setClickableSpan(
    start: Int,
    end: Int,
    onClickListener: (() -> Unit)? = null,
    updateDrawStateBlock: ((TextPaint) -> Unit)? = null
): SpannableStringBuilder {
    setSpan(
        object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickListener?.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                updateDrawStateBlock?.invoke(ds)
            }
        },
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return this
}

fun Double.getCurrency(unit: Currency = Currency.getInstance("VND")): String {
    val formatter: NumberFormat = NumberFormat.getCurrencyInstance()
    formatter.maximumFractionDigits = 0
    formatter.currency = unit
    return formatter.format(this)
}