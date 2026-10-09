package com.example.project2.SynthPage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.HeadingStyle
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink50
import com.example.project2.ui.theme.LabelButton
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

data class Chord(var root: String, var type: String, var beats: Int, var octave: Int, val id: String = UUID.randomUUID().toString())

/** 和弦名：Cmaj7、Dm、G7 */
fun chordName(root: String, type: String) = root + when (type) {
    "major" -> ""
    "minor" -> "m"
    else -> type
}

/**
 * 一枚和弦：宽度按拍数，暖纸底、发丝线框，名字在左上、拍数在右下。长按拖动换序，点一下编辑。
 */
@Composable
fun ChordItem(
    chord: Chord,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .width((chord.beats * 22).dp.coerceAtLeast(72.dp))
            .height(64.dp)
            .background(Hair)
            .padding(1.dp)
            .background(PaperWarm)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(chordName(chord.root, chord.type), style = ChromeStyle, color = Ink)
        Small("${chord.beats} · o${chord.octave}", Modifier.align(Alignment.End))
    }
}

/**
 * 编辑和弦：四个值各自点开选项单；底下 ( save ) ( delete ) ( cancel )。
 */
@Composable
fun ChordDialog(
    initial: Chord,
    onConfirm: (type: String, beats: Int, root: String, octave: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var type by remember { mutableStateOf(initial.type) }
    var beats by remember { mutableStateOf(initial.beats) }
    var root by remember { mutableStateOf(initial.root) }
    var octave by remember { mutableStateOf(initial.octave) }
    var picking by remember { mutableStateOf<String?>(null) }

    PaperDialog(
        title = "chord",
        onDismiss = onDismiss,
        actions = {
            LabelButton("save", active = true, onClick = { onConfirm(type, beats, root, octave) })
            if (onDelete != null) LabelButton("delete", onClick = onDelete)
            LabelButton("cancel", onClick = onDismiss)
        },
    ) {
        Text(chordName(root, type), style = HeadingStyle, color = Ink)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Field("root", root) { picking = "root" }
            Field("type", type) { picking = "type" }
            Field("octave", octave.toString()) { picking = "octave" }
            Field("beats", beats.toString()) { picking = "beats" }
        }
    }

    when (picking) {
        "root" -> PickerSheet("root", ROOTS, root, { root = it; picking = null }, { picking = null })
        "type" -> PickerSheet("type", CHORD_TYPES, type, { type = it; picking = null }, { picking = null })
        "octave" -> PickerSheet("octave", (1..8).toList(), octave, { octave = it; picking = null }, { picking = null })
        "beats" -> PickerSheet("beats", (1..16).toList(), beats, { beats = it; picking = null }, { picking = null })
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
 * 和弦序列：一行可拖的和弦，末尾一枚 ( + chord )。
 * 序列一变就整条重写进合成器，和原来的逻辑一致。
 */
@Composable
fun VerticalReorderList(modifier: Modifier = Modifier) {
    val chords = remember { mutableStateListOf(Chord("C", "maj7", 4, 4)) }
    var editing by remember { mutableStateOf<Chord?>(null) }
    var adding by remember { mutableStateOf(false) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        if (from.index != to.index && from.index < chords.size && to.index < chords.size) Collections.swap(chords, from.index, to.index)
    })

    LaunchedEffect(chords) {
        snapshotFlow { chords.map { it.copy() } }.collectLatest { updated ->
            FluidSynthManager.delAllChordNote()
            updated.forEachIndexed { index, chord ->
                val timeNum = updated.take(index).sumOf { it.beats }
                setChrod(getMidiFromRootNote(chord.root, chord.octave), chord.type, timeNum, svel = 60, clapOnCount = chord.beats)
            }
        }
    }

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LazyRow(
            state = state.listState,
            modifier = Modifier.weight(1f).reorderable(state).detectReorderAfterLongPress(state),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(chords, key = { it.id }) { chord ->
                ReorderableItem(state, key = chord.id) { dragging ->
                    val scale by animateFloatAsState(if (dragging) 1.04f else 1f, label = "drag")
                    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
                        ChordItem(chord = chord, onClick = { editing = chord })
                    }
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        LabelButton("+ chord", onClick = { adding = true })
    }

    editing?.let { chord ->
        ChordDialog(
            initial = chord,
            onConfirm = { type, beats, root, octave ->
                val index = chords.indexOfFirst { it.id == chord.id }
                if (index >= 0) chords[index] = chord.copy(type = type, beats = beats, root = root, octave = octave)
                editing = null
            },
            onDelete = { chords.removeAll { it.id == chord.id }; editing = null },
            onDismiss = { editing = null },
        )
    }
    if (adding) {
        ChordDialog(
            initial = Chord("C", "maj7", 4, 4),
            onConfirm = { type, beats, root, octave -> chords.add(Chord(root, type, beats, octave)); adding = false },
            onDelete = null,
            onDismiss = { adding = false },
        )
    }
}
