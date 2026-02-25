package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.CardContainer
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayoutScope
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedValue
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.AddButton
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.ButtonsStackStorage
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.DefaultButtons
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.default_.DefaultButtonsWithMinus
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.defaultDropdownMenu
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.convertor.unit.UnitSelect
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue
import com.daniil.calculator.core.CalculatorCore
import com.daniil.calculator.universal.UniversalDropDownItem
import com.daniil.calculator.utilites.roundTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor


class FunctionGraphConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {

    override fun onCreate() {
        super.onCreate()
        setContent {
            var settingsOpen by remember { mutableStateOf(false) }

            singleViewMode = true
            val customDropDown =  listOf(UniversalDropDownItem(
                title = "Graph settings",
                iconResource = R.drawable.settings_icon,
                onClick = {
                    settingsOpen = true
                }
            ))

            val selectParam: String? = convertorScreenModel.getParameter(
                key = "selectParam",
                defaultValue = null
            ) as String?

            mode("Graph", name = "Graph", painterId = R.drawable.graph_icon) {
                content = {
                    FunctionGraphScreen()
                    SettingsAlert(
                        expanded = settingsOpen,
                        onDismissRequest = {
                            settingsOpen = false
                        }
                    )
                }
                dropdownMenu = (defaultDropdownMenu() + customDropDown).toMutableStateList()
                showClackPanel.value = false
                allButtons(ButtonsStackStorage(model = convertorScreenModel).DefaultButtonsWithMinus)
            }



        }

    }

}


private object FunctionChach {
    lateinit var chach: HashMap<Int, Float>
    var lastKey: String? = null
    fun clearChach() {
        chach.clear()
    }

    fun init() {
        chach = HashMap()
    }

    suspend fun loadCacheParallel(
        preloadRange: Float = 500f,
        preloadStep: Float = 0.01f,
        function: (Float) -> Float
    ) = withContext(Dispatchers.Default) {
        clearChach()
        val cpuCount = Runtime.getRuntime().availableProcessors()
        val totalRange = preloadRange * 2
        val chunkSize = totalRange / cpuCount

        val jobs = (0 until cpuCount).map { index ->

            val start = -preloadRange + index * chunkSize
            val end = start + chunkSize

            async {
                val localMap = HashMap<Int, Float>()
                var x = start
                while (x <= end) {
                    val key = (x * 1000).toInt()
                    val y = function(x)

                    if (y.isFinite()) {
                        localMap[key] = y
                    }
                    x += preloadStep
                }

                localMap
            }
        }

        jobs.awaitAll().forEach { local ->
            chach.putAll(local)
        }
    }

}

@Serializable
internal data class FunctionParameter(
    val variableChar: Char,
    val value: Float,
)

