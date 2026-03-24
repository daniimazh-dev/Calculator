package com.daniil.calculator.convertorscreen.convertor.convertorpanel.register

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.res.stringResource
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStack
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStackStorage
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.DefaultButtons
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnitJson
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.universal.UniversalDropDownItem


class CustomConvertorManager(
    val convertorScreenModel: ConvertorScreenModel,
) {
    private val implementations = mutableMapOf<String, CustomConvertorImplementation>()

    @SuppressLint("ComposableNaming")
    @Composable
    fun registerImplementation(
        id: String,
        impl: CustomConvertorImplementation,
    ) {
        if (implementations[id] == null) {
            impl.onCreate()
            impl.build()
            implementations[id] = impl
        }
    }
    @Composable
    fun localImplementation(impl: CustomConvertorImplementation): CustomConvertor {
        impl.onCreate()
        return impl.build()
    }

    fun getButtons(id: String, mode: String, unit: ConvertorUnit): ButtonsStack? {
        implementations[id]?.let { impl ->
            return if (impl.getButtonsPanel(mode)?.showKeyboard?.value == true) {
                implementations[id]?.getButtons(mode, unit)
            } else ButtonsStack(4, emptyList())
        }
        return null
    }

    fun getImplementation(id: String): CustomConvertorImplementation? {
        return implementations[id]
    }


    fun getCustomConvertor(id: String): CustomConvertor? {
        Log.i("MyLog", "ID:$id, BuildResult:${implementations[id]?.buildResult}")
        return implementations[id]?.buildResult
    }


    @Composable
    fun getRender(id: String, mode: String): CustomScreenRender? {
        val renderPack = implementations[id]?.getCustomScreen(mode)
        return renderPack?.render
    }
}

interface ConvertorInterface {
    fun onCreate()
    suspend fun onStart() {}
    fun onClose() {}
    fun onHide() {}
}

open class CustomConvertorImplementation(
    val convertorData: ConvertorData,
    val convertorScreenModel: ConvertorScreenModel,
) : ConvertorInterface {
    private var screen: @Composable (ConvertorConstructorScope.() -> Unit)? = null
    var buildResult: CustomConvertor? = null

    fun setContent(
        content: @Composable ConvertorConstructorScope.() -> Unit
    ) {
        screen = content
    }

    @Composable
    internal fun build(): CustomConvertor {
        val constructor = ConvertorConstructorScope(convertorData, getUnits(), convertorScreenModel)
        val scope = screen ?: error("setContent() must be initialize before launching onStart()")
        constructor.scope()
        val build = constructor.build()
        val customConvertor = CustomConvertor(
            convertorScreen = build,
            runtime = CustomConvertorRuntime(
                onStart = { onStart() },
                onClose = { onClose() },
                onHide = { onHide() }
            )
        )
        buildResult = customConvertor
        return customConvertor
    }

    fun getCustomScreen(mode: String): CustomScreen? {
        val viewScreens = buildResult?.convertorScreen?.viewScreens ?: return null
        val key = viewScreens.keys.find { it.id == mode } ?: return null
        return viewScreens[key]
    }

    fun getButtonsPanel(mode: String): CustomButtonPanel? {
        return getCustomScreen(mode)?.butonPanel
    }
    fun getButtons(mode: String): Map<ConvertorUnit, ButtonsStack>? {
        return getButtonsPanel(mode)?.customButtons
    }
    fun getButtons(mode: String, unit: ConvertorUnit): ButtonsStack? {
        return getButtons(mode)?.get(unit)
    }
    @Composable
    fun getUnitsAsSate() = convertorScreenModel.units

    fun getUnits() = convertorScreenModel.convertorCore.getUnits(convertorData.id)

    fun getStartUnits() = convertorScreenModel.convertorCore.getStartUnit(convertorData.id)
    fun setUnit(unitList: List<ConvertorUnitJson>) {
        convertorScreenModel.convertorCore.setUnits(convertorData.id, unitList)
    }

    fun addToUnits(unit: ConvertorUnitJson) {
        convertorScreenModel.convertorCore.addToUnits(convertorData.id, unit)
    }

    override fun onCreate() {}


}

// ---------------------------------------------------------
//                       BUTTON PANEL
// ---------------------------------------------------------


