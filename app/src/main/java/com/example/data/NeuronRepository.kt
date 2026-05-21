package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class NeuronRepository(private val db: NeuronDatabase) {

    private val userDao = db.userDao()
    private val studentDao = db.studentDao()
    private val gameRecordDao = db.gameRecordDao()
    private val agendaDao = db.agendaDao()
    private val pictogramDao = db.pictogramDao()
    private val savedCanvasDao = db.savedCanvasDao()
    private val customGameConfigDao = db.customGameConfigDao()

    // Users
    fun getActiveUserFlow(): Flow<User?> = userDao.getActiveUserFlow()
    suspend fun getActiveUserSync(): User? = userDao.getActiveUserSync()
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun registerUser(name: String, email: String, pin: String): Long {
        val user = User(name = name, email = email, pinCode = pin)
        return userDao.insertUser(user)
    }
    suspend fun activateSession(email: String) {
        userDao.clearAllSessions()
        userDao.markSessionActive(email)
        // Ensure standard presets are loaded
        preloadDefaultPictograms()
    }
    suspend fun clearSession() {
        userDao.clearAllSessions()
    }
    suspend fun updateActiveStudent(userId: Long, studentId: Long?) {
        userDao.updateActiveStudent(userId, studentId)
    }

    // Students
    fun getStudentsFlow(userId: Long): Flow<List<Student>> = studentDao.getStudentsFlow(userId)
    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)
    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }
    suspend fun awardXp(studentId: Long, xp: Int) {
        studentDao.awardXp(studentId, xp)
    }
    suspend fun updateSensoryMode(studentId: Long, lowSensory: Boolean) {
        studentDao.updateSensoryMode(studentId, lowSensory)
    }

    // Game records
    fun getRecordsFlow(studentId: Long): Flow<List<GamePlayRecord>> = gameRecordDao.getRecordsFlow(studentId)
    suspend fun getRecordsSync(studentId: Long): List<GamePlayRecord> = gameRecordDao.getRecordsSync(studentId)
    suspend fun insertGamePlayRecord(record: GamePlayRecord): Long = gameRecordDao.insertRecord(record)

    // Agenda Items
    fun getAgendaFlow(studentId: Long): Flow<List<AgendaItem>> = agendaDao.getAgendaFlow(studentId)
    suspend fun insertAgendaItem(item: AgendaItem): Long = agendaDao.insertAgendaItem(item)
    suspend fun updateAgendaItemCompletion(id: Long, completed: Boolean) = agendaDao.updateCompletion(id, completed)
    suspend fun deleteAgendaItem(id: Long) = agendaDao.deleteAgendaItem(id)

    // Pictograms
    fun getPictogramsFlow(studentId: Long?): Flow<List<Pictogram>> = pictogramDao.getPictogramsFlow(studentId)
    suspend fun getPictogramsSync(studentId: Long?): List<Pictogram> = pictogramDao.getPictogramsSync(studentId)
    suspend fun insertPictogram(picto: Pictogram): Long = pictogramDao.insertPictogram(picto)
    suspend fun deletePictogram(picto: Pictogram) = pictogramDao.deletePictogram(picto)

    // Saved Canvas templates
    fun getSavedCanvasFlow(studentId: Long): Flow<List<SavedCanvas>> = savedCanvasDao.getSavedCanvasFlow(studentId)
    suspend fun insertSavedCanvas(canvas: SavedCanvas): Long = savedCanvasDao.insertCanvas(canvas)
    suspend fun deleteSavedCanvas(id: Long) = savedCanvasDao.deleteCanvas(id)

    // Custom Game Configurations
    fun getCustomGameConfigsFlow(studentId: Long): Flow<List<CustomGameConfig>> = customGameConfigDao.getConfigurationsFlow(studentId)
    suspend fun insertCustomGameConfig(config: CustomGameConfig): Long = customGameConfigDao.insertConfiguration(config)
    suspend fun deleteCustomGameConfig(id: Long) = customGameConfigDao.deleteConfiguration(id)

    // Prepulae pictograms
    suspend fun preloadDefaultPictograms() {
        val existing = pictogramDao.getPictogramsSync(null)
        if (existing.isEmpty()) {
            val presets = listOf(
                // ACCIONES (Green, #FF4CAF50)
                Pictogram(label = "Quiero", speechText = "Quiero", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "touch_app"),
                Pictogram(label = "Jugar", speechText = "Quiero jugar a un juego", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "videogame_asset"),
                Pictogram(label = "Dormir", speechText = "Estoy cansado, quiero ir a dormir", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "bedtime"),
                Pictogram(label = "Correr", speechText = "Quiero correr y hacer deporte", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "directions_run"),
                Pictogram(label = "Escuchar", speechText = "Quiero escuchar con atención", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "hearing"),
                Pictogram(label = "Hablar", speechText = "Quiero hablar contigo", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "record_voice_over"),
                Pictogram(label = "Parar", speechText = "Para por favor", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "stop_circle"),
                Pictogram(label = "Ver", speechText = "Quiero ver la pantalla", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "visibility"),
                Pictogram(label = "Comprar", speechText = "Quiero ir a comprar", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "shopping_cart"),
                Pictogram(label = "Aprender", speechText = "Quiero aprender", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "history_edu"),
                Pictogram(label = "Música", speechText = "Quiero escuchar música", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "music_note"),
                Pictogram(label = "Pasear", speechText = "Quiero dar un paseo", category = "ACCION", colorHex = "#FF4CAF50", presetIconName = "directions_walk"),
                
                // EMOCIONES (Purple, #FF9C27B0)
                Pictogram(label = "Contento", speechText = "Estoy muy contento", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "sentiment_very_satisfied"),
                Pictogram(label = "Triste", speechText = "Me siento un poco triste", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "sentiment_dissatisfied"),
                Pictogram(label = "Enojado", speechText = "Estoy enfadado", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "sentiment_very_dissatisfied"),
                Pictogram(label = "Dolor", speechText = "Me duele algo", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "sick"),
                Pictogram(label = "Asustado", speechText = "Tengo miedo", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "warning"),
                Pictogram(label = "Cansado", speechText = "Tengo sueño, estoy cansado", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "snooze"),
                Pictogram(label = "Feliz", speechText = "Te quiero mucho, soy feliz", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "favorite"),
                Pictogram(label = "Sorprendido", speechText = "¡Wow, qué sorpresa!", category = "EMOCION", colorHex = "#FF9C27B0", presetIconName = "auto_awesome"),

                // ALIMENTOS (Orange, #FFFF9800)
                Pictogram(label = "Comer", speechText = "Tengo hambre, quiero comer", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "restaurant"),
                Pictogram(label = "Beber", speechText = "Tengo sed, quiero beber algo", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "local_drink"),
                Pictogram(label = "Manzana", speechText = "Quiero comer una manzana", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "nutrition"),
                Pictogram(label = "Leche", speechText = "Quiero un vaso de leche", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "coffee"),
                Pictogram(label = "Galletas", speechText = "Quiero galletas dulce", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "cookie"),
                Pictogram(label = "Agua", speechText = "Quiero un vaso de agua fresca", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "water_drop"),
                Pictogram(label = "Pan", speechText = "Quiero comer pan", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "bakery_dining"),
                Pictogram(label = "Sopa", speechText = "Tengo ganas de tomar sopa", category = "ALIMENTO", colorHex = "#FFFF9800", presetIconName = "soup_kitchen"),

                // HIGIENE (Blue, #FF2196F3)
                Pictogram(label = "Baño", speechText = "Necesito ir al cuarto de baño", category = "HIGIENE", colorHex = "#FF2196F3", presetIconName = "wc"),
                Pictogram(label = "Lavar Manos", speechText = "Quiero lavarme las manos", category = "HIGIENE", colorHex = "#FF2196F3", presetIconName = "wash"),
                Pictogram(label = "Ducha", speechText = "Toca tomar un baño o ducha", category = "HIGIENE", colorHex = "#FF2196F3", presetIconName = "shower"),
                Pictogram(label = "Dientes", speechText = "Quiero cepillarme los dientes", category = "HIGIENE", colorHex = "#FF2196F3", presetIconName = "brush"),
                Pictogram(label = "Cambiar Ropa", speechText = "Quiero cambiarme de ropa", category = "HIGIENE", colorHex = "#FF2196F3", presetIconName = "checkroom"),

                // PREGUNTAS / COMUNICACIÓN AUX (Pink, #FFE91E63)
                Pictogram(label = "Ayuda", speechText = "Necesito ayuda por favor", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "help_outline"),
                Pictogram(label = "Sí", speechText = "Sí", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "check_circle"),
                Pictogram(label = "No", speechText = "No", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "cancel"),
                Pictogram(label = "Gracias", speechText = "Muchas gracias", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "favorite"),
                Pictogram(label = "Por favor", speechText = "Por favor", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "volunteer_activism"),
                Pictogram(label = "Hola", speechText = "Hola, buenos días", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "waving_hand"),
                Pictogram(label = "Adiós", speechText = "Adiós, hasta luego", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "back_hand"),
                Pictogram(label = "¿Qué es?", speechText = "¿Qué es esto?", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "help_center"),
                Pictogram(label = "¿Quién?", speechText = "¿Quién es él?", category = "PREGUNTAS", colorHex = "#FFE91E63", presetIconName = "person_search"),

                // GENERAL / FAMILIA, NÚMEROS Y LETRAS (Brown, #FF795548)
                Pictogram(label = "Casa", speechText = "Quiero ir a casa", category = "GENERAL", colorHex = "#FF795548", presetIconName = "home"),
                Pictogram(label = "Colegio", speechText = "Ir al colegio", category = "GENERAL", colorHex = "#FF795548", presetIconName = "school"),
                Pictogram(label = "Abuelo", speechText = "Mi abuelito querido", category = "GENERAL", colorHex = "#FF795548", presetIconName = "elderly"),
                Pictogram(label = "Abuela", speechText = "Mi abuelita querida", category = "GENERAL", colorHex = "#FF795548", presetIconName = "elderly_woman"),
                Pictogram(label = "Pelota", speechText = "La pelota de fútbol o basket", category = "GENERAL", colorHex = "#FF795548", presetIconName = "sports_soccer"),
                Pictogram(label = "Libro de un Cuento", speechText = "Quiero que leamos un libro", category = "GENERAL", colorHex = "#FF795548", presetIconName = "menu_book"),
                Pictogram(label = "Coche", speechText = "Un viaje en coche", category = "GENERAL", colorHex = "#FF795548", presetIconName = "directions_car"),
                Pictogram(label = "Sol", speechText = "Hace un hermoso día de sol", category = "GENERAL", colorHex = "#FF795548", presetIconName = "wb_sunny"),
                Pictogram(label = "Lluvia", speechText = "Está cayendo lluvia con paraguas", category = "GENERAL", colorHex = "#FF795548", presetIconName = "umbrella"),
                
                // Letras e Idioma (Numbers and alphabetic values inside GENERAL)
                Pictogram(label = "Letras ABC", speechText = "El abecedario y la fonología de las letras", category = "GENERAL", colorHex = "#FF795548", presetIconName = "abc"),
                Pictogram(label = "Contar 123", speechText = "Contar números del uno al diez", category = "GENERAL", colorHex = "#FF795548", presetIconName = "numbers"),
                Pictogram(label = "Número 1", speechText = "Uno", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_one"),
                Pictogram(label = "Número 2", speechText = "Dos", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_two"),
                Pictogram(label = "Número 3", speechText = "Tres", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_3"),
                Pictogram(label = "Número 4", speechText = "Cuatro", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_4"),
                Pictogram(label = "Número 5", speechText = "Cinco", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_5"),
                Pictogram(label = "Número 6", speechText = "Seis", category = "GENERAL", colorHex = "#FF795548", presetIconName = "looks_6")
            )
            presets.forEach { picto ->
                pictogramDao.insertPictogram(picto)
            }
        }
    }
}
