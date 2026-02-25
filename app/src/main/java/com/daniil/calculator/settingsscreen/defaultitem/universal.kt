package com.daniil.calculator.settingsscreen.defaultitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.settingContainer(
    enable: Boolean,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
): Modifier {
    return this.then(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = {
                    if (enable) onClick()
                },
                onLongClick = {
                    if (enable) onLongClick()
                }
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(if (enable) 1f else 0.6f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .alpha(if (enable) 1f else 0.5f)

    )
}
