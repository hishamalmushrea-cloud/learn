package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.data.local.entity.LessonDetailEntity
import com.indolearn.data.local.entity.LessonEntity
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel

@Composable
fun LessonDetailScreen(
    navController: NavController,
    viewModel: LearnViewModel,
    lessonId: Int
) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    var lesson by remember { mutableStateOf<LessonEntity?>(null) }
    var lessonDetail by remember { mutableStateOf<LessonDetailEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(lessonId) {
        isLoading = true
        lesson = viewModel.getLessonById(lessonId)
        lessonDetail = viewModel.getLessonDetail(lessonId)
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (lesson == null || lessonDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لم يتم العثور على الدرس")
            Button(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("رجوع")
            }
        }
        return
    }

    var currentSpeed by remember { mutableStateOf(1.0f) }
    var showQuiz by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("المرحلة ${(lesson?.level ?: 0) + 1}", style = MaterialTheme.typography.labelMedium)
                Text(lesson?.titleAr ?: "درس", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Goal
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🎯 هدف الدرس", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(lesson?.description ?: "")
                    }
                }
            }

            // Explanation
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("📖 الشرح", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(lessonDetail?.explanation ?: "")
                    }
                }
            }

            // Example with full analysis
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🇮🇩 مثال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        val example = lessonDetail?.wordByWord?.split("\n")?.firstOrNull() ?: ""
                        Text(example, style = MaterialTheme.typography.titleLarge)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { tts.speak(example, currentSpeed) }) {
                                Text("🔊")
                            }
                            Text("استمع للمثال", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(Modifier.height(12.dp))
                        Text("🔎 كلمة بكلمة", fontWeight = FontWeight.Bold)
                        Text(lessonDetail?.wordByWord ?: "")

                        Spacer(Modifier.height(8.dp))
                        Text("🧩 تركيب الجملة", fontWeight = FontWeight.Bold)
                        Text(lessonDetail?.sentenceStructure ?: "")
                    }
                }
            }

            // Daily Usage + Formality
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🗣️ الاستخدام اليومي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(lessonDetail?.dailyUsage ?: "")
                        Spacer(Modifier.height(8.dp))
                        Text("📚 " + (lessonDetail?.formalVsCasual ?: ""))
                    }
                }
            }

            // Common Mistakes
            if (!lessonDetail?.commonMistakes.isNullOrBlank()) {
                item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("⚠️ خطأ شائع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(lessonDetail?.commonMistakes ?: "")
                        }
                    }
                }
            }

            // Quiz & Completion
            item {
                Button(
                    onClick = {
                        viewModel.markLessonDone(lessonId)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("✅ إكمال الدرس وحفظ التقدم")
                }
            }
        }
    }
}
