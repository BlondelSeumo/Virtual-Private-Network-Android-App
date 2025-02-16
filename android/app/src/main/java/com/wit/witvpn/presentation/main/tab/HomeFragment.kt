package com.wit.witvpn.presentation.main.tab

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.wit.witvpn.BuildConfig
import com.wit.witvpn.R
import com.wit.witvpn.core.base.BaseFragment
import com.wit.witvpn.core.extension.hide
import com.wit.witvpn.core.extension.mapToPropertyNotNull
import com.wit.witvpn.core.extension.rateApp
import com.wit.witvpn.core.extension.show
import com.wit.witvpn.core.util.DialogUtils
import com.wit.witvpn.core.util.Util
import com.wit.witvpn.data.model.VPNServer
import com.wit.witvpn.databinding.FragmentHomeBinding
import com.wit.witvpn.presentation.main.MainFragment
import com.wit.witvpn.presentation.main.MainViewModel
import com.wit.witvpn.presentation.main.MainViewModel.UIAction
import com.wit.witvpn.presentation.main.MainViewModel.UICommand
import com.wit.witvpn.presentation.main.MainViewModel.UIState
import dagger.hilt.android.AndroidEntryPoint
import de.blinkt.openvpn.DisconnectVPNActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Furuichi on 25/09/2023
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<MainViewModel, FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    companion object {
        private const val ACTION_CONNECTION_STATE = "connectionState"
        private const val EXTRA_STATE = "state"
        private const val EXTRA_DOWNLOAD = "download"
        private const val EXTRA_UPLOAD = "upload"
    }

    enum class State {
        Disconnected, Connecting, Connected
    }

    override val viewModel: MainViewModel by hiltNavGraphViewModels(R.id.nav_main_graph)

    //Progress View
    private var animator: ValueAnimator? = null
    private var originWidth = 0

    //region#VPN
    private var state: State = State.Disconnected
        set(value) {
            field = value
            when (value) {
                State.Disconnected -> bindDisconnected()
                State.Connecting -> bindConnecting()
                State.Connected -> bindConnected()
            }
        }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        uiScope.launch {
            delay(500)
            startVPN()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val safeIntent = intent ?: return
            safeIntent.getStringExtra(EXTRA_STATE)?.also {
                Timber.i("state = $it")
                if (it == "CONNECTED") {
                    state = State.Connected
                }

                if (it == "DISCONNECTED") {
                    state = State.Disconnected
                }
            }
            safeIntent.getStringExtra(EXTRA_UPLOAD)?.also {
                viewBinding.tvUpload.text = it
            }
            safeIntent.getStringExtra(EXTRA_DOWNLOAD)?.also {
                viewBinding.tvDownload.text = it
            }
        }
    }
    //endregion

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        setupViews()
        bindState()
        bindCommand()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, IntentFilter(ACTION_CONNECTION_STATE))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    /**
     * SETUP VIEWS
     */
    private fun setupViews() {

        viewBinding.btnLocation.setOnClickListener {
            viewModel.accept(UIAction.SetCurrentTab(MainFragment.Tab.Location))
        }

        viewBinding.btnConnect.setOnClickListener {
            when (state) {
                State.Disconnected -> {
                    prepareStartVpn()
                }

                State.Connected -> {
                    doSyncTraffic()
                    state = State.Disconnected
                    DisconnectVPNActivity.forceDisconnect(requireContext())
                }

                State.Connecting -> {
                    state = State.Disconnected
                    DisconnectVPNActivity.forceDisconnect(requireContext())
                }
            }
        }
    }

    /**
     * STATE
     */
    private fun bindState() {
        viewBinding.viewProgress.post {
            originWidth = viewBinding.viewProgress.width
        }

        viewModel.uiState
            .mapToPropertyNotNull(UIState::currentServer)
            .onEach(::bindCurrentServer)
            .launchIn(uiScope)
    }

    /**
     * COMMAND
     */
    private fun bindCommand() {
        viewModel.uiCommand
            .filterIsInstance<UICommand.SetupAd>()
            .onEach { initializeMobileAdsSdk() }
            .launchIn(uiScope)

        viewModel.uiCommand
            .filterIsInstance<UICommand.Purchased>()
            .onEach { viewBinding.adContainer.removeAllViews() }
            .launchIn(uiScope)
    }

    private fun prepareStartVpn() {
        VpnService.prepare(context)
            ?.also(startForResult::launch)
            ?: startVPN()

    }

    private fun startVPN() {
        val currentVPNServer = viewModel.uiState.value.currentServer ?: run {
            viewModel.accept(UIAction.SetCurrentTab(MainFragment.Tab.Location))
            return
        }
        val data: ByteArray
        try {
            data = Base64.decode(currentVPNServer.ovpn, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        state = State.Connecting
        OpenVpnApi.startVpn(requireContext(), data, currentVPNServer.country)
    }

    private fun doSyncTraffic() {
        val byteIn = OpenVPNService.getByteIn()
        val byteOut = OpenVPNService.getByteOut()
        viewModel.accept(UIAction.SyncTraffic(byteOut = byteOut, byteIn = byteIn))
    }

    private fun bindDisconnected() {
        animator?.cancel()
        viewBinding.viewProgress.hide()
        viewBinding.tvState.text = getString(R.string.connect_vpn_now)

        viewBinding.lottieState.apply {
            setAnimation(R.raw.ic_logo_connect)
            repeatMode = LottieDrawable.RESTART
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    private fun bindConnecting() {
        viewBinding.viewProgress.show()

        val layoutParam: FrameLayout.LayoutParams = viewBinding.viewProgress.layoutParams as FrameLayout.LayoutParams

        animator = ValueAnimator.ofFloat(0f, 100f)
            .apply {
                duration = 5000
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    var value = (it.animatedValue as Float).toInt()
                    if (value == 100) {
                        value = 99
                    }
                    val process = originWidth.times(value) / 100
                    layoutParam.width = process
                    viewBinding.tvState.text = getString(R.string.connecting, "$value%")
                    viewBinding.viewProgress.layoutParams = layoutParam
                    viewBinding.viewProgress.visibility = View.VISIBLE
                }
            }
        animator?.start()
    }

    private fun bindConnected() {
        animator?.cancel()
        viewBinding.viewProgress.hide()
        viewBinding.tvState.text = getString(R.string.disconnect_vpn)
        showRateAppIfNeeded()
        showInterstitial()

        viewBinding.lottieState.apply {
            setAnimation(R.raw.ic_logo_connected)
            repeatMode = LottieDrawable.RESTART
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    private fun bindCurrentServer(vpnServer: VPNServer) {
        viewBinding.tvLocation.text = vpnServer.country
        viewBinding.imgFlag.apply {
            val flagIcon = Util.getResId(vpnServer.countryCode) ?: R.drawable.ic_location
            setImageResource(flagIcon)
        }
    }

    private fun showRateAppIfNeeded() {
        if (viewModel.uiState.value.isRefuseRateApp) {
            return
        }
        DialogUtils.showRateApp(
            context = requireContext(),
            onRefuse = {
                viewModel.accept(UIAction.Refuse)
                it.dismiss()
            },
            onRate = {
                context?.rateApp(BuildConfig.APPLICATION_ID)
                it.dismiss()
            }
        )
    }

    //region#Ads
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.get()) {
            return
        }
        isMobileAdsInitializeCalled.set(true)

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(requireContext()) {
            loadInterstitialAd()
        }

        viewBinding.adContainer.apply {
            removeAllViews()
            val bannerId = viewModel.uiState.value.config?.bannerAdUnitId ?: return
            val adRequest = AdRequest.Builder().build()
            val adView = AdView(context)
                .apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = bannerId
                    // Request an ad.
                    loadAd(adRequest)
                }
            addView(adView)
            show()
        }
    }

    private fun loadInterstitialAd() {
        val interstitialAdUnitId = viewModel.uiState.value.config?.interstitialAdUnitId ?: return

        // Request a new ad if one isn't already loaded.
        if (adIsLoading || interstitialAd != null) {
            return
        }
        adIsLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    val error = "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Timber.d("#onAdFailedToLoad $error")
                    interstitialAd = null
                    adIsLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    adIsLoading = false
                }
            }

        )
    }

    private fun showInterstitial() {
        if (viewModel.uiState.value.purchaseInfo != null) {
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        interstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                    }
                }
            interstitialAd?.show(requireActivity())
        } else {
            loadInterstitialAd()
        }
    }
    //endregion
}