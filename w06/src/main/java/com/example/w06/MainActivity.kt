package com.example.w06

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.w06.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BubbleGameScreen()
                }
            }
        }
    }
}

enum class BubbleType { NORMAL, BONUS, BOMB }
enum class GamePhase { START, PLAYING, GAMEOVER }

data class Bubble(
    val id: Int,
    var position: Offset,
    val radius: Float,
    val color: Color,
    val type: BubbleType = BubbleType.NORMAL,
    val creationTime: Long = System.currentTimeMillis(),
    val velocityX: Float = 0f,
    val velocityY: Float = 0f
)

data class ExplosionParticle(
    val id: Int = Random.nextInt(),
    var position: Offset,
    var velocity: Offset,
    var radius: Float,
    var color: Color,
    var alpha: Float = 1f,
    val creationTime: Long = System.currentTimeMillis()
)

class GameState(
    initialBubbles: List<Bubble> = emptyList(),
    initialParticles: List<ExplosionParticle> = emptyList()
) {
    var bubbles by mutableStateOf(initialBubbles)
    var particles by mutableStateOf(initialParticles)
    var score by mutableStateOf(0)
    var timeLeft by mutableStateOf(60)
    var comboCount by mutableStateOf(0)
    var lastClickTime by mutableStateOf(0L)
    var phase by mutableStateOf(GamePhase.START)
}

@Composable
fun GameStatusRow(score: Int, timeLeft: Int, combo: Int) {
    val timeColor = if (timeLeft <= 10) Color(0xFFE53935) else Color.Black
    val comboColor = if (combo >= 2) Color(0xFF43A047) else Color.DarkGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Score: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            if (combo >= 2)
                Text(text = "Combo x${combo + 1}", color = comboColor, fontSize = 18.sp)
        }
        Text(text = "Time: ${timeLeft}s", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = timeColor)
    }
}

fun makeNewBubble(maxWidth: Dp, maxHeight: Dp): Bubble {
    val typeRoll = Random.nextFloat()
    val type = when {
        typeRoll < 0.05f -> BubbleType.BONUS
        typeRoll < 0.10f -> BubbleType.BOMB
        else -> BubbleType.NORMAL
    }

    val color = when (type) {
        BubbleType.BONUS -> Color(0xFFFFEB3B)
        BubbleType.BOMB -> Color(0xFF212121)
        else -> Color(
            red = Random.nextInt(100, 255),
            green = Random.nextInt(100, 255),
            blue = Random.nextInt(100, 255),
            alpha = 200
        )
    }

    return Bubble(
        id = Random.nextInt(),
        position = Offset(
            x = Random.nextFloat() * maxWidth.value,
            y = Random.nextFloat() * maxHeight.value
        ),
        radius = Random.nextFloat() * 40 + 40,
        velocityX = Random.nextFloat() * 3 - 1.5f,
        velocityY = Random.nextFloat() * 3 - 1.5f,
        color = color,
        type = type
    )
}

@Composable
fun BubbleComposable(bubble: Bubble, onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (clicked) 1.8f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scaleAnim"
    )

    val alpha by animateFloatAsState(
        targetValue = if (clicked) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "alphaAnim"
    )

    Canvas(
        modifier = Modifier
            .size((bubble.radius * 2).dp)
            .offset(x = bubble.position.x.dp, y = bubble.position.y.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                clicked = true
                onClick()
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
    ) {
        drawCircle(
            color = bubble.color,
            radius = size.width / 2,
            center = center
        )
    }
}

fun updateBubblePositions(
    bubbles: List<Bubble>,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    density: Density
): List<Bubble> {
    return bubbles.map { bubble ->
        with(density) {
            val radiusPx = bubble.radius.dp.toPx()
            var xPx = bubble.position.x.dp.toPx()
            var yPx = bubble.position.y.dp.toPx()
            var vx = bubble.velocityX
            var vy = bubble.velocityY

            vy += 0.05f
            vx += (Random.nextFloat() - 0.5f) * 0.1f
            vy += (Random.nextFloat() - 0.5f) * 0.1f

            xPx += vx.dp.toPx()
            yPx += vy.dp.toPx()

            if (xPx < radiusPx || xPx > canvasWidthPx - radiusPx) {
                vx *= -1 * (0.9f + Random.nextFloat() * 0.2f)
            }
            if (yPx < radiusPx || yPx > canvasHeightPx - radiusPx) {
                vy *= -1 * (0.9f + Random.nextFloat() * 0.2f)
            }

            xPx = xPx.coerceIn(radiusPx, canvasWidthPx - radiusPx)
            yPx = yPx.coerceIn(radiusPx, canvasHeightPx - radiusPx)

            bubble.copy(
                position = Offset(xPx.toDp().value, yPx.toDp().value),
                velocityX = vx,
                velocityY = vy
            )
        }
    }
}

