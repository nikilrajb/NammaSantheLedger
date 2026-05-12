package com.nammasanthe.ledger.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("kn", "IN"))
    private val numberFormat   = NumberFormat.getNumberInstance(Locale("kn", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /** Format a Double as Indian Rupees: ₹1,234.50 */
    fun formatCurrency(amount: Double): String =
        currencyFormat.format(amount)

    /** Format as plain number: 1,234.50 (without ₹ symbol) */
    fun formatAmount(amount: Double): String =
        numberFormat.format(amount)

    /** Validate phone number — must be exactly 10 digits */
    fun isValidPhone(phone: String): Boolean =
        phone.trim().matches(Regex("^[0-9]{10}$"))

    /** Validate amount — must be a positive number */
    fun isValidAmount(input: String): Boolean {
        val d = input.toDoubleOrNull() ?: return false
        return d > 0.0
    }
}