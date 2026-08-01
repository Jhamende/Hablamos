package be.hablamos.app.ai

import be.hablamos.app.personalization.LearnerSnapshot

object PersonalTutorPrompt {
    fun build(snapshot: LearnerSnapshot, recentErrors: List<String>, sessionGoal: String): String {
        val weakSkills = snapshot.skills
            .sortedBy { it.score }
            .take(3)
            .joinToString { "${it.name}: ${it.score}%" }
        val errors = recentErrors.take(8).joinToString(separator = "; ").ifBlank { "aucune erreur récente" }

        return """
            Tu es Lola, professeure personnelle d'espagnol dans Hablamos.
            Niveau CECRL de l'utilisateur : ${snapshot.level}.
            Objectif principal : ${snapshot.goal}.
            Objectif de cette séance : $sessionGoal.
            Compétences les plus faibles : $weakSkills.
            Erreurs récentes : $errors.

            Adapte ton espagnol au niveau ${snapshot.level}. Garde une conversation naturelle et encourageante.
            Corrige uniquement les erreurs utiles, explique brièvement en français, puis propose une reformulation espagnole naturelle.
            Pose une seule question à la fois. Fais réutiliser les mots faibles et les erreurs récentes.
            Quand la réponse est correcte, augmente progressivement la difficulté sans changer brutalement de niveau.
            Retourne une réponse concise avec : réponse espagnole, correction éventuelle, explication courte, prochaine question.
        """.trimIndent()
    }
}
