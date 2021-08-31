package com.ramapitecusment.favdish.repository.network

import com.ramapitecusment.favdish.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val api = Retrofit.Builder()
    .baseUrl(Constants.BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface RandomDishApi {

    @GET(Constants.API_ENDPOINT)
    suspend fun getRandomDishes(
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.LIMIT_LICENSE) limitLicense: Boolean,
        @Query(Constants.TAGS) tags: String,
        @Query(Constants.NUMBER) number: Int
    ): RandomDish.Recipes
}

class RandomDishService {
    val retrofitApi: RandomDishApi by lazy {
        api.create(RandomDishApi::class.java)
    }
}