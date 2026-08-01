package be.hablamos.app.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.hablamos.app.ai.AiCoachService
import be.hablamos.app.data.Lesson
import be.hablamos.app.data.ProfileStore
import be.hablamos.app.data.UserProfile
import be.hablamos.app.data.lessons
import kotlinx.coroutines.launch
import java.util.Locale

private enum class Screen { Home, Lessons, Coach, Profile }
private data class PlacementQuestion(val text: String, val options: List<String>, val answer: Int)

private val placementQuestions = listOf(
    PlacementQuestion("Que signifie « Buenos días » ?", listOf("Bonsoir", "Bonjour", "À demain"), 1),
    PlacementQuestion("Complète : Me ___ Jonathan.", listOf("llamo", "soy", "tengo"), 0),
    PlacementQuestion("Choisis la bonne phrase pour commander.", listOf("Quisiera una paella.", "Tengo una paella.", "Estoy una paella."), 0),
    PlacementQuestion("Que signifie « Ayer fui al mercado » ?", listOf("Demain j'irai au marché", "Hier je suis allé au marché", "Je vais souvent au marché"), 1),
    PlacementQuestion("Complète : Si tuviera tiempo, ___ más.", listOf("viajo", "viajaría", "viajé"), 1),
    PlacementQuestion("Quel mot introduit généralement une opposition ?", listOf("aunque", "porque", "entonces"), 0)
)

@Composable
fun HablamosApp() {
    val context = LocalContext.current
    val store = remember { ProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }

    MaterialTheme {
        if (!profile.onboardingComplete) {
            OnboardingScreen { result ->
                profile = result
                store.save(result)
            }
        } else {
            MainExperience(profile = profile, onProfileChange = {
                profile = it
                store.save(it)
            }, onReset = {
                store.reset()
                profile = UserProfile()
            })
        }
    }
}

@Composable
private fun OnboardingScreen(onFinished: (UserProfile) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Voyager") }
    var dailyMinutes by remember { mutableIntStateOf(10) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }

    val gradient = Brush.verticalGradient(listOf(Color(0xFFFF735C), Color(0xFFFFA25C)))
    Box(Modifier.fillMaxSize().background(gradient).padding(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth())
                when (step) {
                    0 -> {
                        Text("¡Hola!", fontSize = 38.sp, fontWeight = FontWeight.Bold)
                        Text("Créons un parcours d'espagnol adapté à ton niveau et à tes objectifs.")
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ton prénom") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Continuer") }
                    }
                    1 -> {
                        Text("Ton objectif", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        listOf("Voyager", "Converser", "Travail", "Vivre en Espagne").forEach { option ->
                            FilterChip(selected = goal == option, onClick = { goal = option }, label = { Text(option) }, leadingIcon = {
                                Icon(if (goal == option) Icons.Default.Check else Icons.Default.Flag, null)
                            })
                        }
                        Text("Temps quotidien", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15, 20).forEach { minutes ->
                                FilterChip(selected = dailyMinutes == minutes, onClick = { dailyMinutes = minutes }, label = { Text("$minutes min") })
                            }
                        }
                        Button(onClick = { step = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Évaluer mon niveau") }
                    }
                    2 -> {
                        val q = placementQuestions[questionIndex]
                        Text("Test de niveau", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Question ${questionIndex + 1}/${placementQuestions.size}")
                        Text(q.text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        q.options.forEachIndexed { index, option ->
                            OutlinedButton(onClick = {
                                if (index == q.answer) score++
                                if (questionIndex == placementQuestions.lastIndex) step = 3 else questionIndex++
                            }, modifier = Modifier.fillMaxWidth()) { Text(option) }
                        }
                    }
                    else -> {
                        val level = levelForScore(score)
                        Text("Ton niveau estimé", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(level, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Score : $score/${placementQuestions.size}")
                        Text(levelDescription(level))
                        Button(onClick = {
                            onFinished(UserProfile(name.ifBlank { "Amigo" }, level, goal, dailyMinutes, score, true))
                        }, modifier = Modifier.fillMaxWidth()) { Text("Créer mon parcours") }
                    }
                }
            }
        }
    }
}

private fun levelForScore(score: Int) = when (score) {
    in 0..1 -> "A1"
    in 2..3 -> "A2"
    4 -> "B1"
    else -> "B2"
}

private fun levelDescription(level: String) = when (level) {
    "A1" -> "Tu vas construire des bases solides avec des phrases très pratiques."
    "A2" -> "Tu connais déjà les bases. Le parcours renforcera ta fluidité au quotidien."
    "B1" -> "Tu peux communiquer. Le parcours développera précision et spontanéité."
    else -> "Tu as un bon niveau. Le parcours ciblera les nuances et les conversations naturelles."
}

@Composable
private fun MainExperience(profile: UserProfile, onProfileChange: (UserProfile) -> Unit, onReset: () -> Unit) {
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    var completed by remember { mutableIntStateOf(0) }

    Scaffold(bottomBar = {
        NavigationBar {
            listOf(
                Triple(Screen.Home, Icons.Default.Home, "Accueil"),
                Triple(Screen.Lessons, Icons.AutoMirrored.Filled.MenuBook, "Parcours"),
                Triple(Screen.Coach, Icons.Default.AutoAwesome, "Coach IA"),
                Triple(Screen.Profile, Icons.Default.Person, "Profil")
            ).forEach { item ->
                NavigationBarItem(selected = screen == item.first, onClick = { screen = item.first; selectedLesson = null }, icon = { Icon(item.second, null) }, label = { Text(item.third) })
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                selectedLesson != null -> LessonScreen(selectedLesson!!) {
                    completed = (completed + 1).coerceAtMost(lessons.size)
                    selectedLesson = null
                    screen = Screen.Home
                }
                screen == Screen.Home -> ModernHome(profile, completed) { screen = Screen.Lessons }
                screen == Screen.Lessons -> AdaptiveLessons(profile.level) { selectedLesson = it }
                screen == Screen.Coach -> CoachScreen(profile)
                screen == Screen.Profile -> ProfileScreen(profile, onProfileChange, onReset)
            }
        }
    }
}

@Composable
private fun ModernHome(profile: UserProfile, completed: Int, onContinue: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFFF735C), Color(0xFFFFA25C))), RoundedCornerShape(28.dp)).padding(22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¡Hola, ${profile.name}!", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Niveau ${profile.level} • Objectif ${profile.goal}", color = Color.White.copy(alpha = .9f))
                    LinearProgressIndicator(progress = { completed / lessons.size.toFloat() }, modifier = Modifier.fillMaxWidth(), color = Color.White)
                    Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFE65D47))) {
                        Icon(Icons.Default.PlayArrow, null); Text(" Continuer mon parcours")
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("🔥", "3 jours", "Série", Modifier.weight(1f))
                MetricCard("⭐", "${completed * 40}", "XP", Modifier.weight(1f))
                MetricCard("⏱️", "${profile.dailyMinutes} min", "Objectif", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Recommandé pour toi", "Adapté à ton niveau ${profile.level}") }
        item {
            Card(onClick = onContinue, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (profile.goal == "Voyager") "✈️" else "💬", fontSize = 40.sp)
                    Column(Modifier.weight(1f)) {
                        Text(if (profile.level == "A1") "Premiers échanges" else "Conversation naturelle", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Text("Une séance courte avec vocabulaire, écoute et mise en situation.")
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
        item { SectionTitle("Défi du jour", "Parle pendant 2 minutes") }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎙️ Présente ta journée", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("Utilise aujourd'hui, ensuite et parce que. Le Coach IA peut te corriger.")
                }
            }
        }
    }
}

