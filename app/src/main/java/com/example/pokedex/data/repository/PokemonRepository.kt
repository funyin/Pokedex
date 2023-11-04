package com.example.pokedex.data.repository

import com.example.pokedex.data.remote.PokeApi
import com.example.pokedex.data.remote.responses.Pokemon
import com.example.pokedex.data.remote.responses.PokemonList
import com.example.pokedex.uitl.Resources
import dagger.hilt.android.scopes.ActivityScoped

import java.lang.Exception
import javax.inject.Inject

@ActivityScoped
class PokemonRepository @Inject constructor(private val api: PokeApi) {

    suspend fun getPokemons(limit: Int, offset: Int): Resources<PokemonList> {
        val response: Resources<PokemonList> = try {
            Resources.Success(api.getPokemonList(limit, offset))
        } catch (e: Exception) {
            Resources.Error(message = "An unknown error occurred")
        }
        return response
    }

    suspend fun getPokemonInfo(name: String): Resources<Pokemon> {
        val response: Resources<Pokemon> = try {
            Resources.Success(api.getPokemon(name))
        } catch (e: Exception) {
            Resources.Error(message = "An unknown error occurred")
        }
        return response
    }
}