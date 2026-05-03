package com.daniil.calculator.settingsscreen.itemtype.customitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R


@Composable
fun AboutDeveloper(
    clickCount: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Developer", fontWeight = FontWeight.Bold)
            Text(
                text = "Daniil Mazhyrovsky",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (clickCount == 0) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.numeration_system_icon),
                contentDescription = "dev",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("$clickCount/5")
        }
        Spacer(modifier = Modifier.width(12.dp))

    }
}