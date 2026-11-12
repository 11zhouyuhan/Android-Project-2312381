package ie.griffith.marioplan.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.R
import ie.griffith.marioplan.model.DayKey
import ie.griffith.marioplan.ui.components.Header
import ie.griffith.marioplan.ui.components.MainButton
import ie.griffith.marioplan.ui.components.PaperCard
import ie.griffith.marioplan.ui.components.ProgressTrack
import ie.griffith.marioplan.ui.components.RewardCard
import ie.griffith.marioplan.ui.components.StatCard
import java.util.Calendar

//Progress
@Composable
 fun ProgressScreen(
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