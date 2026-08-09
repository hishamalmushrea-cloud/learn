package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun ReviewScreen(navController: NavController, viewModel: MainViewModel) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("مراجعة اليوم", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        Text("5 كلمات + 2 قواعد + 1 محادثة")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.navigate("flashcards") }) {
            Text("ابدأ المراجعة")
        }
    }
}