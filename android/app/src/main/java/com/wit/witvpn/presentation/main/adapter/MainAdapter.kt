package com.wit.witvpn.presentation.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wit.witvpn.presentation.main.MainFragment

/**
 * Created by Furuichi on 26/09/2023
 */
class MainAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return MainFragment.Tab.values().size
    }

    override fun createFragment(position: Int): Fragment {
        return MainFragment.Tab.values()[position].fragment
    }

}