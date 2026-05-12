package com.nammasanthe.ledger.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat    = SimpleDateFormat("dd MMM yyyy", Locale("kn", "IN"))
    private val timeFormat    = SimpleDateFormat("hh:mm a",    Locale.getDefault())
    private val dateTimeFormat= SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    /** Format a Unix timestamp to '12 Jan 2025' */
    fun formatDate(epochMs: Long): String = dateFormat.format(Date(epochMs))

    /** Format a Unix timestamp to '03:45 PM' */
    fun formatTime(epochMs: Long): String = timeFormat.format(Date(epochMs))

    /** Format to '12 Jan, 03:45 PM' — for transaction list items */
    fun formatDateTime(epochMs: Long): String = dateTimeFormat.format(Date(epochMs))

    /** Start of today in milliseconds (midnight) */
    fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);       cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** End of today in milliseconds (23:59:59.999) */
    fun endOfToday(): Long = startOfToday() + 86_400_000L - 1L

    /** Check if a timestamp falls within today */
    fun isToday(epochMs: Long): Boolean =
        epochMs >= startOfToday() && epochMs <= endOfToday()

    /** Format 'Today', 'Yesterday', or a date string */
    fun formatRelativeDate(epochMs: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { timeInMillis = epochMs }
        return when {
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> "Today"
            now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1 &&
                    now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> "Yesterday"
            else -> formatDate(epochMs)
        }
    }
}