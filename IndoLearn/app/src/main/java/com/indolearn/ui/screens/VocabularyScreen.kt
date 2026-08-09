package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.MainViewModel

@Composable
fun VocabularyScreen(navController: NavController, viewModel: MainViewModel) {
    val words = viewModel.vocabulary.collectAsState().value
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("المفردات", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(words) { word ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row {
                            Text(word.indonesian, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            IconButton(onClick = { tts.speak(word.indonesian) }) {
                                Text("🔊")
                            }
                            IconButton(onClick = {
                                viewModel.toggleFavorite(word.id, !word.favorite)
                            }) {
                                Icon(
                                    if (word.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null
                                )
                            }
                        }
                        Text(word.pronunciation)
                        Text(word.arabic, style = MaterialTheme.typography.bodyLarge)
                        Text(word.exampleTranslation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}