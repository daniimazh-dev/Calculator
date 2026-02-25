package com.daniil.calculator.utilites

fun String.roundTo(
    number: Int = 2,
    indication: Boolean = true,
): String {
    val pointCharIndex = this.indexOf(".")
    val eCharIndex = this.indexOf("E").let { if (it == -1) null else it }
    val eBlock = eCharIndex?.let {this.substring(eCharIndex, this.length)  }
    val indication = if (indication) "..." else ""

    if (pointCharIndex != -1) {
        val first = this.substring(0, pointCharIndex)
        val last = this.substring(pointCharIndex+1, this.length)
        val lastParsed = last.take(number)
        if (number == 0) return  first
        val eCharIndexOfLast = lastParsed.indexOf("E").let { if (it == -1) null else it }
        eCharIndexOfLast?.let {
            return "$first.$last"
        }
        var formated = "$first.$lastParsed"
        formated += if (last.length > number) indication else ""

        eBlock?.let {
            formated += eBlock
        }
        return formated

    } else return this
}


fun Double.roundTo(number: Int): Double {
    return this.toString().roundTo(number, indication = false).toDouble()
}
fun Float.roundTo(number: Int): Float {
    return this.toString().roundTo(number, indication = false).toFloat()
}
fun Float.roundToString(number: Int,  indication: Boolean = true): String {
    return this.toString().roundTo(number, indication = indication)
}

fun Double.roundToString(number: Int, indication: Boolean = true,): String {
    return this.toString().roundTo(number, indication = indication)
}