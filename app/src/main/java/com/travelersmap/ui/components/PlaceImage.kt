package com.travelersmap.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

/**
 * Network image with disk/memory cache, shimmer while loading, and a solid fallback on error.
 */
@Composable
fun CachedPlaceImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    if (url.isNullOrBlank()) {
        ImageFallback(modifier)
        return
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(300)
            .memoryCacheKey(url)
            .diskCacheKey(url)
            // Wikimedia and some CDNs reject empty/default agents.
            .addHeader(
                "User-Agent",
                "TravelersMap/1.1 (Android; Uzbekistan tourism; coil)"
            )
            .allowHardware(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Empty -> {
                ShimmerBlock(Modifier.fillMaxSize())
            }
            is AsyncImagePainter.State.Error -> {
                ImageFallback(Modifier.fillMaxSize(), broken = true)
            }
            else -> SubcomposeAsyncImageContent()
        }
    }
}

@Composable
fun ImageFallback(modifier: Modifier = Modifier, broken: Boolean = false) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surface
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (broken) Icons.Outlined.BrokenImage else Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(48.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroImageCarousel(
    urls: List<String>,
    placeName: String,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp
) {
    val photos = urls.filter { it.isNotBlank() }.ifEmpty { listOf("") }
    val pager = rememberPagerState(pageCount = { photos.size })

    Box(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            CachedPlaceImage(
                url = photos[page].ifBlank { null },
                contentDescription = "$placeName photo ${page + 1}",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Soft bottom fade so title text stays readable without blacking out the hero.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height * 0.45f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
                    )
                )
        )

        if (photos.size > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(photos.size) { i ->
                    val selected = pager.currentPage == i
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryThumb(
    url: String,
    modifier: Modifier = Modifier
) {
    CachedPlaceImage(
        url = url,
        contentDescription = null,
        modifier = modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}
