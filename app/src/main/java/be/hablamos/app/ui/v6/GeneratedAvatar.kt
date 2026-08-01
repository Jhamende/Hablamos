package be.hablamos.app.ui.v6

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class AvatarAsset(val fileName: String, val displayName: String, val role: String) {
    PACO("paco.webp.b64", "Paco", "Grammaire"),
    MIA("mia.webp.b64", "Mia", "Conversation"),
    PEPE("pepe.webp.b64", "Pepe", "Expressions"),
    ALBA("alba.webp.b64", "Alba", "Culture")
}

@Composable
fun GeneratedAvatar(
    avatar: AvatarAsset,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmap = remember(avatar) {
        runCatching {
            val encoded = context.assets.open("avatars/${avatar.fileName}")
                .bufferedReader()
                .use { it.readText() }
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE9E5FF)),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "${avatar.displayName}, ${avatar.role}",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}
