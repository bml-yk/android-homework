package cn.edu.sicnucs.bml.homework5_recyclerview

import cn.edu.sicnucs.bml.homework5_recyclerview.model.Card
import cn.edu.sicnucs.bml.homework5_recyclerview.model.CardMatchingGame
import cn.edu.sicnucs.bml.homework5_recyclerview.model.Deck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardGameTest {

    @Test
    fun threeCardsWithSameRankMatch() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "A"), Card("♦", "A"))
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)

        assertEquals(45, game.score)
        assertTrue(game.allMatched)
    }

    @Test
    fun threeCardsWithSameSuitMatch() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♥", "8"), Card("♥", "K"))
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)

        assertEquals(9, game.score)
        assertTrue(game.allMatched)
    }

    @Test
    fun threeCardsWithoutCommonRankOrSuitCloseAgain() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "8"), Card("♦", "K"))
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)

        assertEquals(-5, game.score)
        assertTrue(
            (0 until game.count).all {
                !game.cardAtIndex(it).isChosen && !game.cardAtIndex(it).isMatched
            }
        )
        assertFalse(game.hasAvailableMatch)
    }

    @Test
    fun turningBackDoesNotChargeAgain() {
        val game = CardMatchingGame(listOf(Card("♥", "A")))

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(0)

        assertEquals(-1, game.score)
        assertFalse(game.cardAtIndex(0).isChosen)
    }

    @Test
    fun deckContainsFiftyTwoDistinctCards() {
        val deck = Deck()
        val cards = (1..52).map { requireNotNull(deck.drawRandomCard()).toString() }

        assertEquals(52, cards.toSet().size)
        assertNull(deck.drawRandomCard())
    }

    @Test
    fun resetClearsScoreAndCardState() {
        val game = CardMatchingGame(24)
        game.chooseCardAtIndex(0)

        game.reset()

        assertEquals(24, game.count)
        assertEquals(0, game.score)
        assertTrue(
            (0 until game.count).all {
                !game.cardAtIndex(it).isChosen && !game.cardAtIndex(it).isMatched
            }
        )
    }

    @Test
    fun hasAvailableMatchRequiresThreeCompatibleCards() {
        val pairOnly = CardMatchingGame(listOf(Card("♥", "A"), Card("♠", "A")))
        val triple = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "A"), Card("♦", "A"))
        )

        assertFalse(pairOnly.hasAvailableMatch)
        assertTrue(triple.hasAvailableMatch)
    }

    @Test
    fun savedGameRetainsScoreAndCards() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "A"), Card("♦", "A"))
        )
        game.chooseCardAtIndex(0)

        val bytes = java.io.ByteArrayOutputStream()
        java.io.ObjectOutputStream(bytes).use { it.writeObject(game) }
        val restored = java.io.ObjectInputStream(
            java.io.ByteArrayInputStream(bytes.toByteArray())
        ).use { it.readObject() as CardMatchingGame }

        assertEquals(-1, restored.score)
        assertTrue(restored.cardAtIndex(0).isChosen)
        restored.chooseCardAtIndex(1)
        restored.chooseCardAtIndex(2)
        assertEquals(45, restored.score)
        assertTrue(restored.allMatched)
    }
}
