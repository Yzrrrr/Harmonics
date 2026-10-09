package com.example.project2.FrontPage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.R
import com.example.project2.ui.theme.ChromeInset
import com.example.project2.ui.theme.ChromeRow
import com.example.project2.ui.theme.Hairline
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Label
import com.example.project2.ui.theme.Paper
import com.example.project2.ui.theme.Project2Theme
import com.example.project2.ui.theme.Small
import com.example.project2.ui.theme.TitleStyle

/**
 * 首页：一页目录。
 * 顶部是框（署名、索引标签），中间是名字，下面三行入口，各带编号和发丝线。
 * 没有卡片、没有图标、没有"试着问问"。
 */
@Composable
fun FrontScreen(
    modifier: Modifier = Modifier,
    onClickJumpToAssistant: () -> Unit = {},
    onClickJumpToSynth: () -> Unit = {},
    onClickJumpToMusicGen: () -> Unit = {},
) {
    // 开场：名字和三行目录依次从下面升起来，和站的 RevealLine 同一种呼吸
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    @Composable fun Modifier.rise(order: Int): Modifier {
        val t by animateFloatAsState(if (shown) 1f else 0f, tween(700, delayMillis = 120 + order * 90, easing = FastOutSlowInEasing), label = "rise")
        return graphicsLayer { alpha = t; translationY = (1f - t) * 28f * density }
    }

    Column(modifier = modifier.fillMaxSize().background(Paper)) {
        ChromeRow(
            left = { Text("yzr", style = com.example.project2.ui.theme.ChromeStyle, color = Ink) },
            center = { Label("index") },
            right = { Small("v 1.0") },
        )
        Spacer(Modifier.weight(0.9f))
        Column(Modifier.padding(horizontal = ChromeInset)) {
            Label("sound", Modifier.rise(0))
            Spacer(Modifier.height(18.dp))
            Text("harmonics", style = TitleStyle, color = Ink, modifier = Modifier.rise(1))
            Spacer(Modifier.height(44.dp))
            Hairline(Modifier.rise(2))
            IndexRow("01", stringResource(R.string.front_synth), onClickJumpToSynth, Modifier.rise(3))
            IndexRow("02", stringResource(R.string.front_generate), onClickJumpToMusicGen, Modifier.rise(4))
            IndexRow("03", stringResource(R.string.front_assistant), onClickJumpToAssistant, Modifier.rise(5))
        }
        Spacer(Modifier.weight(1.1f))
        ChromeRow(left = { Label("storyware") }, right = { Small("01 / 04") })
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FrontScreenPrev() {
    Project2Theme { FrontScreen() }
}
