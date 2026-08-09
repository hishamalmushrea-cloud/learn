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
fun CurriculumScreen(navController: NavController, viewModel: MainViewModel) {
    val stages = listOf(
        "المرحلة 1 — الصفر" to "التحيات، الأرقام، الضمائر",
        "المرحلة 2 — المبتدئ" to "ترتيب الجملة، النفي، السؤال",
        "المرحلة 3 — المبتدئ المتقدم" to "الأزمنة، المقارنة، القدرة",
        "المرحلة 4 — البادئات واللواحق" to "me-, ber-, di-, ter-, -kan",
        "المرحلة 5 — المتوسط العملي" to "محادثات، قراءة، كتابة",
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("المنهج الكامل", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("من الصفر إلى المتوسط", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(stages) { (title, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            // Future: navigate to stage detail
                            navController.navigate("lessons")
                        }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium)
                        Text(desc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}