fun updateExplosionParticles(
    particles: List<ExplosionParticle>,
    density: Density
): List<ExplosionParticle> {
    val now = System.currentTimeMillis()
    return particles.mapNotNull { p ->
        val dt = (now - p.creationTime).coerceAtLeast(1)
        val newAlpha = 1f - (dt / 400f)
        if (newAlpha <= 0f) return@mapNotNull null

        with(density) {
            val newPos = Offset(
                x = p.position.x + p.velocity.x,
                y = p.position.y + p.velocity.y
            )
            val newVel = Offset(
                x = p.velocity.x * 0.95f,
                y = (p.velocity.y + 0.05f) * 0.95f
            )
            p.copy(
                position = newPos,
                velocity = newVel,
                radius = p.radius * 0.95f,
                alpha = newAlpha
            )
        }
    }
}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BubbleGameScreen() {
    val gameState = remember { GameState() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFE8F5E9))
                )
            )
    ) {
        val density = LocalDensity.current
        val canvasWidthPx = with(density) { maxWidth.toPx() }
        val canvasHeightPx = with(density) { maxHeight.toPx() }

        // ✅ 1. 게임 시작 전 대기 화면
        if (gameState.phase == GamePhase.START) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // 시작 시 초기화 후 PLAYING 상태로 전환
                        gameState.score = 0
                        gameState.timeLeft = 60
                        gameState.comboCount = 0
                        gameState.bubbles = emptyList()
                        gameState.particles = emptyList()
                        gameState.phase = GamePhase.PLAYING
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TAP TO START",
                    color = Color.DarkGray,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // ✅ 2. 실제 게임 플레이 화면
        else {
            // 타이머 (GamePhase.PLAYING일 때만 작동)
            LaunchedEffect(gameState.phase) {
                if (gameState.phase == GamePhase.PLAYING) {
                    while (true) {
                        delay(1000L)
                        gameState.timeLeft--
                        if (gameState.timeLeft <= 0) {
                            gameState.phase = GamePhase.GAMEOVER
                            break
                        }
                        val now = System.currentTimeMillis()
                        gameState.bubbles = gameState.bubbles.filter { now - it.creationTime < 4000 }
                    }
                }
            }

            // 버블 이동 + 입자 이동
            LaunchedEffect(gameState.phase) {
                if (gameState.phase == GamePhase.PLAYING) {
                    while (true) {
                        delay(16)
                        if (gameState.bubbles.isEmpty()) {
                            gameState.bubbles = List(3) { makeNewBubble(maxWidth, maxHeight) }
                        }
                        if (Random.nextFloat() < 0.05f && gameState.bubbles.size < 20) {
                            val newBubble = makeNewBubble(maxWidth, maxHeight)
                            gameState.bubbles = gameState.bubbles + newBubble
                        }
                        gameState.bubbles = updateBubblePositions(
                            gameState.bubbles,
                            canvasWidthPx,
                            canvasHeightPx,
                            density
                        )
                        gameState.particles = updateExplosionParticles(gameState.particles, density)
                    }
                }
            }

            // UI 구성
            Column(modifier = Modifier.fillMaxSize()) {
                if (gameState.phase == GamePhase.PLAYING)
                    GameStatusRow(score = gameState.score, timeLeft = gameState.timeLeft, combo = gameState.comboCount)

                Box(modifier = Modifier.fillMaxSize()) {
                    // ✅ 버블 표시 및 클릭 처리
                    if (gameState.phase == GamePhase.PLAYING) {
                        gameState.bubbles.forEach { bubble ->
                            BubbleComposable(bubble = bubble) {
                                val now = System.currentTimeMillis()

                                // 콤보 계산
                                if (now - gameState.lastClickTime < 1000L) {
                                    gameState.comboCount++
                                } else {
                                    gameState.comboCount = 0
                                }
                                gameState.lastClickTime = now

                                // 점수 계산
                                val baseScore = (100 - bubble.radius).toInt().coerceAtLeast(10) / 10
                                when (bubble.type) {
                                    BubbleType.BONUS -> gameState.score += 5
                                    BubbleType.BOMB -> gameState.score -= 3
                                    else -> gameState.score += baseScore + gameState.comboCount
                                }

                                // 폭발 이펙트 생성
                                val explosion = List(10) {
                                    val angle = Random.nextFloat() * 360f
                                    val speed = Random.nextFloat() * 2 + 1
                                    ExplosionParticle(
                                        position = bubble.position,
                                        velocity = Offset(
                                            cos(angle) * speed,
                                            sin(angle) * speed
                                        ),
                                        radius = bubble.radius / 4,
                                        color = bubble.color
                                    )
                                }
                                gameState.particles += explosion

                                // 버블 제거
                                gameState.bubbles = gameState.bubbles.filterNot { it.id == bubble.id }
                            }
                        }
                    }

                    // 폭발 입자 그리기
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        gameState.particles.forEach { p ->
                            drawCircle(
                                color = p.color.copy(alpha = p.alpha),
                                radius = p.radius,
                                center = Offset(p.position.x.dp.toPx(), p.position.y.dp.toPx())
                            )
                        }
                    }

                    // ✅ 3. 게임 오버 화면
                    if (gameState.phase == GamePhase.GAMEOVER) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xAA000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GAME OVER",
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Final Score: ${gameState.score}",
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(onClick = {
                                    gameState.phase = GamePhase.START
                                }) {
                                    Text("Restart")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BubbleGamePreview() {
    MyApplicationTheme {
        BubbleGameScreen()
    }
}
