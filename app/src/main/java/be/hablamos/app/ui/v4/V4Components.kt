package be.hablamos.app.ui.v4

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val HablamosPurple = Color(0xFF6750E8)
internal val HablamosCoral = Color(0xFFFF6B6B)
internal val HablamosMint = Color(0xFF31C6A4)
internal val HablamosGold = Color(0xFFFFC857)

@Composable
fun LolaMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.Happy,
    animated: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "lola")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) -10f else 0f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    Canvas(modifier.graphicsLayer { translationY = bob }) {
        val w = size.width
        val h = size.height
        val orange = Color(0xFFF28C45)
        val cream = Color(0xFFFFE5C7)
        val dark = Color(0xFF3B2C35)

        val leftEar = Path().apply {
            moveTo(w * .18f, h * .34f); lineTo(w * .28f, h * .02f); lineTo(w * .43f, h * .31f); close()
        }
        val rightEar = Path().apply {
            moveTo(w * .57f, h * .31f); lineTo(w * .72f, h * .02f); lineTo(w * .82f, h * .34f); close()
        }
        drawPath(leftEar, orange); drawPath(rightEar, orange)
        drawCircle(orange, radius = w * .34f, center = Offset(w * .5f, h * .48f))
        drawOval(cream, topLeft = Offset(w * .28f, h * .38f), size = Size(w * .44f, h * .42f))
        drawCircle(dark, radius = w * .025f, center = Offset(w * .39f, h * .44f))
        drawCircle(dark, radius = w * .025f, center = Offset(w * .61f, h * .44f))
        drawCircle(dark, radius = w * .035f, center = Offset(w * .5f, h * .56f))
        when (mood) {
            MascotMood.Happy -> drawArc(dark, 15f, 150f, false, Offset(w * .40f, h * .54f), Size(w * .20f, h * .16f), strokeWidth = w * .025f)
            MascotMood.Thinking -> drawLine(dark, Offset(w * .43f, h * .68f), Offset(w * .57f, h * .68f), strokeWidth = w * .025f)
            MascotMood.Celebrating -> {
                drawArc(dark, 0f, 180f, false, Offset(w * .38f, h * .52f), Size(w * .24f, h * .22f), strokeWidth = w * .028f)
                drawCircle(HablamosCoral, radius = w * .035f, center = Offset(w * .29f, h * .58f))
                drawCircle(HablamosCoral, radius = w * .035f, center = Offset(w * .71f, h * .58f))
            }
        }
    }
}

enum class MascotMood { Happy, Thinking, Celebrating }

@Composable
fun SpeechBubble(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surface
) {
    Surface(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = container
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = HablamosPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
    }
}

@Composable
fun StatPill(icon: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .96f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon); Spacer(Modifier.width(5.dp)); Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SkillNode(
    title: String,
    emoji: String,
    progress: Float,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(82.dp), strokeWidth = 7.dp, color = if (locked) Color.Gray else HablamosPurple, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            FilledIconButton(onClick = onClick, enabled = !locked, modifier = Modifier.size(62.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (locked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer)) {
                Text(if (locked) "🔒" else emoji, fontSize = 27.sp)
            }
        }
        Spacer(Modifier.height(7.dp)); Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
