package com.daniil.calculator.convertorscreen.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ConvertorItemTile(
    modifier: Modifier = Modifier,
    convertorData: ConvertorData,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ).alpha( if (convertorData.release != ConvertorReleseState.Unavailable) 1f else 0.6f,)


            .sharedBounds(
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

        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(remember { convertorData.getIcon() }),
                contentDescription = convertorData.id,
//                tint = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = convertorData.title,
                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurface,
                    autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 18.sp),
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
            ReleaseStateTable(release = convertorData.release)


        }

    }

}

@Preview
@Composable
private fun Preview() {
    SharedTransitionLayout {
        AnimatedVisibility(true) {
            ConvertorItemTile(
                modifier = Modifier.width(112.dp),
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