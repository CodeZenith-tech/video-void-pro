package com.novawolf.videovoid

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ffmpegkit.ytdlp.YtDlp
import dev.ffmpegkit.ytdlp.YtDlpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { YtDlp.init(applicationContext) } catch (_: Exception) {}
        setContent { VideoVoidScreen(this) }
    }
}

private fun sourceName(url: String): String = when {
    url.contains("instagram.com", true) -> "Instagram"
    url.contains("youtube.com", true) || url.contains("youtu.be", true) -> "YouTube"
    url.contains("facebook.com", true) || url.contains("fb.watch", true) -> "Facebook"
    url.contains("tiktok.com", true) -> "TikTok"
    url.contains("vimeo.com", true) -> "Vimeo"
    url.contains("x.com", true) || url.contains("twitter.com", true) -> "X"
    url.contains("reddit.com", true) -> "Reddit"
    else -> "Supported source"
}

private fun outputDir(context: Context): File {
    val base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    return File(base, "VideoVoid").apply { mkdirs() }
}

@Composable
fun VideoVoidScreen(activity: Activity) {
    var url by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf("Best") }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    var tab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip?.let { if (it.itemCount > 0) url = it.getItemAt(0).coerceToText(activity).toString() }
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF050507), surface = Color(0xFF101014))) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2B203B), Color(0xFF09090D), Color.Black)))) {
            Column(Modifier.fillMaxSize().padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("VIDEOVOID", fontSize = 23.sp, fontWeight = FontWeight.Black); Text("Public video downloader", fontSize = 12.sp, color = Color.LightGray) }
                    Icon(Icons.Default.Settings, null, tint = Color.LightGray)
                }
                Spacer(Modifier.height(22.dp))
                if (tab == 0) {
                    OutlinedTextField(
                        value = url, onValueChange = { url = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Paste video link") }, singleLine = true, shape = RoundedCornerShape(18.dp),
                        trailingIcon = { IconButton(onClick = {
                            val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.primaryClip?.let { if (it.itemCount > 0) url = it.getItemAt(0).coerceToText(activity).toString() }
                        }) { Icon(Icons.Default.ContentPaste, null) } }
                    )
                    if (url.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(sourceName(url), fontWeight = FontWeight.Bold, color = Color(0xFFE8DDF5))
                    }
                    Spacer(Modifier.weight(1f))
                    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(0.96f, 1.05f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "pulse")
                    Box(Modifier.size(205.dp).scale(if (downloading) pulse else 1f).background(Brush.radialGradient(listOf(Color(0xFF3B2C4D), Color(0xFF0C0A10), Color.Transparent)), CircleShape), contentAlignment = Alignment.Center) {
                        Button(onClick = {
                            if (downloading) return@Button
                            if (!url.startsWith("http")) { Toast.makeText(activity, "Paste a valid URL", Toast.LENGTH_SHORT).show(); return@Button }
                            downloading = true; progress = 0f; status = "Starting…"
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val dir = outputDir(activity)
                                    val format = when (quality) {
                                        "2160p" -> "bv*[height<=2160]+ba/b[height<=2160]"
                                        "1440p" -> "bv*[height<=1440]+ba/b[height<=1440]"
                                        "1080p" -> "bv*[height<=1080]+ba/b[height<=1080]"
                                        "720p" -> "bv*[height<=720]+ba/b[height<=720]"
                                        "480p" -> "bv*[height<=480]+ba/b[height<=480]"
                                        "Audio only" -> "ba/b"
                                        else -> "bv*+ba/b"
                                    }
                                    val request = YtDlpRequest(url)
                                        .setOutputTemplate(File(dir, "%(title).180B.%(ext)s").absolutePath)
                                        .addOption("-f", format)
                                        .addOption("--no-playlist")
                                        .addOption("--merge-output-format", "mp4")
                                        .addOption("--newline")
                                    if (quality == "Audio only") request.addOption("-x").addOption("--audio-format", "mp3")
                                    YtDlp.executeAsync(request) { p, eta, _ ->
                                        progress = (p.coerceIn(0f, 100f) / 100f)
                                        status = if (p >= 100f) "Finalizing…" else "Downloading ${p.toInt()}% • ETA ${eta}s"
                                    }
                                    withContext(Dispatchers.Main) { status = "Download started" }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) { status = "Failed: ${e.message ?: "Unknown error"}" }
                                } finally {
                                    withContext(Dispatchers.Main) { downloading = false }
                                }
                            }
                        }, modifier = Modifier.size(132.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17131F))) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("♥", fontSize = 42.sp); Text(if (downloading) "DOWNLOADING" else "DOWNLOAD", fontSize = 10.sp) }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), trackColor = Color.White.copy(alpha = .08f))
                    Spacer(Modifier.height(7.dp)); Text(status, fontSize = 12.sp, color = Color.LightGray)
                    Spacer(Modifier.height(14.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box { OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Quality: $quality") }; DropdownMenu(expanded, { expanded = false }) { listOf("Best","2160p","1440p","1080p","720p","480p","Audio only").forEach { DropdownMenuItem({ Text(it) }, { quality = it; expanded = false }) } } }
                } else {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) { Text("Download History", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Completed files are saved in Downloads/VideoVoid.", color = Color.LightGray) }
                }
                Spacer(Modifier.height(16.dp))
                NavigationBar(containerColor = Color.Black.copy(alpha = .4f)) {
                    NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("Download") })
                    NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.History, null) }, label = { Text("History") })
                }
            }
        }
    }
}
