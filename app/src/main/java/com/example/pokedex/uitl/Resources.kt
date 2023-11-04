package com.example.pokedex.uitl

sealed class Resources<T>(
    open val data: T? = null,
    open val message: String? = null
) {
    data class Success<T>(override val data: T) : Resources<T>(data, null)
    data class Error<T>(override val message: String?) : Resources<T>(null, null)
    object Loading : Resources<Nothing>(null, null)
}