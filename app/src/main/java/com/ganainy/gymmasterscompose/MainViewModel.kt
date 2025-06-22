package com.ganainy.gymmasterscompose

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.prefs.ExerciseDownloadPrefs
import com.ganainy.gymmasterscompose.ui.repository.AuthRepository
import com.ganainy.gymmasterscompose.ui.repository.IExerciseRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel responsible for managing the application's startup state and initialization.
 * 
 * This ViewModel handles:
 * - User authentication state management
 * - Initial data synchronization (likes, exercises)
 * - App startup configuration
 * 
 * @property authRepository Repository for authentication operations
 * @property likeRepository Repository for like/unlike operations
 * @property exerciseDownloadPrefs Preferences for tracking exercise download status
 * @property exerciseRepository Repository for exercise data operations
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val likeRepository: ILikeRepository,
    private val exerciseDownloadPrefs: ExerciseDownloadPrefs,
    private val exerciseRepository: IExerciseRepository,
) : ViewModel() {

    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Internal state flow for authentication status.
     * Starts with Loading state and updates based on user login status.
     */
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    
    /**
     * Public state flow exposing authentication status to the UI.
     */
    val authState = _authState.asStateFlow()

    // ==================== INITIALIZATION ====================
    
    init {
        initializeApplication()
    }

    /**
     * Performs all necessary initialization tasks when the app starts.
     * This includes authentication state monitoring and data synchronization.
     */
    private fun initializeApplication() {
        viewModelScope.launch {
            // Monitor authentication state changes
            monitorAuthenticationState()
            
            // Synchronize any pending like operations between local and remote storage
            synchronizePendingLikes()
        }

        // Download exercise data on first app launch for offline access
        downloadExercisesIfNeeded()
    }

    /**
     * Continuously monitors the user's authentication status and updates the UI state accordingly.
     */
    private suspend fun monitorAuthenticationState() {
        authRepository.isUserLoggedIn().collect { isUserLoggedIn ->
            _authState.value = if (isUserLoggedIn) {
                AuthUiState.Authenticated
            } else {
                AuthUiState.Unauthenticated
            }
        }
    }

    /**
     * Synchronizes any pending like operations that may have been cached locally
     * due to network connectivity issues.
     */
    private suspend fun synchronizePendingLikes() {
        likeRepository.syncPendingLikes()
    }

    // ==================== EXERCISE DATA MANAGEMENT ====================
    
    /**
     * Triggers the initial exercise download if it hasn't been completed before.
     * This ensures the app has exercise data available for offline use.
     */
    private fun downloadExercisesIfNeeded() {
        if (!exerciseDownloadPrefs.isInitialDownloadComplete()) {
            viewModelScope.launch {
                Log.d("MainViewModel", "Starting initial exercise data download...")
                val downloadResult = exerciseRepository.fetchAllExercisesAndCache()
                
                when (downloadResult) {
                    is ResultWrapper.Success -> {
                        Log.i("MainViewModel", "Exercise data download completed successfully.")
                        // The repository automatically sets the completion flag on success
                    }
                    is ResultWrapper.Error -> {
                        Log.e("MainViewModel", "Exercise data download failed.", downloadResult.exception)
                        // TODO: Consider implementing retry mechanism with WorkManager
                        // or showing a user-friendly error message
                    }
                    is ResultWrapper.Loading -> { 
                        // This should not occur from a suspend function, but handled defensively
                        Log.w("MainViewModel", "Unexpected loading state during exercise download.")
                    }
                }
            }
        } else {
            Log.d("MainViewModel", "Exercise data already downloaded previously.")
        }
    }
}

/**
 * Represents the different authentication states of the application.
 * 
 * This sealed class ensures type-safe handling of authentication states
 * throughout the UI layer.
 */
sealed class AuthUiState {
    /** The app is checking authentication status */
    data object Loading : AuthUiState()
    
    /** User is successfully authenticated and logged in */
    data object Authenticated : AuthUiState()
    
    /** User is not authenticated and needs to log in */
    data object Unauthenticated : AuthUiState()
}