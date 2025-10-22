package com.example.w05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.w05.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val count = remember { mutableStateOf(0) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE3F2FD), Color(0xFFF1F8E9))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "카운터 & 타이머",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        CounterApp(count)
                        Spacer(modifier = Modifier.height(40.dp))
                        StopWatchApp()
                    }
                }
            }
        }
    }
}

@Composable
fun CounterApp(count: MutableState<Int>) {
    // 숫자 변화 시 살짝 커졌다 작아지는 애니메이션
    val scale by animateFloatAsState(
        targetValue = if (count.value != 0) 1.1f else 1f,
        label = "countScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Count: ${count.value}",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            modifier = Modifier.scale(scale)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { count.value++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
            ) { Text("＋") }

            Button(
                onClick = { if (count.value > 0) count.value-- },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
            ) { Text("－") }

            Button(
                onClick = { count.value = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
            ) { Text("초기화") }
        }
    }
}

@Composable
fun StopWatchApp() {
    var timeInMillis by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                delay(10L)
                timeInMillis += 10L
            }
        }
    }

    val displayColor =
        if (isRunning) Color(0xFF43A047) else Color(0xFF455A64)

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(timeInMillis),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = displayColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { isRunning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("시작") }
                Button(
                    onClick = { isRunning = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("정지") }
                Button(
                    onClick = {
                        isRunning = false
                        timeInMillis = 0L
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
                ) { Text("초기화") }
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val minutes = (timeInMillis / 1000) / 60
    val seconds = (timeInMillis / 1000) % 60
    val millis = (timeInMillis % 1000) / 10
    return String.format("%02d:%02d:%02d", minutes, seconds, millis)
}
