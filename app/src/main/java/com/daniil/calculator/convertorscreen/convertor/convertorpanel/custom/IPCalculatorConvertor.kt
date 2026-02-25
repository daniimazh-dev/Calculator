package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.BigInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInput
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData


class IPCalculatorConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {
    override fun onCreate() {
        super.onCreate()
        setContent {
            singleViewMode = true
            startViewMode = "IPv4"
            mode(id ="IPv4", painterId = R.drawable.ipv4_icon) {
                content = {
                    IPCalculatorContentIPv4()
                }
                showClackPanel.value = false
            }
            mode(id ="IPv6", painterId = R.drawable.ipv6_icon) {
                content = {
                    IPCalculatorContentIPv6()
                }
                showClackPanel.value = false
            }
        }
    }
}

@Composable
private fun IPCalculatorConvertorImplementation.IPCalculatorContentIPv4() {
    val content by convertorScreenModel.calckBlock.collectAsState()
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = convertorData,
        containerColor = Color.Transparent
    ) {
        SmallInput(
            title = "IPv4",
            content = content
        ) {}
        if (isValidIpv4(content)) {

        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Invalid ip address",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

    }
}


@Composable
private fun IPCalculatorConvertorImplementation.IPCalculatorContentIPv6() {
    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = convertorData,
        containerColor = Color.Transparent
    ) {

    }
}



private fun ipToInt(ip: String): Int {
    return ip.split(".")
        .map { it.toInt() }
        .fold(0) { acc, octet -> (acc shl 8) or octet }
}

private fun intToIp(ip: Int): String {
    return listOf(
        (ip shr 24) and 0xFF,
        (ip shr 16) and 0xFF,
        (ip shr 8) and 0xFF,
        ip and 0xFF
    ).joinToString(".")
}


private fun cidrToMask(cidr: Int): Int {
    return if (cidr == 0) 0 else (-1 shl (32 - cidr))
}


private data class IpResult(
    val network: String,
    val broadcast: String,
    val firstHost: String,
    val lastHost: String,
    val hosts: Int
)

private enum class IpType {
    IPV4, IPV6, INVALID
}

private fun detectIpType(input: String): IpType {
    return when {
        isValidIpv4(input) -> IpType.IPV4
        isValidIpv6(input) -> IpType.IPV6
        else -> IpType.INVALID
    }
}

private fun safeCalculateIp(
    ip: String,
    cidr: Int? = null
): Result<IpResult> {

    if (!isValidIpv4(ip)) {
        return Result.failure(IllegalArgumentException("Invalid IPv4 address"))
    }

    val finalCidr = cidr ?: defaultCidrForIpv4(ip)
    ?: return Result.failure(IllegalArgumentException("Cannot determine CIDR"))

    if (finalCidr !in 0..32) {
        return Result.failure(IllegalArgumentException("Invalid CIDR"))
    }

    return Result.success(calculateIp(ip, finalCidr))
}


fun defaultCidrForIpv4(ip: String): Int? {
    if (!isValidIpv4(ip)) return null

    val firstOctet = ip.substringBefore(".").toInt()

    return when (firstOctet) {
        in 1..126 -> 8     // Class A
        in 128..191 -> 16  // Class B
        in 192..223 -> 24  // Class C
        else -> null       // Multicast / Reserved
    }
}


private fun isValidIpv6(ip: String): Boolean {
    if (ip.count { it == ':' } < 2) return false

    val parts = ip.split("::")
    if (parts.size > 2) return false

    fun isValidBlock(block: String): Boolean =
        block.isNotEmpty() &&
                block.length <= 4 &&
                block.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }

    val left = parts[0]
        .takeIf { it.isNotEmpty() }
        ?.split(":")
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    val right = parts.getOrNull(1)
        ?.takeIf { it.isNotEmpty() }
        ?.split(":")
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    if (left.size + right.size > 8) return false
    return (left + right).all(::isValidBlock)
}


private fun isValidIpv4(ip: String): Boolean {
    val parts = ip.split(".")
    if (parts.size != 4) return false

    return parts.all { part ->
        part.toIntOrNull()?.let { it in 0..255 } ?: false
    }
}


private fun calculateIp(ip: String, cidr: Int): IpResult {
    val ipInt = ipToInt(ip)
    val mask = cidrToMask(cidr)

    val network = ipInt and mask
    val broadcast = network or mask.inv()

    val hosts = if (cidr >= 31) 0 else (1 shl (32 - cidr)) - 2

    return IpResult(
        network = intToIp(network),
        broadcast = intToIp(broadcast),
        firstHost = if (hosts > 0) intToIp(network + 1) else "-",
        lastHost = if (hosts > 0) intToIp(broadcast - 1) else "-",
        hosts = hosts
    )
}
