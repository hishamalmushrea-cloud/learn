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

// Enriched Country Model representing an Editorial Reference Book
data class EnrichedCountry(
    val id: Int,
    val flag: String,
    val name: String,
    val officialName: String,
    val capital: String,
    val divisionLevel1: String, // e.g. محافظة, ولاية
    val divisionLevel2: String, // e.g. مديرية, معتمدية
    val coreUnits: List<String>,
    val coreCities: List<String>,
    
    // Breathtakingly deep encyclopedic details (The Reference Book chapters)
    val geography: String,
    val history: String,
    val societyAndDialect: String,
    val dialectVocabulary: List<Triple<String, String, String>>, // Word, Pronunciation, Meaning
    val traditionalFood: String,
    val traditionalClothing: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncyclopediaScreen(navController: NavController) {
    val context = LocalContext.current
    var currentSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("📋 الدليل والأهداف", "🌐 الموسوعة العربية الشاملة")

    // Extensive Editorial Dataset for primary representative Arab countries
    val enrichedCountries = remember {
        listOf(
            EnrichedCountry(
                id = 1,
                flag = "🇾🇪",
                name = "اليمن",
                officialName = "الجمهورية اليمنية",
                capital = "صنعاء",
                divisionLevel1 = "محافظة (21 محافظة)",
                divisionLevel2 = "مديرية (333 مديرية)",
                coreUnits = listOf("صنعاء", "عدن", "تعز", "حضرموت", "الحديدة", "إب", "شبوة", "المهرة"),
                coreCities = listOf("صنعاء", "عدن", "تعز", "المكلا", "سيئون", "إب", "الحديدة", "الغيظة"),
                geography = "يقع اليمن في جنوب غرب شبه الجزيرة العربية، ويطل برياً على معبر باب المندب الاستراتيجي وبحرياً على البحر الأحمر وبحر العرب. تحده السعودية شمالاً وعمان شرقاً. يتميز بتضاريس جبلية شاهقة (مثل جبال السراة وحراز وجبل النبي شعيب الأعلى في الجزيرة العربية) ووديان خصبة ممتدة كواد حضرموت، وصحراء الربع الخالي شمالاً، وسواحل رملية دافئة ممتدة.",
                history = "اليمن هو مهد الحضارات القديمة في جنوب الجزيرة؛ حيث شهد قيام ممالك سبأ ومعين وقتبان وحضرموت وحمير التي اشتهرت بسد مأرب وتجارة البخور واللبان. دخلت اليمن الإسلام في القرن السابع الهجري، وتوالت عليها الدول المستقلة كالدولة الزيادية والصليحية والرسولية. شهد التاريخ الحديث قيام ثورة 26 سبتمبر و14 أكتوبر وتحقيق الوحدة اليمنية عام 1990.",
                societyAndDialect = "المجتمع اليمني قبلي ومترابط للغاية، تسود فيه قيم الكرم والضيافة والشهامة والترابط الأسري والجيرة. تتنوع اللهجات اليمنية بدقة شديدة بين لهجة صنعانية (تتميز بنطق القاف كحرف g الإنجليزي)، ولهجة تعزية وصنعانية خفيفة، ولهجة حضرمية (تتميز بنطق الجيم ياءً في بعض المناطق)، ولهجة عدنية وتهامية ومهرية قديمة عريقة.",
                dialectVocabulary = listOf(
                    Triple("سلامات / الله معك", "Selamat", "ترحيب ووداع يومي مألوف"),
                    Triple("سِيدَا", "Seeda", "امشِ للأمام مباشرة وبخط مستقيم"),
                    Triple("مَالِي؟", "Mali", "ماذا بي؟ أو ما خطبي؟"),
                    Triple("شاقي", "Shaqi", "العامل المجتهد والمكافح")
                ),
                traditionalFood = "الأطباق الوطنية اليمنية مشهورة عالمياً وعريقة: السلتة (الطبق الوطني الأول، يطبخ في مقلى حرضة من الطين ويحتوي على مرق ولحم وحلبة رغوية)، والفتة، والمندي والمظبي والحنيذ (طرق طهي اللحم تحت الأرض أو فوق الجمر)، وخبز الملوج والرشوش الساخن، ومشروب القشر (من قشور القهوة والزنجبيل).",
                traditionalClothing = "الملابس التقليدية تتنوع بين المناطق: يرتدي الرجال الصنعانيون الثوب والجنبية (الخنجر اليماني العريق) على حزام مطرز بالزري مع الشال الصنعاني المطرز على الرأس. ويرتدي رجال تهامة والسواحل المعوز (إزار منسوج يدوياً)، بينما ترتدي النساء الستارة الصنعانية الملونة، أو الدرع العدني الخفيف، أو الثوب التهامي المطرز."
            ),
            EnrichedCountry(
                id = 2,
                flag = "🇸🇦",
                name = "السعودية",
                officialName = "المملكة العربية السعودية",
                capital = "الرياض",
                divisionLevel1 = "منطقة إدارية (13 منطقة)",
                divisionLevel2 = "محافظة (فئة أ / ب) ثم مركز",
                coreUnits = listOf("الرياض", "مكة المكرمة", "المدينة المنورة", "المنطقة الشرقية", "عسير", "تبوك", "القصيم", "حائل"),
                coreCities = listOf("الرياض", "جدة", "مكة المكرمة", "المدينة المنورة", "الدمام", "أبها", "تبوك", "بريدة"),
                geography = "تحتل السعودية الجزء الأكبر من شبه الجزيرة العربية وتتمتع بحدود جغرافية واسعة وتضاريس غنية. تطل غرباً على البحر الأحمر حيث جبال الحجاز وعسير الشاهقة وسهول تهامة الساحلية، وشرقاً على الخليج العربي. يتوسطها هضبة نجد الصحراوية وتغطيها صحارى شاسعة مثل صحراء النفود الكبير وصحراء الدهناء وصحراء الربع الخالي الجنوبية الغنية بالكثبان الرملية الذهبية.",
                history = "بدأ التاريخ الحديث بتأسيس الدولة السعودية الأولى عام 1727 في الدرعية على يد الإمام محمد بن سعود. تلاها تأسيس الدولة السعودية الثانية، ثم استعاد الملك عبد العزيز بن عبد الرحمن آل سعود الرياض عام 1902 ووحد أرجاء البلاد ليعلن تأسيس المملكة العربية السعودية المعاصرة عام 1932. شهدت البلاد نمواً اقتصادياً هائلاً بعد اكتشاف النفط لتتحول إلى قطب اقتصادي وديني وعالمي.",
                societyAndDialect = "يتسم المجتمع السعودي بالتمسك بالقيم الإسلامية والأعراف العربية الأصيلة، كالكرم المفرط وحسن استقبال الضيوف والولاء الأسري والترابط الاجتماعي الكثيف. تتنوع اللهجات بين اللهجة النجدية (لهجة وسط المملكة)، واللهجة الحجازية (الغربية المتميزة برقتها وسرعتها)، واللهجة الشرقية، واللهجة الجنوبية (العسيرية والتهامية الغنية بالمفردات العربية القديمة).",
                dialectVocabulary = listOf(
                    Triple("وش لونك؟ / كيف حالك؟", "Wesh lounak?", "السؤال الشائع عن الحال"),
                    Triple("زيّن", "Zein", "جيد / ممتاز / حسن المظهر"),
                    Triple("يا بعد حيي", "Ya baad hayyi", "تعبير حائلي حميم يعني يا بعد أهلي"),
                    Triple("تكفى", "Tekfa", "أرجوك أو أسألك بالله للمساعدة")
                ),
                traditionalFood = "الكبسة هي الطبق الوطني الأشهر في جميع أرجاء المملكة (أرز مطبوخ ببهارات خاصة مع لحم الضأن أو الدجاج)، والجرش (قمح مجروش مطبوخ باللبن والبهارات)، والمطازيز، والمرقوق، والحنيذ والمندي في المنطقة الجنوبية، وحلوى الحنيني، والمقشوش التأسيسي، والقهوة السعودية الأصيلة بالهيل والزعفران التي تقدم مع التمور الفاخرة.",
                traditionalClothing = "يرتدي الرجال الثوب الأبيض الفضفاض مع الشماغ الأحمر أو الغترة البيضاء، ويثبت بالعقال الأسود، وفي المناسبات الرسمية يرتدون البشت المطرز بالخيوط الذهبية (الزري). وترتدي النساء العباءة السوداء الأنيقة، مع تصاميم مطرزة بالخيوط والخرز الملون في اللباس التقليدي التراثي مثل الثوب العسيري المطرز وثوب النشل."
            ),
            EnrichedCountry(
                id = 3,
                flag = "🇲🇪",
                name = "مصر",
                officialName = "جمهورية مصر العربية",
                capital = "القاهرة",
                divisionLevel1 = "محافظة (27 محافظة)",
                divisionLevel2 = "مركز / قسم / بلدية",
                coreUnits = listOf("القاهرة", "الجيزة", "الإسكندرية", "القليوبية", "الدقهلية", "الشرقية", "الغربية", "أسوان"),
                coreCities = listOf("القاهرة", "الجيزة", "الإسكندرية", "المنصورة", "طنطا", "الزقازيق", "أسوان", "الأقصر"),
                geography = "تقع مصر في الشمال الشرقي لقارة أفريقيا، وتتمتع بموقع محوري يربط بين قارتي أفريقيا وآسيا عبر شبه جزيرة سيناء وقناة السويس الاستراتيجية. تطل شمالاً على البحر المتوسط وشرقاً على البحر الأحمر. يقطعها من الجنوب إلى الشمال نهر النيل العظيم واهب الحياة والحضارة، ومحيطها الصحراوي ينقسم لصحراء غربية ممتدة وصحراء شرقية جبلية صلبة.",
                history = "تمتد حضارة مصر لأكثر من 5000 عام بدءاً من توحيد القطرين على يد الملك مينا وتأسيس الأسر الفرعونية القديمة مشيدة الأهرامات العظيمة والمعابد. شهدت مصر العصر اليوناني والروماني، ثم الفتح الإسلامي في القرن السابع لتتحول إلى عاصمة الدول الإسلامية كالدولة الفاطمية والأيوبية والمملوكية. في العصر الحديث، قاد محمد علي باشا نهضة مصر وتأسيس الدولة الحديثة.",
                societyAndDialect = "المجتمع المصري حيوي، متماسك، ومشهور بخفة الظل والود والترحاب والترابط العائلي الكثيف في الحارات والأرياف والمجتمعات الساحلية. اللهجة المصرية (القاهرية) هي اللهجة الأكثر انتشاراً وفهماً في الوطن العربي بفضل السينما والإعلام، وتتفرع للهجة إسكندرانية ساحلية، ولهجة صعيدية عريقة وقوية المخارج في الصعيد الجنوبي.",
                dialectVocabulary = listOf(
                    Triple("إزيك؟ / عامل إيه؟", "Ezayyak?", "السؤال اليومي والشائع عن الحال"),
                    Triple("كويّس", "Kwayyes", "بخير / جيد / ممتاز"),
                    Triple("بجد؟ / والله؟", "Beged?", "السؤال للاستفسار والـتأكيد"),
                    Triple("جدع", "Gadaa", "الرجل الشهم الشجاع الوفي")
                ),
                traditionalFood = "الأطباق الوطنية المصرية غنية ومحبوبة: الكشري (مزيج من الأرز والمعكرونة والعدس والحمص والبصل المقرمش والصلصة بالثوم والخل)، والفول المدمس والطعمية (الركن الأساسي للفطور اليومي)، والملوخية بالأرانب أو الدجاج، والمحشي بجميع أنواعه، والفتة بالخل والثوم، والفسيخ والرنجة في الأعياد، وحلويات أم علي والبسبوسة.",
                traditionalClothing = "يرتدي رجال الريف والصعيد الجلباب البلدي الفضفاض المصنوع من القطن المصري الفاخر مع العمة البيضاء على الرأس والعباءة الصوفية للشتاء. وترتدي النساء في الأحياء الشعبية الجلباب المنزلي الملون المريح، بينما يشتهر الثوب السيناوي المطرز بخيوط الحرير الملونة وثوب الواحات في الواحات الغربية كفن تراثي أصيل."
            ),
            EnrichedCountry(
                id = 4,
                flag = "🇲🇦",
                name = "المغرب",
                officialName = "المملكة المغربية",
                capital = "الرباط",
                divisionLevel1 = "جهة (12 جهة)",
                divisionLevel2 = "عمالة / إقليم ثم جماعة ترابية",
                coreUnits = listOf("الرباط-سلا-القنيطرة", "الدار البيضاء-سطات", "مراكش-آسفي", "طنجة-تطوان-الحسيمة", "فاس-مكناس", "سوس-ماسة"),
                coreCities = listOf("الرباط", "الدار البيضاء", "مراكش", "طنجة", "فاس", "مكناس", "أكادير", "وجدة"),
                geography = "تقع المملكة المغربية في أقصى الشمال الغربي لأفريقيا، وتتميز بتنوع جغرافي فريد. تطل غرباً على المحيط الأطلسي وشمالاً على البحر الأبيض المتوسط ويفصلها مضيق جبل طارق عن أوروبا. يتوسطها سلاسل جبال الأطلس (الأطلس الكبير والمتوسط والصغير) وجبال الريف الشمالية، وسواحلها خصبة تمتد لتلتقي بالرموز الرملية الحارة للصحراء الكبرى بالجنوب.",
                history = "تمتد حضارة المغرب لعصور قديمة ارتبطت بالأمازيغ والفينيقيين والرومان. بدأ التاريخ الإسلامي المستقل للمغرب بتأسيس دولة الأدارسة عام 789م على يد المولى إدريس الأول، وتوالت على حكمه إمبراطوريات عظمى كالدولة المرابطية والموحدية والمرينية والسعدية وصولاً للدولة العلوية الشريفة. تميزت هذه الممالك ببسط نفوذها وتأسيس معالم الأندلس وحماية الثغور البحرية.",
                societyAndDialect = "المجتمع المغربي غني ومتنوع بالروافد الثقافية (الأمازيغية، العربية، الصحراوية، الأندلسية). تسود فيه قيم حسن الجوار وإكرام الضيف ومشاركة الشاي بالنعناع كطقس اجتماعي يومي أصيل. اللهجة المغربية (الدارجة) تتميز بدمج الكلمات العربية الأصيلة مع لمسات أمازيغية وفرنسية، وتتنوع بين لهجة شمالية، وفاسية، وجبلية، وحسانية بالجنوب الصحراوي.",
                dialectVocabulary = listOf(
                    Triple("لا بأس؟ / كيف حالك؟", "Labas?", "السؤال الشائع عن الصحة والحال"),
                    Triple("مزيان", "Mezyan", "بخير / جيد جداً / ممتاز"),
                    Triple("دابا", "Daba", "الآن / في هذه اللحظة فوراً"),
                    Triple("بزّاف", "Bezzaf", "كثيراً / جداً (مشتقة من بجزاف)")
                ),
                traditionalFood = "المطبخ المغربي مصنف كأحد أفضل المطابخ العالمية لعراقته وتناغم نكهاته: الكسكس بالخضار السبعة واللحم (الطبق الوطني المخصص للجمعة)، والطاجين بمختلف أنواعه (لحم بالبرقوق واللوز، دجاج بالزيتون والليمون المصير)، والبسطيلة الفاخرة (فطيرة مقرمشة محشوة بالدجاج واللوز والزعفران والقرفة)، وحساء الحريرة الدافئ.",
                traditionalClothing = "الملابس التقليدية المغربية فخمة ومشهورة بالخياطة اليدوية (المعلم والبرشمان): يرتدي الرجال والنساء الجلابة المغربية ذات القلنسوة مع البلغة والبرنس الصوفي للشتاء. وترتدي النساء في المناسبات القفطان المغربي والتكشيطة (فستان فخم من قطعتين مطرز بالصقلي الذهبي ومزين بحزام المضمة الفخم) كقطعة تراثية تنافس الموضة العالمية."
            )
        )
    }

    var expandedCountryId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 الموسوعة العربية الشاملة (v2)") },
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
                    0 -> DetailedResearchPlanTab()
                    1 -> EnrichedCountriesTab(enrichedCountries, expandedCountryId) { id ->
                        expandedCountryId = if (expandedCountryId == id) null else id
                    }
                }
            }
        }
    }
}

