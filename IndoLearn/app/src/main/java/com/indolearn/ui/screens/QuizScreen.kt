package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun QuizScreen(navController: NavController, viewModel: MainViewModel) {
    var currentQuestion by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    val words = viewModel.vocabulary.collectAsState().value

    if (words.isEmpty()) {
        Text("جاري تحميل الأسئلة...")
        return
    }

    val questions = listOf(
        Triple("ما معنى 'makan'؟", listOf("يأكل", "يشرب", "ينام"), 0),
        Triple("ما معنى 'terima kasih'؟", listOf("مرحبا", "شكراً", "مع السلامة"), 1),
        Triple("ما معنى 'saya'؟", listOf("أنت", "هو", "أنا"), 2)
    )

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showResult) {
            Text("النتيجة: $score / ${questions.size}", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { navController.popBackStack() }) { Text("العودة") }
        } else {
            val q = questions[currentQuestion]
            Text("سؤال ${currentQuestion + 1} / ${questions.size}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))
            Text(q.first, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(32.dp))

            q.second.forEachIndexed { index, option ->
                Button(
                    onClick = {
                        if (index == q.third) score++
                        if (currentQuestion < questions.size - 1) {
                            currentQuestion++
                        } else {
                            showResult = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Text(option)
                }
            }
        }
    }
}