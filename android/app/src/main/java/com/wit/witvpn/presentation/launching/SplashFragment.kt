package com.wit.witvpn.presentation.launching

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.base.BaseViewModel
import com.wit.witvpn.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class SplashFragment : BaseFragment<BaseViewModel, FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    override val viewModel: BaseViewModel by viewModels()

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        uiScope.launch {
            delay(1500)
            findNavController().navigate(R.id.action_splashFragment_to_nav_main_graph)
        }
    }
}