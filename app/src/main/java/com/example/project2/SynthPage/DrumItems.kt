package com.example.project2.SynthPage

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.PaperWarm
import com.example.project2.ui.theme.Small

private data class Drum(val name: String, val note: Int, val vel: Int = 100)

private val DRUMS = listOf(
    Drum("boom", 35), Drum("clap", 38), Drum("tom", 45), Drum("crash", 51), Drum("hats", 42),
)

/**
 * 鼓组：五行十六步的格。每行左边一个小字名，右边十六个格子，
 * 每四格之间一条深一档的线标出拍。点亮的格是墨。
 */
@Composable
fun DrumSet(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(52.dp))
            repeat(4) { beat ->
                Small("${beat + 1}", Modifier.weight(1f).padding(start = if (beat == 0) 0.dp else 6.dp))
            }
        }
        DRUMS.forEach { drum -> DrumRow(drum) }
    }
}

@Composable
private fun DrumRow(drum: Drum) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Small(drum.name, Modifier.width(52.dp))
        repeat(4) { beat ->
            Row(
                Modifier
                    .weight(1f)
                    .padding(start = if (beat == 0) 0.dp else 6.dp)
                    .background(Hair),
            ) {
                repeat(4) { sub ->
                    val step = beat * 4 + sub
                    DrumStep(
                        modifier = Modifier.weight(1f).padding(start = if (sub == 0) 1.dp else 0.dp, end = 1.dp, top = 1.dp, bottom = 1.dp),
                        onStart = { FluidSynthManager.setDrumNote(timeNum = step, note = drum.note, svel = drum.vel) },
                        onStop = { FluidSynthManager.delDrumNote(timeNum = step, note = drum.note) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrumStep(modifier: Modifier = Modifier, onStart: () -> Unit, onStop: () -> Unit) {
    var on by remember { mutableStateOf(false) }
    val fill by animateColorAsState(if (on) Ink else PaperWarm, label = "step")
    Box(
        modifier
            .height(30.dp)
            .background(fill)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                on = !on
                if (on) onStart() else onStop()
            },
    )
}
