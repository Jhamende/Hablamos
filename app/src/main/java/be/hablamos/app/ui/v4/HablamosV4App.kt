package be.hablamos.app.ui.v4

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import be.hablamos.app.data.ProfileStore
import be.hablamos.app.ui.HablamosApp

private enum class V4Screen { Home, Path, Practice, Profile }

@Composable
fun HablamosV4App() {
    val context = LocalContext.current
    val store = remember { ProfileStore(context) }
    val profile = remember { store.load() }
    if (!profile.onboardingComplete) {
        HablamosApp()
        return
    }

    var screen by remember { mutableStateOf(V4Screen.Home) }
    var xp by remember { mutableIntStateOf(380) }
    var streak by remember { mutableIntStateOf(6) }
    var gems by remember { mutableIntStateOf(75) }

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme(
            primary = Color(0xFFB9A8FF),
            secondary = Color(0xFF66E0C5),
            tertiary = Color(0xFFFFB3B3)
        ) else lightColorScheme(
            primary = HablamosPurple,
            secondary = HablamosMint,
            tertiary = HablamosCoral,
            background = Color(0xFFF7F5FC),
            surface = Color.White
        )
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    listOf(
                        Triple(V4Screen.Home, Icons.Default.Home, "Accueil"),
                        Triple(V4Screen.Path, Icons.AutoMirrored.Filled.MenuBook, "Parcours"),
                        Triple(V4Screen.Practice, Icons.Default.Bolt, "Défis"),
                        Triple(V4Screen.Profile, Icons.Default.Person, "Profil")
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
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInHorizontally { it / 6 }) togetherWith fadeOut(tween(180))
                },
                modifier = Modifier.fillMaxSize().padding(padding),
                label = "v4-navigation"
            ) { current ->
                when (current) {
                    V4Screen.Home -> V4Home(
                        name = profile.name,
                        level = profile.level,
                        xp = xp,
                        streak = streak,
                        gems = gems,
                        onContinue = { screen = V4Screen.Path }
                    )
                    V4Screen.Path -> LearningPath(onPractice = { screen = V4Screen.Practice })
                    V4Screen.Practice -> DailyChallenge(
                        onSuccess = { xp += 25; gems += 5; streak = maxOf(streak, 7) },
                        onFinish = { screen = V4Screen.Home }
                    )
                    V4Screen.Profile -> ProfileOverview(profile.name, profile.level, xp, streak, gems)
                }
            }
        }
    }
}