@Composable
fun FunctionGraphConvertorImplementation.FunctionGraphScreen() {
    val context = LocalContext.current

    var customFuncPickerShow by remember { mutableStateOf(false) }

    @Suppress("UNCHECKED_CAST")
    val parameterList: List<FunctionParameter> = convertorScreenModel.getParameter(
        key = "paramList",
        defaultValue = emptyList<FunctionParameter>()
    ) as List<FunctionParameter>


    val selectParam: String? = convertorScreenModel.getParameter(
        key = "selectParam",
        defaultValue = null
    ) as String?


    val function: String = convertorScreenModel.getParameter(
        key = "function",
        defaultValue = "x"
    ) as String

    val girdStep: Float = convertorScreenModel.getParameter(
        key = "gridStep",
        defaultValue = 1f
    ).toString().toFloat()
    val maxAsymptotesIgnoring: Float = convertorScreenModel.getParameter(
        key = "maxAsymptotesIgnoring",
        defaultValue = 50f
    ).toString().toFloat()
    val graphStep: Float = convertorScreenModel.getParameter(
        key = "graphStep",
        defaultValue = 8f
    ).toString().toFloat()


    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = convertorData,
        containerColor = Color.Transparent,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardContainer(
                modifier = Modifier
                    .weight(1f)
            ) {
                Graph(
                    modifier = Modifier.fillMaxSize(),
                    graphStep = graphStep / 40,
                    gridStep = girdStep,
                    maxAsymptotesIgnoring = maxAsymptotesIgnoring * 10,
                    cacheUpdateKey = function + parameterList.joinToString(),
                    function = { x ->
                        var input = function.replace("x", x.toString())

                        for (parm in parameterList) {
                            val variable = parm.variableChar.toString()
                            val value = parm.value.toString()
                            val regex = Regex("(?<![a-zA-Z0-9_])$variable(?![a-zA-Z0-9_])")
                            input = input.replace(regex, value)
                        }

                        val result = CalculatorCore.evaluate(input)
                        result?.toFloatOrNull() ?: Float.NaN
                    }
                )
            }

            SmallResult(
                title = stringResource(R.string.function),
                content = "y=$function",
                copyPasteMenu = CopyPasteMenu.Full,
                onPaste = { str ->
                    str?.let {
                        convertorScreenModel.saveParameters {
                            setStringData("function", it.replace("y=", ""))
                        }
                    }
                },
                onClick = {
                    customFuncPickerShow = true
                    convertorScreenModel.saveParameters {
                        setStringData("selectParam", null)
                    }
                }
            )

            GroupedLayout(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large),
                scrollState = rememberScrollState()
            ) { groupedValue ->
                parameterList.forEach { parm ->
                    ParameterItem(
                        parm = parm,
                        groupedValue = groupedValue,
                        onChange = { new ->
                            val index = parameterList.indexOf(parm)
                            if (index != -1) {
                                val list = parameterList.toMutableList()
                                list[index] = FunctionParameter(variableChar = new, parm.value)
                                convertorScreenModel.saveParameters {
                                    setObject("paramList", list)
                                }

                            }
                        }
                    )
                }
            }
            if (parameterList.size < 24) { // 26 later - 'x' and 'y' = 24 later
                AddButton(
                    padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    onClick = {
                        val abcd = ('a'..'z').filterNot {
                            it == 'x' || it == 'y'
                        }
                        val char = if (parameterList.isNotEmpty()) {
                            val charList = parameterList.map { it.variableChar }
                            abcd.first { it !in charList }
                        } else 'a'
                        convertorScreenModel.saveParameters {
                            setObject(
                                "paramList",
                                parameterList + listOf(
                                    FunctionParameter(
                                        variableChar = char,
                                        value = 0f
                                    )
                                )
                            )
                        }
                    }
                )
            }

        }
    }
    CustomFunctionPicker(
        expanded = customFuncPickerShow,
        value = function,
        onDismissRequest = {
            customFuncPickerShow = false
        },
        onConfirm = {
            convertorScreenModel.saveParameters {
                setStringData("function", it.replace("y=", ""))
            }
            customFuncPickerShow = false
        }
    )
}