// ==========================================
// === TAB 1: DETAILED PEDAGOGICAL PLAN ===
// ==========================================

@Composable
fun DetailedResearchPlanTab() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "📋 خطة وتوجيهات بناء الموسوعة الأكاديمية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "تم صياغة المخطط بناءً على الـ 246 قاعدة لضمان الانتقال السلس للمتعلم من مرحلة الملاحظة إلى الطلاقة التلقائية.",
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
                    Text("💡 مبدأ التدرج المعرفي الحقيقي (القاعدة 5)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "التطبيق لا يحفظ الكلمات عشوائياً، بل ينقل المتعلم تدريجياً: رؤية موقف ➔ فهم معنى ➔ تدرج تصاعدي ➔ تطبيق تمرين ➔ إنتاج محادثة واقعية حية.",
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
                    Text("⚠️ مكافحة أخطاء التداخل الثقافي (Native Transfer)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "العقبة الكبرى لمتعلم اللغة هي نقل قواعد لغته الأم للغة الجديدة (مثل وضع الفعل بمنتصف الجملة التركية أو خلط النفي بالإندونيسية). يوفر الدليل رصداً وقائياً فورياً لهذه الأخطاء بتمارين تصحيح تفاعلية.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ==========================================
// === TAB 2: DETAILED REFERENCE BOOK CORES ===
// ==========================================

@Composable
fun EnrichedCountriesTab(
    countries: List<EnrichedCountry>,
    expandedId: Int?,
    onExpandToggle: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "🌐 مرجع جغرافية وتاريخ وثقافة الشعوب العربية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "تفصح الأقسام أدناه عن تفاصيل موسوعية غنية لكل دولة كأنك تقرأ كتاباً أكاديمياً مرجعياً فخماً:",
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
                        Text(country.flag, fontSize = 32.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(country.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(country.officialName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CorrectGreen)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("مرجع معتمد 🟢", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            Spacer(Modifier.height(12.dp))

                            // 1. Geography Section
                            Text("🗺️ الفصل الأول: الجغرافيا والموقع الجغرافي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(country.geography, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
                            Spacer(Modifier.height(12.dp))

                            // 2. History Section
                            Text("⌛ الفصل الثاني: التاريخ والخط الزمني للحضارات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(country.history, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
                            Spacer(Modifier.height(12.dp))

                            // 3. Society & Dialect
                            Text("🗣️ الفصل الثالث: المجتمع واللغة واللهجات الفرعية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(country.societyAndDialect, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
                            Spacer(Modifier.height(10.dp))

                            // Dialect Vocabulary List (Standard Word Representation - Rule 106)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("💬 كلمات مميزة من اللهجة المحلية السائدة:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.height(8.dp))
                                    country.dialectVocabulary.forEach { (word, pron, meaning) ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("• $word", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                            Spacer(Modifier.width(8.dp))
                                            Text("($pron)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            Spacer(Modifier.weight(1f))
                                            Text(meaning, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // 4. Food Section
                            Text("🍲 الفصل الرابع: المطبخ والأطباق الوطنية الشهيرة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(country.traditionalFood, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
                            Spacer(Modifier.height(12.dp))

                            // 5. Clothing Section
                            Text("👔 الفصل الخامس: الزي التقليدي والملابس التراثية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text(country.traditionalClothing, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Justify)
                            
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "💡 اضغط على كرت الدولة مجدداً للطي",
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
