package com.daniil.calculator.settingsscreen.itemtype.customitem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSetting
import com.daniil.calculator.ui.theme.appLogoBackground

@Composable
fun AppLogo(
    setting: DynamicSetting,
    settingsScreenModel: SettingsScreenModel,
) {
    var clickCount by remember { mutableIntStateOf(0) }
    val size = 64.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(MaterialTheme.shapes.medium)
                .background(appLogoBackground)
                .clickable {
                    clickCount += 1
                    if (clickCount > 4) {
                        settingsScreenModel.goToCustomScreen(setting.id)
                        clickCount = 0
                    }
                },
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier
                    .size(size - 16.dp)
                    .padding(2.dp),
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "logo",
            )


        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.app_name),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}


