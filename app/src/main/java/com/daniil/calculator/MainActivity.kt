package com.daniil.calculator

import android.app.Application
import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.daniil.calculator.calculatorscreen.CalculatorScreenModel
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.core.UserDataManager
import com.daniil.calculator.core.VersionRequest
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager
import com.daniil.calculator.settingsscreen.settingsInit
import com.daniil.calculator.ui.theme.CalculatorTheme
import com.daniil.calculator.ui.theme.getThemeMode
import com.daniil.csb.SettingsNavigationModel
import com.daniil.csb.SettingsProvider
import com.daniil.csb.classes.Select
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

val openScreen = mutableIntStateOf(0)
const val currentVersionCode = 7

var globalVersion: VersionRequest? = null

class MainActivity : AppCompatActivity() {
    private val calculatorScreenModel: CalculatorScreenModel by viewModels()
    val convertorScreenModel: ConvertorScreenModel by viewModels()
    private val settingsNavigationModel: SettingsNavigationModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            lifecycleScope.launch(Dispatchers.IO) {
                UserDataManager.loadUserData(this@MainActivity)
            }
            settingsInit(settingsNavigationModel, this@MainActivity, lifecycleScope)
        }

        setContent {
            val themeMode = getThemeMode()
            val language by SettingsProvider.getValue<Select.Option>("language").collectAsState()
            LaunchedEffect(language) { setLanguage() }

            CalculatorTheme(darkTheme = themeMode.value) {
                calculatorScreenModel.loadButtons(themeMode.value)
                MainNavHost(
                    calculatorScreenModel = calculatorScreenModel,
                    convertorScreenModel = convertorScreenModel,
                    settingsNavigationModel = settingsNavigationModel,
                )
            }

        }
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { load(null) }
        LogManager.k("OnResume started", "MainActivity fun \"load\" is started")
    }
    private fun getLanguage(): String {
        val systemLanguageCode = getSystemWideLocaleUniversal().language
        val languageSetting: Select = SettingsProvider.findById("language") as Select

        if (languageSetting.value.value.id == "System") {
            val languageIsExist = languageSetting.options.any { it.id == systemLanguageCode }
            return if (languageIsExist) systemLanguageCode else "en"
        }
        return languageSetting.value.value.id

    }

    private fun setLanguage() {
        val currentSetCode =
            this.resources.configuration.locales.get(0)?.language ?: Locale.getDefault().language
        val currentSelectCode = getLanguage()

        if (currentSelectCode == currentSetCode) return

        val localeList = LocaleListCompat.forLanguageTags(currentSelectCode)
        AppCompatDelegate.setApplicationLocales(localeList)

    }

    private fun getSystemWideLocaleUniversal(): Locale {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = this.getSystemService(LOCALE_SERVICE) as LocaleManager
            val systemLocales: LocaleList = localeManager.systemLocales
            return if (systemLocales.isEmpty) Locale.getDefault() else systemLocales.get(0)
        } else {
            return Locale.getDefault()
        }
    }


    private suspend fun load(savedInstanceState: Bundle?) = withContext(Dispatchers.IO) {
        if (savedInstanceState == null) {
            val context = this@MainActivity

            launch(Dispatchers.IO) {
                UserDataManager.active()
            }
            launch {
                calculatorScreenModel.loadCalck(context)
            }
            launch {
                convertorScreenModel.load(context, getLanguage())
            }
            launch(Dispatchers.IO) {
                LogManager.loadLogs(context)
                LogManager.k(
                    "MainActivity load complete",
                    "MainActivity fun \"load\" is COMPLETE"
                )
            }
        }
    }

    private suspend fun save() = withContext(Dispatchers.IO) {
        val context = this@MainActivity
        launch {
            convertorScreenModel.save()
        }
        launch(Dispatchers.IO) {
            calculatorScreenModel.saveClack(context)
        }
        launch(Dispatchers.IO) {
            LogManager.k("MainActivity save complete", "MainActivity fun \"save\" is COMPLETE")
            LogManager.saveLogs(context)
        }
        SettingsProvider.saveData(this@MainActivity)
    }

    override fun onPause() {
        LogManager.k("OnPause started", "MainActivity fun \"save\" is started")
        lifecycleScope.launch {
            save()
        }
        super.onPause()
    }

    override fun onStop() {
        LogManager.k("OnStop started", "MainActivity fun \"save\" is started")
        lifecycleScope.launch {
            launch(Dispatchers.IO) {
                UserDataManager.unactive()
            }
            save()
        }
        if (SettingsProvider.getValue<Boolean>("first_open_app").value) {
            SettingsProvider.setValue("first_open_app", false)
        }
        super.onStop()
    }

    override fun onDestroy() {
        calculatorScreenModel.sendReqestSessionOut()
        super.onDestroy()
    }
}


// for sendRequestSessionOut()
class MyApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}


