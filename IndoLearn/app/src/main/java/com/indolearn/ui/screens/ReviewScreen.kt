package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.domain.coach.CoachTask
import com.indolearn.domain.coach.TaskType
import com.indolearn.viewmodel.LearnViewModel

/**
 * شاشة "مراجعة اليوم" — مبنية على بيانات المستخدم الفعلية.
 *
 * قبل الإصلاح كانت هذه الشاشة 27 سطراً تعرض نصاً ثابتاً:
 *   "5 كلمات + 2 قواعد + 1 محادثة"
 * وهو رقم مختلق غير مرتبط بأي بيانات على الإطلاق.
 *
 * الآن كل رقم معروض مشتق من `review_states` عبر [com.indolearn.domain.coach.DailyCoach].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController, viewModel: LearnViewModel) {
    val session = viewModel.session.collectAsState().value
    val sessionFailed = viewModel.sessionFailed.collectAsState().value

    LaunchedEffect(Unit) { viewModel.refreshDailySession() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔄 جلسة اليوم") },
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
        if (session == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (sessionFailed) {
                    // فشل بناء الجلسة: نقول ذلك صراحةً ونتيح إعادة المحاولة،
                    // بدل ترك دوّامة تدور إلى الأبد بلا تفسير.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("⚠️", fontSize = 44.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "تعذّر تجهيز جلسة اليوم",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "حدث خطأ أثناء قراءة بيانات مراجعتك.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshDailySession() }) {
                            Text("إعادة المحاولة")
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // رسالة المرشد
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                        Text("🧭", fontSize = 28.sp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "مرشد التعلم",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                session.guidance,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // إحصاءات حقيقية
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip("مستحق", session.dueCount, Modifier.weight(1f))
                    StatChip("ضعيف", session.weakCount, Modifier.weight(1f))
                    StatChip("منسي", session.forgottenCount, Modifier.weight(1f))
                    StatChip("متقن", session.masteredCount, Modifier.weight(1f))
                }
            }

            if (session.isEmpty) {
                item {
                    Card(shape = RoundedCornerShape(18.dp)) {
                        Column(
                            Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 48.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "لا توجد مراجعات مستحقة الآن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "التكرار المتباعد يعمل أثناء راحتك. عُد غداً.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(18.dp))
                            OutlinedButton(onClick = { navController.navigate("flashcards") }) {
                                Text("تعلّم كلمات جديدة")
                            }
                        }
                    }
                }
            } else {
                items(session.tasks) { task -> TaskCard(task, navController) }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskCard(task: CoachTask, navController: NavController) {
    val (emoji, title, route) = when (task.type) {
        TaskType.RECOVER_FORGOTTEN -> Triple("🔴", "استرجاع ما نسيته", "flashcards")
        TaskType.DRILL_WEAK -> Triple("🟠", "تقوية نقاط الضعف", "flashcards")
        TaskType.REVIEW_DUE -> Triple("🔵", "مراجعة مستحقة", "flashcards")
        TaskType.SCENARIO_PRACTICE -> Triple("🎭", "تدريب على موقف واقعي", "casual_interactive")
        TaskType.NEW_LESSON -> Triple("🟢", "درس جديد", "lesson/${task.itemIds.firstOrNull() ?: 1}")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = { navController.navigate(route) }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (task.type != TaskType.NEW_LESSON) {
                        Spacer(Modifier.width(8.dp))
                        Badge { Text("${task.size}") }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    task.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}
