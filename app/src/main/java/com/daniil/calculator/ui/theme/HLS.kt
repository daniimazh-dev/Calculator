@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.daniil.calculator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/* ============================
   HSL utils
   ============================ */

data class Hsl(val h: Float, val s: Float, val l: Float)

private fun clamp01(v: Float) = v.coerceIn(0f, 1f)

private fun Color.toHsl(): Hsl {
    val r = red
    val g = green
    val b = blue
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val l = (max + min) / 2f

    if (max == min) return Hsl(0f, 0f, l)

    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        r -> (g - b) / d + (if (g < b) 6 else 0)
        g -> (b - r) / d + 2
        else -> (r - g) / d + 4
    } / 6f

    return Hsl(h * 360f, s, l)
}

private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val H = ((h % 360f) + 360f) % 360f
    val S = clamp01(s)
    val L = clamp01(l)

    val c = (1f - abs(2 * L - 1f)) * S
    val x = c * (1f - abs((H / 60f) % 2 - 1f))
    val m = L - c / 2f

    val (r1, g1, b1) = when {
        H < 60f -> Triple(c, x, 0f)
        H < 120f -> Triple(x, c, 0f)
        H < 180f -> Triple(0f, c, x)
        H < 240f -> Triple(0f, x, c)
        H < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(r1 + m, g1 + m, b1 + m, 1f)
}

private fun withLightness(color: Color, newL: Float): Color {
    val (h, s, _) = color.toHsl()
    return hslToColor(h, s, newL)
}

private fun withSaturation(color: Color, newS: Float): Color {
    val (h, _, l) = color.toHsl()
    return hslToColor(h, newS.coerceIn(0f, 1f), l)
}

private fun shiftHsl(
    color: Color,
    hueShift: Float = 0f,
    satMul: Float = 1f,
    lightMul: Float = 1f
): Color {
    val (h, s, l) = color.toHsl()
    return hslToColor(h + hueShift, (s * satMul).coerceIn(0f, 1f), (l * lightMul).coerceIn(0f, 1f))
}

private fun reTone(color: Color, targetL: Float, satMul: Float = 1f): Color {
    val (h, s, _) = color.toHsl()
    return hslToColor(h, (s * satMul).coerceIn(0f, 1f), targetL.coerceIn(0f, 1f))
}

/* ============================
   Contrast helpers (colored on-colors)
   ============================ */

