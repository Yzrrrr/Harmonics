package com.example.project2.MusicGenPage

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class MusicGenViewModel : ViewModel() {

    private val _generatedMusicUri = MutableStateFlow<Uri?>(null)
    val generatedMusicUri: StateFlow<Uri?> = _generatedMusicUri

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.SECONDS)
        .readTimeout(5000, TimeUnit.SECONDS)
        .writeTimeout(3000, TimeUnit.SECONDS)
        .build()

    fun uploadMusicAndGenerate(context: Context, musicUri: Uri?, prompt: String) {
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = "http://207.148.111.64:12525/funk_generate"  // Android 访问本机的方式
                _generatedMusicUri.value = null
                //val jsonPrompt = """{"prompt": "$prompt"}"""
                //val jsonRequestBody = jsonPrompt.toRequestBody("application/json".toMediaType())
                val requestBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("prompt", prompt)  // 添加 prompt

                // **如果用户上传了音频**
                musicUri?.let {
                    val file = uriToFile(context, it)
                    run {
                        val requestBody = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                        requestBuilder.addFormDataPart("audio", file.name, requestBody)
                    }
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestBuilder.build())
                    .build()



                val response = client.newCall(request).execute()

                Log.d("MusicGenViewModel", "Response received, code: ${response.code}")


                if (response.body == null) {
                    Log.e("MusicGenViewModel", "Received empty response body from server")
                    return@launch
                }

                if (response.isSuccessful) {
                    Log.d("MusicGenViewModel", "音频数据成功返回，正在保存...")
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Audio.Media.DISPLAY_NAME, "generated_music.wav")
                        put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                        put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

                    uri?.let { generatedUri ->
                        resolver.openOutputStream(generatedUri)?.use { outputStream ->
                            response.body?.byteStream()?.copyTo(outputStream)
                        }
                        _generatedMusicUri.value = generatedUri
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val file = File(context.cacheDir, "input_audio.wav")
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}

private const val s = "Describe your music"

@Composable
fun MusicGenerationScreen(context: Context) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var musicUri by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var generatedMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var generatedIsPlaying by remember { mutableStateOf(false) }
    val viewModel: MusicGenViewModel = viewModel()

    var progress by remember { mutableStateOf(0.0f) }
    var generatedProgress by remember { mutableStateOf(0.0f) }

    val musicPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            musicUri = it
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, it)
                prepare()
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                progress = player.currentPosition / player.duration.toFloat()
            }
            delay(500) // 每 500ms 更新一次进度
        }
    }

    LaunchedEffect(generatedIsPlaying) {
        while (generatedIsPlaying) {
            generatedMediaPlayer?.let { player ->
                generatedProgress = player.currentPosition / player.duration.toFloat()
            }
            delay(500) // 每 500ms 更新一次进度
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 输入区域标题
        Text(
            text = stringResource(R.string.input),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 导入音乐按钮
        Button(
            onClick = { musicPickerLauncher.launch("audio/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text(text = stringResource(R.string.import_your_raw_music), color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.Folder, contentDescription = "Import", tint = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 播放音乐的进度条
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f),
                color = Color.Black,
            )
            IconButton(onClick = {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                    }
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 文本输入框
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text(stringResource(R.string.describe_your_music)) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 提交按钮
        Button(
            onClick = {
                if (musicUri != null || textInput.isNotEmpty()) {
                    viewModel.uploadMusicAndGenerate(context, musicUri, textInput)
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.please_select_a_music_file_or_make_text_input_first), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = stringResource(R.string.submit_and_generate), color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 进度条
        IndeterminateIndicator(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // **模型生成的音频文件展示**
        if (viewModel.generatedMusicUri.collectAsState().value != null) {
            val generatedUri = viewModel.generatedMusicUri.collectAsState().value
            if (generatedMediaPlayer == null && generatedUri != null) {
                generatedMediaPlayer = MediaPlayer().apply {
                    setDataSource(context, generatedUri)
                    prepare()
                }
            }

            Text(
                text = stringResource(R.string.generated_music_output),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { generatedProgress },
                    modifier = Modifier.weight(1f),
                    color = Color.Blue,
                )
                IconButton(onClick = {
                    generatedMediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            generatedIsPlaying = false
                        } else {
                            player.start()
                            generatedIsPlaying = true
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (generatedIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }
    }
}

@Composable
fun IndeterminateIndicator(viewModel: MusicGenViewModel) {
    if (!viewModel.isGenerating.collectAsState().value) return

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LinearProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = Color.Blue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("Generating...", fontSize = 14.sp, fontStyle = FontStyle.Italic)
    }
}

@Preview
@Composable
private fun MusicGenerationScreenPrev() {
    val context = LocalContext.current
    MusicGenerationScreen(context)
}