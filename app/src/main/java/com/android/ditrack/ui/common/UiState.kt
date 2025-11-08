package com.android.ditrack.ui.common

import com.android.ditrack.domain.common.NetworkError

sealed class UiState<out T> {
    data object Empty : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: NetworkError) : UiState<Nothing>()
}