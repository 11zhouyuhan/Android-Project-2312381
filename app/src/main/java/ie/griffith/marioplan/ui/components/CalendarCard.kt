package ie.griffith.marioplan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.R
import ie.griffith.marioplan.model.DayKey
import java.util.Calendar

@Composable
 fun CalendarCard(today: DayKey, selected: DayKey, onDay: (DayKey) -> Unit) {
    // The month shown is always the real current month of the device.
    val first = Calendar.getInstance().apply { clear(); set(today.year, today.month, 1) }
    val offset = first.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeks = (offset + daysInMonth + 6) / 7
    PaperCard {
        Text(
            "${today.monthName()} ${today.year}",
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Row {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    it,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        repeat(weeks) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { weekday ->
                    val date = week * 7 + weekday - offset + 1
                    val valid = date in 1..daysInMonth
                    val isSelected = valid &&
                            selected.year == today.year &&
                            selected.month == today.month &&
                            selected.day == date
                    val isToday = valid && date == today.day
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .height(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    valid -> colorResource(R.color.calendar_day)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isToday && !isSelected) {
                                    Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(5.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(enabled = valid) {
                                onDay(DayKey(today.year, today.month, date))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (valid) "$date" else "",
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}