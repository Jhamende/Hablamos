package be.hablamos.app.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import be.hablamos.app.ai.ConversationMessage
import be.hablamos.app.ai.SecureAiClient
import be.hablamos.app.data.ProfileStore
import be.hablamos.app.data.UserProfile
import kotlinx.coroutines.launch
import java.util.Locale

private enum class V3Screen { Learn, Practice, Coach, Profile }
private data class Challenge(val prompt: String, val choices: List<String>, val answer: Int, val explanation: String)
private val challenges = listOf(
    Challenge("Comment dit-on « Je voudrais une table pour quatre » ?", listOf("Tengo una mesa para cuatro", "Quisiera una mesa para cuatro", "Soy una mesa para cuatro"), 1, "Quisiera est une formulation polie pour demander."),
    Challenge("Complète : Me ___ la paella.", listOf("gusta", "gusto", "gustas"), 0, "Avec gustar, la chose appréciée est le sujet."),
    Challenge("Que signifie « Ayer fui al mercado » ?", listOf("Hier je suis allé au marché", "Demain j'irai au marché", "Je vais au marché"), 0, "Ayer = hier et fui = je suis allé."),
    Challenge("Choisis la phrase naturelle.", listOf("Estoy 34 años", "Tengo 34 años", "Soy 34 años"), 1, "En espagnol, on utilise tener pour l'âge.")
)

@Composable
fun HablamosV3App() {
    val context = LocalContext.current
    val store = remember { ProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }
    if (!profile.onboardingComplete) {
        HablamosApp()
        return
    }
    var screen by remember { mutableStateOf(V3Screen.Learn) }
    var xp by remember { mutableIntStateOf(120) }
    var hearts by remember { mutableIntStateOf(5) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(V3Screen.Learn, Icons.Default.Home, "Apprendre"),
                    Triple(V3Screen.Practice, Icons.Default.Extension, "Exercices"),
                    Triple(V3Screen.Coach, Icons.Default.AutoAwesome, "Coach IA"),
                    Triple(V3Screen.Profile, Icons.Default.Person, "Profil")
                ).forEach { item -> NavigationBarItem(screen == item.first, { screen = item.first }, { Icon(item.second, null) }, label = { Text(item.third) }) }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                V3Screen.Learn -> LearnDashboard(profile, xp, hearts) { screen = V3Screen.Practice }
                V3Screen.Practice -> PracticeScreen(hearts, onCorrect = { xp += 10 }, onWrong = { hearts = (hearts - 1).coerceAtLeast(0) })
                V3Screen.Coach -> VoiceCoach(profile) { xp += it }
                V3Screen.Profile -> V3Profile(profile, xp, hearts) {
                    store.reset(); profile = UserProfile()
                }
            }
        }
    }
}

@Composable
private fun LearnDashboard(profile: UserProfile, xp: Int, hearts: Int, onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFF00B894))), RoundedCornerShape(28.dp)).padding(22.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("¡Hola, ${profile.name}!", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Niveau ${profile.level} • ${profile.goal}", color = Color.White.copy(.9f))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) { Text("🔥 4 jours", color = Color.White); Text("💎 $xp XP", color = Color.White); Text("❤️ $hearts", color = Color.White) }
                    LinearProgressIndicator({ .42f }, Modifier.fillMaxWidth(), color = Color.White)
                }
            }
        }
        item { Text("Ton parcours", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        items(listOf("Premiers échanges" to "👋", "Commander au restaurant" to "🍽️", "Se déplacer" to "🧭", "Conversation libre" to "🎙️")) { unit ->
            Card(onClick = onStart, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(unit.second, fontSize = 30.sp) }
                    Column(Modifier.weight(1f)) { Text(unit.first, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("Vocabulaire • écoute • expression") }
                    Icon(Icons.Default.PlayArrow, null)
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Défi quotidien", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Parle 2 minutes avec le Coach IA sans utiliser le français.")
                }
            }
        }
    }
}

@Composable
private fun PracticeScreen(hearts: Int, onCorrect: () -> Unit, onWrong: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Int?>(null) }
    val q = challenges[index]
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator({ (index + 1) / challenges.size.toFloat() }, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp)); Text("❤️ $hearts", fontWeight = FontWeight.Bold)
        }
        Text("Choisis la bonne réponse", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Text(q.prompt, Modifier.padding(22.dp), fontSize = 21.sp, fontWeight = FontWeight.Medium) }
        q.choices.forEachIndexed { choiceIndex, choice ->
            val isSelected = selected == choiceIndex
            OutlinedButton(onClick = {
                if (selected == null) {
                    selected = choiceIndex
                    if (choiceIndex == q.answer) onCorrect() else onWrong()
                }
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.outlinedButtonColors()) { Text(choice) }
        }
        selected?.let {
            val correct = it == q.answer
            Surface(color = if (correct) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (correct) "¡Excelente! +10 XP" else "Pas encore", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(q.explanation)
                    Button(onClick = { selected = null; index = (index + 1) % challenges.size }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Continuer") }
                }
            }
        }
    }
}

@Composable
private fun VoiceCoach(profile: UserProfile, onXp: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { SecureAiClient() }
    var input by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf(ConversationMessage("assistant", "¡Hola! Vamos a practicar. Cuéntame cómo ha sido tu día."))) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale("es", "ES") }
        tts = engine
        onDispose { engine.shutdown() }
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = spoken
    }
    fun startSpeech() {
        listening = true
        speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla en español")
        })
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startSpeech() }
    fun send() {
        val text = input.trim(); if (text.isBlank() || loading) return
        input = ""; messages = messages + ConversationMessage("user", text); loading = true
        scope.launch {
            val answer = client.reply(messages, profile.level, profile.goal)
            val formatted = buildString {
                append(answer.reply)
                answer.correction?.let { append("\n\n✍️ Correction : $it") }
                answer.explanation?.let { append("\n💡 $it") }
                answer.suggestedReply?.let { append("\n\n➡️ Tu peux répondre : $it") }
            }
            messages = messages + ConversationMessage("assistant", formatted)
            onXp(answer.xp); loading = false
            tts?.speak(answer.reply, TextToSpeech.QUEUE_FLUSH, null, "coach")
        }
    }
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null) }
            Spacer(Modifier.width(10.dp)); Column { Text("Coach IA vocal", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Espagnol ${profile.level} • corrections instantanées") }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { message ->
                val user = message.role == "user"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (user) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(.86f)) {
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
            FilledIconButton(onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }) { Icon(if (listening) Icons.Default.GraphicEq else Icons.Default.Mic, null) }
            OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("Parle ou écris en espagnol…") }, maxLines = 3)
            FilledIconButton(onClick = { send() }, enabled = input.isNotBlank() && !loading) { Icon(Icons.Default.Send, null) }
        }
        Text("Les réponses IA réelles nécessitent le backend sécurisé fourni dans le dépôt.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun V3Profile(profile: UserProfile, xp: Int, hearts: Int, onReset: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Profil", fontSize = 29.sp, fontWeight = FontWeight.Bold) }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(profile.name, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Niveau ${profile.level} • ${profile.goal}"); Text("💎 $xp XP   ❤️ $hearts   🔥 4 jours") } } }
        item { Button(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Refaire le test de niveau") } }
    }
}
