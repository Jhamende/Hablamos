package be.hablamos.app.data

data class Phrase(
    val spanish: String,
    val french: String,
    val hint: String
)

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val phrases: List<Phrase>
)

val lessons = listOf(
    Lesson(
        id = 1,
        title = "Les bases",
        subtitle = "Saluer et se présenter",
        emoji = "👋",
        phrases = listOf(
            Phrase("Hola, ¿qué tal?", "Bonjour, comment ça va ?", "Prononce : ola, ké tal"),
            Phrase("Me llamo Jonathan.", "Je m'appelle Jonathan.", "ll se prononce souvent comme y"),
            Phrase("Mucho gusto.", "Enchanté.", "Prononce : moutcho gousto"),
            Phrase("Hasta luego.", "À plus tard.", "Le h est muet")
        )
    ),
    Lesson(
        id = 2,
        title = "Au restaurant",
        subtitle = "Commander simplement",
        emoji = "🍽️",
        phrases = listOf(
            Phrase("Quisiera una mesa para cuatro.", "Je voudrais une table pour quatre.", "Quisiera est une formule polie"),
            Phrase("¿Me trae la carta, por favor?", "Pouvez-vous m'apporter la carte ?", "carta signifie menu"),
            Phrase("Para mí, una paella.", "Pour moi, une paella.", "Une manière naturelle de commander"),
            Phrase("La cuenta, por favor.", "L'addition, s'il vous plaît.", "Phrase essentielle au restaurant")
        )
    ),
    Lesson(
        id = 3,
        title = "Se déplacer",
        subtitle = "Demander son chemin",
        emoji = "🧭",
        phrases = listOf(
            Phrase("¿Dónde está la estación?", "Où se trouve la gare ?", "Dónde signifie où"),
            Phrase("¿Está lejos?", "Est-ce loin ?", "Lejos signifie loin"),
            Phrase("Gire a la derecha.", "Tournez à droite.", "Derecha signifie droite"),
            Phrase("Siga todo recto.", "Continuez tout droit.", "Recto signifie tout droit")
        )
    ),
    Lesson(
        id = 4,
        title = "Faire des achats",
        subtitle = "Prix, tailles et paiements",
        emoji = "🛍️",
        phrases = listOf(
            Phrase("¿Cuánto cuesta?", "Combien cela coûte ?", "Cuánto signifie combien"),
            Phrase("¿Tiene una talla más grande?", "Avez-vous une taille plus grande ?", "Talla signifie taille de vêtement"),
            Phrase("Solo estoy mirando.", "Je regarde seulement.", "Utile pour décliner poliment"),
            Phrase("¿Puedo pagar con tarjeta?", "Puis-je payer par carte ?", "Tarjeta signifie carte")
        )
    )
)
