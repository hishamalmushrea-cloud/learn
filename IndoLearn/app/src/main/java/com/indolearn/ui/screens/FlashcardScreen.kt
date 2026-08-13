package com.indolearn.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.domain.srs.Grade
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel

/**
 * بطاقات المراجعة — مدعومة بمحرك التكرار المتباعد.
 *
 * ما تغيّر ولماذا:
 *
 * 1. **الطابور**: كانت الشاشة تعرض `viewModel.vocabulary` كاملة (كل الكلمات، دائماً،
 *    بنفس الترتيب). الآن تعرض `reviewQueue` = العناصر **المستحقة فعلاً** حسب SM-2،
 *    وإن لم توجد مستحقات تُقدَّم عناصر جديدة لم تُدرس.
 *
 * 2. **الاسترجاع النشط**: كان الانتقال بزر "التالي" فقط — أي أن المستخدم
 *    يشاهد ولا يُختبر، ولا يُسجَّل أداؤه في أي مكان. الآن بعد قلب البطاقة
 *    يجب أن يقيّم استرجاعه بأربع درجات، وكل تقييم يُحدِّث الجدولة فعلياً.
 *
 * 3. **اتجاه البطاقة**: كان الوجه الأمامي دائماً الكلمة الأجنبية.
 *    الآن يمكن عكس الاتجاه (عربي ⇦ أجنبي) وهو استرجاع أصعب وأكثر فائدة للإنتاج.
 *
 * 4. **اللغة**: كان العَلَم مثبتاً على 🇮🇩 حتى عند تعلم التركية.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(navController: NavController, viewModel: LearnViewModel) {
    val queue = viewModel.reviewQueue.collectAsState().value
    val lang = viewModel.currentLanguage.collectAsState().value
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    var index by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var reverseMode by remember { mutableStateOf(false) }
    var graded by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.refreshDailySession() }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "flip"
    )

    val flag = if (lang == "TR") "🇹🇷" else "🇮🇩"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🃏 بطاقات المراجعة") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    TextButton(onClick = { reverseMode = !reverseMode; isFlipped = false }) {
                        Text(
                            if (reverseMode) "عربي ← $flag" else "$flag ← عربي",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                queue.isEmpty() && graded == 0 -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌙", fontSize = 56.sp)
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "لا توجد بطاقات مستحقة الآن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "هذا جيد — التكرار المتباعد يمنع المراجعة الزائدة.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                index >= queue.size -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎉", fontSize = 64.sp)
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "أنهيت الجلسة",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "راجعت $graded عنصراً. أُعيدت جدولتها حسب أدائك.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(22.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("العودة")
                            }
                        }
                    }
                }

                else -> {
                    val card = queue[index]
                    val front = if (reverseMode) card.arabic else card.indonesian
                    val back = if (reverseMode) card.indonesian else card.arabic

                    Text(
                        "بطاقة ${index + 1} من ${queue.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (index + 1).toFloat() / queue.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable { isFlipped = !isFlipped },
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rotation <= 90f)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (rotation <= 90f) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (reverseMode) "🇸🇦" else flag, fontSize = 30.sp)
                                    Spacer(Modifier.height(14.dp))
                                    Text(
                                        front,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    if (!reverseMode) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            card.pronunciation,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "اضغط لكشف الإجابة",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                                ) {
                                    Text(
                                        back,
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    if (card.example.isNotBlank()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            card.example,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            card.exampleTranslation,
                                            style = MaterialTheme.typography.bodySmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    TextButton(onClick = { tts.speak(card.indonesian, 1.0f, lang) }) {
                                        Text("🔊 استمع")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (!isFlipped) {
                        Text(
                            "استرجع المعنى من ذاكرتك قبل الكشف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // التقييم الذاتي — هذا ما يغذّي محرك الجدولة.
                        Text(
                            "كيف كان استرجاعك؟",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.height(10.dp))

                        fun advance(grade: Grade) {
                            viewModel.gradeItem(card.id, grade)
                            graded++
                            isFlipped = false
                            index++
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GradeButton("لم أتذكر", Color(0xFFD32F2F), Modifier.weight(1f)) {
                                advance(Grade.AGAIN)
                            }
                            GradeButton("صعب", Color(0xFFF57C00), Modifier.weight(1f)) {
                                advance(Grade.HARD)
                            }
                            GradeButton("جيد", Color(0xFF388E3C), Modifier.weight(1f)) {
                                advance(Grade.GOOD)
                            }
                            GradeButton("سهل", Color(0xFF1976D2), Modifier.weight(1f)) {
                                advance(Grade.EASY)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradeButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}
