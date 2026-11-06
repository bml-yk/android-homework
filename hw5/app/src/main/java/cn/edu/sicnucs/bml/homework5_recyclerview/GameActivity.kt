package cn.edu.sicnucs.bml.homework5_recyclerview

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnucs.bml.homework5_recyclerview.model.CardMatchingGame

class GameActivity : Screen() {
    companion object {
        const val BEST = "best"
        private const val GAME_STATE = "game"
        private const val PORTRAIT_CARD_COUNT = 16
        private const val LANDSCAPE_CARD_COUNT = 24
    }

    private lateinit var game: CardMatchingGame
    private lateinit var cardAdapter: CardAdapter
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

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLayout(R.layout.activity_game)

        val savedGame = savedInstanceState?.getSerializable(GAME_STATE) as? CardMatchingGame
        game = savedGame?.takeIf { it.count == desiredCardCount() }
            ?: CardMatchingGame(desiredCardCount())
        best = savedInstanceState?.getInt(BEST) ?: 0

        cardAdapter = CardAdapter(
            game = game,
            photoResources = photos,
            bitmapFor = { resource ->
                bitmapCache.getOrPut(resource) { PhotoLoader.load(resources, resource) }
            },
            onCardClick = { index ->
                game.chooseCardAtIndex(index)
                render()
            }
        )
        findViewById<RecyclerView>(R.id.cardRecyclerView).apply {
            layoutManager = GridLayoutManager(this@GameActivity, columnCount())
            adapter = cardAdapter
            setHasFixedSize(true)
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            game.reset()
            render()
        }
        findViewById<Button>(R.id.btnExit).setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        render()
    }

    private fun desiredCardCount(): Int {
        return if (isLandscape()) LANDSCAPE_CARD_COUNT else PORTRAIT_CARD_COUNT
    }

    private fun columnCount(): Int = if (isLandscape()) 6 else 4

    private fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    private fun render() {
        best = maxOf(best, game.score)
        setResult(Activity.RESULT_OK, Intent().putExtra(BEST, best))
        findViewById<TextView>(R.id.tvScore).text = getString(R.string.score_value, game.score)
        findViewById<TextView>(R.id.tvBest).text = getString(R.string.best_value, best)
        findViewById<TextView>(R.id.tvStatus).setText(
            when {
                game.allMatched -> R.string.done
                !game.hasAvailableMatch -> R.string.stuck
                else -> R.string.playing
            }
        )
        cardAdapter.refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(GAME_STATE, game)
        outState.putInt(BEST, best)
        super.onSaveInstanceState(outState)
    }
}
