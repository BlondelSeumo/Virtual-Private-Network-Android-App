package com.wit.witvpn.presentation.main.tab

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.limerse.iap.DataWrappers
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.base.IndicatorState
import com.wit.witvpn.core.extension.mapToProperty
import com.wit.witvpn.core.extension.mapToPropertyNotNull
import com.wit.witvpn.core.extension.toStringWithPattern
import com.wit.witvpn.data.model.UserInfo
import com.wit.witvpn.databinding.FragmentAccountBinding
import com.wit.witvpn.presentation.main.MainViewModel
import com.wit.witvpn.presentation.main.MainViewModel.UIAction
import com.wit.witvpn.presentation.main.MainViewModel.UIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class AccountFragment : BaseFragment<MainViewModel, FragmentAccountBinding>(FragmentAccountBinding::inflate) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.nav_main_graph)

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindState()
    }

    override fun handleError(error: IndicatorState.Error) {}

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {
        viewBinding.btnLogout.setOnClickListener {
            viewModel.accept(UIAction.SignOut)
        }
    }

    /**
     * STATE
     */
    private fun bindState() {
        viewModel.uiState
            .mapToPropertyNotNull(UIState::userInfo)
            .onEach(::bindUserInfo)
            .launchIn(uiScope)

        viewModel.uiState
            .mapToProperty(UIState::purchaseInfo)
            .onEach(::bindPurchaseInfo)
            .launchIn(uiScope)
    }

    private fun bindUserInfo(userInfo: UserInfo) {
        viewBinding.tvEmail.text = userInfo.email
    }

    private fun bindPurchaseInfo(purchaseInfo: DataWrappers.PurchaseInfo?) {
        purchaseInfo ?: run {
            viewBinding.tvPackage.text = getString(R.string.free)
            return
        }

        val calendar = Calendar.getInstance()
            .apply {
                timeInMillis = purchaseInfo.purchaseTime
            }

        val text = if (purchaseInfo.sku.contains("month")) {
            calendar.add(Calendar.MONTH, 1)
            val dateFormatted = calendar.toStringWithPattern("dd/MM/yyyy")
            val monthly = getString(R.string.monthly)
            "$monthly $dateFormatted"
        } else {
            calendar.add(Calendar.YEAR, 1)
            val dateFormatted = calendar.toStringWithPattern("dd/MM/yyyy")
            val yearly = getString(R.string.yearly)
            "$yearly $dateFormatted"
        }

        viewBinding.tvPackage.text = text
    }

}