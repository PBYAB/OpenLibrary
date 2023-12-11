package com.example.openlibrary

import com.google.gson.annotations.SerializedName

class BookContainer(
    @SerializedName("docs") val books: List<Book>

)