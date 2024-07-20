package com.mhss.app.domain.model

sealed interface NetworkResult
sealed interface NetworkError

data class Success<T>(val data: T) : NetworkResult
data object InvalidToken : NetworkResult, NetworkError
data object InternetError : NetworkResult, NetworkError
data object UnexpectedError : NetworkResult, NetworkError
