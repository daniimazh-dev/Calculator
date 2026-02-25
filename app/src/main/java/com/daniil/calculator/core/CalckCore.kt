package com.daniil.calculator.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object CalculatorCore {
    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Op(val op: Char) : Token()
        data class Func(val name: String) : Token() // sin, cos, tan, log, sqrt, etc.
        object Percent : Token()
        object LParen : Token()
        object RParen : Token()
        object End : Token()
        object Factorial : Token()
        object Abs : Token() // '|'
    }

    // Значення з інформацією про кількість знакiв %
    private data class Value(val number: Double, val percentCount: Int = 0)

    // Головна функція — повертає рядок або null
    fun evaluate(input: String): String? {
        return try {
            val tokens = tokenize(input)
            val parser = Parser(tokens)
            val resultValue = parser.parseExpression()
            if (!parser.atEnd()) return null // лишні токени => помилка
            val numeric = toNumeric(resultValue)
            if (numeric.isNaN() || numeric.isInfinite()) null else formatENumber(numeric)
        } catch (t: Throwable) {
            // будь-яка помилка парсингу/обчислення — "Error" => тут повертаємо null
            null
        }
    }

    // ------------------- Токенізатор -------------------
    private fun tokenize(s: String): List<Token> {
        val out = mutableListOf<Token>()
        var i = 0
        val str = s.trim()
        fun startsIdentifier(id: String): Boolean {
            return str.regionMatches(i, id, 0, id.length, ignoreCase = true)
        }

        while (i < str.length) {
            val c = str[i]
            when {
                c.isWhitespace() -> i++

                // числа з підтримкою експоненційної нотації 1.23E+4, .5E-3, 5, 3. -> 3.0
                c.isDigit() || c == '.' -> {
                    val start = i
                    // integer/frac part
                    while (i < str.length && (str[i].isDigit() || str[i] == '.')) i++
                    // exponent if present
                    if (i < str.length && (str[i] == 'E' || str[i] == 'e')) {
                        i++
                        if (i < str.length && (str[i] == '+' || str[i] == '-')) i++
                        // exponent digits
                        while (i < str.length && str[i].isDigit()) i++
                    }
                    val numText = str.substring(start, i)
                    val num = numText.toDoubleOrNull() ?: throw IllegalArgumentException("Bad number: $numText")
                    out.add(Token.Number(num))
                }

                // функції: sin, cos, tan, log, sqrt (регістр не важливий)
                startsIdentifier("sin") -> {
                    out.add(Token.Func("sin")); i += 3
                }

                startsIdentifier("cos") -> {
                    out.add(Token.Func("cos")); i += 3
                }

                startsIdentifier("tan") -> {
                    out.add(Token.Func("tan")); i += 3
                }

                startsIdentifier("log") -> {
                    out.add(Token.Func("log")); i += 3
                }

                startsIdentifier("sqrt") -> {
                    out.add(Token.Func("sqrt")); i += 4
                }
                c == '√' -> { // альтернатива символом
                    out.add(Token.Func("sqrt")); i++
                }

                // дужки та оператори
                c == '(' -> { out.add(Token.LParen); i++ }
                c == ')' -> { out.add(Token.RParen); i++ }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' -> {
                    out.add(Token.Op(c)); i++
                }

                c == '%' -> { out.add(Token.Percent); i++ }
                c == '!' -> { out.add(Token.Factorial); i++ }

                c == '|' -> { out.add(Token.Abs); i++ }

                else -> throw IllegalArgumentException("Unknown char: $c at position $i")
            }
        }
        out.add(Token.End)
        return out
    }

    // ------------------- Парсер (recursive descent) -------------------
    private class Parser(private val tokens: List<Token>) {
        private var pos = 0
        private fun peek() = tokens[pos]
        private fun next() = tokens[pos++]

        fun atEnd() = peek() is Token.End

        // expression := term (('+'|'-') term)*
        fun parseExpression(): Value {
            var left = parseTerm()
            loop@ while (true) {
                when (val p = peek()) {
                    is Token.Op -> {
                        val op = p.op
                        if (op == '+' || op == '-') {
                            next()
                            val right = parseTerm()
                            left = applyOp(left, op, right)
                        } else break@loop
                    }
                    else -> break@loop
                }
            }
            return left
        }

        // term := factor (('*'|'/') factor)*
        private fun parseTerm(): Value {
            var left = parseFactor()
            loop@ while (true) {
                when (val p = peek()) {
                    is Token.Op -> {
                        val op = p.op
                        if (op == '*' || op == '/') {
                            next()
                            val right = parseFactor()
                            left = applyOp(left, op, right)
                        } else break@loop
                    }
                    else -> break@loop
                }
            }
            return left
        }

        // factor := power ('^' factor)?  (правоасоц.)
        private fun parseFactor(): Value {
            var left = parseUnary()
            if (peek() is Token.Op && (peek() as Token.Op).op == '^') {
                // right-associative
                next()
                val right = parseFactor()
                val leftNum = toNumeric(left)
                val rightNum = toNumeric(right)
                // обробка негативних степенів і т.д.
                val res = leftNum.pow(rightNum)
                return Value(res, 0)
            }
            return left
        }

        // unary := ('+'|'-') unary | primary
        private fun parseUnary(): Value {
            return when (val p = peek()) {
                is Token.Op -> {
                    if (p.op == '+') {
                        next()
                        parseUnary()
                    } else if (p.op == '-') {
                        next()
                        val v = parseUnary()
                        Value(-toNumeric(v), 0)
                    } else parsePrimary()
                }
                else -> parsePrimary()
            }
        }

        // primary := number ('%')* ('!')* | func '(' expr ')' ('%')* ('!')* | '|' expr '|' ('%')* ('!')* | '(' expr ')' ('%')* ('!')*
        private fun parsePrimary(): Value {
            return when (val p = peek()) {
                is Token.Number -> {
                    val num = (next() as Token.Number).value
                    var pct = 0
                    while (peek() is Token.Percent) { pct++; next() }
                    var v = Value(num, pct)
                    // факторіал/постфіксні оператори після числа або процентів
                    while (peek() is Token.Factorial) {
                        next()
                        v = Value(factorial(toNumeric(v)), 0)
                    }
                    v
                }

                is Token.Func -> {
                    val funcName = (next() as Token.Func).name.lowercase()
                    // функція повинна мати аргумент у дужках
                    if (peek() !is Token.LParen) throw IllegalArgumentException("Expected ( after function $funcName")
                    next() // '('
                    val arg = parseExpression()
                    if (peek() !is Token.RParen) throw IllegalArgumentException("Missing ) after function arg")
                    next() // ')'
                    var res = when (funcName) {
                        "sin" -> sin(toNumeric(arg))
                        "cos" -> cos(toNumeric(arg))
                        "tan" -> tan(toNumeric(arg))
                        "log" -> ln(toNumeric(arg)) // натуральний логарифм
                        "sqrt" -> {
                            val x = toNumeric(arg)
                            if (x < 0.0) throw IllegalArgumentException("sqrt of negative")
                            sqrt(x)
                        }
                        else -> throw IllegalArgumentException("Unknown function $funcName")
                    }
                    // процентні знаки після виклику функції, наприклад sin(...)% (зменшення в 100^k)
                    var pct = 0
                    while (peek() is Token.Percent) { pct++; next() }
                    var v = Value(res, pct)
                    // факторіал (якщо користувач захотів) — застосуємо до числового значення
                    while (peek() is Token.Factorial) {
                        next()
                        v = Value(factorial(toNumeric(v)), 0)
                    }
                    v
                }

                is Token.Abs -> {
                    next() // пропускаємо '|'
                    val inner = parseExpression()
                    if (peek() !is Token.Abs) throw IllegalArgumentException("Missing |")
                    next() // закриваюча '|'
                    var res = abs(toNumeric(inner))
                    var pct = 0
                    while (peek() is Token.Percent) { pct++; next() }
                    var v = Value(res, pct)
                    while (peek() is Token.Factorial) {
                        next()
                        v = Value(factorial(toNumeric(v)), 0)
                    }
                    v
                }

                is Token.LParen -> {
                    next() // '('
                    val v = parseExpression()
                    if (peek() !is Token.RParen) throw IllegalArgumentException("Missing )")
                    next() // ')'
                    var pct = v.percentCount
                    // проценти після дужок
                    while (peek() is Token.Percent) { pct++; next() }
                    var resV = Value(v.number, pct)
                    // факторіал після групи
                    while (peek() is Token.Factorial) {
                        next()
                        resV = Value(factorial(toNumeric(resV)), 0)
                    }
                    resV
                }

                else -> throw IllegalArgumentException("Unexpected token in primary: $p")
            }
        }

        // Застосування бінарних операторів з урахуванням %
        private fun applyOp(left: Value, op: Char, right: Value): Value {
            val leftNum = toNumeric(left)
            return when (op) {
                '+' -> {
                    if (right.percentCount > 0) {
                        // a + b% => a + a * (b / 100^k)
                        val rightFrac = right.number / (100.0.pow(right.percentCount.toDouble()))
                        Value(leftNum + leftNum * rightFrac, 0)
                    } else {
                        Value(leftNum + toNumeric(right), 0)
                    }
                }

                '-' -> {
                    if (right.percentCount > 0) {
                        val rightFrac = right.number / (100.0.pow(right.percentCount.toDouble()))
                        Value(leftNum - leftNum * rightFrac, 0)
                    } else {
                        Value(leftNum - toNumeric(right), 0)
                    }
                }

                '*' -> {
                    // multiplicative: treat percent as its numeric fraction
                    val rightNum = toNumeric(right) // already divides by 100^k
                    Value(leftNum * rightNum, 0)
                }

                '/' -> {
                    val rightNum = toNumeric(right)
                    if (rightNum == 0.0) throw ArithmeticException("Division by zero")
                    Value(leftNum / rightNum, 0)
                }

                else -> throw IllegalArgumentException("Unknown op $op")
            }
        }
    }

    // ------------------- Перетворення Value у число (з урахуванням %) -------------------
    private fun toNumeric(v: Value): Double =
        if (v.percentCount > 0) v.number / (100.0.pow(v.percentCount.toDouble())) else v.number

    // ------------------- Факторіал (працює для невід'ємних цілих) -------------------
    private fun factorial(n: Double): Double {
        if (n < 0.0) throw IllegalArgumentException("Factorial only for non-negative integers")
        if (n % 1.0 != 0.0) throw IllegalArgumentException("Factorial only for integers")
        val ni = n.toInt()
        // обчислюємо у Double (можна переповнення => Infinity)
        var res = 1.0
        for (i in 1..ni) {
            res *= i.toDouble()
            // якщо стало Infinite — повертаємо Infinity (далі evaluate відфільтрує)
            if (res.isInfinite()) return res
        }
        return res
    }

    // ------------------- Форматування числа -------------------
    fun formatENumber(d: Double): String? {
        if (d.isNaN() || d.isInfinite()) return null

        val absD = abs(d)

        // Порогові значення: великі > 1e12 або дуже малі < 1e-6 (але не нуль) — показувати в E-нотації
        if (absD >= 1e12 || (absD != 0.0 && absD < 1e-6)) {
            val df = DecimalFormat("0.########E0", DecimalFormatSymbols(Locale.US))
            return df.format(d)
        }

        return if (d % 1.0 == 0.0) {
            // ціле
            try {
                d.toLong().toString()
            } catch (e: Exception) {
                // на випадок надвеликих чисел — fallback у наукову нотацію
                val df = DecimalFormat("0.########E0", DecimalFormatSymbols(Locale.US))
                df.format(d)
            }
        } else {
            // десяткове зі стріпом зайвих нулів, до 10 знаків після коми
            val bd = BigDecimal.valueOf(d).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
            // Якщо після обрізки ми отримали експоненціальний формат у BigDecimal, toPlainString може бути дуже довгим,
            // але для наших порогів це ок
            bd.toPlainString()
        }
    }
    fun unformatENumber(value: String): String? {
        return try {
            if (!value.contains('E', true)) {
                value // вже звичайне число
            } else {
                BigDecimal(value).toPlainString()
            }
        } catch (e: Exception) {
            null
        }
    }

}
