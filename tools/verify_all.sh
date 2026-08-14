#!/usr/bin/env bash
# بوابة الجودة الكاملة لمشروع IndoLearn.
#
# ⚠️ اقرأ هذا قبل الاعتماد على النتيجة:
# هذه البوابة **ليست بديلاً عن بناء Android**. لا يمكن تشغيل Gradle في بيئة
# التطوير المستخدمة لأن مستودعات Google/Maven/Gradle محجوبة عن الشبكة.
# ما تتحقق منه هذه البوابة فعلياً:
#   1. سلامة بنية كل ملفات Kotlin (أقواس/تعليقات/سلاسل).
#   2. صحة عدد وسائط بواني الكيانات في بيانات البذر.
#   3. تطابق كل مراجع DAO/المستودع/ViewModel/الشاشات/التنقل.
#   4. سلامة بيانات المحتوى (مفاتيح، روابط، وسم اللغة، صحة الأسئلة).
#   5. نقاء اللغة (عدم تسرب كلمات بين الإندونيسية والتركية والعربية).
#   6. تشغيل فعلي لمحلل Markdown على ملفات الموسوعة الـ27.
#   7. تشغيل فعلي لاختبار SeedManager (البذر مرة واحدة، ذرّي، بلا سباق).
#   8. تشغيل فعلي لاختبارات محرك التعلم (20 اختباراً) عبر kotlinc + JDK.
#
# ما لا تتحقق منه: تجميع Compose/Room/Hilt، وتوليد كود Room، واختبارات الأجهزة.

set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

fail=0
step() {
  echo
  echo "══════════════════════════════════════════════════════════════"
  echo "▶ $1"
  echo "══════════════════════════════════════════════════════════════"
}

step "1/11 الفحص البنيوي لملفات Kotlin"
python3 tools/validate_structure.py IndoLearn/app/src/main/java || fail=1

step "2/11 عدد وسائط بواني الكيانات"
python3 tools/validate_arity.py || fail=1

step "3/11 المراجع المتقاطعة (DAO / Repository / ViewModel / Navigation)"
python3 tools/validate_refs.py || fail=1

step "4/11 سلامة بيانات المحتوى"
python3 tools/validate_seed.py || fail=1

step "5/11 محاكاة قاعدة البيانات (هل تصل البيانات للشاشات؟)"
python3 tools/simulate_db.py || fail=1

step "6/11 نقاء اللغة"
python3 tools/validate_language.py || fail=1

step "7/11 تدقيق المنهج (هل عنوان الدرس يطابق محتواه؟)"
python3 tools/validate_curriculum.py || fail=1

step "8/11 تدقيق المحتوى التعليمي (تصنيف، تكرار، أمثلة)"
python3 tools/audit_content.py --strict || fail=1

step "9/11 اختبار محلل Markdown على ملفات الموسوعة (تنفيذ فعلي)"
if [ -x "/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java" ] || command -v java >/dev/null 2>&1; then
  ./tools/run_markdown_spec.sh || fail=1
else
  echo "⚠ تخطٍّ: JDK غير متوفر (تخطٍّ صريح، لا يُحتسب نجاحاً)"
fi

step "10/11 اختبار SeedManager (تنفيذ فعلي)"
if [ -x "/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java" ] || command -v java >/dev/null 2>&1; then
  ./tools/run_seed_spec.sh || fail=1
else
  echo "⚠ تخطٍّ: JDK غير متوفر (تخطٍّ صريح، لا يُحتسب نجاحاً)"
fi

step "11/11 اختبارات محرك التعلم (تنفيذ فعلي)"
if command -v java >/dev/null 2>&1 || [ -x "/tmp/jvenv/lib/python3.11/site-packages/jdk4py/java-runtime/bin/java" ]; then
  ./tools/run_engine_spec.sh || fail=1
else
  echo "⚠ تخطٍّ: JDK غير متوفر. راجع tools/run_engine_spec.sh لتعليمات التثبيت."
  echo "  (هذا تخطٍّ صريح — ولا يُحتسب نجاحاً)"
fi

echo
echo "══════════════════════════════════════════════════════════════"
if [ "$fail" -eq 0 ]; then
  echo "✅ اجتازت جميع البوابات القابلة للتنفيذ في هذه البيئة"
  echo "   (تذكير: بناء Android/Gradle لم يُنفَّذ — الشبكة محجوبة)"
else
  echo "❌ فشلت بوابة واحدة أو أكثر"
fi
echo "══════════════════════════════════════════════════════════════"
exit "$fail"
