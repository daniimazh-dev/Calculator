package com.daniil.calculator.convertorscreen.homescreen.dataclass

import com.daniil.calculator.R

fun ConvertorData.getIcon(): Int {
    return this.painterName.getIcon()
}


private fun String.getIcon(): Int {
    return when (this) {
        // Physics
        "length_icon" -> R.drawable.length_icon
        "mass_icon" -> R.drawable.mass_icon
        "area_icon" -> R.drawable.square_icon
        "volume_icon" -> R.drawable.capacity_icon
        "speed_icon" -> R.drawable.speed_icon
        "temperature_icon" -> R.drawable.thermostat_icon
        "pressure_icon" -> R.drawable.pressure_icon
        "energy_icon" -> R.drawable.energy_icon
        "power_icon" -> R.drawable.power_icon
        "electricity_icon" -> R.drawable.voltage_icon
        "efficiency_icon" -> R.drawable.efficiency_icon
        "magnetism_icon" -> R.drawable.magnet_icon
        "radiation_icon" -> R.drawable.radioactive_icon
        "optics_icon" -> R.drawable.optic_icon
        "acoustics_icon" -> R.drawable.acoustics_icon

// Finance
        "currency_icon" -> R.drawable.currency_icon
        "discount_icon" -> R.drawable.discount_icon
        "loans_icon" -> R.drawable.credit_icon
        "taxes_icon" -> R.drawable.taxes_icon
        "inflation_icon" -> R.drawable.inflation_icon
        "assets_icon" -> R.drawable.assets_icon
        "crypto_icon" -> R.drawable.crypto_icon

// Time
        "duration_icon" -> R.drawable.duration_icon
        "timezone_icon" -> R.drawable.time_zone_icon
        "time_icon" -> R.drawable.time_icon

// Computing
        "datasize_icon" -> R.drawable.data2_icon
        "datarate_icon" -> R.drawable.data_icon
        "resolution_icon" -> R.drawable.resolution_icon
        "latency_icon" -> R.drawable.data_rate
        "bandwidth_icon" -> R.drawable.bandwidth_icon
        "ip_icon" -> R.drawable.ip_icon

// Daily life
        "cooking_icon" -> R.drawable.spoon_icon
        "calories_icon" -> R.drawable.energy_icon
        "alcohol_icon" -> R.drawable.alcohol_icon

// Math
        "angles_icon" -> R.drawable.angle_icon
        "graph_icon" -> R.drawable.graph_icon
        "distance_icon" -> R.drawable.distance_icon
        "flux_icon" -> R.drawable.sunny_flux_icon

// Geography
        "coordinates_icon" -> R.drawable.coordinates_icon
        "elevation_icon" -> R.drawable.elevation_icon
        "landarea_icon" -> R.drawable.land_aera_icon
        "mapdistance_icon" -> R.drawable.distance_icon

// Chemistry
        "concentration_icon" -> R.drawable.experiment_icon
        "molarity_icon" -> R.drawable.molarity_icon
        "massfraction_icon" -> R.drawable.mass_faction_icon
        "ph_icon" -> R.drawable.water_ph_icon
        "solubility_icon" -> R.drawable.concentration_icon

// Science
        "viscosity_icon" -> R.drawable.viscosity_icon
        "thermal_icon" -> R.drawable.thermal_conductivity_icon
        "density_icon" -> R.drawable.density_icon
        "heatcapacity_icon" -> R.drawable.heat_capacity_icon
        "heatflux_icon" -> R.drawable.heat_flux_icon

// Transport
        "nautical_icon" -> R.drawable.boat_icon
        "aviation_icon" -> R.drawable.airplane_icon
        "fuel_icon" -> R.drawable.fuel_icon

// Other
        "bmi_icon" -> R.drawable.imt_icon
        "activity_icon" -> R.drawable.run_icon
        "numeration_system_icon" -> R.drawable.numeration_system_icon


        "calculate_icon" -> R.drawable.calculator_icon

        else -> R.drawable.browse_icon
    }
}

