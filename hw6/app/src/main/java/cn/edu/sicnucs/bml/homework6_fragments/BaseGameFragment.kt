package cn.edu.sicnucs.bml.homework6_fragments

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnucs.bml.homework6_fragments.model.CardMatchingGame

abstract class BaseGameFragment : Fragment() {
    protected abstract val cardsPerMatch: Int
    protected abstract val cardCount: Int
    @get:StringRes
    protected abstract val gameTitle: Int
    @get:StringRes
    protected abstract val gameRules: Int
    @get:StringRes
    protected abstract val playingText: Int

    protected abstract fun gameFrom(state: AppState): CardMatchingGame?
    protected abstract fun saveGame(state: AppState, game: CardMatchingGame)
    protected abstract fun bestFrom(state: AppState): Int
    protected abstract fun saveBest(state: AppState, best: Int)

    private lateinit var game: CardMatchingGame
    private lateinit var adapter: CardAdapter
    private var best = 0
    private val bitmapCache = mutableMapOf<Int, Bitmap>()
    private val photos = intArrayOf(
        R.drawable.photo_a,
        R.drawable.photo_2,
        R.drawable.photo_3,
        R.drawable.photo_4,
        R.drawable.photo_5,
        R.drawable.photo_6,
        R.drawable.photo_7,
        R.drawable.photo_8,
        R.drawable.photo_9,
        R.drawable.photo_10,
        R.drawable.photo_j,
        R.drawable.photo_q,
        R.drawable.photo_k
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_game, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = requireActivity() as AppStateHost
        game = gameFrom(host.appState)?.takeIf {
            it.cardsPerMatch == cardsPerMatch && it.count == cardCount
        } ?: CardMatchingGame(cardCount, cardsPerMatch)
        saveGame(host.appState, game)
        best = bestFrom(host.appState)

        view.findViewById<TextView>(R.id.title).setText(gameTitle)
        view.findViewById<TextView>(R.id.rules).setText(gameRules)
        adapter = CardAdapter(
            game = game,
            photoResources = photos,
            bitmapFor = { resource ->
                bitmapCache.getOrPut(resource) { PhotoLoader.load(resources, resource) }
            },
            onCardClick = { index ->
                game.chooseCardAtIndex(index)
                persistAndRender()
            }
        )
        view.findViewById<RecyclerView>(R.id.cardRecyclerView).apply {
            layoutManager = GridLayoutManager(requireContext(), columnCount())
            adapter = this@BaseGameFragment.adapter
            setHasFixedSize(true)
        }
        view.findViewById<Button>(R.id.btnReset).setOnClickListener {
            game.reset()
            persistAndRender()
        }
        render(view)
    }

    override fun onPause() {
        (requireActivity() as AppStateHost).persistState()
        super.onPause()
    }

    private fun persistAndRender() {
        val host = requireActivity() as AppStateHost
        saveGame(host.appState, game)
        render(requireView())
        host.persistState()
    }

    private fun render(view: View) {
        best = maxOf(best, game.score)
        saveBest((requireActivity() as AppStateHost).appState, best)
        view.findViewById<TextView>(R.id.tvScore).text =
            getString(R.string.score_value, game.score)
        view.findViewById<TextView>(R.id.tvBest).text =
            getString(R.string.best_value, best)
        view.findViewById<TextView>(R.id.tvStatus).setText(
            when {
                game.allMatched -> R.string.done
                !game.hasAvailableMatch -> R.string.stuck
                else -> playingText
            }
        )
        adapter.refresh()
    }

    private fun columnCount(): Int {
        val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        return if (cardsPerMatch == 2) {
            if (landscape) 4 else 3
        } else {
            if (landscape) 6 else 4
        }
    }
}
