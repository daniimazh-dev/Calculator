package com.daniil.calculator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun exclusiveTheme(isDarkMode: Boolean): ColorScheme {
    if (isDarkMode) {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF115F86),              // світла бірюза на темному фоні
            onPrimary = Color(0xFF00334C),            // темно-синій текст
            primaryContainer = Color(0xFF004C6D),     // насичений темно-блакитний контейнер
            onPrimaryContainer = Color(0xFFB3E5FC),   // ніжно-блакитний текст

            inversePrimary = Color(0xFF40C4FF),       // яскравий акцент для світлих елементів

            secondary = Color(0xFF4DD0E1),            // холодна бірюза
            onSecondary = Color(0xFF002F36),
            secondaryContainer = Color(0xFF006874),
            onSecondaryContainer = Color(0xFFB2EBF2),

            tertiary = Color(0xFF90CAF9),             // м’який небесно-блакитний
            onTertiary = Color(0xFF1B3F65),
            tertiaryContainer = Color(0xFF00497B),
            onTertiaryContainer = Color(0xFFD0E3FF),

            background = Color(0xFF041721),           // глибокий темно-синій фон
            onBackground = Color(0xFFE0F7FA),         // світлий текст

            surface = Color(0xFF002233),              // темна поверхня з легким відтінком морської хвилі
            onSurface = Color(0xFFE1F5FE),            // чисто світлий текст

            surfaceVariant = Color(0xFF003C55),
            onSurfaceVariant = Color(0xFFB3E5FC),

            surfaceTint = Color(0xFF40C4FF),
            inverseSurface = Color(0xFFE3F2FD),
            inverseOnSurface = Color(0xFF001018),

            error = Color(0xFFFF8A80),
            onError = Color(0xFF400000),
            errorContainer = Color(0xFF8A1C1C),
            onErrorContainer = Color(0xFFFFCDD2),

            outline = Color(0xFF4DD0E1),
            outlineVariant = Color(0xFF006064),

            scrim = Color(0x99000000),                // темна напівпрозора маска

            surfaceBright = Color(0xFF01314A),
            surfaceDim = Color(0xFF001620),

            surfaceContainer = Color(0xFF002738),
            surfaceContainerLow = Color(0xFF00202E),
            surfaceContainerHigh = Color(0xFF003246),
            surfaceContainerLowest = Color(0xFF00141E),
            surfaceContainerHighest = Color(0xFF00485E)
        )


    } else {
        return MaterialTheme.colorScheme.copy(
            primary = Color(0xFF19A9E7),              // яскравий блакитний
            onPrimary = Color(0xFF3760A9),            // чисто білий текст на primary
            primaryContainer = Color(0xFF05628A),     // світлий контейнер
            onPrimaryContainer = Color(0xFF042742),   // темний контраст для тексту
            inversePrimary = Color(0xFF01579B),       // глибокий синьо-бірюзовий

            secondary = Color(0xFF00ACC1),            // насичений бірюзовий
            onSecondary = Color(0xFF6176DA),
            secondaryContainer = Color(0xFF088898),   // пастельна бірюза
            onSecondaryContainer = Color(0xFF004D55),

            tertiary = Color(0xFF82B1FF),             // ніжно-блакитний з фіолетовим відтінком
            onTertiary = Color(0xFF0561E8),
            tertiaryContainer = Color(0xFFD0E3FF),
            onTertiaryContainer = Color(0xFF002F6C),

            background = Color(0xFF66BAF8),           // м’який фон у відтінках неба
            onBackground = Color(0xFF002E4D),

            surface = Color(0xFF2D9DE7),              // біла поверхня з легким голубим
            onSurface = Color(0xFF00334C),

            surfaceVariant = Color(0xFF135370),
            onSurfaceVariant = Color(0xFF00496B),

            surfaceTint = Color(0xFF40C4FF),
            inverseSurface = Color(0xFF002C40),
            inverseOnSurface = Color(0xFFE0F7FA),

            error = Color(0xFFD32F2F),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFCDD2),
            onErrorContainer = Color(0xFF3B0000),

            outline = Color(0xFF0D2D31),
            outlineVariant = Color(0xFFB2EBF2),

            scrim = Color(0x66000000),                // напівпрозора затемнена маска

            surfaceBright = Color(0xFF4AB0D5),
            surfaceDim = Color(0xFF0B4F65),

            surfaceContainer = Color(0xFF21A2BE),
            surfaceContainerLow = Color(0xFF2D9DE7),
            surfaceContainerHigh = Color(0xFF1F84A4),
            surfaceContainerLowest = Color(0xFF1887B4),
            surfaceContainerHighest = Color(0xFF1AB0D9)
        )
    }

}