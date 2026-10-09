package com.example.project2.SynthPage

import android.view.MotionEvent
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

/**
 * 键盘：一排竖条，暖纸底、发丝线分格，按下的那格转成墨。
 * 横向是音阶里的音，纵向是力度（越往下越重）。上面两组小字是音数和八度，点 − / + 调。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Keyboards(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val info by viewModel.musicInfo.collectAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val channel = 1
    var key by remember { mutableIntStateOf(0) }
    var vel by remember { mutableIntStateOf(0) }
    var pressed by remember { mutableIntStateOf(-1) }
    val baseVel = 40
    var keyCount by remember { mutableIntStateOf(12) }
    var octave by remember { mutableIntStateOf(4) }
    val rootKey = getMidiFromRootNote(info.root, octave = octave)
    val midiNotes = getScaleNotes(rootMidi = rootKey, mode = info.scale, noteCount = keyCount)

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Stepper("notes", keyCount, 1, 24) { keyCount = it }
            Stepper("octave", octave, 1, 8) { octave = it }
            Spacer(Modifier.weight(1f))
            Small("${info.root} ${scaleShort(info.scale)}", Modifier.align(Alignment.CenterVertically))
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
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
                            FluidSynthManager.playNote(key, vel, channel)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val nextKey = midiNotes[index]
                            val nextVel = (baseVel + ny * 87).toInt()
                            if (nextKey != key || kotlin.math.abs(nextVel - vel) > 5) {
                                FluidSynthManager.stopNoteDelay(key, channel)
                                key = nextKey
                                vel = nextVel
                                pressed = index
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
            KeyboardsItem(count = keyCount, pressed = pressed)
        }
    }
}

@Composable
fun KeyboardsItem(modifier: Modifier = Modifier, count: Int, pressed: Int) {
    Row(modifier.fillMaxWidth().fillMaxHeight().background(Hair)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = if (index == 0) 1.dp else 0.dp, end = 1.dp, top = 1.dp, bottom = 1.dp)
                    .background(if (index == pressed) Ink else PaperWarm),
            )
        }
    }
}

/** ( keys ) 12 −  + */
@Composable
fun Stepper(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Small("( $label )")
        Text(value.toString(), style = ChromeStyle, color = Ink)
        StepKey("−") { if (value > min) onChange(value - 1) }
        StepKey("+") { if (value < max) onChange(value + 1) }
    }
}

@Composable
private fun StepKey(glyph: String, onClick: () -> Unit) {
    Box(
        Modifier
            .width(28.dp)
            .height(28.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(glyph, style = ChromeStyle, color = Ink50) }
}
