package com.example.project2

import android.content.Context
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project2.ChatScreen.ChatScreen
import com.example.project2.ChatScreen.ChatViewModel
import com.example.project2.FrontPage.FrontScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.project2.MusicGenPage.MusicGenerationScreen
import com.example.project2.SynthPage.MetronomeViewModel
import com.example.project2.SynthPage.SynthScreen
import kotlinx.serialization.Serializable
import java.io.File


@Composable
fun NavgationGraph(modifier: Modifier = Modifier,
                   navController: NavHostController = rememberNavController(),
                   startDestination : FrontScreenPage = FrontScreenPage,
                   chatViewModel : ChatViewModel,
                   metronomeViewModel: MetronomeViewModel,
                   filepath: File,
                   context: Context
)
{
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    NavHost(
        navController = navController,
        startDestination = FrontScreenPage,
        modifier = modifier
    ){
        composable<FrontScreenPage>{
            FrontScreen(
                onClickJumpToAssistant = {navController.navigateSingleTopTo(ChatScreenPage)},
                onClickJumpToSynth = {navController.navigateSingleTopTo(SynthScreenPage)},
                onClickJumpToMusicGen = {navController.navigateSingleTopTo(MusicGenScreenPage)}
            )
        }
        composable<ChatScreenPage> {
            ChatScreen(viewModel =  chatViewModel)
        }
        composable<SynthScreenPage> {
            SynthScreen(
                modifier = Modifier,
                metronomeViewModel = metronomeViewModel,
                filepath = filepath
            )
        }
        composable<MusicGenScreenPage> {
            MusicGenerationScreen(context)
        }
    }
}


fun NavHostController.navigateSingleTopTo(route: Any) =
    this.navigate(route) {
        launchSingleTop = true
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id){ saveState = true }
        restoreState = true
    }