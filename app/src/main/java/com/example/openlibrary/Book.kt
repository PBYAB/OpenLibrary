package com.example.openlibrary

import com.google.gson.annotations.SerializedName

class Book(
    @SerializedName("key") val key: String?,

    @SerializedName("title") val title: String?,

    @SerializedName("author_name") val authors: List<String>?,

    @SerializedName("cover_i") val cover: String?,

    @SerializedName("number_of_pages_median") val numberOfPages: String?,

    @SerializedName("publisher_facet") val publisher: List<String>?,

    @SerializedName("publish_date") val publishDate: List<String>?,




)