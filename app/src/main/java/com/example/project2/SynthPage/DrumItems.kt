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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink20
import com.example.project2.ui.theme.Ink35
import com.example.project2.ui.theme.Ink70
import com.example.project2.ui.theme.Ink50
import com.example.project2.ui.theme.Paper
import com.example.project2.ui.theme.PaperWarm
import com.example.project2.ui.theme.Small

private data class Drum(val name: String, val note: Int)

private val DRUMS = listOf(
    Drum("boom", 35), Drum("clap", 38), Drum("tom", 45), Drum("crash", 51), Drum("hats", 42),
)

/** 三档力度：轻、中、重。点一下升一档，重了再点就灭 */
private val LEVEL_VEL = listOf(0, 55, 85, 115)
private val LEVEL_INK = listOf(PaperWarm, Ink35, Ink70, Ink)

/**
 * 鼓机：五行十六步的格。每行左边一个小字名，右边十六个格子分四拍。
 * 格子有三档墨：轻、中、重，点一下升一档，重了再点就灭。
 * 播放头是一列：走到哪一步，那一列的空格变深一档，亮格淡一下，像被敲了一下。
 */
@Composable
fun DrumSet(modifier: Modifier = Modifier, clock: Clock) {
    val step = clock.step16
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(52.dp))
            repeat(4) { beat ->
                Small("${beat + 1}", Modifier.weight(1f).padding(start = if (beat == 0) 0.dp else 6.dp), color = if (beat == clock.beatInBar) Ink else Ink50)
            }
        }
        DRUMS.forEach { drum -> DrumRow(drum, step) }
    }
}

@Composable
private fun DrumRow(drum: Drum, playStep: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Small(drum.name, Modifier.width(52.dp))
        repeat(4) { beat ->
            Row(Modifier.weight(1f).padding(start = if (beat == 0) 0.dp else 6.dp).background(Hair)) {
                repeat(4) { sub ->
                    val step = beat * 4 + sub
                    DrumStep(
                        modifier = Modifier.weight(1f).padding(start = if (sub == 0) 1.dp else 0.dp, end = 1.dp, top = 1.dp, bottom = 1.dp),
                        playing = step == playStep,
                        onLevel = { level ->
                            FluidSynthManager.delDrumNote(timeNum = step, note = drum.note)
                            if (level > 0) FluidSynthManager.setDrumNote(timeNum = step, note = drum.note, svel = LEVEL_VEL[level])
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrumStep(modifier: Modifier = Modifier, playing: Boolean, onLevel: (Int) -> Unit) {
    var level by remember { mutableIntStateOf(0) }
    val base = LEVEL_INK[level]
    val target = when {
        level > 0 && playing -> base.copy(alpha = 0.55f)
        playing -> Ink20
        else -> base
    }
    val fill by animateColorAsState(target, label = "step")
    Box(
        modifier
            .height(34.dp)
            .background(PaperWarm)
            .background(fill)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                level = (level + 1) % 4
                onLevel(level)
            },
    )
}
