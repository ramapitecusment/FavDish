package com.ramapitecusment.favdish.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ramapitecusment.favdish.R
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.view.adapters.FavDishAdapter

@BindingAdapter("bindImage")
fun bindImage(image: ImageView, imgUrl: String?) {
    imgUrl?.let {
        Glide.with(image.context)
            .load(imgUrl)
            .apply(
                RequestOptions().placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_connection_error)
            ).into(image)
    }
}

@BindingAdapter(value = ["imgUrl", "imgId"], requireAll = false)
fun bindImageAndPalette(relativeLayout: RelativeLayout, imgUrl: String?, image: ImageView) {
    imgUrl?.let { img ->
        Glide.with(image.context)
            .load(img)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { drawable ->
                        Palette.from(drawable.toBitmap()).generate { palette ->
                            val intColor = palette?.vibrantSwatch?.rgb ?: 0
                            relativeLayout.setBackgroundColor(intColor)
                        }
                    }
                    return false
                }
            }).into(image)
    }
}

@BindingAdapter("addDataToRV", "textViewId")
fun setDataToRecyclerView(recyclerView: RecyclerView, data: List<FavDish>?, dishNotFound: TextView) {
    data?.let {
        if (it.isNotEmpty()) {
            val adapter = recyclerView.adapter as? FavDishAdapter
            adapter?.submitList(it)
            recyclerView.visibility = View.VISIBLE
            dishNotFound.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            dishNotFound.visibility = View.VISIBLE
        }
    }
}