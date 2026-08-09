package com.indolearn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.MainViewModel

@Composable
fun LessonsScreen(navController: NavController, viewModel: MainViewModel) {
    val lessons = viewModel.lessons.collectAsState().value

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("الدروس - المستوى 0", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(lessons) { lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            navController.navigate("lesson/${lesson.id}")
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(lesson.titleAr, style = MaterialTheme.typography.titleMedium)
                        Text(lesson.titleId, style = MaterialTheme.typography.bodyMedium)
                        if (lesson.completed) Text("✓ مكتمل", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}