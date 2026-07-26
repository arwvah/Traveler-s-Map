package com.travelersmap.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.travelersmap.domain.model.TouristPlace
import com.travelersmap.ui.theme.TouristAccent

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun RatingStars(rating: Float) {
    Text(
        text = "★ %.1f".format(rating),
        style = MaterialTheme.typography.labelLarge,
        color = TouristAccent
    )
}

@Composable
fun PlaceMetaLine(place: TouristPlace) {
    Text(
        text = "${place.city} · ${place.category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ShimmerBlock(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        ),
        start = Offset(x - 200f, 0f),
        end = Offset(x, 200f)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@Composable
fun LoadingSkeleton() {
    Column(Modifier.padding(16.dp)) {
        ShimmerBlock(Modifier.fillMaxWidth().height(180.dp))
        Spacer(Modifier.height(12.dp))
        ShimmerBlock(Modifier.fillMaxWidth(0.6f).height(20.dp))
        Spacer(Modifier.height(8.dp))
        ShimmerBlock(Modifier.fillMaxWidth().height(14.dp))
        Spacer(Modifier.height(8.dp))
        ShimmerBlock(Modifier.fillMaxWidth(0.9f).height(14.dp))
    }
}

@Composable
fun EmptyState(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(24.dp)
    )
}
