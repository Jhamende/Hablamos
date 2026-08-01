package be.hablamos.app.ui.v4

data class LearningUnit(
    val id: String,
    val level: String,
    val title: String,
    val icon: String,
    val goal: String,
    val lessonCount: Int,
    val vocabularyTarget: Int,
    val phraseTarget: Int
)

val learningCatalog = listOf(
    LearningUnit("a0-survival", "A0", "Premiers mots", "🌱", "Comprendre et utiliser les mots indispensables", 8, 120, 80),
    LearningUnit("a1-intro", "A1", "Se présenter", "👋", "Saluer, parler de soi et poser des questions simples", 12, 220, 150),
    LearningUnit("a1-family", "A1", "Famille et maison", "🏡", "Décrire son entourage et son quotidien", 14, 260, 180),
    LearningUnit("a1-travel", "A1", "Voyage essentiel", "✈️", "Se déplacer, réserver et demander de l'aide", 16, 300, 220),
    LearningUnit("a2-daily", "A2", "Vie quotidienne", "☀️", "Raconter sa journée et exprimer ses habitudes", 18, 380, 260),
    LearningUnit("a2-social", "A2", "Relations et loisirs", "🎉", "Participer à une conversation simple", 18, 420, 300),
    LearningUnit("b1-stories", "B1", "Raconter et expliquer", "📖", "Parler du passé, de projets et d'opinions", 22, 650, 420),
    LearningUnit("b1-real-life", "B1", "Espagnol réel", "🗣️", "Gérer des situations complexes du quotidien", 24, 720, 480),
    LearningUnit("b2-fluency", "B2", "Fluidité et nuances", "🌊", "Argumenter, nuancer et parler spontanément", 28, 1050, 650),
    LearningUnit("c1-professional", "C1", "Maîtrise professionnelle", "💼", "Communiquer avec précision au travail", 32, 1600, 900),
    LearningUnit("c2-mastery", "C2", "Maîtrise complète", "🏆", "Comprendre les nuances culturelles et stylistiques", 40, 2600, 1400)
)
