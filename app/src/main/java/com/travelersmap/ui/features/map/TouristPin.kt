package com.travelersmap.ui.features.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelersmap.ui.theme.Black
import com.travelersmap.ui.theme.TouristAccent
import com.travelersmap.ui.theme.White

/**
 * Oversized tourist-only pin. Accent gold is reserved for POIs, not chrome.
 */
@Composable
fun TouristPin(selected: Boolean, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "pinScale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .size(if (selected) 56.dp else 48.dp)
            .shadow(10.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(TouristAccent)
            .border(3.dp, if (selected) White else White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = Black,
            modifier = Modifier.size(if (selected) 30.dp else 26.dp)
        )
    }
}

/** Dense cluster bubble — still tourist-accent only. */
@Composable
fun ClusterBubble(count: Int, modifier: Modifier = Modifier) {
    val size = when {
        count >= 20 -> 64.dp
        count >= 10 -> 56.dp
        else -> 48.dp
    }
    Box(
        modifier = modifier
            .size(size)
            .shadow(12.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Black)
            .border(2.dp, TouristAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
