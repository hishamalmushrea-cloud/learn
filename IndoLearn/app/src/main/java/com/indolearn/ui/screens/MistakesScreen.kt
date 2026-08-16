package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.LearnViewModel
import java.text.DateFormat
import java.util.Date

/** سجل أخطاء قابل للتعلّم: يعرض ما أجاب به المستخدم ولماذا كانت الإجابة الصحيحة. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakesScreen(navController: NavController, viewModel: LearnViewModel) {
    val mistakes = viewModel.mistakes.collectAsState().value
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 سجل الأخطاء والتصحيح (${mistakes.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    if (mistakes.isNotEmpty()) {
                        TextButton(onClick = { confirmClear = true }) { Text("مسح") }
                    }
                }
            )
        }
    ) { padding ->
        if (mistakes.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("✅ لا توجد أخطاء محفوظة", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(
                    "أجب عن أسئلة الاختبار، وستظهر الإجابات الخاطئة هنا مع التصحيح والشرح.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "لا تحفظ الجواب فقط: اقرأ سبب الخطأ، ثم قل الإجابة الصحيحة من الذاكرة قبل الانتقال.",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { navController.navigate("mistake_review") },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("🎯 ابدأ نقاط اليوم المستحقة") }
                        }
                    }
                }
                items(mistakes, key = { it.id }) { attempt ->
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                AssistChip(onClick = {}, label = { Text(attempt.category) })
                                Text(
                                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                        .format(Date(attempt.attemptedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(attempt.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Text("إجابتك: ${attempt.userAnswer}", color = MaterialTheme.colorScheme.error)
                            Text("الصحيح: ${attempt.correctAnswer}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (attempt.explanation.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text("💡 ${attempt.explanation}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("مسح سجل الأخطاء؟") },
            text = { Text("سيُحذف سجل محاولات اللغة الحالية فقط. لن يتأثر تقدم الدروس أو البطاقات.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearQuestionHistory()
                    confirmClear = false
                }) { Text("مسح") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("إلغاء") } }
        )
    }
}
