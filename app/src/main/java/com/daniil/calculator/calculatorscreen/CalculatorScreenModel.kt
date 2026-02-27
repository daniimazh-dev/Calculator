package com.daniil.calculator.calculatorscreen

import android.app.Application
import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.daniil.calculator.MyApplication
import com.daniil.calculator.R
import com.daniil.calculator.calculatorscreen.buttons.collectCalckButton
import com.daniil.calculator.calculatorscreen.history.HistoryManager
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonUi
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.core.UserDataManager
import com.daniil.calculator.settingsscreen.settings.manager.DynamicSettingsManager
import com.daniil.calculator.universal.ButtonData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalculatorScreenModel(application: Application) : AndroidViewModel(application) {

    val tokenRegex =
        MutableStateFlow<Regex>(Regex("Error+|[0-9.E]+|[+\\-*/%!^√]+|[()|]+|sin+|cos+|tan+|log"))

    private val _buttons = MutableStateFlow(mutableStateListOf<ButtonData>())
    var emptyButtons = listOf<ButtonData>()

    private val _buttonPanelExpandedMode = MutableStateFlow(false)
    private val _calckBlock = MutableStateFlow("0")
    private val _predictiveCalckBlock = MutableStateFlow<String?>("0")
    private val _errorCalculate = MutableStateFlow(false)
    private val _calculateButtonClick = MutableStateFlow(false)
    private val _currentScreen = MutableStateFlow(CalculatorScreensRoute.Calculator)

    private val _replaceTokenIndex = MutableStateFlow<Int?>(null)

    val calckHistory = HistoryManager()
    val resultAnimate = MutableStateFlow(false)
    val historyScrollState = LazyListState()

    var componentOffset = MutableStateFlow(0f)

    var waveEffect = MutableStateFlow(false)

    val calckBlock: StateFlow<String> = _calckBlock
    val predictiveCalckBlock: StateFlow<String?> = _predictiveCalckBlock
    val errorCalculate: StateFlow<Boolean> = _errorCalculate
    val buttonPanelExpandedMode: StateFlow<Boolean> = _buttonPanelExpandedMode
    val buttons: StateFlow<SnapshotStateList<ButtonData>> = _buttons
    val calculateButtonClick: StateFlow<Boolean> = _calculateButtonClick
    val replaceTokenIndex: StateFlow<Int?> = _replaceTokenIndex
    val currentScreen: StateFlow<CalculatorScreensRoute> = _currentScreen

    // ---------------- Screens ----------------
    fun goToScree(screen: CalculatorScreensRoute) {
        _currentScreen.value = screen
    }

    // ---------------- Токенізація ----------------
    private fun tokenize(expr: String): MutableList<String> =
        tokenRegex.value.findAll(expr).map { it.value }.toMutableList()

    private fun rebuild(tokens: List<String>): String =
        tokens.joinToString("")

    private fun setExpressionFromTokens(tokens: List<String>) {
        _calckBlock.value = if (tokens.isEmpty()) "0" else rebuild(tokens)
        predictiveCalck()
    }

    fun getTokensForUI(): List<String> {
        return tokenize(_calckBlock.value).map {
            it.replace('/', '÷')
                .replace('*', '×')

        }
    }

    fun getTokens() = tokenize(_calckBlock.value)


    // ---------------- Збереження / Завантаження ----------------


    fun sendReqestSessionOut() {
        val appScope = (getApplication<MyApplication>()).applicationScope
        appScope.launch {
            try {
                UserDataManager.sessionOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun loadCalck(context: Context) {
        viewModelScope.launch { calckHistory.load(context) }
        val sharedPref = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        setCalck(sharedPref.getString("calck_block", null) ?: "0")
    }

    fun saveClack(context: Context) {
        viewModelScope.launch { calckHistory.save(context) }

        val sharedPref = context.getSharedPreferences("saves", Context.MODE_PRIVATE)
        sharedPref.edit(commit = true) {
            putString("calck_block", _calckBlock.value)
        }
    }


    fun loadButtons(isDarkMode: Boolean) {
        emptyButtons = collectCalckButton(isDarkMode)
        val filledButtons = emptyButtons.map { button ->
            button.copy(
                onClick = {
                    when (button.content) {
                        "C" -> clearCalck()
                        "delete" -> setCalck(removeLastCalck())
                        "=" -> calculateCalck()
                        "π" -> addCalck("3.14")
                        "|x|" -> addCalck("|")
                        "more/less" -> expandedButtonPanelMode(!_buttonPanelExpandedMode.value)
                        else -> addCalck(button.content)
                    }
                },
                onPressed = {
                    if (button.content == "delete") setCalck(removeLastCalck())
                }
            )
        }
        _buttons.value = filledButtons.toMutableStateList()
    }

    // ---------------- Основна логіка ----------------

    private val operatorRegex = Regex("[+\\-*/%!^]")
    private val functionRegex = Regex("sin|cos|tan|log|√")
    private val numberRegex = Regex("[0-9.E]+")

    fun setCalck(string: String, animateResult: Boolean = false) {
        val string = CalculatorCore.unformatENumber(string) ?: string
        if (animateResult) animateChangeResult(string)
        _calckBlock.value = string
        predictiveCalck()
    }

    fun setCalck(stringList: List<String>, animateResult: Boolean = false) {
        val join = stringList.joinToString()
        val string = CalculatorCore.unformatENumber(join) ?: join
        if (animateResult) animateChangeResult(string)
        setExpressionFromTokens(stringList)
        predictiveCalck()
    }

    fun addCalck(input: String) {
        var tokens = getTokens()


        if (_replaceTokenIndex.value != null) {
            val index = _replaceTokenIndex.value!!
            if (index in tokens.indices) {
                tokens = when {
                    input == "( )" -> handleBracket()
                    input.matches(operatorRegex) -> handleOperator(input)
                    input.matches(functionRegex) -> handleFunction(input)
                    else -> handleNumber(input)
                }.toMutableList()
            }
        } else {

            tokens = when {
                input == "." -> handleDot()
                input.matches(operatorRegex) -> handleOperator(input)
                input.matches(functionRegex) -> handleFunction(input)
                else -> handleNumber(input)
            }.toMutableList()
        }

        setExpressionFromTokens(tokens)
        predictiveCalck()
    }

    fun checkStartToken(): Boolean {
        val tokens = getTokens().toMutableList()
        return if (tokens.size == 1 || tokens.isEmpty()) {
            when {
                tokens[0] == "0" -> true
                tokens[0] == "Error" -> true
                else -> false
            }
        } else false
    }

    private fun handleNumber(input: String): List<String> {
        var tokens = getTokens().toMutableList()
        if (checkStartToken()) tokens = mutableListOf()
        if (_replaceTokenIndex.value != null) {
            if (tokens.isEmpty()) tokens = mutableListOf("0")
            val index = _replaceTokenIndex.value!!
            if (tokens[index] == "0") {
                tokens[index] = input
            } else {
                tokens[index] += input
            }

            return tokens
        } else {

            if (tokens.isEmpty() || !tokens.last().matches(numberRegex)) {
                tokens.add(input)
            } else {
                tokens[tokens.lastIndex] += input
            }
            return tokens
        }
    }

    fun handleFunction(input: String): List<String> {
        val tokens = getTokens().toMutableList()
        val index = _replaceTokenIndex.value ?: tokens.lastIndex
        val inputResult = "$input("
        if (checkStartToken()) return listOf(inputResult)

        if (
            _replaceTokenIndex.value == null
            && tokens[index] != "("
            && !tokens[index].matches(operatorRegex)
        ) {
            tokens.add("*")
        }

        when {
            tokens[index].matches(functionRegex) -> {
                if (input == tokens[index] && _replaceTokenIndex.value == null) {
                    tokens.add("$input(")
                } else {
                    if (tokens.getOrNull(index + 1) == "(") tokens.removeAt(index + 1)
                    tokens.removeAt(index)
                    tokens.add(index, "$input(")
                }

            }

            tokens[index] == "(" -> {
                if (tokens[tokens.lastIndex - 1].matches(functionRegex)) {
                    if (input == tokens[tokens.lastIndex - 1] && _replaceTokenIndex.value == null) {
                        tokens.add("$input(")
                    } else {
                        tokens.removeAt(index)
                        tokens.removeAt(index - 1)
                        tokens.add(index - 1, "$input(")
                    }
                }

            }

            else -> tokens.add(inputResult)
        }
        return tokens
    }

    fun removeFunction(): List<String> {
        val tokens = getTokens().toMutableList()
        val index = _replaceTokenIndex.value ?: tokens.lastIndex
        when {
            tokens[index].matches(functionRegex) -> {
                tokens.removeAt(index)
            }

            tokens[index] == "(" -> {
                if (tokens[tokens.lastIndex - 1].matches(functionRegex)) {
                    tokens.removeAt(index)
                    tokens.removeAt(index - 1)
                }

            }
        }
        return tokens
    }

    private fun handleDot(): List<String> {
        val tokens = getTokens().toMutableList()
        if (tokens.isEmpty() || !tokens.last().matches(numberRegex)) {
            tokens.add("0.")
        } else if (!tokens.last().contains('.')) {
            tokens[tokens.lastIndex] += "."
        }
        return tokens
    }

    private fun handleOperator(input: String): List<String> {
        val tokens = getTokens().toMutableList()
        val tokenIndex = _replaceTokenIndex.value ?: tokens.size

        if (tokens.isEmpty()) {
            tokens.add("0")
            tokens.add(input)

        } else if (tokenIndex in tokens.indices) {
            val current = tokens[tokenIndex]
            if (current.matches(operatorRegex)) {
                tokens[tokenIndex] = input
            } else {
//                tokens.add(tokenIndex, input)
            }
        } else {
            val last = tokens.lastOrNull()
            if (last != null && last.matches(operatorRegex)) {
                tokens[tokens.lastIndex] = input
            } else {
                tokens.add(input)
            }
        }

        return tokens
    }

    private fun handleBracket(): List<String> {
        val tokens = getTokens().toMutableList()


        val lastToken = tokens.lastOrNull()
        if (checkStartToken()) return mutableListOf("(")
        if (tokens.findLast { it.matches(operatorRegex) || it == "(" } != null && lastToken != "(") {
            tokens.add(")")
        } else {
            if (lastToken?.matches(numberRegex) == true) {
                tokens.add("*")
            }
            tokens.add("(")
        }
        return tokens
    }

    fun removeLastCalck(): List<String> {
        var tokens = getTokens().toMutableList()
        if (_replaceTokenIndex.value != null) {

            val index = _replaceTokenIndex.value!!
            if (index in tokens.indices) {
                val currentToken = tokens[index]
                when {
                    currentToken.length > 1 -> {
                        tokens[index] = currentToken.dropLast(1)
                    }

                    else -> if (currentToken.isDigitsOnly()) tokens[index] = "0"
                }
            }
        } else {
            if (checkStartToken()) return tokens
            val last = tokens.last()
            try {
                when {
                    (last == "(" && tokens.getOrNull(tokens.lastIndex - 1)
                        ?.matches(functionRegex) == true) ->
                        tokens = removeFunction().toMutableList()

                    last.length > 1 -> tokens[tokens.lastIndex] = last.dropLast(1)
                    else -> tokens.removeAt(tokens.lastIndex)
                }
            } catch (e: IndexOutOfBoundsException) {
//                e.printStackTrace()
                tokens = mutableListOf("0")
            }

        }

        if (tokens.isEmpty()) tokens.add("0")
        return tokens
    }

    fun clearCalck() {
        val tokens = getTokens()
        val replaceTokenIndex = _replaceTokenIndex.value
        if (replaceTokenIndex == null) {
            tokens.clear()
            tokens.add("0")
            _calckBlock.value = "0"
            _errorCalculate.value = false
            _predictiveCalckBlock.value = "0"
        } else {
            tokens[replaceTokenIndex] = "0"
        }
        setExpressionFromTokens(tokens)
        predictiveCalck()
    }

    // ---------------- Робота з токенами ----------------

    fun setToken(index: Int, newValue: String) {
        val tokens = getTokens().toMutableList()
        if (index in tokens.indices) {
            tokens[index] = newValue
            setExpressionFromTokens(tokens)
        }
    }

    fun dropToken(index: Int) {
        val tokens = getTokens().toMutableList()
        if (index in tokens.indices) {
            tokens.removeAt(index)
            setExpressionFromTokens(tokens)
        }
    }

    fun setReplaceToken(index: Int) {
        if (index in getTokens().indices) {
            _replaceTokenIndex.value = index
            val idx = _buttons.value.indexOfFirst { it.id == "=" }
            if (idx != -1) {
                _buttons.value[idx] = ButtonData(
                    content = "ok",
                    id = "=",
                    onClick = { dropReplaceToken() },
                    type = ButtonUi.Special,
                    painterIcon = R.drawable.check_circle_icon
                )
            }
        }
    }

    fun dropReplaceToken() {
        _replaceTokenIndex.value = null
        val idx = _buttons.value.indexOfFirst { it.id == "=" }
        if (idx != -1) {
            _buttons.value[idx] = ButtonData(
                content = "=",
                id = "=",
                onClick = { calculateCalck() },
                type = ButtonUi.Special
            )
        }
    }

    fun predictiveCalck() {
        val expr = _calckBlock.value
        val result = CalculatorCore.evaluate(expr)
        if (result != null) _predictiveCalckBlock.value = result
        _errorCalculate.value = result == null
    }

    fun calculateCalck() {
        _calculateButtonClick.value = true
        viewModelScope.launch {
            val result = CalculatorCore.evaluate(_calckBlock.value) ?: "Error"
            if (predictiveCalckBlock.value == "67" && calckBlock.value == "67") {
                waveEffect.value = !waveEffect.value
            } else {
                if (DynamicSettingsManager.getValue("save_history").toBoolean()) {
                    calckHistory.addHistory(_calckBlock.value, result)
                }
            }

            animateChangeResult(result)
            delay(10)
            _calculateButtonClick.value = false

        }
    }

    fun validateCalckBlockValue(value: String?): Pair<Boolean, String?> {
        return true to tokenize(value.orEmpty()).joinToString("")

//        if (value?.matches(tokenRegex.value) == false) return (false to "Invalid characters")
//        return (true to null)
    }

    private fun animateChangeResult(result: String) {
        resultAnimate.value = true
        viewModelScope.launch {
            _predictiveCalckBlock.value = result
            delay(300)
            _calckBlock.value = result
            resultAnimate.value = false
        }
    }

    // ---------------- Панель ----------------
    fun expandedButtonPanelMode(expand: Boolean) {
        _buttonPanelExpandedMode.value = expand
    }


}
