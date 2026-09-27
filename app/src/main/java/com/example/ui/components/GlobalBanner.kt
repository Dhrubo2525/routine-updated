package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BannerSettings

enum class ZoneVibe {
    STUDENT,
    DASHBOARD,
    TEACHERS,
    QUIZ,
    CALENDAR,
    ADMIN
}

@Composable
fun GlobalBanner(
    bannerSettings: BannerSettings,
    currentZone: ZoneVibe,
    modifier: Modifier = Modifier
) {
    if (!bannerSettings.enabled) return

    val infiniteTransition = rememberInfiniteTransition(label = "banner_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "banner_shimmer_offset"
    )

    // Zone specific vibes
    val (gradientStart, gradientMid, gradientEnd) = when (currentZone) {
        ZoneVibe.STUDENT -> Triple(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF0284C7))
        ZoneVibe.DASHBOARD -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF312E81))
        ZoneVibe.TEACHERS -> Triple(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488))
        ZoneVibe.QUIZ -> Triple(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF9333EA))
        ZoneVibe.CALENDAR -> Triple(Color(0xFF831843), Color(0xFFBE185D), Color(0xFFE11D48))
        ZoneVibe.ADMIN -> Triple(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF3F3F46))
    }

    val bannerBrush = Brush.horizontalGradient(
        colors = listOf(gradientStart, gradientMid, gradientEnd)
    )

    val titleFontFamily = when (bannerSettings.font) {
        "Academic Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Tech Bold" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }

    val titleFontWeight = when (bannerSettings.font) {
        "Tech Bold" -> FontWeight.Black
        "Academic Serif" -> FontWeight.Bold
        else -> FontWeight.Bold
    }

    val textAlign = when (bannerSettings.position) {
        "Left" -> TextAlign.Start
        "Right" -> TextAlign.End
        else -> TextAlign.Center
    }

    val contentAlignment = when (bannerSettings.position) {
        "Left" -> Alignment.Start
        "Right" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("global_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = contentAlignment
            ) {
                // Top Highlights (if any configured for Top)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!bannerSettings.highlight1Hidden && bannerSettings.highlight1Position == "Top") {
                        HighlightChip(text = bannerSettings.highlight1, color = Color(0xFFF59E0B))
                    }
                    if (!bannerSettings.highlight2Hidden && bannerSettings.highlight2Position == "Top") {
                        HighlightChip(text = bannerSettings.highlight2, color = Color(0xFF38BDF8))
                    }
                    if (!bannerSettings.highlight3Hidden && bannerSettings.highlight3Position == "Top") {
                        HighlightChip(text = bannerSettings.highlight3, color = Color(0xFFA7F3D0))
                    }
                }

                // Middle Row: Left highlights, Main text block, Right highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Highlights
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (!bannerSettings.highlight1Hidden && bannerSettings.highlight1Position == "Left") {
                            HighlightChip(text = bannerSettings.highlight1, color = Color(0xFFF59E0B))
                        }
                        if (!bannerSettings.highlight2Hidden && bannerSettings.highlight2Position == "Left") {
                            HighlightChip(text = bannerSettings.highlight2, color = Color(0xFF38BDF8))
                        }
                        if (!bannerSettings.highlight3Hidden && bannerSettings.highlight3Position == "Left") {
                            HighlightChip(text = bannerSettings.highlight3, color = Color(0xFFA7F3D0))
                        }
                    }

                    // Main Text
                    Column(
                        modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp),
                        horizontalAlignment = contentAlignment
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = bannerSettings.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = titleFontFamily,
                                fontWeight = titleFontWeight,
                                textAlign = textAlign
                            )
                        }

                        if (bannerSettings.subtitle.isNotBlank()) {
                            Text(
                                text = bannerSettings.subtitle,
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp,
                                textAlign = textAlign,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Right Highlights
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        if (!bannerSettings.highlight1Hidden && bannerSettings.highlight1Position == "Right") {
                            HighlightChip(text = bannerSettings.highlight1, color = Color(0xFFF59E0B))
                        }
                        if (!bannerSettings.highlight2Hidden && bannerSettings.highlight2Position == "Right") {
                            HighlightChip(text = bannerSettings.highlight2, color = Color(0xFF38BDF8))
                        }
                        if (!bannerSettings.highlight3Hidden && bannerSettings.highlight3Position == "Right") {
                            HighlightChip(text = bannerSettings.highlight3, color = Color(0xFFA7F3D0))
                        }
                    }
                }

                // Bottom Highlights (if any configured for Bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!bannerSettings.highlight1Hidden && bannerSettings.highlight1Position == "Bottom") {
                        HighlightChip(text = bannerSettings.highlight1, color = Color(0xFFF59E0B))
                    }
                    if (!bannerSettings.highlight2Hidden && bannerSettings.highlight2Position == "Bottom") {
                        HighlightChip(text = bannerSettings.highlight2, color = Color(0xFF38BDF8))
                    }
                    if (!bannerSettings.highlight3Hidden && bannerSettings.highlight3Position == "Bottom") {
                        HighlightChip(text = bannerSettings.highlight3, color = Color(0xFFA7F3D0))
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = color.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
