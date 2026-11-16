package cn.edu.sicnucs.bml.homework4_fanpai_game

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import cn.edu.sicnucs.bml.homework4_fanpai_game.model.CardMatchingGame

class GameActivity : Screen() {
 companion object { const val BEST = "best" }
 private lateinit var game: CardMatchingGame
 private var best = 0
 private val tiles = mutableListOf<FrameLayout>()
 private val photos = intArrayOf(R.drawable.photo_a,R.drawable.photo_2,R.drawable.photo_3,R.drawable.photo_4,R.drawable.photo_5,R.drawable.photo_6,R.drawable.photo_7,R.drawable.photo_8,R.drawable.photo_9,R.drawable.photo_10,R.drawable.photo_j,R.drawable.photo_q,R.drawable.photo_k)
 private val bitmapCache = mutableMapOf<Int, android.graphics.Bitmap>()
 private val ranks = listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
 @Suppress("DEPRECATION")
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); showLayout(R.layout.activity_game)
  game = (savedInstanceState?.getSerializable("game") as? CardMatchingGame) ?: CardMatchingGame(16)
  best = savedInstanceState?.getInt(BEST) ?: 0
  val grid = findViewById<GridLayout>(R.id.cardGrid)
  repeat(game.count) { i ->
   val tile = FrameLayout(this)
   tile.id = View.generateViewId(); tile.isFocusable = true
   tile.setOnClickListener { game.chooseCardAtIndex(i); render() }
   val params = GridLayout.LayoutParams(GridLayout.spec(i/4),GridLayout.spec(i%4,1f)).apply {
    width=0; height=dp(112); setMargins(dp(3),dp(3),dp(3),dp(3))
   }
   grid.addView(tile,params); tiles.add(tile)
  }
  findViewById<Button>(R.id.btnReset).setOnClickListener { game=CardMatchingGame(16); render() }
  findViewById<Button>(R.id.btnExit).setOnClickListener { finish() }
  onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
   override fun handleOnBackPressed() { finish() }
  })
  render()
 }
 private fun dp(value: Int) = (value*resources.displayMetrics.density).toInt()
 private fun render() {
  best=maxOf(best,game.score)
  setResult(Activity.RESULT_OK,Intent().putExtra(BEST,best))
  findViewById<TextView>(R.id.tvScore).text=getString(R.string.score_value,game.score)
  findViewById<TextView>(R.id.tvBest).text=getString(R.string.best_value,best)
  findViewById<TextView>(R.id.tvStatus).setText(when {
   game.allMatched -> R.string.done
   !game.hasAvailableMatch -> R.string.stuck
   else -> R.string.playing
  })
  tiles.forEachIndexed { i,tile ->
   val card=game.cardAtIndex(i)
   tile.removeAllViews(); tile.isEnabled= !card.isMatched
   tile.alpha=if(card.isMatched) 0.65f else 1f
   tile.background=GradientDrawable().apply {
    setColor(if(card.isChosen) Color.WHITE else Color.rgb(52,69,103))
    cornerRadius=dp(8).toFloat()
    setStroke(dp(2),if(card.isMatched) Color.rgb(47,145,95) else Color.rgb(168,181,201))
   }
   if(card.isChosen || card.isMatched) {
    val photo=ImageView(this).apply {
     val resource = photos[ranks.indexOf(card.rank)]
     setImageBitmap(bitmapCache.getOrPut(resource) { PhotoLoader.load(resources,resource) })
     scaleType=ImageView.ScaleType.CENTER_CROP
     importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    tile.addView(photo,FrameLayout.LayoutParams(-1,-1).apply { setMargins(dp(3),dp(3),dp(3),dp(3)) })
    listOf(Gravity.TOP or Gravity.START,Gravity.BOTTOM or Gravity.END).forEach { position ->
     val label=TextView(this).apply {
      text=getString(R.string.card_label,card.rank,card.suit)
      textSize=13f; setTypeface(null,Typeface.BOLD)
      setTextColor(if(card.suit=="♥" || card.suit=="♦") Color.rgb(178,26,48) else Color.rgb(25,32,44))
      setBackgroundColor(Color.argb(235,255,255,255)); setPadding(dp(3),dp(1),dp(3),dp(1))
      gravity=Gravity.CENTER; importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
     }
     tile.addView(label,FrameLayout.LayoutParams(-2,-2,position).apply { setMargins(dp(4),dp(4),dp(4),dp(4)) })
    }
    tile.contentDescription=getString(if(card.isMatched) R.string.matched else R.string.face,card.suit,card.rank)
   } else {
    tile.addView(TextView(this).apply {
     setText(R.string.symbol); textSize=30f; gravity=Gravity.CENTER; setTextColor(Color.rgb(235,201,144))
     importantForAccessibility=View.IMPORTANT_FOR_ACCESSIBILITY_NO
    },FrameLayout.LayoutParams(-1,-1))
    tile.contentDescription=getString(R.string.back,i+1)
   }
  }
 }
 override fun onSaveInstanceState(outState: Bundle) {
  outState.putSerializable("game",game); outState.putInt(BEST,best); super.onSaveInstanceState(outState)
 }
}
