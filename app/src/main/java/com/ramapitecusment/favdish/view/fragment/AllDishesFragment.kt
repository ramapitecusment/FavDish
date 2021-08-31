package com.ramapitecusment.favdish.view.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ramapitecusment.favdish.R
import com.ramapitecusment.favdish.application.FavDishApplication
import com.ramapitecusment.favdish.databinding.DialogListBinding
import com.ramapitecusment.favdish.databinding.FragmentAllDishesBinding
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.utils.Constants
import com.ramapitecusment.favdish.view.activity.AddUpdateDishActivity
import com.ramapitecusment.favdish.view.activity.MainActivity
import com.ramapitecusment.favdish.view.adapters.DialogListItemAdapter
import com.ramapitecusment.favdish.view.adapters.FavDishAdapter
import com.ramapitecusment.favdish.view.adapters.OnCategoryClickListener
import com.ramapitecusment.favdish.viewmodel.FavDishViewModel
import com.ramapitecusment.favdish.viewmodel.FavDishViewModelFactory
import java.util.*

class AllDishesFragment : Fragment() {

    private lateinit var binding: FragmentAllDishesBinding
    private lateinit var customListDialog: Dialog
    private val TAG = "DataToObserve"
    private lateinit var adapter: FavDishAdapter

    private val viewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, sI: Bundle?): View {
        binding = FragmentAllDishesBinding.inflate(i)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = FavDishAdapter(clickListener, onMoreClickListener, true)
        binding.rvFavDish.adapter = adapter
    }

    private val onMoreClickListener: (dish: FavDish, button: ImageButton) -> Unit = { dish, more ->
        val popup = PopupMenu(requireContext(), more)
        popup.menuInflater.inflate(R.menu.menu_more, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_edit_dish) {
                val intent = Intent(requireContext(), AddUpdateDishActivity::class.java)
                intent.putExtra(Constants.DISH_UPDATE, dish)
                startActivity(intent)
            } else if (menuItem.itemId == R.id.action_delete_dish) {
                deleteDish(dish)
            }
            true
        }
        popup.show()
    }

    private val clickListener: (dish: FavDish) -> Unit = { dish ->
        navigateToDishDetails(dish)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_all_dishes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_dish -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                return true
            }
            R.id.action_filter_dish -> {
                filterDishDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.showBottomNavigationView()
        }
    }

    private fun navigateToDishDetails(dish: FavDish) {
        findNavController().navigate(AllDishesFragmentDirections.actionDishDetails(dish))
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)?.hideBottomNavigationView()
        }
    }

    private fun deleteDish(dish: FavDish) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.title_delete_dish))
        builder.setMessage(getString(R.string.msg_delete_dish_dialog, dish.title))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(getString(R.string.btn_yes)) { dialogInterface, _ ->
            viewModel.delete(dish)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(getString(R.string.btn_no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun filterDishDialog() {
        customListDialog = Dialog(requireActivity())
        val dialogBinding = DialogListBinding.inflate(layoutInflater)
        customListDialog.setContentView(dialogBinding.root)

        dialogBinding.tvDialogListTitle.text = getString(R.string.title_select_item_to_filter)
        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)

        val adapterDialog = DialogListItemAdapter(
            dishTypes,
            Constants.FILTER_SELECTION,
            OnCategoryClickListener { category, selection ->
                Log.i(TAG, "filterDishDialog: $category + $selection")
                filterSelection(category)
            })

        dialogBinding.rvDialogList.layoutManager = LinearLayoutManager(requireActivity())
        dialogBinding.rvDialogList.adapter = adapterDialog

        customListDialog.show()
    }

    private fun filterSelection(filter: String) {
        customListDialog.dismiss()
        Log.i(TAG, "filterSelection: $filter")
        if (filter == Constants.ALL_ITEMS) {
            viewModel.allDishesList.observe(viewLifecycleOwner) {
                handleRecycler(it)
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    getString(R.string.title_all_dish)
            }
        } else {
            viewModel.getDishFiltered(filter).observe(viewLifecycleOwner) {
                handleRecycler(it)
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    filter.capitalize(Locale.ROOT)
            }
        }
    }

    private fun handleRecycler(dish: List<FavDish>?) {
        dish?.let {
            if (dish.isNotEmpty()) {
                adapter.submitList(dish)
                binding.tvDishNotFound.visibility = View.GONE
                binding.rvFavDish.visibility = View.VISIBLE
            } else {
                binding.tvDishNotFound.visibility = View.VISIBLE
                binding.rvFavDish.visibility = View.GONE
            }
        }
    }
}