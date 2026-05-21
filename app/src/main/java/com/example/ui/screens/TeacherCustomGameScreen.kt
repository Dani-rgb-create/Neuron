package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomGameConfig
import com.example.ui.NeuronViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TeacherCustomGameScreen(viewModel: NeuronViewModel) {
    val activeStudent by viewModel.activeStudent.collectAsState()
    val customConfigs by viewModel.customGameConfigs.collectAsState()

    var showCreatorForm by remember { mutableStateOf(false) }
    var activeLaunchedConfig by remember { mutableStateOf<CustomGameConfig?>(null) }

    // Form states
    var configTitle by remember { mutableStateOf("") }
    var selectedBaseGame by remember { mutableStateOf("LETRAS") } // LETRAS, MATEMATICAS, MEMORIA
    var selectedFont by remember { mutableStateOf("Mayúsculas") } // Mayúsculas, Minúsculas, Ligada, Imprenta
    var selectedBackground by remember { mutableStateOf("Bosque Encantado") } // Bosque Encantado, Espacio Cósmico, Atardecer Suave, Pixel Clásico, Alto Contraste Retina
    var customWordsText by remember { mutableStateOf("CASA,MAMA,SOPA,LUNA") }
    var maxNumberMath by remember { mutableStateOf(10) }
    var timeLimitSec by remember { mutableStateOf(60) }
    var customInstructions by remember { mutableStateOf("Hola, hoy jugaremos a una actividad diseñada especialmente para ti.") }
    var speedSetting by remember { mutableStateOf("NORMAL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeLaunchedConfig != null) {
            // Launcher is running
            CustomGamePlayer(
                config = activeLaunchedConfig!!,
                viewModel = viewModel,
                onExit = { activeLaunchedConfig = null }
            )
        } else {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Build, tint = MaterialTheme.colorScheme.tertiary, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Creador de Juegos Personalizados 🛠️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Modifica tipografía, vocabulario, fondo sensorial e instrucciones por voz para adaptar el gimnasio lúdico a cada estudiante.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showCreatorForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Configurar Nueva Escena", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = configTitle,
                            onValueChange = { configTitle = it },
                            label = { Text("Título de la Configuración (ej. Mis Frutas favoritas)") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_game_title_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Base Game Selection
                        Text("Tipo de Juego Base:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("LETRAS" to "Letras 🔠", "MATEMATICAS" to "Cuenta 🔢", "MEMORIA" to "Memoria 🧠").forEach { (key, label) ->
                                val sel = selectedBaseGame == key
                                FilterChip(
                                    selected = sel,
                                    onClick = { selectedBaseGame = key },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Font Selection
                        Text("Tipografía de Lectura Escrita:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Mayúsculas", "Minúsculas", "Ligada", "Imprenta").forEach { font ->
                                val sel = selectedFont == font
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(2.dp, if (sel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedFont = font }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when(font) {
                                            "Mayúsculas" -> "ABCD"
                                            "Minúsculas" -> "abcd"
                                            "Ligada" -> "✍️ ligada"
                                            else -> "Imprenta"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Background theme Selection
                        Text("Estilo de Fondo Sensorial:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "Bosque Encantado" to "Pine-soft verde tranquilizador 🌲",
                                "Espacio Cósmico" to "Negro espacial con estrellas 🚀",
                                "Atardecer Suave" to "Gradiente cálido estimulante lento 🌅",
                                "Pixel Clásico" to "Púrpura y cian de consola arcade 👾",
                                "Alto Contraste Retina" to "Texto amarillo en fondo oscuro para visión reducida 👁️",
                                "Mar Profundo" to "Gradiente azul marino pacífico anti-ansiedad 🌊",
                                "Aura Aurora" to "Mágico flujo verde y violeta de enfoque magnético 🌌",
                                "Arena Dorada" to "Ocre y dorado suave para estabilidad cognitiva alta 🏜️",
                                "Nebulosa de Ensueño" to "Tonos lavanda y rosa pastel con efectos de contorno sedoso 🌸"
                            ).forEach { (id, desc) ->
                                val sel = selectedBackground == id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (sel) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                        .clickable { selectedBackground = id }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = sel, onClick = { selectedBackground = id })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(id, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (selectedBaseGame == "LETRAS") {
                            OutlinedTextField(
                                value = customWordsText,
                                onValueChange = { customWordsText = it },
                                label = { Text("Palabras separadas por comas") },
                                supportingText = { Text("Ej: PATA,NENE,CUNA,MULA") },
                                modifier = Modifier.fillMaxWidth().testTag("custom_game_words_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else if (selectedBaseGame == "MATEMATICAS") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Límite de la suma:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(5, 10, 15, 20).forEach { num ->
                                        val sel = maxNumberMath == num
                                        ElevatedFilterChip(
                                            selected = sel,
                                            onClick = { maxNumberMath = num },
                                            label = { Text("Hasta $num") }
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Instructions for speech synthesiser
                        OutlinedTextField(
                            value = customInstructions,
                            onValueChange = { customInstructions = it },
                            label = { Text("Intrucciones Iniciales Habladas (TTS)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Sensory protection settings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Modo Sensorial TEA", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Animaciones lentas con bajo nivel de ruido.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = speedSetting == "SOFT",
                                onCheckedChange = { speedSetting = if (it) "SOFT" else "NORMAL" }
                            )
                        }

                        // Save action
                        Button(
                            onClick = {
                                activeStudent?.let { student ->
                                    if (configTitle.isNotBlank()) {
                                        viewModel.saveCustomGameConfig(
                                            CustomGameConfig(
                                                studentId = student.id,
                                                title = configTitle,
                                                gameType = selectedBaseGame,
                                                fontType = selectedFont,
                                                backgroundStyle = selectedBackground,
                                                customWords = customWordsText.uppercase(),
                                                maxNumber = maxNumberMath,
                                                timeLimitSec = timeLimitSec,
                                                customInstructions = customInstructions,
                                                speedSetting = speedSetting
                                            )
                                        )
                                        // Reset
                                        configTitle = ""
                                        showCreatorForm = false
                                    }
                                }
                            },
                            enabled = configTitle.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("save_custom_game_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Configuración de Profesor", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuraciones Creadas (${customConfigs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showCreatorForm = !showCreatorForm },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showCreatorForm) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (showCreatorForm) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (showCreatorForm) Icons.Filled.Close else Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showCreatorForm) "Cerrar" else "Diseñar Juego")
                }
            }

            if (customConfigs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                        Text(
                            "Ningún juego personalizado guardado todavía.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Pulsa 'Diseñar Juego' para crear adaptaciones específicas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
                ) {
                    items(customConfigs) { config ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(config.gameType) {
                                            "LETRAS" -> Icons.Filled.SortByAlpha
                                            "MATEMATICAS" -> Icons.Filled.Calculate
                                            else -> Icons.Filled.Extension
                                        },
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = null
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(config.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Fondo: ${config.backgroundStyle}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Letra: ${config.fontType}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { 
                                            activeLaunchedConfig = config 
                                        },
                                        modifier = Modifier.testTag("launch_custom_game_btn_${config.id}")
                                    ) {
                                        Icon(Icons.Filled.PlayCircle, contentDescription = "Iniciar Juego Adaptado", tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteCustomGameConfig(config.id) }
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// THE CUSTOM RUNTIME PLAYER ENGINE
// ----------------------------------------------------
@Composable
fun CustomGamePlayer(
    config: CustomGameConfig,
    viewModel: NeuronViewModel,
    onExit: () -> Unit
) {
    // Game loops and scores
    var currentScore by remember { mutableStateOf(0) }
    var errorsCount by remember { mutableStateOf(0) }
    var gameCompleted by remember { mutableStateOf(false) }

    // Word list logic for Letras
    val wordsList = remember { config.customWords.split(",").filter { it.isNotBlank() } }
    var wordIndex by remember { mutableStateOf(0) }
    val activeWord = remember(wordIndex) { wordsList.getOrNull(wordIndex) ?: "CASA" }

    // Sliced available scrambled characters remaining
    var lettersPool by remember { mutableStateOf(emptyList<Char>()) }
    var answerLetters by remember { mutableStateOf(emptyList<Char>()) }

    // Dynamic math states
    var mathNum1 by remember { mutableStateOf((1..config.maxNumber/2).random()) }
    var mathNum2 by remember { mutableStateOf((1..config.maxNumber/2).random()) }
    val mathCorrectAnswer = remember(mathNum1, mathNum2) { mathNum1 + mathNum2 }
    var mathOptions by remember { mutableStateOf(emptyList<Int>()) }

    // Trigger Speak on start
    LaunchedEffect(config) {
        viewModel.speak(config.customInstructions)
        if (config.gameType == "LETRAS") {
            // scramble first word
            lettersPool = activeWord.toList().shuffled()
            answerLetters = emptyList()
        } else if (config.gameType == "MATEMATICAS") {
            // make math options
            val right = mathCorrectAnswer
            mathOptions = listOf(right, right + 1, (right - 2).coerceAtLeast(1), right + 2).distinct().shuffled()
        }
    }

    LaunchedEffect(wordIndex) {
        if (config.gameType == "LETRAS" && wordIndex < wordsList.size) {
            lettersPool = activeWord.toList().shuffled()
            answerLetters = emptyList()
            viewModel.speak("Siguiente palabra. Deletrea: " + getWordSpelled(activeWord, config.fontType))
        }
    }

    // Dynamic styled font styling
    val fontStyle = when (config.fontType) {
        "Ligada" -> FontFamily.Cursive
        "Imprenta" -> FontFamily.Serif
        else -> FontFamily.Monospace
    }

    // Gradient background extraction
    val themeBrush = when (config.backgroundStyle) {
        "Bosque Encantado" -> Brush.verticalGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)))
        "Espacio Cósmico" -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
        "Atardecer Suave" -> Brush.verticalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFED7AA)))
        "Pixel Clásico" -> Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF)))
        "Alto Contraste Retina" -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF121212)))
        "Mar Profundo" -> Brush.verticalGradient(listOf(Color(0xFF021B3E), Color(0xFF004D7A)))
        "Aura Aurora" -> Brush.verticalGradient(listOf(Color(0xFF0A0214), Color(0xFF163E30)))
        "Arena Dorada" -> Brush.verticalGradient(listOf(Color(0xFFFDFBF7), Color(0xFFF0E6D2)))
        "Nebulosa de Ensueño" -> Brush.verticalGradient(listOf(Color(0xFFFFF0F5), Color(0xFFEBE0FF)))
        else -> Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
    }

    val onThemeTextColor = if (
        config.backgroundStyle == "Espacio Cósmico" || 
        config.backgroundStyle == "Alto Contraste Retina" ||
        config.backgroundStyle == "Mar Profundo" ||
        config.backgroundStyle == "Aura Aurora"
    ) Color.White else Color.Black
    val contrastButtonColor = if (config.backgroundStyle == "Alto Contraste Retina") Color(0xFFFFEB3B) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(510.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(themeBrush)
            .border(3.dp, if (config.backgroundStyle == "Alto Contraste Retina") Color(0xFFFFEB3B) else Color.Transparent, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExit) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Salir", tint = onThemeTextColor)
                }
                Text(
                    text = "Juego Adaptado: " + config.title,
                    fontWeight = FontWeight.Black,
                    color = onThemeTextColor,
                    fontSize = 13.sp
                )
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        "Puntos: $currentScore XP",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (!gameCompleted) {
                // Game View logic
                if (config.gameType == "LETRAS") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Deletrea la palabra:",
                            color = onThemeTextColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Progress bars
                        Text(
                            "Palabra ${wordIndex + 1} de ${wordsList.size}",
                            color = onThemeTextColor.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )

                        // Word empty character Slots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            for (i in 0 until activeWord.length) {
                                val letter = answerLetters.getOrNull(i)
                                val convertedDisp = if (letter != null) {
                                    getCharByFont(letter, config.fontType)
                                } else "_"

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (letter != null) Color(0xFF10B981) else onThemeTextColor.copy(alpha = 0.15f))
                                        .border(2.dp, if (config.backgroundStyle == "Alto Contraste Retina") Color(0xFFFFEB3B) else Color.Transparent, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = convertedDisp,
                                        fontFamily = fontStyle,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (letter != null) Color.White else onThemeTextColor
                                    )
                                }
                            }
                        }

                        // Input letter pool
                        Text(
                            text = "Pulsa las letras en orden correcto:",
                            color = onThemeTextColor.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            lettersPool.forEachIndexed { index, char ->
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(contrastButtonColor)
                                        .clickable {
                                            viewModel.speak(char.toString())
                                            // Check matching order logic
                                            val expectedIndex = answerLetters.size
                                            if (activeWord.getOrNull(expectedIndex) == char) {
                                                val list = answerLetters.toMutableList()
                                                list.add(char)
                                                answerLetters = list

                                                // Remove from pool
                                                val rem = lettersPool.toMutableList()
                                                rem.removeAt(index)
                                                lettersPool = rem

                                                if (answerLetters.size == activeWord.length) {
                                                    // Finished active word
                                                    currentScore += 50
                                                    viewModel.speak("¡Bien hecho! Completaste la palabra.")
                                                    if (wordIndex + 1 < wordsList.size) {
                                                        wordIndex++
                                                    } else {
                                                        gameCompleted = true
                                                        viewModel.recordGameScore(
                                                            game = "PERSONALIZADO_LETRAS",
                                                            earnedScore = currentScore,
                                                            errorsCount = errorsCount,
                                                            speedSec = 30,
                                                            notes = "Escena profesor de Ortografía: ${config.title} con tipo de letra<sup>${config.fontType}</sup>."
                                                        )
                                                    }
                                                }
                                            } else {
                                                errorsCount++
                                                viewModel.speak("Intenta otra letra.")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = getCharByFont(char, config.fontType),
                                        fontFamily = fontStyle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (config.backgroundStyle == "Alto Contraste Retina") Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                } else if (config.gameType == "MATEMATICAS") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Suma visual interactiva adaptada:",
                            color = onThemeTextColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Mathematical Visual Representation with Pictos
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("$mathNum1", fontSize = 36.sp, fontFamily = fontStyle, fontWeight = FontWeight.Black, color = onThemeTextColor)
                            Text("+", fontSize = 28.sp, color = onThemeTextColor.copy(alpha = 0.5f))
                            Text("$mathNum2", fontSize = 36.sp, fontFamily = fontStyle, fontWeight = FontWeight.Black, color = onThemeTextColor)
                            Text("=", fontSize = 28.sp, color = onThemeTextColor.copy(alpha = 0.5f))
                            Text("?", fontSize = 36.sp, fontWeight = FontWeight.Black, color = contrastButtonColor)
                        }

                        // Pictorial Representation blocks
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Elements 1 blocks
                            Column {
                                for(i in 1..mathNum1) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(24.dp))
                                }
                            }
                            Text("y", color = onThemeTextColor.copy(alpha = 0.5f))
                            // Elements 2 blocks
                            Column {
                                for(i in 1..mathNum2) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Selection options
                        Text("Elige la respuesta correcta:", color = onThemeTextColor, fontSize = 12.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            mathOptions.forEach { opt ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(contrastButtonColor)
                                        .clickable {
                                            viewModel.speak(opt.toString())
                                            if (opt == mathCorrectAnswer) {
                                                currentScore += 100
                                                viewModel.speak("¡Extraordinario! $mathNum1 más $mathNum2 es igual a $mathCorrectAnswer.")
                                                gameCompleted = true
                                                viewModel.recordGameScore(
                                                    game = "PERSONALIZADO_MATEMATICAS",
                                                    earnedScore = currentScore,
                                                    errorsCount = errorsCount,
                                                    speedSec = 25,
                                                    notes = "Cálculo adaptado de Profesor: ${config.title}. Número límite: ${config.maxNumber}."
                                                )
                                            } else {
                                                errorsCount++
                                                viewModel.speak("Sigue sumando las estrellas.")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.toString(),
                                        fontFamily = fontStyle,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (config.backgroundStyle == "Alto Contraste Retina") Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // MEMORIA GAME
                    val memoryCards = remember(config) {
                        val emojis = listOf("🍎", "🐶", "🚀", "🍎", "🐶", "🚀").shuffled()
                        emojis.mapIndexed { index, emoji -> Pair(index, emoji) }
                    }
                    var flippedCards by remember { mutableStateOf(setOf<Int>()) }
                    var matchedCards by remember { mutableStateOf(setOf<Int>()) }
                    val scope = rememberCoroutineScope()
                    var isProcessing by remember { mutableStateOf(false) }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Empareja pictogramas sensoriales:",
                            color = onThemeTextColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Encuentra las 3 parejas de pictogramas interactivos.",
                            color = onThemeTextColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Match Grid of 2x3 cards
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(0..2, 3..5).forEach { range ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    range.forEach { idx ->
                                        val card = memoryCards[idx]
                                        val cardId = card.first
                                        val emoji = card.second
                                        val isFlipped = cardId in flippedCards || cardId in matchedCards

                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isFlipped) Color.White.copy(alpha = 0.9f) else contrastButtonColor)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (cardId in matchedCards) Color(0xFF10B981) else Color.Transparent,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    if (isProcessing || cardId in flippedCards || cardId in matchedCards) return@clickable
                                                    
                                                    viewModel.speak("Tarjeta revelada")
                                                    flippedCards = flippedCards + cardId
                                                    
                                                    if (flippedCards.size == 2) {
                                                        isProcessing = true
                                                        val firstId = flippedCards.first()
                                                        val secondId = flippedCards.last()
                                                        val firstEmoji = memoryCards[firstId].second
                                                        val secondEmoji = memoryCards[secondId].second
                                                        
                                                        if (firstEmoji == secondEmoji) {
                                                            matchedCards = matchedCards + firstId + secondId
                                                            flippedCards = emptySet()
                                                            currentScore += 30
                                                            viewModel.speak("¡Excelente! Has emparejado la figura.")
                                                            isProcessing = false
                                                            if (matchedCards.size == memoryCards.size) {
                                                                gameCompleted = true
                                                                viewModel.recordGameScore(
                                                                    game = "PERSONALIZADO_MEMORIA",
                                                                    earnedScore = currentScore + 50,
                                                                    errorsCount = errorsCount,
                                                                    speedSec = 20,
                                                                    notes = "Asociación de memoria de Profesor: ${config.title}."
                                                                )
                                                            }
                                                        } else {
                                                            scope.launch {
                                                                delay(1200)
                                                                errorsCount++
                                                                flippedCards = emptySet()
                                                                isProcessing = false
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isFlipped) {
                                                Text(emoji, fontSize = 32.sp)
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.QuestionMark,
                                                    contentDescription = null,
                                                    tint = if (config.backgroundStyle == "Alto Contraste Retina") Color.Black else Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Completed Success Card
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¡ESPECTACULAR! 🎉", color = onThemeTextColor, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    Text("Has terminado con éxito esta rutina personalizada.", color = onThemeTextColor.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(containerColor = onThemeTextColor, contentColor = if (onThemeTextColor == Color.White) Color.Black else Color.White)
                    ) {
                        Text("Regresar al aula", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Map char values dynamic casing
private fun getCharByFont(char: Char, fontType: String): String {
    return when (fontType) {
        "Minúsculas", "Ligada" -> char.lowercaseChar().toString()
        else -> char.uppercaseChar().toString()
    }
}

private fun getWordSpelled(word: String, fontType: String): String {
    return if (fontType == "Minúsculas") word.lowercase() else word.uppercase()
}
