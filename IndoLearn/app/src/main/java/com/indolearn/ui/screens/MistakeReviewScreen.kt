package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.data.local.entity.TrainingItemEntity
import com.indolearn.domain.quiz.AnswerEvaluator
import com.indolearn.viewmodel.LearnViewModel

/** جلسة علاج قصيرة: بحد أقصى 5 أسئلة مستحقة، وإعادة واحدة داخل الجلسة عند الخطأ. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeReviewScreen(navController: NavController, viewModel: LearnViewModel) {
    var questions by remember { mutableStateOf<List<TrainingItemEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var index by remember { mutableStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Boolean?>(null) }
    var correctCount by remember { mutableStateOf(0) }
    val retried = remember { mutableStateListOf<Int>() }

    LaunchedEffect(Unit) {
        questions = viewModel.getDueMistakeQuestions(5)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 نقاط تحتاج تثبيت") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                questions.isEmpty() -> EmptyMistakeReview(navController)
                index >= questions.size -> Column(
                    Modifier.align(Alignment.Center).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("✅", style = MaterialTheme.typography.displayLarge)
                    Text("أتممت جلسة التثبيت", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("أجبت صحيحاً $correctCount مرة. ستعود النقاط في موعدها التالي حسب أدائك.", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("العودة إلى خطة اليوم") }
                }
                else -> {
                    val item = questions[index]
                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${index + 1} / ${questions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(
                            progress = { index.toFloat() / questions.size },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                        AssistChip(onClick = {}, label = { Text("🔁 مراجعة محاولة سابقة · ${item.category}") })
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                item.question,
                                Modifier.fillMaxWidth().padding(22.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(20.dp))

                        if (feedback == null) {
                            if (item.options.isNotBlank()) {
                                item.options.split(',').map { it.trim() }.forEach { option ->
                                    OutlinedButton(
                                        onClick = {
                                            answer = option
                                            val correct = option == item.correctAnswer
                                            feedback = correct
                                            if (correct) correctCount++
                                            viewModel.recordQuestionAttempt(item, option, correct)
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) { Text(option) }
                                }
                            } else {
                                OutlinedTextField(
                                    value = answer,
                                    onValueChange = { answer = it },
                                    label = { Text("اكتب الإجابة من الذاكرة") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    enabled = answer.isNotBlank(),
                                    onClick = {
                                        val correct = AnswerEvaluator.isCorrect(answer, item.correctAnswer)
                                        feedback = correct
                                        if (correct) correctCount++
                                        viewModel.recordQuestionAttempt(item, answer.trim(), correct)
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                ) { Text("تحقق") }
                            }
                        } else {
                            val correct = feedback == true
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (correct) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(if (correct) "✅ ثبتت الإجابة" else "لنصححها الآن", fontWeight = FontWeight.Bold)
                                    if (!correct) Text("إجابتك: $answer")
                                    Text("الصحيح: ${item.correctAnswer}", fontWeight = FontWeight.Bold)
                                    if (item.explanation.isNotBlank()) Text("💡 ${item.explanation}")
                                }
                            }
                            Button(
                                onClick = {
                                    if (!correct && item.id !in retried) {
                                        retried += item.id
                                        questions = questions + item
                                    }
                                    feedback = null
                                    answer = ""
                                    index++
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                            ) {
                                Text(
                                    when {
                                        !correct && item.id !in retried -> "صححتها — أعدها بعد قليل"
                                        index == questions.lastIndex -> "إنهاء"
                                        else -> "التالي"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMistakeReview(navController: NavController) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌱", style = MaterialTheme.typography.displayLarge)
        Text("لا توجد نقاط مستحقة اليوم", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "لن نكرر الأسئلة أكثر من اللازم. سيختار المدرب النقاط في موعدها التالي.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = { navController.popBackStack() }) { Text("العودة") }
    }
}