class ConvertorConstructorScope(
    val convertorData: ConvertorData,
    val units: List<ConvertorUnit>,
    private val convertorScreenModel: ConvertorScreenModel,
) {
    val viewScreens = mutableMapOf<ViewMode, CustomScreen>()
    var startViewMode: String? = null
    var singleViewMode = false


    @Composable
    fun mode(
        id: String,
        painterId: Int,
        name: String = id,
        builder: @Composable ScreenBuilder.() -> Unit
    ) {
        if (startViewMode == null) startViewMode = id
        val screenBuilder = ScreenBuilder(
            screenId = id,
            convertorData = convertorData,
            convertorScreenModel = convertorScreenModel,
            units = units
        )
        screenBuilder.builder()
        val customScreen = screenBuilder.build()
        val viewMode = ViewMode(id = id, name = name, painterId = painterId)
        viewScreens[viewMode] = customScreen
    }


    fun build(): ConvertorScreen {
        return ConvertorScreen(
            singleViewMode = singleViewMode,
            startViewModeId = startViewMode,
            viewScreens = viewScreens
        )
    }

}

open class ScreenBuilder internal constructor(
    val screenId: String,
    val units: List<ConvertorUnit>,
    val convertorData: ConvertorData,
    val convertorScreenModel: ConvertorScreenModel,
) {
    var description: (() -> ConvertorDescription?)? = null
    var showClackPanel = mutableStateOf(true)
    var dropdownMenu = mutableStateListOf<UniversalDropDownItem>()
    var content: CustomScreenRender = {}

    var column = 4
    var showKeyboard = mutableStateOf(true)
    val defaultButtons = ButtonsStackStorage(convertorScreenModel).DefaultButtons
    private var customButtons: MutableMap<ConvertorUnit, ButtonsStack>? = null


    private fun defaultButtons(): Map<ConvertorUnit, ButtonsStack> {
        return units.associateWith { defaultButtons }
    }
    fun buttonsToUnit(
        unit: ConvertorUnit,
        buttonStack: ButtonsStack,
        column: Int = this.column
    ) {
        if (customButtons == null) customButtons = mutableMapOf()
        customButtons?.set(unit, buttonStack.copy(column = column))
    }
    fun buttonMap(
        map: Map<ConvertorUnit, ButtonsStack>,
    ) {
        customButtons = map.toMutableMap()
    }

    fun allButtons(
        buttonStack: ButtonsStack,
        column: Int = this.column
    ) {
        units.forEach {
            buttonsToUnit(it, buttonStack, column)
        }
    }

    @Composable
    internal fun build(): CustomScreen {
        return CustomScreen(
            id = screenId,
            description = description,
            customDropdownMenu = dropdownMenu.ifEmpty { defaultDropdownMenu().toMutableStateList() },
            showCalckPanel = showClackPanel,
            render = content,
            butonPanel = CustomButtonPanel(
                customButtons = customButtons ?: defaultButtons(),
                showKeyboard = showKeyboard
            ),
        )
    }
}


@Composable
fun ScreenBuilder.defaultDropdownMenu(): List<UniversalDropDownItem> {
    return listOf(
        UniversalDropDownItem(
            title = stringResource(R.string.report),
            iconResource = R.drawable.report_icon,
            onClick = {
                convertorScreenModel.reportErrorSheetShow.value = true
            }
        ),
        UniversalDropDownItem(
            title = stringResource(R.string.clear_param),
            iconResource = R.drawable.delete_icon,
            onClick = {
                convertorScreenModel.clearParam(convertorData.id, null)
                convertorScreenModel.setCalck("0")
                convertorScreenModel.convertorCore.clearSavedUnit(convertorData.id)
                convertorScreenModel.goToHome()
            }
        ),
    )
}


data class CustomConvertor(
    val convertorScreen: ConvertorScreen,
    val runtime: CustomConvertorRuntime,
)


data class CustomScreen(
    val id: String,
    val render: CustomScreenRender,
    val showCalckPanel: State<Boolean>,
    val description: (() -> ConvertorDescription?)?,
    val customDropdownMenu: SnapshotStateList<UniversalDropDownItem>,
    val butonPanel: CustomButtonPanel,
)

data class ConvertorScreen(
    val singleViewMode: Boolean,
    val startViewModeId: String?,
    val viewScreens: Map<ViewMode, CustomScreen>,
)

fun Map<ViewMode, CustomScreen>.findOfId(mode: String?): CustomScreen? {
    val keys = this.keys
    val key =  keys.find { it.id == mode } ?: this.keys.first()
    return this.getOrElse(key) { null }
}


data class ConvertorDescription(
    val closeable: Boolean = true,
    val content: (@Composable () -> Unit)?,
)

data class ViewMode(
    val id: String,
    val name: String, // With locale
    val painterId: Int,
)


data class CustomConvertorRuntime(
    val onStart: suspend () -> Unit,
    val onClose: () -> Unit,
    val onHide: () -> Unit,
)


data class CustomButtonPanel(
    val customButtons: Map<ConvertorUnit, ButtonsStack>,
    val showKeyboard: State<Boolean>,
)


private typealias CustomScreenRender = @Composable () -> Unit
