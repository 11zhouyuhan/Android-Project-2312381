package ie.griffith.marioplan

import android.R.id.toggle
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.ui.theme.AdventureTheme
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

data class Quest(
    val id: Int,
    val title: String,
    val time: String,
    val monster: String,
    val afternoon: Boolean,
    val done: Boolean,
    @DrawableRes val image: Int
)
enum class Screen { Home, Map, Progress }
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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdventureTheme {
                AdventureApp()
            }
        }

    }
}

@Composable
private fun AdventureApp() {
    var screen by remember { mutableStateOf(Screen.Home) }
    var today by remember { mutableStateOf(currentDay()) }
    var selected by remember { mutableStateOf(today) }
    var showAdd by remember { mutableStateOf(false) }
    // Keep "today" in step with the phone / emulator clock.
    // If the date changes (e.g. after midnight) the calendar moves on by itself.
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            val now = currentDay()
            if (now != today) {
                if (selected == today) selected = now
                today = now
            }
        }
    }
    val quests = remember {
        mutableStateListOf(
            Quest(1, "Read 10 pages", "08:00", "Moss slime", false, true, R.drawable.monster_slime),
            Quest(2, "Review lecture notes", "09:30", "Cloud bat", false, true, R.drawable.monster_bat),
            Quest(3, "Drink 2L of water", "11:00", "Coral crab", false, true, R.drawable.monster_crab),
            Quest(4, "Finish app wireframes", "14:00", "Mist ghost", true, false, R.drawable.monster_ghost),
            Quest(5, "Walk for 20 minutes", "16:30", "Amber blob", true, false, R.drawable.monster_blob)
        )
    }
    val completed = quests.count { it.done }
    val progress = if (quests.isEmpty()) 0f else completed.toFloat() / quests.size
    val toggle: (Int) -> Unit = { id ->
        val index = quests.indexOfFirst { it.id == id }
        if (index >= 0) {
            quests[index] = quests[index].copy(done = !quests[index].done)
        }
    }
    val context = LocalContext.current
    // Receives the result sent back by LevelMapActivity.
    val mapLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val doneIds = data.getIntArrayExtra("doneIds")?.toSet() ?: emptySet()
            for (i in quests.indices) {
                quests[i] = quests[i].copy(done = quests[i].id in doneIds)
            }
            if (data.getBooleanExtra("goProgress", false)) {
                screen = Screen.Progress
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp)
        ) {
            when (screen) {
                Screen.Home -> HomeScreen(
                    quests = quests,
                    today = today,
                    selected = selected,
                    onDay = { selected = it },
                    progress = progress,
                    completed = completed,
                    onToggle = toggle,
                    onAdd = { showAdd = true },
                    onMap = { val intent = Intent(context, LevelMapActivity::class.java).apply {
                        putExtra("ids", quests.map { it.id }.toIntArray())
                        putExtra("titles", quests.map { it.title }.toTypedArray())
                        putExtra("times", quests.map { it.time }.toTypedArray())
                        putExtra("monsters", quests.map { it.monster }.toTypedArray())
                        putExtra("afternoon", quests.map { it.afternoon }.toBooleanArray())
                        putExtra("done", quests.map { it.done }.toBooleanArray())
                        putExtra("images", quests.map { it.image }.toIntArray())
                        putExtra("year", selected.year)
                        putExtra("month", selected.month)
                        putExtra("day", selected.day )
                    }
                        mapLauncher.launch(intent) }
                )
                Screen.Map -> MapScreen(
                    quests = quests,
                    selected = selected,
                    progress = progress,
                    completed = completed,
                    onToggle = toggle,
                    onProgress = { screen = Screen.Progress },
                    onBack = { screen = Screen.Home }
                )
                Screen.Progress -> ProgressScreen(
                    total = quests.size,
                    completed = completed,
                    progress = progress,
                    selected = selected,
                    onHome = { screen = Screen.Home }
                )
            }
        }
    }
    if (showAdd) {
        AddQuestDialog(
            onDismiss = { showAdd = false },
            onAdd = { title, afternoon ->
                quests.add(
                    Quest(
                        id = (quests.maxOfOrNull { it.id } ?: 0) + 1,
                        title = title,
                        time = if (afternoon) "15:00" else "10:00",
                        monster = "Forest sprite",
                        afternoon = afternoon,
                        done = false,
                        image = R.drawable.monster_sprite
                    )
                )
                showAdd = false
            }
        )
    }
}
//Reusable pieces
@Composable
private fun Header(kicker: String, title: String, @DrawableRes icon: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                kicker.uppercase(),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )
            Text(
                title,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
@Composable
private fun PaperCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    padding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .padding(padding),
        content = content
    )
}
@Composable
private fun MainButton(
    text: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.primary,
            contentColor = if (secondary) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
@Composable
private fun ProgressSummary(completed: Int, total: Int, progress: Float) {
    PaperCard(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.star),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row {
                    Text(
                        "$completed/$total tasks completed",
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                ProgressTrack(progress)
            }
        }
    }
}
@Composable
private fun ProgressTrack(progress: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.track))
    ) {
        if (progress > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
//Home
@Composable
private fun HomeScreen(
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
@Composable
private fun CalendarCard(today: DayKey, selected: DayKey, onDay: (DayKey) -> Unit) {
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
@Composable
private fun SectionLabel(label: String, quests: List<Quest>) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            label,
            Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "${quests.count { it.done }} of ${quests.size} complete",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
@Composable
private fun QuestRow(quest: Quest, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onToggle)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = quest.done, onCheckedChange = { onToggle() })
        Column(
            Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Text(quest.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${quest.time} · ${quest.monster}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Image(
            painter = painterResource(quest.image),
            contentDescription = quest.monster,
            modifier = Modifier.size(30.dp)
        )
    }
}
//Level map
@Composable
 fun MapScreen(
    quests: List<Quest>,
    selected: DayKey,
    progress: Float,
    completed: Int,
    onToggle: (Int) -> Unit,
    onProgress: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Header(
            "${selected.weekdayName()}, ${selected.monthName()} ${selected.day}",
            "LEVEL MAP",
            R.drawable.flag_green
        )
        ProgressSummary(completed, quests.size, progress)
        Row(Modifier.fillMaxWidth()) {
            Text(
                "Meadow trail",
                Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "LEVEL 01",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        // The map takes all the space that is left on the screen.
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colorResource(R.color.sky))
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 12.dp)
            ) {
                quests.reversed().forEachIndexed { index, quest ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (index % 2 == 0) 16.dp else 80.dp,
                                end = if (index % 2 == 0) 80.dp else 16.dp
                            )
                            .clickable { onToggle(quest.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(quest.image),
                                contentDescription = quest.monster,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    quest.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    if (quest.done) "✓ DEFEATED" else "PENDING",
                                    modifier = Modifier
                                        .background(
                                            if (quest.done) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(3.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                    color = if (quest.done) MaterialTheme.colorScheme.onPrimary
                                    else colorResource(R.color.brown),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(colorResource(R.color.grass))
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(colorResource(R.color.brown))
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            // The player and the ground are fixed at the bottom of the map.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.hero_blue),
                    contentDescription = "You",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    " YOU",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(colorResource(R.color.grass))
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(colorResource(R.color.brown))
            )
        }
        PaperCard {
            Text(
                "Shake your phone to jump",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Tap a monster to update its task.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainButton(
                "Calendar", R.drawable.ic_back,
                modifier = Modifier.weight(1f),
                secondary = true,
                onClick = onBack
            )
            MainButton(
                "Progress", R.drawable.ic_chart,
                modifier = Modifier.weight(1f),
                onClick = onProgress
            )
        }
    }
}
//Progress
@Composable
private fun ProgressScreen(
    total: Int,
    completed: Int,
    progress: Float,
    selected: DayKey,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val percent = (progress * 100).toInt()
    // Day-of-month of the 7 days that end on the selected day (real dates).
    val weekDays = (0..6).map { index ->
        Calendar.getInstance().apply {
            clear()
            set(selected.year, selected.month, selected.day)
            add(Calendar.DAY_OF_MONTH, index - 6)
        }.get(Calendar.DAY_OF_MONTH)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Header(
            "${selected.weekdayName()}, ${selected.monthName()} ${selected.day}",
            "MY PROGRESS",
            R.drawable.star
        )
        PaperCard(color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                "TODAY'S ADVENTURE",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$percent%",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    " complete",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.hero_blue),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp)
                )
            }
            ProgressTrack(progress)
            Spacer(Modifier.height(8.dp))
            Text(
                "$completed monsters down. Keep your adventure going!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                "$completed",
                "Completed tasks",
                "monsters defeated",
                Modifier.weight(1f)
            )
            StatCard(
                "$total",
                "Total tasks",
                "${total - completed} still on the trail",
                Modifier.weight(1f)
            )
        }
        PaperCard {
            Text(
                "Your week in quests",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Illustrative history · latest bar is live",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(8.dp))
            val values = listOf(40, 60, 80, 40, 100, 80, percent)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEachIndexed { index, value ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            "$value%",
                            fontSize = 9.sp,
                            color = if (index == 6) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((value * 0.85f).coerceAtLeast(2f).dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 3.dp,
                                        topEnd = 3.dp
                                    )
                                )
                                .background(
                                    if (index == 6) MaterialTheme.colorScheme.primary
                                    else colorResource(R.color.bar_past)
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${weekDays[index]}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Text(
            "Little wins, big adventure",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RewardCard("First victory", R.drawable.star, completed >= 1, Modifier.weight(1f))
            RewardCard("Triple clear", R.drawable.gem, completed >= 3, Modifier.weight(1f))
            RewardCard(
                "Perfect day",
                R.drawable.flag_blue,
                total > 0 && completed == total,
                Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MainButton(
                "Home", R.drawable.ic_home,
                modifier = Modifier.weight(1f),
                secondary = true,
                onClick = onHome
            )
            // Implicit intent: Android opens the share sheet
            MainButton(
                "Share Progress", R.drawable.ic_share,
                modifier = Modifier.weight(1f)
            ) {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "My Adventure: $completed/$total tasks completed " +
                                "today — $percent%! ★"
                    )
                }
                context.startActivity(Intent.createChooser(share, "Share Progress"))
            }
        }
    }
}
@Composable
private fun StatCard(
    value: String,
    title: String,
    subtitle: String,
    modifier: Modifier
) {
    PaperCard(modifier) {
        Text(
            value.padStart(2, '0'),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 26.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
private fun RewardCard(
    title: String,
    @DrawableRes icon: Int,
    earned: Boolean,
    modifier: Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (earned) MaterialTheme.colorScheme.surface
                else colorResource(R.color.calendar_day)
            )
            .padding(vertical = 12.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = title,
            alpha = if (earned) 1f else 0.3f,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            if (earned) "UNLOCKED" else "LOCKED",
            color = if (earned) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 8.sp
        )
    }
}
// Add task dialog
@Composable
private fun AddQuestDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var afternoon by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A new adventure") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task description") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Afternoon task", Modifier.weight(1f))
                    Switch(
                        checked = afternoon,
                        onCheckedChange = { afternoon = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onAdd(title.trim(), afternoon) }
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}