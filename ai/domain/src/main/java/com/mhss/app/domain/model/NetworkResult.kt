package com.mhss.app.domain.model

sealed interface NetworkResult {
    data class Success<T>(val data: T) : NetworkResult
    data object InvalidKey : NetworkResult, UserError
    data object InternetError : NetworkResult, UserError
    data class OtherError(val message: String? = null) : NetworkResult, NetworkError
}
sealed interface NetworkError
sealed interface UserError: NetworkError