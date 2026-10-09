package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeInset
import com.example.project2.ui.theme.ChromeRow
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hairline
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Label
import com.example.project2.ui.theme.Paper
import com.example.project2.ui.theme.Small
import java.io.File

/**
 * 合成器：一页纸，从上到下——
 * 框（署名 · 拍号）→ 走带 → 播放头 → 速度与调（大字）→ 键盘（主角）→ 鼓机 → 和弦时间线。
 * 段与段之间只有一条发丝线和一个括号标签，没有卡片；会动的只有墨。
 */
@Composable
fun SynthScreen(
    modifier: Modifier = Modifier,
    metronomeViewModel: MetronomeViewModel,
    filepath: File,
    musicViewModel: MusicViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { FluidSynthManager.initialize() }
    DisposableEffect(Unit) { onDispose { FluidSynthManager.shutdown() } }

    val info by musicViewModel.musicInfo.collectAsState()
    val clock = rememberClock(metronomeViewModel, info)

    Column(modifier = modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState())) {
        ChromeRow(
            left = { Text("harmonics", style = ChromeStyle, color = Ink) },
            center = { Label("synth") },
            right = { BeatTicks(clock) },
        )

        Column(Modifier.padding(horizontal = ChromeInset)) {
            Transport(filepath = filepath)
            Spacer(Modifier.height(10.dp))
            Playhead(clock)

            Spacer(Modifier.height(34.dp))
            BasicMusicInfoSet(viewModel = musicViewModel)

            Section("keyboard")
            Keyboards(modifier = Modifier.fillMaxWidth(), viewModel = musicViewModel)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { KeyboardSteppers() }

            Section("drums")
            DrumSet(clock = clock)

            Section("chords")
            ChordTimeline(clock = clock)

            Spacer(Modifier.height(ChromeInset))
            Hairline()
            Spacer(Modifier.height(14.dp))
            Small("02 / 04")
            Spacer(Modifier.height(ChromeInset))
        }
    }
}

/** 段落标题：一条发丝线，下面左边一个括号标签，右边可以放这一段的小控件 */
@Composable
fun Section(name: String, trailing: (@Composable () -> Unit)? = null) {
    Spacer(Modifier.height(30.dp))
    Hairline()
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Label(name)
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
    Spacer(Modifier.height(16.dp))
}
