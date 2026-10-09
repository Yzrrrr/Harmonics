package com.example.project2.FrontPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(modifier = modifier.fillMaxSize().background(Paper)) {
        ChromeRow(
            left = { Text("yzr", style = com.example.project2.ui.theme.ChromeStyle, color = Ink) },
            center = { Label("index") },
            right = { Small("v 1.0") },
        )
        Spacer(Modifier.weight(0.9f))
        Column(Modifier.padding(horizontal = ChromeInset)) {
            Label("sound")
            Spacer(Modifier.height(18.dp))
            Text("harmonics", style = TitleStyle, color = Ink)
            Spacer(Modifier.height(44.dp))
            Hairline()
            IndexRow("01", stringResource(R.string.front_synth), onClickJumpToSynth)
            IndexRow("02", stringResource(R.string.front_generate), onClickJumpToMusicGen)
            IndexRow("03", stringResource(R.string.front_assistant), onClickJumpToAssistant)
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
