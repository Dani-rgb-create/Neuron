package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NeuronViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun EducativeGames(viewModel: NeuronViewModel) {
    val activeStudent by viewModel.activeStudent.collectAsState()
    val isTtsReady by viewModel.isTtsInitialized.collectAsState()
    
    var activeEngine by remember { mutableStateOf<String?>(null) } // "LETRAS", "PAREJAS", etc.
    var activeSubTab by remember { mutableStateOf("GIMNASIO") } // "GIMNASIO", "CREATOR", "TESTS"

    val enginesList = listOf(
        GameEngineInfo("LETRAS", "Motor de Letras", "Completa la palabra arrastrando o pulsando letras en orden.", Icons.Filled.SortByAlpha, Color(0xFF3B82F6)),
        GameEngineInfo("PAREJAS", "Asociación Parejas", "Conecta las emociones con su respectiva situación.", Icons.Filled.Compare, Color(0xFFF59E0B)),
        GameEngineInfo("MEMORIA", "Memoria Visual", "Encuentra los pares de pictogramas terapéuticos.", Icons.Filled.Extension, Color(0xFF10B981)),
        GameEngineInfo("COMPRENSION", "Comprensión Escenas", "Responde a preguntas de comprensión social.", Icons.Filled.QuestionAnswer, Color(0xFFEC4899)),
        GameEngineInfo("FONETICA", "Discriminación B/M/P", "Escucha el sonido y selecciona la respuesta correcta.", Icons.Filled.VolumeUp, Color(0xFF8B5CF6)),
        GameEngineInfo("EJECUTIVAS", "Secuencias de Pasos", "Ordena cronológicamente los pasos de higiene diaria.", Icons.Filled.PlaylistAddCheck, Color(0xFF06B6D4)),
        GameEngineInfo("SOCIAL", "Habilidades Sociales", "Escenarios interactivos para entrenar la empatía.", Icons.Filled.InterpreterMode, Color(0xFFEF4444)),
        GameEngineInfo("AUTONOMIA", "Seguridad y Autonomía", "Aprende pautas de autonomía e higiene urbana.", Icons.Filled.Accessibility, Color(0xFF14B8A6)),
        GameEngineInfo("EXPLORACION", "Encuentra el Intruso", "Identifica qué objeto no pertenece al grupo.", Icons.Filled.Search, Color(0xFF10B981)),
        GameEngineInfo("MATEMATICAS", "Matemáticas Visuales", "Suma y cuenta pictogramas geométricos y alimentos.", Icons.Filled.Calculate, Color(0xFFF59E0B)),
        GameEngineInfo("PICTOGRAMAS", "Sintaxis Pictogramas", "Construye oraciones válidas ordenando pictogramas.", Icons.Filled.Grid4x4, Color(0xFF3B82F6))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .then(if (activeSubTab == "GIMNASIO" && activeEngine == null) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeEngine == null) {
            // Screen header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Gimnasio Cerebro-Cognitivo", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Selecciona un motor de juego para ejercitar el neurodesarrollo, o diseña adaptaciones y evaluaciones con el creador personalizado y tests infinitos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("GIMNASIO", "Gimnasio 🎮", Icons.Filled.SportsEsports),
                    Triple("CREATOR", "Creador 🛠️", Icons.Filled.Build),
                    Triple("TESTS", "Tests 🎯", Icons.Filled.Psychology)
                ).forEach { (id, label, icon) ->
                    val isSel = activeSubTab == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeSubTab = id }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(icon, contentDescription = null, tint = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            when (activeSubTab) {
                "CREATOR" -> {
                    TeacherCustomGameScreen(viewModel)
                }
                "TESTS" -> {
                    InfiniteTestScreen(viewModel)
                }
                else -> {
                    Text("Motores Disponibles (11)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

                    enginesList.forEach { engine ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    activeEngine = engine.key
                                    viewModel.speak("Comenzando juego de ${engine.title}")
                                }
                                .testTag("engine_card_${engine.key}"),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(engine.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(engine.icon, contentDescription = null, tint = engine.color)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(engine.title, fontWeight = FontWeight.Bold)
                                    Text(engine.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        } else {
            // Render active engine inside a nested wrapper
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeEngine = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jugando: ${enginesList.find { it.key == activeEngine }?.title ?: ""}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Route dynamically
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("active_game_engine_surface")
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    when (activeEngine) {
                        "LETRAS" -> LettersEngine(viewModel) { activeEngine = null }
                        "PAREJAS" -> PartnersEngine(viewModel) { activeEngine = null }
                        "MEMORIA" -> MemoryEngine(viewModel) { activeEngine = null }
                        "COMPRENSION" -> ComprehensionEngine(viewModel) { activeEngine = null }
                        "FONETICA" -> PhonicsEngine(viewModel) { activeEngine = null }
                        "EJECUTIVAS" -> ExecutiveEngine(viewModel) { activeEngine = null }
                        "SOCIAL" -> SocialEngine(viewModel) { activeEngine = null }
                        "AUTONOMIA" -> AutonomyEngine(viewModel) { activeEngine = null }
                        "EXPLORACION" -> IntruderEngine(viewModel) { activeEngine = null }
                        "MATEMATICAS" -> MathematicsEngine(viewModel) { activeEngine = null }
                        "PICTOGRAMAS" -> SyntaxEngine(viewModel) { activeEngine = null }
                    }
                }
            }
        }
    }
}

data class GameEngineInfo(
    val key: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

// ==========================================
// 1. MOTOR LETRAS
// ==========================================
@Composable
fun LettersEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val targetWord = "CASA"
    val initialLetters = listOf('A', 'S', 'C', 'A')
    
    var scrambled by remember { mutableStateOf(initialLetters) }
    var answerList by remember { mutableStateOf<List<Char>>(emptyList()) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var isDone by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("¡Forma la palabra del pictograma!", fontWeight = FontWeight.Bold)
        
        // Target picto
        Box(
            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Home, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(48.dp))
        }

        // Selected letters slots
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (i in 0 until targetWord.length) {
                val filled = answerList.getOrNull(i)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (filled != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filled?.toString() ?: "_",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
            }
        }

        if (!isDone) {
            Text("Letras disponibles (toca en orden):", style = MaterialTheme.typography.bodySmall)
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                scrambled.forEachIndexed { index, char ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                viewModel.speak(char.toString())
                                // Logical check
                                val nextNeededIndex = answerList.size
                                if (targetWord.getOrNull(nextNeededIndex) == char) {
                                    val updated = answerList.toMutableList()
                                    updated.add(char)
                                    answerList = updated
                                    
                                    val rem = scrambled.toMutableList()
                                    rem.removeAt(index)
                                    scrambled = rem
                                    
                                    if (answerList.size == targetWord.length) {
                                        isDone = true
                                        viewModel.speak("¡Excelente trabajo! Has deletreado Casa.")
                                        val totalMs = System.currentTimeMillis() - startTime
                                        viewModel.recordGameScore("LETRAS", 100, errors, (totalMs/1000).toInt(), "Eje lúdico: Ortografía silábica de Casa.")
                                    }
                                } else {
                                    errors++
                                    viewModel.speak("No es correcto, intenta de nuevo.")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(char.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        } else {
            CongratsCard {
                // Restart
                scrambled = initialLetters.shuffled()
                answerList = emptyList()
                errors = 0
                isDone = false
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 2. MOTOR PAREJAS
// ==========================================
@Composable
fun PartnersEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    // Left: Emotion, Right: Logical Match Situation
    val pairs = listOf(
        Pair("Contento 😊", "¡Recibir un regalo! 🎁"),
        Pair("Enojado 😡", "¡Que dañen mi juguete! 🧸"),
        Pair("Cansado 😴", "¡Jugar fútbol todo el día! ⚽")
    )
    
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }
    var completedCount by remember { mutableStateOf(0) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var done by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Empareja Emoción con su Situación Lógica:", fontWeight = FontWeight.Bold)

        if (!done) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Column Left
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Emociones", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    pairs.forEach { p ->
                        val isSelected = selectedLeft == p.first
                        Button(
                            onClick = { selectedLeft = p.first; viewModel.speak("Emoción " + p.first) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(p.first, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                // Column Right
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Escenarios", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    pairs.shuffled(Random(123)).forEach { p ->
                        val isSelected = selectedRight == p.second
                        Button(
                            onClick = { 
                                selectedRight = p.second
                                viewModel.speak("Situación " + p.second)
                                // Trigger matching check
                                if (selectedLeft != null && selectedRight != null) {
                                    val matchedPair = pairs.find { it.first == selectedLeft }
                                    if (matchedPair?.second == p.second) {
                                        completedCount++
                                        viewModel.speak("¡Bien hecho! Pareja correcta.")
                                        selectedLeft = null
                                        selectedRight = null
                                        if (completedCount == pairs.size) {
                                            done = true
                                            val speedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                                            viewModel.recordGameScore("PAREJAS", 120, errors, speedSec, "Asociación Emocional Lógica completa.")
                                        }
                                    } else {
                                        errors++
                                        viewModel.speak("Eso no coincide, busca la emoción lógica.")
                                        selectedLeft = null
                                        selectedRight = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(p.second, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
            Text("Puntuación actual: $completedCount / ${pairs.size} aciertos. Errores: $errors", style = MaterialTheme.typography.labelSmall)
        } else {
            CongratsCard {
                completedCount = 0
                errors = 0
                done = false
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 3. MOTOR MEMORIA
// ==========================================
@Composable
fun MemoryEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val pictoOptions = listOf("🍎", "🐶", "🎈", "🍎", "🐶", "🎈")
    var cardStates by remember { mutableStateOf(List(pictoOptions.size) { false }) } // false = closed
    var firstSelectedIdx by remember { mutableStateOf<Int?>(null) }
    var matchedIndices by remember { mutableStateOf(emptyList<Int>()) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var done by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¡Buscando parejas de símbolos!", fontWeight = FontWeight.Bold)

        if (!done) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(180.dp)
            ) {
                itemsIndexed(pictoOptions) { idx, picto ->
                    val isRevealed = cardStates[idx] || matchedIndices.contains(idx)
                    Card(
                        onClick = {
                            if (matchedIndices.contains(idx) || idx == firstSelectedIdx) return@Card
                            viewModel.speak("Tarjeta revelada")
                            
                            val opened = cardStates.toMutableList()
                            opened[idx] = true
                            cardStates = opened

                            if (firstSelectedIdx == null) {
                                firstSelectedIdx = idx
                            } else {
                                val firstIdx = firstSelectedIdx!!
                                if (pictoOptions[firstIdx] == picto) {
                                    // Matched
                                    val matches = matchedIndices.toMutableList()
                                    matches.add(firstIdx)
                                    matches.add(idx)
                                    matchedIndices = matches
                                    viewModel.speak("¡Acertaste!")
                                    
                                    if (matchedIndices.size == pictoOptions.size) {
                                        done = true
                                        viewModel.recordGameScore("MEMORIA", 150, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Memoria visual de pictogramas frutales y animales.")
                                    }
                                } else {
                                    errors++
                                    viewModel.speak("No coinciden.")
                                    // Delay hiding cards
                                    coroutineScope.launch {
                                        delay(1000)
                                        val closed = cardStates.toMutableList()
                                        closed[firstIdx] = false
                                        closed[idx] = false
                                        cardStates = closed
                                    }
                                }
                                firstSelectedIdx = null
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRevealed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(70.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRevealed) picto else "?",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        } else {
            CongratsCard {
                cardStates = List(pictoOptions.size) { false }
                matchedIndices = emptyList()
                errors = 0
                done = false
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 4. MOTOR COMPRENSIÓN
// ==========================================
@Composable
fun ComprehensionEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    var answeredCorrectly by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Escenario: El Timbre Escolar Fuerte 🔔", fontWeight = FontWeight.Bold)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "El timbre de la escuela suena muy ruidoso y te lastima los oídos. ¿Cuál es la mejor solución terapéutica?",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        if (!answeredCorrectly) {
            Button(
                onClick = {
                    viewModel.speak("Opción incorrecta. Correr asustado a la calle es peligroso.")
                    errors++
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔴 Salir corriendo a la calle asustado", color = MaterialTheme.colorScheme.onErrorContainer)
            }

            Button(
                onClick = {
                    answeredCorrectly = true
                    viewModel.speak("¡Muy sabio! El uso de auriculares canceladores amortigua el dolor sensorial.")
                    viewModel.recordGameScore("COMPRENSION", 110, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Habilidades de autocuidado contra hipersensibilidad sonora.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🟢 Ponerme audífonos canceladores de ruido", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            CongratsCard {
                answeredCorrectly = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 5. MOTOR FONÉTICA
// ==========================================
@Composable
fun PhonicsEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    var answeredCorrectly by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Asociación Auditiva Silábica:", fontWeight = FontWeight.Bold)
        
        Button(
            onClick = { viewModel.speak("Busca la sílaba MA de Manzana") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.VolumeUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("🔊 Escuchar instrucción fonética")
        }

        if (!answeredCorrectly) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Pair("Banana 🍌", false),
                    Pair("Manzana 🍎", true),
                    Pair("Sandía 🍉", false)
                ).forEach { item ->
                    Card(
                        onClick = {
                            if (item.second) {
                                answeredCorrectly = true
                                viewModel.speak("Excelente. Manzana inicia con MA.")
                                viewModel.recordGameScore("FONETICA", 120, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Discriminación del fonema M silábico.")
                            } else {
                                errors++
                                viewModel.speak("Ese sonido inicia diferente, intenta buscando Manzana.")
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f).height(80.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(item.first, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            CongratsCard {
                answeredCorrectly = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 6. MOTOR FUNCIONES EJECUTIVAS
// ==========================================
@Composable
fun ExecutiveEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val steps = listOf("1. Mojar manos 🚰", "2. Poner jabón 🧼", "3. Enjuagar y secar con toalla 🧼🚿")
    var orderIndex by remember { mutableStateOf(0) }
    var errors by remember { mutableStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ordenando los Pasos de Lavarnos las Manos:", fontWeight = FontWeight.Bold)

        if (!done) {
            Text("Toca el paso que represente la acción número ${orderIndex + 1}:", style = MaterialTheme.typography.bodySmall)

            val shuffled = steps.shuffled(Random(456))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                shuffled.forEach { step ->
                    Button(
                        onClick = {
                            if (step == steps[orderIndex]) {
                                viewModel.speak("Paso correcto.")
                                orderIndex++
                                if (orderIndex == steps.size) {
                                    done = true
                                    viewModel.speak("¡Higienizado perfecto! Has ordenado la secuencia temporal.")
                                    viewModel.recordGameScore("EJECUTIVAS", 130, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Secuencia motora de lavado terapéutico.")
                                }
                            } else {
                                errors++
                                viewModel.speak("Ese paso va en otra posición temporal.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(step, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        } else {
            CongratsCard {
                orderIndex = 0
                errors = 0
                done = false
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 7. MOTOR SOCIAL
// ==========================================
@Composable
fun SocialEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    var isDone by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Empatía en el Aula 🏫", fontWeight = FontWeight.Bold)
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Text(
                "Llegas al patio del colegio y observas a Lucas llorando solo en un rincón. ¿Qué puedes hacer para ayudarlo?",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        if (!isDone) {
            Button(
                onClick = {
                    errors++
                    viewModel.speak("Tomar fotos para burlarse provoca hostigamiento. No es correcto.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❌ Reírse y señalar con otros niños", color = MaterialTheme.colorScheme.onErrorContainer)
            }

            Button(
                onClick = {
                    isDone = true
                    viewModel.speak("¡Increíble nivel de empatía! Acercarse con suavidad a invitar a Lucas a jugar calma su tristeza.")
                    viewModel.recordGameScore("SOCIAL", 100, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Habilidades de asertividad y reducción de acoso.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✅ Sentarte al lado y preguntarle si desea un juguete", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else {
            CongratsCard {
                isDone = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 8. MOTOR AUTONOMÍA
// ==========================================
@Composable
fun AutonomyEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    var answeredCorrectly by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Cruzar la Calle con Seguridad 🚦🛣️", fontWeight = FontWeight.Bold)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                "Quieres cruzar de calle pero el semáforo para peatones está pintado de ROJO...",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }

        if (!answeredCorrectly) {
            Button(
                onClick = {
                    answeredCorrectly = true
                    viewModel.speak("¡Seguridad total! Debemos esperar a que el semáforo se ilumine en VERDE.")
                    viewModel.recordGameScore("AUTONOMIA", 140, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Seguridad vial urbana del alumno.")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🟢 Esperar parado en la acera", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    errors++
                    viewModel.speak("Correr con semáforo rojo es muy peligroso.")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔴 Cruzar corriendo rápido", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        } else {
            CongratsCard {
                answeredCorrectly = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 9. MOTOR EXPLORACIÓN
// ==========================================
@Composable
fun IntruderEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val itemsList = listOf(
        Pair("Plátano 🍌", false),
        Pair("Manzana 🍎", false),
        Pair("Zapato 🥾", true), // shoe is the intruder among fruits
        Pair("Fresa 🍓", false)
    )
    var isDone by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Halla al Intruso (Objeto que no pertenece):", fontWeight = FontWeight.Bold)

        if (!isDone) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(130.dp)
            ) {
                items(itemsList) { item ->
                    Card(
                        onClick = {
                            if (item.second) {
                                isDone = true
                                viewModel.speak("Excelente. El zapato no es una fruta.")
                                viewModel.recordGameScore("EXPLORACION", 100, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Identificación y exclusión lógica.")
                            } else {
                                errors++
                                viewModel.speak("Eso sí pertenece a las frutas.")
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            Text(item.first, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            CongratsCard {
                isDone = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 10. MOTOR MATEMÁTICAS
// ==========================================
@Composable
fun MathematicsEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val countTarget = 4
    val scrambledOptions = listOf(2, 3, 4, 5)
    var isDone by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("¿Cuántas galletas hay en el plato? 🥧", fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..countTarget) {
                Text("🍪", fontSize = 36.sp)
            }
        }

        if (!isDone) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                scrambledOptions.forEach { qty ->
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                if (qty == countTarget) {
                                    isDone = true
                                    viewModel.speak("¡Excelente! Hay exactamente cuatro galletas.")
                                    viewModel.recordGameScore("MATEMATICAS", 110, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Razonamiento y conteo cardinal simple.")
                                } else {
                                    errors++
                                    viewModel.speak("Es un número equivocado, vuelve a contar.")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(qty.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        } else {
            CongratsCard {
                isDone = false
                errors = 0
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// ==========================================
// 11. MOTOR PICTOGRAMAS
// ==========================================
@Composable
fun SyntaxEngine(viewModel: NeuronViewModel, onFinish: () -> Unit) {
    val targetSentence = listOf("Yo", "Quiero", "Dormir")
    var userSequence by remember { mutableStateOf<List<String>>(emptyList()) }
    var scrambleList by remember { mutableStateOf(listOf("Quiero", "Yo", "Comer", "Dormir")) }
    var errors by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var done by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Construye la frase: 'Yo Quiero Dormir'", fontWeight = FontWeight.Bold)

        // Selected Sequence Box
        Card(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userSequence.isEmpty()) {
                    Text("Selecciona pictogramas abajo...", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(start = 12.dp))
                } else {
                    userSequence.forEach { item ->
                        SuggestionChip(onClick = {}, label = { Text(item) })
                    }
                }
            }
        }

        if (!done) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scrambleList.forEachIndexed { index, item ->
                    Button(
                        onClick = {
                            viewModel.speak(item)
                            val nextCheckedIdx = userSequence.size
                            if (targetSentence.getOrNull(nextCheckedIdx) == item) {
                                val updated = userSequence.toMutableList()
                                updated.add(item)
                                userSequence = updated
                                
                                val rem = scrambleList.toMutableList()
                                rem.removeAt(index)
                                scrambleList = rem

                                if (userSequence.size == targetSentence.size) {
                                    done = true
                                    viewModel.speak("¡Grandioso! Has ordenado 'Yo Quiero Dormir'.")
                                    viewModel.recordGameScore("PICTOGRAMAS", 150, errors, ((System.currentTimeMillis() - startTime)/1000).toInt(), "Sintaxis lingüística con tarjetas CAA.")
                                }
                            } else {
                                errors++
                                viewModel.speak("Eso rompe el sentido lógico gramatical.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(item)
                    }
                }
            }
        } else {
            CongratsCard {
                userSequence = emptyList()
                scrambleList = listOf("Quiero", "Yo", "Comer", "Dormir")
                errors = 0
                done = false
                startTime = System.currentTimeMillis()
            }
        }
    }
}

// Congratulations Widget reused by motors
@Composable
fun CongratsCard(onReset: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(32.dp))
                Text("¡Buen Trabajo!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            }
            Text("Puntuación subida al registro local del alumno con éxito.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Volver a Jugar")
            }
        }
    }
}
