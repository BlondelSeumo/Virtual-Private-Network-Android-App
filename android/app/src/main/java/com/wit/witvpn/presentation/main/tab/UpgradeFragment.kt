package com.wit.witvpn.presentation.main.tab

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.limerse.iap.DataWrappers
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.base.IndicatorState
import com.wit.witvpn.core.extension.*
import com.wit.witvpn.databinding.FragmentUpgradeBinding
import com.wit.witvpn.presentation.main.MainViewModel
import com.wit.witvpn.presentation.main.MainViewModel.UIState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class UpgradeFragment : BaseFragment<MainViewModel, FragmentUpgradeBinding>(FragmentUpgradeBinding::inflate) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.nav_main_graph)

    private var skuDetails: DataWrappers.SkuDetails? = null

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindState()
    }

    override fun handleError(error: IndicatorState.Error) {}

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {
        viewBinding.btnTerm.setOnClickListener {
            findNavController().navigate(R.id.action_to_privatePolicyFragment)
        }
        viewBinding.btnPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_to_privatePolicyFragment)
        }
    }

    /**
     * STATE
     */
    private fun bindState() {
        viewModel.uiState
            .mapToProperty(UIState::skuDetailsList)
            .onEach(::bindSubscriptions)
            .launchIn(uiScope)

        viewModel.uiState
            .mapToProperty(UIState::purchaseInfo)
            .onEach(::bindPurchaseInfo)
            .launchIn(uiScope)
    }

    private fun bindSubscriptions(values: List<DataWrappers.SkuDetails>) {
        if (values.size == 0) {
            viewBinding.viewPackage.hide()
            viewBinding.btnUpgrade.hide()
            return
        }
        viewBinding.viewPackage.show()
        viewBinding.btnUpgrade.show()

        viewBinding.btnUpgrade.setOnClickListener {
            skuDetails
                ?.let {
                    viewModel.subscribe(requireActivity(), it)
                }
                ?: run {
                    context?.showToast("Service Unavailable.")
                }
        }

        values
            .minByOrNull { it.priceAmountMicros }
            ?.also { skuDetails ->
                this.skuDetails = skuDetails
                viewBinding.viewMonth.apply {
                    root.isActivated = true
                    imgCheck.show()
                    tvPrice.text = skuDetails.formattedPrice
                    tvPackage.text = getString(R.string.monthly)
                    root.setOnClickListener {
                        root.isActivated = true
                        imgCheck.show()
                        viewBinding.viewYear.root.isActivated = false
                        viewBinding.viewYear.imgCheck.hide()
                        this@UpgradeFragment.skuDetails = skuDetails
                    }
                }
            }
        values
            .maxByOrNull { it.priceAmountMicros }
            ?.also { skuDetails ->
                viewBinding.viewYear.apply {
                    imgCheck.hide()
                    tvSummary.show()
                    tvPrice.text = skuDetails.formattedPrice
                    tvPackage.text = getString(R.string.yearly)
                    root.setOnClickListener {
                        root.isActivated = true
                        viewBinding.viewMonth.root.isActivated = false
                        viewBinding.viewMonth.imgCheck.hide()
                        this@UpgradeFragment.skuDetails = skuDetails
                    }
                }
            }
    }

    private fun bindPurchaseInfo(purchaseInfo: DataWrappers.PurchaseInfo?) {
        purchaseInfo ?: run {
            viewBinding.viewSubscriptions.show()
            viewBinding.viewPurchased.hide()
            return
        }
        viewBinding.viewSubscriptions.hide()
        viewBinding.viewPurchased.show()

        val calendar = Calendar.getInstance()
            .apply {
                timeInMillis = purchaseInfo.purchaseTime
            }

        val (packageName, date) = if (purchaseInfo.sku.contains("month")) {
            calendar.add(Calendar.MONTH, 1)
            val dateFormatted = calendar.toStringWithPattern("dd/MM/yyyy")
            val monthly = getString(R.string.monthly).lowercase()
            Pair(monthly, dateFormatted)
        } else {
            calendar.add(Calendar.YEAR, 1)
            val dateFormatted = calendar.toStringWithPattern("dd/MM/yyyy")
            val yearly = getString(R.string.yearly).lowercase()
            Pair(yearly, dateFormatted)
        }

        viewBinding.tvPurchased.text = getString(R.string.you_current_xxx, packageName, date)
    }

}