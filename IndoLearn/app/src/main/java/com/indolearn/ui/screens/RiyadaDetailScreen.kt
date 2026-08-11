package com.indolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.indolearn.data.local.riyada.RiyadaDataResponse
import com.indolearn.viewmodel.RiyadaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiyadaDetailScreen(navController: NavController, viewModel: RiyadaViewModel, sectionId: String) {
    val dataState = viewModel.riyadaData.collectAsState().value

    val sectionInfo = dataState?.SECTIONS?.find { it.id == sectionId }
    val categoryJson = getCategoryJson(sectionId, dataState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sectionInfo?.title ?: "دليل رائد الأعمال") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                categoryJson != null && categoryJson.isJsonArray -> {
                    if (sectionId == "scenarios") {
                        RenderScenarios(categoryJson.asJsonArray)
                    } else if (sectionId == "mistakes") {
                        RenderMistakes(categoryJson.asJsonArray)
                    } else if (sectionId == "reflections" || sectionId == "favorites") {
                        RenderGenericArray("", categoryJson.asJsonArray)
                    } else {
                        RenderGenericArray("", categoryJson.asJsonArray)
                    }
                }
                categoryJson != null && categoryJson.isJsonObject -> {
                    RenderJsonObject(categoryJson.asJsonObject)
                }
                else -> {
                    Text("جاري تحميل المحتوى...", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RenderMistakes(mistakes: JsonArray) {
    for (i in 0 until mistakes.size()) {
        val mistake = mistakes[i].asJsonObject
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                mistake.getStringField("err")?.let { err ->
                    Text("❌ $err", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                mistake.getStringField("fix")?.let { fix ->
                    Text("✅ $fix", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun RenderScenarios(scenarios: JsonArray) {
    for (i in 0 until scenarios.size()) {
        val scenario = scenarios[i].asJsonObject
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val title = scenario.getStringField("title") ?: scenario.getStringField("sit") ?: scenario.getStringField("situation")
                title?.let {
                    Text("💬 $it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                }
                
                scenario.getArrayField("approach")?.let { approach ->
                    for (j in 0 until approach.size()) {
                        Text("- ${approach[j].asString}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                }
                
                scenario.getStringField("say")?.let { say ->
                    Text("🗣️ $say", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun RenderJsonObject(json: JsonObject) {
    // Render "intro" or "desc" explicitly if they exist at root
    json.getStringField("intro")?.let { intro ->
        Text(text = intro, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, lineHeight = 28.sp)
        Spacer(Modifier.height(20.dp))
    }
    json.getStringField("desc")?.let { desc ->
        Text(text = desc, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, lineHeight = 28.sp)
        Spacer(Modifier.height(20.dp))
    }

    // Render ANY arrays
    for ((key, value) in json.entrySet()) {
        if (value.isJsonArray) {
            val arr = value.asJsonArray
            if (key == "phases") {
                for (i in 0 until arr.size()) {
                    if (arr[i].isJsonObject) RenderPhaseCard(arr[i].asJsonObject)
                }
            } else {
                for (i in 0 until arr.size()) {
                    val item = arr[i]
                    if (item.isJsonPrimitive) {
                        Text("• ${item.asString}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
                    } else if (item.isJsonObject) {
                        RenderSubSectionCard(item.asJsonObject)
                    }
                }
            }
        } else if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
            if (key !in listOf("intro", "desc", "id", "color", "title")) {
                Text(text = value.asString, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, lineHeight = 28.sp, modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

@Composable
private fun RenderPhaseCard(phase: JsonObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            phase.getStringField("phase")?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
            }
            phase.getArrayField("skills")?.let { skills ->
                for (i in 0 until skills.size()) {
                    val skill = skills[i].asJsonObject
                    RenderSkillItem(skill)
                }
            }
        }
    }
}

@Composable
private fun RenderSkillItem(skill: JsonObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            skill.getStringField("name")?.let {
                Text(text = "📌 $it", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            skill.getStringField("why")?.let {
                Spacer(Modifier.height(4.dp))
                Text(text = "لماذا؟ $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            skill.getStringField("when")?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = "⏰ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            skill.getStringField("source")?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = "📖 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            skill.getStringField("practice")?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = "🎯 $it", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun RenderSubSectionCard(section: JsonObject) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Try common title fields: sub, title, name, trait, phase, q, sit
            val title = section.getStringField("sub") ?: section.getStringField("title") 
                ?: section.getStringField("name") ?: section.getStringField("trait")
                ?: section.getStringField("phase") ?: section.getStringField("q")
                ?: section.getStringField("sit")
                
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
            }

            // Render all string/array fields
            for ((key, value) in section.entrySet()) {
                if (key in listOf("sub", "title", "name", "trait", "phase", "q", "sit", "color")) continue
                
                if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                    Text(
                        text = value.asString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                } else if (value.isJsonArray) {
                    val arr = value.asJsonArray
                    if (key == "skills") {
                        for (i in 0 until arr.size()) {
                            if (arr[i].isJsonObject) RenderSkillItem(arr[i].asJsonObject)
                        }
                    } else {
                        for (i in 0 until arr.size()) {
                            val item = arr[i]
                            if (item.isJsonPrimitive) {
                                Text("- ${item.asString}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp, start = 8.dp))
                            } else if (item.isJsonObject) {
                                RenderSubSectionCard(item.asJsonObject) // recursive
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderGenericArray(label: String, array: JsonArray) {
    for (i in 0 until array.size()) {
        val item = array[i]
        when {
            item.isJsonPrimitive -> {
                Text("• ${item.asString}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
            }
            item.isJsonObject -> {
                RenderSubSectionCard(item.asJsonObject)
            }
        }
    }
}

// Helper extensions
private fun JsonObject.getStringField(key: String): String? {
    return if (has(key) && get(key).isJsonPrimitive) get(key).asString else null
}

private fun JsonObject.getArrayField(key: String): JsonArray? {
    return if (has(key) && get(key).isJsonArray) get(key).asJsonArray else null
}

private fun getCategoryJson(id: String, data: RiyadaDataResponse?): JsonElement? {
    if (data == null) return null
    return when (id) {
        "reflections" -> data.REFLECTIONS
        "learning" -> data.LEARNING
        "currentjob" -> data.CURRENTJOB
        "personality" -> data.PERSONALITY
        "sales" -> data.SALES
        "money" -> data.MONEY
        "project" -> data.PROJECT
        "reputation" -> data.REPUTATION
        "books" -> data.BOOKS
        "courses" -> data.COURSES
        "ai" -> data.AI
        "habits" -> data.HABITS
        "mistakes" -> data.MISTAKES
        "scenarios" -> data.SCENARIOS
        "future" -> data.FUTURE
        "dashboard" -> data.DASHBOARD
        else -> null
    }
}
