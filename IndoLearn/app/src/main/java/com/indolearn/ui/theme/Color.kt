package com.indolearn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// === PREMIUM LIGHT THEME (Indigo, Emerald & Amber) ===
val PrimaryLight = Color(0xFF4F46E5)        // Premium Indigo Blue (Academic & Business Hybrid)
val OnPrimaryLight = Color.White
val PrimaryContainerLight = Color(0xFFEEF2FF) // Very soft Indigo tint
val OnPrimaryContainerLight = Color(0xFF312E81)

val SecondaryLight = Color(0xFF10B981)       // Emerald Green (Success & Growth)
val OnSecondaryLight = Color.White
val SecondaryContainerLight = Color(0xFFECFDF5) // Soft Emerald tint
val OnSecondaryContainerLight = Color(0xFF064E3B)

val TertiaryLight = Color(0xFFF59E0B)        // Amber/Gold (Rewards, Streaks & Achievements)
val OnTertiaryLight = Color.White
val TertiaryContainerLight = Color(0xFFFFFBEB) // Soft Amber tint
val OnTertiaryContainerLight = Color(0xFF78350F)

val BackgroundLight = Color(0xFFF8FAFC)      // Ultra-clean Slate White
val OnBackgroundLight = Color(0xFF0F172A)    // Deep Slate Blue for text
val SurfaceLight = Color.White
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)  // Soft Slate grey
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFF94A3B8)         // Slate outline
val OutlineVariantLight = Color(0xFFE2E8F0)  // Very soft border Slate
val ErrorLight = Color(0xFFEF4444)           // Rose Red for errors
val OnErrorLight = Color.White
val ErrorContainerLight = Color(0xFFFEF2F2)  // Soft Rose tint

// === PREMIUM DARK THEME (Deep Obsidian & Neon Accents) ===
val BackgroundDark = Color(0xFF0B0F19)       // Deep Obsidian Space Black
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceDark = Color(0xFF111827)          // Obsidian Grey
val OnSurfaceDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF1F2937)
val OnSurfaceVariantDark = Color(0xFF9CA3AF)

val PrimaryDark = Color(0xFF818CF8)          // Neon Indigo
val OnPrimaryDark = Color(0xFF1E1B4B)
val PrimaryContainerDark = Color(0xFF312E81)
val OnPrimaryContainerDark = Color(0xFFE0E7FF)

val SecondaryDark = Color(0xFF34D399)         // Neon Emerald
val OnSecondaryDark = Color(0xFF064E3B)
val SecondaryContainerDark = Color(0xFF065F46)
val OnSecondaryContainerDark = Color(0xFFA7F3D0)

val TertiaryDark = Color(0xFFFBBF24)          // Neon Amber
val OnTertiaryDark = Color(0xFF78350F)
val TertiaryContainerDark = Color(0xFF92400E)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

val OutlineDark = Color(0xFF4B5563)
val OutlineVariantDark = Color(0xFF374151)
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF7F1D1D)
val ErrorContainerDark = Color(0xFF991B1B)

// === CUSTOM ACCENT COLORS (Adaptive) ===
val GoldBadge: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFF59E0B)

val CorrectGreen: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF34D399) else Color(0xFF10B981)

val WrongRed: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF87171) else Color(0xFFEF4444)

val CardBlue: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF172554) else Color(0xFFEFF6FF)

val CardGreen: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF022C22) else Color(0xFFECFDF5)

val CardOrange: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF451A03) else Color(0xFFFFFBEB)

val CardPurple: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2E1065) else Color(0xFFFAF5FF)

val CardPink: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF500724) else Color(0xFFFDF2F8)

val OnCardBlue: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFDBEAFE) else Color(0xFF1E40AF)

val OnCardGreen: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFD1FAE5) else Color(0xFF065F46)

val OnCardOrange: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFEF3C7) else Color(0xFF92400E)

val OnCardPurple: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFEDE9FE) else Color(0xFF5B21B6)

val OnCardPink: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFCE7F3) else Color(0xFF9D174D)
