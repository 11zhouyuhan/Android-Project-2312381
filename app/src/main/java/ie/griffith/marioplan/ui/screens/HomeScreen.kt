package ie.griffith.marioplan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.R
import ie.griffith.marioplan.model.DayKey
import ie.griffith.marioplan.model.Quest
import ie.griffith.marioplan.ui.components.CalendarCard
import ie.griffith.marioplan.ui.components.Header
import ie.griffith.marioplan.ui.components.MainButton
import ie.griffith.marioplan.ui.components.ProgressSummary
import ie.griffith.marioplan.ui.components.QuestRow
import ie.griffith.marioplan.ui.components.SectionLabel

//Home
@Composable
 fun HomeScreen(
    quests: List<Quest>,
    today: DayKey,
    selected: DayKey,
    onDay: (DayKey) -> Unit,
    progress: Float,
    completed: Int,
    onToggle: (Int) -> Unit,
    onAdd: () -> Unit,
    onMap: () -> Unit
) {
    val morning = quests.filter { !it.afternoon }
    val afternoon = quests.filter { it.afternoon }
    val dayLabel = if (selected == today) {
        "Today, ${selected.weekdayName()} ${selected.day}"
    } else {
        "${selected.weekdayName()} ${selected.day} ${selected.monthName()}"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Header("A little progress, every day", "MY ADVENTURE", R.drawable.hero_green)
        CalendarCard(today, selected, onDay)
        ProgressSummary(completed, quests.size, progress)
        Row(Modifier.fillMaxWidth()) {
            Text(
                dayLabel,
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${quests.size} quests",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionLabel("MORNING", morning)
            morning.forEach { quest -> QuestRow(quest) { onToggle(quest.id) } }
            SectionLabel("AFTERNOON", afternoon)
            afternoon.forEach { quest -> QuestRow(quest) { onToggle(quest.id) } }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainButton(
                "Add Task", R.drawable.ic_add,
                modifier = Modifier.weight(1f),
                secondary = true,
                onClick = onAdd
            )
            MainButton(
                "Level Map", R.drawable.ic_map,
                modifier = Modifier.weight(1f),
                onClick = onMap
            )
        }
    }
}