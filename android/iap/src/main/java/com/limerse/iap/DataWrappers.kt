package com.limerse.iap

class DataWrappers {

    enum class ProductType {
        InApp, Subs
    }

    data class SkuDetails(
        val title: String?,
        val productId: String,
        val type: ProductType?,
        val formattedPrice: String?,
        val priceAmountMicros: Long
    )

    data class PurchaseInfo(
        val sku: String,
        val originalJson: String,
        val isAcknowledged: Boolean,
        val purchaseTime: Long,
    )
}