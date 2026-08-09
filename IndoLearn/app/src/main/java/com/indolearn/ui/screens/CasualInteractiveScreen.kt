package com.indolearn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.indolearn.utils.TtsManager
import com.indolearn.viewmodel.MainViewModel

@Composable
fun CasualInteractiveScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val tts = remember { TtsManager(context) }

    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("التعبيرات", "السيناريوهات", "التدريب")

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("مدرب الإندونيسية اليومية", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        // Progress indicator (demo)
        LinearProgressIndicator(
            progress = 0.65f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("تقدمك: 65%", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = currentTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = currentTab == index,
                    onClick = { currentTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (currentTab) {
            0 -> ExpressionsTab(tts)
            1 -> ScenariosTab(tts)
            2 -> TrainingTab(tts)
        }
    }
}

@Composable
fun ExpressionsTab(tts: TtsManager) {
    val expressions = listOf(
        "Makasih ya!" to "شكرًا! (ودي)",
        "Santai aja" to "خذها ببساطة",
        "Ke sini dong!" to "تعال هنا!",
        "Murah banget!" to "رخيص جدًا!",
        "Udah makan?" to "هل أكلت؟",
        "Bisa kurang?" to "هل يمكن تخفيض السعر؟",
        "Nggak apa-apa" to "لا بأس",
        "Mau ke mana?" to "إلى أين ذاهب؟"
    )

    LazyColumn {
        items(expressions) { (exp, meaning) ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(exp, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { tts.speak(exp) }) {
                            Text("🔊")
                        }
                    }
                    Text(meaning)
                    Row {
                        TextButton(onClick = { /* TODO: Show explanation dialog */ }) {
                            Text("💡 اشرح لي")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScenariosTab(tts: TtsManager) {
    Column {
        Text("سيناريو: السوق (مبتدئ)", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        val dialogue = listOf(
            "Penjual: Ke sini dong! Lihat-lihat dulu." to "تعال هنا! تفرّج أولًا.",
            "Pembeli: Iya, saya lihat-lihat dulu." to "نعم، سأتفرج أولًا.",
            "Penjual: Mau yang mana?" to "أي واحد تريد؟",
            "Pembeli: Yang ini berapa?" to "هذا كم سعره؟"
        )

        dialogue.forEach { (id, ar) ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Row {
                        Text(id, modifier = Modifier.weight(1f))
                        IconButton(onClick = { tts.speak(id.split(": ").last()) }) {
                            Text("🔊")
                        }
                    }
                    Text(ar, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun TrainingTab(tts: TtsManager) {
    var userAnswer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var showListenExercise by remember { mutableStateOf(false) }

    Column {
        Text("تدريب: اكتب الجملة المناسبة", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Text("أنت في السوق. تريد أن تسأل عن السعر:")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            label = { Text("اكتب الجملة بالإندونيسية") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            feedback = when {
                userAnswer.contains("berapa", true) || userAnswer.contains("harga", true) ->
                    "✅ صحيح! Berapa harganya?"
                else -> "❌ حاول مرة أخرى. الإجابة المتوقعة: Berapa harganya?"
            }
        }) {
            Text("تحقق من الإجابة")
        }

        if (feedback.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(feedback, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(24.dp))
        Divider()
        Spacer(Modifier.height(16.dp))

        Button(onClick = { showListenExercise = !showListenExercise }) {
            Text("🎧 تمرين استماع")
        }

        if (showListenExercise) {
            Spacer(Modifier.height(12.dp))
            Text("استمع ثم اختر المعنى الصحيح:")
            Button(onClick = { tts.speak("Mau ke mana?") }) {
                Text("🔊 استمع")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { feedback = "✅ صحيح! إلى أين ذاهب؟" }) { Text("إلى أين ذاهب؟") }
            Button(onClick = { feedback = "❌ خطأ" }) { Text("ماذا تريد؟") }
        }
    }
}