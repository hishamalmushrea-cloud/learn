package com.indolearn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.data.local.entity.TrainingItemEntity
import com.indolearn.ui.theme.*
import com.indolearn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController, viewModel: LearnViewModel) {
    var questions by remember { mutableStateOf<List<TrainingItemEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var textAnswer by remember { mutableStateOf("") }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        questions = viewModel.getRandomQuizzes(10)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧪 اختبار سريع") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Scaffold
            }
            if (questions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("لا توجد أسئلة متاحة حالياً.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) { Text("العودة") }
                    }
                }
                return@Scaffold
            }

            if (showResult) {
                // RESULT SCREEN
                Spacer(Modifier.height(48.dp))
                Text(if (score >= questions.size / 2) "🎉" else "💪", fontSize = 72.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    if (score >= questions.size / 2) "أحسنت!" else "حاول مرة أخرى!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(24.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (score >= questions.size / 2)
                            MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("النتيجة", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$score / ${questions.size}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("إنهاء والعودة") }
            } else {
                // QUESTION SCREEN
                val q = questions[currentQuestionIndex]

                // Top Bar with score
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "سؤال ${currentQuestionIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            "⭐ $score",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { currentQuestionIndex.toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(24.dp))

                // Question Type Label
                val typeTitle = when (q.type) {
                    "TRANSLATE" -> "✏️ ترجم العبارة التالية"
                    "ORDER_WORDS" -> "🧩 رتب الكلمات"
                    "SITUATION" -> "💬 ماذا تقول في هذا الموقف؟"
                    else -> "🔘 اختر الإجابة الصحيحة"
                }
                Text(typeTitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(12.dp))

                // Question
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        q.question,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(24.dp))

                if (showFeedback) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                if (isCorrect) "✅ إجابة صحيحة!" else "❌ إجابة خاطئة",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("الإجابة: ${q.correctAnswer}", style = MaterialTheme.typography.bodyLarge)
                            if (q.explanation.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("💡 ${q.explanation}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showFeedback = false; textAnswer = ""
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                // حفظ النتيجة — لم تكن تُحفظ إطلاقاً قبل الإصلاح،
                                // فتضيع بمجرد مغادرة الشاشة (لا تاريخ ولا أفضل نتيجة).
                                viewModel.saveQuizResult(0, score, questions.size)
                                showResult = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("السؤال التالي ➡️") }
                } else {
                    if (q.options.isNotBlank()) {
                        val optionsList = q.options.split(",").map { it.trim() }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            optionsList.forEach { option ->
                                OutlinedButton(
                                    onClick = {
                                        isCorrect = option == q.correctAnswer
                                        if (isCorrect) score++
                                        showFeedback = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(option, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = textAnswer,
                            onValueChange = { textAnswer = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("اكتب إجابتك هنا...") },
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val cleanUser = textAnswer.trim().lowercase().replace(Regex("[^a-z0-9أ-ي ]"), "")
                                val cleanCorrect = q.correctAnswer.trim().lowercase().replace(Regex("[^a-z0-9أ-ي ]"), "")
                                isCorrect = cleanUser == cleanCorrect || (cleanCorrect.contains(cleanUser) && cleanUser.length > 3)
                                if (isCorrect) score++
                                showFeedback = true
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = textAnswer.isNotBlank()
                        ) { Text("تحقق من الإجابة ✓") }
                    }
                }
            }
        }
    }
}
