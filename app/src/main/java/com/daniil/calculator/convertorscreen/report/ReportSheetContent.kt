package com.daniil.calculator.convertorscreen.report

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import com.daniil.calculator.core.DaniilServerAPI
import com.daniil.calculator.core.UserDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSheetContent(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    convertorScreenModel: ConvertorScreenModel,
    convertorData: ConvertorData,
) {
    convertorScreenModel.onHideScreen() // save convertor data

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val bitmaps = remember { mutableStateListOf<Bitmap>() }

    val packageName = context.packageName
    var report by remember { mutableStateOf<ReportRequest?>(null) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: byteArrayOf()
            bitmaps.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        Text(
            text = stringResource(R.string.report_error),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            maxLines = 6
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            placeholder = { Text("your@gmail.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Screenshot placeholder

        Text(
            text = stringResource(R.string.attach_screenshot),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                )
                .animateContentSize()
        ) {
            if (bitmaps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    bitmaps.forEach { bitmap ->
                        ImageContainer(bitmap = bitmap) {
                            bitmaps.remove(bitmap)
                        }
                    }
                    if (bitmaps.size < 4) {
                        AddImage() {
                            pickImage.launch("image/*")
                        }

                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pickImage.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add image"
                        )
                        Text(
                            text = "Add image",
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }

        }


        // Submit button
        Button(
            onClick = {

                val byteArrayList = bitmaps.map {
                    val resized = it.scale(1024, (it.height * 1024f / it.width).toInt())
                    bitmapToByteArray(bitmap = resized, quality = 30)
                }
                var isReturned = false
                val toast: (massage: String) -> Unit = {
                    isReturned = true
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }

                when {
                    title.isEmpty() -> toast("Title is empty")
                    description.isEmpty() -> toast("Description is empty")
                }
                if (isReturned) return@Button
                report = ReportRequest(
                    title = title,
                    description = description,
                    email = email,
                    images = byteArrayList,
                    convertorDataId = convertorData.id,
//                    logs = convertorScreenModel.log.getLogData().filterNot { it.type == ConvertorLogType.Info },
                    logs = listOf(),
                    packageName = packageName,
                    token = UserDataManager.token
                )

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Submit")
        }
    }
    val coroutine = rememberCoroutineScope()
    if (report != null) {
        AlertSend(
            report = report!!,
            onConfirm = {
                coroutine.launch {
                    Log.i("MyLog", Json.encodeToString(report))
                    val result = send(report!!)
                    launch(Dispatchers.Main) {
                        val massage = when (result) {
                            null, 404 -> "No connection to server"
                            200 -> "Report is send"
                            400 -> "Error on server"
                            else -> ""
                        }
                        Toast.makeText(context, massage, Toast.LENGTH_SHORT).show()
                    }
                    report = null
                }


            },
            onDismissRequest = {
                report = null
            }

        )
    }
}

private fun bitmapToByteArray(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 80,
): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}

private fun byteArrayToBitmap(bytes: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


private suspend fun send(report: ReportRequest): Int?
= withContext(Dispatchers.IO) {
    val result = try {
        DaniilServerAPI().sendReport(report)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    return@withContext result?.code()

}


@Composable
private fun AlertSend(
    report: ReportRequest,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    val bitmaps = remember { report.images.map { byteArrayToBitmap(it) } }

    AlertDialog(
        icon = {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        },
        title = {
            Text(text = "Send report")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Title: ${report.title}\n" +
                            "Description: ${report.description}\n" +
                            "Email: ${report.email}\n" +
                            "Convertor: ${report.convertorDataId}"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small
                        )
                        .animateContentSize()
                ) {
                    if (bitmaps.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            bitmaps.forEach { bitmap ->
                                ImageContainer(bitmap = bitmap, deleteIconShow = false) {}
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
    )

}




@Composable
private fun ImageContainer(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    deleteIconShow: Boolean = true,
    onDeleteCLicked: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(180.dp, 320.dp)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.TopEnd
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Image",
            contentScale = ContentScale.Crop
        )
        if (deleteIconShow) {
            Box(
                modifier = Modifier.padding(6.dp)
            ) {
                FilledIconButton(
                    modifier = Modifier,
                    colors = IconButtonDefaults.iconButtonColors().copy(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = { onDeleteCLicked() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_icon),
                        contentDescription = "Delete image"
                    )
                }
            }
        }

    }
}


@Composable
private fun AddImage(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(180.dp, 320.dp)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add image"
                )
            }
            Text(
                text = "Add new",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
        }

    }
}

//@Preview(showBackground = true)
//@Composable
//private fun Preview() {
//    ReportSheetContent()
//}