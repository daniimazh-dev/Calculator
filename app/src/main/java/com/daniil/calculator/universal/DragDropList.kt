package com.daniil.calculator.universal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Job
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

fun LazyListState.getVisibleItemInfoFor(absolute: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(absolute - this.layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to)
        return

    val element = this.removeAt(from) ?: return
    this.add(to, element)
}


enum class DragDropOrientation {
    Vertical,
    Horizontal
}
@Composable
fun rememberDragDropListState(
    orientation: DragDropOrientation,
    isMoved: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    onMove: (Int, Int) -> Unit,
    onMoveFinished: (Int, Int) -> Unit = { _, _ -> } // 🆕
): DragDropListState {
    return remember {
        DragDropListState(
            orientation = orientation,
            isMoved = isMoved,
            lazyListState = lazyListState,
            onMove = onMove,
            onMoveFinished = onMoveFinished
        )
    }
}

class DragDropListState(
    val orientation: DragDropOrientation,
    val lazyListState: LazyListState,
    val isMoved: Boolean = true,
    private val onMove: (Int, Int) -> Unit,
    private val onMoveFinished: (Int, Int) -> Unit // 🆕
) {
    var draggedDistance by mutableStateOf(0f)
    var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
    var overScrollJob by mutableStateOf<Job?>(null)

    private var initialIndex: Int? = null // 🆕 запам’ятовуємо, звідки почали drag

    private val LazyListItemInfo.start: Int get() = offset
    private val LazyListItemInfo.end: Int get() = offset + size

    val initialOffsets: Pair<Int, Int>? get() = initiallyDraggedElement?.let { it.start to it.end }

    val elementDisplacement: Float?
        get() = currentIndexOfDraggedItem
            ?.let { lazyListState.getVisibleItemInfoFor(it) }
            ?.let { item ->
                (initiallyDraggedElement?.start ?: 0f).toFloat() + draggedDistance - item.start
            }

    val currentElement: LazyListItemInfo?
        get() = currentIndexOfDraggedItem?.let { lazyListState.getVisibleItemInfoFor(it) }

    fun onDragStart(offset: Offset) {
        if (!isMoved) return
        lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            when (orientation) {
                DragDropOrientation.Vertical -> offset.y.toInt() in item.start..item.end
                DragDropOrientation.Horizontal -> offset.x.toInt() in item.start..item.end
            }
        }?.also {
            currentIndexOfDraggedItem = it.index
            initiallyDraggedElement = it
            initialIndex = it.index // 🆕 зберігаємо старт
        }
    }

    fun onDragInterrupted() {
        // 🆕 викликаємо onMoveFinished якщо треба
        val from = initialIndex
        val to = currentIndexOfDraggedItem
        if (from != null && to != null && from != to) {
            onMoveFinished(from, to)
        }

        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        initialIndex = null
        overScrollJob?.cancel()
    }

    fun onDrag(offset: Offset) {
        if (!isMoved) return
        draggedDistance += when (orientation) {
            DragDropOrientation.Vertical -> offset.y
            DragDropOrientation.Horizontal -> offset.x
        }

        initialOffsets?.let { (start, end) ->
            val startOffset = start + draggedDistance
            val endOffset = end + draggedDistance

            currentElement?.let { hovered ->
                lazyListState.layoutInfo.visibleItemsInfo
                    .filterNot { item ->
                        item.end < startOffset || item.start > endOffset || hovered.index == item.index
                    }
                    .firstOrNull { item ->
                        val delta = startOffset - hovered.start
                        when {
                            delta > 0 -> (endOffset > item.end)
                            else -> (startOffset < item.start)
                        }
                    }?.also { item ->
                        currentIndexOfDraggedItem?.let { current ->
                            onMove(current, item.index)
                        }
                        currentIndexOfDraggedItem = item.index
                    }
            }
        }
    }

    fun checkForOverScroll(): Float {
        if (!isMoved) return 0f
        return initiallyDraggedElement?.let {
            val startOffset = it.start + draggedDistance
            val endOffset = it.end + draggedDistance

            when (orientation) {
                DragDropOrientation.Vertical -> when {
                    draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset)
                        .takeIf { diff -> diff > 0 }
                    draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset)
                        .takeIf { diff -> diff < 0 }
                    else -> null
                }
                DragDropOrientation.Horizontal -> when {
                    draggedDistance > 0 -> (endOffset - lazyListState.layoutInfo.viewportEndOffset)
                        .takeIf { diff -> diff > 0 }
                    draggedDistance < 0 -> (startOffset - lazyListState.layoutInfo.viewportStartOffset)
                        .takeIf { diff -> diff < 0 }
                    else -> null
                }
            }
        } ?: 0f
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> DragDropList(
    modifier: Modifier = Modifier,

    items: List<T>,
    key:  ((Int, T) -> Any)? = null,
    orientation: DragDropOrientation = DragDropOrientation.Vertical,
    onMove: (Int, Int) -> Unit = { _, _ -> },
    onMoveFinished: (Int, Int) -> Unit = { _, _ -> },
    isMoved: Boolean = true,
    dragDropListState: DragDropListState = rememberDragDropListState(
        orientation = orientation,
        onMove = onMove,
        onMoveFinished = onMoveFinished,
        isMoved = isMoved
    ),
    content: @Composable (index: Int, item: T, draggable: Int?) -> Unit
) {
    val scope = rememberCoroutineScope()

    val listState = dragDropListState.lazyListState

    val dragModifier = if (dragDropListState.isMoved) {
        Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset -> dragDropListState.onDragStart(offset) },
                onDrag = { change, offset ->
                    change.consume()
                    dragDropListState.onDrag(offset)
                    dragDropListState.checkForOverScroll().takeIf { it != 0f }?.let {
                        scope.launch { listState.animateScrollBy(it) }
                    }
                },
                onDragEnd = { dragDropListState.onDragInterrupted() },
                onDragCancel = { dragDropListState.onDragInterrupted() }
            )
        }
    } else Modifier

    when (dragDropListState.orientation) {
        DragDropOrientation.Vertical -> LazyColumn(
            state = listState,
            modifier = modifier.then(dragModifier)
        ) {
            itemsIndexed(items, key = key) { index, item ->
                val isDragged = index == dragDropListState.currentIndexOfDraggedItem
                val itemModifier = if (isDragged) Modifier.fillMaxWidth()
                else Modifier.fillMaxWidth()
//                    .animateItem()

                Box(
                    modifier = itemModifier.graphicsLayer {
                        if (dragDropListState.isMoved && isDragged) {
                            translationY = dragDropListState.elementDisplacement ?: 0f
                        }
                    }
                ) {
                    content(index, item, dragDropListState.currentIndexOfDraggedItem)
                }
            }
        }

        DragDropOrientation.Horizontal -> LazyRow(
            state = listState,
            modifier = modifier.then(dragModifier)
        ) {
            itemsIndexed(items, key = key) { index, item ->
                val isDragged = index == dragDropListState.currentIndexOfDraggedItem
                val itemModifier = if (isDragged) Modifier.fillMaxWidth()
                else Modifier.fillMaxWidth()
//                    .animateItem()

                Box(
                    modifier = itemModifier.graphicsLayer {
                        if (dragDropListState.isMoved && isDragged) {
                            translationX = dragDropListState.elementDisplacement ?: 0f
                        }
                    }
                ) {
                    content(index, item, dragDropListState.currentIndexOfDraggedItem)
                }
            }
        }
    }
}
