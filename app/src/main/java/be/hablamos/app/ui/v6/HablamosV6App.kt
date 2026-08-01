package be.hablamos.app.ui.v6

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import be.hablamos.app.personalization.*
import be.hablamos.app.ui.HablamosApp
import be.hablamos.app.ui.v4.LolaMascot
import be.hablamos.app.ui.v4.MascotMood
import be.hablamos.app.ui.v4.SpeechBubble
import java.time.LocalDate

@Composable
fun HablamosV6App() {
    val context = LocalContext.current
    val profile = remember { ProfileStore(context).load() }
    if (!profile.onboardingComplete) {
        HablamosApp()
        return
    }

    val snapshot = remember(profile) {
        LearnerSnapshot(
            level = profile.level,
            goal = profile.goal,
            dailyMinutes = profile.dailyMinutes,
            skills = listOf(
                SkillState("Vocabulaire", 64, .68f, 8, 0),
                SkillState("Compréhension", 58, .61f, 11, 0),
                SkillState("Expression orale", 39, .42f, 17, 0),
                SkillState("Grammaire", 52, .55f, 13, 0),
                SkillState("Prononciation", 44, .47f, 15, 0)
            ),
            reviews = listOf(
                ReviewItem("ayer", "Que signifie ayer ?", "hier", .28f, 1, LocalDate.now().toEpochDay(), setOf("temps", "vocabulaire")),
                ReviewItem("quisiera", "Complète : ___ una mesa para dos", "Quisiera", .36f, 2, LocalDate.now().toEpochDay(), setOf("restaurant", "politesse")),
                ReviewItem("fui", "Traduis : je suis allé", "fui", .41f, 1, LocalDate.now().minusDays(1).toEpochDay(), setOf("passé", "verbes"))
            ),
            recurringErrors = mapOf("ser / estar" to 6, "por / para" to 4, "genre des noms" to 3)
        )
    }
    val session = remember(snapshot) { LearnerEngine.buildSession(snapshot, LocalDate.now().toEpochDay()) }

    MaterialTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth().background(
                        Brush.linearGradient(listOf(Color(0xFF5037C8), Color(0xFF7B61E8), Color(0xFF24B99A))),
                        RoundedCornerShape(30.dp)
                    ).padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Ton professeur personnel", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LolaMascot(Modifier.size(104.dp), MascotMood.Happy)
                            SpeechBubble("LOLA", session.tutorMessage, Modifier.weight(1f), Color.White)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("Niveau ${snapshot.level}") })
                            AssistChip(onClick = {}, label = { Text("${snapshot.dailyMinutes} min/jour") })
                        }
                    }
                }
            }

            item {
                Text(session.title, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("${session.estimatedMinutes} minutes • construit selon tes difficultés actuelles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(session.blocks.size) { index ->
                val block = session.blocks[index]
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(iconFor(block.type), fontSize = 34.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(block.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(block.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${block.minutes} min", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) {
                    Text("Commencer ma séance", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text("Ton profil linguistique", fontSize = 23.sp, fontWeight = FontWeight.Black)
            }

            items(snapshot.skills.size) { index ->
                val skill = snapshot.skills[index]
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(skill.name, fontWeight = FontWeight.SemiBold)
                        Text("${skill.score}%")
                    }
                    LinearProgressIndicator(progress = { skill.score / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Text("Confiance ${(skill.confidence * 100).toInt()}% • ${skill.mistakes} erreurs observées", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Erreur récurrente détectée", fontWeight = FontWeight.Black, fontSize = 19.sp)
                        Text("ser / estar revient souvent. Paco préparera une micro-leçon ciblée avant ta prochaine conversation.")
                    }
                }
            }
        }
    }
}

private fun iconFor(type: String) = when (type) {
    "review" -> "🧠"
    "skill" -> "🎯"
    "correction" -> "🛠️"
    else -> "💬"
}
