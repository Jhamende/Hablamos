package be.hablamos.app.ui.v6

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import be.hablamos.app.ui.v4.SpeechBubble
import java.time.LocalDate

@Composable
fun HablamosV6App() {
    val context = LocalContext.current
    val profile = remember { ProfileStore(context).load() }

    val snapshot = remember(profile) {
        LearnerSnapshot(
            level = profile.level.ifBlank { "A1" },
            goal = profile.goal.ifBlank { "Parler espagnol avec confiance" },
            dailyMinutes = profile.dailyMinutes.coerceAtLeast(10),
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth().background(
                        Brush.linearGradient(listOf(Color(0xFF35288A), Color(0xFF6750D8), Color(0xFF1CA98C))),
                        RoundedCornerShape(32.dp)
                    ).padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Ton professeur personnel", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            GeneratedAvatar(AvatarAsset.MIA, Modifier.size(width = 112.dp, height = 142.dp))
                            SpeechBubble("MIA", session.tutorMessage, Modifier.weight(1f), Color.White)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("Niveau ${snapshot.level}") })
                            AssistChip(onClick = {}, label = { Text("${snapshot.dailyMinutes} min/jour") })
                        }
                    }
                }
            }

            if (!profile.onboardingComplete) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Personnalise ton professeur", fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text("Complète bientôt le test de niveau pour que Mia adapte précisément les séances. En attendant, tu peux déjà découvrir toute la nouvelle interface V6.")
                        }
                    }
                }
            }

            item {
                Text("Ton équipe pédagogique", fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text("Chaque compagnon intervient selon la compétence travaillée.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AvatarAsset.entries.size) { index ->
                        val avatar = AvatarAsset.entries[index]
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.width(154.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GeneratedAvatar(avatar, Modifier.fillMaxWidth().height(196.dp))
                                Text(avatar.displayName, fontSize = 19.sp, fontWeight = FontWeight.Black)
                                Text(avatar.role, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(session.title, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("${session.estimatedMinutes} minutes • construit selon tes difficultés actuelles")
                    }
                }
            }

            items(session.blocks.size) { index ->
                val block = session.blocks[index]
                Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
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
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("Commencer ma séance", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            item { Text("Ton profil linguistique", fontSize = 23.sp, fontWeight = FontWeight.Black) }

            items(snapshot.skills.size) { index ->
                val skill = snapshot.skills[index]
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(skill.name, fontWeight = FontWeight.SemiBold)
                            Text("${skill.score}%", fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = { skill.score / 100f }, modifier = Modifier.fillMaxWidth().height(9.dp))
                        Text("Confiance ${(skill.confidence * 100).toInt()}% • ${skill.mistakes} erreurs observées", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(24.dp)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GeneratedAvatar(AvatarAsset.PACO, Modifier.size(width = 84.dp, height = 108.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("Erreur récurrente détectée", fontWeight = FontWeight.Black, fontSize = 19.sp)
                            Text("ser / estar revient souvent. Paco préparera une micro-leçon ciblée avant ta prochaine conversation.")
                        }
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
