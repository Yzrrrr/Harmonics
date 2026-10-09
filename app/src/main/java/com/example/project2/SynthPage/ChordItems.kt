package com.example.project2.SynthPage

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.HeadingStyle
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink20
import com.example.project2.ui.theme.LabelButton
import com.example.project2.ui.theme.Paper
import com.example.project2.ui.theme.PaperDialog
import com.example.project2.ui.theme.PaperWarm
import com.example.project2.ui.theme.PickerSheet
import com.example.project2.ui.theme.Small
import kotlinx.coroutines.flow.collectLatest
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.Collections
import java.util.UUID

val CHORD_TYPES = listOf("major", "minor", "7", "maj7", "m7")

/** steps 以十六分音符计：4 步 = 一拍 */
data class Chord(var root: String, var type: String, var steps: Int, var octave: Int, val id: String = UUID.randomUUID().toString())

/** 和弦名：Cmaj7、Dm、G7 */
fun chordName(root: String, type: String) = rootLabel(root) + when (type) {
    "major" -> ""
    "minor" -> "m"
    else -> type
}

/**
 * 时间线上的一块和弦：宽度按步数分摊整行，暖纸底、发丝线框；播放头走到它时整块变墨、字变纸色。
 * 长按拖动换序，点一下编辑。
 */
@Composable
fun ChordBlock(chord: Chord, width: Dp, current: Boolean, onClick: () -> Unit) {
    val fill by animateColorAsState(if (current) Ink else PaperWarm, label = "chord")
    val text by animateColorAsState(if (current) Paper else Ink, label = "chordText")
    Column(
        Modifier
            .width(width)
            .height(64.dp)
            .background(Hair)
            .padding(1.dp)
            .background(fill)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(chordName(chord.root, chord.type), style = ChromeStyle, color = text, maxLines = 1)
        Small("${chord.steps / 4f}".removeSuffix(".0") + " beat", color = text.copy(alpha = 0.6f))
    }
}

/**
 * 编辑和弦：四个值各自点开选项单；底下 ( save ) ( delete ) ( cancel )。
 */
@Composable
fun ChordDialog(
    initial: Chord,
    onConfirm: (type: String, steps: Int, root: String, octave: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var type by remember { mutableStateOf(initial.type) }
    var steps by remember { mutableStateOf(initial.steps) }
    var root by remember { mutableStateOf(initial.root) }
    var octave by remember { mutableStateOf(initial.octave) }
    var picking by remember { mutableStateOf<String?>(null) }

    PaperDialog(
        title = "chord",
        onDismiss = onDismiss,
        actions = {
            LabelButton("save", active = true, onClick = { onConfirm(type, steps, root, octave) })
            if (onDelete != null) LabelButton("delete", onClick = onDelete)
            LabelButton("cancel", onClick = onDismiss)
        },
    ) {
        Text(chordName(root, type), style = HeadingStyle, color = Ink)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Field("root", rootLabel(root)) { picking = "root" }
            Field("type", type) { picking = "type" }
            Field("octave", octave.toString()) { picking = "octave" }
            Field("steps", steps.toString()) { picking = "steps" }
        }
    }

    when (picking) {
        "root" -> PickerSheet("root", ROOTS, root, { root = it; picking = null }, { picking = null }, ::rootLabel)
        "type" -> PickerSheet("type", CHORD_TYPES, type, { type = it; picking = null }, { picking = null })
        "octave" -> PickerSheet("octave", (1..8).toList(), octave, { octave = it; picking = null }, { picking = null })
        "steps" -> PickerSheet("steps (4 = one beat)", (1..16).toList(), steps, { steps = it; picking = null }, { picking = null })
    }
}

@Composable
private fun Field(label: String, value: String, onClick: () -> Unit) {
    Column(Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Small("( $label )")
        Spacer(Modifier.height(6.dp))
        Text(value, style = ChromeStyle, color = Ink)
    }
}

/**
 * 和弦时间线：上面一排刻度（每步一道，每拍一道长的），下面一行按步数分宽的和弦块，末尾 ( + chord )。
 * 序列一变就整条重写进合成器，和原来的逻辑一致。
 */
@Composable
fun ChordTimeline(modifier: Modifier = Modifier, clock: Clock) {
    val chords = remember { mutableStateListOf(Chord("C", "maj7", 8, 4), Chord("A", "m7", 8, 4)) }
    var editing by remember { mutableStateOf<Chord?>(null) }
    var adding by remember { mutableStateOf(false) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        if (from.index != to.index && from.index < chords.size && to.index < chords.size) Collections.swap(chords, from.index, to.index)
    })

    LaunchedEffect(chords) {
        snapshotFlow { chords.map { it.copy() } }.collectLatest { updated ->
            FluidSynthManager.delAllChordNote()
            updated.forEachIndexed { index, chord ->
                val timeNum = updated.take(index).sumOf { it.steps }
                setChrod(getMidiFromRootNote(chord.root, chord.octave), chord.type, timeNum, svel = 60, clapOnCount = chord.steps)
            }
        }
    }

    val total = chords.sumOf { it.steps }.coerceAtLeast(1)
    val position = clock.position16 % total
    val currentIndex = run {
        var acc = 0
        chords.indexOfFirst { acc += it.steps; position < acc }
    }

    Column(modifier.fillMaxWidth()) {
        // 刻度
        Row(Modifier.fillMaxWidth().height(10.dp), verticalAlignment = Alignment.Bottom) {
            repeat(total) { i ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.BottomStart) {
                    Box(Modifier.width(1.dp).height(if (i % 4 == 0) 10.dp else 4.dp).background(if (i % 4 == 0) Ink20 else Hair))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val gap = 4.dp
            val usable = maxWidth - gap * (chords.size - 1).coerceAtLeast(0)
            LazyRow(
                state = state.listState,
                modifier = Modifier.fillMaxWidth().reorderable(state).detectReorderAfterLongPress(state),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                items(chords, key = { it.id }) { chord ->
                    ReorderableItem(state, key = chord.id) { _ ->
                        ChordBlock(
                            chord = chord,
                            width = usable * chord.steps / total,
                            current = chords.indexOf(chord) == currentIndex,
                            onClick = { editing = chord },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LabelButton("+ chord", onClick = { adding = true })
        }
    }

    editing?.let { chord ->
        ChordDialog(
            initial = chord,
            onConfirm = { type, steps, root, octave ->
                val index = chords.indexOfFirst { it.id == chord.id }
                if (index >= 0) chords[index] = chord.copy(type = type, steps = steps, root = root, octave = octave)
                editing = null
            },
            onDelete = { chords.removeAll { it.id == chord.id }; editing = null },
            onDismiss = { editing = null },
        )
    }
    if (adding) {
        ChordDialog(
            initial = Chord("C", "maj7", 4, 4),
            onConfirm = { type, steps, root, octave -> chords.add(Chord(root, type, steps, octave)); adding = false },
            onDelete = null,
            onDismiss = { adding = false },
        )
    }
}
