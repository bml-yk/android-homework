package cn.edu.sicnucs.bml.homework5_recyclerview

import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnucs.bml.homework5_recyclerview.model.Card
import cn.edu.sicnucs.bml.homework5_recyclerview.model.CardMatchingGame

class CardAdapter(
    private val game: CardMatchingGame,
    private val photoResources: IntArray,
    private val bitmapFor: (Int) -> Bitmap,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = game.count

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(game.cardAtIndex(position), position)
    }

    fun refresh() {
        notifyItemRangeChanged(0, itemCount)
    }

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardImage = itemView.findViewById<ImageView>(R.id.cardImage)
        private val cardLabel = itemView.findViewById<TextView>(R.id.cardLabel)
        private val cardBack = itemView.findViewById<TextView>(R.id.cardBack)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardClick(position)
                }
            }
        }

        fun bind(card: Card, index: Int) {
            val context = itemView.context
            val revealed = card.isChosen || card.isMatched
            val density = context.resources.displayMetrics.density

            itemView.isEnabled = !card.isMatched
            itemView.alpha = if (card.isMatched) 0.65f else 1f
            itemView.background = GradientDrawable().apply {
                setColor(
                    ContextCompat.getColor(
                        context,
                        if (revealed) R.color.white else R.color.card_hidden
                    )
                )
                cornerRadius = 8f * density
                setStroke(
                    (2f * density).toInt(),
                    ContextCompat.getColor(
                        context,
                        if (card.isMatched) R.color.card_matched_border else R.color.card_border
                    )
                )
            }

            cardImage.visibility = if (revealed) View.VISIBLE else View.INVISIBLE
            cardLabel.visibility = if (revealed) View.VISIBLE else View.INVISIBLE
            cardBack.visibility = if (revealed) View.INVISIBLE else View.VISIBLE

            if (revealed) {
                val rankIndex = Card.rankStrings.indexOf(card.rank)
                val photoResource = photoResources[rankIndex]
                cardImage.setImageBitmap(bitmapFor(photoResource))
                cardLabel.text = context.getString(R.string.card_label, card.rank, card.suit)
                cardLabel.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (card.suit == "♥" || card.suit == "♦") {
                            R.color.card_rank_red
                        } else {
                            R.color.card_rank_black
                        }
                    )
                )
                cardLabel.setTypeface(null, Typeface.BOLD)
            }

            itemView.contentDescription = when {
                card.isMatched -> context.getString(R.string.matched, card.suit, card.rank)
                card.isChosen -> context.getString(R.string.face, card.suit, card.rank)
                else -> context.getString(R.string.back, index + 1)
            }
        }
    }
}
