package com.example.openlibrary

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.openlibrary.ui.theme.OpenLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookDetailsActivity : ComponentActivity() {

    private val bookApiService = RetrofitInstance.getRetrofitInstance().create(BookService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bokKey = intent.getStringExtra(MainActivity.EXTRA_EDIT_BOOK_KEY)
        Log.d("BookDetailsActivity", "onCreate called with book: $bokKey")

        setContent {
            OpenLibraryTheme {
                BookDetailsScreen(bokKey!!)
            }
        }
    }

    @Composable
    fun BookDetailsScreen(bookKey: String) {
        var book by remember { mutableStateOf<Book?>(null) }

        LaunchedEffect(bookKey) {
            book = searchBook(bookKey)
        }

        if (book != null) {
            BookDetails(book!!)
        }
    }

    @Composable
    fun BookDetails(book: Book) {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            BookCoverImage(book)
            Text(text ="Title: " + book.title!!)
            Text(text ="Authors: " + book.authors?.get(0))
            Text(text ="Publish date: : " + (book.publishDate?.get(0)))
            Text(text ="Publisher: " + book.publisher?.get(0))
            Text(text ="Number of pages: " +  book.numberOfPages.toString())
            Text(text ="Cover: " + book.cover!!)
            Text(text ="Key: " + book.key!!)
        }
    }

    private suspend fun searchBook(key: String): Book {
        return withContext(Dispatchers.IO) {
            val bookApiCall = bookApiService.findBook(key)

            val response = bookApiCall.execute()
            val bookContainer = response.body()
            bookContainer?.books?.firstOrNull() ?: Book("No book found", "", emptyList(), null, null, emptyList(), emptyList())
        }
    }

    @Composable
    fun BookCoverImage(book: Book) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = "${MainActivity.IMAGE_BASE_URL}${book.cover}-L.jpg").apply(block = fun ImageRequest.Builder.() {
                    placeholder(R.drawable.baseline_book_24)
                    error(R.drawable.baseline_book_24)
                }).build()
        )

        Image(
            painter = painter,
            contentDescription = "Book Cover",
            modifier = Modifier
                .padding(16.dp)
                .size(512.dp)
        )
    }

}