package com.indolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.theme.*
import com.indolearn.viewmodel.HomeViewModel
import com.indolearn.ui.components.bounceClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

data class HomeMenuItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val route: String,
    val bgColor: Color
)

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val progress = viewModel.progress.collectAsState().value

    val learnItems = listOf(
        HomeMenuItem("📖", "الدروس", "تعلم خطوة بخطوة", "lessons", MaterialTheme.colorScheme.primaryContainer),
        HomeMenuItem("📝", "المفردات", "أهم الكلمات", "vocabulary", MaterialTheme.colorScheme.secondaryContainer),
        HomeMenuItem("📐", "القواعد", "قواعد اللغة", "grammar", MaterialTheme.colorScheme.surfaceVariant),
        HomeMenuItem("🗣️", "اللغة اليومية", "تعبيرات واقعية", "casual", MaterialTheme.colorScheme.tertiaryContainer),
    )

    val practiceItems = listOf(
        HomeMenuItem("🃏", "البطاقات", "راجع بالبطاقات", "flashcards", MaterialTheme.colorScheme.errorContainer),
        HomeMenuItem("🧪", "اختبار سريع", "اختبر نفسك", "quiz", MaterialTheme.colorScheme.tertiaryContainer),
        HomeMenuItem("🔄", "مراجعة اليوم", "كرر ما تعلمته", "review", MaterialTheme.colorScheme.primaryContainer),
        HomeMenuItem("⭐", "المفضلة", "كلماتك المحفوظة", "favorites", MaterialTheme.colorScheme.secondaryContainer),
    )

    val otherItems = listOf(
        HomeMenuItem("🎯", "المنهج الكامل", "خريطة التعلم", "curriculum", MaterialTheme.colorScheme.surfaceVariant),
        HomeMenuItem("🎓", "مدرب اليومية", "تدريب تفاعلي", "casual_interactive", MaterialTheme.colorScheme.errorContainer),
        HomeMenuItem("🔍", "البحث", "ابحث عن كلمة", "search", MaterialTheme.colorScheme.primaryContainer),
        HomeMenuItem("⚙️", "الإعدادات", "تخصيص التطبيق", "settings", MaterialTheme.colorScheme.tertiaryContainer),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === HERO HEADER ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // Gamification Streak Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Streak", tint = TertiaryLight)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "3 أيام",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "مرحباً! 👋",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "تابع رحلتك في تعلم الإندونيسية",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(20.dp))

                // Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "تقدمك",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "${progress.completedLessons} / ${progress.totalLessons} درس",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.totalLessons > 0)
                                    progress.completedLessons.toFloat() / progress.totalLessons
                                else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SecondaryLight,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // === CONTENT ===
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section: Learn
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("📚 التعلم")
            }
            items(learnItems) { item ->
                HomeCard(item) { navController.navigate(item.route) }
            }

            // Section: Practice
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("🧠 التدريب")
            }
            items(practiceItems) { item ->
                HomeCard(item) { navController.navigate(item.route) }
            }

            // Section: Other
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("⚙️ أخرى")
            }
            items(otherItems) { item ->
                HomeCard(item) { navController.navigate(item.route) }
            }

            // Bottom spacer
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun HomeCard(item: HomeMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = item.bgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 20.sp)
            }
            Column {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}