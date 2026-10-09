package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
 * 框（署名 · 小节计数）→ 走带 → 四个值（bpm / clap / root / scale）与小节数 →
 * 键盘 → 鼓组 → 和弦序列。
 * 段与段之间只有一条发丝线和一个括号标签，没有卡片。
 */
@Composable
fun SynthScreen(modifier: Modifier = Modifier, metronomeViewModel: MetronomeViewModel, filepath: File) {
    LaunchedEffect(Unit) { FluidSynthManager.initialize() }
    DisposableEffect(Unit) { onDispose { FluidSynthManager.shutdown() } }

    Column(
        modifier = modifier.fillMaxSize().background(Paper).verticalScroll(rememberScrollState()),
    ) {
        ChromeRow(
            left = { Text("harmonics", style = ChromeStyle, color = Ink) },
            center = { Label("synth") },
            right = { BeatCounter(viewModel = metronomeViewModel) },
        )

        Column(Modifier.padding(horizontal = ChromeInset)) {
            Transport(filepath = filepath, viewModel = metronomeViewModel)
            Spacer(Modifier.height(10.dp))
            BeatLine(viewModel = metronomeViewModel)

            Section("key")
            BasicMusicInfoSet()

            Section("keys")
            Keyboards(modifier = Modifier.fillMaxWidth())

            Section("drums")
            DrumSet()

            Section("chords")
            VerticalReorderList()

            Spacer(Modifier.height(ChromeInset))
            Hairline()
            Spacer(Modifier.height(14.dp))
            Small("02 / 04")
            Spacer(Modifier.height(ChromeInset))
        }
    }
}

/** 段落标题：一条发丝线，下面一个括号标签 */
@Composable
fun Section(name: String) {
    Spacer(Modifier.height(30.dp))
    Hairline()
    Spacer(Modifier.height(14.dp))
    Label(name)
    Spacer(Modifier.height(16.dp))
}
