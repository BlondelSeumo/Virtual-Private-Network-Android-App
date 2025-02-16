package com.wit.witvpn.data.repository

import com.limerse.iap.DataWrappers
import com.wit.witvpn.data.model.UserInfo
import com.wit.witvpn.data.remote.FirebaseService
import com.wit.witvpn.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Created by Furuichi on 25/09/2023
 */
interface UserRepository {

    val userInfoState: StateFlow<UserInfo?>

    fun getCurrentUser(): Flow<Unit>

    fun signIn(email: String, password: String): Flow<UserInfo>

    fun signUp(email: String, password: String): Flow<UserInfo>

    fun resetPassword(email: String): Flow<Unit>

    fun syncUserInfo(): Flow<Unit>

    fun syncTraffic(byteOut: Long, byteIn: Long): Flow<Unit>

    fun syncPurchase(purchase: DataWrappers.PurchaseInfo?): Flow<Unit>

    suspend fun signOut()

}

class UserRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val firebaseService: FirebaseService
) : UserRepository {

    private val _userInfoState = MutableStateFlow<UserInfo?>(null)
    override val userInfoState: StateFlow<UserInfo?>
        get() = _userInfoState

    override fun getCurrentUser(): Flow<Unit> {
        return flow {
            val userInfo = firebaseService.getCurrentUser()
            _userInfoState.emit(userInfo)
            emit(Unit)
        }
    }

    override fun signIn(email: String, password: String): Flow<UserInfo> {
        return flow {
            val userInfo = firebaseService.signIn(email, password)
            _userInfoState.emit(userInfo)
            emit(userInfo)
        }.flowOn(ioDispatcher)
    }

    override fun signUp(email: String, password: String): Flow<UserInfo> {
        return flow {
            firebaseService.signUp(email, password)
            val userInfo = firebaseService.getCurrentUser()
            _userInfoState.emit(userInfo)
            emit(userInfo)
        }.flowOn(ioDispatcher)
    }

    override fun resetPassword(email: String): Flow<Unit> {
        return flow {
            firebaseService.sendPasswordResetEmail(email)
            emit(Unit)
        }.flowOn(ioDispatcher)
    }

    override fun syncUserInfo(): Flow<Unit> {
        return flow {
            firebaseService.syncUserInfoLogin()
            emit(Unit)
        }
    }

    override fun syncTraffic(byteOut: Long, byteIn: Long): Flow<Unit> {
        return flow {
            firebaseService.syncTraffic(byteOut, byteIn)
            emit(Unit)
        }.flowOn(ioDispatcher)
    }

    override fun syncPurchase(purchase: DataWrappers.PurchaseInfo?): Flow<Unit> {
        return flow {
            firebaseService.syncPurchase(originalJson = purchase?.originalJson)
            emit(Unit)
        }.flowOn(ioDispatcher)
    }

    override suspend fun signOut() {
        firebaseService.signOut()
        _userInfoState.emit(null)
    }

}