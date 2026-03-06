package com.scripturewidgets.presentation.screens.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.scripturewidgets.domain.model.BibleVerse
import com.scripturewidgets.domain.model.VerseCategory
import com.scripturewidgets.presentation.viewmodel.BrowseViewModel

@Composable
fun BrowseScreen(viewModel: BrowseViewModel = hiltViewModel()) {
    val verses:      List<BibleVerse> by viewModel.verses.collectAsState()
    val searchQuery: String           by viewModel.searchQuery.collectAsState()
    val selectedCat: VerseCategory    by viewModel.selectedCategory.collectAsState()

    // Local captures to avoid smart-cast issues
    val currentVerses   = verses
    val currentQuery    = searchQuery
    val currentCategory = selectedCat

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value         = currentQuery,
            onValueChange = viewModel::updateSearch,
            modifier      = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder   = { Text("Search verses, books, topics…", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon   = { Icon(Icons.Default.Search, null) },
            trailingIcon  = {
                if (currentQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateSearch("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            textStyle     = MaterialTheme.typography.bodyMedium,
            singleLine    = true,
            shape         = RoundedCornerShape(24.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VerseCategory.entries) { cat ->
                FilterChip(
                    selected    = currentCategory == cat,
                    onClick     = { viewModel.selectCategory(cat) },
                    label       = { Text(cat.displayName) },
                    leadingIcon = if (currentCategory == cat) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(cat.colorHex).copy(alpha = 0.85f),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (currentVerses.size == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SearchOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                    Text("No verses found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentVerses) { verse ->
                    val isFav: Boolean by viewModel.isFavorite(verse.id).collectAsState(initial = false)
                    VerseListItem(
                        verse           = verse,
                        isFavorite      = isFav,
                        onToggleFavorite = { viewModel.toggleFavorite(verse) },
                        onShare         = { viewModel.shareVerse(verse) }
                    )
                }
            }
        }
    }
}

@Composable
fun VerseListItem(verse: BibleVerse, isFavorite: Boolean, onToggleFavorite: () -> Unit, onShare: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color(verse.category.colorHex).copy(alpha = 0.15f)) {
                    Text(verse.category.displayName, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(verse.category.colorHex))
                }
                Spacer(Modifier.weight(1f))
                Text(verse.translation.abbreviation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            Text(verse.text, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, maxLines = 4)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(verse.reference, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
