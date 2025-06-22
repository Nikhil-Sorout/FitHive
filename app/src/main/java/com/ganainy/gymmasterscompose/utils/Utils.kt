package com.ganainy.gymmasterscompose.utils

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import kotlin.random.Random

/**
 * Utility object containing common helper functions used throughout the FitHive application.
 * 
 * This object provides validation, formatting, and general utility functions that
 * are shared across different parts of the application.
 */
object Utils {

    // ==================== VALIDATION FUNCTIONS ====================
    
    /**
     * Validates if the provided string is a valid email address format.
     * 
     * Uses Android's built-in email pattern matcher to ensure the email follows
     * standard email format requirements.
     * 
     * @param email The email string to validate
     * @return true if the email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
                )
    }

    /**
     * Validates if a field meets minimum length requirements or is empty.
     * 
     * This function allows empty fields (trimmed) or fields that meet the minimum
     * length requirement. Useful for optional fields that have length constraints
     * when filled.
     * 
     * @param field The string field to validate
     * @param length The minimum required length if the field is not empty
     * @return true if the field is empty or meets the length requirement
     */
    fun isValidFieldLength(field: String, length: Int): Boolean {
        return (field.trim { it <= ' ' }
            .isEmpty()) || field.length >= length
    }

    // ==================== GENERATION FUNCTIONS ====================
    
    /**
     * Generates a random username using a combination of colors, words, and numbers.
     * 
     * Creates usernames in the format: [color][word][number]
     * Example: "redninja123", "bluepanda456"
     * 
     * @return A randomly generated username string
     */
    fun generateRandomUsername(): String {
        val words = listOf(
            "ninja", "pirate", "wizard", "panda", "robot",
            "unicorn", "dragon", "zombie", "viking", "alien"
        )

        val colors = listOf(
            "red", "blue", "green", "yellow", "purple",
            "orange", "black", "white", "silver", "gold"
        )

        val word = words.random()
        val color = colors.random()
        val number = Random.nextInt(100, 999)

        return "$color$word$number"
    }

    /**
     * Generates a random ID with a specified prefix.
     * 
     * Creates IDs in the format: [prefix]_[randomString]
     * Example: "user_abc123def", "post_xyz789ghi"
     * 
     * @param prefix The prefix to add before the random string
     * @return A randomly generated ID string
     */
    fun generateRandomId(prefix: String): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..10).map { chars.random() }.joinToString("")
        return "$prefix _$randomString"
    }

    // ==================== TIME FORMATTING FUNCTIONS ====================
    
    /**
     * Formats a timestamp into a relative time string (e.g., "2 hours ago").
     * 
     * Uses the TimeAgo library to convert millisecond timestamps into human-readable
     * relative time strings based on the current locale.
     * 
     * @param timestamp The timestamp in milliseconds
     * @return A formatted relative time string
     */
    @Composable
    fun formatRelativeTime(timestamp: Long): String {
        val currentLocale = LocalContext.current.resources.configuration.locales[0]
        val timeAgoMessages = remember(currentLocale) {
            TimeAgoMessages.Builder().withLocale(currentLocale).build()
        }
        return TimeAgo.using(timestamp, timeAgoMessages)
    }

    /**
     * Formats a Firestore Timestamp into a relative time string.
     * 
     * Converts Firestore Timestamp objects to relative time strings using the TimeAgo library.
     * This is specifically designed for Firestore timestamp objects.
     * 
     * @param timestamp The Firestore Timestamp object
     * @return A formatted relative time string
     */
    @Composable
    fun formatRelativeTimeFromFireStoreTimeStamp(timestamp: com.google.firebase.Timestamp): String {
        val currentLocale = LocalContext.current.resources.configuration.locales[0]
        val timeAgoMessages = remember(currentLocale) {
            TimeAgoMessages.Builder().withLocale(currentLocale).build()
        }
        return TimeAgo.using(timestamp.toDate().time, timeAgoMessages)
    }

    // ==================== UI HELPER FUNCTIONS ====================
    
    /**
     * Displays a toast message to the user.
     * 
     * A convenience function for showing toast messages with customizable duration.
     * 
     * @param context The application context
     * @param message The message to display
     * @param duration The duration to show the toast (defaults to LENGTH_SHORT)
     */
    fun showToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }

    // ==================== TEXT PROCESSING FUNCTIONS ====================
    
    /**
     * Extracts hashtags from a text content string.
     * 
     * Parses the content and finds all words that start with '#' symbol,
     * removes the '#' prefix, and returns a list of unique hashtags.
     * 
     * @param content The text content to extract hashtags from
     * @return A list of unique hashtags without the '#' symbol
     */
    fun extractHashtags(content: String): List<String> {
        return content.split("\\s+".toRegex())
            .filter { it.startsWith("#") && it.length > 1 }
            .map { it.substring(1) } // Remove the # symbol
            .distinct() // Remove duplicates
    }

}