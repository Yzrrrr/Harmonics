package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.DisplayStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink20
import com.example.project2.ui.theme.Label
import com.example.project2.ui.theme.LabelButton
import com.example.project2.ui.theme.PickerSheet
import com.example.project2.ui.theme.Small

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

/** 根音写法：#C → C♯ */
fun rootLabel(root: String) = if (root.startsWith("#")) root.drop(1) + "♯" else root

/** 音阶里有哪些半音（相对根音，0–11） */
fun scaleSemitones(scale: String): Set<Int> = runCatching {
    getScaleNotes(60, scale, 8).map { (it - 60) % 12 }.toSet()
}.getOrDefault(emptySet())

/**
 * 速度与调：两个大字——120 和 C maj——这是整个乐器暴露出来的全部参数。
 * 速度上下拖着拧，点开是选项单；调下面十二个小格标出音阶里的音；右边是拍号与小节数。
 */
@Composable
fun BasicMusicInfoSet(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val info by viewModel.musicInfo.collectAsState()
    var picking by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current

    LaunchedEffect(info.BPM, info.bar, info.clap) { FluidSynthManager.setBasicMusicInfo(info.BPM, info.bar, info.clap) }

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // 速度：拖
            Column(
                Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        var carry = 0f
                        val stepPx = with(density) { 10.dp.toPx() }
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            carry += dragAmount
                            val steps = (carry / stepPx).toInt()
                            if (steps != 0) {
                                carry -= steps * stepPx
                                viewModel.updateBPM((viewModel.musicInfo.value.BPM - steps).coerceIn(40, 240))
                            }
                        }
                    }
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { picking = "bpm" },
            ) {
                Text(info.BPM.toString(), style = DisplayStyle, color = Ink)
                Spacer(Modifier.height(12.dp))
                Label("bpm · drag")
            }
            // 调：根音 + 音阶
            Column(Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        rootLabel(info.root), style = DisplayStyle, color = Ink,
                        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { picking = "root" },
                    )
                    Spacer(Modifier.padding(6.dp))
                    Text(
                        scaleShort(info.scale), style = DisplayStyle.copy(fontSize = DisplayStyle.fontSize * 0.5f, lineHeight = DisplayStyle.lineHeight * 0.55f), color = Ink,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { picking = "scale" },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScaleGlyph(scaleSemitones(info.scale))
                    Spacer(Modifier.padding(6.dp))
                    Label("key")
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Small("${info.clap} / 4", Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { picking = "clap" })
            listOf(1, 2, 4, 8).forEach { bars ->
                LabelButton(text = "$bars bar", active = info.bar == bars, onClick = { viewModel.updateBar(bars) })
            }
        }
    }

    when (picking) {
        "bpm" -> PickerSheet("bpm", (40..240).toList(), info.BPM, { viewModel.updateBPM(it); picking = null }, { picking = null })
        "clap" -> PickerSheet("beats per bar", listOf(3, 4), info.clap, { viewModel.updateClap(it); picking = null }, { picking = null })
        "root" -> PickerSheet("root", ROOTS, info.root, { viewModel.updateRoot(it); picking = null }, { picking = null }, ::rootLabel)
        "scale" -> PickerSheet("scale", SCALES, info.scale, { viewModel.updateScale(it); picking = null }, { picking = null }, ::scaleLabel)
    }
}

/** 十二个半音格：音阶里的是墨，其余是发丝线 */
@Composable
fun ScaleGlyph(semitones: Set<Int>) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(12) { i ->
            Box(Modifier.size(width = 7.dp, height = 10.dp).background(if (i in semitones) Ink else Ink20.copy(alpha = 0.5f)))
        }
    }
}
