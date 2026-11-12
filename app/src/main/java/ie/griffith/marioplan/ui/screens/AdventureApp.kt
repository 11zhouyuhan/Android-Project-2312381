package ie.griffith.marioplan.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ie.griffith.marioplan.LevelMapActivity
import ie.griffith.marioplan.R
import ie.griffith.marioplan.model.Quest
import ie.griffith.marioplan.model.Screen
import ie.griffith.marioplan.model.currentDay
import ie.griffith.marioplan.ui.components.AddQuestDialog
import kotlinx.coroutines.delay

@Composable
 fun AdventureApp() {
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
            Quest(
                2,
                "Review lecture notes",
                "09:30",
                "Cloud bat",
                false,
                true,
                R.drawable.monster_bat
            ),
            Quest(
                3,
                "Drink 2L of water",
                "11:00",
                "Coral crab",
                false,
                true,
                R.drawable.monster_crab
            ),
            Quest(
                4,
                "Finish app wireframes",
                "14:00",
                "Mist ghost",
                true,
                false,
                R.drawable.monster_ghost
            ),
            Quest(
                5,
                "Walk for 20 minutes",
                "16:30",
                "Amber blob",
                true,
                false,
                R.drawable.monster_blob
            )
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