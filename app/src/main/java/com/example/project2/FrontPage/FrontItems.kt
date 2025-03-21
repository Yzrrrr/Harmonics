package com.example.project2.FrontPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.R

@Composable
fun ModelContainer(modifier: Modifier = Modifier, setter: ClassContainerSetter, onClick: () -> Unit = {}) {
    Surface(
         modifier=modifier
             .heightIn(min = 0.dp, max = 250.dp)
             .clickable(onClick = onClick)
             .shadow(elevation = 10.dp)
             .padding(horizontal = 5.dp),
         shape = RoundedCornerShape(16.dp),
        color = setter.color
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = setter.text
            )
            Icon(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                imageVector = setter.icon,
                contentDescription = null
            )
        }
    }
}

data class ClassContainerSetter(
    val text: String,
    val icon: ImageVector,
    val color: Color
)

@Preview
@Composable
private fun ModelContainerPrev() {
    val setter = ClassContainerSetter(
        text = stringResource(R.string.get_start),
        icon = Icons.Filled.Face,
        color = MaterialTheme.colorScheme.primary
    )
    ModelContainer(setter = setter, onClick = {})
}