package com.ramapitecusment.favdish.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ramapitecusment.favdish.databinding.DialogListItemBinding

class DialogListItemAdapter(
    private val listCategory: List<String>,
    private val selection: String,
    private val listener: OnCategoryClickListener
) : RecyclerView.Adapter<DialogListItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listCategory[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            listener.onClick(item, selection)
        }
    }

    override fun getItemCount(): Int = listCategory.size

    class ViewHolder(private val binding: DialogListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            binding.tvCategory.text = category
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = DialogListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(view)
            }
        }
    }
}

class OnCategoryClickListener(val clickListener: (category: String, selection: String) -> Unit) {
    fun onClick(cat: String, selection: String) = clickListener(cat, selection)
}