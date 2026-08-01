package be.hablamos.app.ai

data class ConversationMessage(val role: String, val text: String)

data class CoachAnswer(
    val reply: String,
    val correction: String? = null,
    val explanation: String? = null,
    val suggestedReply: String? = null,
    val xp: Int = 5
)
