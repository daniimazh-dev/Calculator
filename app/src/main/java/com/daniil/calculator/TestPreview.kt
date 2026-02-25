package com.daniil.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daniil.calculator.convertorscreen.ConvertorScreenModel

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val convertorScreenModel = viewModel() { ConvertorScreenModel() }
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(portraitMode = true, convertorScreenModel = convertorScreenModel)
        HorizontalDivider()
        TestScreen()
    }
}

@Composable
private fun TestScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}


@Composable
private fun TopBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OperationButton(icon = Icons.AutoMirrored.Filled.ArrowBack) {}
            OperationButton(icon = ImageVector.vectorResource(R.drawable.desine_icon)) {}
            OperationButton(icon = ImageVector.vectorResource(R.drawable.comment_icon)) {}
            OperationButton(icon = ImageVector.vectorResource(R.drawable.view1_icon)) {}
            OperationButton(icon = ImageVector.vectorResource(R.drawable.browse_icon)) {}
            OperationButton(icon = Icons.Default.Menu) {}
        }
    }

}

@Composable
private fun OperationButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = {
            onClick()
        }
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}