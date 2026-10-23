package com.example.edit88.typography

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.UrduFontChoice
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.LateefFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily

@Composable
fun UrduSubtitleRenderer(
    clip: SubtitleClip,
    currentPlayheadMs: Long,
    modifier: Modifier = Modifier
) {
    val fontFamily = when (clip.fontChoice) {
        UrduFontChoice.NOTO_NASTALIQ -> NotoNastaliqUrduFontFamily
        UrduFontChoice.GULZAR -> GulzarFontFamily
        UrduFontChoice.AMIRI -> AmiriFontFamily
        UrduFontChoice.LATEEF -> LateefFontFamily
        UrduFontChoice.SYSTEM_SANS -> FontFamily.Default
        UrduFontChoice.SERIF -> FontFamily.Serif
        UrduFontChoice.IMPACT -> FontFamily.SansSerif
        UrduFontChoice.CURSIVE -> FontFamily.Cursive
    }

    val direction = if (clip.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            when (clip.stylePreset) {
                SubtitleStylePreset.KARAOKE -> {
                    KaraokeSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        currentPlayheadMs = currentPlayheadMs
                    )
                }
                SubtitleStylePreset.NEON_GLOW -> {
                    NeonGlowSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily
                    )
                }
                SubtitleStylePreset.BOXED_SUB -> {
                    BoxedSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily
                    )
                }
                SubtitleStylePreset.RETRO_STROKE -> {
                    RetroStrokeSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily
                    )
                }
                SubtitleStylePreset.MINIMAL -> {
                    MinimalSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KaraokeSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily,
    currentPlayheadMs: Long
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.6f).sp else (clip.fontSizeSp * 1.35f).sp

    if (clip.words.isNotEmpty()) {
        FlowRow(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .background(Color(0x88000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            clip.words.forEach { timedWord ->
                val isActive = currentPlayheadMs in timedWord.startOffsetMs..timedWord.endOffsetMs
                val wordScale by animateFloatAsState(
                    targetValue = if (isActive) 1.15f else 1.0f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "wordScale"
                )

                val wordColor = if (isActive) {
                    Color(clip.activeColorHex)
                } else {
                    Color(clip.textColorHex)
                }

                Text(
                    text = "${timedWord.word} ",
                    fontFamily = fontFamily,
                    fontSize = clip.fontSizeSp.sp,
                    lineHeight = lineHeightVal,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = wordColor,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = true),
                        shadow = if (isActive) Shadow(
                            color = Color(clip.activeColorHex).copy(alpha = 0.85f),
                            offset = Offset(0f, 0f),
                            blurRadius = 16f
                        ) else Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 6f
                        )
                    ),
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .scale(wordScale)
                )
            }
        }
    } else {
        // Fallback when no words breakdown
        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.Bold,
            color = Color(clip.textColorHex),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true),
                shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 8f)
            ),
            modifier = Modifier
                .widthIn(max = 540.dp)
                .background(Color(0x88000000), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun NeonGlowSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.6f).sp else (clip.fontSizeSp * 1.35f).sp

    Box(
        modifier = Modifier
            .widthIn(max = 540.dp)
            .border(1.5.dp, Color(0xFF06B6D4), RoundedCornerShape(14.dp))
            .background(Color(0xAA12131F), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF67E8F9),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true),
                shadow = Shadow(
                    color = Color(0xFF06B6D4),
                    offset = Offset(0f, 0f),
                    blurRadius = 18f
                )
            )
        )
    }
}

@Composable
private fun BoxedSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.6f).sp else (clip.fontSizeSp * 1.35f).sp

    Box(
        modifier = Modifier
            .widthIn(max = 540.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .background(Color(0xDD000000), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.SemiBold,
            color = Color(clip.textColorHex),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            )
        )
    }
}

@Composable
private fun RetroStrokeSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.6f).sp else (clip.fontSizeSp * 1.35f).sp

    Text(
        text = clip.text,
        fontFamily = fontFamily,
        fontSize = (clip.fontSizeSp + 2).sp,
        lineHeight = lineHeightVal,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFFBBF24),
        textAlign = TextAlign.Center,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            shadow = Shadow(
                color = Color.Black,
                offset = Offset(4f, 4f),
                blurRadius = 2f
            )
        ),
        modifier = Modifier
            .widthIn(max = 540.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun MinimalSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.6f).sp else (clip.fontSizeSp * 1.35f).sp

    Text(
        text = clip.text,
        fontFamily = fontFamily,
        fontSize = clip.fontSizeSp.sp,
        lineHeight = lineHeightVal,
        fontWeight = FontWeight.Normal,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            shadow = Shadow(
                color = Color(0xCC000000),
                offset = Offset(1.5f, 1.5f),
                blurRadius = 4f
            )
        ),
        modifier = Modifier
            .widthIn(max = 540.dp)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
