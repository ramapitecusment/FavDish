package com.ramapitecusment.favdish.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ramapitecusment.favdish.application.FavDishApplication
import com.ramapitecusment.favdish.databinding.FragmentFavouriteDishesBinding
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.view.activity.MainActivity
import com.ramapitecusment.favdish.view.adapters.FavDishAdapter
import com.ramapitecusment.favdish.viewmodel.FavDishViewModel
import com.ramapitecusment.favdish.viewmodel.FavDishViewModelFactory

class FavouriteDishesFragment : Fragment() {

    private val TAG = "DataToObserve"

    private val viewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    private lateinit var binding: FragmentFavouriteDishesBinding

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, sI: Bundle?): View {
        binding = FragmentFavouriteDishesBinding.inflate(i)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
    }

    private fun setupAdapter() {
        binding.rvFavDish.adapter = FavDishAdapter(clickListener, onMoreClickListener, false)
    }

    private val onMoreClickListener: (dish: FavDish, button: ImageButton) -> Unit = { _, _ ->

    }

    private val clickListener: (dish: FavDish) -> Unit = { dish ->
        navigateToDishDetails(dish)
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }

    private fun navigateToDishDetails(dish: FavDish) {
        findNavController()
            .navigate(FavouriteDishesFragmentDirections.actionFavoriteDishDetails(dish))
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }
}