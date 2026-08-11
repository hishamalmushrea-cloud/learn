package com.indolearn.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.indolearn.data.local.entity.NoteEntity
import com.indolearn.ui.theme.*
import com.indolearn.viewmodel.LearnViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(navController: NavController, viewModel: LearnViewModel) {
    val notes = viewModel.notes.collectAsState().value
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showAddBlock by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 دفتر أفكاري وملاحظاتي") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBlock = !showAddBlock },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(if (showAddBlock) "❌" else "➕", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Expanding Form to Add a New Note
            AnimatedVisibility(
                visible = showAddBlock,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📝 تدوين فكرة أو ملاحظة جديدة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("عنوان الملاحظة / الكلمة...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("اكتب تفاصيل الفكرة أو الجملة أو الدرس هنا...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                    viewModel.saveNote(noteTitle, noteContent)
                                    noteTitle = ""
                                    noteContent = ""
                                    showAddBlock = false
                                    Toast.makeText(context, "تم حفظ الملاحظة بنجاح! 💾", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء ملء حقل العنوان والمحتوى!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("حفظ الملاحظة في الدفتر 💾", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📓", fontSize = 72.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "دفتر أفكارك فارغ!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "هنا يمكنك تدوين مفرداتك الخاصة، أو صياغة جملك الجديدة، أو تلخيص الدروس التي تعلمتها ليسهل الرجوع إليها وحفظها دائماً.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Bottom padding for FAB space
                ) {
                    items(notes) { note ->
                        NoteCard(
                            note = note,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString("${note.title}\n${note.content}"))
                                Toast.makeText(context, "تم نسخ الملاحظة للحافظة! 📋", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                viewModel.deleteNote(note)
                                Toast.makeText(context, "تم حذف الملاحظة. 🗑️", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()) }
    val dateString = remember(note.timestamp) { sdf.format(Date(note.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onCopy) {
                    Text("📋", fontSize = 18.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الملاحظة",
                        tint = WrongRed
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(Modifier.height(6.dp))
            Text(
                text = "🕒 تدوين: $dateString",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
