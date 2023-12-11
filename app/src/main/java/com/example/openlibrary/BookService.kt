package com.example.openlibrary

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {
    @GET("search.json")
    fun findBooks(@Query("q") query: String): Call<BookContainer>

    // New method
    @GET("search.json")
    fun findBook(@Query("q") key: String): Call<BookContainer>


}