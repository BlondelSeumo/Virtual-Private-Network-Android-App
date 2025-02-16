package com.wit.witvpn.core.util

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import android.view.Window
import com.wit.witvpn.R
import com.wit.witvpn.core.extension.dpToPx
import com.wit.witvpn.databinding.DialogRateAppBinding
import com.wit.witvpn.databinding.DialogResetPasswordEmailBinding


/**
 * Created by Furuichi on 26/09/2023
 */
object DialogUtils {
    fun showRateApp(
        context: Context,
        onRefuse: (Dialog) -> Unit,
        onRate: (Dialog) -> Unit,
    ) {
        val dialog = Dialog(context)
        val viewBinding = DialogRateAppBinding.inflate(dialog.layoutInflater)
        //#1
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(viewBinding.root)
        dialog.setCancelable(true)

        //#2
        dialog.window?.apply {
            val widthScreen = context.resources.displayMetrics.widthPixels
            attributes?.windowAnimations = R.style.DialogAnimation
            setBackgroundDrawableResource(R.drawable.background_dialog)
            setLayout(
                widthScreen - context.dpToPx(32f),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        viewBinding.apply {
            btnRefuse.setOnClickListener {
                onRefuse.invoke(dialog)
            }

            btnRate.setOnClickListener {
                onRate.invoke(dialog)
            }
        }

        dialog.show()
    }

    fun showResetPassword(
        context: Context,
        onOk: (Dialog) -> Unit
    ) {
        val dialog = Dialog(context)
        val viewBinding = DialogResetPasswordEmailBinding.inflate(dialog.layoutInflater)
        //#1
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(viewBinding.root)
        dialog.setCancelable(false)

        //#2
        dialog.window?.apply {
            val widthScreen = context.resources.displayMetrics.widthPixels
            attributes?.windowAnimations = R.style.DialogAnimation
            setBackgroundDrawableResource(R.drawable.background_dialog)
            setLayout(
                widthScreen - context.dpToPx(32f),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        viewBinding.apply {
            btnOk.setOnClickListener {
                onOk.invoke(dialog)
            }
        }

        dialog.show()
    }

}