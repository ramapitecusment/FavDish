package com.ramapitecusment.favdish.repository.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavDishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favDish: FavDish)

    @Update
    suspend fun update(favDish: FavDish)

    @Delete
    suspend fun deleteFavDish(favDish: FavDish)

    @Query("SELECT * FROM fav_dishes_table WHERE id = :id")
    suspend fun getFavDishById(id: Long): FavDish

    @Query("SELECT * FROM fav_dishes_table WHERE favourite_dish = 1")
    fun getFavouriteDishesList(): Flow<List<FavDish>>

    @Query("SELECT * FROM fav_dishes_table WHERE type = :filter")
    fun getDishByType(filter: String): Flow<List<FavDish>>

    @Query("SELECT * FROM fav_dishes_table ORDER BY id")
    fun getAllFavDish(): Flow<List<FavDish>>

    @Query("DELETE FROM fav_dishes_table")
    suspend fun deleteAllFavDish()
}