private fun Color.luminance(): Float {
    fun chan(c: Float) = if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    val r = chan(red)
    val g = chan(green)
    val b = chan(blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private fun contrastRatio(fg: Color, bg: Color): Float {
    val l1 = fg.luminance() + 0.05f
    val l2 = bg.luminance() + 0.05f
    return max(l1, l2) / min(l1, l2)
}

/**
 * Створює "onColor" того ж відтінку, що й bg, але із протилежною тональністю.
 * Підганяє lightness, щоб досягти принаймні ~4.5 контрасту, не скочуючись у чисто чорний/білий.
 */
private fun coloredOn(bg: Color, preferLightTextOnDarkBg: Boolean = true): Color {
    val (h, s, l) = bg.toHsl()
    // Стартовий кандидат: якщо фон темний — дуже світлий текст тієї ж H/S, і навпаки
    var candidate = hslToColor(h, (s * 0.85f).coerceIn(0f, 1f), if (l < 0.5f) 0.92f else 0.12f)

    var tries = 0
    while (contrastRatio(candidate, bg) < 4.5f && tries < 6) {
        val step = 0.08f
        val (ch, cs, cl) = candidate.toHsl()
        val newL = if (l < 0.5f) (cl + step) else (cl - step)
        // Трохи підсилюємо насиченість, щоб “on” був виразнішим
        val newS = (cs * 1.05f + 0.02f).coerceIn(0f, 1f)
        candidate = hslToColor(ch, newS, newL)
        tries++
    }
    return candidate
}

/* ============================
   Heuristics близькі до Material You
   ============================ */

private data class Palettes(
    val primaryBase: Color,
    val secondaryBase: Color,
    val tertiaryBase: Color,
    val neutralBase: Color,
    val neutralVariantBase: Color
)

private fun buildPalettes(base: Color): Palettes {
    val primaryBase = base
    val secondaryBase = shiftHsl(base, hueShift = 40f, satMul = 0.80f)   // тепліший відтінок
    val tertiaryBase = shiftHsl(base, hueShift = 90f, satMul = 0.70f)  // контрастний
    val neutralBase = withSaturation(base, newS = (base.toHsl().s * 0.08f).coerceAtMost(0.10f))
    val neutralVariantBase = withSaturation(base, newS = (base.toHsl().s * 0.16f).coerceAtMost(0.22f))
    return Palettes(primaryBase, secondaryBase, tertiaryBase, neutralBase, neutralVariantBase)
}

/* ============================
   Error palette (узгоджений, не від base)
   ============================ */

private fun errorTone(light: Boolean): Pair<Color, Color> {
    // Ближче до Material3: насичений червоний + контейнер
    val err = if (light) hslToColor(4f, 0.70f, 0.45f) else hslToColor(4f, 0.65f, 0.70f)
    val errContainer = if (light) hslToColor(4f, 0.65f, 0.92f) else hslToColor(4f, 0.55f, 0.25f)
    return err to errContainer
}

/* ============================
   Основний генератор схеми
   ============================ */

fun generateHslColorScheme(base: Color, dark: Boolean): ColorScheme {
    val p = buildPalettes(base)

    return if (dark) {
        // DARK: Primary/Secondary/Tertiary — світлі тони; Container — темні
        val primary = reTone(p.primaryBase, targetL = 0.72f)
        val onPrimary = coloredOn(primary)
        val primaryContainer = reTone(p.primaryBase, targetL = 0.28f, satMul = 0.95f)
        val onPrimaryContainer = coloredOn(primaryContainer)

        val secondary = reTone(p.secondaryBase, targetL = 0.70f)
        val onSecondary = coloredOn(secondary)
        val secondaryContainer = reTone(p.secondaryBase, targetL = 0.26f, satMul = 0.95f)
        val onSecondaryContainer = coloredOn(secondaryContainer)

        val tertiary = reTone(p.tertiaryBase, targetL = 0.70f)
        val onTertiary = coloredOn(tertiary)
        val tertiaryContainer = reTone(p.tertiaryBase, targetL = 0.25f, satMul = 0.95f)
        val onTertiaryContainer = coloredOn(tertiaryContainer)

        val background = reTone(p.neutralBase, targetL = 0.07f)
        val onBackground = coloredOn(background)

        val surfaceDim = reTone(p.neutralBase, targetL = 0.06f)
        val surface = reTone(p.neutralBase, targetL = 0.08f)
        val surfaceBright = reTone(p.neutralBase, targetL = 0.13f)
        val surfaceContainerLowest = reTone(p.neutralBase, targetL = 0.04f)
        val surfaceContainerLow = reTone(p.neutralBase, targetL = 0.10f)
        val surfaceContainer = reTone(p.neutralBase, targetL = 0.12f)
        val surfaceContainerHigh = reTone(p.neutralBase, targetL = 0.14f)
        val surfaceContainerHighest = reTone(p.neutralBase, targetL = 0.18f)

        val onSurface = coloredOn(surface)
        val surfaceVariant = reTone(p.neutralVariantBase, targetL = 0.22f)
        val onSurfaceVariant = coloredOn(surfaceVariant)

        val outline = reTone(p.neutralVariantBase, targetL = 0.36f)
        val outlineVariant = reTone(p.neutralVariantBase, targetL = 0.28f)

        val inverseSurface = reTone(p.neutralBase, targetL = 0.96f)
        val inverseOnSurface = coloredOn(inverseSurface)
        val inversePrimary = reTone(p.primaryBase, targetL = 0.85f)

        val (error, errorContainer) = errorTone(light = false)
        val onError = coloredOn(error)
        val onErrorContainer = coloredOn(errorContainer)

        val surfaceTint = primary
        val scrim = Color(0xCC000000) // напівпрозорий чорний

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,

            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,

            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,

            background = background,
            onBackground = onBackground,

            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,

            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,

            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,

            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,

            surfaceBright = surfaceBright,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceDim = surfaceDim
        )
    } else {
        // LIGHT: Primary/Secondary/Tertiary — середні тони; Container — дуже світлі
        val primary = reTone(p.primaryBase, targetL = 0.42f)
        val onPrimary = coloredOn(primary)
        val primaryContainer = reTone(p.primaryBase, targetL = 0.92f, satMul = 0.95f)
        val onPrimaryContainer = coloredOn(primaryContainer)

        val secondary = reTone(p.secondaryBase, targetL = 0.45f)
        val onSecondary = coloredOn(secondary)
        val secondaryContainer = reTone(p.secondaryBase, targetL = 0.94f, satMul = 0.95f)
        val onSecondaryContainer = coloredOn(secondaryContainer)

        val tertiary = reTone(p.tertiaryBase, targetL = 0.45f)
        val onTertiary = coloredOn(tertiary)
        val tertiaryContainer = reTone(p.tertiaryBase, targetL = 0.94f, satMul = 0.95f)
        val onTertiaryContainer = coloredOn(tertiaryContainer)

        val background = reTone(p.neutralBase, targetL = 0.99f)
        val onBackground = coloredOn(background)

        val surfaceBright = reTone(p.neutralBase, targetL = 1.00f)
        val surface = reTone(p.neutralBase, targetL = 0.99f)
        val surfaceDim = reTone(p.neutralBase, targetL = 0.93f)
        val surfaceContainerLowest = reTone(p.neutralBase, targetL = 0.96f)
        val surfaceContainerLow = reTone(p.neutralBase, targetL = 0.94f)
        val surfaceContainer = reTone(p.neutralBase, targetL = 0.92f)
        val surfaceContainerHigh = reTone(p.neutralBase, targetL = 0.90f)
        val surfaceContainerHighest = reTone(p.neutralBase, targetL = 0.88f)

        val onSurface = coloredOn(surface)
        val surfaceVariant = reTone(p.neutralVariantBase, targetL = 0.90f)
        val onSurfaceVariant = coloredOn(surfaceVariant)

        val outline = reTone(p.neutralVariantBase, targetL = 0.60f)
        val outlineVariant = reTone(p.neutralVariantBase, targetL = 0.80f)

        val inverseSurface = reTone(p.neutralBase, targetL = 0.12f)
        val inverseOnSurface = coloredOn(inverseSurface)
        val inversePrimary = reTone(p.primaryBase, targetL = 0.30f)

        val (error, errorContainer) = errorTone(light = true)
        val onError = coloredOn(error)
        val onErrorContainer = coloredOn(errorContainer)

        val surfaceTint = primary
        val scrim = Color(0x99000000)

        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,

            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,

            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,

            background = background,
            onBackground = onBackground,

            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = surfaceTint,

            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,

            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,

            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,

            surfaceBright = surfaceBright,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceDim = surfaceDim
        )
    }
}
