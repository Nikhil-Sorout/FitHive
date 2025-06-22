package com.ganainy.gymmasterscompose

/**
 * Application-wide constants used throughout the FitHive app.
 * 
 * This object contains all the static configuration values, database endpoints,
 * validation rules, and other constants that are used across different parts
 * of the application.
 */
object Constants {

    // ==================== DATABASE & FIREBASE CONFIGURATION ====================
    
    /**
     * Firebase Realtime Database URL for the FitHive application.
     * This is the root endpoint for all database operations.
     */
    const val FIREBASE_DATABASE_NAME = "https://gym-masters-compose.firebaseio.com/"

    // ==================== DATABASE FIELD NAMES ====================
    
    /**
     * Common field name for unique identifier across all database entities.
     */
    const val ID = "id"

    /**
     * Database collection/field name for workout-related data.
     */
    const val WORKOUT = "workout"

    /**
     * Database field name for user following relationships.
     */
    const val FOLLOWING = "following"

    // ==================== STORAGE & MEDIA ====================
    
    /**
     * Firebase Storage folder name for workout cover images.
     */
    const val WORKOUT_COVER_IMAGES = "workout_cover_images"
    
    /**
     * Field name for cover image URL in workout documents.
     */
    const val COVER_IMAGE = "cover_image"

    // ==================== VALIDATION RULES ====================
    
    /**
     * Minimum required length for user passwords to ensure security.
     */
    const val MINIMUM_PASSWORD_LENGTH = 6
    
    /**
     * Minimum required length for user names to ensure meaningful identification.
     */
    const val MINIMUM_NAME_LENGTH = 4

}