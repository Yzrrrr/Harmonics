package com.example.project2.SynthPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2.FluidSynthManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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