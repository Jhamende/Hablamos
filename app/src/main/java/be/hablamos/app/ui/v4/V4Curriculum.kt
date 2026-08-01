package be.hablamos.app.ui.v4

data class CurriculumUnit(
    val id: String,
    val level: String,
    val title: String,
    val icon: String,
    val companion: CompanionKind,
    val targetWords: Int,
    val targetPhrases: Int,
    val lessons: Int,
    val outcomes: List<String>
)

enum class CompanionKind { LOLA, PACO, MIA, PEPE, ALBA }

object HablamosCurriculum {
    val units = listOf(
        CurriculumUnit("a0-survival", "A0", "Premiers mots", "👋", CompanionKind.LOLA, 120, 80, 12, listOf("Saluer", "Donner son nom", "Comprendre les consignes essentielles")),
        CurriculumUnit("a1-identity", "A1", "Identité et famille", "🙂", CompanionKind.MIA, 260, 180, 18, listOf("Se présenter", "Décrire sa famille", "Poser des questions simples")),
        CurriculumUnit("a1-daily", "A1", "Vie quotidienne", "🏡", CompanionKind.LOLA, 320, 220, 20, listOf("Parler de sa routine", "Dire l'heure", "Faire des achats")),
        CurriculumUnit("a1-travel", "A1", "Voyage essentiel", "✈️", CompanionKind.ALBA, 280, 210, 18, listOf("Réserver", "Demander son chemin", "Commander au restaurant")),
        CurriculumUnit("a2-past", "A2", "Raconter au passé", "🕰️", CompanionKind.PACO, 420, 300, 24, listOf("Employer les temps du passé", "Raconter une expérience", "Exprimer une chronologie")),
        CurriculumUnit("a2-social", "A2", "Vie sociale", "🎉", CompanionKind.MIA, 380, 280, 22, listOf("Inviter", "Donner son avis", "Parler de ses loisirs")),
        CurriculumUnit("a2-services", "A2", "Services et santé", "🏥", CompanionKind.LOLA, 360, 260, 22, listOf("Expliquer un problème", "Prendre rendez-vous", "Comprendre des instructions")),
        CurriculumUnit("b1-story", "B1", "Raconter et argumenter", "📖", CompanionKind.PACO, 620, 420, 32, listOf("Structurer un récit", "Justifier une opinion", "Nuancer ses propos")),
        CurriculumUnit("b1-work", "B1", "Travail et projets", "💼", CompanionKind.MIA, 560, 390, 30, listOf("Participer à une réunion", "Écrire un message professionnel", "Présenter un projet")),
        CurriculumUnit("b1-culture", "B1", "Culture hispanophone", "🌎", CompanionKind.ALBA, 480, 360, 28, listOf("Comprendre des références culturelles", "Comparer des habitudes", "Commenter un média")),
        CurriculumUnit("b2-debate", "B2", "Débat et précision", "🗣️", CompanionKind.PACO, 780, 520, 38, listOf("Défendre une position", "Reformuler", "Employer des connecteurs avancés")),
        CurriculumUnit("b2-natural", "B2", "Espagnol naturel", "🎬", CompanionKind.PEPE, 760, 560, 38, listOf("Comprendre l'humour", "Employer des expressions idiomatiques", "Suivre une conversation rapide")),
        CurriculumUnit("c1-professional", "C1", "Maîtrise professionnelle", "🎓", CompanionKind.MIA, 1050, 700, 48, listOf("Présenter avec aisance", "Négocier", "Rédiger des textes structurés")),
        CurriculumUnit("c1-nuance", "C1", "Nuances et registres", "🎭", CompanionKind.PEPE, 980, 680, 46, listOf("Adapter son registre", "Comprendre l'implicite", "Employer des tournures soutenues")),
        CurriculumUnit("c2-mastery", "C2", "Maîtrise complète", "🏆", CompanionKind.ALBA, 2600, 1400, 72, listOf("Comprendre presque tout", "S'exprimer avec précision", "Maîtriser les variations régionales"))
    )

    fun forLevel(level: String): List<CurriculumUnit> {
        val order = listOf("A0", "A1", "A2", "B1", "B2", "C1", "C2")
        val index = order.indexOf(level).coerceAtLeast(0)
        return units.filter { order.indexOf(it.level) <= index + 1 }
    }

    fun totals(): Triple<Int, Int, Int> = Triple(
        units.sumOf { it.targetWords },
        units.sumOf { it.targetPhrases },
        units.sumOf { it.lessons }
    )
}
