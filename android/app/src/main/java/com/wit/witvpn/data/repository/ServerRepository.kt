package com.wit.witvpn.data.repository

import com.wit.witvpn.data.model.VPNServer
import com.wit.witvpn.data.remote.FirebaseService
import com.wit.witvpn.di.qualifier.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Created by Furuichi on 25/09/2023
 */
interface ServerRepository {
    fun getServers(): Flow<List<VPNServer>>
}

class ServerRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val firebaseService: FirebaseService
) : ServerRepository {

    override fun getServers(): Flow<List<VPNServer>> {
        return flow {
            val servers = firebaseService.getServers()
            emit(servers)
        }.flowOn(ioDispatcher)
    }
}