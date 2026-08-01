package be.hablamos.app.v6

import android.content.Context

data class SkillScore(
    val vocabulary: Int = 42,
    val listening: Int = 35,
    val speaking: Int = 28,
    val grammar: Int = 46,
    val reading: Int = 52,
    val pronunciation: Int = 31
) {
    fun weakest(): LearningSkill = listOf(
        LearningSkill.VOCABULARY to vocabulary,
        LearningSkill.LISTENING to listening,
        LearningSkill.SPEAKING to speaking,
        LearningSkill.GRAMMAR to grammar,
        LearningSkill.READING to reading,
        LearningSkill.PRONUNCIATION to pronunciation
    ).minBy { it.second }.first
}

enum class LearningSkill { VOCABULARY, LISTENING, SPEAKING, GRAMMAR, READING, PRONUNCIATION }

data class PersonalLearningProfile(
    val name: String = "Amigo",
    val cefrLevel: String = "A1",
    val goal: String = "Converser",
    val dailyMinutes: Int = 15,
    val streak: Int = 1,
    val xp: Int = 0,
    val skills: SkillScore = SkillScore(),
    val recurringErrors: List<String> = listOf("ser / estar", "genre des noms", "prononciation de j"),
    val dueReviews: Int = 8,
    val knownWords: Int = 146,
    val activePhrases: Int = 54
)

class PersonalProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("hablamos_v6_profile", Context.MODE_PRIVATE)

    fun load(fallbackName: String, fallbackLevel: String, fallbackGoal: String, dailyMinutes: Int): PersonalLearningProfile =
        PersonalLearningProfile(
            name = prefs.getString("name", fallbackName) ?: fallbackName,
            cefrLevel = prefs.getString("level", fallbackLevel) ?: fallbackLevel,
            goal = prefs.getString("goal", fallbackGoal) ?: fallbackGoal,
            dailyMinutes = prefs.getInt("daily_minutes", dailyMinutes),
            streak = prefs.getInt("streak", 1),
            xp = prefs.getInt("xp", 0),
            dueReviews = prefs.getInt("due_reviews", 8),
            knownWords = prefs.getInt("known_words", 146),
            activePhrases = prefs.getInt("active_phrases", 54),
            skills = SkillScore(
                vocabulary = prefs.getInt("skill_vocabulary", 42),
                listening = prefs.getInt("skill_listening", 35),
                speaking = prefs.getInt("skill_speaking", 28),
                grammar = prefs.getInt("skill_grammar", 46),
                reading = prefs.getInt("skill_reading", 52),
                pronunciation = prefs.getInt("skill_pronunciation", 31)
            )
        )

    fun save(profile: PersonalLearningProfile) {
        prefs.edit()
            .putString("name", profile.name)
            .putString("level", profile.cefrLevel)
            .putString("goal", profile.goal)
            .putInt("daily_minutes", profile.dailyMinutes)
            .putInt("streak", profile.streak)
            .putInt("xp", profile.xp)
            .putInt("due_reviews", profile.dueReviews)
            .putInt("known_words", profile.knownWords)
            .putInt("active_phrases", profile.activePhrases)
            .putInt("skill_vocabulary", profile.skills.vocabulary)
            .putInt("skill_listening", profile.skills.listening)
            .putInt("skill_speaking", profile.skills.speaking)
            .putInt("skill_grammar", profile.skills.grammar)
            .putInt("skill_reading", profile.skills.reading)
            .putInt("skill_pronunciation", profile.skills.pronunciation)
            .apply()
    }
}