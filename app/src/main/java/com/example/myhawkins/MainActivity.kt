package com.example.myhawkins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hawkins()
        }
    }
}

@Composable
fun Hawkins() {
    val coroutineScope = rememberCoroutineScope()
    var gameStarted by remember { mutableStateOf(false) }
    var vecnaScore by remember { mutableStateOf(5f) }
    val visited = remember { mutableStateListOf<Pair<Int, Int>>() }
    var gameOver by remember { mutableStateOf(false) }
    var gameWon by remember { mutableStateOf(false) }

    var score by remember { mutableStateOf(150f) }
    val choices = listOf("Real world", "Upside world")

    var hasPowerUp by remember { mutableStateOf(false) }
    var activePowerUpName by remember { mutableStateOf("None") }

    var triggerDamageFlash by remember { mutableStateOf(false) }
    val shakeAnim = remember { Animatable(0f) }

    var exitRow by remember { mutableStateOf(Random.nextInt(0, 4)) }
    var exitColumn by remember { mutableStateOf(Random.nextInt(1, 5)) }

    var dice by remember { mutableStateOf(1) }
    var isDiceRolling by remember { mutableStateOf(false) }

    val grid = remember { List(5) { List(5) { Random.nextInt(1, 7) } } }
    val gridChoice = remember { List(5) { List(5) { choices.random() } } }

    val gridElements = remember {
        List(5) { r ->
            List(5) { c ->
                val rand = Random.nextFloat()
                when {
                    (r == 4 && c == 0) || (r == exitRow && c == exitColumn) -> "None"
                    rand < 0.15f -> "Flamethrower"
                    rand < 0.28f -> "Psychic Power"
                    rand < 0.42f -> "Demogorgon"
                    rand < 0.55f -> "Mind Flayer"
                    else -> "None"
                }
            }
        }
    }

    var playerRow by remember { mutableStateOf(4) }
    var playerColumn by remember { mutableStateOf(0) }

    LaunchedEffect(gameStarted, gameOver) {
        if (gameStarted && !gameOver) {
            while (!gameOver) {
                delay(1000)
                vecnaScore += 1f
                if (score <= 0f || vecnaScore >= score) {
                    gameOver = true
                    gameWon = false
                }
            }
        }
    }

    LaunchedEffect(triggerDamageFlash) {
        if (triggerDamageFlash) {
            shakeAnim.animateTo(12f, animationSpec = tween(40, easing = LinearEasing))
            shakeAnim.animateTo(-12f, animationSpec = tween(40, easing = LinearEasing))
            shakeAnim.animateTo(8f, animationSpec = tween(40, easing = LinearEasing))
            shakeAnim.animateTo(-8f, animationSpec = tween(40, easing = LinearEasing))
            shakeAnim.animateTo(0f, animationSpec = tween(40, easing = LinearEasing))
            triggerDamageFlash = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Chime")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "ChimePulse"
    )

    val diceRotation = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .offset(x = shakeAnim.value.dp)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "INV: $activePowerUpName", color = Color(0xFFFF007F), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "HEALTH: ${score.toInt()}", color = Color(0xFF00AA00), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "VECNA: ${vecnaScore.toInt()}",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 80.dp)
                .width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer(rotationZ = diceRotation.value)
                    .background(Color.Black, RoundedCornerShape(8.dp))
                    .clickable {
                        if (!gameOver && !isDiceRolling) {
                            isDiceRolling = true
                            coroutineScope.launch {
                                repeat(8) {
                                    dice = Random.nextInt(1, 7)
                                    diceRotation.snapTo(Random.nextFloat() * 360f)
                                    delay(60)
                                }
                                diceRotation.animateTo(0f, tween(100))
                                isDiceRolling = false
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val dotRadius = 5.dp.toPx()
                val left = size.width * 0.25f
                val right = size.width * 0.75f
                val top = size.height * 0.25f
                val bottom = size.height * 0.75f

                when (dice) {
                    1 -> drawCircle(Color.Cyan, dotRadius, center)
                    2 -> {
                        drawCircle(Color.Cyan, dotRadius, Offset(left, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, bottom))
                    }
                    3 -> {
                        drawCircle(Color.Cyan, dotRadius, Offset(left, top))
                        drawCircle(Color.Cyan, dotRadius, center)
                        drawCircle(Color.Cyan, dotRadius, Offset(right, bottom))
                    }
                    4 -> {
                        drawCircle(Color.Cyan, dotRadius, Offset(left, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(left, bottom))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, bottom))
                    }
                    5 -> {
                        drawCircle(Color.Cyan, dotRadius, Offset(left, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, top))
                        drawCircle(Color.Cyan, dotRadius, center)
                        drawCircle(Color.Cyan, dotRadius, Offset(left, bottom))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, bottom))
                    }
                    6 -> {
                        drawCircle(Color.Cyan, dotRadius, Offset(left, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, top))
                        drawCircle(Color.Cyan, dotRadius, Offset(left, center.y))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, center.y))
                        drawCircle(Color.Cyan, dotRadius, Offset(left, bottom))
                        drawCircle(Color.Cyan, dotRadius, Offset(right, bottom))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isDiceRolling) "Rolling..." else "Click to Roll",
                fontSize = 11.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                repeat(5) { row ->
                    Row {
                        repeat(5) { column ->
                            val isCurrentPos = (playerRow == row && playerColumn == column)
                            val isVisited = (row to column) in visited
                            val isUpsideDownBlock = gridChoice[row][column] == "Upside world"

                            val isAdjacent = (row == playerRow && (column == playerColumn + 1 || column == playerColumn - 1)) ||
                                    (column == playerColumn && (row == playerRow + 1 || row == playerRow - 1))

                            val cellBackgroundColor = when {
                                isVisited || isCurrentPos -> if (isUpsideDownBlock) Color.Transparent else Color(0xFF90EE90)
                                else -> Color.Black
                            }

                            val cellBorderColor = when {
                                isCurrentPos -> Color(0xFF00E5FF)
                                isVisited && isUpsideDownBlock -> Color(0xFFFF073A)
                                isVisited -> Color(0xFF00FF66)
                                else -> Color(0xFF00E5FF)
                            }

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(1.dp, cellBorderColor)
                                    .background(cellBackgroundColor)
                                    .clickable {
                                        if (!gameOver && !isDiceRolling && isAdjacent) {
                                            val minReqd = grid[row][column]
                                            if (dice >= minReqd) {
                                                if (!gameStarted) gameStarted = true

                                                visited.add(playerRow to playerColumn)
                                                playerRow = row
                                                playerColumn = column
                                                visited.add(playerRow to playerColumn)

                                                // 1. Environmental Damage calculation (Always triggers in Upside Down)
                                                if (isUpsideDownBlock) {
                                                    score -= 15f
                                                    triggerDamageFlash = true
                                                }

                                                // 2. Combat & Power-Up Collection layer (Completely independent)
                                                val element = gridElements[playerRow][playerColumn]

                                                if (element == "Flamethrower" || element == "Psychic Power") {
                                                    hasPowerUp = true
                                                    activePowerUpName = element
                                                } else if (element == "Demogorgon" || element == "Mind Flayer") {
                                                    if (hasPowerUp) {
                                                        // Defend cleanly using inventory weapon asset
                                                        hasPowerUp = false
                                                        activePowerUpName = "None"
                                                    } else {
                                                        // Missing combat countermeasures, sustain penalty damage
                                                        score -= 30f
                                                        triggerDamageFlash = true
                                                    }
                                                }

                                                if (playerRow == exitRow && playerColumn == exitColumn) {
                                                    gameOver = true
                                                    gameWon = score > vecnaScore
                                                } else if (score <= 0f || vecnaScore >= score) {
                                                    gameOver = true
                                                    gameWon = false
                                                }
                                            }
                                        }
                                    }
                            ) {
                                if (isVisited && isUpsideDownBlock) {
                                    Image(
                                        painter = painterResource(id = R.drawable.vines),
                                        contentDescription = "Upside Down Vine Tile Art",
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (isCurrentPos) {
                                    Image(
                                        painter = painterResource(id = R.drawable.spider),
                                        contentDescription = "Player Blue Spider Icon",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(80.dp).align(Alignment.Center)
                                    )
                                } else if (isVisited) {
                                    val symbol = when (gridElements[row][column]) {
                                        "Flamethrower" -> "🔥"
                                        "Psychic Power" -> "🧠"
                                        "Demogorgon" -> "👹"
                                        "Mind Flayer" -> "🐙"
                                        else -> ""
                                    }
                                    if (symbol.isNotEmpty()) {
                                        Text(text = symbol, fontSize = 22.sp, modifier = Modifier.align(Alignment.Center))
                                    }
                                } else {
                                    val textToShow = if (isAdjacent) "${grid[row][column]}" else ""
                                    Text(
                                        text = textToShow,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (triggerDamageFlash) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.35f)))
        }

        if (gameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gameWon) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (gameWon) "YOU WIN!\nGate Closed Successfully." else "GAME OVER!\nVecna Claimed Hawkins.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
