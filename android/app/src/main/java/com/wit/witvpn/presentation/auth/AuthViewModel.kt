package com.wit.witvpn.presentation.auth

import androidx.lifecycle.viewModelScope
import com.wit.witvpn.core.base.BaseViewModel
import com.wit.witvpn.data.model.UserInfo
import com.wit.witvpn.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Furuichi on 25/09/2023
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepos: UserRepository
) : BaseViewModel() {
    /**
     * Stream of immutable states representative of the UI.
     */
    private val _onSignInSuccess = MutableSharedFlow<Unit>()
    val onSignInSuccess: SharedFlow<Unit>
        get() = _onSignInSuccess

    private val _onSignUpSuccess = MutableSharedFlow<Unit>()
    val onSignUpSuccess: SharedFlow<Unit>
        get() = _onSignUpSuccess

    private val _onSendPasswordResetEmailSuccess = MutableSharedFlow<Unit>()
    val onSendPasswordResetEmailSuccess: SharedFlow<Unit>
        get() = _onSendPasswordResetEmailSuccess

    /**
     * Processor of side effects from the UI which in turn feedback into [accept]
     */
    fun accept(action: UiAction) = viewModelScope.launch {
        _actionEvent.emit(action)
    }

    private val _actionEvent = MutableSharedFlow<UiAction>(extraBufferCapacity = 1)
    private val _uiStateEvent = MutableStateFlow(value = UiState())

    init {
        observeUserInfo()

        _actionEvent
            .map(this::transform)
            .launchIn(viewModelScope)
    }

    private fun observeUserInfo() {
        userRepos.userInfoState
            .onEach { userInfo ->
                _uiStateEvent.update { it.copy(userInfo = userInfo) }
            }
            .launchIn(viewModelScope)
    }

    private fun transform(action: UiAction) {
        when (action) {
            is UiAction.SignIn -> handleSignIn(action)
            is UiAction.SignUp -> handleSignUp(action)
            is UiAction.ResetPassword -> handleResetPassword(action)
        }
    }

    private fun handleSignIn(action: UiAction.SignIn) {
        userRepos.signIn(action.email, action.password)
            .transformLoading()
            .onEach {
                _onSignInSuccess.emit(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun handleSignUp(action: UiAction.SignUp) {
        userRepos.signUp(action.email, action.password)
            .transformLoading()
            .onEach {
                _onSignUpSuccess.emit(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun handleResetPassword(action: UiAction.ResetPassword) {
        userRepos.resetPassword(action.email)
            .transformLoading()
            .onEach(_onSendPasswordResetEmailSuccess::emit)
            .launchIn(viewModelScope)
    }

    sealed class UiAction {
        data class SignIn(val email: String, val password: String) : UiAction()
        data class SignUp(val email: String, val password: String) : UiAction()
        data class ResetPassword(val email: String) : UiAction()

    }

    data class UiState(
        val userInfo: UserInfo? = null
    )
}