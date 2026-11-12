package ie.griffith.marioplan.model

import androidx.annotation.DrawableRes

data class Quest(
    val id: Int,
    val title: String,
    val time: String,
    val monster: String,
    val afternoon: Boolean,
    val done: Boolean,
    @DrawableRes val image: Int
)