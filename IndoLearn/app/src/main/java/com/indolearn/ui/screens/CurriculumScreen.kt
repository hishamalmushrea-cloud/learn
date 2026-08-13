package com.indolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.components.EmptyOrLoading
import com.indolearn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurriculumScreen(navController: NavController, viewModel: LearnViewModel) {
    val stages = viewModel.stages.collectAsState().value
    val isReady = viewModel.isReady.collectAsState().value
    val lessonCounts = viewModel.lessonCounts.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 المنهج الكامل") },
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
            // Header Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "خريطة طريق تعلم الإندونيسية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "نهج تعليمي متسلسل ومدروس بعناية ينتقل بك من مستوى الصفر المطلق إلى مرحلة الطلاقة والمحادثة اليومية الحرة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            if (stages.isEmpty()) {
                EmptyOrLoading(isReady, "لا توجد مراحل بعد", "أعد فتح التطبيق. إن استمرت المشكلة فامسح بيانات التطبيق.")
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(stages) { stage ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = stage.isUnlocked && (lessonCounts[stage.level] ?: 0) > 0) {
                                    navController.navigate("lessons/${stage.level}")
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (stage.isUnlocked) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (stage.isUnlocked) 2.dp else 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stage.titleAr,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (stage.isUnlocked) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            }
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val count = lessonCounts[stage.level] ?: 0
                                        // الصدق مع المستخدم: مرحلة بلا دروس تُعرض كذلك،
                                        // بدل أن تُفتح على شاشة فارغة بلا تفسير.
                                        when {
                                            count == 0 -> Badge(
                                                containerColor = MaterialTheme.colorScheme.outlineVariant,
                                                contentColor = MaterialTheme.colorScheme.outline
                                            ) {
                                                Text("قيد الإعداد", modifier = Modifier.padding(horizontal = 4.dp))
                                            }
                                            stage.isUnlocked -> Badge(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ) {
                                                Text("$count درساً", modifier = Modifier.padding(horizontal = 4.dp))
                                            }
                                            else -> Badge(
                                                containerColor = MaterialTheme.colorScheme.outlineVariant,
                                                contentColor = MaterialTheme.colorScheme.outline
                                            ) {
                                                Text("مغلق", modifier = Modifier.padding(horizontal = 4.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "🇮🇩 " + stage.titleId,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (stage.isUnlocked) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        }
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stage.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = if (stage.isUnlocked) 1f else 0.5f
                                        )
                                    )
                                }
                                
                                Text(
                                    text = when {
                                        (lessonCounts[stage.level] ?: 0) == 0 -> "🚧"
                                        stage.isUnlocked -> "🔓"
                                        else -> "🔒"
                                    },
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
