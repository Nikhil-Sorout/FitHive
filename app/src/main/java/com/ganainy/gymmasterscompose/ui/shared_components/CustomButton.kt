package com.ganainy.gymmasterscompose.ui.shared_components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A customizable button component with modern Material Design styling.
 * 
 * This button features:
 * - Smooth scale animation on press
 * - Gradient background support
 * - Optional icon with text
 * - Disabled state styling
 * - Rounded corners and shadow effects
 * 
 * @param text The button text to display
 * @param onClick The action to perform when the button is clicked
 * @param modifier Additional modifier to apply to the button
 * @param icon Optional icon to display before the text
 * @param enabled Whether the button is interactive (default: true)
 * @param backgroundGradient The gradient brush for the button background
 * @param contentColor The color for text and icon content
 */
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    backgroundGradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    ),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    // Animate button scale based on enabled state for visual feedback
    val animatedScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Standard Material Design button height
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = if (enabled) 6.dp else 2.dp, // Reduced shadow when disabled
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                brush = if (enabled) {
                    // Use provided gradient when enabled
                    backgroundGradient
                } else {
                    // Use muted gradient when disabled
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    )
                }
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Display icon if provided
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor.copy(alpha = if (enabled) 1f else 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Display button text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor.copy(alpha = if (enabled) 1f else 0.5f)
            )
        }
    }
}

/**
 * Preview function showcasing different CustomButton variations.
 * 
 * This preview demonstrates:
 * - Enabled buttons with and without icons
 * - Disabled button state
 * - Custom gradient styling
 */
@Preview(showBackground = true)
@Composable
fun CustomButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enabled button with icon
            CustomButton(
                text = "Save Changes",
                onClick = { /* Handle click */ },
                icon = Icons.Default.Check
            )

            // Enabled button without icon
            CustomButton(
                text = "Submit",
                onClick = { /* Handle click */ }
            )

            // Disabled button with icon
            CustomButton(
                text = "Disabled Button",
                onClick = { /* Handle click */ },
                icon = Icons.Default.Close,
                enabled = false
            )

            // Custom gradient button
            CustomButton(
                text = "Custom Gradient",
                onClick = { /* Handle click */ },
                backgroundGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6200EA),
                        Color(0xFF3700B3)
                    )
                ),
                contentColor = Color.White
            )
        }
    }
}