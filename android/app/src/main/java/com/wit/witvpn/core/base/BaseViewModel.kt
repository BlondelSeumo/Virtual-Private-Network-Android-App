package com.wit.witvpn.core.base

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertificateException
import javax.net.ssl.SSLHandshakeException

/**
 * Created by Furuichi on 25/09/2023
 */
open class BaseViewModel : ViewModel() {
    val indicatorEvent: SharedFlow<IndicatorState>
        get() = indicatorPublisher

    protected val indicatorPublisher = MutableSharedFlow<IndicatorState>(extraBufferCapacity = 1)

    fun <T> Flow<T>.transformLoading(indicatorState: IndicatorState = IndicatorState.Loading, defaultValue: T? = null): Flow<T> {
        return this
            .onStart {
                indicatorPublisher.emit(indicatorState)
            }
            .catch { error ->
                Timber.e(error.localizedMessage)
                defaultValue
                    ?.let { value ->
                        emit(value)
                    }
                    ?: run {
                        indicatorPublisher.emit(IndicatorState.Error(throwable = error))
                    }
            }
            .onCompletion {
                indicatorPublisher.emit(IndicatorState.Idle)
            }
    }

    fun <T> Flow<T>.catchFlow(
        block: suspend ((Throwable) -> Unit) = {
            Timber.e((it))
            indicatorPublisher.emit(IndicatorState.Error(throwable = it))
        }
    ): Flow<T> {
        return this.catch {
            block.invoke(it)
        }
    }
}

sealed class IndicatorState {
    object Idle : IndicatorState()

    object Loading : IndicatorState()

    class Error(
        private val error: String? = null,
        val throwable: Throwable? = null
    ) : IndicatorState() {
        val message: String
            get() {
                error?.let {
                    return it
                }
                return when (throwable) {
                    is UnknownHostException,
                    is ConnectException,
                    is SocketTimeoutException,
                    is SSLHandshakeException,
                    is CertificateException -> {
                        "Please check your network and try again"
                    }

                    is FirebaseException -> {
                        throwable.message ?: "Service unavailable"
                    }

                    else -> {
                        "Service unavailable"
                    }
                }
            }
    }
}