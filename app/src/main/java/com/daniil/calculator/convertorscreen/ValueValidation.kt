package com.daniil.calculator.convertorscreen

import com.daniil.calculator.convertorscreen.ValidationParam.Except
import com.daniil.calculator.convertorscreen.ValidationParam.Rule
import com.daniil.calculator.convertorscreen.convertor.unit.ConvertorUnit
import com.daniil.calculator.convertorscreen.convertor.unit.NullableUnit
import com.daniil.calculator.settingsscreen.customscreen.logs.LogManager

private open class ValidationMessages() {
    // exception
    val valueIsEmpty = "Value is empty"
    val valueIsString = "Value is text"
    val convertorIsNull = "Converter not found"

    val mustNegative = "Value must be negative"
    val mustPositive = "Value must be positive"
    val nonZero = "Value must not be a zero"
    val nonFloat = "Value must not be a fraction"
    val moreThen: (value: Any) -> String = { value -> "Value must be more then $value" }
    val lessThen: (value: Any) -> String = { value -> "Value must be less then $value" }
    val inRange: (value1: Any, value2: Any) -> String =
        { value1, value2 -> "Value must be in range from $value1 to $value2" }
    val nonEquals: (listValue: List<Any>) -> String =
        { listValue -> "Value must not be ${listValue.joinToString()}" }
    val constraints: (listValue: List<Any>) -> String =
        { listValue -> "Value must constrains ${listValue.joinToString()}" }

    // critical exception
    val validationError = "Validation error. See the logs"
    val unitIsNullable = "Unit is nullable"

    // massage
    val convertorNoReq = "Convertor has no requirements"
    val unitNotReq = "Unit has no requirements"
    val ignoreConvertor = "Ignore current convertor"
    val notFoundRule = "Not found rule"
}


private class Validate(
    value: String?,
    val convertorScreenModel: ConvertorScreenModel,
) : ValidationMessages() {
    val value = value.takeIf { !it.isNullOrEmpty() }

    fun validate(
        rule: ValidationParam,
    ): Pair<Boolean, String?> {
        value ?: return (false to valueIsEmpty)

        fun Except.notConstrains(onPredicate: () -> Unit) {
            if (rule.except?.find { it == this } == null) onPredicate()
        }
        try {
            val exceptList = mutableListOf<Pair<Boolean, String?>>()
            // verification except
            Except.IsString.notConstrains {
                value.toFloatOrNull() ?: exceptList.add(false to valueIsString)
            }
            if (!exceptList.all { it.first }) return (false to exceptList.joinToString { it.second.toString() })

            // rules
            val ruleArgs = rule.args.map { it.toString() }
            if (rule.rule == null) return (true to notFoundRule)
            var result = when (rule.rule) {
                Rule.Negative -> (value.toFloat() <= 0f) to mustNegative
                Rule.Positive -> (value.toFloat() >= 0f) to mustPositive
                Rule.NonZero -> (value.toFloat() != 0f) to nonZero
                Rule.NonFloat -> (!(value.contains('.'))) to nonFloat
                Rule.MoreThen -> (value.toFloat() > ruleArgs[0].toFloat()) to moreThen(ruleArgs[0])
                Rule.LessThen -> (value.toFloat() < ruleArgs[0].toFloat()) to lessThen(ruleArgs[0])
                Rule.InRange -> (value.toFloat() in ruleArgs[0].toFloat()..ruleArgs[1].toFloat()) to inRange(ruleArgs[0], ruleArgs[1])
                Rule.NonEquals -> (value.toFloat() !in ruleArgs.map { it.toFloat() }) to nonEquals(ruleArgs)
                Rule.Constrain -> (ruleArgs.map { value.contains(it) }.all { it }) to constraints(ruleArgs)
            }
            // inverse
            result = if (rule.inverseRule) {
                !result.first to ("Not " + result.second)
            } else result
            return result

        } catch (e: Exception) {
           LogManager.e(
                "ValidationValue",
                "Except in validation rule: \"${rule.rule?.name}\", except: \"${rule.except?.joinToString { it.name }}\", rule args: \"${rule.args.joinToString()}}\", Exception: " +
                        "\n ${e.toString().take(100)}"
            )
            return (false to validationError)
        }
    }

    fun validate(
        ruleList: List<ValidationParam>,
    ): Pair<Boolean, String?> {
        try {
            val resultList = mutableListOf<Pair<Boolean, String?>>()
            ruleList.forEach { rule ->
                resultList.add(validate(rule))
            }
            return if (resultList.all { it.first }) {
                true to resultList.joinToString { it.second ?: "" }
            } else {
                false to resultList.filter { !it.first }.joinToString { it.second ?: "" }
            }

        } catch (e: Exception) {
            LogManager.e(
                "ValidationValue",
                "Except in validation: ${e.toString().take(100)}"
            )
            return (false to validationError)
        }
    }
}

fun ConvertorScreenModel.validateValue(
    value: String?,
    ignoreConvertor: Boolean = false,
    unit: ConvertorUnit? = null,
    rules: ValidationParam? = null,
): Pair<Boolean, String?> = with(Validate(value, this)) {
    // general
    val convertor = currentConvertor.value ?: return (false to convertorIsNull)
    if (unit == NullableUnit) return (false to unitIsNullable)

    // convertors
    val nonNegativeConvertors = listOf("Length", "Area", "Mass", "Volume", "Currency", "Discount", "Area", "Time", "BMI", "IP_calculator")

    if (rules != null) {
        val result = validate(rules)
        if (!result.first) return result
    }

    if (unit != null) {
        // UNITS
        val result = when (unit.id) {
            else -> {
                LogManager.d("ValidationValue", unitNotReq + "\nUnit:  ${unit.id}")
                (true to unitNotReq)
            }
        }
        if (!result.first) return result
    }


    if (!ignoreConvertor) {
        // CONVERTORS
        return when (convertor.id) {
            in nonNegativeConvertors -> validate(ValidationParam(rule = Rule.Positive))
            "Numeration_system" -> validate(ValidationParam(except = listOf(Except.IsString)))

            else -> {
                LogManager.d("ValidationValue", convertorNoReq + "\nConvertor:  ${convertor.id}")
                (true to convertorNoReq)
            }
        }
    } else {
        LogManager.d("ValidationValue", this.ignoreConvertor + "\nConvertor: ${convertor.id}")
        return true to this.ignoreConvertor
    }

}

data class ValidationParam(
    val rule: Rule? = null,
    val except: List<Except>? = null,
    val inverseRule: Boolean = false,
    val args: List<Any> = emptyList(),
) {
    enum class Rule {
        Negative,
        Positive,
        NonZero,
        NonFloat,
        MoreThen,
        LessThen,
        InRange,
        NonEquals,
        Constrain
    }

    enum class Except {
        IsString,
    }
}

private operator fun ValidationParam.plus(other: ValidationParam): List<ValidationParam> {
    return listOf(this, other)
}

private operator fun List<ValidationParam>.plus(other: ValidationParam): List<ValidationParam> {
    return this + listOf(other)
}
