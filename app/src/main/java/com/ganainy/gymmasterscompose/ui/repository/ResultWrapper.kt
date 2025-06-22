package com.ganainy.gymmasterscompose.ui.repository

/**
 * A sealed class that represents the result of asynchronous operations.
 * 
 * This wrapper provides a type-safe way to handle success, loading, and error states
 * for operations that may fail or take time to complete. It's used throughout the
 * application to standardize error handling and loading states.
 * 
 * @param T The type of data that will be returned on success
 * @param isLoading Whether the operation is currently in progress
 */
sealed class ResultWrapper<out T>(open val isLoading: Boolean = false) {
    
    /**
     * Represents a successful operation with the resulting data.
     * 
     * @param data The successful result data
     * @param isLoading Whether the operation is still loading (typically false for success)
     */
    data class Success<T>(val data: T, override val isLoading: Boolean = false) : ResultWrapper<T>(isLoading)
    
    /**
     * Represents an operation that is currently in progress.
     * 
     * @param isLoading Always true for loading state
     */
    data class Loading(override val isLoading: Boolean = true) : ResultWrapper<Nothing>(isLoading)
    
    /**
     * Represents a failed operation with the exception that caused the failure.
     * 
     * @param exception The exception that occurred during the operation
     * @param isLoading Whether the operation is still loading (typically false for error)
     */
    data class Error(val exception: Exception, override val isLoading: Boolean = false) : ResultWrapper<Nothing>(isLoading)
}

/**
 * Extension function to safely extract data from a ResultWrapper or return a default value.
 * 
 * This is useful when you want to provide a fallback value in case of loading or error states.
 * 
 * @param defaultValue The value to return if the ResultWrapper is not a Success
 * @return The data if Success, otherwise the default value
 */
fun <T> ResultWrapper<T>?.orEmpty(defaultValue: T): T {
    return (this as? ResultWrapper.Success<T>)?.data ?: defaultValue
}

/**
 * Extension function to perform an action only when the ResultWrapper is a Success.
 * 
 * This allows for side effects to be performed only on successful results without
 * affecting the original ResultWrapper.
 * 
 * @param action The function to execute with the success data
 * @return The original ResultWrapper unchanged
 */
fun <T> ResultWrapper<T>.onSuccess(action: (T) -> Unit): ResultWrapper<T> {
    if (this is ResultWrapper.Success<T>) action(data)
    return this
}

/**
 * Extension function to perform an action only when the ResultWrapper is an Error.
 * 
 * This allows for error handling side effects to be performed without affecting
 * the original ResultWrapper.
 * 
 * @param action The function to execute with the exception
 * @return The original ResultWrapper unchanged
 */
fun ResultWrapper<*>.onError(action: (Exception) -> Unit): ResultWrapper<*> {
    if (this is ResultWrapper.Error) action(exception)
    return this
}