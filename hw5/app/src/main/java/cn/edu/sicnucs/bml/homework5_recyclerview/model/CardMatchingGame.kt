package cn.edu.sicnucs.bml.homework5_recyclerview.model

class CardMatchingGame(initialCards: List<Card>) : java.io.Serializable {
    private val cards = initialCards.toMutableList()
    constructor(count: Int) : this(drawCards(count))

    companion object {
        const val MAX_SELECTED_CARDS = 3

        private fun drawCards(count: Int): List<Card> {
            require(count in 1..52)
            val deck = Deck()
            return List(count) { requireNotNull(deck.drawRandomCard()) }
        }
    }

    val count: Int get() = cards.size
    val allMatched: Boolean get() = cards.all { it.isMatched }
    val hasAvailableMatch: Boolean get() {
        val remaining = cards.filter { !it.isMatched }
        if (remaining.size < MAX_SELECTED_CARDS) return false

        return remaining.indices.any { first ->
            (first + 1 until remaining.size).any { second ->
                (second + 1 until remaining.size).any { third ->
                    remaining[first].match(
                        arrayOf(remaining[second], remaining[third])
                    ) > 0
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

        if (selectedCards.size == MAX_SELECTED_CARDS - 1) {
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
