package com.example.pokedex.di

import com.example.pokedex.data.remote.PokeApi
import com.example.pokedex.data.repository.PokemonRepository
import com.example.pokedex.uitl.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePokemonRepo(api: PokeApi): PokemonRepository {
        return PokemonRepository(api)
    }

    @Provides
    @Singleton
    fun providePokeApi(): PokeApi {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(Level.BASIC)
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()
            )
            .build()
            .create(PokeApi::class.java)
    }
}