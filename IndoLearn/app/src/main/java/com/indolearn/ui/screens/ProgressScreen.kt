package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.HomeViewModel

@Composable
fun ProgressScreen(navController: NavController, viewModel: HomeViewModel) {
    val progress = viewModel.progress.collectAsState().value
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("تقدمي", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("المستوى الحالي: ${progress.currentLevel}")
        Text("الدروس المكتملة: ${progress.completedLessons} / ${progress.totalLessons}")
        Text("الكلمات المتعلمة: ${progress.learnedWords} / ${progress.totalWords}")
    }
}