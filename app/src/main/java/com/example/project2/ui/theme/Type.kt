package com.example.project2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.project2.R

/** 拉丁用 Space Grotesk；中文落到系统字体（Android 上没有苹方，让它自然回退）。 */
val Grotesk = FontFamily(
    Font(R.font.space_grotesk_light, FontWeight.Light),
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
)

private val tight = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)

/** 括号小标签：全站唯一的小字 */
val LabelStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Normal,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.16.em,
    lineHeightStyle = tight,
)

/** 框上的字：顶栏、关闭键 */
val ChromeStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Medium,
    fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp, lineHeightStyle = tight,
)

/** 值：BPM、根音、和弦名 */
val ValueStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Medium,
    fontSize = 30.sp, lineHeight = 32.sp, letterSpacing = (-0.02).em, lineHeightStyle = tight,
)

/** 大标题：首页的 harmonics */
val TitleStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Medium,
    fontSize = 56.sp, lineHeight = 52.sp, letterSpacing = (-0.035).em, lineHeightStyle = tight,
)

val HeadingStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Medium,
    fontSize = 26.sp, lineHeight = 28.sp, letterSpacing = (-0.03).em, lineHeightStyle = tight,
)

val BodyStyle = TextStyle(
    fontFamily = Grotesk, fontWeight = FontWeight.Light,
    fontSize = 15.sp, lineHeight = 26.sp, letterSpacing = 0.01.em,
)

val Typography = Typography(
    displayLarge = TitleStyle,
    headlineMedium = HeadingStyle,
    titleMedium = ValueStyle,
    bodyLarge = BodyStyle,
    bodyMedium = BodyStyle,
    labelSmall = LabelStyle,
    labelLarge = ChromeStyle,
)
