package com.lockedin.feature.tools.calculator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.*

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val history: List<String> = emptyList()
)

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onInput(value: String) {
        _uiState.update { state ->
            state.copy(expression = state.expression + value)
        }
    }

    fun onClear() {
        _uiState.update { CalculatorUiState(history = it.history) }
    }

    fun onAllClear() {
        _uiState.update { CalculatorUiState() }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.expression.isNotEmpty()) {
                // Handle multi-char functions like sin(, cos(, etc.
                val funcs = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
                val matchedFunc = funcs.find { state.expression.endsWith(it) }
                if (matchedFunc != null) {
                    state.copy(expression = state.expression.dropLast(matchedFunc.length))
                } else {
                    state.copy(expression = state.expression.dropLast(1))
                }
            } else state
        }
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) return

        try {
            val result = evaluate(expr)
            val resultStr = if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                String.format("%.10g", result)
            }
            val historyEntry = "$expr = $resultStr"
            _uiState.update { state ->
                val newHistory = (listOf(historyEntry) + state.history).take(20)
                state.copy(
                    expression = resultStr,
                    result = "",
                    history = newHistory
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(result = "Error") }
        }
    }

    // Recursive descent parser for mathematical expressions
    private fun evaluate(expression: String): Double {
        val tokens = tokenize(expression)
        val parser = Parser(tokens)
        val result = parser.parseExpression()
        if (parser.hasMore()) throw IllegalArgumentException("Unexpected tokens")
        return result
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val s = expr.replace("×", "*").replace("÷", "/").replace("−", "-")
            .replace("π", "${Math.PI}").replace("e", "${Math.E}")

        while (i < s.length) {
            when {
                s[i].isWhitespace() -> i++
                s[i].isDigit() || s[i] == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(s.substring(start, i))
                }
                s.substring(i).startsWith("sin") -> { tokens.add("sin"); i += 3 }
                s.substring(i).startsWith("cos") -> { tokens.add("cos"); i += 3 }
                s.substring(i).startsWith("tan") -> { tokens.add("tan"); i += 3 }
                s.substring(i).startsWith("log") -> { tokens.add("log"); i += 3 }
                s.substring(i).startsWith("ln") -> { tokens.add("ln"); i += 2 }
                s.substring(i).startsWith("sqrt") -> { tokens.add("sqrt"); i += 4 }
                s[i] in "+-*/^%()!" -> { tokens.add(s[i].toString()); i++ }
                else -> i++ // Skip unknown characters
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>) {
        private var pos = 0

        fun hasMore() = pos < tokens.size

        private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null

        private fun consume(): String = tokens[pos++]

        fun parseExpression(): Double {
            var result = parseTerm()
            while (peek() == "+" || peek() == "-") {
                val op = consume()
                val right = parseTerm()
                result = if (op == "+") result + right else result - right
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parsePower()
            while (peek() == "*" || peek() == "/" || peek() == "%") {
                val op = consume()
                val right = parsePower()
                result = when (op) {
                    "*" -> result * right
                    "/" -> result / right
                    "%" -> result % right
                    else -> result
                }
            }
            return result
        }

        private fun parsePower(): Double {
            var result = parseUnary()
            while (peek() == "^") {
                consume()
                val right = parseUnary()
                result = result.pow(right)
            }
            return result
        }

        private fun parseUnary(): Double {
            if (peek() == "-") {
                consume()
                return -parseUnary()
            }
            if (peek() == "+") {
                consume()
                return parseUnary()
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var result = parsePrimary()
            while (peek() == "!") {
                consume()
                result = factorial(result.toInt()).toDouble()
            }
            return result
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            // Functions
            if (token in listOf("sin", "cos", "tan", "log", "ln", "sqrt")) {
                consume()
                if (peek() == "(") consume()
                val arg = parseExpression()
                if (peek() == ")") consume()
                return when (token) {
                    "sin" -> sin(Math.toRadians(arg))
                    "cos" -> cos(Math.toRadians(arg))
                    "tan" -> tan(Math.toRadians(arg))
                    "log" -> log10(arg)
                    "ln" -> ln(arg)
                    "sqrt" -> sqrt(arg)
                    else -> throw IllegalArgumentException("Unknown function: $token")
                }
            }

            // Parenthesized expression
            if (token == "(") {
                consume()
                val result = parseExpression()
                if (peek() == ")") consume()
                return result
            }

            // Number
            return try {
                consume().toDouble()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected number, got: $token")
            }
        }

        private fun factorial(n: Int): Long {
            if (n < 0) throw IllegalArgumentException("Factorial of negative number")
            if (n <= 1) return 1
            var result = 1L
            for (i in 2..n) result *= i
            return result
        }
    }
}
