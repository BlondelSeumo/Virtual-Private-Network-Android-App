package com.wit.witvpn.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseDialogFragment
import com.wit.witvpn.core.extension.fillColorAndFontBy
import com.wit.witvpn.core.extension.hide
import com.wit.witvpn.core.extension.hideSoftKeyboard
import com.wit.witvpn.core.extension.show
import com.wit.witvpn.core.extension.showToast
import com.wit.witvpn.core.util.isValidEmail
import com.wit.witvpn.databinding.FragmentAuthBinding
import com.wit.witvpn.presentation.auth.AuthViewModel.UiAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class AuthFragment : BaseDialogFragment<AuthViewModel, FragmentAuthBinding>(FragmentAuthBinding::inflate) {

    override val viewModel: AuthViewModel by viewModels()

    enum class Mode {
        SignIn, SignUp
    }

    private var mode: Mode = Mode.SignIn
        set(value) {
            field = value
            toggleMode()
        }

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

        viewBinding.btnAction.setOnClickListener {
            val email = viewBinding.inputEmail.text

            if (!email.isValidEmail()) {
                viewBinding.inputEmail.error = true
                context?.showToast(context?.getString(R.string.invalid_email_please_try_again_or_use_the_new_one))
                return@setOnClickListener
            }
            val password = viewBinding.inputPassword.text
            when (mode) {
                Mode.SignIn -> viewModel.accept(UiAction.SignIn(email, password))
                Mode.SignUp -> {
                    val confirmPassword = viewBinding.inputConfirmPassword.text
                    if (password != confirmPassword) {
                        viewBinding.inputPassword.error = true
                        context?.showToast(context?.getString(R.string.password_doesn_t_match))
                        return@setOnClickListener
                    }

                    viewModel.accept(UiAction.SignUp(email, password))
                }
            }
        }

        viewBinding.btnRight.setOnClickListener {
            mode = if (mode == Mode.SignIn) Mode.SignUp else Mode.SignIn
        }

        viewBinding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_forgotPasswordFragment)
        }

        viewBinding.tvSummary.fillColorAndFontBy(
            R.font.worksans_bold,
            R.color.accent_2,
            onClicked = {
                findNavController().navigate(R.id.action_to_privatePolicyFragment)
            },
            "Privacy Policy", "Term of Conditions"
        )

        viewBinding.inputEmail.onTextChanged = {
            toggleButton()
        }
        viewBinding.inputPassword.onTextChanged = {
            toggleButton()
        }
        viewBinding.inputConfirmPassword.onTextChanged = {
            toggleButton()
        }
    }

    /**
     * EVENT
     */
    private fun bindEvent() {
        viewModel.onSignUpSuccess
            .onEach {
                mode = Mode.SignIn
            }
            .launchIn(uiScope)

        viewModel.onSignInSuccess
            .onEach {
                findNavController().popBackStack()
            }
            .launchIn(uiScope)
    }

    private fun toggleMode() {
        clearFocus()
        resetInput()

        when (mode) {
            Mode.SignIn -> {
                viewBinding.tvTitle.text = getString(R.string.sign_in)
                viewBinding.btnRight.text = getString(R.string.register)
                viewBinding.btnAction.text = getString(R.string.sign_in)
                viewBinding.btnForgotPassword.show()
                viewBinding.inputConfirmPassword.hide()
                viewBinding.tvSummary.hide()
            }

            Mode.SignUp -> {
                viewBinding.tvTitle.text = getString(R.string.register)
                viewBinding.btnRight.text = getString(R.string.sign_in)
                viewBinding.btnAction.text = getString(R.string.register)
                viewBinding.btnForgotPassword.hide()
                viewBinding.inputConfirmPassword.show()
                viewBinding.tvSummary.show()
            }
        }
    }

    private fun toggleButton() {
        val email = viewBinding.inputEmail.text
        val password = viewBinding.inputPassword.text
        val disableButton = email.isEmpty() || password.isEmpty()
        viewBinding.btnAction.isEnabled = !disableButton
    }

    private fun clearFocus() {
        activity?.hideSoftKeyboard()
        viewBinding.inputEmail.clearFocus()
        viewBinding.inputPassword.clearFocus()
        viewBinding.inputConfirmPassword.clearFocus()
    }

    private fun resetInput() {
        viewBinding.inputEmail.reset()
        viewBinding.inputPassword.reset()
        viewBinding.inputConfirmPassword.reset()
    }

}