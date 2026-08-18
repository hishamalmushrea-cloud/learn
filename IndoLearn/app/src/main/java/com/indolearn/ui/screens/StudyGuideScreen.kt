package com.indolearn.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.theme.*
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGuideScreen(navController: NavController, viewModel: LearnViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    val currentLanguage = viewModel.currentLanguage.collectAsState().value

    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("كيف تتعلم فعلاً؟", "🧩 القوالب", "⚠️ أخطاء العرب", "✅ تقييم ذاتي")

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💡 مرشد التعلم وصندوق القوالب") },
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header Language Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (currentLanguage == "TR") "🇹🇷" else "🇮🇩", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == "TR") "المرشد التأسيسي للغة التركية" else "المرشد التأسيسي للغة الإندونيسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = if (currentLanguage == "TR") "بناء الجمل التفاعلي وقواعد التوافق الصوتي" else "قوالب التحدث الجاهزة وقواعد النفي والتركيب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Sub TabRow
            TabRow(
                selectedTabIndex = currentTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    0 -> HowLanguagesAreLearnedTab()
                    1 -> TemplateBuilderTab(currentLanguage, tts)
                    2 -> CommonMistakesTab(currentLanguage)
                    3 -> SelfAssessmentTab()
                }
            }
        }
    }
}

// ==========================================
// === TAB 1: INTERACTIVE TEMPLATE BUILDER ===
// ==========================================

