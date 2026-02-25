package com.daniil.calculator.convertorscreen.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.ui.theme.Orange

@Composable
fun ReleaseStateTable(
    modifier: Modifier = Modifier,
    release: ConvertorReleseState,
) {

    Box(
        modifier = modifier,
    ) {

        when (release) {
            ConvertorReleseState.Beta -> {
                Text(
                    modifier = Modifier,
                    text = "Beta",
                    fontSize = 12.sp,
                    color = Orange
                )
            }
            ConvertorReleseState.Experimental -> {
                Text(
                    modifier = Modifier,
                    text = "Experimental",
                    fontSize = 12.sp,
                    color = Orange
                )
            }
            ConvertorReleseState.Unavailable -> {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = "Unavailable",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            ConvertorReleseState.Verified -> {}
        }
    }

}

@Preview
@Composable
private fun Preview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReleaseStateTable(release = ConvertorReleseState.Beta)
        ReleaseStateTable(release = ConvertorReleseState.Experimental)
        ReleaseStateTable(release = ConvertorReleseState.Unavailable)
    }

}