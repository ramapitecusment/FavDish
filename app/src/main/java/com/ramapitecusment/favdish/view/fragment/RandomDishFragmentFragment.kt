package com.ramapitecusment.favdish.view.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.ramapitecusment.favdish.R
import com.ramapitecusment.favdish.application.FavDishApplication
import com.ramapitecusment.favdish.databinding.FragmentRandomDishBinding
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.repository.network.RandomDish
import com.ramapitecusment.favdish.utils.Constants
import com.ramapitecusment.favdish.viewmodel.*

class RandomDishFragmentFragment : Fragment() {

    private val viewModel: RandomDishViewModel by viewModels {
        RandomDishViewModelFactory(requireActivity().application)
    }

    private var addedToFavorite = false
    private var customProgressDialog: Dialog? = null

    private lateinit var binding: FragmentRandomDishBinding

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, sI: Bundle?): View {
        binding = FragmentRandomDishBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.srlRandomSwipe.setOnRefreshListener {
            viewModel.refreshDataFromInternet()
        }
    }

    private fun observeViewModel() {
        viewModel.randomDishResponse.observe(viewLifecycleOwner) { randomDishResponse ->
            randomDishResponse?.let {
                if (binding.srlRandomSwipe.isRefreshing) {
                    binding.srlRandomSwipe.isRefreshing = false
                }
                setRandomDishResponseInUI(randomDishResponse.recipes[0])
            }
        }

        viewModel.randomDishLoading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let {
                Log.i(Constants.TAG, "isLoading: $isLoading")
                if (isLoading && !binding.srlRandomSwipe.isRefreshing){
                    showProgressbarDialog()
                } else {
                    hideProgressDialog()
                }
            }
        }

        viewModel.randomDishLoadingError.observe(viewLifecycleOwner) { isError ->
            isError?.let {
                if (binding.srlRandomSwipe.isRefreshing) {
                    binding.srlRandomSwipe.isRefreshing = false
                }
                Log.i(Constants.TAG, "isError: $isError")
            }
        }
    }

    private fun showProgressbarDialog() {
        customProgressDialog = Dialog(requireActivity())
        customProgressDialog?.let {
            it.setContentView(R.layout.dialog_progress_bar)
            it.show()
        }
    }

    private fun hideProgressDialog() {
        customProgressDialog?.let {
            it.dismiss()
        }
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(binding.ivDishImage)

        binding.tvTitle.text = recipe.title

        var dishType: String = "other"
        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            binding.tvType.text = dishType
        }


        binding.tvCategory.text = "Other"
        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            ingredients = if (ingredients.isEmpty()) {
                value.original
            } else {
                ingredients + ", \n" + value.original
            }
        }

        binding.tvIngredients.text = ingredients
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            binding.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        binding.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

        binding.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )
        addedToFavorite = false


        binding.ivFavoriteDish.setOnClickListener {
            if (addedToFavorite) {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )

                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)
                addedToFavorite = true

                binding.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )

                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favorites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}