package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import kotlinx.coroutines.launch
import com.example.data.Student
import com.example.ui.NeuronViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(viewModel: NeuronViewModel) {
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateFlowOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    // Forms states
    var name by remember { mutableStateFlowOf("") }
    var ageString by remember { mutableStateFlowOf("") }
    var selectedPresetAvatar by remember { mutableStateFlowOf(0) }
    var cognitiveLevel by remember { mutableStateFlowOf("Medio") } // Inicial, Medio, Avanzado
    var observations by remember { mutableStateFlowOf("") }
    var lowSensoryMode by remember { mutableStateFlowOf(false) }

    // Checkable states for difficulties and strengths
    val difficultyOptions = listOf("Habla No Verbal", "Atención Distraída (TDAH)", "Coordinación Motora", "Hipersensibilidad Sensorial (TEA)", "Lectoescritura")
    val selectedDifficulties = remember { mutableStateListOf<String>() }

    val strengthOptions = listOf("Memoria Visual", "Expresión Artística", "Afinidad Numérica", "Habilidades Rítmicas", "Enfoque en Detalles")
    val selectedStrengths = remember { mutableStateListOf<String>() }

    // Student Edit/Export States
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var editName by remember { mutableStateFlowOf("") }
    var editAgeString by remember { mutableStateFlowOf("") }
    var editSelectedPresetAvatar by remember { mutableStateFlowOf(0) }
    var editCognitiveLevel by remember { mutableStateFlowOf("Medio") }
    var editObservations by remember { mutableStateFlowOf("") }
    var editLowSensoryMode by remember { mutableStateFlowOf(false) }
    val editSelectedDifficulties = remember { mutableStateListOf<String>() }
    val editSelectedStrengths = remember { mutableStateListOf<String>() }

    var studentToExport by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentToEdit) {
        studentToEdit?.let { s ->
            editName = s.name
            editAgeString = s.age.toString()
            editSelectedPresetAvatar = s.avatarPreset
            editCognitiveLevel = s.cognitiveLevel
            editLowSensoryMode = s.lowSensoryMode
            editObservations = s.observations
            editSelectedDifficulties.clear()
            if (s.difficulties.isNotEmpty()) {
                editSelectedDifficulties.addAll(s.difficulties.split(";"))
            }
            editSelectedStrengths.clear()
            if (s.strongSkills.isNotEmpty()) {
                editSelectedStrengths.addAll(s.strongSkills.split(";"))
            }
        }
    }

    // Beautiful illustrative avatar icons list to match Nintendo style
    val avatarIcons = listOf(
        Icons.Filled.Face,
        Icons.Filled.EmojiEmotions,
        Icons.Filled.FaceRetouchingNatural,
        Icons.Filled.Pets,
        Icons.Filled.ChildCare,
        Icons.Filled.Star,
        Icons.Filled.WorkspacePremium,
        Icons.Filled.Favorite
    )
    val avatarColors = listOf(
        Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEC4899),
        Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFFEF4444), Color(0xFF14B8A6)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NEURON",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            text = "Profesor: ${activeUser?.name ?: "Local"}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            importJsonText = ""
                            importErrorMessage = null
                            showImportDialog = true 
                        },
                        modifier = Modifier.testTag("import_student_btn")
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Importar Ficha JSON", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_btn")
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Reset fields
                    name = ""
                    ageString = ""
                    selectedPresetAvatar = 0
                    cognitiveLevel = "Medio"
                    observations = ""
                    lowSensoryMode = false
                    selectedDifficulties.clear()
                    selectedStrengths.clear()
                    showAddDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Añadir Alumno", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (studentsList.isEmpty()) {
                // Empty state friendly screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.SupervisorAccount,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Bienvenidos a NEURON!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Para comenzar la terapia adaptativa y ejercitar capacidades cognitivas de tus alumnos, presiona el botón '+' para registrar un perfil.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 400.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Tus Alumnos Registrados",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                     items(studentsList) { student ->
                        Card(
                            onClick = { viewModel.selectStudent(student) },
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_card_${student.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle styled exactly like the HTML template profile badge
                                val color = avatarColors[student.avatarPreset % avatarColors.size]
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .border(3.dp, Color.White, CircleShape)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = avatarIcons[student.avatarPreset % avatarIcons.size],
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(18.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("${student.age} años", fontSize = 11.sp) }
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Nivel: ${student.cognitiveLevel}", fontSize = 11.sp) }
                                        )
                                        if (student.lowSensoryMode) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("🔇 Sensorial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { studentToExport = student },
                                        modifier = Modifier.testTag("export_student_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "Exportar",
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { studentToEdit = student },
                                        modifier = Modifier.testTag("edit_student_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteStudent(student) },
                                        modifier = Modifier.testTag("delete_student_${student.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Borrar",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dialogo de creacion de Alumno
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = {
                        Text(
                            "Crear Perfil del Alumno", 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nombre del Alumno") },
                                modifier = Modifier.fillMaxWidth().testTag("add_stu_name"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = ageString,
                                onValueChange = { if (it.all { char -> char.isDigit() }) ageString = it },
                                label = { Text("Edad") },
                                modifier = Modifier.fillMaxWidth().testTag("add_stu_age"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Select avatar
                            Text("Icono del Perfil (Avatar)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(avatarIcons) { idx, icon ->
                                    val isSelected = selectedPresetAvatar == idx
                                    val color = avatarColors[idx % avatarColors.size]
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) color.copy(alpha = 0.25f) 
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { selectedPresetAvatar = idx }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Level
                            Text("Nivel Cognitivo Terapéutico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Inicial", "Medio", "Avanzado").forEach { lvl ->
                                    val isSelected = cognitiveLevel == lvl
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { cognitiveLevel = lvl },
                                        label = { Text(lvl) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Low sensory toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Hearing,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Entorno Sensory-Comfort 🔇", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("Atenúa colores de fondo y sonidos fuertes. Altamente recomendado para TEA.", style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(
                                    checked = lowSensoryMode,
                                    onCheckedChange = { lowSensoryMode = it }
                                )
                            }

                            // Difficulties
                            Text("Dificultades (Áreas de apoyo)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            difficultyOptions.forEach { option ->
                                var checked by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            checked = !checked
                                            if (checked) selectedDifficulties.add(option) else selectedDifficulties.remove(option)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { 
                                            checked = it
                                            if (it) selectedDifficulties.add(option) else selectedDifficulties.remove(option)
                                        }
                                    )
                                    Text(option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Strengths
                            Text("Habilidades Sobresalientes (Puntos fuertes)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            strengthOptions.forEach { option ->
                                var checked by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            checked = !checked
                                            if (checked) selectedStrengths.add(option) else selectedStrengths.remove(option)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { 
                                            checked = it
                                            if (it) selectedStrengths.add(option) else selectedStrengths.remove(option)
                                        }
                                    )
                                    Text(option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            OutlinedTextField(
                                value = observations,
                                onValueChange = { observations = it },
                                label = { Text("Observaciones Clínicas / Notas libres") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (name.isEmpty() || ageString.isEmpty()) return@Button
                                viewModel.addStudent(
                                    name = name,
                                    age = ageString.toIntOrNull() ?: 6,
                                    avatarPreset = selectedPresetAvatar,
                                    cognitiveLevel = cognitiveLevel,
                                    difficulties = selectedDifficulties,
                                    strongSkills = selectedStrengths,
                                    observations = observations,
                                    isSensoryComfort = lowSensoryMode
                                )
                                showAddDialog = false
                            },
                            modifier = Modifier.testTag("add_student_submit")
                        ) {
                            Text("Guardar Ficha", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Dialogo de edicion de Alumno
            studentToEdit?.let { currentProfile ->
                AlertDialog(
                    onDismissRequest = { studentToEdit = null },
                    title = {
                        Text(
                            "Editar Perfil del Alumno", 
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nombre del Alumno") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_stu_name"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = editAgeString,
                                onValueChange = { if (it.all { char -> char.isDigit() }) editAgeString = it },
                                label = { Text("Edad") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_stu_age"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Select avatar
                            Text("Icono del Perfil (Avatar)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(avatarIcons) { idx, icon ->
                                    val isSelected = editSelectedPresetAvatar == idx
                                    val color = avatarColors[idx % avatarColors.size]
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) color.copy(alpha = 0.25f) 
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { editSelectedPresetAvatar = idx }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Level
                            Text("Nivel Cognitivo Terapéutico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Inicial", "Medio", "Avanzado").forEach { lvl ->
                                    val isSelected = editCognitiveLevel == lvl
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { editCognitiveLevel = lvl },
                                        label = { Text(lvl) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Low sensory toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Hearing,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Entorno Sensory-Comfort 🔇", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("Atenúa colores de fondo y sonidos fuertes. Altamente recomendado para TEA.", style = MaterialTheme.typography.labelSmall)
                                }
                                Switch(
                                    checked = editLowSensoryMode,
                                    onCheckedChange = { editLowSensoryMode = it }
                                )
                            }

                            // Difficulties
                            Text("Dificultades (Áreas de apoyo)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            difficultyOptions.forEach { option ->
                                val checked = editSelectedDifficulties.contains(option)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            if (checked) editSelectedDifficulties.remove(option) else editSelectedDifficulties.add(option)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { 
                                            if (it) editSelectedDifficulties.add(option) else editSelectedDifficulties.remove(option)
                                        }
                                    )
                                    Text(option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Strengths
                            Text("Habilidades Sobresalientes (Puntos fuertes)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            strengthOptions.forEach { option ->
                                val checked = editSelectedStrengths.contains(option)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            if (checked) editSelectedStrengths.remove(option) else editSelectedStrengths.add(option)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { 
                                            if (it) editSelectedStrengths.add(option) else editSelectedStrengths.remove(option)
                                        }
                                    )
                                    Text(option, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            OutlinedTextField(
                                value = editObservations,
                                onValueChange = { editObservations = it },
                                label = { Text("Observaciones Clínicas / Notas libres") },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editName.isEmpty() || editAgeString.isEmpty()) return@Button
                                viewModel.editStudent(
                                    id = currentProfile.id,
                                    name = editName,
                                    age = editAgeString.toIntOrNull() ?: currentProfile.age,
                                    avatarPreset = editSelectedPresetAvatar,
                                    cognitiveLevel = editCognitiveLevel,
                                    difficulties = editSelectedDifficulties.toList(),
                                    strongSkills = editSelectedStrengths.toList(),
                                    observations = editObservations,
                                    isSensoryComfort = editLowSensoryMode,
                                    totalScore = currentProfile.totalScore
                                )
                                studentToEdit = null
                            },
                            modifier = Modifier.testTag("edit_student_submit")
                        ) {
                            Text("Actualizar Ficha", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { studentToEdit = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Dialogo de exportar ficha y reportes offline
            studentToExport?.let { currentProfile ->
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                AlertDialog(
                    onDismissRequest = { studentToExport = null },
                    title = {
                        Text(
                            "Exportar Ficha / Informes",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Exporta la información completa de ${currentProfile.name} de forma 100% local bajo privacidad absoluta.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Markdown option
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val report = viewModel.generateStudentMarkdownReport(currentProfile)
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, report)
                                            type = "text/plain"
                                        }
                                        val chooser = Intent.createChooser(intent, "Compartir Reporte de ${currentProfile.name}")
                                        context.startActivity(chooser)
                                        studentToExport = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("export_md_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Informe Clínico (Markdown)", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // JSON option
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val json = viewModel.generateStudentJsonReport(currentProfile)
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, json)
                                            type = "text/plain"
                                        }
                                        val chooser = Intent.createChooser(intent, "Respaldar JSON de ${currentProfile.name}")
                                        context.startActivity(chooser)
                                        studentToExport = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("export_json_btn"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copia de Seguridad (JSON)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { studentToExport = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            if (showImportDialog) {
                AlertDialog(
                    onDismissRequest = { showImportDialog = false },
                    title = {
                        Text(
                            "Importar Ficha desde Respaldo 📥",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Pega el bloque de texto JSON de respaldo copiado desde otro dispositivo NEURON para restaurar el alumno, historial, juegos y analíticas de forma íntegra local.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            OutlinedTextField(
                                value = importJsonText,
                                onValueChange = { 
                                    importJsonText = it
                                    importErrorMessage = null
                                },
                                label = { Text("Pegar código de respaldo aquí...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .testTag("import_json_textarea"),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 12
                            )

                            importErrorMessage?.let { err ->
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (importJsonText.isNotBlank()) {
                                    viewModel.importStudentFromJson(
                                        jsonStr = importJsonText,
                                        onSuccess = {
                                            showImportDialog = false
                                            importJsonText = ""
                                        },
                                        onError = { errorText ->
                                            importErrorMessage = errorText
                                        }
                                    )
                                }
                            },
                            enabled = importJsonText.isNotBlank(),
                            modifier = Modifier.testTag("import_json_confirm_btn")
                        ) {
                            Text("Importar", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
