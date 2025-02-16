package com.wit.witvpn.core.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.wit.witvpn.R
import com.wit.witvpn.core.extension.showToast
import com.wit.witvpn.core.util.ProgressDialog
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


abstract class BaseDialogFragment<VM : BaseViewModel, VB : ViewBinding>(private val _binding: (LayoutInflater, ViewGroup?, Boolean) -> VB) : DialogFragment() {

    private var _viewBinding: VB? = null
    protected val viewBinding: VB
        get() {
            return _viewBinding ?: throw IllegalStateException(
                "should never call auto-cleared-value get when it might not be available"
            )
        }

    protected val uiScope: LifecycleCoroutineScope
        get() {
            return viewLifecycleOwner.lifecycleScope
        }

    abstract val viewModel: VM
    private var progressDialog: ProgressDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        println("🤩 ${this.javaClass.simpleName} #${this.hashCode()}  onCreateDialog()")
        // the content
        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // creating the fullscreen dialog
        val dialog = Dialog(requireActivity())
            .apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(root)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        return dialog
    }

    abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    open fun shouldCancelable(): Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("🤣 ${this.javaClass.simpleName} #${this.hashCode()} onCreateView()")
        _viewBinding = _binding(inflater, container, false)
        return viewBinding.root
            .also {
                bindIndicatorEvent()
            }
    }

    private fun bindIndicatorEvent() {
        viewModel
            .indicatorEvent
            .filterIsInstance<IndicatorState.Error>()
            .debounce(300)
            .onEach(this::handleError)
            .launchIn(uiScope)

        viewModel.indicatorEvent
            .filterNot { it is IndicatorState.Error }
            .debounce(300)
            .onEach(this::toggleLoading)
            .launchIn(uiScope)
    }

    open fun handleError(error: IndicatorState.Error) {
        context?.showToast(error.message)
    }

    open fun toggleLoading(loading: IndicatorState) {
        if (loading == IndicatorState.Idle) {
            hideLoading()
            return
        }

        if (progressDialog == null) {
            progressDialog = ProgressDialog()
                .apply {
                    isCancelable = false
                }
        }
        if (progressDialog?.isAdded == true || progressDialog?.dialog?.isShowing == true) {
            return
        }
        progressDialog?.show(childFragmentManager, null)
    }

    private fun hideLoading() {
        if (progressDialog?.isAdded == true && progressDialog?.dialog?.isShowing == true) {
            progressDialog?.dismissAllowingStateLoss()
            progressDialog = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("🤩 ${this.javaClass.simpleName} #${this.hashCode()}  onViewCreated() view: $view")
        onFragmentCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("🥰 ${this.javaClass.simpleName} #${this.hashCode()}   onAttach()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = shouldCancelable()
        println("😀 ${this.javaClass.simpleName} #${this.hashCode()}  onCreate()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("🥵 ${this.javaClass.simpleName} #${this.hashCode()}  onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("🥶 ${this.javaClass.simpleName} #${this.hashCode()}  onDestroy()")
    }

    override fun onDetach() {
        super.onDetach()
        println("💀 ${this.javaClass.simpleName} #${this.hashCode()} onDetach()")
    }

    override fun onResume() {
        super.onResume()
        println("🎃 ${this.javaClass.simpleName} #${this.hashCode()} onResume()")
    }

    override fun onPause() {
        super.onPause()
        println("😱 ${this.javaClass.simpleName} #${this.hashCode()} onPause()")
    }
}