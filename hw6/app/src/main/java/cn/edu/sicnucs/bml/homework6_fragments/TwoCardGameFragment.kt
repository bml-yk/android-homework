package cn.edu.sicnucs.bml.homework6_fragments

import cn.edu.sicnucs.bml.homework6_fragments.model.CardMatchingGame

class TwoCardGameFragment : BaseGameFragment() {
    override val cardsPerMatch: Int = 2
    override val cardCount: Int = 12
    override val gameTitle: Int = R.string.two_game_title
    override val gameRules: Int = R.string.two_game_rules
    override val playingText: Int = R.string.playing_two

    override fun gameFrom(state: AppState): CardMatchingGame? = state.twoCardGame

    override fun saveGame(state: AppState, game: CardMatchingGame) {
        state.twoCardGame = game
    }

    override fun bestFrom(state: AppState): Int = state.twoCardBest

    override fun saveBest(state: AppState, best: Int) {
        state.twoCardBest = best
    }

    companion object {
        const val TAG = "TwoCardGameFragment"
    }
}
