package be.hablamos.app.v6

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import be.hablamos.app.ui.v4.LolaMascot
import be.hablamos.app.ui.v4.MascotMood
import be.hablamos.app.ui.v4.SpeechBubble

private enum class TutorScreen { TODAY, SESSION, PROGRESS, MEMORY }

@Composable
fun HablamosV6App() {
    val context = LocalContext.current
    val store = remember { PersonalProfileStore(context) }
    var profile by remember { mutableStateOf(store.load("Jonathan", "A1", "Converser", 15)) }
    var screen by remember { mutableStateOf(TutorScreen.TODAY) }
    val session = remember(profile) { AdaptiveSessionEngine.build(profile) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf(
                        Triple(TutorScreen.TODAY, Icons.Default.Home, "Aujourd’hui"),
                        Triple(TutorScreen.SESSION, Icons.Default.PlayCircle, "Séance"),
                        Triple(TutorScreen.PROGRESS, Icons.Default.Insights, "Progression"),
                        Triple(TutorScreen.MEMORY, Icons.Default.Psychology, "Mémoire")
                    ).forEach { item ->
                        NavigationBarItem(
                            selected = screen == item.first,
                            onClick = { screen = item.first },
                            icon = { Icon(item.second, null) },
                            label = { Text(item.third) }
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.fillMaxSize().padding(padding),
                label = "v6-navigation"
            ) { current ->
                when (current) {
                    TutorScreen.TODAY -> TutorHome(profile, session) { screen = TutorScreen.SESSION }
                    TutorScreen.SESSION -> SessionScreen(session) { earned ->
                        profile = profile.copy(
                            xp = profile.xp + earned,
                            streak = profile.streak + 1,
                            dueReviews = (profile.dueReviews - 3).coerceAtLeast(0),
                            knownWords = profile.knownWords + 5,
                            activePhrases = profile.activePhrases + 2,
                            skills = profile.skills.copy(speaking = (profile.skills.speaking + 2).coerceAtMost(100))
                        )
                        store.save(profile)
                        screen = TutorScreen.PROGRESS
                    }
                    TutorScreen.PROGRESS -> ProgressScreen(profile)
                    TutorScreen.MEMORY -> MemoryScreen(profile)
                }
            }
        }
    }
}

@Composable
private fun TutorHome(profile: PersonalLearningProfile, session: AdaptiveSession, onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF5E4AE3), Color(0xFF8B6CF6), Color(0xFF22BFA0))),
                    RoundedCornerShape(30.dp)
                ).padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Ton professeur personnel", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LolaMascot(Modifier.size(108.dp), MascotMood.Happy)
                        SpeechBubble("LOLA", session.coachMessage, Modifier.weight(1f), Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("🔥 ${profile.streak} jours") })
                        AssistChip(onClick = {}, label = { Text("⭐ ${profile.xp} XP") })
                        AssistChip(onClick = {}, label = { Text("${profile.cefrLevel}") })
                    }
                }
            }
        }

        item {
            Text("Séance conçue pour toi", fontSize = 23.sp, fontWeight = FontWeight.Black)
            Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${session.totalMinutes} minutes • priorité ${skillLabel(session.focus)}", fontWeight = FontWeight.Bold)
                    session.activities.forEach { activity ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activityEmoji(activity.type), fontSize = 28.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(activity.title, fontWeight = FontWeight.SemiBold)
                                Text(activity.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${activity.minutes} min")
                        }
                    }
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        Icon(Icons.Default.PlayArrow, null)
                        Text(" Commencer ma séance", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Ce que Lola connaît de toi", fontSize = 22.sp, fontWeight = FontWeight.Black)
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${profile.knownWords} mots connus • ${profile.activePhrases} phrases actives")
                    Text("Erreurs à retravailler : ${profile.recurringErrors.joinToString()}")
                    Text("${profile.dueReviews} éléments à revoir aujourd’hui")
                }
            }
        }
    }
}

