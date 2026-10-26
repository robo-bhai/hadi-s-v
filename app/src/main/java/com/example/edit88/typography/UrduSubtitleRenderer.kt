package com.example.edit88.typography

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.model.SubtitleAnimationPreset
import com.example.edit88.model.SubtitleClip
import com.example.edit88.model.SubtitleStylePreset
import com.example.edit88.model.UrduFontChoice
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.LateefFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import java.io.File

@Composable
fun UrduSubtitleRenderer(
    clip: SubtitleClip,
    currentPlayheadMs: Long,
    modifier: Modifier = Modifier
) {
    // Resolve Font: Custom file or Built-in
    val fontFamily = remember(clip.fontChoice, clip.customFontPath) {
        if (!clip.customFontPath.isNullOrBlank()) {
            val file = File(clip.customFontPath)
            if (file.exists() && file.canRead()) {
                try {
                    FontFamily(Font(file))
                } catch (_: Exception) {
                    NotoNastaliqUrduFontFamily
                }
            } else {
                NotoNastaliqUrduFontFamily
            }
        } else {
            when (clip.fontChoice) {
                UrduFontChoice.NOTO_NASTALIQ -> NotoNastaliqUrduFontFamily
                UrduFontChoice.GULZAR -> GulzarFontFamily
                UrduFontChoice.AMIRI -> AmiriFontFamily
                UrduFontChoice.LATEEF -> LateefFontFamily
                UrduFontChoice.SYSTEM_SANS -> FontFamily.Default
                UrduFontChoice.SERIF -> FontFamily.Serif
                UrduFontChoice.IMPACT -> FontFamily.SansSerif
                UrduFontChoice.CURSIVE -> FontFamily.Cursive
                UrduFontChoice.CUSTOM_IMPORTED -> NotoNastaliqUrduFontFamily
            }
        }
    }

    val direction = if (clip.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Animation Transitions
    val isClipActive = currentPlayheadMs in clip.startMs..clip.endMs
    val elapsedInClipMs = (currentPlayheadMs - clip.startMs).coerceAtLeast(0L)

    val entryScale by animateFloatAsState(
        targetValue = when (clip.animationPreset) {
            SubtitleAnimationPreset.POP -> if (isClipActive) 1.0f else 0.85f
            SubtitleAnimationPreset.KARAOKE_BOUNCE -> if (isClipActive) 1.02f else 0.9f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "animScale"
    )

    val entryAlpha by animateFloatAsState(
        targetValue = when (clip.animationPreset) {
            SubtitleAnimationPreset.FADE -> if (isClipActive) 1.0f else 0.0f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "animAlpha"
    )

    val entryOffsetY by animateFloatAsState(
        targetValue = when (clip.animationPreset) {
            SubtitleAnimationPreset.SLIDE_UP -> if (isClipActive) 0f else 25f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "animSlide"
    )

    val textAlign = when (clip.textAlign.uppercase()) {
        "LEFT" -> TextAlign.Start
        "RIGHT" -> TextAlign.End
        else -> TextAlign.Center
    }

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .graphicsLayer {
                    scaleX = entryScale
                    scaleY = entryScale
                    alpha = entryAlpha
                    translationY = entryOffsetY
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            when (clip.stylePreset) {
                SubtitleStylePreset.KARAOKE -> {
                    KaraokeSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        currentPlayheadMs = currentPlayheadMs,
                        elapsedInClipMs = elapsedInClipMs,
                        textAlign = textAlign
                    )
                }
                SubtitleStylePreset.NEON_GLOW -> {
                    NeonGlowSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        textAlign = textAlign
                    )
                }
                SubtitleStylePreset.BOXED_SUB -> {
                    BoxedSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        textAlign = textAlign
                    )
                }
                SubtitleStylePreset.RETRO_STROKE -> {
                    RetroStrokeSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        textAlign = textAlign
                    )
                }
                SubtitleStylePreset.MINIMAL -> {
                    MinimalSubtitleView(
                        clip = clip,
                        fontFamily = fontFamily,
                        textAlign = textAlign
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
    currentPlayheadMs: Long,
    elapsedInClipMs: Long,
    textAlign: TextAlign
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.8f).sp else (clip.fontSizeSp * 1.45f).sp

    val backgroundColor = Color(clip.bgColorHex).copy(alpha = clip.bgOpacity.coerceIn(0f, 1f))
    val outlineBorderColor = if (clip.outlineWidthDp > 0f) Color(clip.outlineColorHex) else Color.Transparent

    val horizontalAlign = when (textAlign) {
        TextAlign.Start -> Alignment.Start
        TextAlign.End -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    if (clip.words.isNotEmpty()) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(clip.outlineWidthDp.dp, outlineBorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 22.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = horizontalAlign),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (timedWord in clip.words) {
                // Typewriter support: only show words if elapsed time has reached their offset
                val isVisibleForTypewriter = if (clip.animationPreset == SubtitleAnimationPreset.TYPEWRITER) {
                    currentPlayheadMs >= timedWord.startOffsetMs
                } else true

                if (isVisibleForTypewriter) {
                    val isActive = currentPlayheadMs in timedWord.startOffsetMs..timedWord.endOffsetMs
                    val isKaraokeBounce = clip.animationPreset == SubtitleAnimationPreset.KARAOKE_BOUNCE

                    val wordScale by animateFloatAsState(
                        targetValue = if (isActive) {
                            if (isKaraokeBounce) 1.20f else 1.10f
                        } else 1.0f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "wordScale"
                    )

                    val wordColor = if (isActive) {
                        Color(clip.activeColorHex)
                    } else {
                        Color(clip.textColorHex)
                    }

                    val glowShadow = if (clip.glowRadius > 0f) {
                        Shadow(color = Color(clip.glowColorHex), offset = Offset.Zero, blurRadius = clip.glowRadius)
                    } else if (isActive) {
                        Shadow(color = Color(clip.activeColorHex).copy(alpha = 0.85f), offset = Offset.Zero, blurRadius = 16f)
                    } else {
                        Shadow(color = Color(clip.shadowColorHex), offset = Offset(2f, 2f), blurRadius = clip.shadowBlurRadius)
                    }

                    Text(
                        text = timedWord.word,
                        fontFamily = fontFamily,
                        fontSize = clip.fontSizeSp.sp,
                        lineHeight = lineHeightVal,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                        color = wordColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        textAlign = textAlign,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = true),
                            shadow = glowShadow
                        ),
                        modifier = Modifier
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                            .scale(wordScale)
                    )
                }
            }
        }
    } else {
        // Fallback when no words breakdown - Movie-style horizontal subtitle
        val glowShadow = if (clip.glowRadius > 0f) {
            Shadow(color = Color(clip.glowColorHex), offset = Offset.Zero, blurRadius = clip.glowRadius)
        } else {
            Shadow(color = Color(clip.shadowColorHex), offset = Offset(2f, 2f), blurRadius = clip.shadowBlurRadius)
        }

        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.Bold,
            color = Color(clip.textColorHex),
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true),
                shadow = glowShadow
            ),
            modifier = Modifier
                .wrapContentWidth()
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(clip.outlineWidthDp.dp, outlineBorderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 22.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun NeonGlowSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily,
    textAlign: TextAlign
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.8f).sp else (clip.fontSizeSp * 1.45f).sp
    val glowColor = if (clip.glowRadius > 0f) Color(clip.glowColorHex) else Color(0xFF06B6D4)

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .border(1.5.dp, glowColor, RoundedCornerShape(14.dp))
            .background(Color(clip.bgColorHex).copy(alpha = clip.bgOpacity), RoundedCornerShape(14.dp))
            .padding(horizontal = 22.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.Bold,
            color = Color(clip.textColorHex),
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true),
                shadow = Shadow(
                    color = glowColor,
                    offset = Offset(0f, 0f),
                    blurRadius = if (clip.glowRadius > 0f) clip.glowRadius else 18f
                )
            )
        )
    }
}

