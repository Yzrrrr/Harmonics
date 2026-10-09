package com.example.project2.SynthPage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.roundToInt

/**
 * 走时。
 *
 * 底层每拍把 count 加一，getCount 给的是 count / (bar*clap-1)。这里把它还原成拍序号，
 * 再用帧时间在两拍之间插值，播放头才能连续地走，而不是每 100ms 跳一下。
 */
data class Clock(
    /** 循环里的第几拍，0 起 */
    val beat: Int,
    /** 这一拍走了多少，0–1 */
    val phase: Float,
    val beatsPerBar: Int,
    val bars: Int,
) {
    val beatInBar get() = beat % beatsPerBar
    /** 整个循环的进度 0–1 */
    val loop get() = ((beat + phase) / (beatsPerBar * bars)).coerceIn(0f, 1f)
    /** 一小节 16 步里的位置（鼓机、和弦都按十六分音符编码） */
    val step16 get() = beatInBar * 4 + (phase * 4).toInt().coerceIn(0, 3)
    /** 循环里的十六分音符位置，连续值 */
    val position16 get() = (beat + phase) * 4
}

@Composable
fun rememberClock(viewModel: MetronomeViewModel, info: BasicMusicInfo): Clock {
    val progress by viewModel.count.collectAsState()
    val total = (info.bar * info.clap).coerceAtLeast(1)
    val beat = (progress * (total - 1)).roundToInt().coerceIn(0, total - 1)
    var phase by remember { mutableFloatStateOf(0f) }
    val beatNs = 60_000_000_000L / info.BPM.coerceAtLeast(1)
    LaunchedEffect(beat, beatNs) {
        var start = -1L
        while (true) {
            withFrameNanos { now ->
                if (start < 0) start = now
                phase = ((now - start).toFloat() / beatNs).coerceIn(0f, 0.999f)
            }
        }
    }
    return Clock(beat, phase, info.clap, info.bar)
}
