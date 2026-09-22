package com.indolearn.ui.screens

import android.net.Uri
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
import com.indolearn.data.local.entity.A0_EXIT_QUIZ_LESSON_ID
import com.indolearn.data.local.entity.LessonDetailEntity
import com.indolearn.data.local.entity.LessonEntity
import com.indolearn.data.repository.A0PronunciationContent
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
    // إطلاق محرك النطق عند مغادرة الشاشة.
    // بدونه يبقى TextToSpeech حياً بعد إغلاق الشاشة (تسريب موارد)،
    // ويتراكم مع كل زيارة للشاشة.
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }
    val currentLanguage = viewModel.currentLanguage.collectAsState().value

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
                Text(
                    when (lesson?.level) {
                        A0PronunciationContent.LEVEL -> "المرحلة A0"
                        0 -> "المرحلة A1"
                        1 -> "المرحلة A2"
                        else -> "المرحلة ${(lesson?.level ?: 0) + 1}"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
                Text(lesson?.titleAr ?: "درس", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }

        // كان هنا شريط تقدم ثابت عند 0.5f — رقم كاذب لا يعكس شيئاً،
        // يوهم المتعلم أنه أنجز نصف الدرس فور فتحه.
        // الحالة الحقيقية الوحيدة المتاحة هنا هي: أُكمل الدرس أم لا.
        if (lesson?.completed == true) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✅ أكملت هذا الدرس", style = MaterialTheme.typography.labelLarge)
            }
        }

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
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val context = androidx.compose.ui.platform.LocalContext.current
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📖 الشرح", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(lessonDetail?.explanation ?: ""))
                                android.widget.Toast.makeText(context, "تم نسخ الشرح! 📋", android.widget.Toast.LENGTH_SHORT).show()
                            }) {
                                Text("📋")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(lessonDetail?.explanation ?: "")
                    }
                }
            }

            item {
                var recall by remember { mutableStateOf("") }
                var revealRecall by remember { mutableStateOf(false) }
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("🇮🇩 أمثلة وترجمة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        val exampleLines = lessonDetail?.wordByWord
                            ?.split("\n")
                            ?.map { it.trim() }
                            ?.filter { it.isNotEmpty() }
                            ?: emptyList()
                        exampleLines.forEach { line ->
                            val idn = line.substringBefore("|||").trim()
                            val ar = line.substringAfter("|||", "").trim()
                            Text(idn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (ar.isNotEmpty()) Text(ar, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    tts.speak(idn, currentSpeed, langCode = currentLanguage)
                                }) { Text("🔊") }
                                Text("استمع", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("🧠 استرجاع نشط", fontWeight = FontWeight.Bold)
                        Text(lessonDetail?.sentenceStructure ?: "")
                        OutlinedTextField(
                            value = recall,
                            onValueChange = { recall = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("اكتب ما تتذكره ثم اكشف") }
                        )
                        TextButton(onClick = { revealRecall = true }) { Text("كشف الدليل") }
                        if (revealRecall) {
                            Text(lessonDetail?.sentenceStructure ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("👂 تدريب تمييز", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(lessonDetail?.dailyUsage ?: "")
                        val listenWords = lessonDetail?.formalVsCasual
                            ?.removePrefix("اسمع:")
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.filter { it.isNotEmpty() }
                            ?: emptyList()
                        listenWords.forEach { word ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { tts.speak(word, currentSpeed, langCode = currentLanguage) }) {
                                    Text("🔊")
                                }
                                Text(word)
                            }
                        }
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

            item {
                val drillCategory = when (lessonId) {
                    401 -> "A0 كتابة"
                    402 -> "A0 صوائت"
                    403 -> "A0 سواكن"
                    404 -> "A0 c j g"
                    405 -> "A0 ng ny"
                    406 -> "A0 sy kh"
                    407 -> "A0 نبر"
                    408 -> "A0 قراءة"
                    else -> null
                }
                if (drillCategory != null) {
                    OutlinedButton(
                        onClick = { navController.navigate("quiz/${Uri.encode(drillCategory)}/60/-1") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("تدريب تمييز لهذا الدرس") }
                    Spacer(Modifier.height(8.dp))
                }
                if (lessonId == 409) {
                    Button(
                        onClick = {
                            navController.navigate(
                                "quiz/${Uri.encode(A0PronunciationContent.EXIT_CATEGORY)}/" +
                                    "${A0PronunciationContent.PASS_PERCENT}/$A0_EXIT_QUIZ_LESSON_ID"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("اختبار انتقال A0 — نجاح 80٪") }
                    Spacer(Modifier.height(8.dp))
                }
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
