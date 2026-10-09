package com.example.project2.SynthPage

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink20
import com.example.project2.ui.theme.Ink50
import com.example.project2.ui.theme.PaperWarm
import com.example.project2.ui.theme.PickerSheet
import com.example.project2.ui.theme.Small

private val NOTE_NAMES = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")

/**
 * 键盘：这件乐器的主角。
 * 一排竖条，暖纸底、发丝线分格，每条上方写音名（只有音阶里的音），根音实墨。
 * 按下去，墨从上往下灌到手指的位置——按得越低越响，墨就越多；松手墨在一秒多里慢慢褪掉，
 * 所以连着弹几个音，键盘上留着这一句的影子。
 * 条底下的小方块是此刻和弦里的音：跟着和弦时间线走，告诉你现在弹哪些最稳。
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Keyboards(modifier: Modifier = Modifier, clock: Clock, viewModel: MusicViewModel = viewModel()) {
    val info by viewModel.musicInfo.collectAsState()
    val keyCount by viewModel.keyboardNotes.collectAsState()
    val octave by viewModel.keyboardOctave.collectAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val channel = 1
    var key by remember { mutableIntStateOf(0) }
    var vel by remember { mutableIntStateOf(0) }
    var pressed by remember { mutableIntStateOf(-1) }
    val fills = remember { mutableStateMapOf<Int, Float>() }
    val baseVel = 40
    val rootKey = getMidiFromRootNote(info.root, octave = octave)
    val midiNotes = remember(rootKey, info.scale, keyCount) { getScaleNotes(rootMidi = rootKey, mode = info.scale, noteCount = keyCount) }
    val chordTones = currentChordTones(viewModel.chords, clock)
    val loopSteps = (info.bar * info.clap * 4).coerceAtLeast(16)

    Column(modifier) {
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
                .height(236.dp)
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
                            fills[index] = ny.coerceAtLeast(0.08f)
                            FluidSynthManager.playNote(key, vel, channel)
                            viewModel.markRoll(clock.position16.toInt() % loopSteps, key)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val nextKey = midiNotes[index]
                            val nextVel = (baseVel + ny * 87).toInt()
                            if (index != pressed) { fills[pressed] = 0f; pressed = index }
                            fills[index] = ny.coerceAtLeast(0.08f)
                            if (nextKey != key || kotlin.math.abs(nextVel - vel) > 5) {
                                FluidSynthManager.stopNoteDelay(key, channel)
                                key = nextKey
                                vel = nextVel
                                FluidSynthManager.playNote(key, vel, channel)
                                if (nextKey != key) viewModel.markRoll(clock.position16.toInt() % loopSteps, key)
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            FluidSynthManager.stopNote(key, channel)
                            fills[pressed] = 0f
                            pressed = -1
                            true
                        }
                        else -> false
                    }
                },
        ) {
            Row(Modifier.fillMaxWidth().fillMaxHeight().background(Hair)) {
                midiNotes.forEachIndexed { index, midi ->
                    KeyStrip(
                        modifier = Modifier.weight(1f).fillMaxHeight().padding(start = if (index == 0) 1.dp else 0.dp, end = 1.dp, top = 1.dp, bottom = 1.dp),
                        fill = fills[index] ?: 0f,
                        held = index == pressed,
                        chordTone = ((midi % 12) + 12) % 12 in chordTones,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        PerformanceRoll(midiNotes = midiNotes, loopSteps = loopSteps, clock = clock, viewModel = viewModel)
    }
}

/** 此刻走到的和弦里有哪些音（相对 C 的半音） */
fun currentChordTones(chords: List<Chord>, clock: Clock): Set<Int> {
    val total = chords.sumOf { it.steps }
    if (total <= 0) return emptySet()
    val position = clock.position16 % total
    var acc = 0
    val chord = chords.firstOrNull { acc += it.steps; position < acc } ?: return emptySet()
    val root = getMidiFromRootNote(chord.root, chord.octave)
    return runCatching { getChrod(root, chord.type).map { ((it % 12) + 12) % 12 }.toSet() }.getOrDefault(emptySet())
}

@Composable
private fun KeyStrip(modifier: Modifier, fill: Float, held: Boolean, chordTone: Boolean) {
    // 按住时墨跟着手指走（不插值），松开后用 1.2 秒褪掉——连弹几个音会留下这一句的影子
    val ink by animateFloatAsState(targetValue = fill, animationSpec = if (held) snap() else tween(1200), label = "ink")
    Box(modifier.background(PaperWarm)) {
        if (ink > 0.001f) Box(Modifier.fillMaxWidth().fillMaxHeight(ink).background(Ink))
        if (chordTone) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).size(6.dp).background(if (ink > 0.92f) PaperWarm else Ink))
    }
}

/**
 * 演奏卷：这一轮循环里按过的键。横轴是循环里的十六分音符，纵轴是键盘上的音，按过的是墨，
 * 播放头走到的那一列淡墨。( clear ) 时清掉。
 */
@Composable
fun PerformanceRoll(midiNotes: List<Int>, loopSteps: Int, clock: Clock, viewModel: MusicViewModel) {
    val roll = viewModel.roll
    val head = clock.position16.toInt() % loopSteps
    Canvas(Modifier.fillMaxWidth().height((midiNotes.size * 4 + 2).dp.coerceAtMost(96.dp))) {
        val cell = size.width / loopSteps
        val row = size.height / midiNotes.size
        drawRect(Hair, Offset(0f, 0f), Size(size.width, size.height))
        drawRect(Ink20.copy(alpha = 0.35f), Offset(head * cell, 0f), Size(cell, size.height))
        for (bar in 1 until loopSteps / 16) drawRect(Ink20, Offset(bar * 16 * cell, 0f), Size(1f, size.height))
        roll.forEach { (step, notes) ->
            notes.forEach { midi ->
                val r = midiNotes.indexOf(midi)
                if (r >= 0) drawRect(Ink, Offset(step * cell + 1f, (midiNotes.size - 1 - r) * row + 1f), Size((cell - 2f).coerceAtLeast(1f), (row - 2f).coerceAtLeast(1f)))
            }
        }
    }
}

/** 段标题右侧：( voice ) e-piano，点开选音色 */
@Composable
fun VoicePicker(viewModel: MusicViewModel = viewModel()) {
    val voice by viewModel.voice.collectAsState()
    var picking by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { picking = true },
    ) {
        Small("( voice )")
        Text(voice.name, style = ChromeStyle, color = Ink)
    }
    if (picking) PickerSheet("voice", VOICES, voice, { viewModel.updateVoice(it); picking = false }, { picking = false }) { it.name }
}

/** 键盘底下那一行，靠右：( notes ) 12 − +   ( octave ) 4 − + */
@Composable
fun KeyboardSteppers(viewModel: MusicViewModel = viewModel()) {
    val notes by viewModel.keyboardNotes.collectAsState()
    val octave by viewModel.keyboardOctave.collectAsState()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
        Stepper("notes", notes) { viewModel.updateKeyboardNotes(it) }
        Stepper("octave", octave) { viewModel.updateKeyboardOctave(it) }
    }
}

@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            .width(22.dp)
            .height(28.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(glyph, style = ChromeStyle, color = Ink50) }
}
