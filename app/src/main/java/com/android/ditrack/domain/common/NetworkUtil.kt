package com.android.ditrack.domain.common

interface NetworkError

enum class NetworkErrorType : NetworkError {
    REQUEST_TIMEOUT,
    UNAUTHORIZED,
    CONFLICT,
    NO_INTERNET,
    PAYLOAD_TOO_LARGE,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN,
    NO_ROUTE_FOUND;
}

sealed interface Result<out D, out E: NetworkError> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: NetworkError>(val error: E): Result<Nothing, E>
}

inline fun <T, E: NetworkError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E: NetworkError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}