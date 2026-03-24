package com.daniil.calculator.convertorscreen

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStack
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStackStorage
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.DefaultButtons
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorManager
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.registerCustomConvertors
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.core.ConvertorCore
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ConvertorScreenModel : ViewModel() {

    // -------------------- STATE --------------------
    // convertors
    private val _convertors =
        MutableStateFlow(mutableMapOf<String, MutableList<ConvertorData>>())

    // current convertor UI
    private val _currentConvertorScreen = MutableStateFlow<ConvertorData?>(null)

    private val _currentScreen = MutableStateFlow<ConvertorScreens>(ConvertorScreens.Home)


    // calculator buttons
    private val _calckButtons = MutableStateFlow<ButtonsStack>(ButtonsStack(buttons = emptyList()))

    // calculator expression
    private val _currentCalckBlock = MutableStateFlow("0")

    private val _currentUnit = MutableStateFlow<ConvertorUnit>(NullableUnit)

    // next change calkBlock erase expression
    var clearNextInput = false

    // core
    lateinit var convertorCore: ConvertorCore

    // custom convertor UI manager
    val customConvertorManager = CustomConvertorManager(this)

    // parameter
    val convertorParametersStore = ConvertorParametersStore()

    var currentParameters = MutableStateFlow<List<Parameter>>(emptyList())
        private set

    val scrollState = LazyListState()

    // convertor panel UI mode
    val viewConvertorMode = MutableStateFlow<String?>(null)

    val reportErrorSheetShow = MutableStateFlow(false)
    var componentOffset = MutableStateFlow(0f)


    val convertors = _convertors.asStateFlow()
    val calckButtons = _calckButtons.asStateFlow()
    val calckBlock = _currentCalckBlock.asStateFlow()
    val currentConvertor = _currentConvertorScreen.asStateFlow()
    val currentScreen = _currentScreen.asStateFlow()
    val currentUnit = _currentUnit.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val units = currentConvertor.flatMapLatest { convertor ->
        convertor?.let { convertorCore.getUnitsFlow(it.id) } ?: flowOf(mutableListOf())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = mutableListOf()
    )
    // -------------------- INIT --------------------
    init {
        LogManager.c("ViewModel START", content = "ConvertorViewModel start init")
    }

    suspend fun load(
        context: Context,
        locale: String
    ) = withContext(Dispatchers.IO) {
        convertorCore = ConvertorCore(this@ConvertorScreenModel, locale)
        LogManager.i("ViewModel load", content = "ConvertorViewModel fun \"load\" is started. Load data")
        convertorCore.load(context)
        _convertors.value = convertorCore.getConvertorsMap()
        launch {
            convertorCore.checkConvertorsRelease()
            _convertors.value = convertorCore.getConvertorsMap()
        }
        LogManager.c("ViewModel load complete", content = "ConvertorViewModel fun \"load\" is complete")

    }
    @Composable
    fun RegisterCustomConvertor() {
        LogManager.i("ViewModel load", content = "ConvertorViewModel fun \"registerCustomConvertor\" is started. Register custom convertors")
        registerCustomConvertors(this)
        LogManager.c("ViewModel complete", content = "Register custom convertors is COMPLETE")
    }

    suspend fun save() {
        convertorCore.saveUnitData(convertorCore.getAllUnits())
        convertorCore.saveConvertorsData(_convertors.value)
    }

    private fun loadParameters() {
        val currentConvertor = _currentConvertorScreen.value ?: return
        convertorParametersStore.setAllOf(currentConvertor.id, currentConvertor.saveParameters)
        currentParameters.value = currentConvertor.saveParameters
    }

    suspend fun saveConvertorButtonsData() {
        convertorCore.saveConvertorsData(_convertors.value)
    }


    private suspend fun saveConvertorData() {
        val currentConvertor = _currentConvertorScreen.value ?: return
        updateButtonData(
            currentConvertor.copy(
                calckBlock = calckBlock.value,
                currentViewMode = viewConvertorMode.value,
                startUnit = currentUnit.value,
                saveParameters = currentParameters.value
            )
        )
        convertorCore.saveConvertorsData(_convertors.value)
    }

    // -------------------- NAVIGATION --------------------

    fun goToHome() {
        val currentConvertor = _currentConvertorScreen.value ?: run {
            _currentScreen.value = ConvertorScreens.Home
            return
        }
        _currentScreen.value = ConvertorScreens.Home
        customConvertorManager.getImplementation(currentConvertor.id)?.buildResult?.runtime?.onClose()

        viewModelScope.launch {
            saveConvertorData()
            if (currentConvertor.favorite) {
                scrollState.scrollToItem(0)
            } else {
                val visible = scrollState.layoutInfo.visibleItemsInfo
                val range =
                    if (visible.isNotEmpty()) visible.first().index..visible.last().index else 0..0
                val itemIndex = (_convertors.value.values.indexOfFirst { list ->
                    list.any { it.id == currentConvertor.id }
                } * 2 + 1).coerceAtLeast(0)
                if (itemIndex !in range) scrollState.scrollToItem(itemIndex)
            }
            delay(20)
            _currentConvertorScreen.value = null
        }

    }


    fun onHideScreen() {
        viewModelScope.launch {
            saveConvertorData()
        }
        customConvertorManager.getImplementation(_currentConvertorScreen.value?.id ?: return)
            ?.buildResult?.runtime?.onHide()

    }

    fun goToConvertor(buttonId: String, customCalckBlock: String? = null) {
        val convertorButtonData =
            getConvertorData(buttonId)

        val customConvertor = customConvertorManager.getCustomConvertor(buttonId)

        if (convertorButtonData != null) {
            _currentScreen.value = ConvertorScreens.Convertor
            _currentConvertorScreen.value = convertorButtonData


            viewConvertorMode.value = convertorButtonData.currentViewMode
                ?: customConvertor?.convertorScreen?.startViewModeId

            _currentUnit.value =
                convertorCore.getUnit(convertorButtonData.id, convertorButtonData.startUnit.name) // for Language
                ?: convertorButtonData.startUnit

            loadParameters()
            viewModelScope.launch {
                val validate = validateValue(customCalckBlock)
                if (customCalckBlock != null) {
                    if (validate.first) {
                        setCalck(customCalckBlock)
                    } else {
                        LogManager.w("ValidationValue", "Exception: " + validate.second)
                    }
                } else {
                    setCalck(convertorButtonData.calckBlock)
                }
                customConvertorManager.getImplementation(convertorButtonData.id)?.buildResult?.runtime?.onStart()
                setButtons(currentUnit.value, convertorButtonData)
            }
        }


    }


    fun setFavoriteButton(
        favorite: Boolean,
        convertorData: ConvertorData? = _currentConvertorScreen.value,
    ) {
        _currentConvertorScreen.value?.let {
            _currentConvertorScreen.value = convertorData?.copy(favorite = favorite)
        }
        convertorData?.let {
            updateButtonData(convertorData.copy(favorite = favorite))
        }
    }

    // -------------------- CALCULATOR LOGIC --------------------

    fun setCalck(string: String) {
        val string = CalculatorCore.unformatENumber(string) ?: string
        _currentCalckBlock.value = string
    }

    fun addCalck(newString: String) {
        if (clearNextInput) {
            _currentCalckBlock.value = ""
            clearNextInput = false
        }
        _currentCalckBlock.value = when (_currentCalckBlock.value) {
            "0", "Error", ")" -> newString
            else -> _currentCalckBlock.value + newString
        }
    }

    fun removeLastCalck() {
        clearNextInput = false
        val oldValue = _currentCalckBlock.value
        _currentCalckBlock.value = when {
            oldValue == "Error" || oldValue == "0" -> "0"
            oldValue.length == 1 -> "0"
            else -> oldValue.dropLast(1)
        }
    }

    // -------------------- CONVERTER LOGIC --------------------

    fun setButtons(
        unit: ConvertorUnit?,
        convertorData: ConvertorData = _currentConvertorScreen.value!!,
    ) {
        val customScreen = customConvertorManager.getImplementation(convertorData.id)?.buildResult?.convertorScreen
//        val firstKey = customScreen?.viewScreens?.keys?.first()?.id
        val buttons = customConvertorManager.getButtons(
            id = convertorData.id,
            unit = unit ?: _currentUnit.value,
            mode = viewConvertorMode.value ?: customScreen?.startViewModeId ?: "Comparison"
        )
        _calckButtons.value = buttons ?: ButtonsStackStorage(this).DefaultButtons
    }

    fun setCurrentUnit(
        unit: ConvertorUnit,
        clearInput: Boolean = false,
        calckBlock: String? = null,
    ) {
        val currentConvertor = _currentConvertorScreen.value ?: return
        calckBlock?.let { setCalck(it) }

        setButtons(unit, currentConvertor)
        if (clearInput) clearNextInput = true

        _currentUnit.value = unit
    }

    fun handleButtonClick(content: String) {
        when (content) {
            "delete" -> removeLastCalck()
            "." -> if (
                !_currentCalckBlock.value.contains(".")
                || currentConvertor.value?.id == "IP_calculator"
                ) _currentCalckBlock.value += "."
            else -> addCalck(content)
        }
    }




    fun saveParameters(
        convertorName: String? = _currentConvertorScreen.value?.id,
        data: ParameterBuilder.() -> Unit,
    ) {
        convertorName?.let {
            val screen = _currentConvertorScreen.value ?: return
            convertorParametersStore.setParameter(convertorName, data)
            currentParameters.value = convertorParametersStore.getAllOf(screen.id) ?: listOf()
        }
    }

    @Composable
    inline fun <reified T> getParameter(
        key: String,
        defaultValue: T,
    ): Any? {
        val currentParameters by currentParameters.collectAsState()
        val parameter = currentParameters.find { it.key == key }
        if (parameter?.data == null) return defaultValue
        return if (parameter.isJson) Json.decodeFromString<T>(parameter.data) else parameter.data
    }


    inline fun <reified T> getParameter(
        key: String,
        saveParameters: List<Parameter>?,
        defaultValue: T,
    ): Any? {
        val parameter = saveParameters?.find { it.key == key }
        if (parameter?.data == null) return defaultValue
        return if (parameter.isJson) Json.decodeFromString<T>(parameter.data) else parameter.data
    }

    fun clearParam(
        convertorName: String? = _currentConvertorScreen.value?.id,
        key: String? = null,
    ) {
        val convertorName = convertorName ?: _currentConvertorScreen.value?.id ?: return

        if (key != null) {
            convertorParametersStore.deleteParameter(convertorName, key)
        } else {
            convertorParametersStore.clearAllParameter(convertorName)
        }
        currentParameters.value = convertorParametersStore.getAllOf(convertorName) ?: listOf()


    }


    private fun updateButtonData(updated: ConvertorData) {
        val entry =
            _convertors.value.entries.firstOrNull { (_, list) ->
                list.any { it.id == updated.id }
            } ?: return

        val list = entry.value
        val index = list.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            val new = _convertors.value[entry.key] ?: return
            new[index] = updated
            _convertors.value[entry.key] = new
        }

    }


    // -------------------- LOGIC HELPERS --------------------
    fun getConvertorData(id: String): ConvertorData? =
        _convertors.value.values.flatten().find { it.id == id }


}
