package com.wit.witvpn.data.model

import com.google.firebase.firestore.QueryDocumentSnapshot

/**
 * Created by Furuichi on 25/09/2023
 */
data class VPNServer(
    val id: String,
    val country: String?,
    val ovpn: String?,
    val ipAddress: String?,
    val premium: Boolean?,
    val recommend: Boolean?,
    val state: String?,
    val countryCode: String?
) {
    companion object {
        fun fromFirebase(snapshot: QueryDocumentSnapshot): VPNServer {
            return VPNServer(
                id = snapshot.id,
                country = snapshot.data["country"] as? String,
                ovpn = snapshot.data["ovpn"] as? String,
                ipAddress = snapshot.data["ipAddress"] as? String,
                premium = snapshot.data["premium"] as? Boolean,
                recommend = snapshot.data["recommend"] as? Boolean,
                state = snapshot.data["state"] as? String,
                countryCode = snapshot.data["countryCode"] as? String
            )
        }
    }
}