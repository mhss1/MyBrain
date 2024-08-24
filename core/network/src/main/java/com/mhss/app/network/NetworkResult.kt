package com.mhss.app.network


sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data object InvalidKey : UserError
    data object InternetError : UserError
    data class OtherError(val message: String? = null): Failure

    sealed interface Failure: NetworkResult<Nothing>
    sealed interface UserError: Failure
}
