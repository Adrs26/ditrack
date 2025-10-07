package com.android.ditrack.ui.feature.utils

import com.android.ditrack.R
import com.android.ditrack.ui.common.UiText

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

fun NetworkError.toMessageError(): UiText {
    return when(this) {
        NetworkErrorType.REQUEST_TIMEOUT -> UiText.StringResource(R.string.request_timeout)
        NetworkErrorType.UNAUTHORIZED -> UiText.StringResource(R.string.unauthorized)
        NetworkErrorType.CONFLICT -> UiText.StringResource(R.string.conflict)
        NetworkErrorType.NO_INTERNET -> UiText.StringResource(R.string.no_internet)
        NetworkErrorType.PAYLOAD_TOO_LARGE -> UiText.StringResource(R.string.payload_too_large)
        NetworkErrorType.SERVER_ERROR -> UiText.StringResource(R.string.server_error)
        NetworkErrorType.SERIALIZATION -> UiText.StringResource(R.string.serialization_error)
        NetworkErrorType.NO_ROUTE_FOUND -> UiText.StringResource(R.string.no_route_found)
        else -> UiText.StringResource(R.string.unknown_error)
    }
}