@Composable
private fun ConvertorLayoutScope.ParameterItem(
    parm: FunctionParameter,
    groupedValue: GroupedValue? = null,
    onChange: (Char) -> Unit,
) {
    val context = LocalContext.current
    var dropDownMenuExpanded by remember { mutableStateOf(false) }

    val calckBlock by convertorScreenModel.calckBlock.collectAsState()

    @Suppress("UNCHECKED_CAST")
    val parameterList: List<FunctionParameter> = convertorScreenModel.getParameter(
        key = "paramList",
        defaultValue = emptyList<FunctionParameter>()
    ) as List<FunctionParameter>


    val selectParam: String? = convertorScreenModel.getParameter(
        key = "selectParam",
        defaultValue = null
    ) as String?

    val selected = selectParam == parm.variableChar.toString()

    val content = if (selected) calckBlock else parm.value.roundTo(3).toString()
    LaunchedEffect(calckBlock) {
        if (selected) {
            val newValue = calckBlock.toFloatOrNull() ?: return@LaunchedEffect
            val list = parameterList.toMutableList()
            val index = parameterList.indexOf(parm)
            list[index] = parm.copy(value = newValue)
            convertorScreenModel.saveParameters {
                setObject("paramList", list)
            }
        }
    }

    CardContainer(
        groupedValue = groupedValue,
        contentPadding = PaddingValues(16.dp),
        onClick = {
            convertorScreenModel.setCalck(parm.value.toString())
            convertorScreenModel.clearNextInput = true
            convertorScreenModel.saveParameters {
                setStringData("selectParam", parm.variableChar.toString())
            }
        },
        onLongClick = {
            dropDownMenuExpanded = true
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledIconButton(
                    colors = IconButtonDefaults.filledIconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = {

                        val list = parameterList.toMutableList()
                        list.remove(parm)

                        convertorScreenModel.saveParameters {
                            setObject("paramList", list)
                            if (selected) {
                                setStringData("selectParam", null)
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_icon),
                        contentDescription = "delete"
                    )
                }
                val units = remember { ('a'..'z').filterNot {
                        it == 'x' || it == 'y'
                    }.map { char -> ConvertorUnit(id = char.toString(), name = "", symbol = char.toString()) }
                }
                UnitSelect(
                    currentUnit = units.find { it.id == parm.variableChar.toString() } ?: NullableUnit,
                    unitList = units,
                    horizontalAlignment = Alignment.End
                ) {
                    onChange(it.id.getOrElse(0) { '-' })
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (!selected) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary
                )
            }


        }
        CopyPasteMenu(
            expanded = dropDownMenuExpanded,
            copyPasteMenu = CopyPasteMenu.Full,
            onDismissRequest = {
                dropDownMenuExpanded = false
            },
            onCopy = {
                return@CopyPasteMenu content
            },
            onPaste = { str ->
                if (str == null) return@CopyPasteMenu
                val validate = convertorScreenModel.validateValue(str)
                if (!validate.first) {
                    Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                    return@CopyPasteMenu
                }
                convertorScreenModel.setCalck(str)
                convertorScreenModel.saveParameters {
                    setStringData("selectParam", parm.variableChar.toString())
                }
            }
        )
    }
}


@Composable
private fun CustomFunctionPicker(
    expanded: Boolean,
    value: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    if (expanded) {
        var value by remember { mutableStateOf(value) }

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = {
                Text("Custom function")
            },
            text = {
                OutlinedTextField(
                    label = { Text("function") },
                    placeholder = { Text("y=") },
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onConfirm(value) }
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }


}


@Composable
private fun FunctionGraphConvertorImplementation.SettingsAlert(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (expanded) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = {
                Text("Graph settings")
            },
            text = {
                SettingsContent()
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }


}

@Composable
private fun FunctionGraphConvertorImplementation.SettingsContent() {
    val girdStep: Float = convertorScreenModel.getParameter(
        key = "gridStep",
        defaultValue = 1f
    ).toString().toFloat()
    val maxAsymptotesIgnoring: Float = convertorScreenModel.getParameter(
        key = "maxAsymptotesIgnoring",
        defaultValue = 50f
    ).toString().toFloat()
    val graphStep: Float = convertorScreenModel.getParameter(
        key = "graphStep",
        defaultValue = 8f
    ).toString().toFloat()

    ConvertorLayout(
        modifier = Modifier,
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        contentPadding = PaddingValues(8.dp),
        convertorData = convertorData,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GroupedLayout(
            scrollState = rememberScrollState()
        ) { groupedValue ->
            CardContainer(
                groupedValue = groupedValue,
                contentPadding = PaddingValues(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        Text("Grid step")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(girdStep.toString().roundTo(1))
                    }
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = girdStep,
                        valueRange = 1f..10f,
                        steps = 8,
                        onValueChange = {
                            convertorScreenModel.saveParameters {
                                setFloatData("gridStep", it)
                            }
                        },
                    )
                }
            }
            CardContainer(
                groupedValue = groupedValue,
                contentPadding = PaddingValues(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        Text("Max asymptotes ignoring")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(maxAsymptotesIgnoring.toString().roundTo(1))
                    }
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = maxAsymptotesIgnoring,
                        valueRange = 20f..200f,
                        onValueChange = {
                            convertorScreenModel.saveParameters {
                                setFloatData("maxAsymptotesIgnoring", it)
                            }
                        },
                    )
                }
            }
            CardContainer(
                groupedValue = groupedValue,
                contentPadding = PaddingValues(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        Text("Graph step calculating")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(graphStep.toString().roundTo(1))
                    }
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = graphStep,
                        valueRange = 1f..10f,
                        steps = 8,
                        onValueChange = {
                            convertorScreenModel.saveParameters {
                                setFloatData("graphStep", it)
                            }
                        },
                    )
                }
            }

            Button(onClick = {
                convertorScreenModel.clearParam()
            }) {
                Text("Reset to default")
            }

        }
    }
}

