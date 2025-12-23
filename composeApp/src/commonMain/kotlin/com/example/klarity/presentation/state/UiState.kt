package com.example.klarity.presentation.state

/**
 * Base sealed class for common UI states across all screens.
 * Can be extended for screen-specific states.
 */
sealed interface UiState<out T> {

    data object Idle : UiState<Nothing>
    
    data object Loading : UiState<Nothing>


    data class Success<T>(val data: T) : UiState<T>


    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : UiState<Nothing>


    data object Empty : UiState<Nothing>
}


val UiState<*>.isLoading: Boolean
    get() = this is UiState.Loading

val UiState<*>.isSuccess: Boolean
    get() = this is UiState.Success


val UiState<*>.isError: Boolean
    get() = this is UiState.Error


fun <T> UiState<T>.getOrNull(): T? = when (this) {
    is UiState.Success -> data
    else -> null
}


fun <T> UiState<T>.getOrDefault(default: T): T = when (this) {
    is UiState.Success -> data
    else -> default
}
