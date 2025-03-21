package com.example.project2.SynthPage

import com.example.project2.FluidSynthManager

data class BasicMusicInfo(
    var BPM : Int,
    var bar : Int,
    var clap : Int,
    var root : String,
    var scale : String
)


fun CountKeyAndVel(num: Int, midiNotes: List<Int> ): Int {
    val key = midiNotes[num]
    return key
}

fun getScaleNotes(rootMidi: Int, mode: String, noteCount: Int): List<Int> {
    val scales = mapOf(
        "major" to listOf(2, 2, 1, 2, 2, 2, 1), // 大调
        "minor" to listOf(2, 1, 2, 2, 1, 2, 2), // 小调
        "harmonic_minor" to listOf(2, 1, 2, 2, 1, 3, 1), // 和声小调
        "melodic_minor" to listOf(2, 1, 2, 2, 2, 2, 1), // 旋律小调
        "blues" to listOf(3, 2, 1, 1, 3, 2), // 布鲁斯音阶
        "major_pentatonic" to listOf(2, 2, 3, 2, 3), // 大五声音阶
        "minor_pentatonic" to listOf(3, 2, 2, 3, 2), // 小五声音阶
        "dorian" to listOf(2, 1, 2, 2, 2, 1, 2), // 多利安
        "phrygian" to listOf(1, 2, 2, 2, 1, 2, 2), // 弗里吉亚
        "lydian" to listOf(2, 2, 2, 1, 2, 2, 1), // 利底亚
        "mixolydian" to listOf(2, 2, 1, 2, 2, 1, 2), // 密克索利底亚
        "locrian" to listOf(1, 2, 2, 1, 2, 2, 2) // 洛克利安
    )
// "harmonic_minor","melodic_minor","major_pentatonic","minor_pentatonic","dorian","phrygian","lydian","mixolydian","locrian"
    // 获取调式对应的音程结构
    val scalePattern = scales[mode.lowercase()] ?: throw IllegalArgumentException("Unknown mode: $mode")

    // 计算音阶中的 MIDI 码
    val midiNotes = mutableListOf(rootMidi)
    var currentNote = rootMidi

    for (i in 0 until (noteCount - 1)) {
        val interval = scalePattern[i % scalePattern.size] // 允许超出 7 音的情况
        currentNote += interval
        midiNotes.add(currentNote)
    }

    return midiNotes
}

fun getChrod(root: Int, type: String):List<Int>{
    val chordIntervals = mapOf(
        "major" to listOf(0, 4, 7), // 大三和弦
        "minor" to listOf(0, 3, 7), // 小三和弦
        "7" to listOf(0, 4, 7, 10), // 属七和弦
        "maj7" to listOf(0, 4, 7, 11), // 大七和弦
        "m7" to listOf(0, 3, 7, 10) // 小七和弦
    )
    return chordIntervals[type]?.map { root + it } ?: error("Unknown chrod")
}

fun getMidiFromRootNote(rootNote: String, octave: Int = 4): Int {
    val noteToMidi = mapOf(
        "C" to 0, "#C" to 1, "D" to 2, "#D" to 3,
        "E" to 4, "F" to 5, "#F" to 6, "G" to 7,
        "#G" to 8, "A" to 9, "#A" to 10, "B" to 11
    )

    return noteToMidi[rootNote]?.let { it + (octave * 12) }
        ?: throw IllegalArgumentException("Unknown Note: $rootNote")
}

fun setChrod(root: Int, type: String, timeNum:Int, svel :Int, clapOnCount: Int){
    val chordIntervals = getChrod(root, type)
    chordIntervals.forEach{note ->
        FluidSynthManager.setChordNote(note, timeNum , svel, clapOnCount)
    }
}

fun delChord(root: Int, type: String, timeNum:Int){
    val chordIntervals = getChrod(root, type)
    chordIntervals.forEach{note ->
        FluidSynthManager.delChordNote(note, timeNum)
    }
}