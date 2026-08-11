package com.indolearn.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.indolearn.ui.theme.*

// Country Model for Administrative Mapping
data class ArabCountry(
    val id: Int,
    val flag: String,
    val name: String,
    val officialName: String,
    val capital: String,
    val divisionLevel1: String, // e.g. محافظة, ولاية, إقليم
    val divisionLevel2: String, // e.g. مديرية, دائرة, بلدية
    val coreUnits: List<String>, // list of key regions/governhorates
    val coreCities: List<String>,
    val confidence: String, // "HIGH", "MEDIUM"
    val docYear: String // "2026"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaScreen(navController: NavController) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("📋 الخطة المنهجية", "🌐 خريطة الـ 22 دولة")

    val arabCountries = remember {
        listOf(
            ArabCountry(1, "🇾🇪", "اليمن", "الجمهورية اليمنية", "صنعاء", "محافظة", "مديرية", listOf("صنعاء", "عدن", "تعز", "حضرموت", "الحديدة", "إب", "أبين", "المهرة"), listOf("صنعاء", "عدن", "تعز", "المكلا", "سيئون", "إب", "الغيظة"), "HIGH", "2026"),
            ArabCountry(2, "🇸🇦", "السعودية", "المملكة العربية السعودية", "الرياض", "منطقة إدارية", "محافظة / مركز", listOf("الرياض", "مكة المكرمة", "المدينة المنورة", "المنطقة الشرقية", "عسير", "تبوك", "القصيم"), listOf("الرياض", "جدة", "مكة", "المدينة", "الدمام", "أبها", "تبوك"), "HIGH", "2026"),
            ArabCountry(3, "🇦🇪", "الإمارات", "الإمارات العربية المتحدة", "أبوظبي", "إمارة", "منطقة / بلدية", listOf("أبوظبي", "دبي", "الشارقة", "عجمان", "أم القيوين", "رأس الخيمة", "الفجيرة"), listOf("أبوظبي", "دبي", "الشارقة", "العين", "خورفكان", "دبا الفجيرة"), "HIGH", "2026"),
            ArabCountry(4, "🇴🇲", "عُمان", "سلطنة عُمان", "مسقط", "محافظة", "ولاية", listOf("مسقط", "ظفار", "مسندم", "الداخلية", "الباطنة شمال", "الباطنة جنوب", "الشرقية شمال"), listOf("مسقط", "صلالة", "خصب", "نزوى", "صحار", "صور"), "HIGH", "2026"),
            ArabCountry(5, "🇶🇦", "قطر", "دولة قطر", "الدوحة", "بلدية", "منطقة إدارية", listOf("الدوحة", "الريان", "الخور والذخيرة", "الوكرة", "الضعاين", "الشيحانية", "الشمال"), listOf("الدوحة", "الريان", "الخور", "الوكرة", "الرويس"), "HIGH", "2026"),
            ArabCountry(6, "🇧🇭", "البحرين", "مملكة البحرين", "المنامة", "محافظة", "منطقة", listOf("العاصمة", "المحرق", "الشمالية", "الجنوبية"), listOf("المنامة", "المحرق", "البديع", "الرفاع", "مدينة حمد"), "HIGH", "2026"),
            ArabCountry(7, "🇰🇼", "الكويت", "دولة الكويت", "الكويت", "محافظة", "منطقة / حارة", listOf("العاصمة", "حولي", "الأحمدي", "الجهراء", "الفروانية", "مبارك الكبير"), listOf("الكويت", "حولي", "الفحيحيل", "الجهراء", "السالمية"), "HIGH", "2026"),
            ArabCountry(8, "🇮🇶", "العراق", "جمهورية العراق", "بغداد", "محافظة", "قضاء / ناحية", listOf("بغداد", "البصرة", "نينوى", "أربيل", "النجف", "كربلاء", "الأنبار", "السليمانية"), listOf("بغداد", "البصرة", "الموصل", "أربيل", "النجف", "كربلاء", "الفلوجة"), "HIGH", "2026"),
            ArabCountry(9, "🇯🇴", "الأردن", "المملكة الأردنية الهاشمية", "عمان", "محافظة", "لواء / قضاء", listOf("عمان", "إربد", "البلقاء", "الكرك", "الزرقاء", "العقبة", "مأدبا", "المفرق"), listOf("عمان", "إربد", "السلط", "الكرك", "الزرقاء", "العقبة"), "HIGH", "2026"),
            ArabCountry(10, "🇵🇸", "فلسطين", "دولة فلسطين", "القدس", "محافظة", "بلدية / قرية", listOf("القدس", "غزة", "رام الله والبيرة", "نابلس", "الخليل", "بيت لحم", "جنين", "أريحا"), listOf("القدس", "غزة", "رام الله", "نابلس", "الخليل", "بيت لحم", "جنين"), "HIGH", "2026"),
            ArabCountry(11, "🇸🇾", "سوريا", "الجمهورية العربية السورية", "دمشق", "محافظة", "منطقة / ناحية", listOf("دمشق", "ريف دمشق", "حلب", "حمص", "حماة", "اللاذقية", "طرطوس", "دير الزور"), listOf("دمشق", "حلب", "حمص", "حماة", "اللاذقية", "طرطوس"), "HIGH", "2026"),
            ArabCountry(12, "🇱🇧", "لبنان", "الجمهورية اللبنانية", "بيروت", "محافظة", "قضاء / بلدية", listOf("بيروت", "جبل لبنان", "الشمال", "الجنوب", "البقاع", "النبطية", "عكار"), listOf("بيروت", "طرابلس", "صيدا", "زحلة", "النبطية", "بعلبك"), "HIGH", "2026"),
            ArabCountry(13, "🇲🇪", "مصر", "جمهورية مصر العربية", "القاهرة", "محافظة", "مركز / قسم / بلدية", listOf("القاهرة", "الجيزة", "الإسكندرية", "القليوبية", "الدقهلية", "الشرقية", "البحر الأحمر", "أسوان"), listOf("القاهرة", "الجيزة", "الإسكندرية", "المنصورة", "الزقازيق", "الغردقة", "أسوان"), "HIGH", "2026"),
            ArabCountry(14, "🇸🇩", "السودان", "جمهورية السودان", "الخرطوم", "ولاية", "محلية", listOf("الخرطوم", "الجزيرة", "البحر الأحمر", "شمال دارفور", "جنوب كردفان", "كسلا", "النيل الأبيض"), listOf("الخرطوم", "أم درمان", "ود مدني", "بورتسودان", "الفاشر", "كادوقلي", "كسلا"), "HIGH", "2026"),
            ArabCountry(15, "🇱🇾", "ليبيا", "دولة ليبيا", "طرابلس", "شعبية / بلدية", "محلة / حارة", listOf("طرابلس", "بنغازي", "مصراتة", "الزاوية", "سبها", "الجبل الأخضر", "الواحات"), listOf("طرابلس", "بنغازي", "مصراتة", "الزاوية", "سبها", "البيضاء"), "HIGH", "2026"),
            ArabCountry(16, "🇹🇳", "تونس", "الجمهورية التونسية", "تونس", "ولاية", "معتمدية / عمادة", listOf("تونس", "أريانة", "بن عروس", "سوسة", "صفاقس", "بنزرت", "القيروان", "نابل"), listOf("تونس", "سوسة", "صفاقس", "بنزرت", "القيروان", "نابل"), "HIGH", "2026"),
            ArabCountry(17, "🇩🇿", "الجزائر", "الجمهورية الجزائرية الديمقراطية الشعبية", "الجزائر", "ولاية", "دائرة / بلدية", listOf("الجزائر", "وهران", "قسنطينة", "عنابة", "تلمسان", "غرداية", "تمنراست", "ورقلة"), listOf("الجزائر", "وهران", "قسنطينة", "عنابة", "تلمسان", "غرداية"), "HIGH", "2026"),
            ArabCountry(18, "🇲🇦", "المغرب", "المملكة المغربية", "الرباط", "جهة", "إقليم / عمالة / جماعة", listOf("الرباط-سلا-القنيطرة", "الدار البيضاء-سطات", "مراكش-آسفي", "طنجة-تطوان-الحسيمة", "فاس-مكناس", "سوس-ماسة"), listOf("الرباط", "الدار البيضاء", "مراكش", "طنجة", "فاس", "مكناس", "أكادير"), "HIGH", "2026"),
            ArabCountry(19, "🇲🇷", "موريتانيا", "الجمهورية الإسلامية الموريتانية", "نواكشوط", "ولاية", "مقاطعة / بلدية", listOf("نواكشوط الشمالية", "نواكشوط الغربية", "نواكشوط الجنوبية", "داخلت نواذيبو", "الحوض الشرقي", "الترارزة"), listOf("نواكشوط", "نواذيبو", "النعمة", "روصو", "أطار", "كيفه"), "HIGH", "2026"),
            ArabCountry(20, "🇸🇴", "الصومال", "جمهورية الصومال الفيدرالية", "مقديشو", "إقليم", "مديرية / قرية", listOf("بنادر", "باري", "نوجال", "مدج", "جدو", "هيران", "شبيلي الوسطى"), listOf("مقديشو", "بوساسو", "غاروي", "جالكعيو", "بلد وين"), "HIGH", "2026"),
            ArabCountry(21, "🇩🇯", "جيبوتي", "جمهورية جيبوتي", "جيبوتي", "إقليم", "دائرة إدارية", listOf("جيبوتي العاصمة", "عرتا", "دخيل", "تاجورة", "أوبوخ", "علي صبيح"), listOf("جيبوتي", "عرتا", "دخيل", "تاجورة", "أوبوخ", "علي صبيح"), "HIGH", "2026"),
            ArabCountry(22, "🇰🇲", "جزر القمر", "الاتحاد القمري", "موروني", "جزيرة حكم ذاتي", "بلدية", listOf("القمر الكبرى", "أنجوان", "موهيلي"), listOf("موروني", "موتسامودو", "فومبوني"), "HIGH", "2026")
        )
    }

    var expandedCountryId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗺️ الموسوعة العربية الشاملة") },
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
                .padding(16.dp)
        ) {
            // Screen Tab Selection
            TabRow(
                selectedTabIndex = currentSubTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentSubTab == index,
                        onClick = { currentSubTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (currentSubTab) {
                    0 -> ResearchPlanTab()
                    1 -> CountriesGridTab(arabCountries, expandedCountryId) { id ->
                        expandedCountryId = if (expandedCountryId == id) null else id
                    }
                }
            }
        }
    }
}

