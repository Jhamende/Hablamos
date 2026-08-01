package be.hablamos.app.v6

enum class ActivityType { REVIEW, VOCABULARY, LISTENING, SPEAKING, GRAMMAR, READING, AI_CONVERSATION }

data class SessionActivity(
    val type: ActivityType,
    val title: String,
    val description: String,
    val minutes: Int,
    val xp: Int
)

data class AdaptiveSession(
    val coachMessage: String,
    val focus: LearningSkill,
    val totalMinutes: Int,
    val activities: List<SessionActivity>
)

object AdaptiveSessionEngine {
    fun build(profile: PersonalLearningProfile): AdaptiveSession {
        val weakest = profile.skills.weakest()
        val budget = profile.dailyMinutes.coerceIn(5, 30)
        val activities = mutableListOf<SessionActivity>()

        if (profile.dueReviews > 0) {
            activities += SessionActivity(
                ActivityType.REVIEW,
                "Révisions intelligentes",
                "${profile.dueReviews.coerceAtMost(10)} mots et phrases arrivés à échéance",
                3,
                20
            )
        }

        activities += when (weakest) {
            LearningSkill.SPEAKING -> SessionActivity(ActivityType.SPEAKING, "Parler sans traduire", "Réponds oralement à trois situations de la vie réelle", 4, 35)
            LearningSkill.PRONUNCIATION -> SessionActivity(ActivityType.SPEAKING, "Atelier prononciation", "Répète des phrases et compare rythme et sons", 4, 35)
            LearningSkill.LISTENING -> SessionActivity(ActivityType.LISTENING, "Compréhension active", "Écoute un dialogue naturel puis reconstruis son sens", 4, 30)
            LearningSkill.GRAMMAR -> SessionActivity(ActivityType.GRAMMAR, "Grammaire en contexte", "Corrige les erreurs récurrentes sans règle abstraite", 4, 30)
            LearningSkill.READING -> SessionActivity(ActivityType.READING, "Lecture guidée", "Lis un mini-récit adapté à ton niveau", 4, 25)
            LearningSkill.VOCABULARY -> SessionActivity(ActivityType.VOCABULARY, "Vocabulaire utile", "Apprends des mots fréquents liés à ton objectif ${profile.goal}", 4, 30)
        }

        activities += SessionActivity(
            ActivityType.AI_CONVERSATION,
            "Conversation avec Lola",
            "Une mise en situation adaptée au niveau ${profile.cefrLevel} et à tes erreurs",
            (budget - activities.sumOf { it.minutes }).coerceAtLeast(4),
            50
        )

        val trimmed = activities.takeWhileWithBudget(budget)
        return AdaptiveSession(
            coachMessage = coachMessage(profile, weakest),
            focus = weakest,
            totalMinutes = trimmed.sumOf { it.minutes },
            activities = trimmed
        )
    }

    private fun List<SessionActivity>.takeWhileWithBudget(budget: Int): List<SessionActivity> {
        var used = 0
        return mapNotNull { activity ->
            if (used >= budget) null else {
                val adjusted = activity.copy(minutes = activity.minutes.coerceAtMost(budget - used))
                used += adjusted.minutes
                adjusted.takeIf { it.minutes > 0 }
            }
        }
    }

    private fun coachMessage(profile: PersonalLearningProfile, weakest: LearningSkill): String {
        val focus = when (weakest) {
            LearningSkill.VOCABULARY -> "vocabulaire actif"
            LearningSkill.LISTENING -> "compréhension orale"
            LearningSkill.SPEAKING -> "expression orale"
            LearningSkill.GRAMMAR -> "grammaire en contexte"
            LearningSkill.READING -> "lecture"
            LearningSkill.PRONUNCIATION -> "prononciation"
        }
        return "Aujourd’hui, je te prépare ${profile.dailyMinutes} minutes centrées sur ta priorité : $focus. Je réutiliserai aussi tes erreurs récurrentes pour les transformer en automatismes."
    }
}