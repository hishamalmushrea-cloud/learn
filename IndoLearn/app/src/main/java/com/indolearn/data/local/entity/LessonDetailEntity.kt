package com.indolearn.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * تفاصيل الدرس.
 *
 * ⚠️ ملاحظة مهمة حول تسمية الحقول (سبب عطل P0-1):
 * كان هذا الكيان يحتوي على حقل باسم `lessonId`، لكنه في بيانات البذر
 * كان يحمل فعلياً **مُعرِّف الوحدة (Unit)** وليس مُعرِّف الدرس.
 * بينما كان الحقل `id` هو المطابق الحقيقي لمُعرِّف الدرس.
 *
 * نتيجة ذلك أن `SELECT ... WHERE lessonId = :lessonId` كان:
 *  - يُرجع محتوى درس خاطئ لـ 21 درساً،
 *  - ولا يُرجع شيئاً إطلاقاً لـ 9 دروس (29→37) فتظهر "لم يتم العثور على الدرس".
 *
 * الإصلاح: أعيدت تسمية الحقل إلى `unitId` ليطابق معناه الحقيقي،
 * والاستعلام صار على `id`. التسمية الصادقة تمنع تكرار الخطأ.
 */
@Entity(
    tableName = "lesson_details",
    indices = [Index("unitId")]
)
data class LessonDetailEntity(
    /** مُعرِّف الدرس الذي تنتمي إليه هذه التفاصيل (يطابق `LessonEntity.id`). */
    @PrimaryKey val id: Int,
    /** مُعرِّف الوحدة التي ينتمي إليها الدرس (يطابق `UnitEntity.id`). */
    val unitId: Int,
    val explanation: String,
    val wordByWord: String,
    val sentenceStructure: String,
    val dailyUsage: String,
    val commonMistakes: String,
    val formalVsCasual: String
)