// ==========================================
// === TAB 1: EXECUTIVE RESEARCH METHODOLOGY ===
// ==========================================

@Composable
fun ResearchPlanTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "📋 الخطة التنفيذية لمشروع الموسوعة العربية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "مشروع بحثي موسوعي شامل يهدف لربط الجغرافيا، التاريخ، المجتمع، واللهجات العربية من الدولة وحتى أصغر وحدة إدارية محلية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📐 التسلسل الهرمي الإداري الإجباري", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "الوطن العربي ➔ الدولة ➔ العاصمة ➔ الأقاليم والولايات والمحافظات والجهات (بالمسميات الرسمية المحلية) ➔ المدن والمراكز والدوائر ➔ المديريات والبلديات والمعتمديات والبلدات ➔ القرى ➔ الأحياء ➔ الحارات والتجمعات السكانية الصغيرة الموثقة.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("💡 معايير مصداقية وتصنيف المعلومات (Confidence Levels)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟢", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("موثقة بدرجة عالية جداً (سجلات رسمية، وزارات، أبحاث جامعية محكّمة).", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟡", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("موثقة لكن توجد اختلافات بين المصادر (تذكر الآراء بالتفصيل).", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟠", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("معلومات محلية أو تاريخية تحتاج حذراً ودراسة نقدية دقيقة.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardOrange)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📖 الأبعاد الـ 20 لملف الدولة والأقاليم", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = OnTertiaryContainerLight)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "الموقع الجغرافي الدقيق • الحدود البرية والبحرية • التضاريس والجغرافيا الطبيعية • التربة والغطاء الزراعي • المناخ والبيئة الطبيعية • الخط الزمني التاريخي المحقق • الشعوب والقبائل والعشائر • الثقافة والفنون والرقص الشعبي • الملابس التقليدية بقطعها المحلية • الأطباق والحلويات والمشروبات المرتبطة بمناطقها • اللهجات وتصنيفاتها وكلماتها اليومية التوضيحية • العادات والتقاليد بآدابها الاجتماعية • المعالم والمواقع الأثرية بالتفصيل.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnTertiaryContainerLight
                    )
                }
            }
        }
    }
}

