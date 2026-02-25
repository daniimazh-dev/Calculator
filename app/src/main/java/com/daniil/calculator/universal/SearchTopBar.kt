package com.daniil.calculator.universal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.R


@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    startData: String? = null,
    onSearchChange: (text: String) -> Unit = {},
    onFocused: (focused: Boolean) -> Unit = {}
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(startData.orEmpty()) }

    val onCancel = {
        searchExpanded = false
        searchText = ""
        onSearchChange(searchText)
    }

    BackHandler(searchExpanded) {
        onCancel()
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            focusRequester.requestFocus()
        }
    }

    Row(modifier = modifier.animateContentSize()) {
        AnimatedVisibility(
            visible = searchExpanded,
            enter = fadeIn() + expandHorizontally() { -it / 2 },
            exit = fadeOut() + shrinkHorizontally() { -it / 2 },
        ) {
            IconButton(onClick = {
                onCancel()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back"
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

        ),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            onClick = {
                searchExpanded = true
            }
        ) {

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "search_icon"
                )
                val visibilityState = remember { MutableTransitionState(false) }
                visibilityState.targetState = true
                Box {
                    Row {
                        AnimatedVisibility(
                            visible = !searchExpanded
                        ) {
                            Text(stringResource(R.string.search) + "...")
                        }
                    }

                    if (searchExpanded) {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth( )
                                .focusable()
                                .focusRequester(focusRequester)
                                .onFocusChanged {
//                                    if (it.isFocused) onCancel()
                                    searchText = ""
                                    onFocused(it.isFocused)
                                },
                            value = searchText,
                            singleLine = true,
                            cursorBrush = SolidColor(Color.White),
                            onValueChange = {
                                searchText = it
                                onSearchChange(it)
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )

                    }

                }

            }

        }
//        AnimatedVisibility(
//            visible = searchExpanded,
//            enter = fadeIn() + expandHorizontally() { it / 2 },
//            exit = fadeOut() + shrinkHorizontally() { it / 2 },
//        ) {
//            IconButton(onClick = {
//                searchText = ""
//            }) {
//                Icon(
//                    imageVector = Icons.Default.Clear,
//                    contentDescription = "clear"
//                )
//            }
//        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchText() {
    SearchTopBar()
}