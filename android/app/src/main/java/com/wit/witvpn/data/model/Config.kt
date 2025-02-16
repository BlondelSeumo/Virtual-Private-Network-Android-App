package com.wit.witvpn.data.model

import com.google.firebase.firestore.QuerySnapshot

/**
 * Created by Furuichi on 25/09/2023
 */
data class Config(
    val subIds: List<String>,
    val bannerAdUnitId: String?,
    val interstitialAdUnitId: String?
) {
    companion object {
        fun fromSnapshot(querySnapshot: QuerySnapshot): Config {
            val data = querySnapshot.documents.firstOrNull()?.data
            val ads = data?.get("ads") as? MutableList<*>
            val map = ads?.firstOrNull() as MutableMap<*, *>
            val google = map["google"] as MutableMap<*, *>
            val iap = data["iap"] as? MutableMap<*, *>
            val monthly = iap?.get("monthly") as? String ?: "gold_monthly"
            val yearly = iap?.get("yearly") as? String ?: "gold_yearly"
            return Config(
                subIds = listOf(monthly, yearly),
                bannerAdUnitId = google["banner"] as? String,
                interstitialAdUnitId = google["show"] as? String,
            )
        }
    }
}