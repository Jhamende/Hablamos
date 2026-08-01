package be.hablamos.app.ui.v4

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class ShellScreen { HOME, PATH, PROFILE }

@Composable
fun HablamosV4Shell() {
    var screen by remember { mutableStateOf(ShellScreen.HOME) }
    var selectedUnit by remember { mutableStateOf<CurriculumUnit?>(null) }
    var xp by remember { mutableIntStateOf(420) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(screen == ShellScreen.HOME, { screen = ShellScreen.HOME; selectedUnit = null }, { Icon(Icons.Default.Home, null) }, label = { Text("Accueil") })
                NavigationBarItem(screen == ShellScreen.PATH, { screen = ShellScreen.PATH; selectedUnit = null }, { Icon(Icons.Default.Map, null) }, label = { Text("Parcours") })
                NavigationBarItem(screen == ShellScreen.PROFILE, { screen = ShellScreen.PROFILE; selectedUnit = null }, { Icon(Icons.Default.Person, null) }, label = { Text("Profil") })
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedUnit ?: screen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize().padding(padding),
            label = "v4-shell"
        ) { target ->
            when (target) {
                is CurriculumUnit -> UnitDetailScreen(target, onBack = { selectedUnit = null }, onStart = { xp += 20 })
                ShellScreen.HOME -> V4Landing(xp) { screen = ShellScreen.PATH }
                ShellScreen.PATH -> CurriculumPathScreen(onUnitSelected = { selectedUnit = it })
                ShellScreen.PROFILE -> V4ProgressProfile(xp)
            }
        }
    }
}

@Composable
private fun V4Landing(xp: Int, onOpenPath: () -> Unit) {
    val totals = HablamosCurriculum.totals()
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF6750E8), Color(0xFF8D7AF3), Color(0xFF31C6A4))),
                    RoundedCornerShape(32.dp)
                ).padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Hablamos", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LolaMascot(Modifier.size(108.dp), MascotMood.Happy)
                        SpeechBubble("LOLA", "Ton parcours complet A0–C2 est prêt. On avance étape par étape.", Modifier.weight(1f), Color.White)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("⭐", "$xp XP", Modifier.weight(1f))
                        StatPill("📚", "${totals.third} leçons", Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Text("Maîtrise complète", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Card(shape = RoundedCornerShape(26.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${totals.first} mots • ${totals.second} phrases", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Un programme structuré du premier mot jusqu'aux nuances professionnelles et régionales.")
                    LinearProgressIndicator(progress = { .08f }, modifier = Modifier.fillMaxWidth().height(9.dp))
                    Button(onClick = onOpenPath, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                        Icon(Icons.Default.PlayArrow, null); Text(" Ouvrir mon parcours")
                    }
                }
            }
        }
    }
}

@Composable
private fun CurriculumPathScreen(onUnitSelected: (CurriculumUnit) -> Unit) {
    var selectedLevel by remember { mutableStateOf("A1") }
    val levels = listOf("A0", "A1", "A2", "B1", "B2", "C1", "C2")
    val units = HablamosCurriculum.units.filter { it.level == selectedLevel }

    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Parcours complet", fontSize = 29.sp, fontWeight = FontWeight.Black)
            Text("Choisis un niveau puis explore chaque unité.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                levels.forEach { level ->
                    FilterChip(selected = selectedLevel == level, onClick = { selectedLevel = level }, label = { Text(level) })
                }
            }
        }
        items(units) { unit ->
            Card(onClick = { onUnitSelected(unit) }, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(unit.icon, fontSize = 38.sp)
                    Column(Modifier.weight(1f)) {
                        Text(unit.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${unit.lessons} leçons • ${unit.targetWords} mots • ${unit.targetPhrases} phrases")
                        Text("Coach : ${unit.companion.name.lowercase().replaceFirstChar { it.uppercase() }}", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitDetailScreen(unit: CurriculumUnit, onBack: () -> Unit, onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onClick = onBack) { Text("← Retour au parcours") } }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${unit.icon} ${unit.level}", fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text(unit.title, fontSize = 27.sp, fontWeight = FontWeight.Black)
                    Text("${unit.lessons} leçons • ${unit.targetWords} mots • ${unit.targetPhrases} phrases")
                }
            }
        }
        item {
            Text("À la fin de cette unité", fontSize = 22.sp, fontWeight = FontWeight.Black)
            unit.outcomes.forEach { outcome ->
                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("✓ $outcome", Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Text("Structure des séances", fontSize = 22.sp, fontWeight = FontWeight.Black)
            listOf("Découverte visuelle", "Écoute et répétition", "Vocabulaire actif", "Conversation guidée", "Révision espacée").forEachIndexed { index, step ->
                Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("${index + 1}", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(12.dp)); Text(step, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) {
                Text("Commencer l'unité")
            }
        }
    }
}

@Composable
private fun V4ProgressProfile(xp: Int) {
    val totals = HablamosCurriculum.totals()
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Progression", fontSize = 29.sp, fontWeight = FontWeight.Black) }
        item {
            Card(shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LolaMascot(Modifier.size(105.dp), MascotMood.Celebrating)
                    Text("$xp XP", fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Objectif final : ${totals.first} mots et ${totals.second} phrases")
                }
            }
        }
    }
}
