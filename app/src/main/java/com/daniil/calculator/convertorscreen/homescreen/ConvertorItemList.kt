package com.daniil.calculator.convertorscreen.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorReleseState
import com.daniil.calculator.convertorscreen.homescreen.dataclass.getIcon


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun SharedTransitionScope.ConvertorItemList(
    modifier: Modifier = Modifier,
    convertorData: ConvertorData,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(
                alpha = if (convertorData.release != ConvertorReleseState.Unavailable) 1f else 0.6f
            )
            )            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = convertorData.id),
                animatedVisibilityScope = animatedVisibilityScope,
            )
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = {

                }
            ),
        contentAlignment = Alignment.Center

    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(remember { convertorData.getIcon() }),
                contentDescription = convertorData.id
            )
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = convertorData.title,
                    style = MaterialTheme.typography.bodySmall,
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 18.sp),
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            ReleaseStateTable(release = convertorData.release)
        }

    }

}


@Preview
@Composable
private fun Preview() {
    SharedTransitionLayout {
        AnimatedVisibility(true) {
            ConvertorItemList(
                modifier = Modifier,
                convertorData = ConvertorData(
                    id = "Text",
                    painterName = "",
                    startUnit = NullableUnit,
                    release = ConvertorReleseState.Beta
                ),
                animatedVisibilityScope = this
            ) { }
        }

    }

}