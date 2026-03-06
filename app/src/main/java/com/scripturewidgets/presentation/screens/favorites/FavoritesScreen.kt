package com.scripturewidgets.presentation.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scripturewidgets.domain.model.BibleVerse
import com.scripturewidgets.presentation.screens.browse.VerseListItem
import com.scripturewidgets.presentation.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = hiltViewModel()) {
    val favorites by viewModel.favorites.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Favorites") })

        val favList = favorites
        if (favList.size == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.HeartBroken, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                    Text("No favorites yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    Text("Tap the heart on any verse to save it here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(favList) { verse: BibleVerse ->
                    VerseListItem(
                        verse = verse,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.removeFavorite(verse.id) },
                        onShare = { viewModel.shareVerse(verse) }
                    )
                }
            }
        }
    }
}