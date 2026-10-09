package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.ChromeStyle
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Hairline
import com.example.project2.ui.theme.HeadingStyle
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Ink20
import com.example.project2.ui.theme.Ink35
import com.example.project2.ui.theme.LabelButton
import com.example.project2.ui.theme.PaperDialog
import com.example.project2.ui.theme.Small
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 走带：rec / play / clear / click / save，五枚括号标签排一行。
 * 开着的那枚字转实墨、底下有线；没有红点，没有图标。
 */
@Composable
fun Transport(modifier: Modifier = Modifier, filepath: File) {
    var recording by remember { mutableStateOf(false) }
    var playing by remember { mutableStateOf(false) }
    var click by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        LabelButton(text = "rec", active = recording, onClick = {
            recording = !recording
            if (recording) FluidSynthManager.startRecording() else FluidSynthManager.stopRecording()
        })
        LabelButton(text = if (playing) "stop" else "play", active = playing, onClick = {
            playing = !playing
            if (playing) FluidSynthManager.startPlayback() else FluidSynthManager.stopPlayback()
        })
        LabelButton(text = "clear", onClick = { FluidSynthManager.clearLoop() })
        LabelButton(text = "click", active = click, onClick = {
            click = !click
            if (click) FluidSynthManager.turnMetronomeON() else FluidSynthManager.turnMetronomeOff()
        })
        LabelButton(text = "save", onClick = { saving = true })
    }

    if (saving) SaveDialog(filepath = filepath, onDismiss = { saving = false })
}

/** 播放头：一条发丝线，上面每小节一个刻度，墨从左往右连续地走完整个循环 */
@Composable
fun Playhead(clock: Clock, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            repeat(clock.bars) { bar ->
                Box(Modifier.weight(1f)) {
                    Box(Modifier.width(1.dp).height(6.dp).background(if (bar == 0) Ink20 else Ink20))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hair)) {
            Box(Modifier.fillMaxWidth(clock.loop).height(1.dp).background(Ink))
        }
    }
}

/** 顶栏右边的拍号：1 2 3 4，走到哪拍哪拍是墨 */
@Composable
fun BeatTicks(clock: Clock) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(clock.beatsPerBar) { beat ->
            Text("${beat + 1}", style = ChromeStyle, color = if (beat == clock.beatInBar) Ink else Ink20)
        }
    }
}

@Composable
fun SaveDialog(filepath: File, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("name") } // name → saving → done
    val scope = rememberCoroutineScope()

    PaperDialog(
        title = when (state) { "name" -> "save as"; "saving" -> "saving"; else -> "saved" },
        onDismiss = onDismiss,
        actions = {
            when (state) {
                "name" -> {
                    LabelButton(text = "save", enabled = name.isNotBlank(), active = name.isNotBlank(), onClick = {
                        state = "saving"
                        scope.launch {
                            withContext(Dispatchers.IO) { FluidSynthManager.SaveToWav(name, filepath.absolutePath) }
                            state = "done"
                        }
                    })
                    LabelButton(text = "cancel", onClick = onDismiss)
                }
                "saving" -> Small("writing wav")
                else -> LabelButton(text = "ok", active = true, onClick = onDismiss)
            }
        },
    ) {
        when (state) {
            "name" -> Column {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = HeadingStyle.copy(color = Ink),
                    cursorBrush = SolidColor(Ink),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box { if (name.isEmpty()) Text("untitled", style = HeadingStyle, color = Ink35); inner() }
                    },
                )
                Spacer(Modifier.height(10.dp))
                Hairline()
                Spacer(Modifier.height(10.dp))
                Small(filepath.absolutePath.removePrefix("/storage/emulated/0/"))
            }
            "saving" -> Small("…")
            else -> Text("$name.wav", style = HeadingStyle, color = Ink)
        }
    }
}
