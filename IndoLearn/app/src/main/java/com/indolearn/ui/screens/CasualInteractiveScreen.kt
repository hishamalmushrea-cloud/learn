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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.theme.*
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// State Machine Models for Branching Dialogues
data class DialogueOption(
    val id: String,
    val text: String,
    val translation: String,
    val nextNodeId: String
)

data class DialogueNode(
    val id: String,
    val textId: String,
    val textAr: String,
    val options: List<DialogueOption>,
    val endingType: String? = null, // "success", "polite", "doubt", "withdrew", "delay"
    val endingFeedback: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasualInteractiveScreen(navController: NavController, viewModel: LearnViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    val currentLanguage = viewModel.currentLanguage.collectAsState().value

    var currentTab by remember { mutableStateOf(0) }
    val tabs = if (currentLanguage == "TR") {
        listOf("🇹🇷 القاموس والضمائر", "🏪 محادثة التعارف", "🎮 الألعاب التفاعلية")
    } else {
        listOf("🗣️ التعبيرات والقاموس", "🏪 مدرب المحادثة المتفرّع", "🎧 التدريبات اليومية")
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    // Reset tab on language change
    LaunchedEffect(currentLanguage) {
        currentTab = 0
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Dual Language Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (currentLanguage == "TR") "🇹🇷 مدرب التركية اليومية" else "🇮🇩 مدرب الإندونيسية اليومية",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (currentLanguage == "TR") "تعلم قواعد التوافق، الضمائر، وتصريف الأفعال" else "تعلم المحادثات العامية والسوق التفاعلية بطريقة اللعب",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Floating toggle button for quick language switch in this screen
            Button(
                onClick = {
                    val nextLang = if (currentLanguage == "TR") "ID" else "TR"
                    viewModel.switchLanguage(nextLang)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(if (currentLanguage == "TR") "🇮🇩 إندونيسي" else "🇹🇷 تركي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        Spacer(Modifier.height(16.dp))

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
            if (currentLanguage == "TR") {
                when (currentTab) {
                    0 -> TurkishDictionaryTab(tts)
                    1 -> TurkishDialogueTab(tts)
                    2 -> TurkishGamesTab(tts)
                }
            } else {
                when (currentTab) {
                    0 -> ExpressionsTab(tts)
                    1 -> BranchingDialogueTab(tts)
                    2 -> TrainingTab(tts)
                }
            }
        }
    }
}

// ==========================================
// === TURKISH TABS (Real PDF Content) ===
// ==========================================

@Composable
fun TurkishDictionaryTab(tts: TtsManager) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val turkishPronouns = listOf(
        Triple("Ben", "أنا", "بين"),
        Triple("Sen", "أنت / أنتِ", "سين"),
        Triple("O", "هو / هي (للغائب، لا تذكير أو تأنيث)", "أو"),
        Triple("Biz", "نحن", "بيز"),
        Triple("Siz", "أنتم / أنتن / بصيغة الاحترام", "سيز"),
        Triple("Onlar", "هم / هن", "أون-لار")
    )

    val turkishColors = listOf(
        Triple("Kırmızı", "أحمر", "كير-مي-زي"),
        Triple("Mavi", "أزرق", "ما-في"),
        Triple("Yeşil", "أخضر", "يي-شيل"),
        Triple("Sarı", "أصفر", "سا-ري"),
        Triple("Siyah", "أسود", "سي-ياه"),
        Triple("Beyaz", "أبيض", "بي-ياز")
    )

    val turkishDays = listOf(
        Triple("Pazartesi", "الاثنين", "با-زار-تي-سي"),
        Triple("Salı", "الثلاثاء", "سا-لي"),
        Triple("Çarşamba", "الأربعاء", "تشار-شام-با"),
        Triple("Perşembe", "الخميس", "بير-شيم-بيه"),
        Triple("Cuma", "الجمعة", "جو-ما"),
        Triple("Cumartesi", "السبت", "جو-مار-تي-سي"),
        Triple("Pazar", "الأحد", "با-زار")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("👤 الضمائر الشخصية (Şahıs Zamirleri)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        items(turkishPronouns) { (word, meaning, pron) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("النطق: $pron", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        Text("المعنى: $meaning", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString("$word ($pron) — $meaning"))
                        Toast.makeText(context, "تم نسخ الضمير! 📋", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋")
                    }
                    IconButton(onClick = { tts.speak(word, langCode = "TR") }) {
                        Text("🔊")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("🎨 الألوان الأساسية (Renkler)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        items(turkishColors) { (word, meaning, pron) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBlue)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("النطق: $pron", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        Text("المعنى: $meaning", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString("$word — $meaning"))
                        Toast.makeText(context, "تم نسخ اللون! 📋", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋")
                    }
                    IconButton(onClick = { tts.speak(word, langCode = "TR") }) {
                        Text("🔊")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("📅 أيام الأسبوع (Günler)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        items(turkishDays) { (word, meaning, pron) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardOrange)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnTertiaryContainerLight)
                        Text("النطق: $pron", style = MaterialTheme.typography.bodyMedium, color = OnTertiaryContainerLight.copy(alpha = 0.6f))
                        Text("المعنى: $meaning", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = OnTertiaryContainerLight)
                    }
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString("$word — $meaning"))
                        Toast.makeText(context, "تم نسخ اليوم! 📋", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("📋")
                    }
                    IconButton(onClick = { tts.speak(word, langCode = "TR") }) {
                        Text("🔊")
                    }
                }
            }
        }
    }
}

@Composable
fun TurkishDialogueTab(tts: TtsManager) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val dialogLines = remember {
        listOf(
            Triple("A: Merhaba, benim adım Ahmet. Senin adın ne?", "أحمد: مرحباً، اسمي أحمد. ما اسمكِ؟", "Ahmet"),
            Triple("B: Merhaba Ahmet, benim adım Zeynep. Memnun oldum.", "زينب: مرحباً أحمد، اسمي زينب. سررت بلقائك.", "Zeynep"),
            Triple("A: Ben de memnun oldum. Nasılsın?", "أحمد: وأنا سررت بلقائكِ أيضاً. كيف حالكِ؟", "Ahmet"),
            Triple("B: İyiyim, teşekkür ederim. Sen nasılsın?", "زينب: بخير، شكراً لك. كيف حالك أنت؟", "Zeynep"),
            Triple("A: Ben de iyiyim, sağ ol.", "أحمد: أنا بخير أيضاً، تسلمين (شكراً لك).", "Ahmet")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        // Dialogue Partner Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("🇹🇷", fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("محادثة التعارف (Tanışma)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("تدرب على نطق الحوار الأساسي بالتركية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dialogLines) { (tr, ar, speaker) ->
                val isAhmet = speaker == "Ahmet"
                val bubbleBg = if (isAhmet) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                val alignment = if (isAhmet) Alignment.Start else Alignment.End
                val bubbleShape = if (isAhmet) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Text(speaker, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 4.dp))
                    Card(
                        shape = bubbleShape,
                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString("$tr — $ar"))
                                    Toast.makeText(context, "تم نسخ العبارة! 📋", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(28.dp)) {
                                    Text("📋", fontSize = 14.sp)
                                }
                                IconButton(onClick = { tts.speak(tr.substringAfter(": "), langCode = "TR") }, modifier = Modifier.size(28.dp)) {
                                    Text("🔊", fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(Modifier.height(4.dp))
                            Text(ar, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TurkishGamesTab(tts: TtsManager) {
    var activeSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("🔴 التوافق الصوتي", "🧩 ترتيب الجمل (SVO vs SOV)", "🃏 تصريفات الأفعال")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeSubTab == index,
                    onClick = { activeSubTab = index },
                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                0 -> VowelHarmonyGame(tts)
                1 -> SentenceOrderingGame()
                2 -> ThreeTenseVerdsCard(tts)
            }
        }
    }
}

// ==========================================
// === TURKISH MINI-GAMES IMPLEMENTATION ===
// ==========================================

@Composable
fun VowelHarmonyGame(tts: TtsManager) {
    val words = listOf(
        Triple("Ev (بيت)", "ler", "حرف 'e' خفيف، لذا يأخذ اللاحقة -ler!"),
        Triple("Kitap (كتاب)", "lar", "حرف 'a' ثقيل، لذا يأخذ اللاحقة -lar!"),
        Triple("Köpek (كلب)", "ler", "حرف 'e' خفيف، لذا يأخذ اللاحقة -ler!"),
        Triple("Araba (سيارة)", "lar", "حرف 'a' ثقيل، لذا يأخذ اللاحقة -lar!"),
        Triple("Masa (طاولة)", "lar", "حرف 'a' ثقيل، لذا يأخذ اللاحقة -lar!"),
        Triple("Göz (عين)", "ler", "حرف 'ö' خفيف، لذا يأخذ اللاحقة -ler!")
    )

    var currentIndex by remember { mutableStateOf(0) }
    val currentWord = words[currentIndex]

    var selectedSuffix by remember { mutableStateOf<String?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎯 تحدي لاحقة الجمع الثنائية (lar / ler)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    currentWord.first,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { tts.speak(currentWord.first.split(" ").first(), langCode = "TR") }) {
                    Text("🔊", fontSize = 24.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("اختر لاحقة الجمع المناسبة لهذه الكلمة:", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("lar", "ler").forEach { suffix ->
                    val isSelected = selectedSuffix == suffix
                    val buttonBg = if (isSelected) {
                        if (hasAnswered) {
                            if (suffix == currentWord.second) CorrectGreen else WrongRed
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }

                    Button(
                        onClick = {
                            if (!hasAnswered) {
                                selectedSuffix = suffix
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(suffix, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            if (selectedSuffix != null && !hasAnswered) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = { hasAnswered = true }) {
                    Text("تحقق من صحة الإجابة ✓")
                }
            }

            if (hasAnswered) {
                Spacer(Modifier.height(16.dp))
                val isCorrect = selectedSuffix == currentWord.second
                Text(
                    text = if (isCorrect) "🟢 إجابة صحيحة! أحسنت." else "🔴 إجابة خاطئة.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) CorrectGreen else WrongRed
                )
                Text(
                    text = currentWord.third,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    selectedSuffix = null
                    hasAnswered = false
                    currentIndex = (currentIndex + 1) % words.size
                }) {
                    Text("الكلمة التالية ➡️")
                }
            }
        }
    }
}

@Composable
fun SentenceOrderingGame() {
    var step by remember { mutableStateOf(0) } // 0 = Indo, 1 = Turk
    val indonesianWords = remember { listOf("Saya", "membaca", "buku") } // Correct: 0, 1, 2 (SVO)
    val turkishWords = remember { listOf("Ben", "kitap", "okuyorum") } // Correct: 0, 1, 2 (SOV)

    val currentWords = if (step == 0) indonesianWords else turkishWords
    val shuffledWords = remember(step) { currentWords.shuffled() }

    val selectedList = remember { mutableStateListOf<String>() }
    var isVerified by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🧩 لغز مقارنة ترتيب الجمل النحوية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "الهدف: رتب الكلمات لصياغة جملة \"أنا أقرأ كتاباً\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))

            // Current state tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (step == 0) CardGreen else CardBlue)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (step == 0) "🇮🇩 الإندونيسية: فاعل + فعل + مفعول (SVO)" else "🇹🇷 التركية: فاعل + مفعول + فعل (SOV)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (step == 0) OnSecondaryContainerLight else OnPrimaryContainerLight
                )
            }

            Spacer(Modifier.height(24.dp))

            // Display current selected sequence
            Text("جملتك الحالية:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (selectedList.isEmpty()) "[اضغط الكلمات أدناه للترتيب]" else selectedList.joinToString(" "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            // Word selection chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shuffledWords.forEach { word ->
                    val isUsed = selectedList.contains(word)
                    val isUsedCheck = selectedList.contains(word)
                    AssistChip(
                        onClick = {
                            if (!isUsedCheck) {
                                selectedList.add(word)
                            } else {
                                selectedList.remove(word)
                            }
                        },
                        label = { Text(word, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                        colors = if (isUsedCheck) {
                            AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            AssistChipDefaults.assistChipColors()
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { selectedList.clear(); isVerified = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("مسح الترتيب 🔄", color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = {
                        isVerified = true
                        val cleanAssembly = selectedList.toList()
                        isCorrect = if (step == 0) {
                            cleanAssembly == indonesianWords
                        } else {
                            cleanAssembly == turkishWords
                        }
                    },
                    enabled = selectedList.size == currentWords.size
                ) {
                    Text("تحقق ✓")
                }
            }

            if (isVerified) {
                Spacer(Modifier.height(16.dp))
                if (isCorrect) {
                    Text("🟢 ترتيب صحيح تماماً! أحسنت.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CorrectGreen)
                    Spacer(Modifier.height(8.dp))
                    if (step == 0) {
                        Text(
                            "لاحظ أن الفعل (membaca) يأتي في المنتصف كالعربية والإنجليزية.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            step = 1
                            selectedList.clear()
                            isVerified = false
                        }) {
                            Text("انتقل للغز التركي ➡️")
                        }
                    } else {
                        Text(
                            "عبقري! لاحظ كيف تضع التركية الفعل (okuyorum) في نهاية الجملة دائماً بينما المفعول به (kitap) في المنتصف!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            step = 0
                            selectedList.clear()
                            isVerified = false
                        }) {
                            Text("إعادة التحدي 🔄")
                        }
                    }
                } else {
                    Text("🔴 الترتيب غير صحيح، حاول مجدداً.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = WrongRed)
                }
            }
        }
    }
}

@Composable
fun ThreeTenseVerdsCard(tts: TtsManager) {
    val verbs = listOf(
        Triple("yapmak (يفعل / يصنع)", mapOf("present" to "yapıyorum", "past" to "yaptım", "future" to "yapacağım"), mapOf("present" to "أفعل الواجب", "past" to "فعلت الواجب", "future" to "سأفعل الواجب")),
        Triple("gitmek (يذهب)", mapOf("present" to "gidiyorum", "past" to "gittim", "future" to "gideceğim"), mapOf("present" to "أنا أذهب", "past" to "أنا ذهبت", "future" to "أنا سأذهب")),
        Triple("sevmek (يحب)", mapOf("present" to "seviyorum", "past" to "sevdim", "future" to "seveceğim"), mapOf("present" to "أنا أحب", "past" to "أنا أحببت", "future" to "أنا سأحب")),
        Triple("almak (يأخذ / يشتري)", mapOf("present" to "alıyorum", "past" to "aldım", "future" to "alacağım"), mapOf("present" to "أنا أشتري", "past" to "أنا اشتريت", "future" to "أنا سأشتري"))
    )

    var verbIndex by remember { mutableStateOf(0) }
    val currentVerb = verbs[verbIndex]

    var selectedTense by remember { mutableStateOf("present") }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🃏 بطاقة تصريف الفعل التركي ثلاثية الأوجه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Text(
                currentVerb.first,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            // Tense selector buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("present" to "الحاضر", "past" to "الماضي", "future" to "المستقبل").forEach { (tenseCode, title) ->
                    val isSelected = selectedTense == tenseCode
                    Button(
                        onClick = { selectedTense = tenseCode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Dynamic tense word card
            Card(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val tenseWord = currentVerb.second[selectedTense] ?: ""
                    val translation = currentVerb.third[selectedTense] ?: ""

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                tenseWord,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = { tts.speak(tenseWord, langCode = "TR") }) {
                            Text("🔊", fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    verbIndex = (verbIndex + 1) % verbs.size
                    selectedTense = "present"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("الفعل التالي ➡️", color = Color.White)
            }
        }
    }
}

// ==========================================
// === ORIGINAL INDONESIAN TABS ===
// ==========================================

@Composable
fun ExpressionsTab(tts: TtsManager) {
    val formalColloquialPairs = listOf(
        Triple("tidak ↔ nggak / gak", "لا", "الأكثر شيوعًا في الكلام اليومي ومواقف الشارع"),
        Triple("sudah ↔ udah", "بالفعل / تم", "شائعة جدًا في اختصار الكلمات اليومية"),
        Triple("sangat ↔ banget", "جدًا", "توضع دائمًا بعد الصفة، مثل: mahal banget (غالي جدًا)"),
        Triple("saja ↔ aja", "فقط", "تستعمل لتأكيد الحصر"),
        Triple("terima kasih ↔ makasih", "شكرًا لك", "صيغة ودية لطيفة ومحبوبة"),
        Triple("apa ↔ apaan", "ماذا", "تفيد الاستغراب الشديد: آپان هذا؟"),
        Triple("kakak ↔ ka", "الأخ الأكبر", "نداء مختصر ودي لمن هم أكبر منك")
    )

    val youthSlang = listOf(
        Triple("gabut", "يشعر بالملل / لا شيء ليفعله", "slang: اختصار لـ gaji buta"),
        Triple("baper", "يبالغ في التأثر عاطفيًا", "slang: اختصار لـ bawa perasaan"),
        Triple("kepo", "فضولي ويتدخل في شؤون الناس", "slang: فضولي"),
        Triple("lebay", "مبالغ فيه / درامي", "slang: مبالغ فيه"),
        Triple("mager", "كسول عن الحركة", "slang: اختصار لـ malas gerak"),
        Triple("nongkrong", "يسهر أو يقضي وقتًا مع الأصدقاء", "slang: تجمع ودي"),
        Triple("ngopi", "يشرب القهوة مع الرفاق", "slang: شرب القهوة"),
        Triple("curhat", "يفضفض عن مشاعره لصديق", "slang: اختصار لـ mencurahkan isi hati"),
        Triple("gaje", "غير واضح أو مبهم", "slang: اختصار لـ nggak jelas")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📘 المقارنات اليومية (الرسمي ↔ العامي)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        items(formalColloquialPairs) { (pair, meaning, note) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            pair,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString("$pair — $meaning"))
                            Toast.makeText(context, "تم نسخ العبارة! 📋", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("📋")
                        }
                        IconButton(onClick = { tts.speak(pair.split(" ↔ ").last()) }) {
                            Text("🔊")
                        }
                    }
                    Text("المعنى: $meaning", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("💡 ملاحظة: $note", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("🔥 قاموس عاميات الشباب (Slang Dictionary)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }

        items(youthSlang) { (slang, meaning, note) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "🔥 $slang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnTertiaryContainerLight,
                            modifier = Modifier.weight(1f)
                        )
                        val clipboardManager = LocalClipboardManager.current
                        val context = LocalContext.current
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString("$slang — $meaning"))
                            Toast.makeText(context, "تم نسخ العبارة! 📋", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("📋")
                        }
                        IconButton(onClick = { tts.speak(slang) }) {
                            Text("🔊")
                        }
                    }
                    Text("المعنى: $meaning", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = OnTertiaryContainerLight)
                    Text("💡 المصدر: $note", style = MaterialTheme.typography.bodySmall, color = OnTertiaryContainerLight.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun BranchingDialogueTab(tts: TtsManager) {
    val coroutineScope = rememberCoroutineScope()

    val dialogueNodes = remember {
        mapOf(
            "n1" to DialogueNode(
                id = "n1",
                textId = "Selamat datang teman! Silakan lihat-lihat, semua murah hari ini.",
                textAr = "أهلًا صديقي! تفضل تفرج، كل شيء رخيص اليوم.",
                options = listOf(
                    DialogueOption("o1_a", "Berapa harganya?", "كم السعر؟", "n2a"),
                    DialogueOption("o1_b", "Saya hanya lihat-lihat.", "فقط أتفرج", "n2b"),
                    DialogueOption("o1_c", "Ada diskon?", "هل من تخفيض؟", "n2c")
                )
            ),
            "n2a" to DialogueNode(
                id = "n2a",
                textId = "Yang mana? Ini harganya seratus lima puluh ribu.",
                textAr = "أي واحد؟ هذا سعره مئة وخمسون ألفاً.",
                options = listOf(
                    DialogueOption("o2a_a", "Bisa kurang?", "هل يمكن التخفيض؟", "n3a"),
                    DialogueOption("o2a_b", "Terlalu mahal!", "غالي جداً!", "n3b")
                )
            ),
            "n2b" to DialogueNode(
                id = "n2b",
                textId = "Tidak apa-apa, lihat saja dulu. Ada yang lain yang kamu suka?",
                textAr = "لا مشكلة، تفرج أولاً. هل يوجد ما يعجبك؟",
                options = listOf(
                    DialogueOption("o2b_a", "Yang mana paling laris?", "أيها الأكثر مبيعًا؟", "n3c"),
                    DialogueOption("o2b_b", "Saya mau pergi dulu.", "سأذهب الآن", "end_polite_leave")
                )
            ),
            "n2c" to DialogueNode(
                id = "n2c",
                textId = "Tentu! Hari ini diskon besar, khusus untuk kamu.",
                textAr = "طبعاً! اليوم تخفيض كبير، خصوصاً لك.",
                options = listOf(
                    DialogueOption("o2c_a", "Wah, keren! Berapa diskonnya?", "رائع! كم الخصم؟", "n3a"),
                    DialogueOption("o2c_b", "Saya tidak percaya.", "لا أصدقك", "end_doubt")
                )
            ),
            "n3a" to DialogueNode(
                id = "n3a",
                textId = "Untuk kamu, saya kasih seratus tiga puluh ribu. Harga teman!",
                textAr = "لك، أعطيك مئة وثلاثين ألفاً. سعر الأصدقاء!",
                options = listOf(
                    DialogueOption("o3a_a", "Oke, saya beli!", "حسنًا، أشتريه!", "n4"),
                    DialogueOption("o3a_b", "Masih mahal. Seratus dua puluh ya.", "ما زال غاليًا. مئة وعشرون؟", "n3b"),
                    DialogueOption("o3a_c", "Saya pikir dulu.", "سأفكر أولًا", "end_delay")
                )
            ),
            "n3b" to DialogueNode(
                id = "n3b",
                textId = "Hmm... Oke, harga terakhir seratus dua puluh lima ribu. Setuju?",
                textAr = "هممم... حسنًا، السعر النهائي مئة وخمسة وعشرون ألفًا. موافق؟",
                options = listOf(
                    DialogueOption("o3b_a", "Setuju!", "موافق!", "n4"),
                    DialogueOption("o3b_b", "Tidak, terlalu mahal.", "لا، غالٍ جدًا", "end_withdrew")
                )
            ),
            "n3c" to DialogueNode(
                id = "n3c",
                textId = "Ini paling laris. Wanginya tahan lama, lebih dari 24 jam.",
                textAr = "هذا الأكثر مبيعاً. رائحته تدوم أكثر من 24 ساعة.",
                options = listOf(
                    DialogueOption("o3c_a", "Boleh saya coba?", "هل يمكنني تجربته؟", "n4"),
                    DialogueOption("o3c_b", "Saya tidak suka wanginya.", "لا تعجبني رائحته", "end_polite_leave")
                )
            ),
            "n4" to DialogueNode(
                id = "n4",
                textId = "Oke! Ini uang kembaliannya. Terima kasih banyak, teman!",
                textAr = "حسنًا! هذا باقي نقودك. شكرًا جزيلًا يا صديقي!",
                options = listOf(
                    DialogueOption("o4_a", "Terima kasih! Sampai jumpa lagi!", "شكرًا! إلى اللقاء!", "end_success_a"),
                    DialogueOption("o4_b", "Makasih ya!", "شكرًا! - عامية مبهجة", "end_success_b"),
                    DialogueOption("o4_c", "Sampai nanti!", "أراك لاحقًا!", "end_success_c")
                )
            ),
            // Endings
            "end_polite_leave" to DialogueNode(
                id = "end_polite_leave",
                textId = "Datang lagi ya! Sampai jumpa.",
                textAr = "تعال مرة أخرى يا! إلى اللقاء.",
                options = emptyList(),
                endingType = "polite",
                endingFeedback = "وداع مهذب 💬: لقد غادرت المتجر بأدب. البائع سعيد بزيارتك ومستعد لاستقبالك لاحقاً!"
            ),
            "end_doubt" to DialogueNode(
                id = "end_doubt",
                textId = "Serius dong! Saya tidak bohong.",
                textAr = "أنا جاد! أنا لا أكذب.",
                options = emptyList(),
                endingType = "doubt",
                endingFeedback = "شك وحذر ⚠️: أدى تعبيرك عن عدم التصديق إلى إنهاء التفاوض بنوع من الحرج."
            ),
            "end_delay" to DialogueNode(
                id = "end_delay",
                textId = "Silakan, nanti datang lagi ya.",
                textAr = "تفضل، عد لاحقاً يا.",
                options = emptyList(),
                endingType = "delay",
                endingFeedback = "تأجيل وعودة ⏱️: قررت التفكير قبل الشراء. هذا ذكاء في التسوّق، والبائع يحترم قرارك!"
            ),
            "end_withdrew" to DialogueNode(
                id = "end_withdrew",
                textId = "Tidak apa-apa, terima kasih sudah mampir.",
                textAr = "لا بأس، شكراً جزيلاً لزيارتك.",
                options = emptyList(),
                endingType = "withdrew",
                endingFeedback = "انسحاب مبرر 🚪: السعر لم يناسبك فقررت الانسحاب بذكاء وهدوء."
            ),
            "end_success_a" to DialogueNode(
                id = "end_success_a",
                textId = "Sama-sama! Hati-hati di jalan ya.",
                textAr = "على الرحب والسعة! رافقتك السلامة في الطريق يا.",
                options = emptyList(),
                endingType = "success",
                endingFeedback = "نجاح باهر بنسبة 100% 🏆: لقد أجريت صفقة شراء عطر ناجحة وممتازة باللغة الرسمية المهذبة!"
            ),
            "end_success_b" to DialogueNode(
                id = "end_success_b",
                textId = "Yo, santai aja bro! Makasih banyak ya.",
                textAr = "أهلاً بك، خذها ببساطة يا أخي! شكراً جزيلاً لك يا.",
                options = emptyList(),
                endingType = "success",
                endingFeedback = "نجاح باهر بأسلوب عامي مبهج 🌟: رائع! لقد استخدمت تعبيرات الشارع بذكاء وانسجمت مع البائع كصديق حقيقي!"
            ),
            "end_success_c" to DialogueNode(
                id = "end_success_c",
                textId = "Dah, sampai nanti!",
                textAr = "إلى اللقاء، أراك لاحقاً!",
                options = emptyList(),
                endingType = "success",
                endingFeedback = "إغلاق سريع وناجح ✅: اشتريت بضاعتك وغادرت بعبارة مختصرة صحيحة لغوياً."
            )
        )
    }

    var currentNodeId by remember { mutableStateOf("n1") }
    val currentNode = dialogueNodes[currentNodeId] ?: dialogueNodes["n1"]!!
    var isTyping by remember { mutableStateOf(false) }

    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // Pair(text, isUser)

    fun resetDialogue() {
        currentNodeId = "n1"
        chatHistory.clear()
        isTyping = false
    }

    LaunchedEffect(currentNodeId) {
        if (chatHistory.isEmpty()) {
            isTyping = true
            delay(600)
            isTyping = false
            tts.speak(currentNode.textId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("🧔", fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("بائع العطور (جاكرتا) 🇮🇩", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("نشط الآن 🟢", style = MaterialTheme.typography.labelSmall, color = CorrectGreen)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { resetDialogue() }) {
                Text("🔄", fontSize = 18.sp)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "💬 المشهد: دخلت متجراً للعطور الفاخرة في وسط جاكرتا. البائع يبتسم لك ويدعوك لتفقد البضاعة. اختياراتك ستغير مجرى الحوار تماماً!",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(chatHistory) { (msg, isUser) ->
                ChatBubbleView(msg = msg, isUser = isUser)
            }

            if (isTyping) {
                item {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("البائع يكتب الآن...", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (!isTyping && currentNode.endingType == null) {
                item {
                    SellerBubbleWithTts(currentNode = currentNode, tts = tts)
                }
            }
        }

        if (currentNode.endingType != null) {
            val endingColor = if (currentNode.endingType == "success") CorrectGreen else if (currentNode.endingType == "polite") MaterialTheme.colorScheme.primary else WrongRed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(endingColor.copy(alpha = 0.15f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currentNode.endingType == "success") "🏆 شراء ناجح ومكتمل!" else if (currentNode.endingType == "polite") "💬 مغادرة مهذبة" else "🚪 حوار غير مكتمل",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = endingColor
                )
                Spacer(Modifier.height(8.dp))
                Text("🧔 البائع: \"${currentNode.textId}\"", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text("(${currentNode.textAr})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(text = currentNode.endingFeedback ?: "", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { resetDialogue() }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = endingColor)) {
                    Text("إعادة المحاولة 🔄", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                currentNode.options.forEach { option ->
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                chatHistory.add(Pair(option.text + " (${option.translation})", true))
                                isTyping = true
                                delay(800)
                                isTyping = false
                                currentNodeId = option.nextNodeId
                                val nextNode = dialogueNodes[option.nextNodeId]
                                if (nextNode != null) {
                                    tts.speak(nextNode.textId)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(option.text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(option.translation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleView(msg: String, isUser: Boolean) {
    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    }

    val bubbleBg = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleBg),
            modifier = Modifier.widthIn(max = 280.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = msg,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun SellerBubbleWithTts(currentNode: DialogueNode, tts: TtsManager) {
    Card(
        shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.widthIn(max = 280.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentNode.textId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                val clipboardManager = LocalClipboardManager.current
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("${currentNode.textId} — ${currentNode.textAr}"))
                        Toast.makeText(context, "تم نسخ العبارة! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("📋", fontSize = 18.sp)
                }
                IconButton(
                    onClick = { tts.speak(currentNode.textId) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("🔊", fontSize = 18.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
            Spacer(Modifier.height(6.dp))
            Text(
                text = currentNode.textAr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun TrainingTab(tts: TtsManager) {
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var showListenExercise by remember { mutableStateOf(false) }

    Column {
        Text("📝 تدريب: اكتب الجملة المناسبة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("أنت في السوق وتريد أن تسأل عن السعر بالإندونيسية:")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            label = { Text("اكتب الجملة هنا...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val clean = userAnswer.trim().lowercase()
                feedback = if (clean.contains("berapa") && (clean.contains("harga") || clean.contains("baju"))) {
                    "✅ صحيح وممتاز! Berapa harganya?"
                } else {
                    "❌ حاول مرة أخرى. الجملة المقترحة: Berapa harganya? (كم سعره؟)"
                }
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("تحقق من الإجابة ✓")
        }

        if (feedback.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(feedback, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showListenExercise = !showListenExercise },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("🎧 تمرين استماع تفاعلي")
        }

        if (showListenExercise) {
            Spacer(Modifier.height(12.dp))
            Text("استمع جيداً للصوت ثم اختر المعنى العربي المقابل لها:")
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { tts.speak("Mau ke mana?") },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🔊 تشغيل الصوت")
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { feedback = "✅ صحيح! النطق يعني: إلى أين تذهب؟" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلى أين تذهب؟")
                }
                Button(
                    onClick = { feedback = "❌ خطأ. النطق لا يعني هذا." },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ماذا تريد؟")
                }
            }
        }
    }
}
