package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.LearnViewModel

@Composable
fun FavoritesScreen(navController: NavController, viewModel: LearnViewModel) {
    val words = viewModel.vocabulary.collectAsState().value.filter { it.favorite }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("المفضلة", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (words.isEmpty()) {
            Text("لا توجد كلمات مفضلة بعد")
        } else {
            LazyColumn {
                items(words) { word ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(word.indonesian, style = MaterialTheme.typography.titleMedium)
                            Text(word.arabic)
                        }
                    }
                }
            }
        }
    }
}
