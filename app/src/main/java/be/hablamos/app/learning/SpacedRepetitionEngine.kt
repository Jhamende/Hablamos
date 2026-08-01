package be.hablamos.app.learning

import kotlin.math.max
import kotlin.math.roundToInt

data class ReviewCard(
    val id: String,
    val spanish: String,
    val french: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueAtEpochDay: Long = 0,
    val lapses: Int = 0,
    val lastScore: Int = 0
)

data class ReviewResult(
    val card: ReviewCard,
    val masteryDelta: Int,
    val message: String
)

object SpacedRepetitionEngine {
    /**
     * SM-2 inspired scheduling. score ranges from 0 (forgotten) to 5 (perfect).
     */
    fun review(card: ReviewCard, score: Int, todayEpochDay: Long): ReviewResult {
        val quality = score.coerceIn(0, 5)
        if (quality < 3) {
            val reset = card.copy(
                intervalDays = 1,
                repetitions = 0,
                dueAtEpochDay = todayEpochDay + 1,
                lapses = card.lapses + 1,
                lastScore = quality,
                easeFactor = max(1.3, card.easeFactor - 0.2)
            )
            return ReviewResult(reset, -2, "À revoir demain")
        }

        val repetitions = card.repetitions + 1
        val interval = when (repetitions) {
            1 -> 1
            2 -> 3
            else -> max(4, (card.intervalDays * card.easeFactor).roundToInt())
        }
        val nextEase = max(
            1.3,
            card.easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        )
        val updated = card.copy(
            easeFactor = nextEase,
            intervalDays = interval,
            repetitions = repetitions,
            dueAtEpochDay = todayEpochDay + interval,
            lastScore = quality
        )
        return ReviewResult(updated, if (quality == 5) 3 else 2, "Prochaine révision dans $interval jour(s)")
    }

    fun dueCards(cards: List<ReviewCard>, todayEpochDay: Long, limit: Int = 20): List<ReviewCard> =
        cards.filter { it.dueAtEpochDay <= todayEpochDay }
            .sortedWith(compareByDescending<ReviewCard> { it.lapses }.thenBy { it.dueAtEpochDay })
            .take(limit)
}
