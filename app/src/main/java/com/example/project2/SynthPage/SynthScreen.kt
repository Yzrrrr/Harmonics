package com.example.project2.SynthPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SynthScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath: File) {

    LaunchedEffect(Unit) {
        FluidSynthManager.initialize() //初始化FluidSynth
    }
    DisposableEffect(Unit) {
        onDispose {
            FluidSynthManager.shutdown() //注销FluidSynth
            onDispose { }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column (
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            LinearDeterminateIndicator(viewModel = metronomeViewModel)
            Spacer(Modifier.padding(2.dp))
            Buttons(filepath =  filepath, viewModel = metronomeViewModel)
            Spacer(Modifier.padding(2.dp))
            BasicMusicInfoSet()
            Spacer(Modifier.padding(2.dp))
            Keyboards( modifier = Modifier
                .heightIn(min = 10.dp, max = 300.dp),
            )
            Spacer(Modifier.padding(2.dp))
            DrumSet()
            Spacer(Modifier.padding(2.dp))
            VerticalReorderList()
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val fakeViewModel = object : MetronomeViewModel() {
        override val count: StateFlow<Double> = MutableStateFlow(4.0)
    }
    val file =  File("/storage/emulated/0/Music")
    Project2Theme {
        SynthScreen(metronomeViewModel = fakeViewModel, filepath = file)
    }
}