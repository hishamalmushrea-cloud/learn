package com.indolearn.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.theme.*
import com.indolearn.utils.TtsManager
import com.indolearn.utils.VerbDetailsHelper
import com.indolearn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyScreen(navController: NavController, viewModel: LearnViewModel) {
    val words = viewModel.vocabulary.collectAsState().value
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    // إطلاق محرك النطق عند مغادرة الشاشة.
    // بدونه يبقى TextToSpeech حياً بعد إغلاق الشاشة (تسريب موارد)،
    // ويتراكم مع كل زيارة للشاشة.
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    var selectedCategory by remember { mutableStateOf("الكل") }
    val categories = listOf("الكل") + words.map { it.category }.distinct().sorted()
    val filteredWords = if (selectedCategory == "الكل") words else words.filter { it.category == selectedCategory }

    var expandedWordId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 قاموس المفردات والأفعال (${filteredWords.size})") },
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category; expandedWordId = null },
                        label = { Text(category, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            if (filteredWords.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredWords) { word ->
                        val isExpanded = expandedWordId == word.id
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    expandedWordId = if (isExpanded) null else word.id 
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Category Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            word.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    
                                    // Formal vs Slang register badge
                                    val isSlang = !word.isFormal || word.category == "عامية" || word.indonesian.lowercase() in listOf("nggak", "udah", "banget", "aja", "makasih", "apahan", "lu", "elo")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSlang) CardOrange else CardGreen)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isSlang) "غير رسمي / عامي" else "رسمي",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSlang) OnTertiaryContainerLight else OnSecondaryContainerLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { tts.speak(word.indonesian, langCode = word.languageCode) }) {
                                        Text("🔊", fontSize = 18.sp)
                                    }
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                    IconButton(onClick = {
                                        val textToCopy = "${word.indonesian} (${word.pronunciation}) — ${word.arabic}" + 
                                            if (word.example.isNotBlank()) "\nمثال: ${word.example} — ${word.exampleTranslation}" else ""
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(textToCopy))
                                        android.widget.Toast.makeText(context, "تم نسخ الكلمة بنجاح! 📋", android.widget.Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("📋", fontSize = 18.sp)
                                    }
                                    IconButton(onClick = {
                                        viewModel.toggleFavorite(word.id, !word.favorite)
                                    }) {
                                        Icon(
                                            if (word.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (word.favorite) WrongRed else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    word.indonesian,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "النطق: " + word.pronunciation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    word.arabic,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                if (word.example.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "📌 مثال: ${word.example}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        word.exampleTranslation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Interactive Card Expansion with Seeding details
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    val verbDetail = VerbDetailsHelper.getDetailFor(word.indonesian)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        Spacer(Modifier.height(10.dp))

                                        if (verbDetail != null) {
                                            // Word Breakdown
                                            Text(
                                                "🔎 تفكيك الكلمات:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                verbDetail.breakdown,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            // Related Words
                                            Text(
                                                "🔗 كلمات مرتبطة:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            ) {
                                                verbDetail.related.forEach { rel ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            rel,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                }
                                            }

                                            // Interactive Exercise
                                            InteractiveExerciseBlock(verbDetail)
                                        } else {
                                            // Default generated details for regular words
                                            Text(
                                                "🔎 تفكيك الكلمات:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                "${word.indonesian} = ${word.arabic}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                        
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "💡 اضغط على البطاقة لطي التفاصيل",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveExerciseBlock(verbDetail: com.indolearn.utils.VerbDetail) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Text(
            "🧠 تمرين تفاعلي سريع:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            verbDetail.question,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            verbDetail.options.forEach { option ->
                val isSelected = selectedOption == option
                val optionBg = if (isSelected) {
                    if (hasChecked) {
                        if (option == verbDetail.answer) CorrectGreen else WrongRed
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                } else {
                    MaterialTheme.colorScheme.surface
                }
                
                val optionContentColor = if (isSelected) {
                    if (hasChecked) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Button(
                    onClick = {
                        if (!hasChecked) {
                            selectedOption = option
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = optionBg,
                        contentColor = optionContentColor
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Text(option, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedOption != null && !hasChecked) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { hasChecked = true },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تحقق ✓")
            }
        }

        if (hasChecked) {
            Spacer(Modifier.height(8.dp))
            val isCorrect = selectedOption == verbDetail.answer
            Text(
                text = if (isCorrect) "🟢 إجابة صحيحة! أحسنت." else "🔴 إجابة خاطئة. الإجابة الصحيحة هي: ${verbDetail.answer}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) CorrectGreen else WrongRed
            )
            TextButton(
                onClick = {
                    selectedOption = null
                    hasChecked = false
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("إعادة المحاولة 🔄")
            }
        }
    }
}
