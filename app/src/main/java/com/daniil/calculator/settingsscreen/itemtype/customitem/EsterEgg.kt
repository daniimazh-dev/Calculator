package com.daniil.calculator.settingsscreen.itemtype.customitem

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R


private data class PanelShape(
    val topStart: Dp = 0.dp,
    val topEnd: Dp = 0.dp,
    val bottomEnd: Dp = 0.dp,
    val bottomStart: Dp = 0.dp,
)

private data class Padding(
    val start: Dp = 0.dp,
    val top: Dp = 0.dp,
    val end: Dp = 0.dp,
    val bottom: Dp = 0.dp
)

@Composable
fun EsterEgg() {

    val totalSize = 240
    val panelSizeK = 2.1
    val padding = Padding(
        start = 6.dp,
        top = 6.dp,
        end = 6.dp,
        bottom = 6.dp,
    )
    val containerColor = Color(red = 169, green = 199, blue = 239)
    val panelColor = Color(red = 54, green = 132, blue = 225, alpha = 255)

    val iconSize = 64
    val iconTint = Color.White

    val roundK = 1
    val round1 = PanelShape(
        topStart = (32 * roundK).dp,
        topEnd = 0.dp,
        bottomEnd = (32 * roundK).dp,
        bottomStart = 0.dp
    )
    val round2 = PanelShape(
        topStart = 0.dp,
        topEnd = (32 * roundK).dp,
        bottomEnd = 0.dp,
        bottomStart = 0.dp
    )
    val round3 = PanelShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = (32 * roundK).dp
    )
    val round4 = PanelShape(
        topStart = (16 * roundK).dp,
        topEnd = (16 * roundK).dp,
        bottomEnd = (32 * roundK).dp,
        bottomStart = (16 * roundK).dp
    )


    val list = remember {
        mutableStateListOf(
            "panel1",
            "panel2",
            "panel3",
            "panel4",
        )
    }

    fun panel(
        modifier: Modifier = Modifier,
        id: String,
        index: Int
    ): @Composable () -> Unit = {
        val roundBase = when (index) {
            0 -> round1
            1 -> round2
            2 -> round3
            3 -> round4
            else -> PanelShape()
        }
        val animSpeed = 1000

        val roundAnim = MaterialTheme.shapes.large.copy(
            topStart = CornerSize(animateDpAsState(roundBase.topStart, tween(animSpeed)).value),
            topEnd = CornerSize(animateDpAsState(roundBase.topEnd, tween(animSpeed)).value),
            bottomEnd = CornerSize(animateDpAsState(roundBase.bottomEnd, tween(animSpeed)).value),
            bottomStart = CornerSize(
                animateDpAsState(
                    roundBase.bottomStart,
                    tween(animSpeed)
                ).value
            )
        )


        val roundAnimMin = MaterialTheme.shapes.large.copy(
            topStart = CornerSize(animateDpAsState(roundBase.topStart / 2, tween(animSpeed)).value),
            topEnd = CornerSize(animateDpAsState(roundBase.topEnd / 2, tween(animSpeed)).value),
            bottomEnd = CornerSize(animateDpAsState(roundBase.bottomEnd / 1.5f, tween(animSpeed)).value),
            bottomStart = CornerSize(animateDpAsState(roundBase.bottomStart / 2, tween(animSpeed)).value)
        )

        val panelPadding = PaddingValues(
            start = padding.start,
            top = if (index == 2) padding.top + 16.dp else padding.top,
            end = padding.end,
            bottom = if (index == 0) 0.dp else padding.bottom,
        )

        when (id) {
            "panel1" -> {
                Panel(
                    modifier = modifier
                        .size((totalSize / panelSizeK).dp)
                        .padding(panelPadding)
                        .clip(roundAnim)
                        .background(panelColor),
                    list = list,
                    item = id

                ) {
                    Icon(
                        modifier = Modifier.size(iconSize.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = "add_icon",
                        tint = iconTint
                    )
                }
            }

            "panel2" -> {
                Panel(
                    modifier = modifier
                        .size((totalSize / panelSizeK).dp)
                        .padding(panelPadding)
                        .clip(roundAnim)
                        .background(panelColor),
                    list = list,
                    item = id

                ) {
                    Icon(
                        modifier = Modifier.size(iconSize.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = "multiple_icon",
                        tint = iconTint
                    )
                }
            }

            "panel3" -> {
                Panel(
                    modifier = modifier
                        .size((totalSize / panelSizeK).dp)
                        .padding(panelPadding)
                        .clip(roundAnim)
                        .background(panelColor),
                    list = list,
                    item = id

                ) {
                    Icon(
                        modifier = Modifier.size(iconSize.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.minus_icon),
                        contentDescription = "minus_icon",
                        tint = iconTint
                    )
                }
            }

            "panel4" -> {
                Panel(
                    modifier = modifier
                        .size((totalSize / panelSizeK).dp)
                        .padding(panelPadding)
                        .clip(roundAnim)
                        .border(8.dp, panelColor, roundAnim),
                    list = list,
                    item = id

                ) {
                    Panel(
                        modifier = Modifier
                            .size(((totalSize / panelSizeK) - (12 * panelSizeK)).dp)
                            .padding(vertical = if (index == 2) padding.start + 8.dp else padding.start, horizontal = padding.start)
                            .clip(roundAnimMin)
                            .background(panelColor),
                        list = list,
                        item = id

                    ) {
                        Icon(
                            modifier = Modifier.size(iconSize.dp),
                            painter = painterResource(R.drawable.equal_icon),
                            contentDescription = "equal_icon",
                            tint = iconTint
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(totalSize.dp)
                    .clip(MaterialTheme.shapes.extraLarge),
//                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2)
                ) {

                    itemsIndexed(
                        items = list,
                        key = { index, item -> item })
                    { index, item ->
                        Box(
                            modifier = Modifier.animateItem()
                        ) {
                            panel(id = item, index = index)()
                        }

                    }
                }

            }
        }
    }

}

@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    list: SnapshotStateList<String>,
    item: String,
    icon: @Composable () -> Unit,
) {
    var dragAmountX by remember { mutableFloatStateOf(0f) }
    var dragAmountY by remember { mutableFloatStateOf(0f) }

    fun moveTo(newIndex: Int) {
        if (newIndex in list.indices) {
            list.remove(item)
            list.add(newIndex, item)

        }
    }
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragAmountX += dragAmount.x
                    dragAmountY += dragAmount.y
                },
                onDragEnd = {
                    val threshold = 120f

                    when {
                        dragAmountY > threshold -> { // swipe down
                            moveTo(list.indexOf(item) + 2)
                        }

                        dragAmountY < -threshold -> { // swipe up
                            moveTo(list.indexOf(item) - 2)
                        }

                        dragAmountX > threshold -> { // swipe right
                            moveTo(list.indexOf(item) + 1)
                        }

                        dragAmountX < -threshold -> { // swipe left
                            moveTo(list.indexOf(item) - 1)
                        }
                    }

                    dragAmountX = 0f
                    dragAmountY = 0f
                },
                onDragCancel = {
                    dragAmountX = 0f
                    dragAmountY = 0f
                }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }


}


@Preview
@Composable
private fun EsterEggPreview() {
    EsterEgg()
}