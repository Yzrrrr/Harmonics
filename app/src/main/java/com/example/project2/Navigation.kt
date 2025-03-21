package com.example.project2

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface NavigationRoute

@Serializable
object FrontScreenPage:NavigationRoute{
    const val PageName = "frontscreen"
}

@Serializable
object ChatScreenPage{
    const val PageName = "chatscreen"
}

@Serializable
object SynthScreenPage{
    const val PageName = "synthscreen"
}

@Serializable
object MusicGenScreenPage{
    const val PageName = "MusicGenScreen"
}


object Destinations{
    val FrontScreenPageRoute = Json.encodeToString(FrontScreenPage)
    val ChatScreenPageRoute = Json.encodeToString(ChatScreenPage)
    val SynthScreenPageRoute = Json.encodeToString(SynthScreenPage)
    val MusicGenScreenPageRoute = Json.encodeToString(MusicGenScreenPage)
}
