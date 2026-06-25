package com.lockedin.feature.tools.converter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class ConversionCategory(val displayName: String) {
    LENGTH("Length"),
    MASS("Mass"),
    TEMPERATURE("Temperature"),
    AREA("Area"),
    VOLUME("Volume")
}

data class ConverterUiState(
    val category: ConversionCategory = ConversionCategory.LENGTH,
    val inputValue: String = "",
    val fromUnit: String = "m",
    val toUnit: String = "km",
    val result: String = ""
)

@HiltViewModel
class ConverterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    fun setCategory(category: ConversionCategory) {
        val defaultUnits = getUnitsForCategory(category)
        _uiState.update {
            it.copy(
                category = category,
                fromUnit = defaultUnits.first(),
                toUnit = defaultUnits.getOrElse(1) { defaultUnits.first() },
                inputValue = "",
                result = ""
            )
        }
    }

    fun setFromUnit(unit: String) {
        _uiState.update { it.copy(fromUnit = unit) }
        convert()
    }

    fun setToUnit(unit: String) {
        _uiState.update { it.copy(toUnit = unit) }
        convert()
    }

    fun setInputValue(value: String) {
        _uiState.update { it.copy(inputValue = value) }
        convert()
    }

    fun swapUnits() {
        _uiState.update {
            it.copy(
                fromUnit = it.toUnit,
                toUnit = it.fromUnit,
                inputValue = it.result,
                result = it.inputValue
            )
        }
    }

    private fun convert() {
        val state = _uiState.value
        val input = state.inputValue.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(result = "") }
            return
        }

        val result = when (state.category) {
            ConversionCategory.LENGTH -> convertLength(input, state.fromUnit, state.toUnit)
            ConversionCategory.MASS -> convertMass(input, state.fromUnit, state.toUnit)
            ConversionCategory.TEMPERATURE -> convertTemperature(input, state.fromUnit, state.toUnit)
            ConversionCategory.AREA -> convertArea(input, state.fromUnit, state.toUnit)
            ConversionCategory.VOLUME -> convertVolume(input, state.fromUnit, state.toUnit)
        }

        val formatted = if (result == result.toLong().toDouble() && result < 1e15) {
            result.toLong().toString()
        } else {
            String.format("%.6g", result)
        }
        _uiState.update { it.copy(result = formatted) }
    }

    fun getUnitsForCategory(category: ConversionCategory): List<String> = when (category) {
        ConversionCategory.LENGTH -> listOf("mm", "cm", "m", "km", "in", "ft", "mi")
        ConversionCategory.MASS -> listOf("mg", "g", "kg", "lb", "oz")
        ConversionCategory.TEMPERATURE -> listOf("°C", "°F", "K")
        ConversionCategory.AREA -> listOf("cm²", "m²", "km²", "ft²", "acre")
        ConversionCategory.VOLUME -> listOf("ml", "L", "fl oz", "cup", "gallon")
    }

    // Length conversion (base unit: meters)
    private fun convertLength(value: Double, from: String, to: String): Double {
        val toMeters = mapOf(
            "mm" to 0.001, "cm" to 0.01, "m" to 1.0, "km" to 1000.0,
            "in" to 0.0254, "ft" to 0.3048, "mi" to 1609.344
        )
        val meters = value * (toMeters[from] ?: 1.0)
        return meters / (toMeters[to] ?: 1.0)
    }

    // Mass conversion (base unit: grams)
    private fun convertMass(value: Double, from: String, to: String): Double {
        val toGrams = mapOf(
            "mg" to 0.001, "g" to 1.0, "kg" to 1000.0,
            "lb" to 453.592, "oz" to 28.3495
        )
        val grams = value * (toGrams[from] ?: 1.0)
        return grams / (toGrams[to] ?: 1.0)
    }

    // Temperature conversion
    private fun convertTemperature(value: Double, from: String, to: String): Double {
        val celsius = when (from) {
            "°C" -> value
            "°F" -> (value - 32) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }
        return when (to) {
            "°C" -> celsius
            "°F" -> celsius * 9.0 / 5.0 + 32
            "K" -> celsius + 273.15
            else -> celsius
        }
    }

    // Area conversion (base unit: m²)
    private fun convertArea(value: Double, from: String, to: String): Double {
        val toSqMeters = mapOf(
            "cm²" to 0.0001, "m²" to 1.0, "km²" to 1_000_000.0,
            "ft²" to 0.092903, "acre" to 4046.86
        )
        val sqMeters = value * (toSqMeters[from] ?: 1.0)
        return sqMeters / (toSqMeters[to] ?: 1.0)
    }

    // Volume conversion (base unit: ml)
    private fun convertVolume(value: Double, from: String, to: String): Double {
        val toMl = mapOf(
            "ml" to 1.0, "L" to 1000.0, "fl oz" to 29.5735,
            "cup" to 236.588, "gallon" to 3785.41
        )
        val ml = value * (toMl[from] ?: 1.0)
        return ml / (toMl[to] ?: 1.0)
    }
}
