package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NeuronViewModel
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay

@Composable
fun NeuronJumper(viewModel: NeuronViewModel) {
    val activeStudent by viewModel.activeStudent.collectAsState()
    
    // Game state variables
    var score by remember { mutableStateOf(0) }
    var highScore by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Retro Game Mode selection:
    // "NEURON" -> Classic Sinapses
    // "MARIO" -> Super Plumber (Red plumber theme + brick patterns + Gold coins)
    // "SONIC" -> Hyper Hedgehog (Blue hedgehog theme + Checkered loops + Power rings)
    // "KONG" -> Donkey Barrel (Barrels & steel girders + Bananas)
    var selectedGameMode by remember { mutableStateOf("NEURON") }

    // Therapeutic speed settings: "NORMAL" or "SOFT" (TEA sensory safety style)
    var speedSetting by remember { mutableStateOf("NORMAL") }
    
    // Automatically inherit student preference
    LaunchedEffect(activeStudent) {
        activeStudent?.let { s ->
            if (s.lowSensoryMode) {
                speedSetting = "SOFT"
            }
        }
    }

    // Dynamic physics attributes depending on selection
    val gravity = if (speedSetting == "SOFT") 0.32f else 0.5f
    val jumpStrength = if (speedSetting == "SOFT") -9.5f else -12f
    val gameLoopDelayMs = if (speedSetting == "SOFT") 24L else 16L

    // Game physics runtime variables
    var ballX by remember { mutableStateOf(250f) }
    var ballY by remember { mutableStateOf(400f) }
    var ballVy by remember { mutableStateOf(0f) }

    // Platforms
    var platforms by remember {
        mutableStateOf(
            listOf(
                Platform(100f, 600f, 200f),
                Platform(350f, 450f, 200f),
                Platform(150f, 300f, 200f),
                Platform(400f, 150f, 200f)
            )
        )
    }

    // Collectible items placed above platforms
    var collectibles by remember {
        mutableStateOf(
            listOf(
                CollectibleItem(1, 200f, 550f, false),
                CollectibleItem(2, 450f, 400f, false),
                CollectibleItem(3, 250f, 250f, false),
                CollectibleItem(4, 500f, 100f, false)
            )
        )
    }

    // High performance retro loop
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isPlaying && !isGameOver) {
            delay(gameLoopDelayMs)
            
            // Apply gravity
            ballVy += gravity
            ballY += ballVy

            // Left / Right physics bounce bounds wrapping
            if (ballX < 0f) ballX = 500f
            if (ballX > 500f) ballX = 0f

            // Move platforms down as character climbs higher
            if (ballY < 280f) {
                val diff = 280f - ballY
                ballY = 280f
                score += (diff / 8).toInt()
                
                // Shift existing platforms down, recycle old ones to the top
                platforms = platforms.mapIndexed { index, plat ->
                    var newY = plat.y + diff
                    var newX = plat.x
                    if (newY > 700f) {
                        newY = 0f
                        newX = kotlin.random.Random.nextDouble(15.0, 360.0).toFloat()
                    }
                    Platform(newX, newY, plat.width)
                }

                // Shift & recycle collectibles
                collectibles = collectibles.mapIndexed { index, col ->
                    var newY = col.y + diff
                    var newX = col.x
                    var collected = col.isCollected
                    if (newY > 700f) {
                        val companionPlatform = platforms[index % platforms.size]
                        newY = companionPlatform.y - 40f
                        newX = companionPlatform.x + (companionPlatform.width / 2f)
                        collected = false
                    }
                    col.copy(x = newX, y = newY, isCollected = collected)
                }
            }

            // Platform collision check (touch from top heading downwards)
            platforms.forEach { plat ->
                if (ballVy > 0f && ballY >= plat.y - 12f && ballY <= plat.y + 12f &&
                    ballX >= plat.x - 12f && ballX <= plat.x + plat.width + 12f) {
                    ballVy = jumpStrength
                    
                    // Specific sound description on jump based on mode
                    val jumpSound = when (selectedGameMode) {
                        "MARIO" -> "¡Boing Saltador Plomero!"
                        "SONIC" -> "¡Turrrbo impulso de luz!"
                        "KONG" -> "¡Barril de rebote!"
                        else -> "¡Impulso Sináptico!"
                    }
                    viewModel.speak(jumpSound)
                }
            }

            // Collectible item collision check
            collectibles = collectibles.map { col ->
                if (!col.isCollected && 
                    Math.abs(ballX - col.x) < 36f && 
                    Math.abs(ballY - col.y) < 36f) {
                    
                    val bonusScore = 40
                    score += bonusScore
                    
                    // Cute customized voices
                    val soundText = when (selectedGameMode) {
                        "MARIO" -> "¡Plim! Moneda de Oro"
                        "SONIC" -> "¡Shhhling! Anillo sónico"
                        "KONG" -> "¡Nyam! Banana arcade"
                        else -> "¡Energía sináptica obtenida!"
                    }
                    viewModel.speak(soundText)
                    col.copy(isCollected = true)
                } else {
                    col
                }
            }

            // Loose check
            if (ballY > 720f) {
                isGameOver = true
                isPlaying = false
                viewModel.speak("¡Fin de la partida! Puntos conseguidos: $score.")
                if (score > highScore) {
                    highScore = score
                    viewModel.recordGameScore(
                        game = "OCIO",
                        earnedScore = score,
                        errorsCount = 0,
                        speedSec = 35,
                        notes = "Record de Ocio Arcade con modo $selectedGameMode ($speedSetting)."
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // High quality design theme header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SALA DE OCIO & CONTROL MOTOR 👾", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Espacio lúdico para recompensar el trabajo del alumno, entrenando la coordinación viso-motora, tiempo de reacción y foco atencional de forma divertida.",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
        }

        // Selection of Retro Skins & Modes
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Preselección de Franquicia del Juego:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        GameModeTheme("NEURON", "Sinapsis", Icons.Filled.Troubleshoot, Color(0xFF3B82F6)),
                        GameModeTheme("MARIO", "S. Mario", Icons.Filled.Celebration, Color(0xFFEF4444)),
                        GameModeTheme("SONIC", "Sonic R.", Icons.Filled.ElectricBolt, Color(0xFF0074D9)),
                        GameModeTheme("KONG", "D. Kong", Icons.Filled.Storefront, Color(0xFFF59E0B))
                    ).forEach { theme ->
                        val isSel = selectedGameMode == theme.key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) theme.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (isSel) theme.color else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { 
                                    selectedGameMode = theme.key 
                                    viewModel.speak("Cambiado a mundo lúdico ${theme.displayName}")
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(theme.icon, contentDescription = null, tint = theme.color, modifier = Modifier.size(20.dp))
                                Text(theme.displayName, fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }
                    }
                }

                // Therapeutic Speed Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Regulación Sensorial de Velocidad:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ElevatedFilterChip(
                            selected = speedSetting == "SOFT",
                            onClick = { 
                                speedSetting = "SOFT"
                                viewModel.speak("Velocidad reducida para seguridad sensorial")
                            },
                            label = { Text("Sensory-Soft 🔇", fontSize = 10.sp) }
                        )

                        ElevatedFilterChip(
                            selected = speedSetting == "NORMAL",
                            onClick = { 
                                speedSetting = "NORMAL"
                                viewModel.speak("Velocidad estándar arcade")
                            },
                            label = { Text("Arcade Estándar", fontSize = 10.sp) }
                        )
                    }
                }
            }
        }

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Puntaje: $score XP", 
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), 
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Máximo local: $highScore XP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Active Theme canvas colors extraction
        val canvasBgColor = when (selectedGameMode) {
            "MARIO" -> Color(0xFF5C94FC) // retro sky blue
            "SONIC" -> Color(0xFF2C3E50) // retro deep navy blue
            "KONG" -> Color(0xFF1E1E1E) // retro steel dark grey
            else -> Color(0xFF0F172A) // galactic neuron night
        }

        // Canvas container frame
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = canvasBgColor),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("arcade_canvas_frame")
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!isPlaying && !isGameOver) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports, 
                            contentDescription = null, 
                            tint = Color.White.copy(alpha = 0.8f), 
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Modo: ${selectedGameMode.uppercase()}", 
                            color = Color.White, 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Presiona 'INICIAR' para comenzar el entrenamiento motor.", 
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, end = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                ballX = 250f
                                ballY = 400f
                                ballVy = 0f
                                score = 0
                                // Re-seed initial game state elements
                                platforms = listOf(
                                    Platform(100f, 600f, 200f),
                                    Platform(350f, 450f, 200f),
                                    Platform(150f, 300f, 200f),
                                    Platform(400f, 150f, 200f)
                                )
                                collectibles = listOf(
                                    CollectibleItem(1, 200f, 550f, false),
                                    CollectibleItem(2, 450f, 400f, false),
                                    CollectibleItem(3, 250f, 250f, false),
                                    CollectibleItem(4, 500f, 100f, false)
                                )
                                isPlaying = true
                                isGameOver = false
                                viewModel.speak("Iniciando juego retro de $selectedGameMode. ¡Prepara tus reflejos!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Impulso Lúdico", fontWeight = FontWeight.Black)
                        }
                    }
                } else if (isGameOver) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("¡Dendritas Apagadas! 🔌", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("Mundo retro $selectedGameMode terminado. Puntuación obtenida: $score XP.", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                ballX = 250f
                                ballY = 400f
                                ballVy = 0f
                                score = 0
                                platforms = listOf(
                                    Platform(100f, 600f, 200f),
                                    Platform(350f, 450f, 200f),
                                    Platform(150f, 300f, 200f),
                                    Platform(400f, 150f, 200f)
                                )
                                collectibles = listOf(
                                    CollectibleItem(1, 200f, 550f, false),
                                    CollectibleItem(2, 450f, 400f, false),
                                    CollectibleItem(3, 250f, 250f, false),
                                    CollectibleItem(4, 500f, 100f, false)
                                )
                                isPlaying = true
                                isGameOver = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Volver a Conectar", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Game Active canvas drawing
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    // Touch target sides move ball symmetrically
                                    ballX = if (offset.x < size.width / 2) {
                                        (ballX - 50f).coerceAtLeast(10f)
                                    } else {
                                        (ballX + 50f).coerceAtMost(490f)
                                    }
                                }
                            }
                    ) {
                        // 1. Draw thematic background patterns
                        if (selectedGameMode == "MARIO") {
                            // Draw brick border lines styled retro
                            drawRect(
                                color = Color(0xFFC84C0C).copy(alpha = 0.3f),
                                size = Size(size.width, 24f),
                                topLeft = Offset(0f, size.height - 24f)
                            )
                        } else if (selectedGameMode == "SONIC") {
                            // Checkered grid floor effect
                            drawRect(
                                color = Color(0xFF22C55E).copy(alpha = 0.3f),
                                size = Size(size.width, 16f),
                                topLeft = Offset(0f, size.height - 16f)
                            )
                        } else if (selectedGameMode == "KONG") {
                            // Industrial girders pattern
                            drawLine(
                                color = Color(0xFFDC2626).copy(alpha = 0.4f),
                                start = Offset(0f, size.height - 10f),
                                end = Offset(size.width, size.height - 10f),
                                strokeWidth = 8f
                            )
                        }

                        // 2. Draw platforms according to active retro game skin
                        platforms.forEach { plat ->
                            val (platformColor, platformStrokeTheme) = when (selectedGameMode) {
                                "MARIO" -> Pair(Color(0xFFC84C0C), true) // Red/orange brick
                                "SONIC" -> Pair(Color(0xFF22C55E), false) // Green grass loop
                                "KONG" -> Pair(Color(0xFFDC2626), true)  // Steel industrial girder
                                else -> Pair(Color(0xFF10B981), false)   // Electric Synapse Green
                            }

                            drawRoundRect(
                                color = platformColor,
                                topLeft = Offset(plat.x, plat.y),
                                size = Size(plat.width, 22f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )

                            // Add vintage girder/brick lines inside the platforms for retro look
                            if (platformStrokeTheme) {
                                lineToHorizontalTiles(this, plat.x, plat.y, plat.width, 22f, selectedGameMode)
                            }
                        }

                        // 3. Draw Collectibles (Coins, Rings, Bananas, Neutrinos)
                        collectibles.forEach { col ->
                            if (!col.isCollected) {
                                when (selectedGameMode) {
                                    "MARIO" -> {
                                        // Gold Coin
                                        drawCircle(
                                            color = Color(0xFFFFD700),
                                            radius = 12f,
                                            center = Offset(col.x, col.y)
                                        )
                                        drawCircle(
                                            color = Color(0xFFB8860B),
                                            radius = 12f,
                                            center = Offset(col.x, col.y),
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                    "SONIC" -> {
                                        // Golden Ring ellipse
                                        drawCircle(
                                            color = Color(0xFFFFCC00),
                                            radius = 11f,
                                            center = Offset(col.x, col.y),
                                            style = Stroke(width = 4f)
                                        )
                                    }
                                    "KONG" -> {
                                        // Curved Yellow Banana slice
                                        val bananaPath = Path().apply {
                                            moveTo(col.x - 10f, col.y - 12f)
                                            quadraticTo(col.x + 10f, col.y, col.x - 10f, col.y + 12f)
                                            quadraticTo(col.x, col.y, col.x - 10f, col.y - 12f)
                                        }
                                        drawPath(
                                            path = bananaPath,
                                            color = Color(0xFFFBBF24)
                                        )
                                    }
                                    else -> {
                                        // Sparkly Yellow Diamond (Neutrino)
                                        val diamondPath = Path().apply {
                                            moveTo(col.x, col.y - 13f)
                                            lineTo(col.x + 10f, col.y)
                                            lineTo(col.x, col.y + 13f)
                                            lineTo(col.x - 10f, col.y)
                                            close()
                                        }
                                        drawPath(
                                            path = diamondPath,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Draw character avatar matching selected retro style
                        val primaryColor = when (selectedGameMode) {
                            "MARIO" -> Color(0xFFEF4444) // Red overalls
                            "SONIC" -> Color(0xFF3B82F6) // Speed hedgehog blue
                            "KONG" -> Color(0xFF92400E)  // Brown Gorilla
                            else -> Color(0xFF60A5FA)     // Electric neon blue
                        }

                        drawCircle(
                            color = primaryColor,
                            radius = 20f,
                            center = Offset(ballX, ballY)
                        )

                        // Mode-specific graphic additions to character skin
                        if (selectedGameMode == "MARIO") {
                            // Plumber Cap visor
                            drawRect(
                                color = Color.White,
                                size = Size(10f, 6f),
                                topLeft = Offset(ballX - 5f, ballY - 18f)
                            )
                            // Mustache
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(ballX - 10f, ballY + 2f),
                                size = Size(20f, 5f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                            )
                        } else if (selectedGameMode == "SONIC") {
                            // Glowing blue speed spikes
                            val spPath = Path().apply {
                                moveTo(ballX - 20f, ballY - 10f)
                                lineTo(ballX - 28f, ballY - 14f)
                                lineTo(ballX - 20f, ballY - 2f)
                                lineTo(ballX - 28f, ballY - 2f)
                                lineTo(ballX - 18f, ballY + 5f)
                            }
                            drawPath(path = spPath, color = primaryColor)
                        } else if (selectedGameMode == "KONG") {
                            // Red cute tie
                            val tiePath = Path().apply {
                                moveTo(ballX, ballY + 14f)
                                lineTo(ballX - 5f, ballY + 24f)
                                lineTo(ballX, ballY + 28f)
                                lineTo(ballX + 5f, ballY + 24f)
                                close()
                            }
                            drawPath(path = tiePath, color = Color.Red)
                        }

                        // Emotive big Pixar-style white eyes
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(ballX - 5f, ballY - 4f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 6f,
                            center = Offset(ballX + 5f, ballY - 4f)
                        )
                        // Pupils pointing upwards
                        drawCircle(
                            color = Color.Black,
                            radius = 3f,
                            center = Offset(ballX - 5f, ballY - 5f)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = 3f,
                            center = Offset(ballX + 5f, ballY - 5f)
                        )
                    }

                    // Tactile Retro control arrow keys overlaid on bottom of canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, top = 0.dp, end = 12.dp, bottom = 16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // LEFT BUTTON
                            Button(
                                onClick = { ballX = (ballX - 70f).coerceAtLeast(15f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Mover Izquierda", tint = Color.White, modifier = Modifier.size(24.dp))
                            }

                            // RIGHT BUTTON
                            Button(
                                onClick = { ballX = (ballX + 70f).coerceAtMost(485f) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.55f)),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Mover Derecha", tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Draw internal structures inside retro brick plates
private fun lineToHorizontalTiles(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    px: Float,
    py: Float,
    pW: Float,
    pH: Float,
    mode: String
) {
    if (mode == "MARIO") {
        // Vertical brick grid line dividers
        val cols = 4
        val colWidth = pW / cols
        for (i in 1 until cols) {
            scope.drawLine(
                color = Color.Black.copy(alpha = 0.25f),
                start = Offset(px + (i * colWidth), py),
                end = Offset(px + (i * colWidth), py + pH),
                strokeWidth = 2f
            )
        }
    } else if (mode == "KONG") {
        // Red steel support beams cross structures
        scope.drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(px, py),
            end = Offset(px + pW, py + pH),
            strokeWidth = 2f
        )
        scope.drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(px + pW, py),
            end = Offset(px, py + pH),
            strokeWidth = 2f
        )
    }
}

data class Platform(
    val x: Float,
    val y: Float,
    val width: Float
)

data class CollectibleItem(
    val id: Int,
    val x: Float,
    val y: Float,
    val isCollected: Boolean,
    val type: String = "COIN"
)

data class GameModeTheme(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val color: Color
)
