package com.example.project2.SynthPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2.FluidSynthManager


@Composable
fun DrumEachClapItem(
    modifier: Modifier = Modifier,
    clapNum: Int,
    note: Int,
    svel :Int
) {
    val timeNum = clapNum*4
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 1, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 1, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 2, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 2, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
        ToggleButtonWithColor(
            onStart = {
                FluidSynthManager.setDrumNote(timeNum = timeNum + 3, note = note, svel = svel)
            },
            onStop = { FluidSynthManager.delDrumNote(timeNum = timeNum + 3, note = note) },
            textStart = timeNum.toString(),
            textend = ""
        )
    }
}

@Composable
fun DrumEachDrumSetItem(modifier: Modifier = Modifier,  clapList : List<Int>, note : Int, svel :Int, Drums:String) {

    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Text(
            modifier = Modifier.width(40.dp),
            text = Drums
        )
        Spacer(Modifier.padding(8.dp))
        clapList.forEach { clapNum ->
            DrumEachClapItem(
                clapNum = clapNum,
                note = note,
                svel = svel
            )
        }
    }
}

@Composable
fun DrumSet(modifier: Modifier = Modifier) {
    Card (
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ){
        Column(
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 35,
                svel = 100,
                Drums = "Boom"
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 38,
                svel = 100,
                Drums = "Clap"
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 45,
                svel = 100,
                Drums = "Tom"
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 51,
                svel = 100,
                Drums = "Crash"
            )
            Spacer(Modifier.padding(2.dp))
            DrumEachDrumSetItem(
                modifier = Modifier,
                clapList = listOf(0, 1, 2, 3),
                note = 42,
                svel = 100,
                Drums = "Hats"
            )

        }
    }
}

@Preview
@Composable
private fun DrumPrev(modifier: Modifier = Modifier.fillMaxSize()) {
    Surface(modifier= Modifier.background(color = Color.Gray),
        color = MaterialTheme.colorScheme.primary,
        ) {
        DrumSet(modifier = Modifier.height(250.dp))
    }
}

@Composable
fun ToggleButtonWithColor(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onStop: () -> Unit,
    textStart: String,
    textend: String,
) {
    var isTriggered by remember { mutableStateOf(false) }

    Button(
        shape = RectangleShape,
        onClick = {
            isTriggered = !isTriggered
            if (isTriggered) {
                onStart()
            } else {
                onStop()
            }
        },
        modifier = modifier
            .size(width = 18.dp, height = 40.dp)
            .padding(1.dp)
        ,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTriggered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = if (isTriggered) textStart else textend)
    }
}