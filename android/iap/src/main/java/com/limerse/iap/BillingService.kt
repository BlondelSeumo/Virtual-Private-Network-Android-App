package com.limerse.iap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class BillingService(
    private val context: Context,
    private val consumableKeys: List<String>,
    private val subscriptionSkuKeys: List<String>
) : IBillingService(),
    PurchasesUpdatedListener,
    BillingClientStateListener,
    AcknowledgePurchaseResponseListener,
    CoroutineScope {

    private lateinit var mBillingClient: BillingClient
    private var decodedKey: String? = null

    private var enableDebug: Boolean = false

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    override fun init(key: String?) {
        decodedKey = key

        mBillingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        mBillingClient.startConnection(this)
        log("startConnection")
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        log("onBillingSetupFinishedOkay: billingResult: $billingResult")

        if (billingResult.isOk()) {
            consumableKeys.queryProductDetails(ProductType.INAPP) {
                subscriptionSkuKeys.queryProductDetails(ProductType.SUBS) {
                    launch {
                        queryPurchases()
                    }
                }
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     * New purchases will be provided to the PurchasesUpdatedListener.
     */
    private suspend fun queryPurchases() {
        if (consumableKeys.size > 0) {
            val inAppResult: PurchasesResult = mBillingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(ProductType.INAPP)
                    .build()
            )
            processPurchases(inAppResult.purchasesList, isRestore = true)
        }

        if (subscriptionSkuKeys.size > 0) {
            val subsResult: PurchasesResult = mBillingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(ProductType.SUBS)
                    .build()

            )
            processPurchases(subsResult.purchasesList, isRestore = true)
        }
    }

    override fun buy(activity: Activity, sku: String) {
        if (!sku.isSkuReady()) {
            log("buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, ProductType.INAPP)
    }

    override fun subscribe(activity: Activity, sku: String) {
        if (!sku.isSkuReady()) {
            log("buy. Google billing service is not ready yet.")
            return
        }

        launchBillingFlow(activity, sku, ProductType.SUBS)
    }

    private fun launchBillingFlow(activity: Activity, sku: String, type: String) {
        sku.toProductDetails(type) { product ->
            if (product != null) {
                val offsetToken = product.subscriptionOfferDetails?.first()?.offerToken ?: ""
                val params = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offsetToken)
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(params)
                    .build()

                mBillingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    override fun unsubscribe(activity: Activity, sku: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val subscriptionUrl = ("http://play.google.com/store/account/subscriptions"
                    + "?package=" + activity.packageName
                    + "&sku=" + sku)
            intent.data = Uri.parse(subscriptionUrl)
            activity.startActivity(intent)
            activity.finish()
        } catch (e: Exception) {
            Log.w(TAG, "Unsubscribing failed.")
        }
    }

    override fun enableDebugLogging(enable: Boolean) {
        this.enableDebug = enable
    }

    /**
     * Called by the Billing Library when new purchases are detected.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        log("onPurchasesUpdated: responseCode:$responseCode debugMessage: $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                log("onPurchasesUpdated. purchase: $purchases")
                processPurchases(purchases)
            }

            BillingClient.BillingResponseCode.USER_CANCELED ->
                log("onPurchasesUpdated: User canceled the purchase")

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                log("onPurchasesUpdated: The user already owns this item")
                //item already owned? call queryPurchases to verify and process all such items
                launch {
                    queryPurchases()
                }
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                Log.e(
                    TAG, "onPurchasesUpdated: Developer error means that Google Play " +
                            "does not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The SKU product ID must match and the APK you " +
                            "are using must be signed with release keys."
                )
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>?, isRestore: Boolean = false) {
        if (purchasesList.isNullOrEmpty()) {
            log("processPurchases: with no purchases")
            subscriptionNoPurchased()
            return
        }
        log("processPurchases: " + purchasesList.size + " purchase(s)")
        purchases@ for (purchase in purchasesList) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.products[0].isSkuReady()) {
                if (!isSignatureValid(purchase)) {
                    log("processPurchases. Signature is not valid for: $purchase")
                    continue@purchases
                }
                // Acknowledge the purchase if it hasn't already been acknowledged.
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                }

                // Grant entitlement to the user.
                val productDetail = productDetailsMap[purchase.products[0]]
                when (productDetail?.productType) {
                    ProductType.INAPP -> {
                        /**
                         * Consume the purchase
                         */
                        if (consumableKeys.contains(purchase.products[0])) {
                            mBillingClient.consumeAsync(
                                ConsumeParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken).build()
                            ) { billingResult, _ ->
                                when (billingResult.responseCode) {
                                    BillingClient.BillingResponseCode.OK -> {
                                        productOwned(getPurchaseInfo(purchase), false)
                                    }

                                    else -> {
                                        Log.d(
                                            TAG,
                                            "Handling consumables : Error during consumption attempt -> ${billingResult.debugMessage}"
                                        )
                                    }
                                }
                            }
                        } else {
                            productOwned(getPurchaseInfo(purchase), isRestore)
                        }
                    }

                    ProductType.SUBS -> {
                        subscriptionOwned(getPurchaseInfo(purchase), isRestore)
                    }
                }
            } else {
                Log.e(
                    TAG, "processPurchases failed. purchase: $purchase " +
                            "purchaseState: ${purchase.purchaseState} isSkuReady: ${purchase.products[0].isSkuReady()}"
                )
            }
        }
    }

    private fun getPurchaseInfo(purchase: Purchase): DataWrappers.PurchaseInfo {
        val productId = purchase.products[0]
        return DataWrappers.PurchaseInfo(
            productId,
            purchase.originalJson,
            purchase.isAcknowledged,
            purchase.purchaseTime
        )
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    /**
     * Update Sku details after initialization.
     * This method has cache functionality.
     */
    private fun List<String>.queryProductDetails(type: String, done: () -> Unit) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady || this.isEmpty()) {
            log("querySkuDetails. Google billing service is not ready yet.")
            done()
            return
        }

        val productList = this.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        mBillingClient.queryProductDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.isOk()) {
                skuDetailsList.forEach { productDetails ->
                    productDetailsMap[productDetails.productId] = productDetails
                }

                productDetailsMap.mapNotNull { entry ->
                    entry.value.let { productDetails ->
                        val productType = if (productDetails.productType == ProductType.INAPP)
                            DataWrappers.ProductType.InApp
                        else
                            DataWrappers.ProductType.Subs

                        entry.key to DataWrappers.SkuDetails(
                            title = productDetails.title.split("(").firstOrNull(),
                            productId = productDetails.productId,
                            type = productType,
                            formattedPrice = productDetails.getFormattedPrice(),
                            priceAmountMicros = productDetails.getPriceAmountMicros() ?: 0
                        )
                    }
                }.let {
                    updatePrices(iapKeyPrices = it.toMap(), type = type)
                }
            }
            done()
        }
    }

    /**
     * Get Sku details by sku and type.
     * This method has cache functionality.
     */
    private fun String.toProductDetails(type: String, done: (product: ProductDetails?) -> Unit = {}) {
        if (::mBillingClient.isInitialized.not() || !mBillingClient.isReady) {
            log("buy. Google billing service is not ready yet.")
            done(null)
            return
        }

        val skuDetailsCached = productDetailsMap[this]
        if (skuDetailsCached != null) {
            done(skuDetailsCached)
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(this)
                .setProductType(type)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()


        mBillingClient.queryProductDetailsAsync(params) { _, skuDetailsList ->
            skuDetailsList.find { it.productId == this }?.let { productDetails ->
                productDetailsMap[this] = productDetails
                done(productDetails)
            } ?: run {
                log("launchBillingFlow. Failed to get details for sku: $this")
                done(null)
            }
        }
    }

    private fun String.isSkuReady(): Boolean {
        return productDetailsMap.containsKey(this) && productDetailsMap[this] != null
    }

    override fun onBillingServiceDisconnected() {
        log("onBillingServiceDisconnected")
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: billingResult: $billingResult")
        launch {
            queryPurchases()
        }
    }

    override fun close() {
        mBillingClient.endConnection()
        super.close()
    }

    private fun BillingResult.isOk(): Boolean {
        return this.responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun log(message: String) {
        if (enableDebug) {
            Log.d(TAG, message)
        }
    }

    companion object {
        const val TAG = "GoogleBillingService"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
}

private fun ProductDetails.getFormattedPrice(): String? {
    if (this.productType == ProductType.INAPP) {
        return this.oneTimePurchaseOfferDetails?.formattedPrice
    }
    return this.subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.formattedPrice
}

private fun ProductDetails.getPriceAmountMicros(): Long? {
    if (this.productType == ProductType.INAPP) {
        return this.oneTimePurchaseOfferDetails?.priceAmountMicros
    }
    return this.subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.priceAmountMicros
}