package com.indolearn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * يعرض «جارٍ التحميل» أو «لا توجد بيانات» — لا يخلط بينهما.
 *
 * ── سبب وجود هذا المكوّن (عطل أبلغ عنه المستخدم) ──
 *
 * كانت كل شاشة محتوى تكتب:
 *
 *     if (list.isEmpty()) { CircularProgressIndicator() } else { ... }
 *
 * وهذا يفترض أن «القائمة فارغة» تعني دائماً «ما زلنا نحمّل».
 * وهو افتراض خاطئ: إذا كان الجدول فارغاً فعلاً (بذر لم يكتمل، أو لغة
 * بلا محتوى، أو بحث بلا نتائج) فإن الشرط يبقى صحيحاً إلى الأبد،
 * فتظل الدوّامة تدور ولا يفهم المستخدم ما يحدث.
 *
 * الآن التمييز صريح:
 *   - `isReady == false` ⇐ التحميل جارٍ فعلاً ⇐ دوّامة.
 *   - `isReady == true` وفارغ ⇐ رسالة مفهومة وسبب وحل.
 *
 * @param isReady هل انتهى تجهيز البيانات (بذر + قراءة أولى)؟
 */
@Composable
fun EmptyOrLoading(
    isReady: Boolean,
    emptyTitle: String,
    emptyHint: String,
    emptyEmoji: String = "📭"
) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        if (!isReady) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(emptyEmoji, fontSize = 44.sp)
                Text(
                    emptyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    emptyHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
