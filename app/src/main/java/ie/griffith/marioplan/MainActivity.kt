package ie.griffith.marioplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ie.griffith.marioplan.ui.screens.AdventureApp
import ie.griffith.marioplan.ui.theme.AdventureTheme


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

