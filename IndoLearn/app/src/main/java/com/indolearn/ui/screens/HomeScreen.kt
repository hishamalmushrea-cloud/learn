package com.indolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
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
    val bgColor: Color,
    val contentColor: Color
)

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
    val progress = viewModel.progress.collectAsState().value
    val currentLanguage = viewModel.currentLanguage.collectAsState().value

    // Ultra-premium cohesive background-colored items
    val learnItems = listOf(
        HomeMenuItem("📖", "الدروس", "تعلم خطوة بخطوة", "lessons", CardBlue, OnPrimaryContainerLight),
        HomeMenuItem("📝", "المفردات", "أهم الكلمات والأفعال", "vocabulary", CardGreen, OnSecondaryContainerLight),
        HomeMenuItem("📐", "القواعد", "قواعد اللغة والنحو", "grammar", CardPurple, OnPrimaryContainerLight),
        HomeMenuItem("📚", "المكتبة المرجعية", "موسوعة كاملة للغة التي تتعلمها", "library", CardBlue, OnPrimaryContainerLight),
        HomeMenuItem("🗣️", "اللغة اليومية", "تعبيرات الشارع الواقعية", "casual", CardOrange, OnTertiaryContainerLight),
    )

    val practiceItems = listOf(
        HomeMenuItem("🃏", "البطاقات", "راجع بالبطاقات ثلاثية الأبعاد", "flashcards", CardPink, OnTertiaryContainerLight),
        HomeMenuItem("🧪", "اختبار سريع", "اختبر مستواك وحصيلتك", "quiz", CardOrange, OnTertiaryContainerLight),
        HomeMenuItem("🔄", "مراجعة اليوم", "كرر وثبت ما تعلمته", "review", CardBlue, OnPrimaryContainerLight),
        HomeMenuItem("⭐", "المفضلة", "كلماتك المحفوظة", "favorites", CardGreen, OnSecondaryContainerLight),
        // كانت شاشة "تقدمي" مُعرَّفة في AppNavigation بلا أي مسار يصل إليها.
        HomeMenuItem("📊", "تقدمي", "إحصاءات إتقانك الحقيقية", "progress", CardPurple, OnPrimaryContainerLight),
    )

    val otherItems = listOf(
        HomeMenuItem("🎯", "المنهج الكامل", "خريطة طريق التعلم", "curriculum", CardPurple, OnPrimaryContainerLight),
        HomeMenuItem("🎓", "مدرب اليومية", "سيناريوهات وألعاب تفاعلية", "casual_interactive", CardPink, OnTertiaryContainerLight),
        HomeMenuItem("🗣️", "محادثات ناطقة", "حوارات ثنائية مسموعة", "dialogue", CardOrange, OnTertiaryContainerLight),
        HomeMenuItem("📓", "دفتر أفكاري", "مذكراتك اللغوية والشخصية", "notebook", CardBlue, OnPrimaryContainerLight),
        HomeMenuItem("🔍", "البحث", "ابحث عن معاني الكلمات", "search", CardGreen, OnSecondaryContainerLight),
        HomeMenuItem("💼", "ريادة الأعمال", "تأسيس وإدارة المشاريع", "riyada_guide", CardPurple, OnPrimaryContainerLight),
        // كانت الموسوعة (405 أسطر محتوى) غير قابلة للوصول إطلاقاً.
        HomeMenuItem("🌍", "الموسوعة", "معلومات ثقافية ومرجعية", "encyclopedia", CardOrange, OnTertiaryContainerLight),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === HERO GLOWING HEADER ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                        )
                    )
                )
                .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                // Gamification & Switcher Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Switcher Button with Glassmorphic Style
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val nextLang = if (currentLanguage == "TR") "ID" else "TR"
                                viewModel.switchLanguage(nextLang)
                            }
                            .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == "TR") "🇹🇷 التركية" else "🇮🇩 الإندونيسية",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("🔄", fontSize = 12.sp)
                        }
                    }

                    // Streak Badge
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Streak", tint = GoldBadge, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "3 أيام",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (currentLanguage == "TR") "مرحباً! 👋 Hoş Geldiniz" else "مرحباً! 👋 Apa Kabar",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (currentLanguage == "TR") "تابع رحلتك في تعلم اللغة التركية 🇹🇷" else "تابع رحلتك في تعلم الإندونيسية 🇮🇩",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(20.dp))

                // Elegant Progress Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "معدل تقدمك الحالي",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${progress.completedLessons} / ${progress.totalLessons} درس مكتمل",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.totalLessons > 0)
                                    progress.completedLessons.toFloat() / progress.totalLessons
                                else 0f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = SecondaryLight,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // === INTRODUCTORY GUIDE BANNER ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .bounceClick { navController.navigate("study_guide") }
                .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💡", fontSize = 24.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مرشد التعلم وصندوق القوالب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnTertiaryContainerLight
                    )
                    Text(
                        text = "ابنِ جملك التفاعلية واكتشف أخطاء العرب الشائعة بقالب علمي",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnTertiaryContainerLight.copy(alpha = 0.75f)
                    )
                }
                Text("›", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnTertiaryContainerLight)
            }
        }

        // === GRID CONTENT ===
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section: Learn
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("📚 منهج التعلم والاستيعاب")
            }
            items(learnItems) { item ->
                HomeCard(item) { navController.navigate(item.route) }
            }

            // Section: Practice
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("🧠 التدريب والتثبيت الذاتي")
            }
            items(practiceItems) { item ->
                HomeCard(item) { navController.navigate(item.route) }
            }

            // Section: Other
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader("💼 ريادة الأعمال وبناء الذات")
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun HomeCard(item: HomeMenuItem, onClick: () -> Unit) {
    // Elegant Asymmetric Rounded Corners for editorial look!
    val asymmetricShape = RoundedCornerShape(
        topStart = 24.dp,
        bottomEnd = 24.dp,
        topEnd = 6.dp,
        bottomStart = 6.dp
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .bounceClick { onClick() }
            .border(1.dp, item.bgColor.copy(alpha = 0.5f), asymmetricShape),
        shape = asymmetricShape,
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 22.sp)
            }
            Column {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = item.contentColor
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = item.contentColor.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}