// ==========================================
// === TAB 2: INTERACTIVE COUNTRIES GRID ===
// ==========================================

@Composable
fun CountriesGridTab(
    countries: List<ArabCountry>,
    expandedId: Int?,
    onExpandToggle: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "🗺️ خريطة التقسيمات الإدارية للـ 22 دولة عربية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "اضغط على أي دولة لتفجر وتستعرض خريطتها الإدارية والمسميات الرسمية المعتمدة لوحداتها:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(4.dp))
        }

        itemsIndexed(countries) { index, country ->
            val isExpanded = expandedId == country.id
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle(country.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(country.flag, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(country.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(country.officialName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        
                        // Confidence Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CorrectGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🟢 موثّق", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(Modifier.height(12.dp))

                            // Details Grid
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text("🏛️ العاصمة الرسمية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    Text(country.capital, style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("🕒 سنة التوثيق البحثي:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    Text(country.docYear, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Local Administrative Terms
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("🛠️ المسميات الإدارية الرسمية المعتمدة محلياً:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(6.dp))
                                    Text("• الوحدة الأولى (المستوى الأول): ${country.divisionLevel1}", style = MaterialTheme.typography.bodyMedium)
                                    Text("• الوحدة الثانية (المستوى الثاني): ${country.divisionLevel2}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Core regions list
                            Text("🗺️ المحافظات / الولايات / الأقاليم الكبرى:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = country.coreUnits.joinToString(" • "),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )

                            // Core Cities list
                            Text("🏙️ المدن والمراكز الرئيسية:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(
                                text = country.coreCities.joinToString(" • "),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "💡 اضغط على الكارت للطي مجدداً",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
