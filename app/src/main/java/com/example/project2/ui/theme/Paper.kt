package com.example.project2.ui.theme

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/** 框距：所有屏的边距都是它 */
val ChromeInset = 24.dp

/** 括号小标签 */
@Composable
fun Label(text: String, modifier: Modifier = Modifier, color: Color = Ink50) {
    Text(text = "( $text )", style = LabelStyle, color = color, modifier = modifier, maxLines = 1)
}

/** 不带括号的小字：编号、数值旁注 */
@Composable
fun Small(text: String, modifier: Modifier = Modifier, color: Color = Ink50) {
    Text(text = text, style = LabelStyle, color = color, modifier = modifier, maxLines = 1)
}

/** 发丝线 */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Hair) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/**
 * 可点的括号标签。选中态：字转实墨，底下一条墨线；
 * 不选中：淡墨，没有线。没有底色、没有圆角、没有波纹。
 */
@Composable
fun LabelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    enabled: Boolean = true,
) {
    val color by animateColorAsState(if (active) Ink else Ink50, label = "label")
    val line by animateColorAsState(if (active) Ink else Color.Transparent, label = "line")
    Column(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Label(text, color = color)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(18.dp).height(1.dp).background(line))
    }
}

/** 屏顶的框：左、中、右三格 */
@Composable
fun ChromeRow(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    center: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = ChromeInset, vertical = ChromeInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { left() }
        if (center != null) Box(contentAlignment = Alignment.Center) { center() }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { right() }
    }
}

/**
 * 选项单：从底下推上来的一页纸，一行一个值、行间发丝线。
 * 当前值实墨，其余淡墨。点一行即选中并收回。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PickerSheet(
    title: String,
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    label: (T) -> String = { it.toString() },
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val index = options.indexOf(selected)
        if (index > 2) listState.scrollToItem(index - 2)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = Paper,
        contentColor = Ink,
        shape = RectangleShape,
        dragHandle = null,
        scrimColor = InkDeep.copy(alpha = 0.35f),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = ChromeInset)) {
            Row(
                Modifier.fillMaxWidth().padding(top = ChromeInset, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Label(title)
                Text("( close )", style = ChromeStyle, color = Ink, modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss,
                ))
            }
            Hairline()
            LazyColumn(state = listState, modifier = Modifier.height(360.dp)) {
                items(options) { option ->
                    val active = option == selected
                    Column(Modifier.fillMaxWidth().clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null,
                    ) { onSelect(option) }) {
                        Text(
                            text = label(option),
                            style = HeadingStyle,
                            color = if (active) Ink else Ink35,
                            modifier = Modifier.padding(vertical = 14.dp),
                        )
                        Hairline()
                    }
                }
            }
            Spacer(Modifier.height(ChromeInset))
        }
    }
}

/**
 * 对话框：一张方纸，没有圆角和阴影，标题是括号标签，动作是两枚括号标签。
 */
@Composable
fun PaperDialog(
    title: String,
    onDismiss: () -> Unit,
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().background(Paper).padding(ChromeInset)) {
            Label(title)
            Spacer(Modifier.height(14.dp))
            Hairline()
            Spacer(Modifier.height(18.dp))
            content()
            Spacer(Modifier.height(18.dp))
            Hairline()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) { actions() }
        }
    }
}

/**
 * 纸的颗粒。和 yizeren.com 同一层：一张 160×160 的噪点图平铺在最上面，正片叠底，不吃触摸。
 */
@Composable
fun PaperGrain(modifier: Modifier = Modifier) {
    val noise = remember {
        val size = 160
        val pixels = IntArray(size * size)
        val random = java.util.Random(7)
        for (i in pixels.indices) {
            val v = 200 + random.nextInt(56)
            pixels[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888).asImageBitmap()
    }
    Box(
        modifier.drawWithCache {
            val brush = ShaderBrush(ImageShader(noise, TileMode.Repeated, TileMode.Repeated))
            onDrawBehind { drawRect(brush, alpha = 0.07f, blendMode = BlendMode.Multiply) }
        },
    )
}

/** 标签 + 大值的一组：( bpm ) / 120 */
@Composable
fun ValueField(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick,
            ) else Modifier,
        ),
    ) {
        Label(label)
        Spacer(Modifier.height(10.dp))
        Text(value, style = ValueStyle, color = Ink, maxLines = 1)
    }
}

