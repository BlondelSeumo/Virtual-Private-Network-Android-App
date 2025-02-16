package com.wit.witvpn.data.remote

import android.content.Context
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wit.witvpn.core.extension.toStringWithPattern
import com.wit.witvpn.core.util.AppConstant
import com.wit.witvpn.core.util.Util
import com.wit.witvpn.data.model.Config
import com.wit.witvpn.data.model.VPNServer
import com.wit.witvpn.data.model.UserInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * Created by Furuichi on 25/09/2023
 */
class FirebaseService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FirebaseService"
    }

    suspend fun getConfig(): Config {
        val querySnapshot = Firebase.firestore
            .collection(AppConstant.Firebase.CONFIGS)
            .get()
            .await()
        return Config.fromSnapshot(querySnapshot)
    }

    suspend fun getServers(): List<VPNServer> {
        val querySnapshot = Firebase.firestore
            .collection(AppConstant.Firebase.SERVERS)
            .whereEqualTo("status", true)
            .orderBy("premium", Query.Direction.ASCENDING)
            .get()
            .await()
        val vpnServers = mutableListOf<VPNServer>()
        querySnapshot.mapTo(vpnServers, VPNServer::fromFirebase)
        return vpnServers
    }

    suspend fun signIn(email: String, password: String): UserInfo {
        val authResult = Firebase.auth.signInWithEmailAndPassword(email, password).await()
        val _email = authResult.user?.email
            ?: throw FirebaseException("Error<#signIn>..`$email` not found")
        val documentSnapshot = Firebase.firestore
            .collection(AppConstant.Firebase.USERS)
            .document(_email)
            .get()
            .await()
        return UserInfo.fromFirebase(documentSnapshot)
    }

    suspend fun getCurrentUser(): UserInfo {
        val email = Firebase.auth.currentUser?.email ?: throw FirebaseException("Error<#getCurrentUser>...Current User is null")
        val documentSnapshot = Firebase.firestore
            .collection(AppConstant.Firebase.USERS)
            .document(email)
            .get()
            .await()
        return UserInfo.fromFirebase(documentSnapshot)
    }

    suspend fun signUp(email: String, password: String) {
        Timber.tag(TAG).i("do #signUp: email = $email, password = $password")
        val user = Firebase.auth.createUserWithEmailAndPassword(email, password)
            .await()
            .user ?: throw FirebaseException("Error<#signUp>...`user` is null")

        Timber.tag(TAG).i("do #sendEmailVerification")
        user.sendEmailVerification().await()

        val data = mapOf(
            "email" to email,
            "createAt" to Calendar.getInstance().toStringWithPattern(),
            "deviceInfo" to Util.getDeviceInfo(context)
        )

        Timber.tag(TAG).i("do #setData $data")
        Firebase.firestore
            .collection(AppConstant.Firebase.USERS)
            .document(email)
            .set(data)
            .await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        Firebase.auth.sendPasswordResetEmail(email).await()
    }

    suspend fun updatePassword(password: String) {
        Firebase.auth.currentUser?.updatePassword(password)?.await()
            ?: throw FirebaseException("Error<#updatePassword>...")
    }

    fun signOut() {
        Firebase.auth.currentUser?.let {
            Firebase.auth.signOut()
        }
    }

    suspend fun syncTraffic(byteOut: Long, byteIn: Long) {
        val email = Firebase.auth.currentUser?.email ?: run {
            return
        }
        syncTrafficInternal(AppConstant.Firebase.USERS, email, byteOut, byteIn)

    }

    suspend fun syncPurchase(originalJson: String?) {
        val email = Firebase.auth.currentUser?.email ?: run {
            return
        }

        val data = originalJson
            ?.let {
                val retMap: Map<String, Any> = Gson().fromJson(
                    originalJson, object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                return@let mapOf("premium" to retMap)
            }
            ?: run {
                mapOf("premium" to FieldValue.delete())
            }

        Firebase.firestore
            .collection(AppConstant.Firebase.USERS)
            .document(email)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun syncUserInfoLogin() {
        val email = Firebase.auth.currentUser?.email ?: run {
            return
        }
        val data = hashMapOf(
            "lastLogin" to Calendar.getInstance().toStringWithPattern()
        )
        Firebase.firestore
            .collection(AppConstant.Firebase.USERS)
            .document(email)
            .set(data, SetOptions.merge())
            .await()
    }

    //region#Private Function
    private suspend fun syncTrafficInternal(collection: String, documentId: String, byteOut: Long, byteIn: Long) {
        val snapshot = Firebase.firestore
            .collection(collection)
            .document(documentId)
            .get()
            .await()

        val trafficServer = snapshot.get("traffic") as? Map<*, *>
        val uploadServer = trafficServer?.get("upload") as? Long ?: 0L
        val downloadServer = trafficServer?.get("download") as? Long ?: 0L

        val upload = uploadServer.plus(byteOut)
        val download = downloadServer.plus(byteIn)

        val data = mapOf(
            "upload" to upload,
            "download" to download,
            "updateAt" to Calendar.getInstance().toStringWithPattern()
        )

        val traffic = mapOf(
            "traffic" to data
        )

        Firebase.firestore
            .collection(collection)
            .document(documentId)
            .set(traffic, SetOptions.merge())
            .await()
    }
    //endregion
}