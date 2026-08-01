package be.hablamos.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CoachReply(
    val text: String,
    val correction: String? = null,
    val suggestion: String? = null
)

class AiCoachService(private val endpoint: String = "") {
    suspend fun reply(message: String, level: String, goal: String): CoachReply = withContext(Dispatchers.IO) {
        if (endpoint.isBlank()) return@withContext localDemoReply(message, level)

        runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject()
                .put("message", message)
                .put("level", level)
                .put("goal", goal)
                .toString()
            connection.outputStream.bufferedWriter().use { it.write(payload) }

            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            CoachReply(
                text = json.optString("reply", "¡Muy bien! Continúa."),
                correction = json.optString("correction").takeIf { it.isNotBlank() },
                suggestion = json.optString("suggestion").takeIf { it.isNotBlank() }
            )
        }.getOrElse { localDemoReply(message, level) }
    }

    private fun localDemoReply(message: String, level: String): CoachReply {
        val normalized = message.lowercase()
        return when {
            normalized.contains("bonjour") || normalized.contains("salut") -> CoachReply(
                text = "¡Hola! ¿Cómo estás hoy?",
                correction = "En espagnol, utilise « Hola » pour dire bonjour.",
                suggestion = "Réponds : Estoy bien, gracias."
            )
            normalized.contains("restaurant") || normalized.contains("manger") -> CoachReply(
                text = "Perfecto. Imagina que estamos en un restaurante. ¿Qué quieres pedir?",
                suggestion = "Essaie : Quisiera una paella, por favor."
            )
            normalized.contains("hotel") -> CoachReply(
                text = "Buenas tardes. Bienvenido al hotel. ¿Tiene una reserva?",
                suggestion = "Essaie : Sí, tengo una reserva a nombre de…"
            )
            else -> CoachReply(
                text = if (level == "A1") "Muy bien. Dime una frase corta sobre tu día." else "Interesante. ¿Puedes explicarlo con un poco más de detalle?",
                suggestion = if (level == "A1") "Exemple : Hoy estoy de vacaciones." else "Utilise porque, pero ou también."
            )
        }
    }
}
