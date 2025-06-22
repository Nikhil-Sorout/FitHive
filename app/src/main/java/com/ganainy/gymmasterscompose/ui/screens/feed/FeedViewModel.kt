package com.ganainy.gymmasterscompose.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganainy.gymmasterscompose.R
import com.ganainy.gymmasterscompose.ui.repository.FeedRepository
import com.ganainy.gymmasterscompose.ui.repository.IFeedRepository
import com.ganainy.gymmasterscompose.ui.repository.ILikeRepository
import com.ganainy.gymmasterscompose.ui.repository.ISocialRepository
import com.ganainy.gymmasterscompose.ui.repository.IUserRepository
import com.ganainy.gymmasterscompose.ui.repository.ResultWrapper
import com.ganainy.gymmasterscompose.ui.repository.onError
import com.ganainy.gymmasterscompose.ui.room.LikeType
import com.ganainy.gymmasterscompose.ui.screens.post_details.FeedPostWithLikesAndComments
import com.ganainy.gymmasterscompose.utils.UiText
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO: Create a cloud function for automatic feed generation when posts are created
// This would add new posts to the feeds of all following users automatically

/**
 * Data class representing the complete state of the feed screen.
 * 
 * This class encapsulates all the data needed to render the feed, including
 * posts, pagination state, loading states, and error information.
 * 
 * @property postList List of posts with their like and comment information
 * @property followingUserIds Set of user IDs that the current user follows
 * @property lastLoadedPostTimestamp Timestamp of the last loaded post for pagination
 * @property lastPostId ID of the last loaded post for pagination (tie-breaker)
 * @property isLoading Whether a page is currently being loaded
 * @property isRefreshing Whether the feed is being refreshed
 * @property hasReachedEnd Whether all available posts have been loaded
 * @property error Current error message, if any
 */
data class FeedUiData(
    val postList: List<FeedPostWithLikesAndComments> = emptyList(),
    val followingUserIds: Set<String> = emptySet(), // Keep track of who is followed
    val lastLoadedPostTimestamp: Timestamp? = null, // Used for pagination cursor
    val lastPostId: String? = null, // Used for pagination cursor (tie-breaker)
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasReachedEnd: Boolean = false,
    val error: UiText? = null
)

