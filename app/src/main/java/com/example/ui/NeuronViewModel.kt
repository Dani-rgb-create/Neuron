package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgendaItem
import com.example.data.GamePlayRecord
import com.example.data.NeuronDatabase
import com.example.data.NeuronRepository
import com.example.data.Pictogram
import com.example.data.SavedCanvas
import com.example.data.Student
import com.example.data.User
import com.example.data.CustomGameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class NeuronViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = NeuronDatabase.getDatabase(application)
    private val repository = NeuronRepository(db)

    private var tts: TextToSpeech? = null
    private val _isTtsInitialized = MutableStateFlow(false)
    val isTtsInitialized = _isTtsInitialized.asStateFlow()

    // Dynamic EU Language
    private val _activeLanguage = MutableStateFlow("ES") // "ES", "EN", "FR", "DE", "IT", "PT", "PL"
    val activeLanguage = _activeLanguage.asStateFlow()

    // Familiar Voice simulation attributes
    private val _voicePitch = MutableStateFlow(1.0f)
    val voicePitch = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(1.0f)
    val voiceRate = _voiceRate.asStateFlow()

    private val _isCustomVoiceActive = MutableStateFlow(false)
    val isCustomVoiceActive = _isCustomVoiceActive.asStateFlow()

    private val _isVoiceRecordingActive = MutableStateFlow(false)
    val isVoiceRecordingActive = _isVoiceRecordingActive.asStateFlow()

    private val _customVoiceLabel = MutableStateFlow("Voz Estándar")
    val customVoiceLabel = _customVoiceLabel.asStateFlow()

    // Pictogram Recorded voice paths (simulated offline recordings)
    private val _recordedAudioPathsByPictoId = MutableStateFlow<Map<Long, String>>(emptyMap())
    val recordedAudioPathsByPictoId = _recordedAudioPathsByPictoId.asStateFlow()

    // Translation Lookups for Preset Pictograms (completamente loca para la Unión Europea)
    val pictogramTranslations = mapOf(
        "Quiero" to mapOf("EN" to "I want", "FR" to "Je veux", "DE" to "Ich will", "IT" to "Voglio", "PT" to "Eu quero", "PL" to "Chcę"),
        "Jugar" to mapOf("EN" to "To play", "FR" to "Jouer", "DE" to "Spielen", "IT" to "Giocare", "PT" to "Jogar", "PL" to "Grać"),
        "Dormir" to mapOf("EN" to "To sleep", "FR" to "Dormir", "DE" to "Schlafen", "IT" to "Dormire", "PT" to "Dormir", "PL" to "Spać"),
        "Correr" to mapOf("EN" to "To run", "FR" to "Courir", "DE" to "Laufen", "IT" to "Correre", "PT" to "Correr", "PL" to "Biegać"),
        "Escuchar" to mapOf("EN" to "To listen", "FR" to "Écouter", "DE" to "Zuhören", "IT" to "Ascoltare", "PT" to "Ouvir", "PL" to "Słuchać"),
        "Hablar" to mapOf("EN" to "To speak", "FR" to "Parler", "DE" to "Sprechen", "IT" to "Parlare", "PT" to "Falar", "PL" to "Mówić"),
        "Parar" to mapOf("EN" to "Stop", "FR" to "Arrêter", "DE" to "Stopp", "IT" to "Fermati", "PT" to "Parar", "PL" to "Zatrzymać"),
        "Ver" to mapOf("EN" to "To see", "FR" to "Voir", "DE" to "Sehen", "IT" to "Vedere", "PT" to "Ver", "PL" to "Widzieć"),
        "Comprar" to mapOf("EN" to "To buy", "FR" to "Acheter", "DE" to "Kaufen", "IT" to "Comprare", "PT" to "Comprar", "PL" to "Kupować"),
        "Aprender" to mapOf("EN" to "To learn", "FR" to "Apprendre", "DE" to "Lernen", "IT" to "Imparare", "PT" to "Aprender", "PL" to "Uczyć się"),
        "Música" to mapOf("EN" to "Music", "FR" to "Musique", "DE" to "Musik", "IT" to "Musica", "PT" to "Música", "PL" to "Muzyka"),
        "Pasear" to mapOf("EN" to "To walk", "FR" to "Se promener", "DE" to "Spazieren", "IT" to "Passeggiare", "PT" to "Passear", "PL" to "Spacerować"),
        "Contento" to mapOf("EN" to "Happy", "FR" to "Content", "DE" to "Glücklich", "IT" to "Felice", "PT" to "Contente", "PL" to "Zadowolony"),
        "Triste" to mapOf("EN" to "Sad", "FR" to "Triste", "DE" to "Traurig", "IT" to "Triste", "PT" to "Triste", "PL" to "Smutny"),
        "Enojado" to mapOf("EN" to "Angry", "FR" to "En colère", "DE" to "Wütend", "IT" to "Arrabbiato", "PT" to "Zangado", "PL" to "Zły"),
        "Dolor" to mapOf("EN" to "Pain", "FR" to "Douleur", "DE" to "Schmerz", "IT" to "Dolore", "PT" to "Dor", "PL" to "Ból"),
        "Asustado" to mapOf("EN" to "Scared", "FR" to "Effrayé", "DE" to "Verängstigt", "IT" to "Spaventato", "PT" to "Assustado", "PL" to "Smutny"),
        "Cansado" to mapOf("EN" to "Tired", "FR" to "Fatigué", "DE" to "Müde", "IT" to "Stanco", "PT" to "Cansado", "PL" to "Zmęczony"),
        "Feliz" to mapOf("EN" to "Happy", "FR" to "Heureux", "DE" to "Glücklich", "IT" to "Felice", "PT" to "Feliz", "PL" to "Szczęśliwy"),
        "Sorprendido" to mapOf("EN" to "Surprised", "FR" to "Surpris", "DE" to "Überrascht", "IT" to "Sorpreso", "PT" to "Surpreso", "PL" to "Zaskoczony"),
        "Comer" to mapOf("EN" to "To eat", "FR" to "Manger", "DE" to "Essen", "IT" to "Mangiare", "PT" to "Comer", "PL" to "Jeść"),
        "Beber" to mapOf("EN" to "To drink", "FR" to "Boire", "DE" to "Trinken", "IT" to "Bere", "PT" to "Beber", "PL" to "Pić"),
        "Manzana" to mapOf("EN" to "Apple", "FR" to "Pomme", "DE" to "Apfel", "IT" to "Mela", "PT" to "Maçã", "PL" to "Jabłko"),
        "Leche" to mapOf("EN" to "Milk", "FR" to "Lait", "DE" to "Milch", "IT" to "Latte", "PT" to "Leite", "PL" to "Mleko"),
        "Galletas" to mapOf("EN" to "Cookies", "FR" to "Biscuits", "DE" to "Kekse", "IT" to "Biscotti", "PT" to "Bolachas", "PL" to "Ciasteczka"),
        "Agua" to mapOf("EN" to "Water", "FR" to "Eau", "DE" to "Wasser", "IT" to "Acqua", "PT" to "Água", "PL" to "Woda"),
        "Pan" to mapOf("EN" to "Bread", "FR" to "Pain", "DE" to "Brot", "IT" to "Pane", "PT" to "Pão", "PL" to "Chleb"),
        "Sopa" to mapOf("EN" to "Soup", "FR" to "Soupe", "DE" to "Suppe", "IT" to "Zupa", "PT" to "Sopa", "PL" to "Zupa"),
        "Baño" to mapOf("EN" to "Bathroom", "FR" to "Toilettes", "DE" to "Toilette", "IT" to "Bagno", "PT" to "Banho", "PL" to "Toaleta"),
        "Lavar Manos" to mapOf("EN" to "Wash hands", "FR" to "Laver mains", "DE" to "Hände waschen", "IT" to "Mani", "PT" to "Lavar as mãos", "PL" to "Myć ręce"),
        "Ducha" to mapOf("EN" to "Shower", "FR" to "Douche", "DE" to "Dusche", "IT" to "Doccia", "PT" to "Duche", "PL" to "Prysznic"),
        "Dientes" to mapOf("EN" to "Teeth", "FR" to "Dents", "DE" to "Zähne", "IT" to "Denti", "PT" to "Dentes", "PL" to "Zęby"),
        "Cambiar Ropa" to mapOf("EN" to "Change clothes", "FR" to "Changer les vêtements", "DE" to "Umziehen", "IT" to "Vestiti", "PT" to "Mudar de roupa", "PL" to "Ubranie"),
        "Ayuda" to mapOf("EN" to "Help", "FR" to "Aide", "DE" to "Hilfe", "IT" to "Aiuto", "PT" to "Ajuda", "PL" to "Pomoc"),
        "Sí" to mapOf("EN" to "Yes", "FR" to "Oui", "DE" to "Ja", "IT" to "Sì", "PT" to "Sim", "PL" to "Tak"),
        "No" to mapOf("EN" to "No", "FR" to "Non", "DE" to "Nein", "IT" to "No", "PT" to "Não", "PL" to "Nie"),
        "Gracias" to mapOf("EN" to "Thanks", "FR" to "Merci", "DE" to "Danke", "IT" to "Grazie", "PT" to "Obrigado", "PL" to "Dziękuję"),
        "Por favor" to mapOf("EN" to "Please", "FR" to "S'il vous plaît", "DE" to "Bitte", "IT" to "Per favore", "PT" to "Por favor", "PL" to "Proszę"),
        "Hola" to mapOf("EN" to "Hello", "FR" to "Bonjour", "DE" to "Hallo", "IT" to "Ciao", "PT" to "Olá", "PL" to "Cześć"),
        "Adiós" to mapOf("EN" to "Goodbye", "FR" to "Au revoir", "DE" to "Tschüss", "IT" to "Ciao", "PT" to "Adeus", "PL" to "Do widzenia"),
        "Casa" to mapOf("EN" to "Home", "FR" to "Maison", "DE" to "Zuhause", "IT" to "Casa", "PT" to "Casa", "PL" to "Dom"),
        "Colegio" to mapOf("EN" to "School", "FR" to "École", "DE" to "Schule", "IT" to "Scuola", "PT" to "Escola", "PL" to "Szkoła"),
        "Abuelo" to mapOf("EN" to "Grandfather", "FR" to "Grand-père", "DE" to "Großvater", "IT" to "Nonno", "PT" to "Avô", "PL" to "Dziadek"),
        "Abuela" to mapOf("EN" to "Grandmother", "FR" to "Grand-mère", "DE" to "Großmutter", "IT" to "Nonna", "PT" to "Avó", "PL" to "Babcia"),
        "Pelota" to mapOf("EN" to "Ball", "FR" to "Ballon", "DE" to "Ball", "IT" to "Palla", "PT" to "Bola", "PL" to "Piłka"),
        "Libro de un Cuento" to mapOf("EN" to "Storybook", "FR" to "Livre", "DE" to "Buch", "IT" to "Libro de fiabe", "PT" to "Livro", "PL" to "Książka"),
        "Coche" to mapOf("EN" to "Car", "FR" to "Voiture", "DE" to "Auto", "IT" to "Macchina", "PT" to "Carro", "PL" to "Samochód"),
        "Sol" to mapOf("EN" to "Sun", "FR" to "Soleil", "DE" to "Sonne", "IT" to "Sole", "PT" to "Sol", "PL" to "Słońce"),
        "Lluvia" to mapOf("EN" to "Rain", "FR" to "Pluie", "DE" to "Regen", "IT" to "Pioggia", "PT" to "Chuva", "PL" to "Deszcz")
    )

    // Speech Phrase Translator for Preset Speech Content
    val phraseTranslations = mapOf(
        "Necesito ayuda por favor" to mapOf("EN" to "I need help please", "FR" to "J'ai besoin d'aide s'il vous plaît", "DE" to "Ich brauche bitte Hilfe", "IT" to "Ho bisogno di aiuto per favore", "PT" to "Preciso de ajuda por favor", "PL" to "Proszę o pomoc"),
        "Tengo hambre, quiero comer" to mapOf("EN" to "I am hungry, I want to eat", "FR" to "J'ai faim, je veux manger", "DE" to "Ich habe Hunger, ich möchte essen", "IT" to "Ho fame, voglio mangiare", "PT" to "Tenho fome, quero comer", "PL" to "Jestem głodny, chcę jeść"),
        "Tengo sed, quiero beber algo" to mapOf("EN" to "I am thirsty, I want to drink", "FR" to "J'ai soif, je veux boire quelque chose", "DE" to "Ich habe Durst, ich will etwas trinken", "IT" to "Ho sete, voglio bere qualcosa", "PT" to "Tenho sede, quero beber algo", "PL" to "Jestem spragniony, chcę pić"),
        "Necesito ir al cuarto de baño" to mapOf("EN" to "I need to go to the restroom", "FR" to "Je dois aller aux toilettes", "DE" to "Ich muss zur Toilette gehen", "IT" to "Ho bisogno di andare in bagno", "PT" to "Preciso de ir à casa de banho", "PL" to "Muszę iść do łazienki"),
        "Quiero lavarme las manos" to mapOf("EN" to "I want to wash my hands", "FR" to "Je veux me laver les mains", "DE" to "Ich will meine Hände waschen", "IT" to "Voglio lavarmi le mani", "PT" to "Quero lavar as mãos", "PL" to "Chcę umyć ręce"),
        "Quiero jugar a un juego" to mapOf("EN" to "I want to play a game", "FR" to "Je veux jouer à un jeu", "DE" to "Ich will ein Spiel spielen", "IT" to "Voglio fare un gioco", "PT" to "Quero jogar um jogo", "PL" to "Chcę zagrać w grę"),
        "Estoy cansado, quiero ir a dormir" to mapOf("EN" to "I am tired, I want to sleep", "FR" to "Je suis fatigué, je veux dormir", "DE" to "Ich bin müde, ich will schlafen gehen", "IT" to "Sono stanco, voglio dormire", "PT" to "Estou cansado, quero dormir", "PL" to "Jestem zmęczony, chcę iść spać"),
        "Quiero correr y hacer deporte" to mapOf("EN" to "I want to run and do sports", "FR" to "Je veux courir et faire du sport", "DE" to "Ich will laufen und Sport machen", "IT" to "Voglio correre e fare sport", "PT" to "Quero correr e praticar desporto", "PL" to "Chcę biegać i uprawiać sport"),
        "Quiero escuchar con atención" to mapOf("EN" to "I want to listen carefully", "FR" to "Je veux écouter attentivement", "DE" to "Ich will aufmerksam zuhören", "IT" to "Voglio ascoltare con attenzione", "PT" to "Quero ouvir com atenção", "PL" to "Chcę uważnie słuchać"),
        "Quiero hablar contigo" to mapOf("EN" to "I want to speak with you", "FR" to "Je veux te parler", "DE" to "Ich will mit dir sprechen", "IT" to "Voglio parlarti", "PT" to "Quero falar contigo", "PL" to "Chcę z tobą porozmawiać"),
        "Para por favor" to mapOf("EN" to "Stop, please", "FR" to "Arrête s'il te plaît", "DE" to "Stopp bitte", "IT" to "Fermati per favore", "PT" to "Para por favor", "PL" to "Przestań proszę"),
        "Quiero ver la pantalla" to mapOf("EN" to "I want to watch the screen", "FR" to "Je veux voir l'écran", "DE" to "Ich will den Bildschirm sehen", "IT" to "Voglio vedere lo schermo", "PT" to "Quero ver o ecrã", "PL" to "Chcę zobaczyć ekran"),
        "Quiero ir a comprar" to mapOf("EN" to "I want to go shopping", "FR" to "Je veux faire des courses", "DE" to "Ich will einkaufen gehen", "IT" to "Voglio andare a fare la spesa", "PT" to "Quero ir às compras", "PL" to "Chcę iść na zakupy"),
        "Quiero aprender" to mapOf("EN" to "I want to learn", "FR" to "Je veux apprendre", "DE" to "Ich will lernen", "IT" to "Voglio imparare", "PT" to "Quero aprender", "PL" to "Chcę się uczyć"),
        "Quiero escuchar música" to mapOf("EN" to "I want to listen to music", "FR" to "Je veux écouter de la musique", "DE" to "Ich will Musik hören", "IT" to "Voglio ascoltare musica", "PT" to "Quero ouvir música", "PL" to "Chcę słuchać muzyki"),
        "Quiero dar un paseo" to mapOf("EN" to "I want to take a walk", "FR" to "Je veux faire une promenade", "DE" to "Ich will spazieren gehen", "IT" to "Voglio fare una passeggiata", "PT" to "Quero dar uma caminhada", "PL" to "Chcę iść na spacer"),
        "Estoy muy contento" to mapOf("EN" to "I am very happy", "FR" to "Je suis très heureux", "DE" to "Ich bin sehr glücklich", "IT" to "Sono molto felice", "PT" to "Estou muito feliz", "PL" to "Jestem bardzo szczęśliwy"),
        "Me siento un poco triste" to mapOf("EN" to "I feel a bit sad", "FR" to "Je me sens un peu triste", "DE" to "Ich fühle mich ein bisschen traurig", "IT" to "Mi sento un po' triste", "PT" to "Sinto-me um pouco triste", "PL" to "Czuję się trochę smutny"),
        "Estoy enfadado" to mapOf("EN" to "I am angry", "FR" to "Je suis en colère", "DE" to "Ich bin wütend", "IT" to "Sono arrabbiato", "PT" to "Estou zangado", "PL" to "Jestem wściekły"),
        "Me duele algo" to mapOf("EN" to "Something hurts", "FR" to "Quelque chose me fait mal", "DE" to "Etwas tut mir weh", "IT" to "Mi fa male qualcosa", "PT" to "Sinto alguma dor", "PL" to "Coś mnie boli"),
        "Tengo miedo" to mapOf("EN" to "I am scared", "FR" to "J'ai peur", "DE" to "Ich habe Angst", "IT" to "Ho paura", "PT" to "Tenho medo", "PL" to "Boję się"),
        "Tengo sueño, estoy cansado" to mapOf("EN" to "I am sleepy and tired", "FR" to "J'ai sommeil et suis fatigué", "DE" to "Ich bin müde und schläfrig", "IT" to "Ho sonno e sono stanco", "PT" to "Tenho sono, estou cansado", "PL" to "Chce mi się spać, jestem zmęczony"),
        "Te quiero mucho, soy feliz" to mapOf("EN" to "I love you very much, I am happy", "FR" to "Je t'aime beaucoup, je suis ravi", "DE" to "Ich habe dich sehr lieb, ich bin froh", "IT" to "Ti voglio molto bene, sono felice", "PT" to "Amo-te muito, sou feliz", "PL" to "Bardzo cię kocham, jestem szczęśliwy"),
        "¡Wow, qué sorpresa!" to mapOf("EN" to "Wow, what a surprise!", "FR" to "Wow, quelle surprise!", "DE" to "Wow, was für eine Überraschung!", "IT" to "Wow, che sorpresa!", "PT" to "Wow, que surpresa!", "PL" to "Wow, co za niespodzianka!"),
        "Toca tomar un baño o ducha" to mapOf("EN" to "It's time to take a bath or shower", "FR" to "C'est l'heure de prendre un bain ou une douche", "DE" to "Es ist Zeit für ein Bad oder eine Dusche", "IT" to "È ora di fare un bagno o una doccia", "PT" to "É hora de tomar banho ou duche", "PL" to "Czas na kąpiel lub prysznic"),
        "Quiero cepillarme los dientes" to mapOf("EN" to "I want to brush my teeth", "FR" to "Je veux me brosser les dientes", "DE" to "Ich will meine Zähne putzen", "IT" to "Voglio lavarmi i denti", "PT" to "Quero escovar os dentes", "PL" to "Chcę umyć zęby"),
        "Quiero cambiarme de ropa" to mapOf("EN" to "I want to change clothes", "FR" to "Je veux me changer", "DE" to "Ich will mich umziehen", "IT" to "Voglio cambiarmi i vestiti", "PT" to "Quero mudar de roupa", "PL" to "Chcę się przebrać"),
        "Ir al colegio" to mapOf("EN" to "Go to school", "FR" to "Aller à l'école", "DE" to "Zur Schule gehen", "IT" to "Andare a scuola", "PT" to "Ir à escola", "PL" to "Iść do szkoły"),
        "Mi abuelito querido" to mapOf("EN" to "My dear grandfather", "FR" to "Mon cher grand-père", "DE" to "Mein lieber Großvater", "IT" to "Il mio caro nonno", "PT" to "Meu querido avô", "PL" to "Mój kochany dziadek"),
        "Mi abuelita querida" to mapOf("EN" to "My dear grandmother", "FR" to "Ma chère grand-mère", "DE" to "Meine liebe Großmutter", "IT" to "La mia cara nonna", "PT" to "Minha querida avó", "PL" to "Moja kochana babcia"),
        "La pelota de fútbol o basket" to mapOf("EN" to "The football or basketball", "FR" to "Le ballon de football ou basket", "DE" to "Der Fußball- oder Basketball", "IT" to "La palla da calcio o basket", "PT" to "A bola de futebol", "PL" to "Piłka nożna"),
        "Quiero que leamos un libro" to mapOf("EN" to "I want us to read a book", "FR" to "Je veux qu'on lise un livre", "DE" to "Ich will, dass wir ein Buch lesen", "IT" to "Voglio leggere un libro con te", "PT" to "Quero ler um livro", "PL" to "Chcę, żebyśmy poczytali książkę"),
        "Un viaje en coche" to mapOf("EN" to "A car ride", "FR" to "Un voyage en voiture", "DE" to "Eine Autofahrt", "IT" to "Un viaggio in macchina", "PT" to "Uma viagem de carro", "PL" to "Podróż samochodem"),
        "Hace un hermoso día de sol" to mapOf("EN" to "It is a beautiful sunny day", "FR" to "Il fait un beau soleil", "DE" to "Es ist ein schöner sonniger Tag", "IT" to "C'è una bellissima giornata di sole", "PT" to "Está um lindo dia de sol", "PL" to "Jest piękny słoneczny dzień"),
        "Está cayendo lluvia con paraguas" to mapOf("EN" to "Rain is falling, use umbrella", "FR" to "Il pleut dehors, prends le parapluie", "DE" to "Es regnet, nimm den Regenschirm", "IT" to "Sta piovendo, si usa l'ombrello", "PT" to "Está a chover, usa o guarda-chuva", "PL" to "Pada deszcz, weź parasol"),
        "El abecedario y la fonología de las letras" to mapOf("EN" to "The alphabet and the letters", "FR" to "L'alphabet et les lettres", "DE" to "Das Alphabet und die Buchstaben", "IT" to "L'alfabeto e la fonetica", "PT" to "A fonação e letras do alfabeto", "PL" to "Alfabet i głoskowanie liter"),
        "Contar números del uno al diez" to mapOf("EN" to "Counting numbers from 1 to 10", "FR" to "Compter de un à dix", "DE" to "Zahlen von eins bis zehn zählen", "IT" to "Contare i numeri da 1 a 10", "PT" to "Contar de um a dez", "PL" to "Liczenie od jednego do dziesięciu"),
        "Uno" to mapOf("EN" to "One", "FR" to "Un", "DE" to "Eins", "IT" to "Uno", "PT" to "Um", "PL" to "Jeden"),
        "Dos" to mapOf("EN" to "Two", "FR" to "Deux", "DE" to "Zwei", "IT" to "Due", "PT" to "Dois", "PL" to "Dwa"),
        "Tres" to mapOf("EN" to "Three", "FR" to "Trois", "DE" to "Drei", "IT" to "Tre", "PT" to "Três", "PL" to "Trzy"),
        "Cuatro" to mapOf("EN" to "Four", "FR" to "Quatre", "DE" to "Vier", "IT" to "Quattro", "PT" to "Quatro", "PL" to "Cztery"),
        "Cinco" to mapOf("EN" to "Five", "FR" to "Cinq", "DE" to "Fünf", "IT" to "Cinque", "PT" to "Cinco", "PL" to "Pięć"),
        "Seis" to mapOf("EN" to "Six", "FR" to "Six", "DE" to "Sechs", "IT" to "Sei", "PT" to "Seis", "PL" to "Sześć")
    )

    // Auth screen states
    val activeUser: StateFlow<User?> = repository.getActiveUserFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Active screen navigation
    private val _currentScreen = MutableStateFlow("AUTH") // "AUTH", "STUDENTS", "DASHBOARD"
    val currentScreen = _currentScreen.asStateFlow()

    // Selected Dashboard Tab: "AGENDA", "CAA", "JUEGOS", "ANALYTICS", "OCIO"
    private val _currentTab = MutableStateFlow("AGENDA")
    val currentTab = _currentTab.asStateFlow()

    // List of students for currently active user
    val students: StateFlow<List<Student>> = activeUser.flatMapLatest { user ->
        if (user != null) {
            repository.getStudentsFlow(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current active student
    private val _activeStudent = MutableStateFlow<Student?>(null)
    val activeStudent = _activeStudent.asStateFlow()

    // Agenda visual items
    val agendaItems: StateFlow<List<AgendaItem>> = activeStudent.flatMapLatest { student ->
        if (student != null) {
            repository.getAgendaFlow(student.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Pictograms (Presets + Custom ones)
    val pictograms: StateFlow<List<Pictogram>> = activeStudent.flatMapLatest { student ->
        repository.getPictogramsFlow(student?.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // History logs for current student
    val playRecords: StateFlow<List<GamePlayRecord>> = activeStudent.flatMapLatest { student ->
        if (student != null) {
            repository.getRecordsFlow(student.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Custom Canvas selections (Accumulated pictos for phrasing)
    private val _phraseCanvas = MutableStateFlow<List<Pictogram>>(emptyList())
    val phraseCanvas = _phraseCanvas.asStateFlow()

    // Saved canvas templates
    val savedCanvases: StateFlow<List<SavedCanvas>> = activeStudent.flatMapLatest { student ->
        if (student != null) {
            repository.getSavedCanvasFlow(student.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Custom game configurations flow
    val customGameConfigs: StateFlow<List<CustomGameConfig>> = activeStudent.flatMapLatest { student ->
        if (student != null) {
            repository.getCustomGameConfigsFlow(student.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Local smart diagnostic analysis state
    private val _diagnosticReport = MutableStateFlow<DiagnosticReport?>(null)
    val diagnosticReport = _diagnosticReport.asStateFlow()

    init {
        tts = TextToSpeech(application, this)
        
        // Load recorded audio paths from SharedPreferences
        val prefs = application.getSharedPreferences("neuron_prefs", android.content.Context.MODE_PRIVATE)
        val savedAudios = prefs.all.filterKeys { it.startsWith("picto_audio_") }
        val loadedMap = savedAudios.mapNotNull { (key, value) ->
            val idStr = key.removePrefix("picto_audio_")
            val id = idStr.toLongOrNull() ?: return@mapNotNull null
            id to (value as? String ?: "")
        }.toMap()
        _recordedAudioPathsByPictoId.value = loadedMap
        
        // Auto-check session on initial launch
        viewModelScope.launch {
            val sessionUser = repository.getActiveUserSync()
            if (sessionUser != null) {
                _currentScreen.value = "STUDENTS"
                // Seed standard preset pictos for safety
                repository.preloadDefaultPictograms()
                
                // If there was an active student previously, reload it
                if (sessionUser.activeStudentId != null) {
                    val s = repository.getStudentById(sessionUser.activeStudentId)
                    if (s != null) {
                        _activeStudent.value = s
                        _currentScreen.value = "DASHBOARD"
                        generateLocalDiagnostics(s.id)
                    }
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES")) // Spanish base
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("NEURON_TTS", "Spanish language is missing/not supported. Fallback to default.")
                tts?.setLanguage(Locale.getDefault())
            }
            _isTtsInitialized.value = true
        } else {
            Log.e("NEURON_TTS", "TTS Initialization failed.")
        }
    }

    fun setLanguage(langCode: String) {
        _activeLanguage.value = langCode
        val locale = when(langCode) {
            "ES" -> Locale("es", "ES")
            "EN" -> Locale.ENGLISH
            "FR" -> Locale.FRANCE
            "DE" -> Locale.GERMANY
            "IT" -> Locale.ITALIAN
            "PT" -> Locale("pt", "PT")
            "PL" -> Locale("pl", "PL")
            else -> Locale("es", "ES")
        }
        tts?.setLanguage(locale)
        speak(when(langCode) {
            "ES" -> "Idioma cambiado a español"
            "EN" -> "Language changed to English"
            "FR" -> "Langue modifiée en français"
            "DE" -> "Sprache auf Deutsch geändert"
            "IT" -> "Lingua cambiata in italiano"
            "PT" -> "Idioma alterado para português"
            "PL" -> "Język zmieniony na polski"
            else -> "Idioma cambiado"
        })
    }

    fun speak(text: String, pitchOverride: Float? = null, rateOverride: Float? = null) {
        if (_isTtsInitialized.value) {
            val p = pitchOverride ?: if (_isCustomVoiceActive.value) _voicePitch.value else 1.0f
            val r = rateOverride ?: if (_isCustomVoiceActive.value) _voiceRate.value else 1.0f
            tts?.setPitch(p)
            tts?.setSpeechRate(r)

            // Dynamic translator for presets
            val currentLang = _activeLanguage.value
            val isPresetTranslationAvailable = phraseTranslations[text]?.get(currentLang) ?: pictogramTranslations[text]?.get(currentLang)
            val finalSpoken = isPresetTranslationAvailable ?: text

            tts?.speak(finalSpoken, TextToSpeech.QUEUE_FLUSH, null, "NEURON_SPEECH_ID")
        }
    }

    fun configureFamiliarVoice(pitch: Float, rate: Float, label: String, active: Boolean) {
        _voicePitch.value = pitch
        _voiceRate.value = rate
        _customVoiceLabel.value = label
        _isCustomVoiceActive.value = active
        speak("Ajuste de voz familiar guardado")
    }

    fun toggleCustomVoice(active: Boolean) {
        _isCustomVoiceActive.value = active
    }

    fun startRecordingSimulator() {
        _isVoiceRecordingActive.value = true
    }

    fun stopRecordingSimulator(pictogramId: Long) {
        _isVoiceRecordingActive.value = false
        val current = _recordedAudioPathsByPictoId.value.toMutableMap()
        val path = "local_recorded_familiar_voice_${pictogramId}.wav"
        current[pictogramId] = path
        _recordedAudioPathsByPictoId.value = current
        val prefs = getApplication<Application>().getSharedPreferences("neuron_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("picto_audio_$pictogramId", path).apply()
        speak("Audio familiar grabado con éxito para este pictograma")
    }

    fun deleteRecordingForPictogram(pictogramId: Long) {
        val current = _recordedAudioPathsByPictoId.value.toMutableMap()
        current.remove(pictogramId)
        _recordedAudioPathsByPictoId.value = current
        val prefs = getApplication<Application>().getSharedPreferences("neuron_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().remove("picto_audio_$pictogramId").apply()
    }

    // ==========================================
    // AUTH LOGIC
    // ==========================================

    fun login(email: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user == null) {
                onError("El correo no está registrado.")
            } else if (user.pinCode != pin) {
                onError("PIN de seguridad incorrecto.")
            } else {
                repository.activateSession(email)
                _currentScreen.value = "STUDENTS"
                onSuccess()
            }
        }
    }

    fun register(name: String, email: String, pin: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                onError("El correo electrónico ya existe.")
            } else {
                repository.registerUser(name, email, pin)
                repository.activateSession(email)
                _currentScreen.value = "STUDENTS"
                onSuccess()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearSession()
            _activeStudent.value = null
            _phraseCanvas.value = emptyList()
            _currentScreen.value = "AUTH"
        }
    }

    // ==========================================
    // STUDENT LOGIC
    // ==========================================

    fun selectStudent(student: Student) {
        viewModelScope.launch {
            _activeStudent.value = student
            val user = activeUser.value
            if (user != null) {
                repository.updateActiveStudent(user.id, student.id)
            }
            // Seed agenda templates if completely empty
            seedInitialAgendaIfEmpty(student.id)
            generateLocalDiagnostics(student.id)
            _currentScreen.value = "DASHBOARD"
        }
    }

    fun deselectStudent() {
        viewModelScope.launch {
            _activeStudent.value = null
            val user = activeUser.value
            if (user != null) {
                repository.updateActiveStudent(user.id, null)
            }
            _currentScreen.value = "STUDENTS"
        }
    }

    fun addStudent(
        name: String,
        age: Int,
        avatarPreset: Int,
        cognitiveLevel: String,
        difficulties: List<String>,
        strongSkills: List<String>,
        observations: String,
        isSensoryComfort: Boolean
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val student = Student(
                userId = user.id,
                name = name,
                age = age,
                avatarPreset = avatarPreset,
                cognitiveLevel = cognitiveLevel,
                difficulties = difficulties.joinToString(";"),
                strongSkills = strongSkills.joinToString(";"),
                observations = observations,
                lowSensoryMode = isSensoryComfort
            )
            repository.insertStudent(student)
        }
    }

    fun editStudent(
        id: Long,
        name: String,
        age: Int,
        avatarPreset: Int,
        cognitiveLevel: String,
        difficulties: List<String>,
        strongSkills: List<String>,
        observations: String,
        isSensoryComfort: Boolean,
        totalScore: Int
    ) {
        viewModelScope.launch {
            val user = activeUser.value ?: return@launch
            val student = Student(
                id = id,
                userId = user.id,
                name = name,
                age = age,
                avatarPreset = avatarPreset,
                cognitiveLevel = cognitiveLevel,
                difficulties = difficulties.joinToString(";"),
                strongSkills = strongSkills.joinToString(";"),
                observations = observations,
                lowSensoryMode = isSensoryComfort,
                totalScore = totalScore
            )
            repository.insertStudent(student)
            // If the updated student is the active student, update its value in state flow so change reflects immediately
            if (_activeStudent.value?.id == id) {
                _activeStudent.value = student
            }
        }
    }

    suspend fun generateStudentMarkdownReport(student: Student): String {
        val records = repository.getRecordsSync(student.id)
        val sb = StringBuilder()
        sb.append("# REPORTE DE EVALUACIÓN COGNITIVA - NEURON\n")
        sb.append("=========================================\n\n")
        sb.append("## IDENTIFICACIÓN DEL ALUMNO\n")
        sb.append("- **Nombre:** ${student.name}\n")
        sb.append("- **Edad:** ${student.age} años\n")
        sb.append("- **Nivel Cognitivo:** ${student.cognitiveLevel}\n")
        sb.append("- **Entorno Sensorial Reducido:** ${if (student.lowSensoryMode) "Habilitado 🔇" else "Deshabilitado"}\n")
        sb.append("- **Puntuación Acumulada (XP):** ${student.totalScore} Puntos\n")
        sb.append("- **Fecha de Exportación:** ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")
        
        sb.append("## ÁREAS DE APOYO (DIFICULTADES)\n")
        if (student.difficulties.isNotEmpty()) {
            student.difficulties.split(";").forEach {
                sb.append("- $it\n")
            }
        } else {
            sb.append("- Ninguna registrada.\n")
        }
        sb.append("\n")

        sb.append("## PUNTOS FUERTES Y HABILIDADES\n")
        if (student.strongSkills.isNotEmpty()) {
            student.strongSkills.split(";").forEach {
                sb.append("- $it\n")
            }
        } else {
            sb.append("- Ninguno registrado.\n")
        }
        sb.append("\n")

        sb.append("## OBSERVACIONES CLÍNICAS / PEDAGÓGICAS\n")
        if (student.observations.isNotEmpty()) {
            sb.append("${student.observations}\n")
        } else {
            sb.append("Sin observaciones registradas por el docente o familia.\n")
        }
        sb.append("\n")

        sb.append("## REGISTRO DE SESIONES DE JUEGO (NEURODIAGNÓSTICO)\n")
        if (records.isNotEmpty()) {
            sb.append("| Juego/Motor | Puntos | Errores | Tiempo | Fecha |\n")
            sb.append("| :--- | :--- | :--- | :--- | :--- |\n")
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            records.forEach { record ->
                val dateStr = dateFormat.format(java.util.Date(record.dateTimestamp))
                sb.append("| ${record.gameType} | ${record.score} | ${record.errors} | ${record.speedMs / 1000}s | $dateStr |\n")
            }
        } else {
            sb.append("Aún no se han registrado sesiones de juego para este alumno.\n")
        }
        sb.append("\n")

        sb.append("-----------------------------------------\n")
        sb.append("Generado en local de manera 100% privada por NEURON App. Código libre y offline.")
        return sb.toString()
    }

    suspend fun generateStudentJsonReport(student: Student): String {
        val records = repository.getRecordsSync(student.id)
        val recordJsonList = records.map { record ->
            """
            {
              "id": ${record.id},
              "gameType": "${record.gameType}",
              "score": ${record.score},
              "errors": ${record.errors},
              "speedMs": ${record.speedMs},
              "dateTimestamp": ${record.dateTimestamp},
              "notes": "${record.notes.replace("\"", "\\\"")}"
            }
            """.trimIndent()
        }.joinToString(separator = ",\n")

        return """
        {
          "neuronExportVersion": 1,
          "app": "NEURON",
          "provider": "Synapse Studio",
          "exportTimestamp": ${System.currentTimeMillis()},
          "student": {
            "id": ${student.id},
            "name": "${student.name.replace("\"", "\\\"")}",
            "age": ${student.age},
            "avatarPreset": ${student.avatarPreset},
            "cognitiveLevel": "${student.cognitiveLevel}",
            "lowSensoryMode": ${student.lowSensoryMode},
            "totalScore": ${student.totalScore},
            "difficulties": "${student.difficulties.replace("\"", "\\\"")}",
            "strongSkills": "${student.strongSkills.replace("\"", "\\\"")}",
            "observations": "${student.observations.replace("\"", "\\\"")}"
          },
          "gameRecords": [
            $recordJsonList
          ]
        }
        """.trimIndent()
    }

    fun importStudentFromJson(jsonStr: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = org.json.JSONObject(jsonStr)
                if (!json.has("app") || json.getString("app") != "NEURON") {
                    onError("El formato del texto no corresponde a un respaldo válido de NEURON.")
                    return@launch
                }
                
                val studentObj = json.getJSONObject("student")
                val user = activeUser.value ?: return@launch
                
                val parsedStudent = Student(
                    userId = user.id,
                    name = studentObj.getString("name"),
                    age = studentObj.getInt("age"),
                    avatarPreset = studentObj.optInt("avatarPreset", 0),
                    cognitiveLevel = studentObj.getString("cognitiveLevel"),
                    difficulties = studentObj.getString("difficulties"),
                    strongSkills = studentObj.getString("strongSkills"),
                    observations = studentObj.optString("observations", ""),
                    lowSensoryMode = studentObj.getBoolean("lowSensoryMode"),
                    totalScore = studentObj.optInt("totalScore", 0)
                )
                
                val newStudentId = repository.insertStudent(parsedStudent)
                
                // Now import game records if any
                if (json.has("gameRecords")) {
                    val recordsArray = json.getJSONArray("gameRecords")
                    for (i in 0 until recordsArray.length()) {
                        val recordObj = recordsArray.getJSONObject(i)
                        repository.insertGamePlayRecord(
                            com.example.data.GamePlayRecord(
                                studentId = newStudentId,
                                gameType = recordObj.getString("gameType"),
                                score = recordObj.getInt("score"),
                                errors = recordObj.getInt("errors"),
                                speedMs = recordObj.getLong("speedMs"),
                                dateTimestamp = recordObj.optLong("dateTimestamp", System.currentTimeMillis()),
                                notes = recordObj.optString("notes", "")
                            )
                        )
                    }
                }
                
                // Seed initial agenda if empty
                seedInitialAgendaIfEmpty(newStudentId)
                generateLocalDiagnostics(newStudentId)
                onSuccess()
            } catch (e: Exception) {
                Log.e("NEURON_IMPORT", "Error importing student", e)
                onError("Error al decodificar la estructura JSON. Asegúrate de copiar el texto completo de respaldo.")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            if (_activeStudent.value?.id == student.id) {
                _activeStudent.value = null
            }
            repository.deleteStudent(student)
        }
    }

    // Toggle low-sensory mode directly
    fun toggleSensoryMode() {
        val student = _activeStudent.value ?: return
        val currentMode = student.lowSensoryMode
        viewModelScope.launch {
            repository.updateSensoryMode(student.id, !currentMode)
            _activeStudent.value = student.copy(lowSensoryMode = !currentMode)
        }
    }

    // ==========================================
    // AGENDA LOGIC
    // ==========================================

    fun completeAgendaItem(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.updateAgendaItemCompletion(id, completed)
        }
    }

    fun addAgendaItem(title: String, time: String, icon: String, colorHex: String) {
        val student = _activeStudent.value ?: return
        viewModelScope.launch {
            val itemsCount = agendaItems.value.size
            repository.insertAgendaItem(
                AgendaItem(
                    studentId = student.id,
                    title = title,
                    timeOfDay = time,
                    iconName = icon,
                    colorHex = colorHex,
                    sequenceOrder = itemsCount
                )
            )
        }
    }

    fun removeAgendaItem(id: Long) {
        viewModelScope.launch {
            repository.deleteAgendaItem(id)
        }
    }

    private suspend fun seedInitialAgendaIfEmpty(studentId: Long) {
        // Check if there's any routine items for the pupil
        val currentList = db.agendaDao().getAgendaFlow(studentId).first() ?: emptyList()
        if (currentList.isEmpty()) {
            val defaults = listOf(
                AgendaItem(studentId = studentId, title = "Rutina de Mañana: Vestirse", timeOfDay = "08:30", iconName = "bedshirt", colorHex = "#FF3B82F6", sequenceOrder = 0),
                AgendaItem(studentId = studentId, title = "Desayuno Nutritivo", timeOfDay = "09:00", iconName = "restaurant", colorHex = "#FFF59E0B", sequenceOrder = 1),
                AgendaItem(studentId = studentId, title = "Sesión Cognitiva: Neuron", timeOfDay = "10:00", iconName = "school", colorHex = "#FF10B981", sequenceOrder = 2),
                AgendaItem(studentId = studentId, title = "Lavarse las manos", timeOfDay = "12:15", iconName = "wash", colorHex = "#FF06B6D4", sequenceOrder = 3),
                AgendaItem(studentId = studentId, title = "Almuerzo", timeOfDay = "13:00", iconName = "restaurant", colorHex = "#FFF59E0B", sequenceOrder = 4),
                AgendaItem(studentId = studentId, title = "Higiene Dental", timeOfDay = "13:45", iconName = "brush", colorHex = "#FF3B82F6", sequenceOrder = 5),
                AgendaItem(studentId = studentId, title = "Tiempo de Ocio / Juego Libre", timeOfDay = "17:00", iconName = "videogame_asset", colorHex = "#FF8B5CF6", sequenceOrder = 6)
            )
            defaults.forEach {
                repository.insertAgendaItem(it)
            }
        }
    }

    // ==========================================
    // CAA / AAC CANVAS LOGIC
    // ==========================================

    fun addPictoToCanvas(picto: Pictogram) {
        speak(picto.label)
        val current = _phraseCanvas.value.toMutableList()
        current.add(picto)
        _phraseCanvas.value = current
    }

    fun removePictoFromCanvas(index: Int) {
        val current = _phraseCanvas.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _phraseCanvas.value = current
        }
    }

    fun clearCanvas() {
        _phraseCanvas.value = emptyList()
    }

    fun playCanvasSequence() {
        val sentence = _phraseCanvas.value.joinToString(separator = ", ") { it.speechText }
        if (sentence.isNotEmpty()) {
            speak(sentence)
        }
    }

    fun addCustomPictogram(label: String, phrase: String, category: String, colorHex: String, iconName: String?, imagePath: String?) {
        val student = _activeStudent.value ?: return
        viewModelScope.launch {
            repository.insertPictogram(
                Pictogram(
                    studentId = student.id,
                    label = label,
                    speechText = phrase.ifEmpty { label },
                    category = category,
                    colorHex = colorHex,
                    localImageUri = imagePath,
                    presetIconName = iconName,
                    isCustom = true
                )
            )
        }
    }

    fun deletePictogram(picto: Pictogram) {
        viewModelScope.launch {
            repository.deletePictogram(picto)
        }
    }

    fun editPictogram(picto: Pictogram) {
        viewModelScope.launch {
            repository.insertPictogram(picto)
        }
    }

    fun getUiTranslation(key: String): String {
        val currentLang = _activeLanguage.value
        val strings = mapOf(
            "agenda" to mapOf("ES" to "Agenda", "EN" to "Schedule", "FR" to "Emploi du temps", "DE" to "Kalender", "IT" to "Agenda", "PT" to "Agenda", "PL" to "Terminarz"),
            "caa" to mapOf("ES" to "Lienzo CAA", "EN" to "AAC Canvas", "FR" to "Canevas CAA", "DE" to "UK-Tafel", "IT" to "Tela CAA", "PT" to "Painel CAA", "PL" to "Tablica AAC"),
            "juegos" to mapOf("ES" to "Juegos", "EN" to "Games", "FR" to "Jeux", "DE" to "Spiele", "IT" to "Giochi", "PT" to "Jogos", "PL" to "Gry"),
            "analiticas" to mapOf("ES" to "Analítica", "EN" to "Analytics", "FR" to "Analytique", "DE" to "Statistiken", "IT" to "Analitica", "PT" to "Analítica", "PL" to "Statystyki"),
            "ocio" to mapOf("ES" to "Recreo", "EN" to "Leisure", "FR" to "Loisirs", "DE" to "Freizeit", "IT" to "Tempo libero", "PT" to "Lazer", "PL" to "Czas wolny"),
            "edit_picto" to mapOf("ES" to "Configurar Pictograma", "EN" to "Configure Pictograph", "FR" to "Configurer le pictogramme", "DE" to "Piktogramm konfigurieren", "IT" to "Configura pittogramma", "PT" to "Configurar pictograma", "PL" to "Skonfiguruj piktogram"),
            "custom_audio" to mapOf("ES" to "Graba Voz Familiar", "EN" to "Record Familiar Voice", "FR" to "Enregistrer voix familière", "DE" to "Vertraute Stimme aufnehmen", "IT" to "Registra voce familiare", "PT" to "Gravar voz familiar", "PL" to "Nagraj znany głos"),
            "voice_familiar" to mapOf("ES" to "Ajuste Frecuencia Vocal", "EN" to "Acoustic Tuning", "FR" to "Ajustement de la voix", "DE" to "Feineinstellung der Synthese", "IT" to "Modula frequenza vocale", "PT" to "Frecuência vocal", "PL" to "Syntezator głosu"),
            "pitch" to mapOf("ES" to "Tono", "EN" to "Pitch", "FR" to "Frecuénce", "DE" to "Stimmton", "IT" to "Tono della voce", "PT" to "Tono", "PL" to "Ton"),
            "speed" to mapOf("ES" to "Velocidad", "EN" to "Speed", "FR" to "Vitesse", "DE" to "Geschwindigkeit", "IT" to "Velocità", "PT" to "Velocidade", "PL" to "Prędkość")
        )
        return strings[key]?.get(currentLang) ?: strings[key]?.get("ES") ?: key
    }

    fun saveActiveCanvasTemplate(title: String) {
        val student = _activeStudent.value ?: return
        val text = _phraseCanvas.value.map { it.id }.joinToString(separator = ",")
        if (text.isEmpty()) return
        viewModelScope.launch {
            repository.insertSavedCanvas(
                SavedCanvas(
                    studentId = student.id,
                    title = title,
                    serializedPictosIds = text
                )
            )
        }
    }

    fun loadCanvasTemplate(template: SavedCanvas) {
        viewModelScope.launch {
            val allDict = repository.getPictogramsSync(_activeStudent.value?.id)
            val ids = template.serializedPictosIds.split(",").mapNotNull { it.toLongOrNull() }
            val loaded = ids.mapNotNull { id ->
                allDict.find { it.id == id }
            }
            _phraseCanvas.value = loaded
            playCanvasSequence()
        }
    }

    fun deleteCanvasTemplate(canvasId: Long) {
        viewModelScope.launch {
            repository.deleteSavedCanvas(canvasId)
        }
    }

    fun saveCustomGameConfig(config: CustomGameConfig) {
        viewModelScope.launch {
            repository.insertCustomGameConfig(config)
            speak("Configuración de juego guardada: ${config.title}")
        }
    }

    fun deleteCustomGameConfig(configId: Long) {
        viewModelScope.launch {
            repository.deleteCustomGameConfig(configId)
            speak("Configuración eliminada")
        }
    }

    // ==========================================
    // CLINICAL INTEL-PLAY HISTORY LOGIC
    // ==========================================

    fun recordGameScore(game: String, earnedScore: Int, errorsCount: Int, speedSec: Int, notes: String = "") {
        val student = _activeStudent.value ?: return
        viewModelScope.launch {
            // Log game record
            repository.insertGamePlayRecord(
                GamePlayRecord(
                    studentId = student.id,
                    gameType = game,
                    score = earnedScore,
                    errors = errorsCount,
                    speedMs = speedSec.toLong() * 1000L,
                    notes = notes
                )
            )
            // Add student total XP score
            repository.awardXp(student.id, earnedScore)
            // Refresh diagnostic calculations
            generateLocalDiagnostics(student.id)
        }
    }

    // ==========================================
    // LOCAL INTELLIGENT AI EXCEL ANALYSIS
    // ==========================================

    private suspend fun generateLocalDiagnostics(studentId: Long) {
        val list = repository.getRecordsSync(studentId)
        val student = repository.getStudentById(studentId)
        if (list.isEmpty() || student == null) {
            _diagnosticReport.value = DiagnosticReport(
                totalGames = 0,
                averageAccuracy = 0f,
                dominantSkill = "Evaluando...",
                improvingArea = "Inicia una actividad",
                detailedPatterns = "Juega por lo menos un juego para que la IA neuronal local calcule patrones cognitivos de aprendizaje.",
                performanceTrends = emptyList()
            )
            return
        }

        val totalGames = list.size
        val totalErrors = list.sumOf { it.errors }
        val avgErrors = totalErrors.toFloat() / totalGames
        // Safety accuracy percentage mapping
        val avgAccuracy = (100f - (avgErrors * 15f)).coerceIn(10f, 100f)

        // Aggregated stats grouped by game types
        val groupedGames = list.groupBy { it.gameType }
        val performanceTrends = groupedGames.map { (gameType, records) ->
            val totalGameErrors = records.sumOf { it.errors }
            val avgSpeedSeg = records.map { it.speedMs / 1000f }.average()
            val scoreAvg = records.map { it.score }.average()
            GamePerformanceTrend(
                gameName = gameType,
                playCount = records.size,
                avgErrors = totalGameErrors.toFloat() / records.size,
                avgSpeedSec = avgSpeedSeg.toFloat(),
                avgScore = scoreAvg.toFloat()
            )
        }

        // Determine dominant and weak categories based on metrics
        val sortedByAccuracy = performanceTrends.sortedBy { it.avgErrors }
        val dominantSkill = when {
            sortedByAccuracy.first().avgErrors <= 0.5f -> "${sortedByAccuracy.first().gameName} (Comprensión Alta)"
            else -> "Atención Visual"
        }

        val weakCategoryName = sortedByAccuracy.last().gameName
        val improvingArea = when (weakCategoryName) {
            "LETRAS" -> "Discriminación de fonemas del lenguaje"
            "PAREJAS" -> "Asociación visual lógica"
            "MEMORIA" -> "Retención visual y memoria secuencial"
            "COMPRENSION" -> "Juicios lógicos y comprensión de lectura"
            "FONETICA" -> "Asociación fonema-grafema"
            "EJECUTIVAS" -> "Secuenciación motora y ordenación de pasos"
            "SOCIAL" -> "Empatía y relaciones interpersonales"
            "AUTONOMIA" -> "Control ejecutivo de seguridad cotidiana"
            "EXPLORACION" -> "Detección de objetos e intrusos"
            "MATEMATICAS" -> "Razonamiento y conteo visual"
            "PICTOGRAMAS" -> "Construcción gramatical de frases"
            else -> "Velocidad de procesamiento"
        }

        // Automated Clinical feedback notes - HIGHLY DETAILED & PROFESSIONAL
        val detailedPatternsBuilder = StringBuilder()
        detailedPatternsBuilder.append("📋 DOSSIER DE SEGUIMIENTO CLÍNICO PEDAGÓGICO (SoBerano y Local):\n\n")
        detailedPatternsBuilder.append("• Alumno: ${student.name} | Edad: ${student.age} años | Nivel: ${student.cognitiveLevel}\n")
        detailedPatternsBuilder.append("• Historial Clínico: Semicolones de Dificultad: [${student.difficulties}] / Puntos Fuertes: [${student.strongSkills}]\n")
        detailedPatternsBuilder.append("• Muestra de Sesiones: $totalGames actividades completadas localmente.\n\n")

        detailedPatternsBuilder.append("🔍 ANÁLISIS CONDUCTUAL DE EJECUCIÓN:\n")
        if (avgAccuracy >= 85f) {
            detailedPatternsBuilder.append(" - Precisión Extraordinaria: El alumno muestra una asimilación excepcional de consignas visuales secundarias.\n")
        } else if (avgAccuracy >= 65f) {
            detailedPatternsBuilder.append(" - Progreso Moderado: Presenta impulsividad intermitente al pulsar. Sugerimos modelado guiado por tutor.\n")
        } else {
            detailedPatternsBuilder.append(" - Impulsividad Crítica: Elevada tasa de error por tanteo rápido. Activar el entorno 'Sensory-Comfort' es prioritario para reducir el estrés visual interno.\n")
        }

        // Cross referencing difficulty categories explicitly to build custom therapeutic suggestions
        detailedPatternsBuilder.append("\n💡 COGNITIVE REMEDIATION (Recomendaciones Clínicas Personalizadas):\n")
        val studentDiffs = student.difficulties.lowercase()

        if (studentDiffs.contains("habla") || studentDiffs.contains("verbal")) {
            detailedPatternsBuilder.append(" - Habla No Verbal / CAA: Usar el panel de Comunicación Aumentativa de forma diaria para oraciones de 3 palabras. Trabajar con el pictograma 'Quiero' + 'Comer/Beber' para fijar el circuito pragmático.\n")
        }
        if (studentDiffs.contains("atención") || studentDiffs.contains("tdah")) {
            detailedPatternsBuilder.append(" - TDAH / Atención: Las sesiones no deben superar los 10 minutos seguidos. Recomendamos estructurar las tareas escolares usando la 'Agenda Visual Temprana' con recompensas de ocio intermedio.\n")
        }
        if (studentDiffs.contains("sensorial") || studentDiffs.contains("tea") || student.lowSensoryMode) {
            detailedPatternsBuilder.append(" - TEA / Hipersensibilidad: Éxito con el entorno amortiguado. Evitar estridencias. Utilizar el motor lúdico en velocidad reducida 'Sensory-Soft' para evitar rechazo por frustración.\n")
        }
        if (studentDiffs.contains("motora")) {
            detailedPatternsBuilder.append(" - Coordinación Fina / Motora: El juego retro de Dendritas (Neuron Jumper) es vital. Se recomienda realizar 3 partidas al día haciendo énfasis en presionar las flechas direccionales físicas inferiores modificadas.\n")
        }
        if (studentDiffs.contains("lectoescritura")) {
            detailedPatternsBuilder.append(" - Lectoescritura: Ejercitar preferentemente el Motor de Letras ('CASA' y otros pictos) y el Motor de Fonemas para consolidar la ruta fonológica cerebral.\n")
        }

        detailedPatternsBuilder.append("\n📈 PATRONES POR MOTOR JUGADO:\n")
        performanceTrends.forEach { trend ->
            detailedPatternsBuilder.append(" - ${trend.gameName}: ${trend.playCount} partidas | Eficacia promedio: ${(100f - trend.avgErrors * 15f).coerceIn(10f, 100f).toInt()}% | T. Reacción Medio: ${"%.2f".format(trend.avgSpeedSec)} Seg.\n")
        }

        detailedPatternsBuilder.append("\n⭐ RUTA DE REFUERZO PRIORITARIA: Estimular '${dominantSkill}' como ancla de autoestima, mientras estimulamos con andamiaje '${improvingArea}'.")

        _diagnosticReport.value = DiagnosticReport(
            totalGames = totalGames,
            averageAccuracy = avgAccuracy,
            dominantSkill = dominantSkill,
            improvingArea = improvingArea,
            detailedPatterns = detailedPatternsBuilder.toString(),
            performanceTrends = performanceTrends
        )
    }

    override fun onCleared() {
        tts?.shutdown()
        super.onCleared()
    }
}

// Data structures for Diagnostic Analysis
data class DiagnosticReport(
    val totalGames: Int,
    val averageAccuracy: Float,
    val dominantSkill: String,
    val improvingArea: String,
    val detailedPatterns: String,
    val performanceTrends: List<GamePerformanceTrend>
)

data class GamePerformanceTrend(
    val gameName: String,
    val playCount: Int,
    val avgErrors: Float,
    val avgSpeedSec: Float,
    val avgScore: Float
)
