package com.curso.android.module2.stream.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.curso.android.module2.stream.ui.components.SongCard
import com.curso.android.module2.stream.ui.viewmodel.HomeUiState
import com.curso.android.module2.stream.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HighlightsScreen(
    // Reutilizamos HomeViewModel para no crear archivos extra,
    // tenemos acceso al repo y al toggleFavorite.
    viewModel: HomeViewModel = koinViewModel(),
    onSongClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Highlights", style = MaterialTheme.typography.headlineMedium)

        when (val state = uiState) {
            is HomeUiState.Success -> {
                // Filtramos las canciones favoritas (UI logico)
                val favoriteSongs = state.categories.flatMap { it.songs }.filter { it.isFavorite }

                if (favoriteSongs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No favorites yet")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp)
                    ) {
                        items(favoriteSongs) { song ->
                            SongCard(
                                song = song,
                                onClick = { onSongClick(song.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(song.id) }
                            )
                        }
                    }
                }
            }
            else -> { /* Loading or Error */ }
        }
    }
}