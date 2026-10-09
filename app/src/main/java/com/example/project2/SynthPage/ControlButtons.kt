package com.example.project2.SynthPage

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager
import com.example.project2.ui.theme.Hair
import com.example.project2.ui.theme.Hairline
import com.example.project2.ui.theme.HeadingStyle
import com.example.project2.ui.theme.Ink
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
fun Transport(modifier: Modifier = Modifier, filepath: File, viewModel: MetronomeViewModel) {
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

    if (saving) SaveDialog(filepath = filepath, viewModel = viewModel, onDismiss = { saving = false })
}

/** 小节进度：一条发丝线，墨从左往右走 */
@Composable
fun BeatLine(modifier: Modifier = Modifier, viewModel: MetronomeViewModel) {
    val count by viewModel.count.collectAsState()
    val progress by animateFloatAsState(
        targetValue = count.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 120, easing = LinearEasing),
        label = "beat",
    )
    Box(modifier.fillMaxWidth().height(1.dp).background(Hair)) {
        Box(Modifier.fillMaxWidth(progress).height(1.dp).background(Ink))
    }
}

/** 右上角的小节计数：把 0–1 的进度换算成 1–4 拍 */
@Composable
fun BeatCounter(viewModel: MetronomeViewModel) {
    val count by viewModel.count.collectAsState()
    val beat = (count * 4).toInt().coerceIn(0, 3) + 1
    Small("beat $beat / 4")
}

@Composable
fun SaveDialog(filepath: File, viewModel: MetronomeViewModel, onDismiss: () -> Unit) {
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
