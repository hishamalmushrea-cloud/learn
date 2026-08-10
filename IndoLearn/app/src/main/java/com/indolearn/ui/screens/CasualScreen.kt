package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.viewmodel.LearnViewModel

@Composable
fun CasualScreen(navController: NavController, viewModel: LearnViewModel) {
    // For demo we use the existing repository via a new flow if needed.
    // Here we show a static rich list of real daily Indonesian.

    val casualExpressions = listOf(
        Triple("Makasih ya!", "ماكاسي يا", "شكرًا! (ودي)"),
        Triple("Santai aja", "سانتاي أجا", "خذها ببساطة"),
        Triple("Ke sini dong!", "كي سيني دونج", "تعال هنا!"),
        Triple("Murah banget!", "موراه بانجيت", "رخيص جدًا!"),
        Triple("Udah makan?", "أوداه ماكان", "هل أكلت؟"),
        Triple("Mau ke mana?", "ماو كي مانا", "إلى أين ذاهب؟"),
        Triple("Bisa kurang?", "بيسا كورانج", "يمكن تخفيض؟"),
        Triple("Nggak apa-apa", "نجاك أبا-أبا", "لا بأس"),
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("الإندونيسية اليومية الواقعية", style = MaterialTheme.typography.headlineSmall)
        Text("Bahasa Sehari-hari", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(casualExpressions) { (id, pron, meaning) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(id, style = MaterialTheme.typography.titleLarge)
                        Text(pron)
                        Text(meaning, style = MaterialTheme.typography.bodyLarge)
                        Text("🔵 يومي / غير رسمي", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
