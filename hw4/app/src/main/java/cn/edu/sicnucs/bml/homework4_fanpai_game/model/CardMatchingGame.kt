package cn.edu.sicnucs.bml.homework4_fanpai_game.model

class CardMatchingGame(initialCards: List<Card>) : java.io.Serializable {
    private val cards = initialCards.toMutableList()
    constructor(count: Int) : this(drawCards(count))
    companion object {
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
        return remaining.indices.any { i -> (i+1 until remaining.size).any { j -> remaining[i].match(arrayOf(remaining[j])) > 0 } }
    }

    var score = 0
        private set

    fun cardAtIndex(index: Int): Card {
        return cards.get(index)
    }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    fun chooseCardAtIndex(index: Int) {
        val card= cardAtIndex(index)
        if (!card.isMatched) {
            if (card.isChosen) {
                card.isChosen = false
            } else {
                for (otherCard in cards) {
                    if (otherCard.isChosen && !otherCard.isMatched) {
                        val matchScore = card.match(arrayOf(otherCard))
                        if (matchScore > 0) {
                            score += matchScore * MATCH_BONUS
                            otherCard.isMatched = true
                            card.isMatched = true
                        } else {
                            score -= MISMATCH_PENALTY
                            otherCard.isChosen = false
                        }
                        break
                    }
                }
                score -= COST_TO_CHOOSE
                card.isChosen = true
            }
        }
    }
}
