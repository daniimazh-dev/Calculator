package com.daniil.calculator.settingsscreen.customscreen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.getCurrentVersion
import com.daniil.calculator.ui.theme.appLogoBackground
import com.daniil.calculator.ui.theme.appLogoBackground2
import com.daniil.calculator.ui.theme.appLogoBackgroundBeta
import com.daniil.csb.SettingsNavigationModel
import com.daniil.csb.classes.utils.ItemGroupPosition
import com.daniil.csb.screens.CustomScreen
import kotlinx.coroutines.delay


@Composable
fun CustomScreen.CustomScreenScope.AboutAppScreen(
    modifier: Modifier = Modifier,
    settingsNavigationModel: SettingsNavigationModel,
) {
    val context = LocalContext.current
    val isBeta = remember { getCurrentVersion(context).contains("Beta") }
    var transition by remember { mutableStateOf(false) }
    val animateBrash by animateFloatAsState(
        if (transition) 0.6f else 0.0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        )
    )
    val infiniteTransition = rememberInfiniteTransition()
    val colorOffset by infiniteTransition.animateColor(
        initialValue = appLogoBackground,
        targetValue = if (!isBeta) appLogoBackground2 else appLogoBackgroundBeta,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val brush = remember(animateBrash, colorOffset) {
        mutableStateOf(
            Brush.verticalGradient(
                colorStops = arrayOf(
                    animateBrash - 0.5f to colorOffset,
                    animateBrash to Color.Transparent
                )
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(150)
        transition = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush.value)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        var clickCount by remember { mutableIntStateOf(0) }
        val size = 64.dp
        ScreenTopBar(
            modifier = Modifier,
            navigationModel = settingsNavigationModel
        )
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
                            settingsNavigationModel
                                .goToScreen("ester_egg_screen")
                            clickCount = 0
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    modifier = Modifier
                        .size(size - 16.dp)
                        .padding(2.dp),
                    painter = if (!isBeta) painterResource(R.drawable.app_icon)
                    else painterResource(R.drawable.app_icon_beta),
                    contentDescription = "logo",
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (!isBeta) stringResource(R.string.app_name)
                else stringResource(R.string.app_name_beta),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        RenderSetting("new_version", itemGroupPosition = ItemGroupPosition.None)
        Spacer(modifier = Modifier.height(12.dp))
        RenderSetting("app_version", itemGroupPosition = ItemGroupPosition.First)
        Spacer(modifier = Modifier.height(4.dp))
        RenderSetting("about_developer", itemGroupPosition = ItemGroupPosition.Last)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Contacts",
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RenderSetting("server_contact", itemGroupPosition = ItemGroupPosition.First)
//            RenderSetting("gmail_contact", itemGroupPosition = ItemGroupPosition.Default)
            RenderSetting("tg_contact", itemGroupPosition = ItemGroupPosition.Default)
            RenderSetting("github_contact", itemGroupPosition = ItemGroupPosition.Last)
        }


    }
}