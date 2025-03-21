package com.example.project2.SynthPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager


@Composable
fun BasicMusicInfoSet(modifier: Modifier = Modifier, viewModel:MusicViewModel = viewModel()) {
    val musicInfo by viewModel.musicInfo.collectAsState()

    LaunchedEffect(musicInfo) {
        FluidSynthManager.setBasicMusicInfo(musicInfo.BPM, musicInfo.bar, musicInfo.clap)
    }


    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.updateBar(1)
                    }
                ) { Text(text = "1 BAR") }
                Button(
                    onClick = {
                        viewModel.updateBar(2)
                    }
                ) { Text(text = "2 BAR") }
                Button(
                    onClick = {
                        viewModel.updateBar(4)
                    }
                ) { Text(text = "4 BAR") }
                Button(
                    onClick = {
                        viewModel.updateBar(8)
                    }
                ) { Text(text = "8 BAR") }
            }
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ){
                DropdownMenu(
                    text = "BPM: ${musicInfo.BPM}",
                    menuItemData = (40..240).toList().map { it.toString() },
                    OnClick = {
                        viewModel.updateBPM(it.toInt())
                    }
                )
                DropdownMenu(
                    text = "Clap: ${musicInfo.clap}",
                    menuItemData = (3..4).toList().map { it.toString() },
                    OnClick = {
                        viewModel.updateClap(it.toInt())
                    }
                )
                DropdownMenu(
                    text = "Root: ${musicInfo.root}",
                    menuItemData = listOf("C", "#C", "D", "#D", "E", "F", "#F", "G", "#G", "A", "#A", "B"),
                    OnClick = {
                        viewModel.updateRoot(it)
                    }
                )
                DropdownMenu(
                    text = "Scale: ${musicInfo.scale}",
                    menuItemData = listOf("major", "minor", "blues", "dorian","major_pentatonic","minor_pentatonic","harmonic_minor","melodic_minor","dorian","phrygian","lydian","mixolydian","locrian"),
                    OnClick = {
                        viewModel.updateScale(it)
                    }
                )
            }
        }

    }
}

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    text :String,
    menuItemData : List<String>,
    OnClick : (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Placeholder list of 100 strings for demonstration

    Box(
        modifier = modifier
            .padding(6.dp)
    ) {
        TextButton(
            onClick = { expanded = !expanded }
        ) {
            Text(text = text)
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuItemData.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { OnClick(option) }
                )
            }
        }
    }
}
