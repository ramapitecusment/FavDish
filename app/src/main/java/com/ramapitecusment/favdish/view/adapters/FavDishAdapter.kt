package com.ramapitecusment.favdish.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramapitecusment.favdish.databinding.FavDishItemBinding
import com.ramapitecusment.favdish.repository.database.FavDish

class FavDishAdapter(
    private val clickListener: (dish: FavDish) -> Unit,
    private val moreButtonCLickListener: (dish: FavDish, moreButton: ImageButton) -> Unit,
    private val isFromAllDishesFragment: Boolean
) : ListAdapter<FavDish, FavDishAdapter.ViewHolder>(FavDishCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, isFromAllDishesFragment)
        holder.itemView.setOnClickListener {
            clickListener(item)
        }

        holder.moreButton.setOnClickListener {
            moreButtonCLickListener(item, holder.moreButton)
        }
    }

    class ViewHolder(private val binding: FavDishItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val moreButton: ImageButton = binding.ibMore

        fun bind(dish: FavDish, isFromAllDishesFragment: Boolean) {
            binding.dish = dish
            binding.executePendingBindings()
            checkIsFromAllDishesFragment(isFromAllDishesFragment)
        }

        private fun checkIsFromAllDishesFragment(isFromAllDishesFragment: Boolean) {
            if (isFromAllDishesFragment) binding.ibMore.visibility = View.VISIBLE
            else binding.ibMore.visibility = View.GONE
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(FavDishItemBinding.inflate(LayoutInflater.from(parent.context)))
            }
        }
    }
}

class FavDishCallback : DiffUtil.ItemCallback<FavDish>() {
    override fun areItemsTheSame(oldItem: FavDish, newItem: FavDish): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FavDish, newItem: FavDish): Boolean {
        return oldItem.id == newItem.id
    }
}



