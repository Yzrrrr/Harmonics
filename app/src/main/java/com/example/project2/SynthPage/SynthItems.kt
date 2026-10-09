package com.example.project2.SynthPage

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink50
import com.example.project2.ui.theme.PaperWarm
import com.example.project2.ui.theme.Small

private val NOTE_NAMES = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")

/**
 * 键盘：这件乐器的主角。
 * 一排竖条，暖纸底、发丝线分格，每条上方写音名（只有音阶里的音）。
 * 按下去，墨从上往下灌到手指的位置——按得越低越响，墨就越多；松手墨在三百毫秒里褪掉。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Keyboards(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val info by viewModel.musicInfo.collectAsState()
    val keyCount by viewModel.keyboardNotes.collectAsState()
    val octave by viewModel.keyboardOctave.collectAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val channel = 1
    var key by remember { mutableIntStateOf(0) }
    var vel by remember { mutableIntStateOf(0) }
    var pressed by remember { mutableIntStateOf(-1) }
    var depth by remember { mutableFloatStateOf(0f) }
    val baseVel = 40
    val rootKey = getMidiFromRootNote(info.root, octave = octave)
    val midiNotes = remember(rootKey, info.scale, keyCount) { getScaleNotes(rootMidi = rootKey, mode = info.scale, noteCount = keyCount) }

    Column(modifier) {
        // 音名行
        Row(Modifier.fillMaxWidth()) {
            midiNotes.forEach { midi ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Small(NOTE_NAMES[((midi % 12) + 12) % 12], color = if (midi % 12 == rootKey % 12) Ink else Ink50)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .onGloballyPositioned { size = it.size }
                .pointerInteropFilter { event ->
                    val nx = if (size.width > 0) (event.x / size.width).coerceIn(0f, 0.999f) else 0f
                    val ny = if (size.height > 0) (event.y / size.height).coerceIn(0f, 1f) else 0f
                    val index = (nx * keyCount).toInt().coerceIn(0, midiNotes.size - 1)
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            key = midiNotes[index]
                            vel = (baseVel + ny * 87).toInt()
                            pressed = index
                            depth = ny
                            FluidSynthManager.playNote(key, vel, channel)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val nextKey = midiNotes[index]
                            val nextVel = (baseVel + ny * 87).toInt()
                            depth = ny
                            pressed = index
                            if (nextKey != key || kotlin.math.abs(nextVel - vel) > 5) {
                                FluidSynthManager.stopNoteDelay(key, channel)
                                key = nextKey
                                vel = nextVel
                                FluidSynthManager.playNote(key, vel, channel)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            FluidSynthManager.stopNote(key, channel)
                            pressed = -1
                            true
                        }
                        else -> false
                    }
                },
        ) {
            KeyboardsItem(count = keyCount, pressed = pressed, depth = depth)
        }
    }
}

@Composable
fun KeyboardsItem(modifier: Modifier = Modifier, count: Int, pressed: Int, depth: Float) {
    Row(modifier.fillMaxWidth().fillMaxHeight().background(Hair)) {
        repeat(count) { index ->
            KeyStrip(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(start = if (index == 0) 1.dp else 0.dp, end = 1.dp, top = 1.dp, bottom = 1.dp),
                fill = if (index == pressed) depth.coerceAtLeast(0.08f) else 0f,
                held = index == pressed,
            )
        }
    }
}

@Composable
private fun KeyStrip(modifier: Modifier, fill: Float, held: Boolean) {
    // 按住时墨跟着手指走（不插值），松开后用 350ms 褪掉
    val ink by animateFloatAsState(targetValue = fill, animationSpec = if (held) snap() else tween(350), label = "ink")
    Box(modifier.background(PaperWarm)) {
        if (ink > 0.001f) Box(Modifier.fillMaxWidth().fillMaxHeight(ink).background(Ink))
    }
}

/** 段标题右侧：( notes ) 12 − +   ( octave ) 4 − + */
@Composable
fun KeyboardSteppers(viewModel: MusicViewModel = viewModel()) {
    val notes by viewModel.keyboardNotes.collectAsState()
    val octave by viewModel.keyboardOctave.collectAsState()
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Stepper("notes", notes) { viewModel.updateKeyboardNotes(it) }
        Stepper("octave", octave) { viewModel.updateKeyboardOctave(it) }
    }
}

@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Small("( $label )")
        Text(value.toString(), style = ChromeStyle, color = Ink)
        StepKey("−") { onChange(value - 1) }
        StepKey("+") { onChange(value + 1) }
    }
}

@Composable
private fun StepKey(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .width(24.dp)
            .height(28.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(glyph, style = ChromeStyle, color = Ink50) }
}
