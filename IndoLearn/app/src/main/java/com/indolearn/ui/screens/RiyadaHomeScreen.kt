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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.indolearn.data.local.riyada.RiyadaSection
import com.indolearn.ui.components.bounceClick
import com.indolearn.ui.theme.*
import com.indolearn.viewmodel.RiyadaViewModel

@Composable
fun RiyadaHomeScreen(navController: NavController, viewModel: RiyadaViewModel) {
    val dataState = viewModel.riyadaData.collectAsState().value

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        text = "دليل رائد الأعمال",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "💼 خطة 3 سنوات عملية",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "من موظف إلى صاحب مشروع ناجح",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        if (dataState == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dataState.SECTIONS.filter { it.id != "home" }) { section ->
                    RiyadaSectionCard(section) {
                        if (section.id == "plan") {
                            navController.navigate("riyada_plan")
                        } else {
                            navController.navigate("riyada_detail/${section.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiyadaSectionCard(section: RiyadaSection, onClick: () -> Unit) {
    val bgColor = when (section.color) {
        "c-teal" -> CardGreen
        "c-blue" -> CardBlue
        "c-amber" -> CardOrange
        "c-purple" -> CardPurple
        "c-green" -> CardGreen
        "c-cyan" -> CardBlue
        "c-orange" -> CardOrange
        "c-rose" -> CardPink
        else -> CardBlue
    }
    val contentColor = when (section.color) {
        "c-teal" -> OnCardGreen
        "c-blue" -> OnCardBlue
        "c-amber" -> OnCardOrange
        "c-purple" -> OnCardPurple
        "c-green" -> OnCardGreen
        "c-cyan" -> OnCardBlue
        "c-orange" -> OnCardOrange
        "c-rose" -> OnCardPink
        else -> OnCardBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .bounceClick(onClick = onClick)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = bgColor.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(contentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = section.num, fontSize = 24.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
