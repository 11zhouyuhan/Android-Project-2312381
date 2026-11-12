package ie.griffith.marioplan.model

import java.util.Calendar
import java.util.Locale

// A calendar day. month is 0-based, the same as java.util.Calendar.
data class DayKey(val year: Int, val month: Int, val day: Int) {
    private fun toCalendar(): Calendar =
        Calendar.getInstance().apply { clear(); set(year, month, day) }
    fun monthName(): String =
        toCalendar().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH) ?: ""
    fun weekdayName(): String =
        toCalendar().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH) ?: ""
}
fun currentDay(): DayKey {
    val now = Calendar.getInstance()
    return DayKey(
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    )
}