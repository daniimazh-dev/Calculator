package com.daniil.calculator

import SettingsScreenController
import android.app.Activity
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowInsetsController
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.daniil.calculator.calculatorscreen.CalculatorScreenController
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.convertorscreen.ConvertorScreenController
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.settingsscreen.SettingsScreenModel
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.utilites.generatePatternBitmap
import com.daniil.calculator.utilites.imageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun MainNavHost(
    calculatorScreenModel: CalculatorScreenModel,
    convertorScreenModel: ConvertorScreenModel,
    settingsScreenModel: SettingsScreenModel,
) {
    val context = LocalContext.current

    var dragAmountX by remember { mutableFloatStateOf(0f) }
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val window = (context as? Activity)?.window

    UpdateVersionAlert()


    LaunchedEffect(isPortrait) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window?.insetsController
            if (controller != null) {
                if (!isPortrait) {
                    controller.hide(android.view.WindowInsets.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(android.view.WindowInsets.Type.systemBars())
                }
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    BackHandler(openScreen.intValue != 0) {
        openScreen.intValue -= 1
    }

    LaunchedEffect(openScreen.intValue) {
        pagerState.animateScrollToPage(
            page = openScreen.intValue,
            animationSpec = tween(400)
        )
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        convertorScreenModel.onHideScreen()
        if (!pagerState.isScrollInProgress) {
            openScreen.intValue = pagerState.currentPage
        }
    }

    val enableImage = DynamicSettingsManager.getValueState("background_image").value.toBoolean()

    var backgroundImage by remember(enableImage) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(enableImage) {
        if (!enableImage) return@LaunchedEffect
        launch(Dispatchers.IO) {
            val bytes = readImageFile(File(context.filesDir,"background_image.jpeg"))
            val bitmap = bytes?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            }
            backgroundImage = bitmap
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (enableImage) {
            backgroundImage?.let {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.7f),
                    bitmap = it,
                    contentDescription = "Background image",
                    contentScale = ContentScale.Crop
                )
            }

        } else {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = generatePatternBitmap(context, imageList, iconSize = 48).asImageBitmap(),
                contentDescription = "Background matrix"
            )
        }

        Scaffold(

            modifier = Modifier
                .fillMaxSize(),
            containerColor = if (!enableImage) MaterialTheme.colorScheme.background else Color.Transparent,
//            containerColor = Color.Transparent,
            topBar = {
                if (isPortrait) {
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors().copy(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (!enableImage) 1f else 0.7f)
                            ),
                            title = {
                                CustomTopAppBar(
                                    portraitMode = true,
                                    convertorScreenModel = convertorScreenModel
                                )
                            })
                        HorizontalDivider()
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->

            Row(
                modifier = Modifier.padding(innerPadding),

                ) {
                if (!isPortrait) {
                    CustomTopAppBar(
                        modifier = Modifier,
                        portraitMode = false,
                        convertorScreenModel = convertorScreenModel
                    )
                    VerticalDivider()
                }
                HorizontalPager(
                    state = pagerState
                ) { page ->
                    when (page) {
                        0 -> {
                            CalculatorScreenController(
                                calckScreenModel = calculatorScreenModel,
                                convertorScreenModel = convertorScreenModel,
                            )
                        }

                        1 -> {
                            ConvertorScreenController(convertorScreenModel = convertorScreenModel)

                        }

                        2 -> {
                            SettingsScreenController(settingsScreenModel = settingsScreenModel)
                        }
                    }
                }

            }


        }
    }
}


private fun readImageFile(file: File): ByteArray? {
    if (!file.exists()) return null
    return try {
        file.readBytes()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}