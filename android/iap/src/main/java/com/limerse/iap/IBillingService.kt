package com.limerse.iap

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.BillingClient.ProductType.SUBS

abstract class IBillingService {

    private val purchaseServiceListeners: MutableList<PurchaseServiceListener> = mutableListOf()
    private val subscriptionServiceListeners: MutableList<SubscriptionServiceListener> =
        mutableListOf()

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener)
    }

    /**
     * @param purchaseInfo       - product specifier
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored product
     */
    fun productOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            productOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun productOwnedInternal(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        for (purchaseServiceListener in purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(purchaseInfo)
            } else {
                purchaseServiceListener.onProductPurchased(purchaseInfo)
            }
        }
    }

    /**
     * @param purchaseInfo       - subscription specifier
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored subscription
     */
    fun subscriptionOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun subscriptionOwnedInternal(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(purchaseInfo)
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(purchaseInfo)
            }
        }
    }

    fun subscriptionNoPurchased() {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            subscriptionServiceListener.onNoPurchased()
        }
    }

    /**
     * V6
     */
    fun updatePrices(iapKeyPrices: Map<String, DataWrappers.SkuDetails>, type: String) {
        findUiHandler().post {
            updatePricesInternal(iapKeyPrices, type)
        }
    }

    /**
     * V6
     */
    private fun updatePricesInternal(iapKeyPrices: Map<String, DataWrappers.SkuDetails>, type: String) {
        if (type == INAPP) {
            for (billingServiceListener in purchaseServiceListeners) {
                billingServiceListener.onPricesUpdated(iapKeyPrices)
            }
        }

        if (type == SUBS) {
            for (billingServiceListener in subscriptionServiceListeners) {
                billingServiceListener.onPricesUpdated(iapKeyPrices)
            }
        }
    }

    abstract fun init(key: String?)
    abstract fun buy(activity: Activity, sku: String)
    abstract fun subscribe(activity: Activity, sku: String)
    abstract fun unsubscribe(activity: Activity, sku: String)
    abstract fun enableDebugLogging(enable: Boolean)

    @CallSuper
    open fun close() {
        subscriptionServiceListeners.clear()
        purchaseServiceListeners.clear()
    }
}

fun findUiHandler(): Handler {
    return Handler(Looper.getMainLooper())
}