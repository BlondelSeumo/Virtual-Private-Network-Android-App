package com.wit.witvpn.core.extension

import android.app.Activity
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.wit.witvpn.core.util.CustomTypefaceSpan
import com.wit.witvpn.core.util.LetterSpacingSpan

/**
 * Created by Furuichi on 05/07/2022
 */
fun TextView.fillColorAndFontBy(@FontRes fontId: Int, @ColorRes color: Int, onClicked: (() -> Unit)? = null, vararg arrStr: String) {

    val spannable = SpannableStringBuilder(text)
        .apply {
            arrStr.forEach {
                val start = text.indexOf(it)
                if (start != -1) {
                    val end = start + it.length
                    setFontSpan(context, fontId, start, end)
                    setColorSpan(context, color, start, end)
                    onClicked?.let { safeClicked ->
                        setClickableSpan(start, end,
                            onClickListener = safeClicked,
                            updateDrawStateBlock = { ds ->
                                ds.isUnderlineText = false
                                ds.bgColor = Color.TRANSPARENT
                            }
                        )
                    }
                }
            }
        }
    this.text = spannable
    if (onClicked != null) {
        this.movementMethod = LinkMovementMethod.getInstance()
    }
}

fun EditText.hideKeyboard(clearFocus: Boolean = true) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
    if (clearFocus) {
        this.clearFocus()
        isCursorVisible = false
    }
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.toggle(): Boolean {
    this.isVisible = !this.isVisible
    return this.isVisible
}


fun EditText.setFontFamilyHint(fontRes: Int, letterSpacing: Float = 0f) {
    val hintText = this.hint.toString()
    val font = ResourcesCompat.getFont(this.context, fontRes)!!
    val span = SpannableString(hintText)
        .apply {
            setSpan(CustomTypefaceSpan(font), 0, hintText.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            setSpan(LetterSpacingSpan(letterSpacing), 0, hintText.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    this.hint = span
}