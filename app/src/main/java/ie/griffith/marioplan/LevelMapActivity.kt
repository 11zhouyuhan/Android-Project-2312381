package ie.griffith.marioplan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.griffith.marioplan.ui.theme.AdventureTheme

//opened with an explicit intent from the Calendar Home screen.
class LevelMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ids = intent.getIntArrayExtra("ids") ?: intArrayOf()
        val titles = intent.getStringArrayExtra("titles") ?: emptyArray()
        val times = intent.getStringArrayExtra("times") ?: emptyArray()
        val monsters = intent.getStringArrayExtra("monsters") ?: emptyArray()
        val afternoon = intent.getBooleanArrayExtra("afternoon") ?: booleanArrayOf()
        val done = intent.getBooleanArrayExtra("done") ?: booleanArrayOf()
        val images = intent.getIntArrayExtra("images") ?: intArrayOf()
        val selected = DayKey(
            intent.getIntExtra("year", 2026),
            intent.getIntExtra("month", 0),
            intent.getIntExtra("day", 1)
        )
        val startQuests = ids.indices.map { i ->
            Quest(ids[i], titles[i], times[i], monsters[i], afternoon[i], done[i], images[i])
        }
        setContent {
            AdventureTheme {
                val quests = remember { mutableStateListOf<Quest>().apply { addAll(startQuests) } }
                val completed = quests.count { it.done }
                val progress = if (quests.isEmpty()) 0f else completed.toFloat() / quests.size
                BackHandler { sendResultAndFinish(quests, false) }
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
                        MapScreen(
                            quests = quests,
                            selected = selected,
                            progress = progress,
                            completed = completed,
                            onToggle = { id ->
                                val index = quests.indexOfFirst { it.id == id }
                                if (index >= 0) {
                                    quests[index] = quests[index].copy(done = !quests[index].done)
                                }
                            },
                            onProgress = { sendResultAndFinish(quests, true) },
                            onBack = { sendResultAndFinish(quests, false) }
                        )
                    }
                }
            }
        }
    }
    // Sends the finished tasks back to MainActivity, then closes this screen.
    private fun sendResultAndFinish(quests: List<Quest>, goProgress: Boolean) {
        val data = Intent().apply {
            putExtra("doneIds", quests.filter { it.done }.map { it.id }.toIntArray())
            putExtra("goProgress", goProgress)
        }
        setResult(RESULT_OK, data)
        finish()
    }
}