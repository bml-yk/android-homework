package cn.edu.sicnucs.bml.homework6_fragments

import cn.edu.sicnucs.bml.homework6_fragments.model.CardMatchingGame
import cn.edu.sicnucs.bml.homework6_fragments.model.Deck
import cn.edu.sicnucs.bml.homework6_fragments.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardGameTest {

    @Test
    fun twoCardsWithSameRankMatch() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "A")),
            cardsPerMatch = 2
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        assertEquals(30, game.score)
        assertTrue(game.allMatched)
    }

    @Test
    fun twoCardsWithSameSuitMatch() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♥", "8")),
            cardsPerMatch = 2
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        assertEquals(6, game.score)
        assertTrue(game.allMatched)
    }

    @Test
    fun twoCardsWithoutCommonRankOrSuitCloseAgain() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "8")),
            cardsPerMatch = 2
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)

        assertEquals(-4, game.score)
        assertTrue(
            (0 until game.count).all {
                !game.cardAtIndex(it).isChosen && !game.cardAtIndex(it).isMatched
            }
        )
        assertFalse(game.hasAvailableMatch)
    }

    @Test
    fun threeCardsWithSameRankMatch() {
        val game = CardMatchingGame(
            listOf(Card("♥", "A"), Card("♠", "A"), Card("♦", "A")),
            cardsPerMatch = 3
        )

        game.chooseCardAtIndex(0)
        game.chooseCardAtIndex(1)
        game.chooseCardAtIndex(2)

        assertEquals(45, game.score)
        assertTrue(game.allMatched)
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
        val game = CardMatchingGame(12, cardsPerMatch = 3)
        game.chooseCardAtIndex(0)

        game.reset()

        assertEquals(12, game.count)
        assertEquals(0, game.score)
        assertTrue(
            (0 until game.count).all {
                !game.cardAtIndex(it).isChosen && !game.cardAtIndex(it).isMatched
            }
        )
    }

    @Test
    fun generatedGamesHaveAvailableMatches() {
        val pairGame = CardMatchingGame(12, cardsPerMatch = 2)
        val tripleGame = CardMatchingGame(12, cardsPerMatch = 3)

        assertTrue(pairGame.hasAvailableMatch)
        assertTrue(tripleGame.hasAvailableMatch)
    }

    @Test
    fun appStateSerializationRetainsGamesAndProfile() {
        val state = AppState(
            profile = ProfileData(
                name = "Alice",
                student = "2024110101",
                hobbies = mutableListOf("reading")
            ),
            twoCardGame = CardMatchingGame(12, cardsPerMatch = 2),
            threeCardGame = CardMatchingGame(12, cardsPerMatch = 3),
            twoCardBest = 12,
            threeCardBest = 24
        )
        state.twoCardGame?.chooseCardAtIndex(0)
        state.threeCardGame?.chooseCardAtIndex(0)

        val bytes = java.io.ByteArrayOutputStream()
        java.io.ObjectOutputStream(bytes).use { it.writeObject(state) }
        val restored = java.io.ObjectInputStream(
            java.io.ByteArrayInputStream(bytes.toByteArray())
        ).use { it.readObject() as AppState }

        assertEquals("Alice", restored.profile.name)
        assertEquals(listOf("reading"), restored.profile.hobbies)
        assertEquals(2, restored.twoCardGame?.cardsPerMatch)
        assertEquals(3, restored.threeCardGame?.cardsPerMatch)
        assertEquals(-1, restored.twoCardGame?.score)
        assertEquals(-1, restored.threeCardGame?.score)
        assertEquals(12, restored.twoCardBest)
        assertEquals(24, restored.threeCardBest)
    }
}
