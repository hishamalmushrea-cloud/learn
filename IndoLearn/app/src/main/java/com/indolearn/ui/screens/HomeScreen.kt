package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val progress = viewModel.progress.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "IndoLearn",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text("تعلم الإندونيسية", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("تقدمك الحالي", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(
                    progress = { progress.completedLessons.toFloat() / progress.totalLessons },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Text("${progress.completedLessons} / ${progress.totalLessons} درس مكتمل")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("lessons") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("متابعة التعلم")
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { navController.navigate("vocabulary") }) { Text("المفردات") }
            OutlinedButton(onClick = { navController.navigate("grammar") }) { Text("القواعد") }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = { navController.navigate("progress") }) { Text("تقدمي") }
        OutlinedButton(onClick = { navController.navigate("flashcards") }) { Text("البطاقات التعليمية") }
        OutlinedButton(onClick = { navController.navigate("quiz") }) { Text("اختبار سريع") }
        OutlinedButton(onClick = { navController.navigate("search") }) { Text("البحث") }
        OutlinedButton(onClick = { navController.navigate("review") }) { Text("مراجعة اليوم") }
        OutlinedButton(onClick = { navController.navigate("favorites") }) { Text("المفضلة") }
        OutlinedButton(onClick = { navController.navigate("casual") }) { Text("اللغة اليومية") }
        OutlinedButton(onClick = { navController.navigate("casual_interactive") }) { Text("مدرب اليومية") }
        OutlinedButton(onClick = { navController.navigate("curriculum") }) { Text("المنهج الكامل") }
        OutlinedButton(onClick = { navController.navigate("settings") }) { Text("الإعدادات") }
    }
}