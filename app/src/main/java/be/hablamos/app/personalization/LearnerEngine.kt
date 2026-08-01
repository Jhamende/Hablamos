package be.hablamos.app.personalization

import kotlin.math.max
import kotlin.math.min

data class SkillState(
    val name: String,
    val score: Int,
    val confidence: Float,
    val mistakes: Int,
    val lastPracticedEpochDay: Long
)

data class ReviewItem(
    val id: String,
    val prompt: String,
    val answer: String,
    val strength: Float,
    val repetitions: Int,
    val dueEpochDay: Long,
    val tags: Set<String>
)

data class LearnerSnapshot(
    val level: String,
    val goal: String,
    val dailyMinutes: Int,
    val skills: List<SkillState>,
    val reviews: List<ReviewItem>,
    val recurringErrors: Map<String, Int>
)

data class SessionBlock(
    val type: String,
    val title: String,
    val minutes: Int,
    val reason: String,
    val focus: Set<String>
)

data class AdaptiveSession(
    val title: String,
    val estimatedMinutes: Int,
    val blocks: List<SessionBlock>,
    val tutorMessage: String
)

object LearnerEngine {
    fun buildSession(snapshot: LearnerSnapshot, todayEpochDay: Long): AdaptiveSession {
        val weakest = snapshot.skills.minByOrNull { it.score + (it.confidence * 20).toInt() }
        val dueReviews = snapshot.reviews.filter { it.dueEpochDay <= todayEpochDay }.sortedBy { it.strength }
        val topError = snapshot.recurringErrors.maxByOrNull { it.value }?.key
        val budget = snapshot.dailyMinutes.coerceIn(5, 30)
        val blocks = mutableListOf<SessionBlock>()

        if (dueReviews.isNotEmpty()) {
            blocks += SessionBlock("review", "Révision intelligente", min(4, budget), "${dueReviews.size} éléments arrivent à échéance", dueReviews.flatMap { it.tags }.toSet())
        }
        weakest?.let {
            blocks += SessionBlock("skill", "Renforcer ${it.name}", min(5, max(3, budget / 3)), "C'est actuellement ta compétence la plus fragile (${it.score} %)", setOf(it.name))
        }
        topError?.let {
            blocks += SessionBlock("correction", "Corriger : $it", 3, "Cette erreur revient souvent", setOf(it))
        }
        blocks += SessionBlock("conversation", "Conversation avec ton professeur", max(3, budget - blocks.sumOf { it.minutes }), "Mettre immédiatement les acquis en situation réelle", setOf(snapshot.goal, snapshot.level))

        val trimmed = trimToBudget(blocks, budget)
        return AdaptiveSession(
            title = "Ta séance personnalisée",
            estimatedMinutes = trimmed.sumOf { it.minutes },
            blocks = trimmed,
            tutorMessage = tutorMessage(snapshot, weakest, dueReviews.size, topError)
        )
    }

    fun updateReview(item: ReviewItem, quality: Int, todayEpochDay: Long): ReviewItem {
        val q = quality.coerceIn(0, 5)
        val nextStrength = (item.strength + (q - 2) * 0.12f).coerceIn(0.05f, 1f)
        val nextRepetitions = if (q < 3) 0 else item.repetitions + 1
        val interval = when (nextRepetitions) {
            0 -> 1L
            1 -> 2L
            2 -> 5L
            else -> (nextRepetitions * nextRepetitions * (1f + nextStrength)).toLong().coerceAtMost(120L)
        }
        return item.copy(strength = nextStrength, repetitions = nextRepetitions, dueEpochDay = todayEpochDay + interval)
    }

    private fun trimToBudget(blocks: List<SessionBlock>, budget: Int): List<SessionBlock> {
        var left = budget
        return blocks.mapNotNull { block ->
            if (left <= 0) null else block.copy(minutes = min(block.minutes, left)).also { left -= it.minutes }
        }
    }

    private fun tutorMessage(snapshot: LearnerSnapshot, weakest: SkillState?, dueCount: Int, topError: String?): String {
        val parts = mutableListOf("Je connais ton objectif : ${snapshot.goal}.")
        weakest?.let { parts += "Aujourd'hui, nous allons surtout renforcer ${it.name.lowercase()}." }
        if (dueCount > 0) parts += "$dueCount révisions sont prêtes au bon moment."
        topError?.let { parts += "Je vais aussi t'aider à éliminer l'erreur « $it »." }
        return parts.joinToString(" ")
    }
}
