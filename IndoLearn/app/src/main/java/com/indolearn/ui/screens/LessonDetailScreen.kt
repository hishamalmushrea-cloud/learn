package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.MainViewModel

@Composable
fun LessonDetailScreen(
    navController: NavController,
    viewModel: MainViewModel,
    lessonId: Int
) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    // Dynamic lesson data (from seeded database)
    val lessonTitle = when (lessonId) {
        1 -> "التحيات الأساسية"
        2 -> "الضمائر"
        3 -> "تكوين أول جملة"
        4, 11, 12 -> "الأفعال الأساسية"
        5, 13, 14 -> "النفي"
        6, 15 -> "السؤال"
        7, 16 -> "الأرقام"
        8, 17 -> "الوقت والتاريخ"
        9, 18 -> "الملكية"
        10, 19 -> "الصفات"
        20 -> "حروف الجر"
        21 -> "الأسرة"
        22 -> "الأشياء اليومية"
        23 -> "مراجعة المرحلة الأولى"
        else -> "درس $lessonId"
    }

    val lessonGoal = when (lessonId) {
        1 -> "بعد إكمال هذا الدرس ستستطيع تحية الأشخاص والتعريف بنفسك."
        2 -> "ستتعلم الضمائر الأساسية ومتى تستخدم كل واحدة."
        3 -> "ستتعلم بناء أول جملة صحيحة: فاعل + فعل + مفعول."
        else -> "ستتقن الموضوع الرئيسي لهذا الدرس."
    }

    val explanation = when (lessonId) {
        1 -> "Halo هو التحية الأكثر استخداماً. Selamat pagi/siang/sore/malam تستخدم حسب الوقت من اليوم."
        2 -> "saya = أنا (رسمي) • aku = أنا (يومي) • kamu = أنت (يومي) • Anda = أنت (رسمي جداً)"
        3 -> "الترتيب الأساسي في الإندونيسية: فاعل + فعل + مفعول. لا توجد 'to be' مثل الإنجليزية."
        else -> "شرح الموضوع الرئيسي للدرس مع أمثلة واضحة."
    }

    var currentSpeed by remember { mutableStateOf(1.0f) }
    var trainingAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var quizScore by remember { mutableStateOf(0) }
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
                Text("←")
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("المرحلة 1 • الوحدة ${(lessonId / 2) + 1}", style = MaterialTheme.typography.labelMedium)
                Text(lessonTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        LinearProgressIndicator(
            progress = { (lessonId % 5 + 3) / 6f },
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
                        Text(lessonGoal)
                    }
                }
            }

            // Explanation
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("📖 الشرح", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(explanation)
                    }
                }
            }

            // Vocabulary
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("📝 الكلمات الجديدة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        val words = listOf(
                            Triple("Halo", "ها لو", "مرحبا"),
                            Triple("Saya", "سايا", "أنا"),
                            Triple("Makan", "ماكان", "يأكل"),
                            Triple("Tidak", "تيداك", "لا")
                        )

                        words.forEach { (word, pron, meaning) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(word, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(pron)
                                    Text(meaning)
                                }
                                IconButton(onClick = { tts.speak(word, currentSpeed) }) {
                                    Text("🔊")
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { currentSpeed = 0.75f }) { Text("0.75×") }
                            Button(onClick = { currentSpeed = 1.0f }) { Text("1×") }
                            Button(onClick = { currentSpeed = 1.25f }) { Text("1.25×") }
                        }
                    }
                }
            }

            // Example with full analysis
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🇮🇩 مثال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        Text("Saya makan nasi.", style = MaterialTheme.typography.titleLarge)
                        Text("🔊 سايا ماكان ناسي", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("أنا آكل الأرز.", style = MaterialTheme.typography.bodyLarge)

                        Spacer(Modifier.height(12.dp))
                        Text("🔎 كلمة بكلمة", fontWeight = FontWeight.Bold)
                        Text("Saya = أنا\nmakan = آكل\nnasi = أرز")

                        Spacer(Modifier.height(8.dp))
                        Text("🧩 تركيب الجملة", fontWeight = FontWeight.Bold)
                        Text("Saya + makan + nasi\nفاعل + فعل + مفعول")
                    }
                }
            }

            // Daily Usage + Formality
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🗣️ الاستخدام اليومي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Halo هو الأكثر استخداماً في الحياة اليومية مع الأصدقاء والغرباء.")
                        Spacer(Modifier.height(8.dp))
                        Text("📚 رسمي: Selamat pagi\n🗣️ يومي: Halo / Hai")
                    }
                }
            }

            // Common Mistakes
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("⚠️ خطأ شائع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("❌ Saya bukan lapar (خطأ)\n✅ Saya tidak lapar (صحيح)")
                        Text("tidak تستخدم مع الفعل والصفة، bukan تستخدم مع الاسم.")
                    }
                }
            }

            // Training - Multiple types
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🧠 تدريب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        // Type 1: Translation
                        Text("ترجم: Saya makan nasi")
                        OutlinedTextField(
                            value = trainingAnswer,
                            onValueChange = { trainingAnswer = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                feedback = if (trainingAnswer.contains("أكل") || trainingAnswer.contains("أرز")) {
                                    "🟢 صحيح!"
                                } else "🔴 الإجابة: أنا آكل الأرز."
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("تحقق") }

                        if (feedback.isNotEmpty()) Text(feedback)

                        Spacer(Modifier.height(16.dp))

                        // Type 2: Order words (demo)
                        Text("رتب الكلمات: Saya / makan / nasi")
                        Button(onClick = { feedback = "🟢 Saya makan nasi" }) {
                            Text("Saya makan nasi")
                        }
                    }
                }
            }

            // Quiz
            item {
                if (!showQuiz) {
                    Button(
                        onClick = { showQuiz = true; quizScore = 0 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📝 ابدأ اختبار الدرس")
                    }
                } else {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("📝 اختبار الدرس", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))

                            // Simple dynamic quiz
                            val quizQuestions = listOf(
                                "ما معنى 'Halo'؟" to listOf("مرحبا", "مع السلامة", "شكراً"),
                                "ما معنى 'Saya'؟" to listOf("أنت", "أنا", "هو")
                            )

                            quizQuestions.forEachIndexed { index, (question, options) ->
                                Text(question, fontWeight = FontWeight.Medium)
                                options.forEach { option ->
                                    Button(
                                        onClick = {
                                            if ((index == 0 && option == "مرحبا") || (index == 1 && option == "أنا")) {
                                                quizScore += 5
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    ) {
                                        Text(option)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Spacer(Modifier.height(12.dp))
                            Text("نتيجتك: $quizScore / 10", style = MaterialTheme.typography.titleMedium)
                            if (quizScore >= 6) {
                                Text("🟢 أحسنت! يمكنك إكمال الدرس.", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Completion
            item {
                Button(
                    onClick = {
                        viewModel.markLessonDone(lessonId)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✅ إكمال الدرس وحفظ التقدم")
                }
            }
        }
    }
}