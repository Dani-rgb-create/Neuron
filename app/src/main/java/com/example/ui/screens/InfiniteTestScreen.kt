package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NeuronViewModel
import kotlinx.coroutines.delay

@Composable
fun InfiniteTestScreen(viewModel: NeuronViewModel) {
    val activeStudent by viewModel.activeStudent.collectAsState()

    var isTestRunning by remember { mutableStateOf(false) }

    // Test Config states
    var selectedLanguage by remember { mutableStateOf(true) }
    var selectedMath by remember { mutableStateOf(true) }
    var selectedLogic by remember { mutableStateOf(true) }
    var selectedEmotion by remember { mutableStateOf(true) }
    var selectedAutonomy by remember { mutableStateOf(true) }

    var testComplexity by remember { mutableStateOf("Medio") } // Inicial, Medio, Avanzado
    var durationLimitsMinutes by remember { mutableStateOf(3) } // 1, 2, 3, 5 Minutos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isTestRunning) {
            // Evaluator player is active
            InfiniteTestRunner(
                selectedCategories = listOfNotNull(
                    if (selectedLanguage) "LENGUAJE" else null,
                    if (selectedMath) "MATEMATICAS" else null,
                    if (selectedLogic) "LOGICA" else null,
                    if (selectedEmotion) "EMOCIONES" else null,
                    if (selectedAutonomy) "AUTONOMIA" else null
                ),
                complexity = testComplexity,
                timeLimitMins = durationLimitsMinutes,
                viewModel = viewModel,
                onFinish = { isTestRunning = false }
            )
        } else {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Psychology, tint = MaterialTheme.colorScheme.primary, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sistema de Tests Infinitos 🎯",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Evaluación psicopedagógica dinámica. Genera problemas infinitos según las áreas seleccionadas, alimentando el dossier analítico de la Base de Datos sin repetirse jamás.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Options Selector panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "1. Seleccionar Áreas de Evaluación en tiempo real:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )

                    // Target check options
                    CategoryItemRow("Lenguaje, Lectoescritura y Fonología 🗣️", selectedLanguage) { selectedLanguage = it }
                    CategoryItemRow("Cálculo, Operaciones y Números 🔢", selectedMath) { selectedMath = it }
                    CategoryItemRow("Lógica, Atención Sostenida e Intrusos 🔍", selectedLogic) { selectedLogic = it }
                    CategoryItemRow("Habilidades Emocionales y Sociales 🎭", selectedEmotion) { selectedEmotion = it }
                    CategoryItemRow("Autonomía y Decisiones Prácticas 🗺️", selectedAutonomy) { selectedAutonomy = it }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        "2. Nivel de Complejidad Dinámica del Test:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Inicial", "Medio", "Avanzado").forEach { level ->
                            val isSel = testComplexity == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { testComplexity = level }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(level, fontWeight = FontWeight.Black, fontSize = 12.sp)
                             }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        "3. Duración Límite de la Evaluación:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tiempo de Test:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3, 5).forEach { mins ->
                                val isSel = durationLimitsMinutes == mins
                                ElevatedFilterChip(
                                    selected = isSel,
                                    onClick = { durationLimitsMinutes = mins },
                                    label = { Text("$mins Minutos") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val startEnabled = selectedLanguage || selectedMath || selectedLogic || selectedEmotion || selectedAutonomy
                    Button(
                        onClick = {
                            isTestRunning = true
                            viewModel.speak("Iniciando una nueva sesión evaluativa de nivel $testComplexity. Presta mucha atención.")
                        },
                        enabled = startEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_test_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Test Vocacional e Infinito 🚀", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItemRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ----------------------------------------------------
// THE INFINITE EXAM RUNNER ENGINE
// ----------------------------------------------------
@Composable
fun InfiniteTestRunner(
    selectedCategories: List<String>,
    complexity: String,
    timeLimitMins: Int,
    viewModel: NeuronViewModel,
    onFinish: () -> Unit
) {
    if (selectedCategories.isEmpty()) {
        onFinish()
        return
    }

    var score by remember { mutableStateOf(0) }
    var errorsCount by remember { mutableStateOf(0) }
    var currentQuestionIdx by remember { mutableStateOf(1) }
    var timeLeftSeconds by remember { mutableStateOf(timeLimitMins * 60) }
    var isRunning by remember { mutableStateOf(true) }

    // Generador de preguntas infinitas en base a templates y matemáticas de semilla random
    val randomSeed = remember { mutableStateOf(kotlin.random.Random.nextInt()) }
    var activeQuestion by remember { mutableStateOf<GeneratedQuestion?>(null) }

    // Dynamic countdown timer
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (timeLeftSeconds > 0 && isRunning) {
            delay(1000)
            timeLeftSeconds--
        }
        if (timeLeftSeconds <= 0 && isRunning) {
            isRunning = false
            viewModel.speak("El tiempo ha concluido. Procesando resultados para el análisis clínico.")
            // Save test report to DB
            viewModel.recordGameScore(
                game = "TEST_EVALUACION",
                earnedScore = score,
                errorsCount = errorsCount,
                speedSec = timeLimitMins * 60,
                notes = "Examen de evaluación infinita: Células cognitivas tratadas de nivel $complexity. Áreas evaluadas: ${selectedCategories.joinToString(", ")}. Respuestas acertadas: ${currentQuestionIdx}. Errores identificados: $errorsCount."
            )
        }
    }

    // Dynamic Infinite question dispenser loop
    LaunchedEffect(currentQuestionIdx, randomSeed.value) {
        val category = selectedCategories.random()
        activeQuestion = generateInfiniteQuestion(category, complexity)
        // Speak question immediately to aid non-verbal visual scanning
        activeQuestion?.let { q ->
            viewModel.speak(q.promptText)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats bar during exam
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nivel: $complexity 📊", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Sesión Infinita", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Clock countdown card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (timeLeftSeconds < 20) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    val minutesStr = timeLeftSeconds / 60
                    val secondsStr = "%02d".format(timeLeftSeconds % 60)
                    Text(
                        text = "⏱️ $minutesStr:$secondsStr",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (timeLeftSeconds < 20) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            AnimatedContent(
                targetState = isRunning,
                label = "exam_results_fade"
            ) { active ->
                if (active && activeQuestion != null) {
                    val q = activeQuestion!!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Current progress
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pregunta #$currentQuestionIdx", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("Aciertos: $score XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }

                        LinearProgressIndicator(
                            progress = (currentQuestionIdx.toFloat() / 15f).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        // Pictogram category symbol helper card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(q.categoryTag, fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = q.promptText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        // Big Visual Illustration (If specified)
                        if (q.optionalVisualGraphic != null) {
                            Text(
                                text = q.optionalVisualGraphic,
                                fontSize = 48.sp,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selection list options (highly visible)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            q.options.forEach { option ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.speak(option)
                                            if (option == q.correctAnswer) {
                                                score += 15
                                                viewModel.speak("¡Respuesta correcta!")
                                                currentQuestionIdx++
                                                randomSeed.value = kotlin.random.Random.nextInt()
                                            } else {
                                                errorsCount++
                                                viewModel.speak("No coincide. Pulsa otra opción.")
                                            }
                                        }
                                        .testTag("exam_option_item"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = option,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Test Complete details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Éxito", tint = Color(0xFFFFD700), modifier = Modifier.size(72.dp))
                        Text(
                            "¡Rutina Diagnóstica Completada!",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "El test infinito ha retroalimentado el dossier clínico. Los patrones sensoriales y tasas de impulsividad han sido almacenados localmente.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Puntuación Final: $score XP", fontWeight = FontWeight.Black)
                                Text("Errores de ejecución: $errorsCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onFinish,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cerrar y ver Análisis en Dossier", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// THE DEEP UN-REPETITIVE INFINITE GENERATOR
// ----------------------------------------------------
data class GeneratedQuestion(
    val categoryTag: String,
    val promptText: String,
    val options: List<String>,
    val correctAnswer: String,
    val optionalVisualGraphic: String? = null
)

private fun generateInfiniteQuestion(category: String, complexity: String): GeneratedQuestion {
    val r = kotlin.random.Random
    return when (category) {
        "LENGUAJE" -> {
            val wordsGroup = listOf(
                Pair("casa", "C"), Pair("luna", "L"), Pair("sopa", "S"), Pair("mama", "M"),
                Pair("perro", "P"), Pair("gato", "G"), Pair("sol", "S"), Pair("boca", "B"),
                Pair("nene", "N"), Pair("dedo", "D"), Pair("fuego", "F"), Pair("reloj", "R"),
                Pair("tren", "T"), Pair("vaca", "V"), Pair("pato", "P"), Pair("globo", "G"),
                Pair("llave", "Ll"), Pair("piña", "P"), Pair("queso", "Q"), Pair("zapato", "Z")
            )
            val selected = wordsGroup.random()
            val incorrectLetters = ('A'..'Z').filter { it.toString() != selected.second }.shuffled().take(3).map { it.toString() }
            val options = (incorrectLetters + selected.second).shuffled()

            GeneratedQuestion(
                categoryTag = "LENGUAJE Y FONOLOGÍA 🗣️",
                promptText = "¿Con qué letra empieza la palabra '${selected.first.uppercase()}'?",
                options = options,
                correctAnswer = selected.second,
                optionalVisualGraphic = when(selected.first) {
                    "casa" -> "🏠"
                    "luna" -> "🌙"
                    "sopa" -> "🍲"
                    "mama" -> "👩"
                    "perro" -> "🐕"
                    "gato" -> "🐈"
                    "sol" -> "☀️"
                    "reloj" -> "⏰"
                    "tren" -> "🚂"
                    "vaca" -> "🐄"
                    "pato" -> "🦆"
                    "globo" -> "🎈"
                    "llave" -> "🔑"
                    "piña" -> "🍍"
                    "queso" -> "🧀"
                    else -> "👟"
                }
            )
        }
        "MATEMATICAS" -> {
            val numA: Int
            val numB: Int
            val operator: String
            val correctVal: Int
            val prompt: String
            val graphic: String

            if (complexity == "Avanzado" && r.nextBoolean()) {
                val start = r.nextInt(1, 12)
                val step = r.nextInt(2, 6)
                val seq = listOf(start, start + step, start + 2 * step, start + 3 * step)
                correctVal = start + 4 * step
                prompt = "Completa la serie numérica: ${seq.joinToString(", ")}, ¿qué número sigue?"
                graphic = "📈"
            } else {
                numA = when (complexity) {
                    "Inicial" -> r.nextInt(1, 5)
                    "Medio" -> r.nextInt(2, 10)
                    else -> r.nextInt(10, 25)
                }
                numB = when (complexity) {
                    "Inicial" -> r.nextInt(1, 4)
                    "Medio" -> r.nextInt(2, 9)
                    else -> r.nextInt(5, 15)
                }
                operator = if (complexity == "Inicial") "+" else listOf("+", "-").random()
                correctVal = if (operator == "+") numA + numB else (numA - numB).coerceAtLeast(1)
                prompt = "Resuelve la operación: $numA $operator $numB = ?"
                graphic = "⭐".repeat(numA.coerceAtMost(10)) + (if (operator == "+") " y " else " quitas ") + "⭐".repeat(numB.coerceAtMost(10))
            }

            val wrongAnswers = listOf(correctVal + 1, correctVal + 2, (correctVal - 1).coerceAtLeast(0), correctVal + 3, correctVal - 2)
                .distinct()
                .filter { it != correctVal && it >= 0 }
                .take(3)
            val options = (wrongAnswers + correctVal).map { it.toString() }.shuffled()

            GeneratedQuestion(
                categoryTag = "CÁLCULO Y NÚMEROS 🔢",
                promptText = prompt,
                options = options,
                correctAnswer = correctVal.toString(),
                optionalVisualGraphic = graphic
            )
        }
        "LOGICA" -> {
            val intruders = listOf(
                QuadrilateralIntruder("Un zapato NO pertenece porque no se come:", listOf("Plátano 🍌", "Manzana 🍎", "Zapato 👟", "Galleta 🍪"), "Zapato 👟"),
                QuadrilateralIntruder("Un coche NO pertenece al grupo de animales:", listOf("Gato 🐈", "Perro 🐕", "Loro 🦜", "Vehículo 🚗"), "Vehículo 🚗"),
                QuadrilateralIntruder("Un pez NO vuela por el aire con las alas:", listOf("Pez 🐟", "Avión ✈️", "Pájaro 🐦", "Mariposa 🦋"), "Pez 🐟"),
                QuadrilateralIntruder("Un libro NO pertenece a los utensilios de cocina:", listOf("Cuchara 🥄", "Tenedor 🍴", "Libro 📚", "Plato 🍽️"), "Libro 📚"),
                QuadrilateralIntruder("El sol NO se usa para asearse en el baño:", listOf("Cepillo 🪥", "Jabón 🧼", "Sol ☀️", "Toalla 🧖"), "Sol ☀️"),
                QuadrilateralIntruder("El helado NO es una prenda de abrigo para invierno:", listOf("Helado 🍦", "Chaqueta 🧥", "Gorro 🧣", "Guantes 🧤"), "Helado 🍦"),
                QuadrilateralIntruder("Un lápiz NO es un juguete de recreo infantil:", listOf("Muñeca 🪆", "Pelota ⚽", "Paseo en Tobogán 🛝", "Lápiz ✏️"), "Lápiz ✏️"),
                QuadrilateralIntruder("La manzana NO es un medio de transporte:", listOf("Tren 🚄", "Barco 🚢", "Manzana 🍎", "Bicicleta 🚲"), "Manzana 🍎")
            ).random()

            GeneratedQuestion(
                categoryTag = "LÓGICA Y ATENCIÓN SOSTENIDA 🔍",
                promptText = intruders.desc,
                options = intruders.options,
                correctAnswer = intruders.correct,
                optionalVisualGraphic = "🧐"
            )
        }
        "EMOCIONES" -> {
            val emotionalScenarios = listOf(
                Pair("Si un amigo te regala un juguete nuevo, ¿cómo te sientes?", listOf("Alegre 😊", "Triste 😢", "Enfadado 😡", "Frustrado 😠")),
                Pair("Si te caes en el recreo y te haces daño en la rodilla, te sientes...", listOf("Dolorido 😭", "Sorprendido 😲", "Contento 😄", "Aburrido 🥱")),
                Pair("Si hay mucho ruido fuerte a tu alrededor, puedes sentirte...", listOf("Agobiado 🤯", "Calmado 😌", "Divertido 🤪", "Cansado 😴")),
                Pair("Si un compañero coge un lápiz sin preguntar, ¿cómo puedes sentirte?", listOf("Frustrado 😠", "Feliz 😁", "Asustado 😱", "Indiferente 😐")),
                Pair("Si vas con tus papás a un parque de atracciones increíble, te sientes...", listOf("Emocionado 🤩", "Triste 😢", "Aburrido 🥱", "Cansado 😴")),
                Pair("Si pierdes tu peluche favorito para dormir, te sientes...", listOf("Triste 😢", "Alegre 😄", "Enfadado 😡", "Entusiasmado 🤩")),
                Pair("Si alguien te da un abrazo suave cuando estás preocupado, te sientes...", listOf("Calmado 😌", "Molesto 😤", "Asustado 😨", "Aburrido 🥱"))
            ).random()

            GeneratedQuestion(
                categoryTag = "INTELIGENCIA EMOCIONAL 🎭",
                promptText = emotionalScenarios.first,
                options = emotionalScenarios.second.shuffled(),
                correctAnswer = emotionalScenarios.second[0],
                optionalVisualGraphic = "💬"
            )
        }
        else -> {
            // AUTONOMIA
            val autonomyScenario = listOf(
                Pair("Antes de cenar en la mesa con tus papás, ¿qué debes hacer?", listOf("Lavar las manos 🧼", "Jugar fútbol ⚽", "Dormir en cama 🛌", "Ver televisión 📺")),
                Pair("¿Dónde debes cruzar de forma segura una calle de coches?", listOf("Paso de peatones 🚶", "Corriendo por el medio 🏃", "Detrás de un camión 🚚", "Saltando la valla 🚧")),
                Pair("Si terminas de comer una manzana, ¿dónde tiras el centro?", listOf("Papelera de basura 🗑️", "Al suelo del salón 🧹", "Debajo del sofá 🛋️", "Por la ventana 🪟")),
                Pair("Para mantener sanos los dientes y prevenir caries, usamos...", listOf("Cepillo y pasta 🪥", "Jabón de manos 🧴", "Peine de cabello 🪮", "Toalla de ducha 🧼")),
                Pair("Si de pronto empieza a llover fuerte al salir a la calle, ¿qué usas?", listOf("Paraguas 🌂", "Gafas de sol 🕶️", "Molinillo de viento 🪁", "Pala de arena 🪣")),
                Pair("Al despertarte por la mañana nos quitamos el pijama y nos vestimos con...", listOf("Ropa limpia 👕", "Bañador de piscina 🩱", "Disfraz de Halloween 👹", "Botas de nieve 🥾")),
                Pair("Al terminar de ir al baño a hacer nuestras necesidades, es indispensable...", listOf("Tirar de la cadena y lavarse las manos 🧼", "Escribir en un papel 📝", "Salir corriendo a jugar 🏃", "Poner música alta 🎵"))
            ).random()

            GeneratedQuestion(
                categoryTag = "AUTONOMÍA Y DECISIONES 🗺️",
                promptText = autonomyScenario.first,
                options = autonomyScenario.second.shuffled(),
                correctAnswer = autonomyScenario.second[0],
                optionalVisualGraphic = "🏠"
            )
        }
    }
}

data class QuadrilateralIntruder(
    val desc: String,
    val options: List<String>,
    val correct: String
)
