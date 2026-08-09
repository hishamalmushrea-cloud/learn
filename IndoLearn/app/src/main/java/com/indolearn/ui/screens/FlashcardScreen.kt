package com.indolearn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun FlashcardScreen(navController: NavController, viewModel: MainViewModel) {
    var front by remember { mutableStateOf(true) }
    val words = viewModel.vocabulary.collectAsState().value.take(5) // demo

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (words.isNotEmpty()) {
            val current = words[0]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable { front = !front }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (front) current.indonesian else current.arabic,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { /* Mark easy */ }) { Text("سهلة") }
                Button(onClick = { /* Mark hard */ }) { Text("صعبة") }
            }
        } else {
            Text("لا توجد بطاقات بعد")
        }
    }
}