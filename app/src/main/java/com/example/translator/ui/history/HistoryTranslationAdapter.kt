package com.example.translator.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.translator.R
import com.example.translator.databinding.TranslationItemBinding
import com.example.translator.domain.models.Translation

class HistoryTranslationAdapter(
    private val listener: OnItemClickListener,
    private val context: Context
    ): ListAdapter<Translation, HistoryTranslationAdapter.HistoryTranslationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTranslationViewHolder {
        val binding = TranslationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryTranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryTranslationViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
    inner class HistoryTranslationViewHolder(private val binding: TranslationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val translation = getItem(position)
                        listener.onItemClick(translation)
                    }
                }
                actionSelect.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val translation = getItem(position)
                        listener.onSelectIconClick(translation, !translation.isSelected)
                    }
                }
            }
        }

        fun bind(translation: Translation) {
            binding.apply {
                sourceText.text = translation.sourceText
                translationText.text = translation.text
                if (translation.isSelected) {
                    actionSelect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_star_24))
                } else {
                    actionSelect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseline_star_outline_24))
                }
            }
        }
    }
    interface OnItemClickListener {
        fun onItemClick(translation: Translation)
        fun onSelectIconClick(translation: Translation, isSelected: Boolean)
    }
    class DiffCallback : DiffUtil.ItemCallback<Translation>() {
        override fun areItemsTheSame(oldItem: Translation, newItem: Translation) =
            newItem.id == oldItem.id
        override fun areContentsTheSame(oldItem: Translation, newItem: Translation) =
            newItem == oldItem

    }
}