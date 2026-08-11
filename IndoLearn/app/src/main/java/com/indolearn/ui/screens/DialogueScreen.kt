package com.indolearn.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.data.local.entity.DialogueEntity
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(navController: NavController, viewModel: LearnViewModel) {
    val dialogues = viewModel.dialogues.collectAsState().value
    val currentLanguage = viewModel.currentLanguage.collectAsState().value
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    var currentSpeed by remember { mutableStateOf(1.0f) }

    DisposableEffect(Unit) {
        onDispose {
            tts.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗣️ المحادثات التفاعلية") },
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
        ) {
            if (dialogues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Dialogue Selector Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dialogues.forEachIndexed { index, dialogue ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = dialogue.titleAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ) 
                            }
                        )
                    }
                }

                // Speed selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "سرعة نطق الصوت:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = currentSpeed == 0.75f,
                            onClick = { currentSpeed = 0.75f },
                            label = { Text("بطيء (0.75x)") }
                        )
                        FilterChip(
                            selected = currentSpeed == 1.0f,
                            onClick = { currentSpeed = 1.0f },
                            label = { Text("طبيعي (1.0x)") }
                        )
                    }
                }

                val activeDialogue = dialogues[selectedTabIndex]
                
                // Show Subtitle
                Text(
                    text = "🇮🇩 ${activeDialogue.titleId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Conversation bubbles
                val lines = activeDialogue.content.split("\n")
                
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(lines) { line ->
                        if (line.isNotBlank() && line.contains(": ")) {
                            val parts = line.split(": ", limit = 2)
                            val speaker = parts[0].trim()
                            val text = parts[1].trim()

                            val isA = speaker.lowercase() == "a" || speaker.lowercase() == "penjual"

                            DialogueBubble(
                                speaker = if (isA) "أحمد 🧔" else "سيتي 🧕",
                                text = text,
                                isA = isA,
                                onSpeak = { tts.speak(text, currentSpeed, langCode = currentLanguage) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogueBubble(
    speaker: String,
    text: String,
    isA: Boolean,
    onSpeak: () -> Unit
) {
    // Simple mock Arabic translations for our seeded dialogues to make it extremely premium!
    val translation = when (text) {
        "Halo, nama saya Ahmad. Saya dari Yaman." -> "مرحبا، اسمي أحمد. أنا من اليمن."
        "Halo, saya Siti. Senang bertemu denganmu." -> "مرحبا، أنا سيتي. سعيدة بلقائك."
        "Saya senang juga. Kamu tinggal di mana?" -> "أنا سعيد أيضاً. أين تسكنين؟"
        "Saya tinggal di Jakarta." -> "أنا أسكن في جاكرتا."
        "Ke sini dong! Lihat-lihat baju bagus dan murah." -> "تعال هنا! تفرج على الملابس الجميلة والرخيصة."
        "Terima kasih. Baju merah ini berapa harganya?" -> "شكراً لك. هذه القميص الأحمر كم سعره؟"
        "Itu murah banget, cuma seratus ribu." -> "هذا رخيص جداً، فقط مائة ألف روبية."
        "Bisa kurang tidak? Delapan puluh ribu saja ya?" -> "هل يمكن تخفيضه؟ ثمانون ألف فقط يا؟"
        "Boleh deh, ambil saja!" -> "حسنٌ، خذه!"
        "Merhaba, benim adım Ahmet. Senin adın ne?" -> "مرحباً، اسمي أحمد. ما اسمكِ؟"
        "Merhaba Ahmet, benim adım Zeynep. Memnun oldum." -> "مرحباً أحمد، اسمي زينب. سررت بلقائكِ."
        "Ben de memnun oldum. Nasılsın?" -> "وأنا سررت بلقائكِ أيضاً. كيف حالكِ؟"
        "İyiyim, teşekkür ederim. Sen nasılsın?" -> "بخير، شكراً لكِ. كيف حالكِ أنتِ؟"
        "Ben de iyiyim, sağ ol." -> "أنا بخير أيضاً، تسلمي."
        else -> ""
    }

    val bubbleShape = if (isA) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    val bubbleBg = if (isA) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isA) Alignment.Start else Alignment.End

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Text(
            text = speaker,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = bubbleBg),
            modifier = Modifier.widthIn(max = 290.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isA) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text + if (translation.isNotEmpty()) " — $translation" else ""))
                            android.widget.Toast.makeText(context, "تم نسخ جملة المحادثة! 📋", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("📋", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("🔊", fontSize = 16.sp)
                    }
                }
                if (translation.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(
                        color = (if (isA) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.15f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = translation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = (if (isA) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
