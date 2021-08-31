package com.ramapitecusment.favdish.application

import android.app.Application
import com.ramapitecusment.favdish.repository.FavDishRepository
import com.ramapitecusment.favdish.repository.database.FavDishDatabase

class FavDishApplication : Application() {
    private val database by lazy { FavDishDatabase.getDatabase(this@FavDishApplication) }

    val repository by lazy { FavDishRepository(database.databaseDao()) }
}