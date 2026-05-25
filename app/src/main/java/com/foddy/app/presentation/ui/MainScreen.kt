package com.foddy.app.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foddy.app.domain.model.Post
import com.foddy.app.presentation.viewmodel.MainViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onPostClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foddy Pro - Posts") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search posts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.fetchPosts(isRefreshing = true) },
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.isLoading && !uiState.isRefreshing) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.fetchPosts() }) {
                                Text("Retry")
                            }
                        }
                    }
                } else {
                    val filteredPosts = uiState.posts.filter {
                        it.title.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.body.contains(uiState.searchQuery, ignoreCase = true)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredPosts) { post ->
                            PostItem(post = post, onClick = { onPostClick(post.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