@Composable
private fun BoxedSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily,
    textAlign: TextAlign
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.8f).sp else (clip.fontSizeSp * 1.45f).sp

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .shadow(clip.shadowBlurRadius.dp, RoundedCornerShape(10.dp))
            .background(Color(clip.bgColorHex).copy(alpha = clip.bgOpacity), RoundedCornerShape(10.dp))
            .border(clip.outlineWidthDp.dp, Color(clip.outlineColorHex), RoundedCornerShape(10.dp))
            .padding(horizontal = 22.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = clip.text,
            fontFamily = fontFamily,
            fontSize = clip.fontSizeSp.sp,
            lineHeight = lineHeightVal,
            fontWeight = FontWeight.SemiBold,
            color = Color(clip.textColorHex),
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = true)
            )
        )
    }
}

@Composable
private fun RetroStrokeSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily,
    textAlign: TextAlign
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.8f).sp else (clip.fontSizeSp * 1.45f).sp

    Text(
        text = clip.text,
        fontFamily = fontFamily,
        fontSize = (clip.fontSizeSp + 1).sp,
        lineHeight = lineHeightVal,
        fontWeight = FontWeight.ExtraBold,
        color = Color(clip.textColorHex),
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            shadow = Shadow(
                color = Color(clip.outlineColorHex),
                offset = Offset(3f, 3f),
                blurRadius = 3f
            )
        ),
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 18.dp, vertical = 5.dp)
    )
}

@Composable
private fun MinimalSubtitleView(
    clip: SubtitleClip,
    fontFamily: FontFamily,
    textAlign: TextAlign
) {
    val isNastaliq = clip.fontChoice in listOf(
        UrduFontChoice.NOTO_NASTALIQ,
        UrduFontChoice.GULZAR,
        UrduFontChoice.AMIRI,
        UrduFontChoice.LATEEF
    )
    val lineHeightVal = if (isNastaliq) (clip.fontSizeSp * 1.8f).sp else (clip.fontSizeSp * 1.45f).sp

    Text(
        text = clip.text,
        fontFamily = fontFamily,
        fontSize = clip.fontSizeSp.sp,
        lineHeight = lineHeightVal,
        fontWeight = FontWeight.Normal,
        color = Color(clip.textColorHex),
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            shadow = Shadow(
                color = Color(clip.shadowColorHex).copy(alpha = 0.8f),
                offset = Offset(1.5f, 1.5f),
                blurRadius = clip.shadowBlurRadius
            )
        ),
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
