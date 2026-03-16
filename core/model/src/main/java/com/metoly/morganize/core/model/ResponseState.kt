package com.metoly.morganize.core.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A sealed interface representing the different states of a UI operation.
 *
 * @param T The type of data returned on success.
 */
sealed interface ResponseState<out T> {

    /** Represents the initial idle state before any operation has started. */
    data object Idle : ResponseState<Nothing>

    /** Represents the state when an operation is in progress. */
    data object Loading : ResponseState<Nothing>

    /** Represents a successful operation with the resulting [data]. */
    data class Success<T>(val data: T) : ResponseState<T>

    /** Represents a failed operation with an error [message]. */
    data class Error(val message: String) : ResponseState<Nothing>

    // region — State flags

    val isIdle: Boolean get() = this is Idle

    val isLoading: Boolean get() = this is Loading

    val isSuccess: Boolean get() = this is Success

    val isError: Boolean get() = this is Error

    // endregion

    // region — Data accessors

    /**
     * Returns the encapsulated data if this is [Success], null otherwise.
     */
    val content: T? get() = (this as? Success)?.data

    /**
     * Returns the error message if this is [Error], null otherwise.
     */
    val error: String? get() = (this as? Error)?.message

    // endregion
}

// region — Flow extensions

fun <T> Flow<T>.asResponseState(): Flow<ResponseState<T>> =
    this
        .map<T, ResponseState<T>> { ResponseState.Success(it) }
        .onStart { emit(ResponseState.Loading) }
        .catch { throwable -> emit(ResponseState.Error(throwable.message ?: "Bilinmeyen hata")) }

fun <T> suspendAsResponseStateFlow(block: suspend () -> T): Flow<ResponseState<T>> = flow {
    emit(ResponseState.Loading)
    try {
        emit(ResponseState.Success(block()))
    } catch (e: Exception) {
        emit(ResponseState.Error(e.message ?: "Bilinmeyen hata"))
    }
}

// endregion