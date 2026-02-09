package com.curso.android.module2.stream.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.curso.android.module2.stream.data.model.Song

@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit, // Requerimiento: Event hoisting
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(140.dp)) {
            // Reutilizamos SongCoverMock existente
            SongCoverMock(colorSeed = song.colorSeed, size = 140.dp)

            // Requerimiento: Heart icon visible
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (song.isFavorite) Color.Red else Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
        Text(text = song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
    }
}