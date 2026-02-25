package com.daniil.calculator.utilites

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposePaint
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.daniil.calculator.R
import kotlin.random.Random
import kotlin.random.nextInt


val imageList = listOf(
    R.drawable.length_icon,
    R.drawable.mass_icon,
    R.drawable.square_icon,
    R.drawable.capacity_icon,
    R.drawable.speed_icon,
    R.drawable.thermostat_icon,
    R.drawable.pressure_icon,
    R.drawable.energy_icon,
    R.drawable.power_icon,
    R.drawable.voltage_icon,
    R.drawable.efficiency_icon,
    R.drawable.magnet_icon,
    R.drawable.radioactive_icon,
    R.drawable.optic_icon,
    R.drawable.acoustics_icon,

// Finance
    R.drawable.currency_icon,
    R.drawable.discount_icon,
    R.drawable.credit_icon,
    R.drawable.taxes_icon,
    R.drawable.inflation_icon,
    R.drawable.assets_icon,
    R.drawable.crypto_icon,
// Time
    R.drawable.duration_icon,
    R.drawable.time_zone_icon,
    R.drawable.time_icon,

// Computing
    R.drawable.data2_icon,
    R.drawable.data_icon,
    R.drawable.resolution_icon,
    R.drawable.data_rate,
    R.drawable.bandwidth_icon,
    R.drawable.ip_icon,

// Daily life
    R.drawable.spoon_icon,
    R.drawable.energy_icon,
    R.drawable.alcohol_icon,

// Math
    R.drawable.angle_icon,
    R.drawable.graph_icon,
    R.drawable.distance_icon,
    R.drawable.sunny_flux_icon,

// Geography
    R.drawable.coordinates_icon,
    R.drawable.elevation_icon,
    R.drawable.land_aera_icon,
    R.drawable.distance_icon,

// Chemistry
    R.drawable.experiment_icon,
    R.drawable.molarity_icon,
    R.drawable.mass_faction_icon,
    R.drawable.water_ph_icon,
    R.drawable.concentration_icon,

// Science
    R.drawable.viscosity_icon,
    R.drawable.thermal_conductivity_icon,
    R.drawable.density_icon,
    R.drawable.heat_capacity_icon,
    R.drawable.heat_flux_icon,

// Transport
    R.drawable.boat_icon,
    R.drawable.airplane_icon,
    R.drawable.fuel_icon,

// Other
    R.drawable.imt_icon,
    R.drawable.run_icon,
    R.drawable.numeration_system_icon,


    R.drawable.calculator_icon,
)
fun generatePatternBitmap(
    context: Context,
    icons: List<Int>,
    tileSize: Int = 1024,
    iconSize: Int = 96,
    rotation: Float = -15f,
    alpha: Int = 255,
): Bitmap {
    val bitmap = createBitmap(460, 1024)

    val canvas = android.graphics.Canvas(bitmap)
    canvas.rotate(rotation, tileSize / 2f, tileSize / 2f)

    val paint = android.graphics.Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.alpha = alpha
    }

    val spacing = iconSize * 1.5f
    var index = 0

    for (y in -iconSize..tileSize step spacing.toInt()) {
        for (x in -iconSize..tileSize step spacing.toInt()) {
            val drawable = ResourcesCompat.getDrawable(
                context.resources,
                icons[index % icons.size],
                null
            ) ?: continue

            drawable.setBounds(
                x,
                y,
                x + iconSize,
                y + iconSize
            )
            drawable.alpha = alpha
            drawable.draw(canvas)

            index++
        }
    }

    return bitmap
}

fun getOrCreatePattern(context: Context): Bitmap {
    return PatternCache.bitmap ?: generatePatternBitmap(
        context = context,
        icons = imageList
    ).also {
        PatternCache.bitmap = it
    }
}
object PatternCache {
    var bitmap: Bitmap? = null
}



@Preview
@Composable
private fun Preview() {
    val context = LocalContext.current
    Image(
        modifier = Modifier.fillMaxSize(),
        bitmap = generatePatternBitmap(context, imageList).asImageBitmap(),
        contentDescription = ""
    )
}