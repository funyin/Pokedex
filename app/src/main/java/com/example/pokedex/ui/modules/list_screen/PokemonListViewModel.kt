package com.example.pokedex.ui.modules.list_screen

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.palette.graphics.Palette
import com.example.pokedex.data.remote.responses.Pokemon
import com.example.pokedex.data.remote.responses.PokemonList
import com.example.pokedex.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class PokemonListViewModel
@Inject constructor(private val repository: PokemonRepository) :
    ViewModel() {

    suspend fun calcMainColor(drawable: Drawable): Color {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
                Palette.from(bitmap).generate { pallete ->
                    (pallete?.vibrantSwatch ?: pallete?.dominantSwatch)?.rgb?.let {
                        coroutine.resume(Color(it))
                    } ?: coroutine.resumeWithException(Throwable("Unable to get color"))
                }
            }
        }
    }

    inner class PokemonPagingSource : PagingSource<Int, PokemonList.Result>() {

        override fun getRefreshKey(state: PagingState<Int, PokemonList.Result>): Int? {
            return state.anchorPosition
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonList.Result> {
            return try {
                val firstPage = 0
                val page = params.key ?: firstPage
                val response = repository.getPokemons(
                    offset = params.loadSize * page,
                    limit = params.loadSize,
                )

                val results = response.data?.results ?: emptyList()
                LoadResult.Page(
                    data = results,
                    prevKey = if (page == firstPage) null else page - 1,
                    nextKey = if (results.size < params.loadSize) null else page + 1
                )
            } catch (e: Exception) {
                return LoadResult.Error(e)
            }
        }
    }
}