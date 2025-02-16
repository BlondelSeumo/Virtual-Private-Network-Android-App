package com.wit.witvpn.presentation.main.tab

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.base.IndicatorState
import com.wit.witvpn.core.extension.mapToProperty
import com.wit.witvpn.databinding.FragmentLocationBinding
import com.wit.witvpn.presentation.main.MainViewModel
import com.wit.witvpn.presentation.main.MainViewModel.UIAction
import com.wit.witvpn.presentation.main.MainViewModel.UIState
import com.wit.witvpn.presentation.main.adapter.LocationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class LocationFragment : BaseFragment<MainViewModel, FragmentLocationBinding>(FragmentLocationBinding::inflate) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.nav_main_graph)

    private val locationAdapter: LocationAdapter
        get() = viewBinding.rvLocation.adapter as LocationAdapter

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindState()
    }

    override fun handleError(error: IndicatorState.Error) {}

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {
        viewBinding.rvLocation.apply {
            setHasFixedSize(true)
            adapter = LocationAdapter {
                viewModel.accept(UIAction.SetCurrentVPNServer(vpnServer = it))
            }
        }


    }

    /**
     * STATE
     */
    private fun bindState() {
        viewModel.uiState
            .mapToProperty(UIState::servers)
            .onEach(locationAdapter::submitList)
            .launchIn(uiScope)

        viewModel.uiState
            .mapToProperty(UIState::currentServer)
            .onEach(locationAdapter::setCurrentServer)
            .launchIn(uiScope)

    }


}