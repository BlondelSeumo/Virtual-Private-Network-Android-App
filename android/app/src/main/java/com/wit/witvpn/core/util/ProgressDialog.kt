package com.wit.witvpn.core.util

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.wit.witvpn.R

/**
 * Created by Furuichi on 08/07/2022
 */
class ProgressDialog : DialogFragment(R.layout.dialog_progress) {
    override fun getTheme(): Int {
        return R.style.ProgressDialogStyle
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            manager.commit(true) {
                add(this@ProgressDialog, tag)
            }
        } catch (ignored: IllegalStateException) {
        }
    }
}