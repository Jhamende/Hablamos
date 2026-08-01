package be.hablamos.app.ui

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.hablamos.app.ai.ConversationMessage
import be.hablamos.app.ai.SecureAiClient
import be.hablamos.app.data.ProfileStore
import be.hablamos.app.data.UserProfile
import kotlinx.coroutines.launch
import java.util.Locale

private enum class FunScreen { Home, Play, Coach, Profile }
private data class MiniLesson(val mascot: String, val name: String, val phrase: String, val translation: String, val accent: Color)
private data class FunQuestion(val mascot: String, val prompt: String, val answers: List<String>, val correct: Int)

private val miniLessons = listOf(
    MiniLesson("🦊", "Lola", "¡Hola! ¿Cómo estás?", "Bonjour ! Comment vas-tu ?", Color(0xFFFF8A65)),
    MiniLesson("🐼", "Paco", "Quisiera una mesa para cuatro.", "Je voudrais une table pour quatre.", Color(0xFF6C5CE7)),
    MiniLesson("🐸", "Rana", "¿Dónde está la estación?", "Où est la gare ?", Color(0xFF00B894)),
    MiniLesson("🐱", "Mia", "Me gusta viajar con mi familia.", "J'aime voyager avec ma famille.", Color(0xFFE84393))
)
private val funQuestions = listOf(
    FunQuestion("🦊", "Que signifie « ¿Cómo estás? » ?", listOf("Comment vas-tu ?", "Où vas-tu ?", "Quel âge as-tu ?"), 0),
    FunQuestion("🐼", "Complète : Me ___ la paella.", listOf("gusta", "gusto", "gustas"), 0),
    FunQuestion("🐸", "Comment dit-on « la gare » ?", listOf("la calle", "la estación", "la cuenta"), 1),
    FunQuestion("🐱", "Choisis la phrase naturelle.", listOf("Tengo 34 años", "Estoy 34 años", "Soy 34 años"), 0)
)

@Composable
fun HablamosV4App() {
    val context = LocalContext.current
    val store = remember { ProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }
    if (!profile.onboardingComplete) { HablamosApp(); return }

    var screen by remember { mutableStateOf(FunScreen.Home) }
    var xp by remember { mutableIntStateOf(160) }
    var hearts by remember { mutableIntStateOf(5) }

    Scaffold(bottomBar = {
        NavigationBar {
            listOf(
                Triple(FunScreen.Home, Icons.Default.Home, "Accueil"),
                Triple(FunScreen.Play, Icons.Default.SportsEsports, "Jouer"),
                Triple(FunScreen.Coach, Icons.Default.AutoAwesome, "Coach"),
                Triple(FunScreen.Profile, Icons.Default.Person, "Profil")
            ).forEach { item -> NavigationBarItem(screen == item.first, { screen = item.first }, { Icon(item.second, null) }, label = { Text(item.third) }) }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(screen, label = "navigation") { current ->
                when (current) {
                    FunScreen.Home -> AnimatedHome(profile, xp, hearts) { screen = FunScreen.Play }
                    FunScreen.Play -> AnimatedPractice(hearts, { xp += 10 }, { hearts = (hearts - 1).coerceAtLeast(0) })
                    FunScreen.Coach -> AnimatedVoiceCoach(profile) { xp += it }
                    FunScreen.Profile -> CuteProfile(profile, xp, hearts) { store.reset(); profile = UserProfile() }
                }
            }
        }
    }
}

@Composable
private fun BouncyAvatar(emoji: String, accent: Color, modifier: Modifier = Modifier, talking: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "avatar")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (talking) 1.08f else 1.035f,
        animationSpec = infiniteRepeatable(tween(if (talking) 420 else 1100), RepeatMode.Reverse),
        label = "bounce"
    )
    Box(modifier.size(72.dp).scale(scale).background(accent.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = 43.sp)
    }
}

