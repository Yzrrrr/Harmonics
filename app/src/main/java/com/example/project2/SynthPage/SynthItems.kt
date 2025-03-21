package com.example.project2.SynthPage

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.FluidSynthManager


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Keyboards(modifier: Modifier = Modifier, viewModel: MusicViewModel = viewModel()) {
    val musicInfo by viewModel.musicInfo.collectAsState()
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var channel by remember { mutableStateOf(1) }
    var key by remember { mutableStateOf(0) }
    var vel by remember { mutableStateOf(0) }
    var temp_key by remember { mutableStateOf(0) }
    var temp_vel by remember { mutableStateOf(0) }
    val baseVel = 40
    var keyInterval by remember { mutableStateOf(12) }
    val mode = musicInfo.scale
    var octave by remember { mutableStateOf(4) }
    val rootKey = getMidiFromRootNote(musicInfo.root, octave = octave)


    val midiNotes = getScaleNotes(rootMidi = rootKey, mode = mode, noteCount = keyInterval)
    Surface(
        modifier = modifier
            .height( 300.dp),
        color = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                modifier = Modifier.height(5.dp),
                value = keyInterval.toFloat(),
                valueRange = 1f..24f,
                steps = 24,
                onValueChange = { keyInterval = it.toInt()}
            )
            Slider(
                modifier = Modifier.height(5.dp),
                value = octave.toFloat(),
                valueRange = 1f..8f,
                steps = 8,
                onValueChange = { octave = it.toInt()}
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp)
                    .onGloballyPositioned { layoutCoordinates ->
                        componentSize = layoutCoordinates.size
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            touchPosition = offset // 获取 x, y 位置
                        }
                    }
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                val normalizedX =
                                    if (componentSize.width > 0) event.x / componentSize.width else 0f
                                val normalizedY =
                                    if (componentSize.height > 0) event.y / componentSize.height else 0f
                                val num = (normalizedX * keyInterval).toInt()

                                key = CountKeyAndVel(num, midiNotes)
                                vel = (baseVel + normalizedY * 117).toInt()

                                FluidSynthManager.playNote(key, vel, channel)
                                true
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val normalizedX =
                                    if (componentSize.width > 0) event.x / componentSize.width else 0f
                                val normalizedY =
                                    if (componentSize.height > 0) event.y / componentSize.height else 0f
                                val num = (normalizedX * keyInterval).toInt()

                                temp_key = CountKeyAndVel(num, midiNotes)
                                temp_vel = (baseVel + normalizedY * 87).toInt()
                                if (temp_vel != vel || key != temp_key) { //过滤抖动减少颤音
                                    FluidSynthManager.stopNoteDelay(key, channel)
                                    if (temp_vel - vel > 5 || temp_vel - vel < -5 || key != temp_key) {
                                        key = temp_key
                                        vel = temp_vel
                                        FluidSynthManager.playNote(key, vel, channel)
                                    }
                                }
                                true
                            }

                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                FluidSynthManager.stopNote(key, channel)
                                true
                            }

                            else -> false
                        }
                    },
            ) {
                KeyboardsItem(count = keyInterval)
            }
        }
    }
}

@Composable
fun KeyboardsItem(modifier: Modifier = Modifier, count: Int) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ){
        repeat(count){
            KeyboardsBubble(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun KeyboardsBubble(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 0.dp, horizontal = 2.dp)
        ,
        color = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(10.dp),
    ){
        Button(
            onClick = {}
        ){

        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ListNumberPicker(
    data: List<T>,
    selectIndex: Int,
    visibleCount: Int,
    modifier: Modifier = Modifier,
    onSelect: (index: Int, item: T) -> Unit,
    content: @Composable (item: T) -> Unit,
) {
    BoxWithConstraints(modifier = modifier, propagateMinConstraints = true) {
        val pickerHeight = maxHeight
        val size = data.size
        val itemHeight = pickerHeight / visibleCount
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = selectIndex
        )
        val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
        LazyColumn(
            modifier = Modifier,
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
        ) {
            //占据相应的高度比如显示5个那么中间那个是选中的其他的就是非选中，但也要占据一定的空间。
            for (i in 1..visibleCount / 2) {
                item {
                    Surface(modifier = Modifier.height(itemHeight)) {}
                }
            }
            items(size) { index ->
                //预防滑动的时候出现数组越界
                if (firstVisibleItemIndex >= size) {
                    onSelect(size - 1, data[size - 1])
                } else {
                    onSelect(firstVisibleItemIndex, data[firstVisibleItemIndex])
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    content(data[index])
                }
            }
            for (i in 1..visibleCount / 2) {
                item {
                    Surface(modifier = Modifier.height(itemHeight)) {}
                }
            }
        }

    }
}


@Preview
@Composable
private fun BasicMusicInfoSetPrev() {
    BasicMusicInfoSet()
}



@Preview
@Composable
private fun KeyboardsItemPrev() {
    Keyboards()
}

@Preview
@Composable
private fun KeyboardsPrev() {
    KeyboardsItem(count = 16)
}

