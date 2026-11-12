package ie.griffith.marioplan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.R
import ie.griffith.marioplan.model.DayKey
import ie.griffith.marioplan.model.Quest
import ie.griffith.marioplan.ui.components.Header
import ie.griffith.marioplan.ui.components.MainButton
import ie.griffith.marioplan.ui.components.PaperCard
import ie.griffith.marioplan.ui.components.ProgressSummary

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