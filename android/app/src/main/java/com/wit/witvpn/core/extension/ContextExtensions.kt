@file:Suppress("DEPRECATION")

package com.wit.witvpn.core.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import kotlin.math.roundToInt


/**
 * Create by QuyenLX
 */

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}

fun Context.readAsset(fileName: String): String =
    this
        .assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)

fun Context.showToast(message: String?, isShort: Boolean = true) {
    val duration = if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
    Toast.makeText(this, message, duration).show()
}

fun Context?.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this?.resources?.displayMetrics
    ).roundToInt()
}

fun Context.getDimension(@DimenRes id: Int): Float {
    return resources.getDimension(id)
}

fun Context.getColorAttr(attr: Int): Int {
    val theme = theme
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attr))
    val color = typedArray.getColor(0, Color.WHITE)
    typedArray.recycle()
    return color
}

fun Context.getDimensionAttr(attr: Int): Int {
    val theme = theme
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attr))
    val dimen = typedArray.getDimensionPixelSize(0, 0)
    typedArray.recycle()
    return dimen
}

fun Context.getDrawableAttr(attr: Int): Drawable? {
    val theme = theme
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attr))
    val drawable = typedArray.getDrawable(0)
    typedArray.recycle()
    return drawable
}

fun Context?.showKeyboard(editText: EditText) {
    editText.requestFocus()
    val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity?.hideSoftKeyboard() {
    val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    var view = this?.currentFocus
    if (view == null) {
        view = View(this)
    }
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}

@SuppressLint("ClickableViewAccessibility")
fun Activity?.hideKeyboardWhenTouchOutside(view: View?, callback: (() -> Unit)? = null) {
    if (view !is EditText) {
        view?.setOnTouchListener { _, _ ->
            this?.hideSoftKeyboard()
            callback?.invoke()
            return@setOnTouchListener false
        }
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            hideKeyboardWhenTouchOutside(innerView)
        }
    }
}

fun Activity.setWindowStatusNav(
    @ColorInt statusBarColor: Int? = null,
    @ColorInt navBarColor: Int? = null,
    flagLight: Boolean? = null
) {
    val flags =
        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
    val uiVisibility: Int =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    window.decorView.systemUiVisibility = uiVisibility

    window.attributes.flags = window.attributes.flags and flags.inv()
    statusBarColor?.let {
        window.statusBarColor = statusBarColor
    }
    navBarColor?.let {
        window.navigationBarColor = navBarColor
    }
    flagLight?.let {
        updateLightStatusBar(flagLight)
    }
}

fun Activity.updateLightStatusBar(flagLight: Boolean = false) {
    val view = this.window.decorView
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (flagLight) {
            view.systemUiVisibility =
                view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            view.systemUiVisibility =
                view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

fun Activity.getNavGraph(navController: NavController?, navigation: Int): NavGraph? {
    return try {
        navController?.graph
    } catch (e: IllegalStateException) {
        val inflater = navController?.navInflater
        return inflater?.inflate(navigation)
    }
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        startActivity(intent)
    } catch (ex: Exception) {

    }
}

fun Context.dimen(@DimenRes dimen: Int) = resources.getDimensionPixelSize(dimen)

fun Context.getActivity(): Activity? {
    if (this is Activity) {
        return this
    } else {
        val contextWrapper = this as? ContextWrapper
        val baseContext = contextWrapper?.baseContext
            ?: return null
        return baseContext.getActivity()
    }
}

fun Context.openMail(
    emails: Array<String>,
    subject: String,
    extraText: String
) {
    try {
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, extraText)
        emailIntent.selector = selectorIntent

        startActivity(Intent.createChooser(emailIntent, "Send email"))
    } catch (e: ActivityNotFoundException) {
        this.showToast("Not found")
    }
}


fun Context?.rateApp(packageName: String?) {
    val uri: Uri = Uri.parse("market://details?id=${packageName}")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    try {
        this?.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        try {
            this?.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${packageName}")
                )
            )
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }
}

fun Context?.getMoreApps(url: String = "https://play.google.com/store/apps/dev?id=7760393210972195220&hl=vi&gl=US") {
    val uri: Uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        this?.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        this?.showToast(e.message)
    }
}

fun Context.viewContent(path: String) {
    val uri = Uri.parse(path)
    val downloadedFile = DocumentFile.fromSingleUri(this, uri)
    if (downloadedFile?.exists() == false) {
        showToast("File not found")
        return
    }
    val mimeType = this.contentResolver.getType(uri) ?: "*/*"

    val intent = Intent(Intent.ACTION_VIEW)
        .apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    try {
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        this.showToast(e.message)
    }
}

fun Context.shareContent(path: String) {
    val uri = Uri.parse(path)
    val file = DocumentFile.fromSingleUri(this, uri)
    if (file?.exists() == false) {
        showToast("File not found")
        return
    }
    val mimeType = this.contentResolver.getType(uri) ?: "*/*"

    val intent = Intent(Intent.ACTION_SEND)
        .apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, mimeType)
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            type = mimeType
        }

    try {
        this.startActivity(Intent.createChooser(intent, null), null)
    } catch (e: ActivityNotFoundException) {
        this.showToast(e.message)
    }
}

suspend fun Context.deleteFileFromUri(fileUri: String, fileName: String) {
    val file = DocumentFile.fromSingleUri(this, Uri.parse(fileUri))
    if (file?.exists() == true) {
        file.delete()

        withContext(Dispatchers.Main) {
            showToast("Deleted $fileName")
        }
    }
}