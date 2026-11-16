package cn.edu.sicnucs.bml.homework6_fragments

import cn.edu.sicnucs.bml.homework6_fragments.model.CardMatchingGame

class ThreeCardGameFragment : BaseGameFragment() {
    override val cardsPerMatch: Int = 3
    override val cardCount: Int = 12
    override val gameTitle: Int = R.string.three_game_title
    override val gameRules: Int = R.string.three_game_rules
    override val playingText: Int = R.string.playing_three

    override fun gameFrom(state: AppState): CardMatchingGame? = state.threeCardGame

    override fun saveGame(state: AppState, game: CardMatchingGame) {
        state.threeCardGame = game
    }

    override fun bestFrom(state: AppState): Int = state.threeCardBest

    override fun saveBest(state: AppState, best: Int) {
        state.threeCardBest = best
    }

    companion object {
        const val TAG = "ThreeCardGameFragment"
    }
}
