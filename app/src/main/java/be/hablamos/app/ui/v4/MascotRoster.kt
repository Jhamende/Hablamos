package be.hablamos.app.ui.v4

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MascotInfo(val icon: String, val name: String, val role: String)

@Composable
fun MascotRoster(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "mascot-roster")
    val bounce by transition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "mascot-bounce"
    )
    val mascots = listOf(
        MascotInfo("🐼", "Paco", "Grammaire"),
        MascotInfo("🐱", "Mia", "Conversation"),
        MascotInfo("🦜", "Pepe", "Expressions"),
        MascotInfo("🦉", "Alba", "Culture")
    )
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(18.dp)) {
            Text("Tes compagnons", fontSize = 21.sp, fontWeight = FontWeight.Black)
            Text("Chacun t'accompagne dans une compétence.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                mascots.forEachIndexed { index, mascot ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(62.dp).graphicsLayer { translationY = if (index % 2 == 0) bounce else -bounce },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) { Text(mascot.icon, Modifier.padding(12.dp), fontSize = 31.sp) }
                        Spacer(Modifier.height(5.dp))
                        Text(mascot.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(mascot.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
