package cn.edu.sicnucs.bml.servicemusicplayer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnucs.bml.servicemusicplayer.R
import cn.edu.sicnucs.bml.servicemusicplayer.data.AudioTrack
import cn.edu.sicnucs.bml.servicemusicplayer.databinding.ItemTrackBinding
import java.util.Locale

class TrackAdapter(
    private val onTrackClick: (Int) -> Unit,
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private val tracks = mutableListOf<AudioTrack>()
    private var selectedIndex = RecyclerView.NO_POSITION

    fun submitList(items: List<AudioTrack>) {
        val oldSize = tracks.size
        if (oldSize > 0) {
            tracks.clear()
            notifyItemRangeRemoved(0, oldSize)
        }
        tracks.addAll(items)
        if (items.isNotEmpty()) notifyItemRangeInserted(0, items.size)
    }

    fun setSelectedIndex(index: Int) {
        val oldIndex = selectedIndex
        selectedIndex = index
        if (oldIndex in tracks.indices) notifyItemChanged(oldIndex)
        if (index in tracks.indices) notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position], position, selectedIndex == position)
    }

    override fun getItemCount(): Int = tracks.size

    inner class TrackViewHolder(
        private val binding: ItemTrackBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(track: AudioTrack, position: Int, selected: Boolean) {
            binding.tvTrackNumber.text = String.format(Locale.getDefault(), "%02d", position + 1)
            binding.tvTrackTitle.text = track.title
            binding.tvTrackArtist.text = track.artist
            binding.tvTrackDuration.text = formatDuration(track.duration)
            binding.cardTrack.strokeColor = binding.root.context.getColor(
                if (selected) R.color.accent else R.color.stroke_soft,
            )
            binding.cardTrack.setCardBackgroundColor(
                binding.root.context.getColor(
                    if (selected) R.color.surface_selected else R.color.surface,
                ),
            )
            binding.tvTrackNumber.setTextColor(
                binding.root.context.getColor(if (selected) R.color.accent else R.color.text_muted),
            )
            binding.root.setOnClickListener { onTrackClick(bindingAdapterPosition) }
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs.coerceAtLeast(0L) / 1000
        return String.format(
            Locale.getDefault(),
            "%d:%02d",
            totalSeconds / 60,
            totalSeconds % 60,
        )
    }
}
