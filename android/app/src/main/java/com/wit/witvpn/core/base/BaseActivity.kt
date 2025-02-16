package com.wit.witvpn.core.base

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(private val _binding: (LayoutInflater) -> VB) : FragmentActivity() {

    private var _viewBinding: VB? = null
    protected val viewBinding: VB
        get() {
            return _viewBinding ?: throw IllegalStateException(
                "should never call auto-cleared-value get when it might not be available"
            )
        }

    abstract fun onActivityCreated(savedInstanceState: Bundle?)

    abstract val isFlagLight: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowStatusNav(
            statusBarColor = Color.TRANSPARENT,
            flagLight = isFlagLight
        )
        _viewBinding = _binding(layoutInflater)
        setContentView(viewBinding.root)
        onActivityCreated(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
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