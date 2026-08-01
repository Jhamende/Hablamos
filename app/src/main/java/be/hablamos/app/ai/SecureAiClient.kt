package be.hablamos.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SecureAiClient(
    private val endpoint: String = "https://YOUR-HABLAMOS-BACKEND.example.com/api/coach"
) {
    suspend fun reply(messages: List<ConversationMessage>, level: String, goal: String): CoachAnswer = withContext(Dispatchers.IO) {
        if (endpoint.contains("YOUR-HABLAMOS-BACKEND")) return@withContext demo(messages.lastOrNull()?.text.orEmpty(), level)
        runCatching {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().apply {
                put("level", level)
                put("goal", goal)
                put("messages", JSONArray().apply {
                    messages.takeLast(12).forEach { put(JSONObject().put("role", it.role).put("text", it.text)) }
                })
            }
            connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }
            val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                .bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) error("Erreur IA ${connection.responseCode}")
            val json = JSONObject(body)
            CoachAnswer(
                reply = json.getString("reply"),
                correction = json.optString("correction").takeIf { it.isNotBlank() },
                explanation = json.optString("explanation").takeIf { it.isNotBlank() },
                suggestedReply = json.optString("suggestedReply").takeIf { it.isNotBlank() },
                xp = json.optInt("xp", 5)
            )
        }.getOrElse { demo(messages.lastOrNull()?.text.orEmpty(), level) }
    }

    private fun demo(text: String, level: String): CoachAnswer {
        val correction = when {
            text.contains("je suis", true) -> "Essaie en espagnol : « Soy… » ou « Estoy… » selon le contexte."
            text.contains("yo gusto", true) -> "On dit « Me gusta… », pas « Yo gusto… »."
            else -> null
        }
        return CoachAnswer(
            reply = if (level == "A1") "¡Muy bien! Cuéntame un poco más. ¿Qué te gusta hacer durante el fin de semana?" else "¡Interesante! ¿Por qué te gusta y desde cuándo lo haces?",
            correction = correction,
            explanation = correction?.let { "Le pronom et la structure changent souvent entre le français et l'espagnol." },
            suggestedReply = "Me gusta pasar tiempo con mi familia porque es importante para mí.",
            xp = if (text.length > 25) 10 else 5
        )
    }
}
