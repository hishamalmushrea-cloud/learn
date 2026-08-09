package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DialogueScreen(navController: NavController) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("المحادثات", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("التعارف", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("A: Halo, nama saya Ahmad.")
                Text("B: Halo, saya Siti. Senang bertemu denganmu.")
                Spacer(Modifier.height(12.dp))
                Text("الترجمة:", style = MaterialTheme.typography.labelMedium)
                Text("أ: مرحبا، اسمي أحمد.")
                Text("ب: مرحبا، أنا سيتي. سعيدة بلقائك.")
            }
        }
    }
}