@Composable
private fun V4Home(name: String, level: String, xp: Int, streak: Int, gems: Int, onContinue: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF6047D9), Color(0xFF8B6CF6), Color(0xFF31C6A4))),
                    RoundedCornerShape(32.dp)
                ).padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hablamos", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(.18f)) {
                            Text("Niveau $level", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LolaMascot(Modifier.size(112.dp), mood = MascotMood.Happy)
                        SpeechBubble(
                            title = "LOLA",
                            text = "¡Hola $name! Aujourd’hui, on apprend à parler naturellement.",
                            modifier = Modifier.weight(1f),
                            container = Color.White
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("🔥", "$streak jours", Modifier.weight(1f))
                        StatPill("⭐", "$xp XP", Modifier.weight(1f))
                        StatPill("💎", "$gems", Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Text("Objectif du jour", fontSize = 23.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(54.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("🎯", fontSize = 28.sp) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Session personnalisée", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("12 min • vocabulaire • écoute • conversation")
                        }
                    }
                    LinearProgressIndicator(progress = { .35f }, modifier = Modifier.fillMaxWidth().height(9.dp), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                    Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                        Icon(Icons.Default.PlayArrow, null); Text(" Continuer mon parcours", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Cette semaine", fontSize = 23.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEachIndexed { index, day ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = RoundedCornerShape(15.dp), color = if (index < 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) {
                            Box(Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
                                Text(if (index < 5) "✓" else day, color = if (index < 5) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(5.dp)); Text(day, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🎙️", fontSize = 38.sp); Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Défi oral", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text("Présente ta journée pendant 60 secondes sans utiliser le français.")
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun LearningPath(onPractice: () -> Unit) {
    val skills = listOf(
        Triple("Salutations", "👋", 1f),
        Triple("Se présenter", "🙂", .75f),
        Triple("Famille", "👨‍👩‍👧", .25f),
        Triple("Restaurant", "🍽️", 0f),
        Triple("Transport", "🚆", 0f),
        Triple("Vie quotidienne", "🏡", 0f)
    )
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Text("Parcours A1", fontSize = 29.sp, fontWeight = FontWeight.Black)
            Text("Un chemin structuré vers la conversation réelle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    LolaMascot(Modifier.size(82.dp), mood = MascotMood.Thinking)
                    Spacer(Modifier.width(12.dp))
                    SpeechBubble("LOLA", "Termine « Se présenter » pour débloquer la famille.", Modifier.weight(1f), MaterialTheme.colorScheme.primaryContainer)
                }
            }
        }
        items(skills.size) { index ->
            val skill = skills[index]
            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (index % 2 == 0) Arrangement.Start else Arrangement.End) {
                SkillNode(skill.first, skill.second, skill.third, locked = index > 2, onClick = onPractice, modifier = Modifier.width(120.dp))
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DailyChallenge(onSuccess: () -> Unit, onFinish: () -> Unit) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val options = listOf("Me llamo Jonathan", "Soy llamo Jonathan", "Tengo llamo Jonathan")
    val correct = selected == 0
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LinearProgressIndicator(progress = { .45f }, modifier = Modifier.fillMaxWidth().height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LolaMascot(Modifier.size(92.dp), mood = if (selected == null) MascotMood.Thinking else if (correct) MascotMood.Celebrating else MascotMood.Thinking)
            SpeechBubble("LOLA", "Comment dis-tu : « Je m’appelle Jonathan » ?", Modifier.weight(1f), MaterialTheme.colorScheme.surface)
        }
        options.forEachIndexed { index, text ->
            OutlinedButton(
                onClick = { if (selected == null) selected = index },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = when {
                    selected == index && index == 0 -> MaterialTheme.colorScheme.secondaryContainer
                    selected == index -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                })
            ) { Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
        }
        AnimatedVisibility(selected != null, enter = fadeIn() + expandVertically()) {
            Card(colors = CardDefaults.cardColors(containerColor = if (correct) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (correct) "¡Perfecto! +25 XP" else "Presque !", fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text(if (correct) "Me llamo… est la tournure naturelle pour dire son nom." else "La bonne réponse est : Me llamo Jonathan.")
                    Button(onClick = { if (correct) onSuccess(); onFinish() }, modifier = Modifier.fillMaxWidth()) { Text("Continuer") }
                }
            }
        }
    }
}

@Composable
private fun ProfileOverview(name: String, level: String, xp: Int, streak: Int, gems: Int) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Ton profil", fontSize = 29.sp, fontWeight = FontWeight.Black) }
        item {
            Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LolaMascot(Modifier.size(110.dp), mood = MascotMood.Happy)
                    Text(name, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Espagnol $level", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatPill("🔥", "$streak jours"); StatPill("⭐", "$xp XP"); StatPill("💎", "$gems")
                    }
                }
            }
        }
        item {
            Text("Compétences", fontSize = 22.sp, fontWeight = FontWeight.Black)
            listOf("Vocabulaire" to .62f, "Compréhension" to .48f, "Expression orale" to .31f, "Grammaire" to .54f).forEach { stat ->
                Column(Modifier.padding(vertical = 7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stat.first, fontWeight = FontWeight.SemiBold); Text("${(stat.second * 100).toInt()}%") }
                    LinearProgressIndicator(progress = { stat.second }, modifier = Modifier.fillMaxWidth().height(8.dp))
                }
            }
        }
    }
}
