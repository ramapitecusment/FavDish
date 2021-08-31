package com.ramapitecusment.favdish.viewmodel

import androidx.lifecycle.*
import com.ramapitecusment.favdish.repository.FavDishRepository
import com.ramapitecusment.favdish.repository.database.FavDish
import kotlinx.coroutines.launch

class FavDishViewModel(private val repository: FavDishRepository) : ViewModel() {

    val allDishesList: LiveData<List<FavDish>> = repository.allDishesList.asLiveData()

    val favouriteDishesList: LiveData<List<FavDish>> = repository.favouriteDishesList.asLiveData()

    fun getDishFiltered(value: String): LiveData<List<FavDish>> =
        repository.dishFiltered(value).asLiveData()

    fun insert(dish: FavDish) = viewModelScope.launch {
        repository.insertFavDish(dish)
    }

    fun update(favDish: FavDish) = viewModelScope.launch {
        repository.updateFavDish(favDish)
    }

    fun delete(favDish: FavDish) = viewModelScope.launch {
        repository.deleteFavDish(favDish)
    }
}

class FavDishViewModelFactory(private val repository: FavDishRepository) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavDishViewModel::class.java)) {
            return FavDishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModelClass")
    }
}
