package com.wit.witvpn.presentation.main.adapter

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wit.witvpn.core.extension.inflate
import com.wit.witvpn.core.util.Util
import com.wit.witvpn.data.model.VPNServer
import com.wit.witvpn.databinding.ItemLocationBinding

class LocationAdapter(private val onItemClicked: (VPNServer) -> Unit) : ListAdapter<VPNServer, LocationAdapter.ViewHolder>(diff) {

    companion object {
        private val diff = object : DiffUtil.ItemCallback<VPNServer>() {
            override fun areItemsTheSame(oldItem: VPNServer, newItem: VPNServer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VPNServer, newItem: VPNServer): Boolean {
                return oldItem == newItem
            }

        }
    }

    private var _idSelected: String? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, true)
        }

    fun setCurrentServer(server: VPNServer?) {
        _idSelected = server?.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewBinding = parent.inflate(ItemLocationBinding::inflate)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        val server = getItem(position) ?: return
        holder.bindData(server)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val server = getItem(position) ?: return
        holder.bindData(server)
    }

    inner class ViewHolder(private val viewBinding: ItemLocationBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bindData(server: VPNServer) {
            viewBinding.root.isActivated = server.id == _idSelected
            viewBinding.imgCheck.isVisible = server.id == _idSelected
            val flagIcon = Util.getResId(server.countryCode) ?: 0
            viewBinding.imgFlag.setImageResource(flagIcon)
            viewBinding.imgVip.isVisible = server.premium == true
            viewBinding.tvCountry.text = server.country
            viewBinding.tvState.apply {
                text = server.state
                isVisible = server.state?.isNotEmpty() == true
            }

            viewBinding.root.setOnClickListener {
                onItemClicked.invoke(server)
            }
        }

    }
}