package cn.edu.sicnucs.bml.homework6_fragments.model

class CardMatchingGame(
    initialCards: List<Card>,
    val cardsPerMatch: Int = 3
) : java.io.Serializable {
    private val cards = initialCards.toMutableList()
    constructor(count: Int, cardsPerMatch: Int = 3) : this(
        drawCards(count, cardsPerMatch),
        cardsPerMatch
    )

    init {
        require(cardsPerMatch in 2..3)
        require(cardsPerMatch <= cards.size)
    }

    companion object {
        private fun drawCards(count: Int, cardsPerMatch: Int): List<Card> {
            require(count in 1..52)
            require(count % cardsPerMatch == 0)
            val cards = mutableListOf<Card>()
            var rankIndex = 0
            while (cards.size < count) {
                val rank = Card.rankStrings[rankIndex % Card.rankStrings.size]
                Card.validSuits.toList().shuffled().take(cardsPerMatch).forEach { suit ->
                    cards += Card(suit, rank)
                }
                rankIndex++
            }
            return cards.shuffled()
        }
    }

    val count: Int get() = cards.size
    val allMatched: Boolean get() = cards.all { it.isMatched }
    val hasAvailableMatch: Boolean get() {
        val remaining = cards.filter { !it.isMatched }
        if (remaining.size < cardsPerMatch) return false

        return if (cardsPerMatch == 2) {
            remaining.indices.any { first ->
                (first + 1 until remaining.size).any { second ->
                    remaining[first].match(arrayOf(remaining[second])) > 0
                }
            }
        } else {
            remaining.indices.any { first ->
                (first + 1 until remaining.size).any { second ->
                    (second + 1 until remaining.size).any { third ->
                        remaining[first].match(
                            arrayOf(remaining[second], remaining[third])
                        ) > 0
                    }
                }
            }
        }
    }

    var score = 0
        private set

    fun cardAtIndex(index: Int): Card {
        return cards.get(index)
    }

    fun reset() {
        score = 0
        cards.forEach {
            it.isChosen = false
            it.isMatched = false
        }
    }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) return

        if (card.isChosen) {
            card.isChosen = false
            return
        }

        val selectedCards = cards.filter { it.isChosen && !it.isMatched }
        card.isChosen = true
        score -= COST_TO_CHOOSE

        if (selectedCards.size == cardsPerMatch - 1) {
            val matchScore = card.match(selectedCards.toTypedArray())
            if (matchScore > 0) {
                score += matchScore * MATCH_BONUS
                selectedCards.forEach { it.isMatched = true }
                card.isMatched = true
            } else {
                score -= MISMATCH_PENALTY
                selectedCards.forEach { it.isChosen = false }
                card.isChosen = false
            }
        }
    }
}
