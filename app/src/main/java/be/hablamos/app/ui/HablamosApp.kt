package be.hablamos.app.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.hablamos.app.data.Lesson
import be.hablamos.app.data.lessons
import java.util.Locale

private enum class Screen { Home, Lessons, Quiz }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HablamosApp() {
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    var completedLessons by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Hablamos") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = screen == Screen.Home,
                    onClick = { screen = Screen.Home; selectedLesson = null },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Accueil") }
                )
                NavigationBarItem(
                    selected = screen == Screen.Lessons,
                    onClick = { screen = Screen.Lessons; selectedLesson = null },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Leçons") }
                )
                NavigationBarItem(
                    selected = screen == Screen.Quiz,
                    onClick = { screen = Screen.Quiz; selectedLesson = null },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text("Quiz") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                selectedLesson != null -> LessonScreen(
                    lesson = selectedLesson!!,
                    onComplete = {
                        completedLessons = (completedLessons + 1).coerceAtMost(lessons.size)
                        selectedLesson = null
                        screen = Screen.Home
                    }
                )
                screen == Screen.Home -> HomeScreen(completedLessons) {
                    screen = Screen.Lessons
                }
                screen == Screen.Lessons -> LessonsScreen { selectedLesson = it }
                screen == Screen.Quiz -> QuizScreen()
            }
        }
    }
}

@Composable
private fun HomeScreen(completedLessons: Int, onStart: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("¡Buenos días!", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Apprends l'espagnol utile, une situation à la fois.")
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Objectif du jour", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("10 minutes • 1 leçon • 5 nouveaux mots")
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Commencer")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Column {
                        Text("Progression", fontWeight = FontWeight.Bold)
                        Text("$completedLessons/${lessons.size} leçons terminées")
                    }
                }
            }
        }
        item {
            Text("Phrase du jour", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Poco a poco se llega lejos.", fontWeight = FontWeight.Bold)
                    Text("Petit à petit, on va loin.")
                }
            }
        }
    }
}

@Composable
private fun LessonsScreen(onLessonClick: (Lesson) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("Leçons", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Des phrases concrètes pour parler en Espagne.")
        }
        items(lessons) { lesson ->
            Card(onClick = { onLessonClick(lesson) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(lesson.emoji, fontSize = 34.sp)
                    Column {
                        Text(lesson.title, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text(lesson.subtitle)
                        Text("${lesson.phrases.size} phrases", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun LessonScreen(lesson: Lesson, onComplete: () -> Unit) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
            }
        }
        tts = engine
        onDispose { engine.shutdown() }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            Text("${lesson.emoji} ${lesson.title}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(lesson.subtitle)
        }
        items(lesson.phrases) { phrase ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text(phrase.spanish, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(phrase.french)
                    Spacer(Modifier.height(8.dp))
                    Text(phrase.hint, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        tts?.speak(phrase.spanish, TextToSpeech.QUEUE_FLUSH, null, phrase.spanish)
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Écouter")
                    }
                }
            }
        }
        item {
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Terminer la leçon")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuizScreen() {
    val questions = remember {
        listOf(
            Triple("Comment dit-on « l'addition » ?", listOf("La carta", "La cuenta", "La mesa"), 1),
            Triple("Que signifie « ¿Cuánto cuesta? » ?", listOf("Quelle heure est-il ?", "Combien cela coûte ?", "Où est la gare ?"), 1),
            Triple("Comment dit-on « tout droit » ?", listOf("Todo recto", "A la izquierda", "Muy lejos"), 0)
        )
    }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var answered by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }

    val question = questions[index]
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Quiz rapide", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Question ${index + 1}/${questions.size} • Score : $score")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(question.first, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                question.second.forEachIndexed { optionIndex, option ->
                    Button(
                        onClick = {
                            if (!answered) {
                                answered = true
                                if (optionIndex == question.third) {
                                    score += 1
                                    feedback = "¡Muy bien!"
                                } else {
                                    feedback = "Bonne réponse : ${question.second[question.third]}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !answered
                    ) { Text(option) }
                }
                if (answered) {
                    Text(feedback, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            answered = false
                            feedback = ""
                            index = if (index == questions.lastIndex) 0 else index + 1
                            if (index == 0) score = 0
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (index == questions.lastIndex) "Recommencer" else "Question suivante")
                    }
                }
            }
        }
    }
}
