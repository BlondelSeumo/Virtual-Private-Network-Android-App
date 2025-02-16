package com.wit.witvpn.presentation.main

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.limerse.iap.DataWrappers
import com.limerse.iap.IapConnector
import com.limerse.iap.SubscriptionServiceListener
import com.wit.witvpn.BuildConfig
import com.wit.witvpn.core.base.BaseViewModel
import com.wit.witvpn.core.base.IndicatorState
import com.wit.witvpn.core.extension.catchReturn
import com.wit.witvpn.core.util.AppConstant
import com.wit.witvpn.data.model.Config
import com.wit.witvpn.data.model.UserInfo
import com.wit.witvpn.data.model.VPNServer
import com.wit.witvpn.data.preferences.DataStoreManager
import com.wit.witvpn.data.repository.CommonRepository
import com.wit.witvpn.data.repository.ServerRepository
import com.wit.witvpn.data.repository.UserRepository
import com.wit.witvpn.presentation.main.MainFragment.Tab
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by Furuichi on 25/09/2023
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val userRepos: UserRepository,
    private val commonRepos: CommonRepository,
    private val serverRepos: ServerRepository,
    private val iapConnector: IapConnector,
    private val storeManager: DataStoreManager
) : BaseViewModel() {
    /**
     * Stream of immutable states representative of the UI.
     */
    val uiState: StateFlow<UIState>
        get() = _uiStateEvent

    val uiCommand: SharedFlow<UICommand>
        get() = _uiCommandEvent


    private val _actionEvent = MutableSharedFlow<UIAction>()
    private val _uiCommandEvent = MutableSharedFlow<UICommand>()
    private val _uiStateEvent = MutableStateFlow(value = UIState())

    private var weakContext = WeakReference(context)

    private val subscriptionServiceListener = object : SubscriptionServiceListener {

        override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.SkuDetails>) {
            Timber.d("onPricesUpdated: ${iapKeyPrices.values}")
            _uiStateEvent.update {
                it.copy(skuDetailsList = iapKeyPrices.values.toList())
            }
        }

        override fun onSubscriptionPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
            Timber.d("onSubscriptionPurchased: $purchaseInfo")
            _uiStateEvent.update {
                it.copy(purchaseInfo = purchaseInfo)
            }
            send(UICommand.Purchased)
            accept(UIAction.SyncPurchased(purchaseInfo = purchaseInfo))
        }

        override fun onSubscriptionRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
            Timber.d("onSubscriptionRestored: $purchaseInfo")
            _uiStateEvent.update {
                it.copy(purchaseInfo = purchaseInfo)
            }
        }

        override fun onNoPurchased() {
            Timber.d("onNoPurchased")
            send(UICommand.SetupAd)
            accept(UIAction.SyncPurchased(purchaseInfo = null))
        }
    }

    init {
        Timber.d("#init $this")
        observeUserInfo()
        observeConfig()
        observeRefuse()

        _actionEvent
            .map(this::transform)
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        Timber.d("#onCleared $this")
        super.onCleared()
    }

    /**
     * Processor of side effects from the UI which in turn feedback into [accept]
     */
    fun accept(action: UIAction) = viewModelScope.launch {
        _actionEvent.emit(action)
    }

    fun send(commmand: UICommand) = viewModelScope.launch {
        _uiCommandEvent.emit(commmand)
    }

    fun subscribe(activity: FragmentActivity, skuDetails: DataWrappers.SkuDetails) {
        iapConnector.subscribe(activity, skuDetails.productId)
    }

    private fun observeUserInfo() {
        userRepos.userInfoState
            .onEach { userInfo ->
                _uiStateEvent.update { it.copy(userInfo = userInfo) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeConfig() {
        commonRepos.configState
            .onEach { config ->
                _uiStateEvent.update { it.copy(config = config) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeRefuse() {
        storeManager.getFlow(AppConstant.Prefs.isRefuseRateApp, false)
            .onEach { isRefuseRateApp ->
                _uiStateEvent.update {
                    it.copy(isRefuseRateApp = isRefuseRateApp)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun transform(action: UIAction) {
        when (action) {
            is UIAction.FetchData -> handleFetchData()
            is UIAction.SignOut -> handleSigOut()
            is UIAction.Refuse -> handleRefuse()
            is UIAction.SetCurrentTab -> handleSetCurrentTab(action)
            is UIAction.SubscriptionPricesUpdated -> handleSubscriptionPricesUpdate(action)
            is UIAction.SetCurrentVPNServer -> handleSetCurrentVPNServer(action)
            is UIAction.SyncPurchased -> handleSyncPurchased(action)
            is UIAction.SyncTraffic -> handleSyncTraffic(action)
        }
    }

    private fun handleSyncPurchased(action: UIAction.SyncPurchased) {
        userRepos.syncPurchase(action.purchaseInfo)
            .transformLoading(IndicatorState.Idle)
            .launchIn(viewModelScope)
    }

    private fun handleSyncTraffic(action: UIAction.SyncTraffic) {
        userRepos.syncTraffic(action.byteOut, action.byteIn)
            .transformLoading(IndicatorState.Idle)
            .launchIn(viewModelScope)
    }

    private fun handleFetchData() {
        merge(
            userRepos.getCurrentUser()
                .catchReturn(Unit),
            commonRepos.getConfigs()
                .onEach(::bindConfig)
                .catchReturn(Unit),
            serverRepos.getServers()
                .onEach { servers ->
                    val vpnServerId = storeManager.get(AppConstant.Prefs.vpnServiceId, "")
                    Timber.d("#getServers: ${servers.size}, vpnServerId = $vpnServerId")
                    val currentServer = servers.firstOrNull { it.id == vpnServerId }
                    _uiStateEvent.update {
                        it.copy(
                            servers = servers,
                            currentServer = currentServer
                        )
                    }
                }
                .catchReturn(Unit)
        )
            .transformLoading(IndicatorState.Idle)
            .launchIn(viewModelScope)
    }

    private fun handleSigOut() {
        runBlocking { userRepos.signOut() }
        _uiStateEvent.update { it.copy(tab = Tab.Home) }
    }

    private fun handleRefuse() {
        viewModelScope.launch {
            storeManager.set(AppConstant.Prefs.isRefuseRateApp, true)
        }
    }

    private fun handleSetCurrentTab(action: UIAction.SetCurrentTab) {
        _uiStateEvent.update {
            it.copy(tab = action.tab)
        }
    }

    private fun handleSubscriptionPricesUpdate(action: UIAction.SubscriptionPricesUpdated) {
        _uiStateEvent.update {
            it.copy(skuDetailsList = action.iapKeyPrices.values.toList())
        }
    }

    private fun handleSetCurrentVPNServer(action: UIAction.SetCurrentVPNServer) = viewModelScope.launch {
        if (uiState.value.purchaseInfo == null && action.vpnServer.premium == true) {
            _uiStateEvent.update { it.copy(tab = Tab.Upgrade) }
            return@launch
        }
        storeManager.set(AppConstant.Prefs.vpnServiceId, action.vpnServer.id)
        _uiStateEvent.update { it.copy(currentServer = action.vpnServer, tab = Tab.Home) }
    }

    private fun bindConfig(config: Config) {
        Timber.d("bindConfig #called")

        val safeContext = weakContext.get() ?: return
        iapConnector.setup(
            safeContext,
            subscriptionKeys = config.subIds,
            enableLogging = BuildConfig.DEBUG
        )
        iapConnector.addSubscriptionListener(subscriptionServiceListener)
    }

    sealed class UIAction {
        object FetchData : UIAction()
        object SignOut : UIAction()
        object Refuse : UIAction()
        data class SyncPurchased(val purchaseInfo: DataWrappers.PurchaseInfo?) : UIAction()
        data class SyncTraffic(val byteOut: Long, val byteIn: Long) : UIAction()
        data class SetCurrentTab(val tab: Tab) : UIAction()
        data class SetCurrentVPNServer(val vpnServer: VPNServer) : UIAction()
        data class SubscriptionPricesUpdated(val iapKeyPrices: Map<String, DataWrappers.SkuDetails>) : UIAction()
    }

    data class UIState(
        val tab: Tab = Tab.Home,
        val userInfo: UserInfo? = null,
        val config: Config? = null,
        val servers: List<VPNServer> = listOf(),
        val currentServer: VPNServer? = null,
        val skuDetailsList: List<DataWrappers.SkuDetails> = listOf(),
        val purchaseInfo: DataWrappers.PurchaseInfo? = null,
        val isRefuseRateApp: Boolean = false,
    )

    sealed class UICommand {
        object SetupAd : UICommand()

        object Purchased : UICommand()
    }
}