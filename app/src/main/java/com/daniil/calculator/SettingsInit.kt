package com.daniil.calculator

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.daniil.csb.SettingsNavigationModel
import com.daniil.csb.SettingsProvider
import com.daniil.csb.classes.MultiplySelect
import com.daniil.csb.classes.Select
import com.daniil.csb.classes.createColorPicker
import com.daniil.csb.classes.createInfo
import com.daniil.csb.classes.createMultiplySelect
import com.daniil.csb.classes.createRedirect
import com.daniil.csb.classes.createSelect
import com.daniil.csb.classes.createSlider
import com.daniil.csb.classes.createStringData
import com.daniil.csb.classes.createSwitch
import com.daniil.csb.screens.createAbstractScreen
import com.daniil.csb.screens.createCustomScreen
import com.daniil.csb.screens.createScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun settingsInit(
    settingsNavigationModel: SettingsNavigationModel,
    context: Context,
    coroutineScope: CoroutineScope
) {
    settingsNavigationModel.initialize(context)



//    settingsNavigationModel.setScreensHeap(
//        mainScreen, secondScreen, abstract, customScreen
//    )
    coroutineScope.launch(Dispatchers.IO) {
        SettingsProvider.loadData(context)
    }
}