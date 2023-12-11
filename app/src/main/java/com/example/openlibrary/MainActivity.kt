package com.example.openlibrary

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.openlibrary.ui.theme.OpenLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call

import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {

    private val snackbarHostState = SnackbarHostState()

    companion object{
        const val IMAGE_BASE_URL = "http://covers.openlibrary.org/b/id/"
        const val EXTRA_EDIT_BOOK_KEY = "com.example.openlibrary.EDIT_BOOK_TITLE"
    }


    private val bookApiService = RetrofitInstance.getRetrofitInstance().create(BookService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenLibraryTheme {
                var books by remember { mutableStateOf<List<Book>>(emptyList()) }

                Scaffold(
                    topBar = {
                        AppBar(
                            onSearch = { newQuery ->
                                searchBooks(newQuery) { newBooks ->
                                    books = newBooks
                                }
                            }
                        )
                    },
                    content = { padding ->
                        Column(
                            modifier = Modifier
                                .padding(padding)
                        ) {
                            BookList(books, Modifier.padding(horizontal = 16.dp))
                        }
                    },
                    bottomBar = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                )
            }
        }
    }

    private fun searchBooks(query: String, onBooksLoaded: (List<Book>) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val finalQuery = prepareQuery(query)
            val booksApiCall = bookApiService.findBooks(finalQuery)

            booksApiCall.enqueue(object : Callback<BookContainer> {
                override fun onResponse(call: Call<BookContainer>, response: Response<BookContainer>) {
                    val newBooks = response.body()?.books ?: emptyList()

                    onBooksLoaded(newBooks)
                }

                override fun onFailure(call: Call<BookContainer>, t: Throwable) {

                        Log.e("MainActivity", "Failed to fetch books", t)
                    }
            })
        }
    }

    private fun prepareQuery(query: String): String {
        val queryParts = query.split("\\s+".toRegex())
        return TextUtils.join("+", queryParts) + "&page=1&limit=10"
    }


    @Composable
    fun AppBar(modifier: Modifier = Modifier, onSearch: (String) -> Unit) {


        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)) {

             MySearchBar(modifier = modifier, onSearch = onSearch)

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MySearchBar(modifier: Modifier = Modifier, onSearch: (String) -> Unit){
        var searchQuery by remember { mutableStateOf("") }
        var isSearchVisible by remember { mutableStateOf(false) }

        val searchColors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            inputFieldColors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { newValue ->
                searchQuery = newValue
            },
            onSearch = {
                onSearch(searchQuery)
                isSearchVisible = false
            },
            active = isSearchVisible,
            onActiveChange = { active ->
                isSearchVisible = active
                if (!active) {
                    searchQuery = ""
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = true,
            placeholder = {
                Text("Search books")
            },
            leadingIcon = {
                IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            },
            colors = searchColors,
            content = {

            }

        )
    }

    @Composable
    fun BookList(book: List<Book>, modifier: Modifier = Modifier) {
        LazyColumn(modifier) {
            items(book) { book ->
                Spacer(modifier = Modifier.padding(4.dp))
                BookListItem(book)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BookListItem(book: Book) {
        var isExpanded by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .combinedClickable(
                    onClick = { isExpanded = !isExpanded },
                    onLongClick = {
                        Intent(this, BookDetailsActivity::class.java).apply {
                            putExtra(EXTRA_EDIT_BOOK_KEY, book.key)
                            startActivity(this)
                        }
                    }
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .weight(2f)
                ) {
                    book.title?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }

                    Spacer(modifier = Modifier.padding(4.dp))

                    Row(){
                        Surface(shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp) {
                            Text(
                                text = book.authors?.joinToString(", ") ?: "",
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Surface(shape = MaterialTheme.shapes.medium,
                            shadowElevation = 1.dp) {

                            Text(
                                text = book.numberOfPages.toString(),
                                modifier = Modifier.padding(4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                    }
                }

                BookCoverImage(book)

            }

        }
    }

    @Composable
    fun BookCoverImage(book: Book) {
        if (book.cover != null) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = "$IMAGE_BASE_URL${book.cover}-S.jpg")
                    .apply(block = fun ImageRequest.Builder.() {
                        placeholder(R.drawable.baseline_book_24)
                        error(R.drawable.baseline_book_24)
                    }).build()
            )

            Image(
                painter = painter,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .padding(16.dp)
                    .size(54.dp)
            )
        }
        else{
            val painter = painterResource(id = R.drawable.baseline_book_24)
            Image(
                painter = painter,
                contentDescription = "Book Cover",
                modifier = Modifier
                    .padding(16.dp)
                    .size(54.dp)
            )
            Log.d("MainActivity", "Book cover is null")
        }
    }

}