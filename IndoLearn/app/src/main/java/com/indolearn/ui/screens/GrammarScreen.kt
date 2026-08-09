package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GrammarScreen(navController: NavController, viewModel: com.indolearn.viewmodel.MainViewModel) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("القواعد", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("ترتيب الجملة: الفاعل + الفعل + المفعول")
        Spacer(Modifier.height(16.dp))
        Text("مثال: Saya makan nasi → أنا آكل الأرز")
    }
}