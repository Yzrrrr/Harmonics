package com.example.project2.SynthPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.LabelButton
import com.example.project2.ui.theme.PickerSheet
import com.example.project2.ui.theme.ValueField

val ROOTS = listOf("C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B")
val SCALES = listOf(
    "major", "minor", "blues", "dorian", "major_pentatonic", "minor_pentatonic",
    "harmonic_minor", "melodic_minor", "phrygian", "lydian", "mixolydian", "locrian",
)

/** 音阶全名：下划线换成空格，选项单里用 */
fun scaleLabel(scale: String) = scale.replace('_', ' ')

/** 音阶简写：大值那一格放不下全名，用乐手的缩写 */
fun scaleShort(scale: String) = when (scale) {
    "major" -> "maj"
    "minor" -> "min"
    "major_pentatonic" -> "maj pent"
    "minor_pentatonic" -> "min pent"
    "harmonic_minor" -> "harm min"
    "melodic_minor" -> "mel min"
    "phrygian" -> "phryg"
    "mixolydian" -> "mixo"
    "locrian" -> "locr"
    else -> scale
}

/**
 * 四个值一行：( bpm ) 120 / ( clap ) 4 / ( root ) C / ( scale ) major。
 * 点值弹选项单。下面一行是小节数：( 1 bar ) ( 2 bar ) ( 4 bar ) ( 8 bar )。
 * 这就是整个乐器暴露出来的全部参数：节奏、和声、结构。
 */
@Composable
fun BasicMusicInfoSet(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val info by viewModel.musicInfo.collectAsState()
    var picking by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(info) { FluidSynthManager.setBasicMusicInfo(info.BPM, info.bar, info.clap) }

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            ValueField("bpm", info.BPM.toString(), Modifier.weight(1.1f)) { picking = "bpm" }
            ValueField("clap", info.clap.toString(), Modifier.weight(0.9f)) { picking = "clap" }
            ValueField("root", info.root, Modifier.weight(0.9f)) { picking = "root" }
            ValueField("scale", scaleShort(info.scale), Modifier.weight(1.6f)) { picking = "scale" }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            listOf(1, 2, 4, 8).forEach { bars ->
                LabelButton(text = "$bars bar", active = info.bar == bars, onClick = { viewModel.updateBar(bars) })
            }
        }
    }

    when (picking) {
        "bpm" -> PickerSheet("bpm", (40..240).toList(), info.BPM, { viewModel.updateBPM(it); picking = null }, { picking = null })
        "clap" -> PickerSheet("clap", listOf(3, 4), info.clap, { viewModel.updateClap(it); picking = null }, { picking = null })
        "root" -> PickerSheet("root", ROOTS, info.root, { viewModel.updateRoot(it); picking = null }, { picking = null })
        "scale" -> PickerSheet("scale", SCALES, info.scale, { viewModel.updateScale(it); picking = null }, { picking = null }, ::scaleLabel)
    }
}
