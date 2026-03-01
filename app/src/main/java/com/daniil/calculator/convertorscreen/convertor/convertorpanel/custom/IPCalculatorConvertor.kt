package com.daniil.calculator.convertorscreen.convertor.convertorpanel.custom

import android.os.NetworkOnMainThreadException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daniil.calculator.R
import com.daniil.calculator.convertorscreen.ConvertorScreenModel
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.ConvertorLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.layout.GroupedLayout
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallInputWithUnit
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.ui.SmallResult
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.convertorComposable.utilites.CopyPasteMenu
import com.daniil.calculator.convertorscreen.convertor.convertorpanel.register.CustomConvertorImplementation
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.convertorscreen.homescreen.dataclass.ConvertorData
import com.daniil.calculator.convertorscreen.validateValue
import java.math.BigInteger
import java.net.InetAddress


class IPCalculatorConvertorImplementation(
    convertorData: ConvertorData,
    model: ConvertorScreenModel,
) : CustomConvertorImplementation(convertorData, model) {
    override fun onCreate() {
        super.onCreate()
        setContent {
            singleViewMode = true
            startViewMode = "IPv4"
            mode(id = "IPv4", painterId = R.drawable.ipv4_icon) {
                content = {
                    IPCalculatorContentIPv4()
                }
                showClackPanel.value = false
            }
            mode(id = "IPv6", painterId = R.drawable.ipv6_icon) {
                content = {
                    IPCalculatorContentIPv6()
                }
                showClackPanel.value = false
            }
//            mode(id = "Network", painterId = R.drawable.wifi_icon) {
//                content = {
//                    Network()
//                }
//                showClackPanel.value = false
//            }
        }
    }
}

@Composable
private fun IPCalculatorConvertorImplementation.IPCalculatorContentIPv4() {
    val context = LocalContext.current
    val content by convertorScreenModel.calckBlock.collectAsState()
    var resultIp by remember { mutableStateOf<IpV4Info?>(null) }
    var isError by remember { mutableStateOf<Boolean>(false) }
    val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "currentUnit",
        defaultValue = getUnits().getOrElse(23) { NullableUnit }
    ) as ConvertorUnit

    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = getUnits(),
        convertorData = convertorData,
        containerColor = Color.Transparent,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SmallInputWithUnit(
            title = "IPv4",
            content = content + "/${currentUnit.symbol}",
            copyPasteMenu = CopyPasteMenu.Full,
            onPaste = { str->
                if (str == null) return@SmallInputWithUnit
                val validate = convertorScreenModel.validateValue(str)
                if (!validate.first) {
                    Toast.makeText(context, validate.second, Toast.LENGTH_SHORT).show()
                    return@SmallInputWithUnit
                }
                convertorScreenModel.setCalck(str)
            },
            currentUnit = currentUnit,
            onChangeUnit = {
                convertorScreenModel.saveParameters {
                    setObject("currentUnit", it)
                }
            },
            onClick = {  },
        )
        LaunchedEffect(content, currentUnit.symbol) {
            val result = IpV4Calculator.calculate(content + "/${currentUnit.symbol}")
            resultIp = result
            isError = false
        }
        if (isError) {
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
        GroupedLayout(
            modifier = Modifier,
            scrollState = rememberScrollState()
        ) {
            SmallResult(
                title = "Network",
                content = (resultIp?.network +  " (${resultIp?.networkClass})"),
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Range",
                content = resultIp?.range ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Category",
                content = resultIp?.category?.name ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Broadcast",
                content = resultIp?.broadcast ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Hosts",
                content = resultIp?.usableHosts ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Total address",
                content = resultIp?.totalIps ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Mask",
                content = resultIp?.subnetMask ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Binary",
                content = resultIp?.binary ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
        }

    }
}


private val ipV6Prefix = (1..128).map { ConvertorUnit(id = it.toString(), name = "", symbol = it.toString()) }

