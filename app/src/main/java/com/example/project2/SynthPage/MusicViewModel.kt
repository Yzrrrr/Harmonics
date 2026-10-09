package com.example.project2.SynthPage

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Voice(val name: String, val program: Int)

/** GeneralUser GS 里挑的十个：名字按乐手叫法 */
val VOICES = listOf(
    Voice("piano", 0), Voice("bright piano", 1), Voice("e-piano", 4), Voice("vibes", 11), Voice("organ", 16),
    Voice("nylon guitar", 24), Voice("bass", 33), Voice("strings", 48), Voice("flute", 73), Voice("square lead", 80),
)

class MusicViewModel : ViewModel() {
    private val _musicInfo = MutableStateFlow(BasicMusicInfo(120, 4, 4, "C", "major"))
    val musicInfo: StateFlow<BasicMusicInfo> = _musicInfo

    fun updateBPM(newBPM: Int) {
        _musicInfo.update { it.copy(BPM = newBPM) }
    }

    fun updateBar(newBar: Int) {
        _musicInfo.update { it.copy(bar = newBar) }
    }

    fun updateClap(newClap: Int) {
        _musicInfo.update { it.copy(clap = newClap) }
    }

    fun updateRoot(newRoot: String) {
        _musicInfo.update { it.copy(root = newRoot) }
    }

    fun updateScale(newScale: String) {
        _musicInfo.update { it.copy(scale = newScale) }
    }

    fun resetMusicInfo() {
        _musicInfo.value = BasicMusicInfo(120, 4, 4, "C", "major")
    }

    /** 键盘：有几个音、从第几个八度起。放在这里是因为段标题旁的步进器和键盘本身不在同一个组合函数里 */
    private val _keyboardNotes = MutableStateFlow(12)
    val keyboardNotes: StateFlow<Int> = _keyboardNotes
    private val _keyboardOctave = MutableStateFlow(4)
    val keyboardOctave: StateFlow<Int> = _keyboardOctave
    fun updateKeyboardNotes(n: Int) { _keyboardNotes.value = n.coerceIn(1, 24) }
    fun updateKeyboardOctave(o: Int) { _keyboardOctave.value = o.coerceIn(1, 8) }

    /** 键盘音色：GM program number */
    private val _voice = MutableStateFlow(VOICES[1])
    val voice: StateFlow<Voice> = _voice
    fun updateVoice(v: Voice) {
        _voice.value = v
        FluidSynthManager.setProgram(1, v.program)
    }

    /** 和弦序列，键盘要知道现在走到哪个和弦 */
    val chords = mutableStateListOf(Chord("C", "maj7", 8, 4), Chord("A", "m7", 8, 4), Chord("F", "maj7", 8, 4), Chord("G", "7", 8, 4))

    /**
     * 演奏卷：这一轮循环里在哪一步按了哪个键，UI 自己记的，和底层录音无关。
     * key = 循环内的十六分音符位置，value = 键盘上的音（midi）
     */
    val roll = mutableStateMapOf<Int, MutableSet<Int>>()

    /** 录音开着时弹的，底层会在循环里回放；这里同步记一份，画成实墨的卷 */
    val recorded = mutableStateMapOf<Int, MutableSet<Int>>()
    private val _recording = MutableStateFlow(false)
    val recording: StateFlow<Boolean> = _recording
    fun setRecording(on: Boolean) { _recording.value = on }

    fun markRoll(step: Int, midi: Int) {
        roll[step] = (roll[step] ?: emptySet()).plus(midi).toMutableSet()
        if (_recording.value) recorded[step] = (recorded[step] ?: emptySet()).plus(midi).toMutableSet()
    }
    fun clearRoll() { roll.clear(); recorded.clear() }

}


open class MetronomeViewModel : ViewModel() {

    private val _count = MutableStateFlow(0.0) // 使用 StateFlow 存储 count
    open val count: StateFlow<Double> = _count.asStateFlow()

    init {
        // 启动协程轮询 count
        viewModelScope.launch {
            while (true) {
                val newCount = FluidSynthManager.getCount() // 调用 C++ 获取最新 count
                _count.value = newCount
                delay(100) // 每 100ms 轮询一次
            }
        }
    }
}