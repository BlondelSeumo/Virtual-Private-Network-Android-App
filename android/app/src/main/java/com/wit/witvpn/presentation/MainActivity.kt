package com.wit.witvpn.presentation

import android.os.Bundle
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.wit.witvpn.core.base.BaseActivity
import com.wit.witvpn.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onActivityCreated(savedInstanceState: Bundle?) {

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(
                listOf(
                    "6F8A2D4A6BB0DC76EB8D7C3AFEAAF5E8"
                )
            ).build()
        )
    }

    override val isFlagLight: Boolean
        get() = false

}