@Composable
fun TemplateBuilderTab(lang: String, tts: TtsManager) {
    if (lang == "TR") {
        // Turkish SOV (Subject + Object + Verb) Word Order Builder
        var selectedObject by remember { mutableStateOf<String?>(null) }
        var selectedVerb by remember { mutableStateOf<String?>(null) }

        val objects = listOf(
            Triple("kitap", "كتاباً", "كي-تاب"),
            Triple("su", "ماءً", "سو"),
            Triple("elma", "تفاحاً", "إيل-ما"),
            Triple("ekmek", "خبزاً", "إيك-ميك")
        )

        val verbs = listOf(
            Triple("okuyorum", "أقرأ", "أو-كو-يو-روم"),
            Triple("istiyorum", "أريد", "إيس-تي-يو-روم"),
            Triple("yiyorum", "آكل", "يي-يو-روم"),
            Triple("alıyorum", "أشتري", "آ-لي-يو-روم")
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🧩 آلة بناء الجمل التركية (SOV)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "اختر مفعولاً به ثم فعلاً لتصيغ الجملة التركية تلقائياً:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // Live Built Sentence Display
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val obj = selectedObject ?: "[اختر مفعولاً]"
                    val verb = selectedVerb ?: "[اختر فعلاً]"
                    val fullSentence = "Ben $obj $verb."
                    
                    val translation = if (selectedObject != null && selectedVerb != null) {
                        val objAr = objects.first { it.first == selectedObject }.second
                        val verbAr = verbs.first { it.first == selectedVerb }.second
                        "أنا $verbAr $objAr."
                    } else {
                        "الترجمة ستظهر هنا..."
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                fullSentence,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        if (selectedObject != null && selectedVerb != null) {
                            IconButton(onClick = { tts.speak(fullSentence, langCode = "TR") }) {
                                Text("🔊", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Word Lists Selection
            Text("1. اختر المفعول به (Object):", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                objects.forEach { (word, meaning, pron) ->
                    val isSelected = selectedObject == word
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedObject = word },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(word, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(meaning, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("2. اختر الفعل (Verb) - يوضع بالآخر دائماً!:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                verbs.forEach { (word, meaning, pron) ->
                    val isSelected = selectedVerb == word
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedVerb = word },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(word, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(meaning, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    selectedObject = null
                    selectedVerb = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text("إعادة تهيئة 🔄", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    } else {
        // Indonesian SVO (Subject + Verb + Object) Word Order Builder
        var selectedVerb by remember { mutableStateOf<String?>(null) }

        val verbs = listOf(
            Triple("makan", "آكل", "ما-كان"),
            Triple("minum", "أشرب", "مي-نوم"),
            Triple("tidur", "أنام", "تي-دور"),
            Triple("beli", "أشتري", "بي-لي"),
            Triple("pergi", "أذهب", "بير-غي")
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🧩 آلة بناء الجمل الإندونيسية (SVO)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "اختر فعلاً لتركيب قالب الرغبة والحدث في الإندونيسية تلقائياً:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // Live Built Sentence Display
            Card(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val verb = selectedVerb ?: "[اختر فعلاً]"
                    val fullSentence = "Saya mau $verb."
                    
                    val translation = if (selectedVerb != null) {
                        val verbAr = verbs.first { it.first == selectedVerb }.second
                        "أنا أريد أن $verbAr."
                    } else {
                        "الترجمة ستظهر هنا..."
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                fullSentence,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        
                        if (selectedVerb != null) {
                            IconButton(onClick = { tts.speak(fullSentence, langCode = "ID") }) {
                                Text("🔊", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Word Lists Selection
            Text("اختر الفعل المرغوب (Verb) - يوضع بالمنتصف دائماً SVO:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(verbs) { (word, meaning, pron) ->
                    val isSelected = selectedVerb == word
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVerb = word },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                word,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                meaning,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// === TAB 2: COMMON NATIVE TRANSFER ERRORS ===
// ==========================================

data class ErrorCard(
    val wrong: String,
    val right: String,
    val reason: String,
    val tip: String
)

@Composable
fun CommonMistakesTab(lang: String) {
    val mistakes = if (lang == "TR") {
        listOf(
            ErrorCard(
                wrong = "Ben okuyorum kitap",
                right = "Ben kitap okuyorum",
                reason = "ترجمة حرفية تضع الفعل في المنتصف كالعربية.",
                tip = "تذكّر: التركية تضع الفعل (okuyorum) في نهاية الجملة دائماً (SOV)."
            ),
            ErrorCard(
                wrong = "Benim ev",
                right = "Benim evim",
                reason = "استخدام ضمير الملكية بدون لاحقة الاسم.",
                tip = "الملكية في التركية تحتاج لواصق طرفية مطابقة للاسم دائماً."
            ),
            ErrorCard(
                wrong = "İki kitaplar",
                right = "İki kitap",
                reason = "جمع الاسم بعد ذكر رقم عددي.",
                tip = "قاعدة: بعد ذكر الأرقام، يبقى الاسم مفرداً في التركية."
            )
        )
    } else {
        listOf(
            ErrorCard(
                wrong = "Saya bukan lapar",
                right = "Saya tidak lapar",
                reason = "خلط أدوات النفي (bukan مع الصفة).",
                tip = "tidak تستخدم لنفي الفعل والصفة، بينما bukan مخصصة لنفي الأسماء فقط."
            ),
            ErrorCard(
                wrong = "Buku saya yang baru",
                right = "Buku baru saya",
                reason = "ترتيب الصفة والملكية بشكل حرفي من العربية.",
                tip = "الصفة تسبق الفاعل في الترتيب، ثم تلحقها الملكية."
            )
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "⚠️ تجنب أخطاء النقل اللغوي للعرب",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "رصد لأبرز الأخطاء الشائعة نتيجة نقل عادات اللغة العربية للغة الجديدة وكيفية تصحيحها:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(4.dp))
        }

        items(mistakes) { card ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❌ الخطأ:", fontWeight = FontWeight.Bold, color = WrongRed, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(card.wrong, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = WrongRed)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✅ الصح:", fontWeight = FontWeight.Bold, color = CorrectGreen, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(card.right, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = CorrectGreen)
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    
                    Text("🧐 لماذا أخطأ الطالب؟", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text(card.reason, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))

                    Text("💡 نصيحة للوقاية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    Text(card.tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// ==========================================
// === TAB 3: STUDY ROADMAP & CHECKLIST ===
// ==========================================

@Composable
fun SelfAssessmentTab() {
    val itemsList = remember {
        listOf(
            "أستطيع تقديم نفسي بطلاقة (الاسم، العمر، العمل، البلد).",
            "أستطيع السؤال عن الأسعار والمساومة بلغة طبيعية بالسوق.",
            "أستطيع التمييز بصرياً وقواعدياً بين الرسمي والعامي.",
            "أفهم تماماً الفروق الهيكلية لترتيب الجملة (SVO vs SOV).",
            "أتقن قواعد التوافق الصوتي (للغة التركية) أو اللواحق (للإندونيسية).",
            "أستطيع تدوين وحفظ كلماتي الخاصة ومراجعتها في دفتري الشخصي."
        )
    }

    // State list for checkable boxes
    val checkedStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📈 تقييم ذاتي صادق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("هذه بنود قدرة عملية محدودة، لا شهادة طلاقة:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(4.dp))
        }

        items(itemsList.size) { index ->
            val text = itemsList[index]
            val isChecked = checkedStates[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { checkedStates[index] = !isChecked },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checkedStates[index] = it }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                        color = if (isChecked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            val checkedCount = checkedStates.count { it }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊 معدل الإتقان والجهوزية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { checkedCount.toFloat() / itemsList.size },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "لقد أكملت إتقان $checkedCount من أصل ${itemsList.size} مهارات عملية كبرى!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
