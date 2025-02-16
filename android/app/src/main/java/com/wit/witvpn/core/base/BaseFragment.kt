package com.wit.witvpn.core.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.wit.witvpn.core.extension.hideKeyboardWhenTouchOutside
import com.wit.witvpn.core.extension.showToast
import com.wit.witvpn.core.util.ProgressDialog
import kotlinx.coroutines.flow.*

abstract class BaseFragment<VM : BaseViewModel, VB : ViewBinding>(private val _binding: (LayoutInflater, ViewGroup?, Boolean) -> VB) : Fragment() {
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

    abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    //region#Lifecycler
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("🤣 ${this.javaClass.simpleName} #${this.hashCode()} onCreateView()")
        _viewBinding = _binding(inflater, container, false)
        return viewBinding.root
            .also {
//                it.fitsSystemWindows = fitsSystemWindows
                bindIndicatorEvent()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("🤩 ${this.javaClass.simpleName} #${this.hashCode()}  onViewCreated() view: $view")
        activity?.hideKeyboardWhenTouchOutside(view)
        onFragmentCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("🥰 ${this.javaClass.simpleName} #${this.hashCode()}   onAttach()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    //endregion
}