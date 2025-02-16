package com.wit.witvpn.core.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding

fun ViewGroup.inflate(@LayoutRes resId: Int): View {
    return LayoutInflater.from(context).inflate(resId, this, false)
}

fun <VB : ViewBinding> ViewGroup.inflate(_binding: (LayoutInflater, ViewGroup?, Boolean) -> VB): VB {
    return _binding.invoke(LayoutInflater.from(context), this, false)
}

fun <VB : ViewBinding> ViewGroup.customViewInflate(_binding: (LayoutInflater, ViewGroup?, Boolean) -> VB): VB {
    return _binding.invoke(LayoutInflater.from(context), this, true)
}

fun <VB : ViewBinding> ViewGroup.customViewInflate(_binding: (LayoutInflater, ViewGroup) -> VB): VB {
    return _binding.invoke(LayoutInflater.from(context), this)
}