@Composable
private fun IPCalculatorConvertorImplementation.IPCalculatorContentIPv6() {

    val content by convertorScreenModel.calckBlock.collectAsState()
    var resultIp by remember { mutableStateOf<IpV6Info?>(null) }
    var isError by remember { mutableStateOf<Boolean>(false) }
    val currentUnit: ConvertorUnit = convertorScreenModel.getParameter(
        key = "currentUnitIpV6",
        defaultValue = ipV6Prefix.getOrElse(63) { NullableUnit }
    ) as ConvertorUnit

    ConvertorLayout(
        convertorScreenModel = convertorScreenModel,
        unitList = ipV6Prefix,
        convertorData = convertorData,
        containerColor = Color.Transparent,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SmallInputWithUnit(
            title = "IPv6",
            copyPasteMenu = CopyPasteMenu.Full,
            onPaste =  { str ->
                str?.let { convertorScreenModel.setCalck(str) }
            },
            content = content + "/${currentUnit.symbol}",
            currentUnit = currentUnit,
            onChangeUnit = {
                convertorScreenModel.saveParameters {
                    setObject("currentUnitIpV6", it)
                }
            },
            onClick = { },
        )
        LaunchedEffect(content, currentUnit.symbol) {
            val result = Ipv6Calculator.calculate(content + "/${currentUnit.symbol}")
            resultIp = result
            isError = false
        }
        if (isError) {
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
        GroupedLayout(
            modifier = Modifier,
            scrollState = rememberScrollState()
        ) {
            SmallResult(
                title = "Network",
                content = resultIp?.network ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Full address",
                content = resultIp?.fullAddress ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Total address",
                content = resultIp?.totalAddresses ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Prefix mask",
                content = resultIp?.prefixMask ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Range start",
                content = resultIp?.rangeStart ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Range end",
                content = resultIp?.rangeEnd ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "First address",
                content = resultIp?.firstIp ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )
            SmallResult(
                title = "Last address",
                content = resultIp?.lastIp ?: "—",
                copyPasteMenu = CopyPasteMenu.CopyOnly,
                reversedLayout = true,
                groupedValue = it,
                onClick = { }
            )

        }

    }
}

@Composable
private fun IPCalculatorConvertorImplementation.Network() {

}


data class IpV4Info(
    val address: String,
    val cidr: Int,
    val subnetMask: String,
    val wildcardMask: String,
    val binary: String,
    val category: Ipv4Category,
    val network: String,
    val networkClass: String,
    val firstIp: String,
    val lastIp: String,
    val broadcast: String,
    val range: String,
    val usableHosts: Long,
    val totalIps: Long
)

data class IpV6Info(
    val address: String,
    val fullAddress: String,
    val network: String,
    val prefixLength: Int,
    val prefixMask: String,
    val firstIp: String,
    val lastIp: String,
    val rangeStart: String,
    val rangeEnd: String,
    val totalAddresses: BigInteger
)

object IpCalculator {
    fun parse(input: String): Pair<Ipv4Address, Int>? {
        val parts = input.split("/")
        val ip = parts[0]
        val cidr = parts.getOrNull(1)?.toIntOrNull()

        if (!isValidIpv4(ip)) return null

        val finalCidr = cidr ?: defaultCidr(ip) ?: return null
        if (finalCidr !in 0..32) return null

        return Ipv4Address(ipToInt(ip)) to finalCidr
    }

    fun calculate(ip: Ipv4Address, cidr: Int): Subnet {

        val mask = cidrToMask(cidr)
        val network = ip.value and mask
        val broadcast = network or mask.inv()

        val total = 1L shl (32 - cidr)

        val usable = when (cidr) {
            32 -> 1
            31 -> 2
            else -> total - 2
        }

        return Subnet(
            network = Ipv4Address(network),
            broadcast = Ipv4Address(broadcast),
            firstHost = if (cidr < 31) Ipv4Address(network + 1) else Ipv4Address(network),
            lastHost = if (cidr < 31) Ipv4Address(broadcast - 1) else Ipv4Address(broadcast),
            cidr = cidr,
            totalHosts = total,
            usableHosts = usable
        )
    }


    fun category(ip: Ipv4Address): Ipv4Category {
        val v = ip.value
        return when {
            v in ipRange("10.0.0.0", "10.255.255.255") -> Ipv4Category.PRIVATE
            v in ipRange("172.16.0.0", "172.31.255.255") -> Ipv4Category.PRIVATE
            v in ipRange("192.168.0.0", "192.168.255.255") -> Ipv4Category.PRIVATE
            v in ipRange("127.0.0.0", "127.255.255.255") -> Ipv4Category.LOOPBACK
            v in ipRange("169.254.0.0", "169.254.255.255") -> Ipv4Category.LINK_LOCAL
            v in ipRange("224.0.0.0", "239.255.255.255") -> Ipv4Category.MULTICAST
            else -> Ipv4Category.PUBLIC
        }
    }


    private fun ipRange(start: String, end: String) =
        ipToInt(start)..ipToInt(end)

    private fun ipToInt(ip: String): Int =
        ip.split(".")
            .map { it.toInt() }
            .fold(0) { acc, octet -> (acc shl 8) or octet }

    private fun cidrToMask(cidr: Int): Int =
        if (cidr == 0) 0 else (-1 shl (32 - cidr))

    private fun isValidIpv4(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { it.toIntOrNull()?.let { n -> n in 0..255 } ?: false }
    }

    private fun defaultCidr(ip: String): Int? {
        val first = ip.substringBefore(".").toIntOrNull() ?: return null
        return when (first) {
            in 1..126 -> 8
            in 128..191 -> 16
            in 192..223 -> 24
            else -> null
        }
    }
}
enum class Ipv4Category {
    PRIVATE, PUBLIC, LOOPBACK, LINK_LOCAL, MULTICAST, RESERVED
}


object Ipv6Calculator {

    fun calculate(input: String): IpV6Info? {

        val parts = input.split("/")
        if (parts.size != 2) return null

        val ipString = parts[0]
        val prefix = parts[1].toIntOrNull() ?: return null
        if (prefix !in 0..128) return null
        val inet = try {
            InetAddress.getByName(ipString)
        } catch (e: NetworkOnMainThreadException) {
            return null
        }

        if (inet.address.size != 16) return null

        val ipBig = BigInteger(1, inet.address)

        val mask = prefixToMask(prefix)
        val network = ipBig.and(mask)
        val last = network.add(mask.not().and(MAX_IPV6))

        val total = BigInteger.ONE.shiftLeft(128 - prefix)

        return IpV6Info(
            address = ipString,
            fullAddress = expandIpv6(ipBig),
            network = compressIpv6(network),
            prefixLength = prefix,
            prefixMask = compressIpv6(mask),
            firstIp = compressIpv6(network),
            lastIp = compressIpv6(last),
            rangeStart = expandIpv6(network),
            rangeEnd = expandIpv6(last),
            totalAddresses = total
        )
    }

    // =========================
    // Mask generator
    // =========================

    private fun prefixToMask(prefix: Int): BigInteger {
        return if (prefix == 0) {
            BigInteger.ZERO
        } else {
            BigInteger.ONE.shiftLeft(128)
                .subtract(BigInteger.ONE)
                .shiftRight(128 - prefix)
                .shiftLeft(128 - prefix)
        }
    }

    private val MAX_IPV6 =
        BigInteger.ONE.shiftLeft(128).subtract(BigInteger.ONE)

    // =========================
    // Formatting
    // =========================

    private fun expandIpv6(value: BigInteger): String {

        val raw = value.toByteArray()

        val bytes = when {
            raw.size == 16 -> raw
            raw.size < 16 -> ByteArray(16 - raw.size) + raw
            raw.size > 16 -> raw.copyOfRange(raw.size - 16, raw.size)
            else -> raw
        }

        return bytes
            .toList()
            .chunked(2)
            .joinToString(":") {
                "%02x%02x".format(it[0], it[1])
            }
    }

    private fun compressIpv6(value: BigInteger): String {

        val raw = value.toByteArray()

        val bytes = when {
            raw.size == 16 -> raw
            raw.size < 16 -> ByteArray(16 - raw.size) + raw
            raw.size > 16 -> raw.copyOfRange(raw.size - 16, raw.size)
            else -> raw
        }

        return InetAddress.getByAddress(bytes).hostAddress
    }
}

object IpV4Calculator {

    fun calculate(input: String): IpV4Info? {

        val (ipObj, cidr) = IpCalculator.parse(input) ?: return null
        val subnet = IpCalculator.calculate(ipObj, cidr)

        val mask = cidrToMask(cidr)
        val wildcard = mask.inv()

        val networkClass = getClassName(ipObj)

        return IpV4Info(
            address = ipObj.toStringIp(),
            cidr = cidr,
            subnetMask = intToIp(mask),
            wildcardMask = intToIp(wildcard),
            binary = ipObj.toBinary(),
            category = IpCalculator.category(ipObj),
            network = "${subnet.network.toStringIp()}/$cidr",
            networkClass = networkClass,
            firstIp = subnet.firstHost?.toStringIp() ?: "—",
            lastIp = subnet.lastHost?.toStringIp() ?: "—",
            broadcast = subnet.broadcast.toStringIp(),
            range = "${subnet.network.toStringIp()} - ${subnet.broadcast.toStringIp()}",
            usableHosts = subnet.usableHosts,
            totalIps = subnet.totalHosts
        )
    }

    private fun getClassName(ip: Ipv4Address): String {
        val first = ip.toStringIp().substringBefore(".").toInt()
        return when (first) {
            in 1..126 -> "Class A"
            in 128..191 -> "Class B"
            in 192..223 -> "Class C"
            in 224..239 -> "Class D"
            else -> "Class E"
        }
    }

    private fun cidrToMask(cidr: Int) =
        if (cidr == 0) 0 else (-1 shl (32 - cidr))

    private fun intToIp(ip: Int) =
        listOf(
            (ip shr 24) and 0xFF,
            (ip shr 16) and 0xFF,
            (ip shr 8) and 0xFF,
            ip and 0xFF
        ).joinToString(".")
}
data class Subnet(
    val network: Ipv4Address,
    val broadcast: Ipv4Address,
    val firstHost: Ipv4Address?,
    val lastHost: Ipv4Address?,
    val cidr: Int,
    val totalHosts: Long,
    val usableHosts: Long
)

data class Ipv4Address(val value: Int) {

    fun toStringIp(): String =
        listOf(
            (value shr 24) and 0xFF,
            (value shr 16) and 0xFF,
            (value shr 8) and 0xFF,
            value and 0xFF
        ).joinToString(".")

    fun toBinary(): String =
        toStringIp().split(".")
            .joinToString(".") {
                it.toInt().toString(2).padStart(8, '0')
            }

    fun reverseDns(): String =
        toStringIp().split(".").reversed().joinToString(".") + ".in-addr.arpa"
}