/**
 * ViewModel responsible for managing the social feed functionality.
 * 
 * This ViewModel handles:
 * - Loading and paginating posts from followed users
 * - Managing like/unlike interactions
 * - Refreshing feed data
 * - Error handling and loading states
 * 
 * @property socialRepository Repository for social operations (following, etc.)
 * @property userRepository Repository for user-related operations
 * @property feedRepository Repository for feed data operations
 * @property likeRepository Repository for like/unlike operations
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val socialRepository: ISocialRepository,
    private val userRepository: IUserRepository,
    private val feedRepository: IFeedRepository,
    private val likeRepository: ILikeRepository
) : ViewModel() {

    // ==================== STATE MANAGEMENT ====================
    
    /**
     * Internal state flow containing all feed-related data and UI state.
     */
    private val _feedUiData = MutableStateFlow(FeedUiData())
    
    /**
     * Public state flow exposing feed data to the UI layer.
     */
    val feedUiData: StateFlow<FeedUiData> = _feedUiData.asStateFlow()

    // ==================== INITIALIZATION ====================
    
    init {
        loadInitialFeed()
    }

    // ==================== FEED LOADING OPERATIONS ====================
    
    /**
     * Loads the initial feed data when the screen is first displayed.
     * 
     * This method resets all pagination state and loads the first page of posts
     * from users that the current user follows.
     */
    private fun loadInitialFeed() {
        viewModelScope.launch {
            // Prevent multiple simultaneous loading operations
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing) return@launch
            
            // Reset pagination state for fresh start
            _feedUiData.update {
                it.copy(
                    lastPostId = null,
                    lastLoadedPostTimestamp = null,
                    postList = emptyList(),
                    isLoading = true,
                    isRefreshing = false,
                    hasReachedEnd = false,
                    error = null
                )
            }
            
            // Load the first page of feed data
            loadFeedPage(isInitialLoad = true)
        }
    }

    /**
     * Loads the next page of feed data for pagination.
     * 
     * This method is called when the user scrolls near the bottom of the current
     * feed to load more posts. It uses the pagination cursors to fetch the next batch.
     */
    fun loadNextPage() {
        viewModelScope.launch {
            // Prevent loading if already in progress or at the end
            if (_feedUiData.value.isLoading ||
                _feedUiData.value.isRefreshing ||
                _feedUiData.value.hasReachedEnd
            ) return@launch

            // Set loading state for pagination
            _feedUiData.update { it.copy(isLoading = true, error = null) }
            
            // Load the next page using existing pagination cursors
            loadFeedPage(isInitialLoad = false)
        }
    }

    /**
     * Core method for loading feed data with pagination support.
     * 
     * This method handles the complex logic of:
     * - Fetching followed users (if needed)
     * - Loading posts with pagination cursors
     * - Fetching like status for each post
     * - Updating the UI state accordingly
     * 
     * @param isInitialLoad Whether this is the first load (true) or pagination (false)
     */
    private suspend fun loadFeedPage(isInitialLoad: Boolean) {
        try {
            val currentUserId = userRepository.getCurrentUserId()
            var relevantUserIds: List<String>

            // Fetch followed users only when necessary (initial load, refresh, or if empty)
            if (_feedUiData.value.followingUserIds.isEmpty() || isInitialLoad || _feedUiData.value.isRefreshing) {
                when (val followingResult = socialRepository.getFollowedUsers(currentUserId)) {
                    is ResultWrapper.Success -> {
                        // Include current user's posts in their own feed
                        relevantUserIds = followingResult.data + currentUserId
                        _feedUiData.update { it.copy(followingUserIds = relevantUserIds.toSet()) }
                    }
                    is ResultWrapper.Error -> {
                        throw followingResult.exception // Propagate error to outer catch block
                    }
                    is ResultWrapper.Loading -> {
                        // Defensive handling of unexpected loading state
                        _feedUiData.update { 
                            it.copy(isLoading = false, error = UiText.String("Error: Unexpected loading state")) 
                        }
                        return
                    }
                }
            } else {
                // Use cached followed users for pagination
                relevantUserIds = _feedUiData.value.followingUserIds.toList()
            }

            // Handle case where user follows no one
            if (relevantUserIds.isEmpty()) {
                _feedUiData.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        postList = emptyList(),
                        hasReachedEnd = true,
                        error = UiText.String("Follow users to see their posts")
                    )
                }
                return
            }

            // Fetch paginated posts from the feed repository
            val feedResult = feedRepository.getPaginatedFeed(
                relevantUserIds = relevantUserIds,
                lastPostTimestamp = if (isInitialLoad) null else _feedUiData.value.lastLoadedPostTimestamp,
                lastPostId = if (isInitialLoad) null else _feedUiData.value.lastPostId
            )

            when (feedResult) {
                is ResultWrapper.Success -> {
                    val newPosts = feedResult.data
                    // Determine if we've reached the end based on returned post count
                    val hasReachedEnd = newPosts.isEmpty() || newPosts.size < FeedRepository.PAGE_SIZE

                    // Fetch like status for each newly loaded post
                    val postsWithLikes = newPosts.map { post ->
                        val likeStatusResult = likeRepository.getLikeStatus(
                            targetId = post.id,
                            type = LikeType.POST,
                            postId = null // postId not needed for POST type likes
                        )
                        // Default to false if like status fetch fails
                        val isLiked = (likeStatusResult as? ResultWrapper.Success<Boolean>)?.data ?: false
                        FeedPostWithLikesAndComments(post = post, isLiked = isLiked)
                    }

                    // Update the feed state with new posts
                    _feedUiData.update { currentState ->
                        // Merge new posts with existing ones, ensuring no duplicates
                        val existingPosts = if (isInitialLoad) emptyList() else currentState.postList
                        val allPosts = (existingPosts + postsWithLikes)
                            .distinctBy { it.post.id } // Ensure uniqueness by post ID

                        currentState.copy(
                            postList = allPosts,
                            // Update pagination cursors with the last item from newly fetched posts
                            lastLoadedPostTimestamp = newPosts.lastOrNull()?.createdAt ?: currentState.lastLoadedPostTimestamp,
                            lastPostId = newPosts.lastOrNull()?.id ?: currentState.lastPostId,
                            isLoading = false,
                            isRefreshing = false,
                            hasReachedEnd = hasReachedEnd,
                            error = if (isInitialLoad && allPosts.isEmpty()) {
                                UiText.String("No posts found in feed.")
                            } else null
                        )
                    }
                }
                is ResultWrapper.Error -> {
                    throw feedResult.exception // Propagate error to outer catch block
                }
                is ResultWrapper.Loading -> { 
                    // This should not occur from a suspend function, but handled defensively
                }
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Error loading feed page (isInitialLoad=$isInitialLoad)", e)
            _feedUiData.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = UiText.String(e.message ?: "Failed to load feed")
                )
            }
        }
    }

    // ==================== INTERACTION OPERATIONS ====================
    
    /**
     * Toggles the like status of a specific post with optimistic UI updates.
     * 
     * This method immediately updates the UI to show the like/unlike action,
     * then performs the actual backend operation. If the backend operation fails,
     * the UI is reverted to the original state.
     * 
     * @param postId The ID of the post to like/unlike
     */
    fun toggleReaction(postId: String) {
        viewModelScope.launch {
            // Verify user is logged in before proceeding
            val currentUserId = try { 
                userRepository.getCurrentUserId() 
            } catch (e: Exception) { 
                null 
            }
            
            if (currentUserId == null) {
                _feedUiData.update { 
                    it.copy(error = UiText.StringResource(R.string.error_must_be_logged_in)) 
                }
                return@launch
            }

            // Find the target post in the current list
            val originalPostList = _feedUiData.value.postList
            val postIndex = originalPostList.indexOfFirst { it.post.id == postId }
            if (postIndex == -1) return@launch // Post not found

            val targetPost = originalPostList[postIndex]
            val isCurrentlyLiked = targetPost.isLiked
            val newLikeStatus = !isCurrentlyLiked
            val currentLikeCount = targetPost.post.postMetrics.likes
            
            // Calculate new like count (ensure it doesn't go below 0)
            val newLikeCount = (if (newLikeStatus) currentLikeCount + 1 else currentLikeCount - 1).coerceAtLeast(0)

            // Optimistically update the UI immediately
            val optimisticPostList = originalPostList.toMutableList()
            optimisticPostList[postIndex] = targetPost.copy(
                isLiked = newLikeStatus,
                post = targetPost.post.copy(
                    postMetrics = targetPost.post.postMetrics.copy(
                        likes = newLikeCount
                    )
                )
            )
            _feedUiData.update { it.copy(postList = optimisticPostList) }

            // Perform the actual backend like/unlike operation
            val backendResult = likeRepository.toggleLike(
                targetId = postId,
                type = LikeType.POST
            )

            // Revert UI if backend operation failed
            if (backendResult is ResultWrapper.Error) {
                Log.e("FeedViewModel", "Failed to toggle like for $postId", backendResult.exception)
                _feedUiData.update { it.copy(postList = originalPostList) } // Revert to original state
                _feedUiData.update { 
                    it.copy(error = UiText.StringResource(R.string.error_updating_reaction)) 
                }
            }
            // If successful, the optimistic update remains (no else needed)
        }
    }

    // ==================== REFRESH OPERATIONS ====================
    
    /**
     * Refreshes the entire feed by resetting pagination and reloading from the beginning.
     * 
     * This method is typically called when the user pulls to refresh the feed.
     * It clears all cached data and starts fresh from the first page.
     */
    fun refreshFeed() {
        viewModelScope.launch {
            // Prevent multiple simultaneous refresh operations
            if (_feedUiData.value.isLoading || _feedUiData.value.isRefreshing) return@launch
            
            // Reset all state for fresh refresh
            _feedUiData.update {
                it.copy(
                    isRefreshing = true,
                    lastPostId = null,
                    lastLoadedPostTimestamp = null,
                    hasReachedEnd = false,
                    followingUserIds = emptySet() // Force refetch of followed users
                )
            }
            
            // Load fresh data from the beginning
            loadFeedPage(isInitialLoad = true)
            // Note: loadFeedPage will set isRefreshing back to false on completion/error
        }
    }
}
