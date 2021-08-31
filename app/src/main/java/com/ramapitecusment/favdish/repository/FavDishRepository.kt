package com.ramapitecusment.favdish.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ramapitecusment.favdish.repository.network.RandomDish
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.repository.database.FavDishDao
import com.ramapitecusment.favdish.repository.network.RandomDishService
import com.ramapitecusment.favdish.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavDishRepository(private val favDishDao: FavDishDao) {

    private val _randomDishResponse: MutableLiveData<RandomDish.Recipes> by lazy {
        MutableLiveData<RandomDish.Recipes>()
    }
    val randomDishResponse: LiveData<RandomDish.Recipes>
        get() = _randomDishResponse

    val allDishesList: Flow<List<FavDish>> = favDishDao.getAllFavDish()
    val favouriteDishesList: Flow<List<FavDish>> = favDishDao.getFavouriteDishesList()

    fun dishFiltered(value: String): Flow<List<FavDish>> = favDishDao.getDishByType(value)

    suspend fun randomDishNetworkCallCoroutine(): RandomDish.Recipes =
        RandomDishService().retrofitApi.getRandomDishes(
            Constants.API_KEY_VALUE,
            Constants.LIMIT_LICENSE_VALUE,
            Constants.TAGS_VALUE,
            Constants.NUMBER_VALUE
        )

    @WorkerThread
    suspend fun insertFavDish(dish: FavDish) {
        favDishDao.insert(dish)
    }

    @WorkerThread
    suspend fun updateFavDish(favDish: FavDish) {
        favDishDao.update(favDish)
    }

    @WorkerThread
    suspend fun deleteFavDish(favDish: FavDish) {
        favDishDao.deleteFavDish(favDish)
    }
}