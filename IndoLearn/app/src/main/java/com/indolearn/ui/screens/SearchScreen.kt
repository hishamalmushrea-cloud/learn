package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun SearchScreen(navController: NavController, viewModel: MainViewModel) {
    var query by remember { mutableStateOf("") }
    val words = viewModel.vocabulary.collectAsState().value

    val filtered = words.filter {
        it.indonesian.contains(query, true) ||
        it.arabic.contains(query, true) ||
        it.pronunciation.contains(query, true)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("ابحث عن كلمة...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(filtered) { word ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(word.indonesian, style = MaterialTheme.typography.titleMedium)
                        Text("${word.pronunciation} — ${word.arabic}")
                    }
                }
            }
        }
    }
}