@Composable
private fun DraggableCanvas(
    modifier: Modifier = Modifier,
    changedContent: DrawScope.(offset: Offset, scale: Float) -> Unit,
) {
    val defaultOffset = Offset.Zero
    val defaultScale = 80f
    var offset by remember { mutableStateOf(defaultOffset) }
    var scale by remember { mutableStateOf(defaultScale) }

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.BottomEnd
    ) {

        Canvas(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        offset += pan
                        scale = (scale * zoom).coerceIn(30f, 150f)
                    }
                }
        ) {
            changedContent(
                offset,
                scale,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
//            Column() {
//                Text("offset x: ${offset.x}", color = Color.Blue, fontSize = 12.sp)
//                Text("offset y: ${offset.y}", color = Color.Green, fontSize = 12.sp)
//                Text("scale: $scale", color = Color.Red, fontSize = 12.sp)
//            }
            AnimatedVisibility(
                visible = offset != defaultOffset,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                OutlinedIconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = {
                        offset = defaultOffset
                        scale = defaultScale
                    }
                ) {
                    Icon(painter = painterResource(R.drawable.fixed_icon), contentDescription = "Fixed")
                }
            }

        }

    }

}


@Composable
fun Graph(
    modifier: Modifier = Modifier,
    graphStep: Float,
    maxAsymptotesIgnoring: Float = 50f,
    gridStep: Float = 1f,
    cacheUpdateKey: String,
    function: (Float) -> Float,
) {

    var loading by remember { mutableStateOf(false) }
    if (FunctionChach.lastKey == null) FunctionChach.init()

    LaunchedEffect(cacheUpdateKey) {
        if (cacheUpdateKey != FunctionChach.lastKey) {
            loading = true
            FunctionChach.lastKey = cacheUpdateKey
            FunctionChach.loadCacheParallel {
                function(it)
            }
            loading = false
        }
    }

    if (loading) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(R.string.loading) + "...")

            }
        }
        return
    }

    val funcColor = MaterialTheme.colorScheme.primary
    val axesColor = MaterialTheme.colorScheme.outlineVariant
    val gridColor = MaterialTheme.colorScheme.outline

    DraggableCanvas(modifier) { offset, scale ->

        fun worldToScreen(x: Float, y: Float): Offset {
            val offsetX = (x * scale + offset.x) + size.width / 2
            val offsetY = (-y * scale + offset.y) + size.height / 2
            return Offset(offsetX, offsetY)
        }

        fun indexOfCoordinate(x: Float, y: Float): Pair<Int, Int> {
            val startPointX = floor(x / gridStep) * gridStep
            val startPointY = floor(y / gridStep) * gridStep
            return startPointX.toInt() to startPointY.toInt()
        }

        val leftX = (-offset.x - size.width / 2) / scale
        val rightX = (size.width - offset.x) / scale
        val bottomY = (offset.y - size.height) / scale
        val topY = (offset.y + size.height / 2) / scale

        // Axes
        drawLine(
            color = axesColor,
            worldToScreen(rightX, 0f),
            worldToScreen(leftX, 0f),
            scale / 80f
        )

        drawLine(
            color = axesColor,
            worldToScreen(0f, bottomY),
            worldToScreen(0f, topY),
            scale / 80f
        )

        // Point
        val startPointX = floor(leftX / gridStep) * gridStep
        val endPointX = ceil(rightX / gridStep) * gridStep
        val startPointY = floor(bottomY / gridStep) * gridStep
        val endPointY = ceil(topY / gridStep) * gridStep

        var xPoint = startPointX

        while (xPoint < endPointX) {
            val coordinate = indexOfCoordinate(xPoint, 0f)
            val xPosition = coordinate.first
            var widthLine = 0.2f
            var stoke = 2f
            if (xPosition % (gridStep * 5) == 0f) {
                widthLine = 0.4f
                stoke = 3f
            }
            val textOffset = worldToScreen(xPoint, widthLine + 0.1f)
            if (xPosition != 0) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = ((stoke * scale) * gridStep / 2) / 5
                    }
                    canvas.nativeCanvas
                        .drawText(xPosition.toString(), textOffset.x, textOffset.y, paint)
                }
            }
            drawLine(
                color = axesColor,
                alpha = 1f,
                start = worldToScreen(xPoint, widthLine),
                end = worldToScreen(xPoint, -widthLine),
                strokeWidth = scale / (stoke * 30)
            )
            xPoint += gridStep
        }

        var yPoint = startPointY
        while (yPoint < endPointY) {
            val coordinate = indexOfCoordinate(0f, yPoint)
            val yPosition = coordinate.second
            var widthLine = 0.2f
            var stoke = 2f
            if (yPosition % (gridStep * 5) == 0f) {
                widthLine = 0.4f
                stoke = 3f
            }
            val textOffset = worldToScreen(widthLine + 0.1f, yPoint)
            if (yPosition != 0) {
                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = ((stoke * scale) * gridStep / 2) / 5
                    }
                    canvas.nativeCanvas
                        .drawText(yPosition.toString(), textOffset.x, textOffset.y, paint)
                }
            }

            drawLine(
                color = axesColor,
                alpha = 1f,
                start = worldToScreen(widthLine, yPoint),
                end = worldToScreen(-widthLine, yPoint),
                strokeWidth = scale / (stoke * 30)
            )
            yPoint += gridStep
        }


        // Grid

        val startGridX = floor(leftX / gridStep) * gridStep
        val endGridX = ceil(rightX / gridStep) * gridStep

        val startGridY = floor(bottomY / gridStep) * gridStep
        val endGridY = ceil(topY / gridStep) * gridStep


        var xGrid = startGridX
        while (xGrid <= endGridX) {
            drawLine(
                color = gridColor,
                alpha = 0.3f,
                start = worldToScreen(xGrid, bottomY),
                end = worldToScreen(xGrid, topY),
                strokeWidth = scale / 100f
            )
            xGrid += gridStep
        }

        var yGrid = startGridY
        while (yGrid <= endGridY) {
            drawLine(
                color = gridColor,
                alpha = 0.3f,
                start = worldToScreen(leftX, yGrid),
                end = worldToScreen(rightX, yGrid),
                strokeWidth = scale / 100f
            )
            yGrid += gridStep
        }


        val maxDeltaY = maxAsymptotesIgnoring / scale

        // Function
        val path = Path()
        var first = true
        var prevY: Float? = null

        val stepX = graphStep / scale

        var x = leftX
        loop@ while (x <= rightX) {
            val key = (x * 1000).toInt()
            val y = FunctionChach.chach.getOrPut(key) {
                function(x)
            }
            if (y.isNaN()) {
                x += stepX
                continue
            }

            if (!y.isFinite()) {
                first = true
                prevY = null
                x += stepX
                continue
            }
            if (prevY != null) {
                val dy = abs(y - prevY)

                val crossesZero =
                    prevY * y < 0 &&
                            abs(prevY) < maxDeltaY &&
                            abs(y) < maxDeltaY

                if (crossesZero) {
                    val zeroX = x - stepX / 2f
                    val p0 = worldToScreen(zeroX, 0f)
                    path.lineTo(p0.x, p0.y)
                }

                // 🔥 справжній розрив / асимптота
                else if (dy > maxDeltaY) {
                    first = true
                    prevY = y
                    x += stepX
                    continue
                }
            }

            val p = worldToScreen(x, y)

            if (first) {
                path.moveTo(p.x, p.y)
                first = false
            } else {
                path.lineTo(p.x, p.y)
            }

            prevY = y
            x += stepX
        }

        drawPath(
            path = path,
            color = funcColor,
            style = Stroke(scale / 60f)
        )
    }
}