@Composable private fun MetricCard(icon: String, value: String, label: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(icon); Text(value, fontWeight = FontWeight.Bold); Text(label, fontSize = 12.sp) } }
}

@Composable private fun SectionTitle(title: String, subtitle: String) { Column { Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
private fun AdaptiveLessons(level: String, onLessonClick: (Lesson) -> Unit) {
    val recommended = if (level in listOf("A1", "A2")) lessons else lessons.reversed()
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Ton parcours $level", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Les unités sont ordonnées selon ton évaluation initiale.") }
        items(recommended) { lesson ->
            Card(onClick = { onLessonClick(lesson) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(lesson.emoji, fontSize = 30.sp) }
                    Column(Modifier.weight(1f)) { Text(lesson.title, fontWeight = FontWeight.Bold, fontSize = 19.sp); Text(lesson.subtitle); Text("${lesson.phrases.size} activités", color = MaterialTheme.colorScheme.primary) }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun CoachScreen(profile: UserProfile) {
    val scope = rememberCoroutineScope()
    val service = remember { AiCoachService() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf("Coach" to "¡Hola! Je suis ton coach. Écris-moi en français ou en espagnol et je t'aiderai à progresser.")) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null) }
            Column { Text("Coach IA", fontSize = 25.sp, fontWeight = FontWeight.Bold); Text("Conversation adaptée au niveau ${profile.level}") }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { message ->
                val user = message.first == "Moi"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(.84f)) {
                        Text(message.second, Modifier.padding(14.dp), color = if (user) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = input, onValueChange = { input = it }, placeholder = { Text("Écris une phrase…") }, modifier = Modifier.weight(1f), maxLines = 3)
            FilledIconButton(enabled = input.isNotBlank() && !loading, onClick = {
                val sent = input.trim(); input = ""; messages = messages + ("Moi" to sent); loading = true
                scope.launch {
                    val reply = service.reply(sent, profile.level, profile.goal)
                    val detail = listOfNotNull(reply.text, reply.correction?.let { "Correction : $it" }, reply.suggestion?.let { "Suggestion : $it" }).joinToString("\n\n")
                    messages = messages + ("Coach" to detail); loading = false
                }
            }) { Icon(Icons.Default.Send, null) }
        }
        Text("Mode démo local actif. Un backend sécurisé peut être branché sans intégrer de clé API dans l'APK.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileScreen(profile: UserProfile, onChange: (UserProfile) -> Unit, onReset: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Mon profil", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(profile.name, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("Niveau ${profile.level} • ${profile.goal}")
                Text("Résultat initial : ${profile.placementScore}/${placementQuestions.size}")
            } }
        }
        item { Text("Objectif quotidien", fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(5, 10, 15, 20).forEach { m -> FilterChip(selected = profile.dailyMinutes == m, onClick = { onChange(profile.copy(dailyMinutes = m)) }, label = { Text("$m min") }) } } }
        item { OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.RestartAlt, null); Text(" Refaire l'évaluation") } }
    }
}

@Composable
private fun LessonScreen(lesson: Lesson, onComplete: () -> Unit) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("es", "ES") }
        tts = engine
        onDispose { engine.shutdown() }
    }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("${lesson.emoji} ${lesson.title}", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(lesson.subtitle) }
        items(lesson.phrases) { phrase ->
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(phrase.spanish, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(phrase.french)
                    Text(phrase.hint, color = MaterialTheme.colorScheme.primary)
                    OutlinedButton(onClick = { tts?.speak(phrase.spanish, TextToSpeech.QUEUE_FLUSH, null, phrase.spanish) }) { Icon(Icons.Default.VolumeUp, null); Text(" Écouter") }
                }
            }
        }
        item { Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Terminer et gagner 40 XP") } }
    }
}
