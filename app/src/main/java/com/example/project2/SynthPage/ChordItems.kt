package com.example.project2.SynthPage

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.flow.collectLatest
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.Collections
import java.util.UUID


@Composable
fun ChordItem(
    modifier: Modifier = Modifier,
    initialChord: String = "maj7",
    RootNote: String = "C",
    initialoctave: Int = 4,
    initialBeats: Int = 4,
    onDelete: () -> Unit = {},
    onChordUpdated: (String, Int, String,Int) -> Unit = {a,b,c,d -> }
) {
    var showDialog by remember { mutableStateOf(false) }
    var chord by remember { mutableStateOf(initialChord) }
    var beats by remember { mutableStateOf(initialBeats) }
    var root by remember { mutableStateOf(RootNote) }
    var octave by remember { mutableStateOf(initialoctave) }
    // 计算宽度
    val chordWidth = (beats * 15).dp  // 每拍 15.dp

    Box(
        modifier = modifier
            .width(chordWidth)
            .height(60.dp)
            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Text(text = root + chord, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }

    if (showDialog) {
        ChordDialog(
            initialChord = chord,
            initialBeats = beats,
            initialRoot = root,
            initialoctave = octave,
            onConfirm = { selectedChord, selectedBeats,selectedRoot,selectedoctave ->
                chord = selectedChord
                beats = selectedBeats
                root = selectedRoot
                octave = selectedoctave
                onChordUpdated(selectedChord, selectedBeats, selectedRoot, selectedoctave)
                showDialog = false
            },
            onDelete = {
                showDialog = false
                onDelete()
            },
            onDismiss = { showDialog = false }
        )
    }
}


@Preview
@Composable
private fun ChordItemPrev() {
    //ChordItem()
}

@Composable
fun ChordDialog(
    initialChord: String,
    initialBeats: Int,
    initialRoot: String,
    initialoctave: Int,
    onConfirm: (String, Int, String, Int) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedChord by remember { mutableStateOf(initialChord) }
    var selectedBeats by remember { mutableStateOf(initialBeats) }
    var selectedRoot by remember { mutableStateOf(initialRoot) }
    var selectedoctave by remember { mutableStateOf(initialoctave) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(selectedChord, selectedBeats, selectedRoot, selectedoctave) }) {
                Text("确定")
            }
        },
        dismissButton = {
            Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("删除")
            }
        },
        title = { Text("编辑和弦") },
        text = {
            Column {
                Text("选择和弦:")
                val chords = listOf("major", "minor", "7", "maj7", "m7")
                DropdownSelector(
                    options = chords,
                    selectedOption = selectedChord,
                    onOptionSelected = { selectedChord = it }
                )
                Text("选择根音")
                val Root = listOf("C","#C","D","#D","E","F","#F","G","#G","A","#A","B")
                DropdownSelector(
                    options = Root,
                    selectedOption = selectedRoot,
                    onOptionSelected = { selectedRoot = it }
                )
                Text("选择音高")
                DropdownSelector(
                    options = (1..8).map { it.toString() },
                    selectedOption = selectedoctave.toString(),
                    onOptionSelected = { selectedoctave = it.toInt() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("选择时长:")
                DropdownSelector(
                    options = (1..16).map { it.toString() },
                    selectedOption = selectedBeats.toString(),
                    onOptionSelected = { selectedBeats = it.toInt() }
                )
            }
        }
    )
}


@Composable
fun DropdownSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedOption)
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewChordSequence() {
    VerticalReorderList()
}

data class Chord(var root: String, var type: String, var beats: Int, var octave:Int, val id: String = UUID.randomUUID().toString())

@Composable
fun VerticalReorderList(modifier: Modifier = Modifier) {
    val chords = remember {
        mutableStateListOf(
            Chord("C", "maj7", 4, 4),
        )
    }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        chords.apply {
            if (from.index != to.index) {
                Collections.swap(this, from.index, to.index)
            }
        }
    })

    LaunchedEffect(chords) {
        snapshotFlow { chords.toList() }
            .collectLatest { updatedChords ->
                Log.d("ChordsDebug", "Updated Chords: $updatedChords")
                // 先删除旧和弦
                FluidSynthManager.delAllChordNote()
                updatedChords.forEachIndexed { index, chord ->

                    val timeNum = updatedChords.take(index).sumOf { it.beats }
                    val rootNum = getMidiFromRootNote(chord.root, chord.octave)
                    // 重新写入新和弦
                    setChrod(rootNum, chord.type, timeNum, svel = 60, clapOnCount = chord.beats)
                }
            }
    }
    Card (
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ){
        Row(
            horizontalArrangement = Arrangement.Start
        ) {
            LazyRow(
                state = state.listState,
                modifier = Modifier
                    .widthIn(min = 20.dp, max = 320.dp)
                    .reorderable(state)
                    .detectReorderAfterLongPress(state)
            ) {
                items(chords, key = { it.id }) { chord ->
                    val chordIndex = chords.indexOf(chord)
                    val timeNum = chords.take(chordIndex).sumOf { it.beats }
                    ReorderableItem(state, key = chord) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 16.dp else 0.dp)
                        Box(
                            modifier = Modifier
                                .shadow(elevation)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 2.dp)
                        ) {
                            ChordItem(
                                initialChord = chord.type,
                                RootNote = chord.root,
                                initialBeats = chord.beats,
                                onChordUpdated = { selectedChord, selectedBeats, selectedRoot, selectedoctave ->
                                    val rootNumPrv = getMidiFromRootNote(chord.root, chord.octave)
                                    delChord(rootNumPrv, chord.type, timeNum)
                                    chord.type = selectedChord
                                    chord.beats = selectedBeats
                                    chord.root = selectedRoot
                                    chord.octave = selectedoctave
                                    Log.d("ChordsDebug", "Updated Chords: $chords")
                                    val rootNum = getMidiFromRootNote(chord.root, chord.octave)
                                    setChrod(
                                        rootNum,
                                        chord.type,
                                        timeNum,
                                        svel = 60,
                                        clapOnCount = chord.beats
                                    )
                                },
                                onDelete = { chords.remove(chord) },
                            )
                        }
                    }
                }
            }
            AddChordButton(
                modifier = Modifier
                    .widthIn(min = 20.dp, max = 40.dp),
                onAddChord = { chord, beats, root, octave ->
                    val newChord = Chord(root, chord, beats, octave)
                    chords.add(newChord)
                }
            )
        }
    }
}

@Composable
fun AddChordButton(modifier: Modifier = Modifier, onAddChord: (chord:String, beats:Int, root:String, octave:Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var chord by remember { mutableStateOf("maj7") }
    var beats by remember { mutableStateOf(4) }
    var root by remember { mutableStateOf("C") }
    var octave by remember { mutableStateOf(4) }

    IconButton(
        onClick = { showDialog =true},
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.AddCircle,
            contentDescription = "send")
    }

    if(showDialog){
        ChordDialog(
            initialChord = chord,
            initialBeats = beats,
            initialRoot = root,
            initialoctave = octave,
            onConfirm = { selectedChord, selectedBeats,selectedRoot,selectedoctave ->
                onAddChord(selectedChord, selectedBeats, selectedRoot, selectedoctave)
                showDialog = false
            },
            onDelete = {
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

//val rootNum: Int = getMidiFromRootNote(root, octave)
//                setChrod(root=rootNum, timeNum = timeNum, svel = 60, clapOnCount = beats, type = chord)
// delChord(root=rootNum, timeNum = timeNum, type = chord)

@Preview
@Composable
private fun DragPrev() {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        VerticalReorderList()
    }
}