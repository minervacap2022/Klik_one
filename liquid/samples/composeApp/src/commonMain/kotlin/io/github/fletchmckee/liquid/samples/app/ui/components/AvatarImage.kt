package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage

/**
 * Displays an avatar image from a URL with a fallback to initials.
 * Uses Coil 3 for async image loading.
 */
@Composable
fun AvatarImage(
    avatarUrl: String?,
    initials: String,
    size: Dp = 40.dp,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.1f),
    initialsColor: Color = Color.Gray,
    initialsStyle: TextStyle = MaterialTheme.typography.titleSmall,
    modifier: Modifier = Modifier
) {
    if (!avatarUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            loading = {
                InitialsAvatar(
                    initials = initials,
                    size = size,
                    backgroundColor = backgroundColor,
                    textColor = initialsColor,
                    textStyle = initialsStyle
                )
            },
            error = {
                InitialsAvatar(
                    initials = initials,
                    size = size,
                    backgroundColor = backgroundColor,
                    textColor = initialsColor,
                    textStyle = initialsStyle
                )
            }
        )
    } else {
        InitialsAvatar(
            initials = initials,
            size = size,
            backgroundColor = backgroundColor,
            textColor = initialsColor,
            textStyle = initialsStyle,
            modifier = modifier
        )
    }
}

@Composable
private fun InitialsAvatar(
    initials: String,
    size: Dp,
    backgroundColor: Color,
    textColor: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = textStyle,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
