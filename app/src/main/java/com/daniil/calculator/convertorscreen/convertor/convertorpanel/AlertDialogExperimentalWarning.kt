package com.daniil.calculator.convertorscreen.convertor.convertorpanel

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AlertDialogExperimentalWarning(
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    if (expanded) {
        AlertDialog(
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            title = {
                Text(text = "Experimental converter!",
                    color = Color.Yellow
                )
                },
            text = {
                Text(
                    text = "This converter is experimental." +
                            " It is not finished or does not work properly." +
                            " The results may be inaccurate or not calculated." +
                            " Please check the accuracy of the results.",
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text("OK")
                }
            },
        )
    }
}



@Composable
fun AlertDialogUnavailableWarning(
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    if (expanded) {
        AlertDialog(
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            title = {
                Text(text = "Unavailable converter!",
                    color = Color.Red
                )
            },
            text = {
                Text(
                    text = "This converter is currently unavailable." +
                            " It does not work or gives critical errors." +
                            " Because of its operation, the application may not work stably or close." +
                            " Please go to the list of other converters"
                )
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text("Back")
                }
            },
        )
    }
}

