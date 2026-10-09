package com.example.project2.FrontPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2.ui.theme.Hairline
import com.example.project2.ui.theme.HeadingStyle
import com.example.project2.ui.theme.Ink
import com.example.project2.ui.theme.Small

/** 目录的一行：编号、名字、发丝线。和站的菜单同一副骨架。 */
@Composable
fun IndexRow(number: String, name: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Small(number, modifier = Modifier.width(36.dp))
            Spacer(Modifier.width(8.dp))
            Text(name, style = HeadingStyle, color = Ink)
        }
        Hairline()
    }
}
