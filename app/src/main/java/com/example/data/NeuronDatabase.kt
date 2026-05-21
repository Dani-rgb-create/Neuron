package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. ENTITIES
// ==========================================

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val pinCode: String, // Offline protection passcode
    val activeStudentId: Long? = null,
    val isSessionActive: Boolean = false
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val age: Int,
    val avatarPreset: Int, // Index of avatar illustration (0 to 7)
    val customAvatarPath: String? = null, // Camera/Gallery path if provided
    val cognitiveLevel: String = "Medio", // Inicial, Medio, Avanzado
    val difficulties: String = "", // Semicolon separated, e.g. "Comunicación verbal; Atención sostenida"
    val strongSkills: String = "", // Semicolon separated, e.g. "Memoria visual; Asociación"
    val observations: String = "",
    val lowSensoryMode: Boolean = false, // True: Soft themes, quiet audio, less motion (TEA friendly)
    val totalScore: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_play_records")
data class GamePlayRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val gameType: String, // e.g. "LETRAS", "PAREJAS", "MEMORIA", "COMPRENSION", "FONETICA", "EJECUTIVAS", "SOCIAL", "AUTONOMIA", "EXPLORACION", "MATEMATICAS", "PICTOGRAMAS"
    val score: Int,
    val errors: Int,
    val speedMs: Long,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val notes: String = "" // Local AI analytics will parse notes to identify error patterns
)

@Entity(tableName = "agenda_items")
data class AgendaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val timeOfDay: String, // e.g. "09:00", "12:30"
    val iconName: String, // Name of the icon, e.g., "brush", "restaurant", "school", "bed"
    val colorHex: String, // HEX value representing visual theme
    val isCompleted: Boolean = false,
    val sequenceOrder: Int = 0,
    val dayOfWeek: Int = 0 // 0 = Lunes, etc., or -1 for daily routine
)

@Entity(tableName = "pictograms")
data class Pictogram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long? = null, // null means global default preset, otherwise belongs to custom user
    val label: String,
    val speechText: String, // What TTS speaks back
    val category: String, // e.g., "ACCION", "ALIMENTO", "HIGIENE", "EMOCION", "PREGUNTAS", "OBJETIVOS"
    val colorHex: String, // Aesthetic background category color
    val localImageUri: String? = null, // If uploaded via file reader/camera
    val presetIconName: String? = null, // Vector icons preset
    val isCustom: Boolean = false
)

@Entity(tableName = "saved_canvas")
data class SavedCanvas(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val serializedPictosIds: String // Comma separated list of custom or preset pictogram ids
)

// ==========================================
// 2. DAOs
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE isSessionActive = 1 LIMIT 1")
    fun getActiveUserFlow(): Flow<User?>

    @Query("SELECT * FROM users WHERE isSessionActive = 1 LIMIT 1")
    suspend fun getActiveUserSync(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("UPDATE users SET isSessionActive = 1 WHERE email = :email")
    suspend fun markSessionActive(email: String)

    @Query("UPDATE users SET isSessionActive = 0")
    suspend fun clearAllSessions()

    @Query("UPDATE users SET activeStudentId = :studentId WHERE id = :userId")
    suspend fun updateActiveStudent(userId: Long, studentId: Long?)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE userId = :userId ORDER BY name ASC")
    fun getStudentsFlow(userId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("UPDATE students SET totalScore = totalScore + :xp WHERE id = :studentId")
    suspend fun awardXp(studentId: Long, xp: Int)

    @Query("UPDATE students SET lowSensoryMode = :lowSensory WHERE id = :studentId")
    suspend fun updateSensoryMode(studentId: Long, lowSensory: Boolean)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_play_records WHERE studentId = :studentId ORDER BY dateTimestamp DESC")
    fun getRecordsFlow(studentId: Long): Flow<List<GamePlayRecord>>

    @Query("SELECT * FROM game_play_records WHERE studentId = :studentId")
    suspend fun getRecordsSync(studentId: Long): List<GamePlayRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GamePlayRecord): Long
}

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda_items WHERE studentId = :studentId ORDER BY sequenceOrder ASC, timeOfDay ASC")
    fun getAgendaFlow(studentId: Long): Flow<List<AgendaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaItem): Long

    @Query("UPDATE agenda_items SET isCompleted = :completed WHERE id = :itemId")
    suspend fun updateCompletion(itemId: Long, completed: Boolean)

    @Query("DELETE FROM agenda_items WHERE id = :itemId")
    suspend fun deleteAgendaItem(itemId: Long)
}

@Dao
interface PictogramDao {
    @Query("SELECT * FROM pictograms WHERE studentId IS NULL OR studentId = :studentId ORDER BY label ASC")
    fun getPictogramsFlow(studentId: Long?): Flow<List<Pictogram>>

    @Query("SELECT * FROM pictograms WHERE studentId IS NULL OR studentId = :studentId ORDER BY label ASC")
    suspend fun getPictogramsSync(studentId: Long?): List<Pictogram>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictogram(picto: Pictogram): Long

    @Delete
    suspend fun deletePictogram(picto: Pictogram)
}

@Dao
interface SavedCanvasDao {
    @Query("SELECT * FROM saved_canvas WHERE studentId = :studentId ORDER BY id DESC")
    fun getSavedCanvasFlow(studentId: Long): Flow<List<SavedCanvas>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanvas(canvas: SavedCanvas): Long

    @Query("DELETE FROM saved_canvas WHERE id = :canvasId")
    suspend fun deleteCanvas(canvasId: Long)
}

@Entity(tableName = "custom_game_configs")
data class CustomGameConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val gameType: String, // LETRAS, MEMORIA, PAREJAS, MATEMATICAS
    val fontType: String, // Mayúsculas, Minúsculas, Ligada, Imprenta
    val backgroundStyle: String, // "Espacio Cósmico", "Bosque Encantado", "Atardecer Suave", "Pixel Clásico", "Alto Contraste Retina"
    val customWords: String, // comma separated
    val maxNumber: Int = 10,
    val timeLimitSec: Int = 60,
    val customInstructions: String = "",
    val speedSetting: String = "NORMAL"
)

@Dao
interface CustomGameConfigDao {
    @Query("SELECT * FROM custom_game_configs WHERE studentId = :studentId ORDER BY id DESC")
    fun getConfigurationsFlow(studentId: Long): Flow<List<CustomGameConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(config: CustomGameConfig): Long

    @Query("DELETE FROM custom_game_configs WHERE id = :configId")
    suspend fun deleteConfiguration(configId: Long)
}

// ==========================================
// 3. DATABASE HOLDER
// ==========================================

@Database(
    entities = [
        User::class,
        Student::class,
        GamePlayRecord::class,
        AgendaItem::class,
        Pictogram::class,
        SavedCanvas::class,
        CustomGameConfig::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NeuronDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun agendaDao(): AgendaDao
    abstract fun pictogramDao(): PictogramDao
    abstract fun savedCanvasDao(): SavedCanvasDao
    abstract fun customGameConfigDao(): CustomGameConfigDao

    companion object {
        @Volatile
        private var INSTANCE: NeuronDatabase? = null

        fun getDatabase(context: Context): NeuronDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NeuronDatabase::class.java,
                    "neuron_local_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
