package com.android.ditrack.ui.feature.utils

import android.content.Context
import android.widget.Toast
import com.android.ditrack.R
import com.android.ditrack.domain.common.NetworkError
import com.android.ditrack.domain.common.NetworkErrorType
import com.android.ditrack.ui.common.UiText

fun String.showMessageWithToast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
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