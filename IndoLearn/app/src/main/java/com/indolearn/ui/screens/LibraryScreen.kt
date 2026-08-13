package com.indolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.data.library.LibraryDoc
import com.indolearn.data.library.LibraryRepository
import com.indolearn.data.library.MdBlock

/**
 * المكتبة المرجعية — موسوعة تعلّم الإندونيسية.
 *
 * تعرض 27 ملفاً من محتوى موسوعة indolang المحفوظ في
 * `assets/encyclopedia/`، بالكامل وبلا إنترنت.
 *
 * لماذا عرض Markdown بدل تحويل كل شيء إلى جداول قاعدة بيانات؟
 * لأن جزءاً كبيراً من المحتوى شرح نثري وسياق ثقافي وتحذيرات استخدام،
 * وتفكيكه إلى صفوف يُفقده معناه. الصفوف المنتظمة فقط استُخرجت إلى
 * قاعدة البيانات (انظر IndoLangContent.kt) لتصبح قابلة للاختبار والمراجعة.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController) {
    val context = LocalContext.current
    val repo = remember { LibraryRepository(context) }
    var docs by remember { mutableStateOf<List<LibraryDoc>>(emptyList()) }
    var openDoc by remember { mutableStateOf<LibraryDoc?>(null) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        docs = repo.listDocs()
        loading = false
    }

    val visible = remember(docs, query) {
        if (query.isBlank()) docs
        else docs.filter {
            it.title.contains(query, true) || it.summary.contains(query, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(openDoc?.title ?: "📚 المكتبة المرجعية") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (openDoc != null) openDoc = null else navController.popBackStack()
                    }) {
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                openDoc != null -> DocumentView(repo, openDoc!!)

                else -> Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        placeholder = { Text("ابحث في فهرس الموسوعة…") },
                        singleLine = true,
                        leadingIcon = { Text("🔍", fontSize = 18.sp) }
                    )
                    if (visible.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text("لا نتائج مطابقة")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(visible) { doc ->
                                Card(
                                    Modifier.fillMaxWidth().clickable { openDoc = doc },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(doc.emoji, fontSize = 24.sp)
                                        Spacer(Modifier.width(14.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                doc.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (doc.summary.isNotBlank()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    doc.summary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                        Text("›", fontSize = 22.sp,
                                            color = MaterialTheme.colorScheme.outline)
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
private fun DocumentView(repo: LibraryRepository, doc: LibraryDoc) {
    var blocks by remember(doc.fileName) { mutableStateOf<List<MdBlock>>(emptyList()) }

    LaunchedEffect(doc.fileName) { blocks = repo.readBlocks(doc.fileName) }

    if (blocks.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    SelectionContainer {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(blocks) { block ->
                when (block) {
                    is MdBlock.Heading -> Text(
                        block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineSmall
                            2 -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = if (block.level <= 2) 12.dp else 4.dp)
                    )

                    is MdBlock.Paragraph -> Text(
                        block.text,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp
                    )

                    is MdBlock.Bullet -> Row(Modifier.padding(start = 8.dp)) {
                        Text("•  ", color = MaterialTheme.colorScheme.primary)
                        Text(block.text, style = MaterialTheme.typography.bodyMedium)
                    }

                    is MdBlock.Divider -> HorizontalDivider(
                        Modifier.padding(vertical = 8.dp)
                    )

                    is MdBlock.Table -> TableBlock(block)
                }
            }
        }
    }
}

/**
 * عرض جدول Markdown.
 * الجداول هنا هي عمود المحتوى (925 صفاً)، لذا تُعرض بتنسيق حقيقي
 * لا كنص خام، مع تمرير أفقي للجداول العريضة.
 */
@Composable
private fun TableBlock(table: MdBlock.Table) {
    val scroll = rememberScrollState()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(
                Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 6.dp)
            ) {
                table.header.forEach { cell ->
                    Text(
                        cell,
                        Modifier.weight(1f).padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            table.rows.forEachIndexed { i, row ->
                Row(
                    Modifier
                        .background(
                            if (i % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .padding(vertical = 6.dp)
                ) {
                    for (c in 0 until table.header.size) {
                        Text(
                            row.getOrElse(c) { "" },
                            Modifier.weight(1f).padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = if (c == 0) FontFamily.Monospace else FontFamily.Default
                        )
                    }
                }
            }
        }
    }
}
