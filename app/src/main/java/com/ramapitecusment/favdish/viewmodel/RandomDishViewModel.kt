package com.ramapitecusment.favdish.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.ramapitecusment.favdish.repository.FavDishRepository
import com.ramapitecusment.favdish.repository.database.FavDishDatabase
import com.ramapitecusment.favdish.repository.network.RandomDish
import com.ramapitecusment.favdish.utils.Constants
import kotlinx.coroutines.launch

class RandomDishViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavDishRepository(FavDishDatabase.getDatabase(application).databaseDao())

    private val _randomDishLoading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private val _randomDishLoadingError: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

     val randomDishResponse: MutableLiveData<RandomDish.Recipes> by lazy {
        MutableLiveData<RandomDish.Recipes>()
    }

    val randomDishLoading: LiveData<Boolean>
        get() = _randomDishLoading
    val randomDishLoadingError: LiveData<Boolean>
        get() = _randomDishLoadingError

    init {
        refreshDataFromInternet()
    }

    fun refreshDataFromInternet() = viewModelScope.launch {
        _randomDishLoading.value = true
        try {
            randomDishResponse.value = repository.randomDishNetworkCallCoroutine()
            _randomDishLoadingError.value = false
        } catch (e: Throwable) {
            Log.i(Constants.TAG, "getRandomDishCoroutine: ${e.printStackTrace()}")
            _randomDishLoadingError.value = true
        }
        _randomDishLoading.value = false

    }
}

class RandomDishViewModelFactory(val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RandomDishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RandomDishViewModel(app) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}