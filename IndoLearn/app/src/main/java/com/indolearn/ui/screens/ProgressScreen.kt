package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.viewmodel.HomeViewModel

/**
 * شاشة التقدم.
 *
 * قبل الإصلاح: ثلاثة أسطر نصية تعرض `completedLessons` و`learnedWords`،
 * وكلاهما **كان صفراً دائماً** لأن `updateProgress` لم يكن يُستدعى من أي مكان،
 * وكان `totalLessons = 50` و`totalWords = 300` رقمين ثابتين خاطئين.
 *
 * الآن كل الأرقام محسوبة من قاعدة البيانات عبر `recomputeProgress`،
 * مع تفصيل حالات الإتقان القادمة من محرك التكرار المتباعد.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController, viewModel: HomeViewModel) {
    val progress = viewModel.progress.collectAsState().value
    val mastery = viewModel.masteryBreakdown.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 تقدمي") },
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
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProgressCard(
                title = "الدروس المكتملة",
                done = progress.completedLessons,
                total = progress.totalLessons
            )
            ProgressCard(
                title = "الكلمات المتعلمة",
                done = progress.learnedWords,
                total = progress.totalWords,
                hint = "\"متعلمة\" = أجبت عليها صحيحاً مرة على الأقل، لا مجرد مشاهدتها."
            )

            Text(
                "حالة الإتقان",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MasteryChip("جديد", mastery.newCount, Color(0xFF90A4AE), Modifier.weight(1f))
                MasteryChip("قيد التعلم", mastery.learning, Color(0xFF42A5F5), Modifier.weight(1f))
                MasteryChip("ضعيف", mastery.weak, Color(0xFFFFA726), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MasteryChip("منسي", mastery.forgotten, Color(0xFFEF5350), Modifier.weight(1f))
                MasteryChip("متقن", mastery.mastered, Color(0xFF66BB6A), Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }

            if (mastery.total == 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "لم تراجع أي بطاقة بعد. ابدأ جلسة مراجعة لترى تقدّمك هنا.",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(title: String, done: Int, total: Int, hint: String? = null) {
    val ratio = if (total > 0) done.toFloat() / total else 0f
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$done / $total", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
            )
            if (hint != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MasteryChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
