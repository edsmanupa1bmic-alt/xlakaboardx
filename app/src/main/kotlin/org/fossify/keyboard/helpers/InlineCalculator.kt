package org.fossify.keyboard.helpers

object InlineCalculator {
    // Automatically detects when typed expression ends with "=" (e.g., "250*4="), 
    // calculates result in real-time, and surfaces it in the suggestion strip.
    
    fun calculate(expression: String): String? {
        if (!expression.endsWith("=")) return null
        
        val cleanExpr = expression.dropLast(1).trim()
        
        // Simple regex-based evaluation for basic math
        val regex = Regex("(\\d+)\\s*([+\\-*/])\\s*(\\d+)")
        val match = regex.find(cleanExpr)
        
        if (match != null) {
            val (num1Str, op, num2Str) = match.destructured
            val num1 = num1Str.toDoubleOrNull() ?: return null
            val num2 = num2Str.toDoubleOrNull() ?: return null
            
            return when (op) {
                "+" -> (num1 + num2).toString()
                "-" -> (num1 - num2).toString()
                "*" -> (num1 * num2).toString()
                "/" -> if (num2 != 0.0) (num1 / num2).toString() else "Error"
                else -> null
            }
        }
        return null
    }
}
