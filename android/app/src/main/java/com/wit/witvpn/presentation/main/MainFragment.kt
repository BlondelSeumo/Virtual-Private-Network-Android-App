package com.wit.witvpn.presentation.main

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.base.updateLightStatusBar
import com.wit.witvpn.core.extension.hide
import com.wit.witvpn.core.extension.mapToProperty
import com.wit.witvpn.core.extension.show
import com.wit.witvpn.databinding.FragmentMainBinding
import com.wit.witvpn.presentation.main.MainViewModel.UIAction
import com.wit.witvpn.presentation.main.MainViewModel.UIState
import com.wit.witvpn.presentation.main.adapter.MainAdapter
import com.wit.witvpn.presentation.main.adapter.MenuAdapter
import com.wit.witvpn.presentation.main.tab.AccountFragment
import com.wit.witvpn.presentation.main.tab.HomeFragment
import com.wit.witvpn.presentation.main.tab.LocationFragment
import com.wit.witvpn.presentation.main.tab.UpgradeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(FragmentMainBinding::inflate) {

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.nav_main_graph)

    enum class Tab {
        Home,
        Upgrade,
        Account,
        Location
        ;

        val fragment: Fragment
            get() = when (this) {
                Home -> HomeFragment()
                Upgrade -> UpgradeFragment()
                Account -> AccountFragment()
                Location -> LocationFragment()
            }

        val title: String
            get() = when (this) {
                Home -> "Home"
                Upgrade -> "Upgrade to VIP"
                Account -> "Account"
                Location -> "Select a Location"
            }

        val iconRes: Int
            get() = when (this) {
                Home -> R.drawable.ic_home
                Upgrade -> R.drawable.ic_vip
                Account -> R.drawable.ic_user_shield
                else -> 0
            }
    }

    private val drawerToggle = object : DrawerLayout.DrawerListener {

        override fun onDrawerOpened(drawerView: View) {
            activity?.updateLightStatusBar(false)
        }

        override fun onDrawerClosed(drawerView: View) {
            activity?.updateLightStatusBar(viewBinding.viewPager.currentItem == Tab.Upgrade.ordinal)
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        override fun onDrawerStateChanged(newState: Int) {}
    }
    private val menuAdapter: MenuAdapter
        get() = viewBinding.rvMenu.adapter as MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.accept(UIAction.FetchData)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindState()
        bindCommand()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.drawerLayout.addDrawerListener(drawerToggle)
    }

    override fun onPause() {
        super.onPause()
        viewBinding.drawerLayout.removeDrawerListener(drawerToggle)
    }

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {
        viewBinding.viewPager.apply {
            isUserInputEnabled = false
            adapter = MainAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        }

        viewBinding.drawerLayout.apply {
            setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
        }

        viewBinding.btnMenu.setOnClickListener {
            if (viewBinding.viewPager.currentItem == Tab.Location.ordinal) {
                viewModel.accept(UIAction.SetCurrentTab(Tab.Home))
                return@setOnClickListener
            }
            viewBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        viewBinding.btnClose.setOnClickListener {
            viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        viewBinding.rvMenu.apply {
            setHasFixedSize(true)
            adapter = MenuAdapter {
                viewBinding.drawerLayout.closeDrawer(GravityCompat.START)
                when (it) {
                    Tab.Home -> {
                        viewModel.accept(UIAction.SetCurrentTab(Tab.Home))
                    }

                    Tab.Upgrade -> {
                        viewModel.accept(UIAction.SetCurrentTab(Tab.Upgrade))
                    }

                    Tab.Account -> {
                        if (viewModel.uiState.value.userInfo == null) {
                            findNavController().navigate(R.id.action_mainFragment_to_nav_auth_graph)
                            return@MenuAdapter
                        }

                        viewModel.accept(UIAction.SetCurrentTab(Tab.Account))
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * STATE
     */
    private fun bindState() {
        Tab.values()
            .filter { it.iconRes != 0 }
            .toList()
            .also(menuAdapter::submitList)

        viewModel.uiState
            .mapToProperty(UIState::tab)
            .onEach(::setCurrentItem)
            .launchIn(uiScope)

        viewModel.uiState
            .mapToProperty(UIState::tab)
            .filter { it != Tab.Location }
            .onEach(menuAdapter::setCurrentTab)
            .launchIn(uiScope)

    }

    /**
     * COMMAND
     */
    private fun bindCommand() {

    }

    private fun setCurrentItem(tab: Tab) {
        viewBinding.viewPager.setCurrentItem(tab.ordinal, false)
        when (tab) {
            Tab.Home -> updateHomeUI()
            Tab.Upgrade -> updateUpgradeUI()
            Tab.Account -> updateAccountUI()
            Tab.Location -> updateLocationUI()
        }
    }

    private fun updateHomeUI() {
        activity?.updateLightStatusBar(false)
        viewBinding.container.setBackgroundResource(R.color.primary)
        viewBinding.btnMenu.setImageResource(R.drawable.ic_menu)
        viewBinding.tvTitle.apply {
            show()
            setText(R.string.home)
        }
        viewBinding.tvUpgrade.hide()
    }

    private fun updateUpgradeUI() {
        activity?.updateLightStatusBar(true)
        viewBinding.container.setBackgroundResource(R.color.color_upgrade_background)
        viewBinding.btnMenu.setImageResource(R.drawable.ic_menu)
        viewBinding.tvTitle.hide()
        viewBinding.tvUpgrade.show()
    }

    private fun updateAccountUI() {
        activity?.updateLightStatusBar(false)
        viewBinding.container.setBackgroundResource(R.color.primary)
        viewBinding.btnMenu.setImageResource(R.drawable.ic_menu)
        viewBinding.tvTitle.apply {
            show()
            setText(R.string.account)
        }
        viewBinding.tvUpgrade.hide()
    }

    private fun updateLocationUI() {
        activity?.updateLightStatusBar(false)
        viewBinding.container.setBackgroundResource(R.color.primary)
        viewBinding.btnMenu.setImageResource(R.drawable.ic_close)
        viewBinding.tvTitle.apply {
            show()
            text = Tab.Location.title
        }
        viewBinding.tvUpgrade.hide()
    }

}