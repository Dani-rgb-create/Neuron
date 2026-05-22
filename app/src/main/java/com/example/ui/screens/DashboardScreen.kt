package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AgendaItem
import com.example.data.Pictogram
import com.example.data.SavedCanvas
import com.example.ui.NeuronViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: NeuronViewModel) {
    val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
    val activeTab by viewModel.currentTab.collectAsStateWithLifecycle()
    
    val lowSensory = activeStudent?.lowSensoryMode == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Student small avatar badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getStudentAvatarIcon(activeStudent?.avatarPreset ?: 0),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                activeStudent?.name ?: "Uso Clínico",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                "Nivel: ${activeStudent?.cognitiveLevel ?: "General"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.deselectStudent() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver a alumnos")
                    }
                },
                actions = {
                    // Dynamic EU Language Dropdown
                    var showLangMenu by remember { mutableStateOf(false) }
                    val currentLang by viewModel.activeLanguage.collectAsStateWithLifecycle()
                    val langFlags = mapOf(
                        "ES" to "🇪🇸",
                        "EN" to "🇬🇧",
                        "FR" to "🇫🇷",
                        "DE" to "🇩🇪",
                        "IT" to "🇮🇹",
                        "PT" to "🇵🇹",
                        "PL" to "🇵🇱"
                    )
                    
                    Box {
                        TextButton(
                            onClick = { showLangMenu = true },
                            modifier = Modifier.testTag("lang_selector_dropdown")
                        ) {
                            Text(
                                text = "${langFlags[currentLang]} $currentLang",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            langFlags.forEach { (code, flag) ->
                                DropdownMenuItem(
                                    text = { Text("$flag $code") },
                                    onClick = {
                                        viewModel.setLanguage(code)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Sensory control badge (Soothing Low sensory indicator)
                    IconButton(
                        onClick = { viewModel.toggleSensoryMode() },
                        modifier = Modifier.testTag("sensory_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (lowSensory) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Control sensorial",
                            tint = if (lowSensory) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                listOf(
                    NavigationTabItem("AGENDA", viewModel.getUiTranslation("agenda"), Icons.Filled.CalendarMonth),
                    NavigationTabItem("CAA", viewModel.getUiTranslation("caa"), Icons.Filled.Forum),
                    NavigationTabItem("JUEGOS", viewModel.getUiTranslation("juegos"), Icons.Filled.Extension),
                    NavigationTabItem("ANALYTICS", viewModel.getUiTranslation("analiticas"), Icons.Filled.Leaderboard),
                    NavigationTabItem("OCIO", viewModel.getUiTranslation("ocio"), Icons.Filled.SportsEsports)
                ).forEach { tab ->
                    val isSelected = activeTab == tab.key
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { 
                            viewModel.speak(tab.label)
                            viewModel.selectTab(tab.key)
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("nav_tab_${tab.key}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "AGENDA" -> AgendaVisualTab(viewModel)
                "CAA" -> CaaComunicatorTab(viewModel)
                "JUEGOS" -> EducativeGames(viewModel)
                "ANALYTICS" -> LocalAnalyticsTab(viewModel)
                "OCIO" -> NeuronJumper(viewModel)
            }
        }
    }
}

data class NavigationTabItem(
    val key: String,
    val label: String,
    val icon: ImageVector
)

// Helper avatar mappings
fun getStudentAvatarIcon(preset: Int): ImageVector {
    val list = listOf(
        Icons.Filled.Face,
        Icons.Filled.EmojiEmotions,
        Icons.Filled.FaceRetouchingNatural,
        Icons.Filled.Pets,
        Icons.Filled.ChildCare,
        Icons.Filled.Star,
        Icons.Filled.WorkspacePremium,
        Icons.Filled.Favorite
    )
    return list[preset % list.size]
}

// ==========================================
// A. TAB AGENDA VISUAL COMPORTAMENTAL
// ==========================================
@Composable
fun AgendaVisualTab(viewModel: NeuronViewModel) {
    val items by viewModel.agendaItems.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    
    var title by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("09:00") }
    var selectedIcon by remember { mutableStateOf("school") }
    var selectedColorIdx by remember { mutableStateOf(0) }

    val presetIcons = listOf("school", "restaurant", "wc", "wash", "brush", "bed", "videogame_asset", "directions_walk")
    val colorsList = listOf("#FF3B82F6", "#FFF59E0B", "#FF10B981", "#FFEC4899", "#FF8B5CF6", "#FFE11D48")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Agenda Visual Diaria", 
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    "Visualiza e incrementa la autonomía estructurada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape).testTag("add_agenda_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir rutina", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        // Immersive UI: Quick Stats Card for Today's Progress
        val completedCount = items.count { it.isCompleted }
        val percent = if (items.isNotEmpty()) (completedCount * 100) / items.size else 0
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("immersive_stats_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "PROGRESO HOY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 36.sp
                        )
                    )
                    Text(
                        text = "$completedCount de ${items.size} completados • Offline Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    )
                }

                // Visual Spark Bars
                Row(
                    modifier = Modifier.height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.40f, 0.60f, 0.55f, 0.90f, 0.85f)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .fillMaxHeight(h)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    "No hay rutinas asignadas para hoy.", 
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agenda_card_${item.id}"),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, if (item.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clock time badge
                        Text(
                            text = item.timeOfDay,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(60.dp)
                        )

                        // Visual icon badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(android.graphics.Color.parseColor(item.colorHex)).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = mapStringToVector(item.iconName),
                                contentDescription = null,
                                tint = Color(android.graphics.Color.parseColor(item.colorHex)),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Checked routine completion
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    viewModel.speak("¡Felicidades! Has completado " + item.title)
                                }
                                viewModel.completeAgendaItem(item.id, isChecked)
                            },
                            modifier = Modifier.testTag("agenda_check_${item.id}")
                        )

                        IconButton(
                            onClick = { viewModel.removeAgendaItem(item.id) },
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nueva Actividad Visual", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título de la Rutina") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = { timeOfDay = it },
                        label = { Text("Hora (ejem 10:30)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pictograma Visual", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetIcons.forEach { ic ->
                            val isSel = selectedIcon == ic
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedIcon = ic }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(mapStringToVector(ic), contentDescription = null, tint = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }

                    Text("Color de Categoría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        colorsList.forEachIndexed { idx, col ->
                            val isSel = selectedColorIdx == idx
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .border(2.dp, if (isSel) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                    .clickable { selectedColorIdx = idx }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.addAgendaItem(title, timeOfDay, selectedIcon, colorsList[selectedColorIdx])
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Añadir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Atrás") }
            }
        )
    }
}

// Map database strings to Material icons
fun mapStringToVector(name: String): ImageVector {
    return when (name.lowercase()) {
        "school" -> Icons.Filled.School
        "restaurant" -> Icons.Filled.Restaurant
        "wc", "bathroom" -> Icons.Filled.Wc
        "wash" -> Icons.Filled.Wash
        "brush" -> Icons.Filled.Brush
        "bed", "bedtime" -> Icons.Filled.Bedtime
        "videogame_asset" -> Icons.Filled.VideogameAsset
        "directions_walk" -> Icons.Filled.DirectionsWalk
        "nutrition" -> Icons.Filled.Spa
        "coffee" -> Icons.Filled.LocalCafe
        "cookie" -> Icons.Filled.Cake
        "check_circle" -> Icons.Filled.CheckCircle
        "cancel" -> Icons.Filled.Cancel
        "favorite" -> Icons.Filled.Favorite
        "volunteer_activism" -> Icons.Filled.VolunteerActivism
        "home" -> Icons.Filled.Home
        "sick" -> Icons.Filled.Sick
        "music_note" -> Icons.Filled.MusicNote
        "sentiment_very_satisfied" -> Icons.Filled.SentimentVerySatisfied
        "sentiment_dissatisfied" -> Icons.Filled.SentimentDissatisfied
        "sentiment_very_dissatisfied" -> Icons.Filled.SentimentVeryDissatisfied
        "touch_app" -> Icons.Filled.TouchApp
        
        // Expanded Pictograms: Actions & Routine
        "directions_run" -> Icons.Filled.DirectionsRun
        "hearing" -> Icons.Filled.Hearing
        "record_voice_over" -> Icons.Filled.RecordVoiceOver
        "stop_circle" -> Icons.Filled.StopCircle
        "visibility" -> Icons.Filled.Visibility
        "shopping_cart" -> Icons.Filled.ShoppingCart
        "history_edu" -> Icons.Filled.HistoryEdu
        "shower" -> Icons.Filled.Bathtub
        "checkroom" -> Icons.Filled.Checkroom
        
        // Expanded Pictograms: Food
        "water_drop" -> Icons.Filled.WaterDrop
        "bakery_dining" -> Icons.Filled.BakeryDining
        "soup_kitchen" -> Icons.Filled.SoupKitchen
        "local_drink" -> Icons.Filled.LocalDrink
        
        // Expanded Pictograms: Feelings / Aux
        "warning" -> Icons.Filled.Warning
        "snooze" -> Icons.Filled.Snooze
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "help_center" -> Icons.Filled.HelpCenter
        "person_search" -> Icons.Filled.PersonSearch
        "waving_hand" -> Icons.Filled.WavingHand
        "back_hand" -> Icons.Filled.BackHand
        
        // Expanded Pictograms: General & Family
        "elderly" -> Icons.Filled.Elderly
        "elderly_woman" -> Icons.Filled.ElderlyWoman
        "sports_soccer" -> Icons.Filled.SportsSoccer
        "menu_book" -> Icons.Filled.MenuBook
        "directions_car" -> Icons.Filled.DirectionsCar
        "wb_sunny" -> Icons.Filled.WbSunny
        "umbrella" -> Icons.Filled.Umbrella
        
        // Letras y Números
        "abc" -> Icons.Filled.SortByAlpha
        "numbers" -> Icons.Filled.Dialpad
        "looks_one" -> Icons.Filled.LooksOne
        "looks_two" -> Icons.Filled.LooksTwo
        "looks_3" -> Icons.Filled.Looks3
        "looks_4" -> Icons.Filled.Looks4
        "looks_5" -> Icons.Filled.Looks5
        "looks_6" -> Icons.Filled.Looks6
        
        else -> Icons.Filled.Category
    }
}

// ==========================================
// B. TAB LIENZO DE COMUNICACIÓN AUGMENTATIVE (CAA)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaaComunicatorTab(viewModel: NeuronViewModel) {
    val pictos by viewModel.pictograms.collectAsStateWithLifecycle()
    val phraseList by viewModel.phraseCanvas.collectAsStateWithLifecycle()
    val templates by viewModel.savedCanvases.collectAsStateWithLifecycle()
    val currentLang by viewModel.activeLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TODAS") }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var templateTitle by remember { mutableStateOf("") }

    // Custom picto creation
    var showCreator by remember { mutableStateOf(false) }
    var customLabel by remember { mutableStateOf("") }
    var customSpeech by remember { mutableStateOf("") }
    var creatorCategory by remember { mutableStateOf("ACCION") }
    var creatorColorHex by remember { mutableStateOf("#FF4CAF50") }
    var creatorIcon by remember { mutableStateOf("touch_app") }

    // Custom picto EDITING & Familar voice recorder/modifiers
    var showEditDialog by remember { mutableStateOf(false) }
    var editingPicto by remember { mutableStateOf<Pictogram?>(null) }
    var editLabel by remember { mutableStateOf("") }
    var editSpeech by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("ACCION") }
    var editColorHex by remember { mutableStateOf("#FF4CAF50") }
    var editIconName by remember { mutableStateOf("touch_app") }

    // Voice simulation & recording parameters
    val isRecordingActive by viewModel.isVoiceRecordingActive.collectAsStateWithLifecycle()
    val recordedPaths by viewModel.recordedAudioPathsByPictoId.collectAsStateWithLifecycle()
    var customVoicePitch by remember { mutableStateOf(1.0f) }
    var customVoiceRate by remember { mutableStateOf(1.0f) }

    val categories = listOf("TODAS", "ACCION", "ALIMENTO", "HIGIENE", "EMOCION", "PREGUNTAS", "GENERAL")
    
    val categoryColors = mapOf(
        "ACCION" to Color(0xFF4CAF50),
        "ALIMENTO" to Color(0xFFFF9800),
        "HIGIENE" to Color(0xFF2196F3),
        "EMOCION" to Color(0xFF9C27B0),
        "PREGUNTAS" to Color(0xFFE91E63),
        "GENERAL" to Color(0xFF795548)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Build active phrase visual canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                .testTag("phrase_canvas_panel")
        ) {
            if (phraseList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Lienzo de Expresión Verbal", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Toca los pictogramas de abajo para construir tu frase.", 
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(phraseList) { index, pic ->
                            Card(
                                onClick = { viewModel.removePictoFromCanvas(index) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(android.graphics.Color.parseColor(pic.colorHex)).copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, Color(android.graphics.Color.parseColor(pic.colorHex)).copy(alpha = 0.5f)),
                                modifier = Modifier.size(90.dp).testTag("canvas_item_$index")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = mapStringToVector(pic.presetIconName ?: "category"),
                                        contentDescription = null,
                                        tint = Color(android.graphics.Color.parseColor(pic.colorHex)),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Text(
                                        text = pic.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Execution Controls
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.playCanvasSequence() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(42.dp).testTag("canvas_play_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir", tint = Color.White)
                        }
                        IconButton(
                            onClick = { viewModel.clearCanvas() },
                            modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, CircleShape).size(42.dp).testTag("canvas_clear_btn")
                        ) {
                            Icon(Icons.Filled.Clear, contentDescription = "Borrar lienzo", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Sub and templates row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showCreator = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("custom_picto_creator_btn")
            ) {
                Icon(Icons.Filled.Create, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear Picto", fontWeight = FontWeight.Bold)
            }

            if (phraseList.isNotEmpty()) {
                TextButton(onClick = { showTemplateDialog = true }) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar Plantilla", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar pictograma en la biblioteca local...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Horizontal scrolling category list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSel = selectedCategory == cat
                ElevatedFilterChip(
                    selected = isSel,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    modifier = Modifier.testTag("cat_chip_$cat")
                )
            }
        }

        // RENDER SAVED LIST TEMPLATES IF ANY
        if (templates.isNotEmpty()) {
            Text("Tus Plantillas Rápidas:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(templates) { template ->
                    ElevatedSuggestionChip(
                        onClick = { viewModel.loadCanvasTemplate(template) },
                        label = { Text(template.title) },
                        icon = { Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.testTag("template_chip_${template.id}")
                    )
                }
            }
        }

        // Dict main grid
        val filtered = pictos.filter { pic ->
            (selectedCategory == "TODAS" || pic.category == selectedCategory) &&
            pic.label.lowercase().contains(searchQuery.lowercase())
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).testTag("presets_grid"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { pic ->
                val designColor = Color(android.graphics.Color.parseColor(pic.colorHex))
                Card(
                    onClick = { viewModel.addPictoToCanvas(pic) },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, designColor.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .height(130.dp)
                        .testTag("picto_card_${pic.id}")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Icon preset draw styled like the HTML template
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(designColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = mapStringToVector(pic.presetIconName ?: "help"),
                                    contentDescription = null,
                                    tint = designColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            val dLabel = viewModel.pictogramTranslations[pic.label]?.get(currentLang) ?: pic.label
                            Text(
                                text = dLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,maxLines = 1
                            )
                        }

                        // Pencil icon to edit Pictogram Name & Configure familiar voice
                        IconButton(
                            onClick = {
                                editingPicto = pic
                                editLabel = pic.label
                                editSpeech = pic.speechText
                                editCategory = pic.category
                                editColorHex = pic.colorHex
                                editIconName = pic.presetIconName ?: "touch_app"
                                showEditDialog = true
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar",
                                tint = designColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Template popup
    if (showTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = { Text("Guardar Plantilla de Comunicación", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = templateTitle,
                    onValueChange = { templateTitle = it },
                    label = { Text("Nombre de la Plantilla (ej: Comer Manzana)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateTitle.isNotEmpty()) {
                            viewModel.saveActiveCanvasTemplate(templateTitle)
                            templateTitle = ""
                            showTemplateDialog = false
                        }
                    }
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) { Text("Atrás") }
            }
        )
    }

    // Creator dialog
    if (showCreator) {
        val pickerColors = listOf("#FF4CAF50", "#FFFF9800", "#FF2196F3", "#FF9C27B0", "#FFE91E63", "#FF795548")
        val iconOptions = listOf("touch_app", "wc", "restaurant", "bedtime", "favorite", "home", "school", "music_note")
        
        AlertDialog(
            onDismissRequest = { showCreator = false },
            title = { Text("Fabricador de Pictograma 🎨", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Nombre / Etiqueta") },
                        modifier = Modifier.fillMaxWidth().testTag("creator_label_field")
                    )

                    OutlinedTextField(
                        value = customSpeech,
                        onValueChange = { customSpeech = it },
                        label = { Text("Frase que hablará (ej: Tengo sed)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Categoría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categoryColors.keys.forEach { cat ->
                            val isSel = creatorCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) categoryColors[cat]!! else Color.LightGray)
                                    .clickable { creatorCategory = cat }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(cat, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("Símbolo Pictográfico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    FlowRow(maxItemsInEachRow = 4, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        iconOptions.forEach { ic ->
                            val isSel = creatorIcon == ic
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { creatorIcon = ic }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(mapStringToVector(ic), contentDescription = null, tint = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }

                    Text("Color Visual", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        pickerColors.forEach { col ->
                            val isSel = creatorColorHex == col
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .border(2.dp, if (isSel) Color.Black else Color.Transparent, CircleShape)
                                    .clickable { creatorColorHex = col }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customLabel.isNotEmpty()) {
                            viewModel.addCustomPictogram(
                                label = customLabel,
                                phrase = customSpeech,
                                category = creatorCategory,
                                colorHex = creatorColorHex,
                                iconName = creatorIcon,
                                imagePath = null
                            )
                            customLabel = ""
                            customSpeech = ""
                            showCreator = false
                        }
                    },
                    modifier = Modifier.testTag("creator_submit")
                ) {
                    Text("Crear Pictograma", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreator = false }) { Text("Atrás") }
            }
        )
    }

    // Edit Dialog (Edit name, speechText and simulate/record familiar voice offline)
    if (showEditDialog && editingPicto != null) {
        val pic = editingPicto!!
        val pickerColors = listOf("#FF4CAF50", "#FFFF9800", "#FF2196F3", "#FF9C27B0", "#FFE91E63", "#FF795548")
        val iconOptions = listOf("touch_app", "wc", "restaurant", "bedtime", "favorite", "home", "school", "music_note")
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = viewModel.getUiTranslation("edit_picto"),
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = editLabel,
                        onValueChange = { editLabel = it },
                        label = { Text("Nombre / Etiqueta del Pictograma") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_picto_label_input")
                    )

                    OutlinedTextField(
                        value = editSpeech,
                        onValueChange = { editSpeech = it },
                        label = { Text("Frase dictada por TTS al tocarlo") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_picto_speech_input")
                    )

                    // 1. FAMILIAR CLINICAL AUDIO RECORDER (Offline local sound record mockup)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                            .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎤 " + viewModel.getUiTranslation("custom_audio"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                if (recordedPaths.containsKey(pic.id)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("✔️ Grabado Local", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Text(
                                "Graba tu voz (profesor, abuelo o madre) de manera 100% privada y local para que el alumno escuche un tono conocido al tocar este pictograma.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isRecordingActive) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Animated recording indicator sound waves
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Red, strokeWidth = 2.dp)
                                    Text("Grabando Audio... ¡Hable ahora de forma natural!", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.Red))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { viewModel.stopRecordingSimulator(pic.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Guardar", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ElevatedButton(
                                        onClick = { viewModel.startRecordingSimulator() },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp).testTag("start_voice_recording")
                                    ) {
                                        Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Iniciar Grabación", fontSize = 11.sp)
                                    }
                                    if (recordedPaths.containsKey(pic.id)) {
                                        TextButton(
                                            onClick = { viewModel.deleteRecordingForPictogram(pic.id) },
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Borrar voz grabada", color = Color.Red, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. ACOUSTIC VOICE SYNTHESIZER SIMULATOR (Modulate pitch and rate dynamically)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🔊 " + viewModel.getUiTranslation("voice_familiar"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Modula la síntesis para imitar la frecuencia vocal de un familiar (tono y velocidad de lectura).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Slider Pitch (tono)
                            Text("${viewModel.getUiTranslation("pitch")}: ${"%.2f".format(customVoicePitch)}x", style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = customVoicePitch,
                                onValueChange = { customVoicePitch = it },
                                valueRange = 0.5f..1.8f,
                                modifier = Modifier.height(24.dp)
                            )

                            // Slider Rate (velocidad)
                            Text("${viewModel.getUiTranslation("speed")}: ${"%.2f".format(customVoiceRate)}x", style = MaterialTheme.typography.labelSmall)
                            Slider(
                                value = customVoiceRate,
                                onValueChange = { customVoiceRate = it },
                                valueRange = 0.5f..1.8f,
                                modifier = Modifier.height(24.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.speak(editSpeech.ifEmpty { editLabel }, customVoicePitch, customVoiceRate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("preview_voice_pitch_rate")
                                ) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Probar Modulación", fontSize = 10.sp)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.configureFamiliarVoice(customVoicePitch, customVoiceRate, "Voz Familiar (" + editLabel + ")", true)
                                    },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Fijar esta modulación", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Category selection
                    Text("Filtrar Categoría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categoryColors.keys.forEach { cat ->
                            val isSel = editCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) categoryColors[cat]!! else Color.LightGray)
                                    .clickable { editCategory = cat }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(cat, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Preset Icon choice
                    Text("Elegir Símbolo Pictográfico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    FlowRow(maxItemsInEachRow = 4, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        iconOptions.forEach { ic ->
                            val isSel = editIconName == ic
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { editIconName = ic }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(mapStringToVector(ic), contentDescription = null, tint = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray)
                            }
                        }
                    }

                    // Grid design colors
                    Text("Elegir Color de Fondo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        pickerColors.forEach { col ->
                            val isSel = editColorHex == col
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .border(2.dp, if (isSel) Color.Black else Color.Transparent, CircleShape)
                                    .clickable { editColorHex = col }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editLabel.isNotEmpty()) {
                            val updated = pic.copy(
                                label = editLabel,
                                speechText = editSpeech.ifEmpty { editLabel },
                                category = editCategory,
                                colorHex = editColorHex,
                                presetIconName = editIconName
                            )
                            viewModel.editPictogram(updated)
                            showEditDialog = false
                            editingPicto = null
                        }
                    },
                    modifier = Modifier.testTag("save_edit_picto_submit")
                ) {
                    Text("Confirmar Cambios", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingPicto = null
                    }
                ) {
                    Text("Atrás")
                }
            }
        )
    }
}

// ==========================================
// C. TAB ANALÍTICA CLÍNICA LOCAL INTELIGENTE
// ==========================================
@Composable
fun LocalAnalyticsTab(viewModel: NeuronViewModel) {
    val report by viewModel.diagnosticReport.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High fidelity medical card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().testTag("clinical_analytics_panel")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "NEURON CLINICAL REPORT", 
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp)
                )
                Text(
                    "Análisis Inteligente Local", 
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Sesiones Completadas", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text("${report?.totalGames?.toString() ?: "0"}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Precisión Cognitiva", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(String.format("%.1f %%", report?.averageAccuracy ?: 0f), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
                    }
                }
            }
        }

        // Custom drawn Canvas Charts for Speed/Accuracy
        if (report != null && report!!.performanceTrends.isNotEmpty()) {
            Text("Gráfica: Rendimiento por Categorías", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val elements = report!!.performanceTrends
                        val barCount = elements.size
                        val barWidth = 44.dp.toPx()
                        val spacing = 20.dp.toPx()
                        val chartHeight = size.height - 30f

                        elements.forEachIndexed { index, trend ->
                            val x = spacing + index * (barWidth + spacing)
                            // Accuracy mapped height
                            val accuracyScale = (10f - trend.avgErrors).coerceIn(1f, 10f) / 10f
                            val h = chartHeight * accuracyScale
                            
                            // Draw bars
                            drawRoundRect(
                                color = Color(0xFF3B82F6),
                                topLeft = androidx.compose.ui.geometry.Offset(x, chartHeight - h),
                                size = androidx.compose.ui.geometry.Size(barWidth, h),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }
                    
                    // Simple text x-axis captions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        report!!.performanceTrends.forEach { trend ->
                            Text(trend.gameName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Diagnostic text cards
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Principales Conclusiones", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = report?.detailedPatterns ?: "Juega a alguna actividad para compilar diagnósticos locales.", 
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Modelo de Datos Local", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text("Los patrones y sugerencias se analizan localmente en el dispositivo utilizando el motor estadístico de Synapse Studio, asegurando total soberanía de datos y privacidad absoluta.", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