@Composable
private fun SessionScreen(session: AdaptiveSession, onComplete: (Int) -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val activity = session.activities[index]
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LinearProgressIndicator(progress = { (index + 1f) / session.activities.size }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            LolaMascot(Modifier.size(96.dp), MascotMood.Thinking)
            SpeechBubble("LOLA", activity.description, Modifier.weight(1f))
        }
        Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(activityEmoji(activity.type), fontSize = 58.sp)
                Text(activity.title, fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text("Activité adaptative de ${activity.minutes} minutes", color = MaterialTheme.colorScheme.onSurfaceVariant)
                when (activity.type) {
                    ActivityType.REVIEW -> Text("Revois : ayer • quisiera • todavía • me gustaría")
                    ActivityType.SPEAKING -> Text("Réponds à voix haute : ¿Qué has hecho hoy?")
                    ActivityType.LISTENING -> Text("Écoute le dialogue puis résume-le avec tes mots.")
                    ActivityType.GRAMMAR -> Text("Choisis entre ser et estar dans des phrases naturelles.")
                    ActivityType.VOCABULARY -> Text("Associe les mots nouveaux à des situations réelles.")
                    ActivityType.READING -> Text("Lis puis explique l’idée principale.")
                    ActivityType.AI_CONVERSATION -> Text("Lola joue un interlocuteur espagnol et adapte la difficulté à tes réponses.")
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                if (index < session.activities.lastIndex) index++
                else onComplete(session.activities.sumOf { it.xp })
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text(if (index < session.activities.lastIndex) "Activité suivante" else "Terminer la séance") }
    }
}

@Composable
private fun ProgressScreen(profile: PersonalLearningProfile) {
    val stats = listOf(
        "Vocabulaire" to profile.skills.vocabulary,
        "Compréhension orale" to profile.skills.listening,
        "Expression orale" to profile.skills.speaking,
        "Grammaire" to profile.skills.grammar,
        "Lecture" to profile.skills.reading,
        "Prononciation" to profile.skills.pronunciation
    )
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Ton profil linguistique", fontSize = 28.sp, fontWeight = FontWeight.Black) }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Niveau global ${profile.cefrLevel}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    stats.forEach { (label, score) ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label); Text("$score%", fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Priorité recommandée", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(skillLabel(profile.skills.weakest()))
                }
            }
        }
    }
}

@Composable
private fun MemoryScreen(profile: PersonalLearningProfile) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Mémoire pédagogique", fontSize = 28.sp, fontWeight = FontWeight.Black) }
        item { Text("Hablamos conserve ce qui doit influencer tes prochaines séances.") }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("À réviser maintenant", fontWeight = FontWeight.Bold)
                    Text("${profile.dueReviews} mots ou phrases")
                    Text("Mots maîtrisés : ${profile.knownWords}")
                    Text("Phrases utilisables spontanément : ${profile.activePhrases}")
                }
            }
        }
        item {
            Text("Erreurs récurrentes", fontSize = 21.sp, fontWeight = FontWeight.Black)
        }
        items(profile.recurringErrors) { error ->
            ListItem(headlineContent = { Text(error) }, leadingContent = { Icon(Icons.Default.Replay, null) })
        }
    }
}

private fun skillLabel(skill: LearningSkill) = when (skill) {
    LearningSkill.VOCABULARY -> "vocabulaire"
    LearningSkill.LISTENING -> "compréhension orale"
    LearningSkill.SPEAKING -> "expression orale"
    LearningSkill.GRAMMAR -> "grammaire"
    LearningSkill.READING -> "lecture"
    LearningSkill.PRONUNCIATION -> "prononciation"
}

private fun activityEmoji(type: ActivityType) = when (type) {
    ActivityType.REVIEW -> "🧠"
    ActivityType.VOCABULARY -> "🧩"
    ActivityType.LISTENING -> "🎧"
    ActivityType.SPEAKING -> "🎙️"
    ActivityType.GRAMMAR -> "📝"
    ActivityType.READING -> "📖"
    ActivityType.AI_CONVERSATION -> "✨"
}