package com.example.translator.ui.selected

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.translator.databinding.TranslationItemBinding
import com.example.translator.domain.models.Translation

class SelectedTranslationAdapter(
    private val listener: OnItemClickListener,
): ListAdapter<Translation, SelectedTranslationAdapter.SelectedTranslationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedTranslationViewHolder {
        val binding = TranslationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectedTranslationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectedTranslationViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
    inner class SelectedTranslationViewHolder(private val binding: TranslationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val translation = getItem(position)
                        listener.onItemClick(translation)
                    }
                }
            }
        }

        fun bind(translation: Translation) {
            binding.apply {
                sourceText.text = translation.sourceText
                translationText.text = translation.text
                actionSelect.visibility = View.INVISIBLE
            }
        }
    }
    interface OnItemClickListener {
        fun onItemClick(translation: Translation)
    }
    class DiffCallback : DiffUtil.ItemCallback<Translation>() {
        override fun areItemsTheSame(oldItem: Translation, newItem: Translation) =
            newItem.id == oldItem.id
        override fun areContentsTheSame(oldItem: Translation, newItem: Translation) =
            newItem == oldItem
    }
}