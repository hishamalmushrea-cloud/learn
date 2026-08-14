package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مرحلة في المنهج.
 *
 * ملاحظة على [isUnlocked]: القيمة الافتراضية `true` بقرار صريح من المستخدم —
 * المحتوى كله مفتوح ولا يوجد تقييد تدريجي. الحقل باقٍ في المخطط لأن
 * حذفه يتطلب ترحيل قاعدة بيانات بلا فائدة، ولإتاحة إعادة تفعيل
 * التدرّج مستقبلاً دون تغيير البنية.
 */
@Entity(tableName = "stages")
data class StageEntity(
    @PrimaryKey val id: Int,
    val titleAr: String,
    val titleId: String,
    val description: String,
    val level: Int,
    val isUnlocked: Boolean = true,
    val languageCode: String = "ID"
)
