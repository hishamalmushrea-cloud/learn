package com.indolearn.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.indolearn.utils.TtsManager
import com.indolearn.ui.components.EmptyOrLoading
import com.indolearn.viewmodel.LearnViewModel

/**
 * اللغة اليومية الواقعية.
 *
 * ما تغيّر ولماذا:
 * كانت هذه الشاشة تعرض **قائمة ثابتة من 8 عبارات مكتوبة داخل الكود**،
 * بينما قاعدة البيانات تحتوي على 40 تعبيراً إندونيسياً مبذوراً لا يراها أحد.
 * وكانت تتجاهل اللغة المختارة تماماً، فيرى متعلم التركية عبارات إندونيسية.
 *
 * الآن: تقرأ من قاعدة البيانات، تحترم اللغة المختارة، وتدعم النطق والترشيح بالفئة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasualScreen(navController: NavController, viewModel: LearnViewModel) {
    val expressions = viewModel.casual.collectAsState().value
    val isReady = viewModel.isReady.collectAsState().value
    val lang = viewModel.currentLanguage.collectAsState().value
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    var category by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    val categories = remember(expressions) { expressions.map { it.category }.distinct() }
    val visible = remember(expressions, category) {
        if (category == null) expressions else expressions.filter { it.category == category }
    }
    val subtitle = if (lang == "TR") "Günlük Türkçe" else "Bahasa Sehari-hari"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗣️ اللغة اليومية") },
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
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text(subtitle, style = MaterialTheme.typography.titleMedium)
            Text(
                "الكلام الطبيعي كما يُستخدم في الشارع — لا لغة الكتب.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            if (categories.isNotEmpty()) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = category == null,
                        onClick = { category = null },
                        label = { Text("الكل") }
                    )
                    categories.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (expressions.isEmpty()) {
                EmptyOrLoading(isReady, "لا توجد تعبيرات بعد", "أعد فتح التطبيق. إن استمرت المشكلة فامسح بيانات التطبيق.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(visible) { e ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        e.expression,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { tts.speak(e.expression, 1.0f, lang) }) {
                                        Text("🔊")
                                    }
                                }
                                Text(
                                    e.pronunciation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(e.meaning, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(onClick = {}, label = { Text(e.formality) })
                                    AssistChip(onClick = {}, label = { Text(e.category) })
                                }
                                if (e.usage.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "💬 ${e.usage}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                e.formalEquivalent?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        "🟢 المقابل الرسمي: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
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
