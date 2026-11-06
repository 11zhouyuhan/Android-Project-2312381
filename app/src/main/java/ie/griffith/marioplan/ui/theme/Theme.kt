package ie.griffith.marioplan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import ie.griffith.marioplan.R

@Composable
fun AdventureTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = colorResource(R.color.purple),
        onPrimary = colorResource(R.color.white),
        secondary = colorResource(R.color.green),
        tertiary = colorResource(R.color.gold),
        background = colorResource(R.color.background),
        onBackground = colorResource(R.color.ink),
        surface = colorResource(R.color.paper),
        onSurface = colorResource(R.color.ink),
        surfaceVariant = colorResource(R.color.pale_green),
        onSurfaceVariant = colorResource(R.color.muted)
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
