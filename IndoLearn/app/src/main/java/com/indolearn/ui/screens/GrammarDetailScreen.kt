package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GrammarDetailScreen(navController: NavController) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("ترتيب الجملة", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("القاعدة: الفاعل + الفعل + المفعول", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))
        Text("مثال:")
        Text("Saya makan nasi.", style = MaterialTheme.typography.bodyLarge)
        Text("النطق: سايا ماكان ناسي")
        Text("المعنى: أنا آكل الأرز.")
    }
}