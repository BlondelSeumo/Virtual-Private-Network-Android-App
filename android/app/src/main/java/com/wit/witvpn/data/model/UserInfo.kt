package com.wit.witvpn.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Created by Furuichi on 25/09/2023
 */
data class UserInfo(
    val email: String,
    val premium: Premium?
) {
    data class Premium(
        val orderId: String?,
        val packageName: String?,
        val productId: String?,
        val purchaseTime: Long?,
        val purchaseToken: String?,
    )

    companion object {
        fun fromFirebase(snapshot: DocumentSnapshot): UserInfo {
            var premium: Premium? = null
            val mapper = snapshot.get("premium") as? Map<*, *>
            mapper?.also {
                premium = Premium(
                    orderId = it["orderId"] as? String,
                    packageName = it["packageName"] as? String,
                    productId = it["productId"] as? String,
                    purchaseTime = (it["purchaseTime"] as? Double)?.toLong(),
                    purchaseToken = it["purchaseToken"] as? String,
                )
            }
            return UserInfo(
                email = snapshot.getString("email") ?: "",
                premium = premium
            )
        }
    }
}