package com.indolearn.domain.quiz

import java.text.Normalizer
import java.util.Locale

/**
 * تقييم إجابات الكتابة بصورة حتمية وآمنة لكل اللغات المدعومة.
 *
 * لا تستخدم مطابقة substring: كانت الإجابة `miyor` تُقبل مكان
 * `Bilmiyorum` لمجرد أنها جزء منها، وهو نجاح زائف يفسد نتيجة المتعلم.
 * نحذف علامات الترقيم ونوحّد المسافات والحالة فقط، مع الإبقاء على
 * الحروف التركية (ç, ğ, ı, ö, ş, ü) لأنها حروف ذات معنى وليست زخرفة.
 * يمكن للمحتوى إعلان بدائل صحيحة صراحة بالفاصل `|`.
 */
object AnswerEvaluator {
    fun isCorrect(userAnswer: String, expected: String): Boolean {
        val user = normalize(userAnswer)
        if (user.isEmpty()) return false
        return expected.split('|').any { normalize(it) == user }
    }

    fun normalize(value: String): String {
        val canonical = Normalizer.normalize(value, Normalizer.Form.NFC)
            .lowercase(Locale.ROOT)
        return canonical
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
