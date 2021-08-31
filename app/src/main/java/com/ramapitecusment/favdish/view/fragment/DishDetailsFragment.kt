package com.ramapitecusment.favdish.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.ramapitecusment.favdish.R
import com.ramapitecusment.favdish.application.FavDishApplication
import com.ramapitecusment.favdish.databinding.FragmentDishDetailsBinding
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.utils.Constants
import com.ramapitecusment.favdish.viewmodel.FavDishViewModel
import com.ramapitecusment.favdish.viewmodel.FavDishViewModelFactory

class DishDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDishDetailsBinding
    private val viewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory(((requireActivity().application) as FavDishApplication).repository)
    }
    private val args: DishDetailsFragmentArgs by navArgs()

    private var favDish: FavDish? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, sI: Bundle?): View {
        binding = FragmentDishDetailsBinding.inflate(i)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_share, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                shareIntent()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareIntent() {
        val type = "text/plain"
        val subject = "Checkout this dish recipe"
        val shareWith = "Share with"
        var extraText = ""

        favDish?.let {
            var image = ""
            if (it.imageSource == Constants.DISH_IMAGE_SOURCE_ONLINE) image = it.image

            var cookingInstructions = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cookingInstructions = Html.fromHtml(
                    it.directionToCook,
                    Html.FROM_HTML_MODE_COMPACT
                ).toString()
            } else {
                @Suppress("DEPRECATION")
                cookingInstructions = Html.fromHtml(it.directionToCook).toString()
            }

            extraText =
                "$image \n" +
                        "\n Title:  ${it.title} \n\n Type: ${it.type} \n\n Category: ${it.category}" +
                        "\n\n Ingredients: \n ${it.ingredients} \n\n Instructions To Cook: \n $cookingInstructions" +
                        "\n\n Time required to cook the dish approx ${it.cookingTime} minutes."
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = type
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, extraText)
        startActivity(Intent.createChooser(intent, shareWith))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favDish = args.dish
        binding.dish = favDish
        setOnClickListeners()
        setDirectionsToCook()
        checkLike()
    }

    private fun setDirectionsToCook() {
        favDish?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvCookingDirection.text = Html.fromHtml(
                    it.directionToCook,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                @Suppress("DEPRECATION")
                binding.tvCookingDirection.text = Html.fromHtml(it.directionToCook)
            }
        }
    }

    private fun setOnClickListeners() {
        binding.ivFavoriteDish.setOnClickListener {
            args.dish.isFavouriteDish = !args.dish.isFavouriteDish
            viewModel.update(args.dish)
            checkLike()
        }
    }

    private fun checkLike() {
        if (args.dish.isFavouriteDish) binding.ivFavoriteDish.setImageResource(R.drawable.ic_favorite_selected)
        else binding.ivFavoriteDish.setImageResource(R.drawable.ic_favorite_unselected)
    }
}