@Composable
private fun SpeechBubble(text: String, translation: String? = null, accent: Color = MaterialTheme.colorScheme.primary) {
    Surface(shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 8.dp), color = accent.copy(alpha = .12f), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 19.sp)
            translation?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun AnimatedHome(profile: UserProfile, xp: Int, hearts: Int, onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFF00B894))), RoundedCornerShape(30.dp)).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    BouncyAvatar("🦊", Color.White, talking = true)
                    Column(Modifier.weight(1f)) {
                        Text("¡Hola, ${profile.name}!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Lola t'accompagne aujourd'hui", color = Color.White.copy(.9f))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text("🔥 4", color = Color.White); Text("💎 $xp", color = Color.White); Text("❤️ $hearts", color = Color.White) }
                    }
                }
            }
        }
        item { SpeechBubble("Poco a poco se llega lejos.", "Petit à petit, on va loin.", Color(0xFFFF8A65)) }
        item { Text("Phrases à apprendre", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        items(miniLessons) { lesson ->
            Card(onClick = onStart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BouncyAvatar(lesson.mascot, lesson.accent, Modifier.size(62.dp))
                    Column(Modifier.weight(1f)) {
                        Text(lesson.name, color = lesson.accent, fontWeight = FontWeight.Bold)
                        Text(lesson.phrase, fontWeight = FontWeight.Bold)
                        Text(lesson.translation, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
        item {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp)) { Icon(Icons.Default.PlayArrow, null); Text(" Commencer une session") }
        }
    }
}

@Composable
private fun AnimatedPractice(hearts: Int, onCorrect: () -> Unit, onWrong: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val q = funQuestions[index]
    val correct = selected == q.correct
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator({ (index + 1) / funQuestions.size.toFloat() }, Modifier.weight(1f)); Spacer(Modifier.width(12.dp)); Text("❤️ $hearts")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BouncyAvatar(q.mascot, Color(0xFFFF8A65), talking = selected == null)
            SpeechBubble(q.prompt, accent = Color(0xFFFF8A65))
        }
        q.answers.forEachIndexed { i, answer ->
            OutlinedButton(onClick = {
                if (selected == null) {
                    selected = i
                    if (i == q.correct) onCorrect() else onWrong()
                }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), colors = if (selected == i) ButtonDefaults.outlinedButtonColors(containerColor = if (i == q.correct) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer) else ButtonDefaults.outlinedButtonColors()) { Text(answer) }
        }
        AnimatedVisibility(selected != null, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
            Surface(color = if (correct) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (correct) "🎉 ¡Excelente! +10 XP" else "💪 Presque !", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text(if (correct) "Lola est fière de toi." else "Bonne réponse : ${q.answers[q.correct]}")
                    Button(onClick = { selected = null; index = (index + 1) % funQuestions.size }, modifier = Modifier.fillMaxWidth()) { Text("Continuer") }
                }
            }
        }
    }
}

@Composable
private fun AnimatedVoiceCoach(profile: UserProfile, onXp: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { SecureAiClient() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf(ConversationMessage("assistant", "¡Hola! Soy Lola. ¿Qué has hecho hoy?"))) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("es", "ES") }
        tts = engine
        onDispose { engine.shutdown() }
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let { input = it }
    }
    fun launchSpeech() {
        listening = true
        speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con Lola en español")
        })
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) launchSpeech() }
    fun send() {
        val text = input.trim(); if (text.isBlank() || loading) return
        input = ""; messages = messages + ConversationMessage("user", text); loading = true
        scope.launch {
            val answer = client.reply(messages, profile.level, profile.goal)
            val full = buildString {
                append(answer.reply)
                answer.correction?.let { append("\n\n✍️ $it") }
                answer.explanation?.let { append("\n💡 $it") }
                answer.suggestedReply?.let { append("\n➡️ $it") }
            }
            messages = messages + ConversationMessage("assistant", full)
            loading = false; onXp(answer.xp)
            tts?.speak(answer.reply, TextToSpeech.QUEUE_FLUSH, null, "lola")
        }
    }

    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BouncyAvatar("🦊", Color(0xFFFF8A65), talking = loading || listening)
            Column { Text("Lola, coach IA", fontSize = 25.sp, fontWeight = FontWeight.Bold); Text("Parle ou écris • niveau ${profile.level}") }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(messages) { message ->
                val user = message.role == "user"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Bottom) {
                    if (!user) { Text("🦊", fontSize = 28.sp); Spacer(Modifier.width(6.dp)) }
                    Surface(color = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, shape = if (user) RoundedCornerShape(22.dp, 22.dp, 8.dp, 22.dp) else RoundedCornerShape(22.dp, 22.dp, 22.dp, 8.dp), modifier = Modifier.fillMaxWidth(.82f)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(message.text, color = if (user) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!user) TextButton(onClick = { tts?.speak(message.text.substringBefore("✍️"), TextToSpeech.QUEUE_FLUSH, null, "repeat") }) { Icon(Icons.Default.VolumeUp, null); Text(" Écouter") }
                        }
                    }
                }
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(onClick = { permission.launch(Manifest.permission.RECORD_AUDIO) }) { Icon(if (listening) Icons.Default.GraphicEq else Icons.Default.Mic, null) }
            OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Écris ou parle…") }, maxLines = 3)
            FilledIconButton(onClick = { send() }, enabled = input.isNotBlank() && !loading) { Icon(Icons.Default.Send, null) }
        }
    }
}

@Composable
private fun CuteProfile(profile: UserProfile, xp: Int, hearts: Int, onReset: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BouncyAvatar("🦊", Color(0xFFFF8A65)); Column { Text(profile.name, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Niveau ${profile.level} • ${profile.goal}") }
            }
        }
        item { SpeechBubble("Tu progresses très bien ! Continue quelques minutes chaque jour.", accent = Color(0xFFFF8A65)) }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("💎 $xp XP"); Text("❤️ $hearts cœurs"); Text("🔥 Série de 4 jours"); Text("⏱️ Objectif : ${profile.dailyMinutes} minutes") } } }
        item { Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Refaire le test de niveau") } }
    }
}
