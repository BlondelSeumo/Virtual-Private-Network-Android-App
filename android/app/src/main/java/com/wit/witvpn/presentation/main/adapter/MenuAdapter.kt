package com.wit.witvpn.presentation.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wit.witvpn.core.extension.inflate
import com.wit.witvpn.databinding.ItemMenuBinding
import com.wit.witvpn.presentation.main.MainFragment

/**
 * Created by Furuichi on 26/09/2023
 */
class MenuAdapter(private val onTabSelected: (MainFragment.Tab) -> Unit) : ListAdapter<MainFragment.Tab, MenuAdapter.MenuViewHolder>(diff) {
    companion object {
        private val diff = object : DiffUtil.ItemCallback<MainFragment.Tab>() {
            override fun areItemsTheSame(oldItem: MainFragment.Tab, newItem: MainFragment.Tab): Boolean {
                return oldItem.ordinal == newItem.ordinal
            }

            override fun areContentsTheSame(oldItem: MainFragment.Tab, newItem: MainFragment.Tab): Boolean {
                return oldItem == newItem
            }

        }
    }

    private var tabSelected: MainFragment.Tab = MainFragment.Tab.Home
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, true)
        }

    fun setCurrentTab(tab: MainFragment.Tab) {
        tabSelected = tab
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuAdapter.MenuViewHolder {
        val viewBinding = parent.inflate(ItemMenuBinding::inflate)
        return MenuViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        val item = getItem(position)
        holder.bindData(item)
    }

    override fun onBindViewHolder(holder: MenuAdapter.MenuViewHolder, position: Int) {
        val tab = getItem(position) ?: return
        holder.bindData(tab)
    }

    inner class MenuViewHolder(private val viewBinding: ItemMenuBinding) : RecyclerView.ViewHolder(viewBinding.root) {

        fun bindData(tab: MainFragment.Tab) {
            viewBinding.root.isActivated = tabSelected == tab
            viewBinding.tvTitle.text = tab.title
            viewBinding.imgLeft.setImageResource(tab.iconRes)
            viewBinding.root.setOnClickListener {
                onTabSelected.invoke(tab)
            }
        }
    }
}