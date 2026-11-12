package ie.griffith.marioplan.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ie.griffith.marioplan.model.Quest

@Composable
 fun SectionLabel(label: String, quests: List<Quest>) {
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
 fun QuestRow(quest: Quest, onToggle: () -> Unit) {
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