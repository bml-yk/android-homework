package cn.edu.sicnucs.bml.homework4_fanpai_game
import cn.edu.sicnucs.bml.homework4_fanpai_game.model.*
import org.junit.Assert.*
import org.junit.Test
class CardGameTest {
 @Test fun sameRankScoresFourteen() {
  val g=CardMatchingGame(listOf(Card("♥","A"),Card("♠","A")))
  g.chooseCardAtIndex(0); g.chooseCardAtIndex(1)
  assertEquals(14,g.score); assertTrue(g.allMatched)
  g.chooseCardAtIndex(0); assertEquals(14,g.score)
 }
 @Test fun sameSuitScoresTwo() {
  val g=CardMatchingGame(listOf(Card("♥","A"),Card("♥","8")))
  g.chooseCardAtIndex(0); g.chooseCardAtIndex(1)
  assertEquals(2,g.score); assertTrue(g.allMatched)
 }
 @Test fun mismatchKeepsSecondOpen() {
  val g=CardMatchingGame(listOf(Card("♥","A"),Card("♠","8")))
  g.chooseCardAtIndex(0); g.chooseCardAtIndex(1)
  assertEquals(-4,g.score); assertFalse(g.cardAtIndex(0).isChosen)
  assertTrue(g.cardAtIndex(1).isChosen); assertFalse(g.hasAvailableMatch)
 }
 @Test fun turningBackDoesNotCharge() {
  val g=CardMatchingGame(listOf(Card("♥","A")))
  g.chooseCardAtIndex(0); g.chooseCardAtIndex(0)
  assertEquals(-1,g.score); assertFalse(g.cardAtIndex(0).isChosen)
 }
 @Test fun deckContainsFiftyTwoDistinctCards() {
  val deck=Deck(); val cards=(1..52).map { requireNotNull(deck.drawRandomCard()).toString() }
  assertEquals(52,cards.toSet().size); assertNull(deck.drawRandomCard())
 }
 @Test fun newRoundResetsState() {
  val g=CardMatchingGame(16)
  assertEquals(16,g.count); assertEquals(0,g.score)
  assertTrue((0 until 16).all { !g.cardAtIndex(it).isChosen && !g.cardAtIndex(it).isMatched })
 }
 @Test fun savedGameRetainsScoreAndCards() {
  val g=CardMatchingGame(listOf(Card("♥","A"),Card("♠","A")))
  g.chooseCardAtIndex(0)
  val bytes=java.io.ByteArrayOutputStream()
  java.io.ObjectOutputStream(bytes).use { it.writeObject(g) }
  val restored=java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() as CardMatchingGame }
  assertEquals(-1,restored.score); assertTrue(restored.cardAtIndex(0).isChosen)
  restored.chooseCardAtIndex(1); assertEquals(14,restored.score)
 }
}
