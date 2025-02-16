package com.wit.witvpn.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseDialogFragment
import com.wit.witvpn.core.extension.showToast
import com.wit.witvpn.core.util.DialogUtils
import com.wit.witvpn.core.util.isValidEmail
import com.wit.witvpn.databinding.FragmentForgotPasswordBinding
import com.wit.witvpn.presentation.auth.AuthViewModel.UiAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class ForgotPasswordFragment : BaseDialogFragment<AuthViewModel, FragmentForgotPasswordBinding>(FragmentForgotPasswordBinding::inflate) {

    override val viewModel: AuthViewModel by viewModels()

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindEvent()
    }

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {
        viewBinding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }

        viewBinding.btnAction.apply {
            setOnClickListener {
                val email = viewBinding.inputEmail.text

                if (!email.isValidEmail()) {
                    viewBinding.inputEmail.error = true
                    context?.showToast(context.getString(R.string.invalid_email_please_try_again_or_use_the_new_one))
                    return@setOnClickListener
                }
                viewModel.accept(UiAction.ResetPassword(email))
            }
        }

        viewBinding.inputEmail.onTextChanged = {
            toggleButton()
        }
    }

    /**
     * EVENT
     */
    private fun bindEvent() {
        viewModel.onSendPasswordResetEmailSuccess
            .onEach {
                DialogUtils.showResetPassword(requireContext()) {
                    it.dismiss()
                    findNavController().popBackStack()
                }
            }
            .launchIn(uiScope)
    }

    private fun toggleButton() {
        val email = viewBinding.inputEmail.text
        val disableButton = email.isEmpty()
        viewBinding.btnAction.isEnabled = !disableButton
    }

}