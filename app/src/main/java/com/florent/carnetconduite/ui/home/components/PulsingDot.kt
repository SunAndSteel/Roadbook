package com.florent.carnetconduite.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
internal fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    // Indicateur animé pour un état "actif".
    val transition = rememberInfiniteTransition(label = "ActiveIndicator")
    val pulseSpec = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = pulseSpec,
        label = "ActiveIndicatorScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = pulseSpec,
        label = "ActiveIndicatorAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}
