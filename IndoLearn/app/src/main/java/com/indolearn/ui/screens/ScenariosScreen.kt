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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.data.local.entity.DailyScenarioEntity
import com.indolearn.ui.components.EmptyOrLoading
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.LearnViewModel

/**
 * مواقف واقعية مبنية على daily_scenarios الفعلية.
 * كانت 57 محادثة مخزنة ولا توجد شاشة تعرضها؛ وكان المدرب يفتح لعبة سوق
 * ثابتة مهما كان معرّف السيناريو الذي اختاره.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenariosScreen(
    navController: NavController,
    viewModel: LearnViewModel,
    initialScenarioId: Int? = null
) {
    val scenarios = viewModel.scenarios.collectAsState().value
    val isReady = viewModel.isReady.collectAsState().value
    val language = viewModel.currentLanguage.collectAsState().value
    var selectedId by remember(initialScenarioId) { mutableStateOf(initialScenarioId) }
    val selected = scenarios.firstOrNull { it.id == selectedId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected?.titleAr ?: "🎭 مواقف الحياة اليومية") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedId != null && initialScenarioId == null) selectedId = null
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                scenarios.isEmpty() -> EmptyOrLoading(
                    isReady,
                    "لا توجد مواقف لهذه اللغة",
                    "اختر لغة أخرى أو ارجع إلى الدروس الأساسية.",
                    "🎭"
                )
                selected != null -> ScenarioDetail(selected, language)
                initialScenarioId != null && isReady -> Column(
                    Modifier.align(Alignment.Center).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تعذر العثور على هذا الموقف")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("العودة") }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "اختر موقفاً، استمع لكل جملة، ثم مثّل أحد الدورين بصوتك.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    items(scenarios, key = { it.id }) { scenario ->
                        Card(
                            onClick = { selectedId = scenario.id },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎭", style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(scenario.titleAr, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${scenario.title} · ${scenario.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("›")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioDetail(scenario: DailyScenarioEntity, language: String) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }
    var speed by remember { mutableStateOf(0.85f) }
    val targetLines = scenario.dialogue.lines().filter { it.isNotBlank() }
    val arabicLines = scenario.translation.lines().filter { it.isNotBlank() }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("الهدف: استخدام اللغة في موقف ${scenario.category}", fontWeight = FontWeight.Bold)
                    Text("اقرأ الدور، استمع، ثم كرره من الذاكرة.", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(speed == 0.7f, { speed = 0.7f }, label = { Text("بطيء") })
                        FilterChip(speed == 0.85f, { speed = 0.85f }, label = { Text("طبيعي") })
                        FilterChip(speed == 1f, { speed = 1f }, label = { Text("سريع") })
                    }
                }
            }
        }
        items(targetLines.indices.toList()) { index ->
            val target = targetLines[index]
            val arabic = arabicLines.getOrNull(index).orEmpty()
            val spokenText = target.substringAfter(':', target).trim()
            val speaker = target.substringBefore(':', "").trim()
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    if (speaker.isNotEmpty()) {
                        Text(speaker, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(spokenText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (arabic.isNotEmpty()) {
                        Text(
                            arabic.substringAfter(':', arabic).trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { tts.speak(spokenText, speed, language) }) { Text("🔊 استمع وكرّر") }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(
                    "تطبيق: أغلق الترجمة ذهنياً، واختر دوراً واحداً وقل جمله كلها دون قراءة.",
                    Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
