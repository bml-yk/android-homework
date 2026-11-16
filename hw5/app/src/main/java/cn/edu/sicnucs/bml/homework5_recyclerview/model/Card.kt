package cn.edu.sicnucs.bml.homework5_recyclerview.model

class Card(val suit: String, val rank: String, var isChosen:Boolean = false, var isMatched:Boolean = false) : java.io.Serializable {
    companion object {
        val rankStrings= arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    override fun toString(): String {
        return suit + rank
    }

    fun match(otherCards: Array<Card>): Int {
        if (otherCards.isEmpty()) return 0

        val group = arrayOf(this) + otherCards
        return when {
            group.all { it.rank == rank } -> group.size * 4
            group.all { it.suit == suit } -> group.size
            else -> 0
        }
    }
}
