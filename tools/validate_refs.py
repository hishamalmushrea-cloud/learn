#!/usr/bin/env python3
"""
مُدقِّق المراجع المتقاطعة في IndoLearn.

لماذا: لا يمكن تشغيل مترجم Kotlin على كود Android في هذه البيئة
(مستودعات Gradle/Google محجوبة)، فلا توجد شبكة أمان تلتقط
استدعاء دالة غير معرَّفة أو مساراً بلا وجهة.

يفحص هذا السكربت:
  1. كل `db.xxxDao().method()` في المستودع ⇦ معرَّف في الـ DAO المقابل.
  2. كل `repository.method()` في ViewModel ⇦ معرَّف في المستودع.
  3. كل `viewModel.member` في الشاشات ⇦ معرَّف في ViewModel الصحيح.
  4. كل `navigate("route")` ⇦ له `composable("route")`.
  5. المسارات المُعرَّفة بلا أي وسيلة وصول.

الخروج بقيمة غير صفرية عند وجود خطأ.
"""

import os
import re
import sys
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "IndoLearn", "app", "src", "main", "java", "com", "indolearn")

errors: list[str] = []


def read(p: str) -> str:
    with open(p, encoding="utf-8") as f:
        return f.read()


def kt_files(sub: str):
    base = os.path.join(SRC, sub)
    for root, _, files in os.walk(base):
        for f in files:
            if f.endswith(".kt"):
                yield os.path.join(root, f)


# ---- 1. DAO methods ----
dao_methods = defaultdict(set)
for p in kt_files("data/local/dao"):
    s = read(p)
    m = re.search(r"interface (\w+)", s)
    if not m:
        continue
    dao_methods[m.group(1)] = set(re.findall(r"fun (\w+)\(", s))

db_src = read(os.path.join(SRC, "data/local/AppDatabase.kt"))
accessors = dict(re.findall(r"abstract fun (\w+)\(\): (\w+)", db_src))

repo_path = os.path.join(SRC, "data/repository/LearnRepository.kt")
repo_src = read(repo_path)
for m in re.finditer(r"db\.(\w+)\(\)\.(\w+)\(", repo_src):
    acc, meth = m.group(1), m.group(2)
    if acc not in accessors:
        errors.append(f"LearnRepository: لا يوجد accessor باسم db.{acc}()")
    elif meth not in dao_methods.get(accessors[acc], set()):
        errors.append(f"LearnRepository: {accessors[acc]}.{meth}() غير معرَّفة")

# ---- 2. repository methods used by viewmodels ----
# المشروع صار يضم أكثر من مستودع (LearnRepository, RiyadaRepository, ...).
# كان الفحص يقرأ LearnRepository وحده، فيبلّغ خطأً كاذباً عن كل ViewModel
# يحقن مستودعاً آخر. نجمع الدوال من كل ملفات data/repository.
repo_funs = set()
_repo_dir = os.path.join(SRC, "data/repository")
if os.path.isdir(_repo_dir):
    for _f in sorted(os.listdir(_repo_dir)):
        if _f.endswith(".kt"):
            repo_funs |= set(re.findall(
                r"fun (\w+)\(", read(os.path.join(_repo_dir, _f))))

# ---- 3. viewmodel members (only the *ViewModel classes, not helper data classes) ----
vm_members = defaultdict(set)
for p in kt_files("viewmodel"):
    s = read(p)
    for cls in re.findall(r"class (\w*ViewModel)\b", s):
        # members declared anywhere in the file's ViewModel class body
        vm_members[cls] |= set(re.findall(r"fun (\w+)\(", s))
        vm_members[cls] |= set(re.findall(r"va[lr] (\w+)\s*[:=]", s))
    for m in re.finditer(r"repository\.(\w+)\(", s):
        if m.group(1) not in repo_funs:
            errors.append(f"{os.path.basename(p)}: repository.{m.group(1)}() غير معرَّفة")

for p in kt_files("ui"):
    s = read(p)
    m = re.search(r"viewModel:\s*(\w+ViewModel)", s)
    if not m:
        continue
    vm = m.group(1)
    for mm in re.finditer(r"viewModel\.(\w+)", s):
        if mm.group(1) not in vm_members.get(vm, set()):
            errors.append(f"{os.path.basename(p)}: {vm}.{mm.group(1)} غير معرَّف")

# ---- 4/5. navigation ----
nav_src = read(os.path.join(SRC, "navigation/AppNavigation.kt"))
declared = set(re.findall(r'composable\("([^"]+)"', nav_src))
declared_base = {r.split("/")[0] for r in declared}

used = set()
for sub in ("ui", "navigation", "viewmodel"):
    for p in kt_files(sub):
        s = read(p)
        for r in re.findall(r'navigate\(\s*"([^"$]+)', s):
            used.add(r.split("/")[0].rstrip("/"))
        for r in re.findall(r'HomeMenuItem\([^)]*?"([a-z_]+)",\s*Card', s):
            used.add(r)
bb = os.path.join(SRC, "ui/components/AnimatedBottomBar.kt")
if os.path.exists(bb):
    for r in re.findall(r'BottomNavItem\(\s*"([a-z_]+)"', read(bb)):
        used.add(r)
    for r in re.findall(r'object \w+ : BottomNavItem\(\s*"([a-z_]+)"', read(bb)):
        used.add(r)
    for r in re.findall(r'"([a-z_]+)"', read(bb)):
        used.add(r)

for u in sorted(used):
    if u and u not in declared_base and u != "home":
        errors.append(f"التنقل: المسار \"{u}\" مستخدم بلا وجهة معرَّفة")

ENTRY = {"onboarding", "home"}
unreachable = sorted(r for r in declared if r.split("/")[0] not in used and r not in ENTRY)

print("=" * 60)
if errors:
    print(f"أخطاء المراجع ({len(errors)}):")
    for e in errors:
        print("  ✗ " + e)
else:
    print("✓ جميع مراجع DAO / المستودع / ViewModel / الشاشات / التنقل صحيحة")
if unreachable:
    print(f"\n⚠ مسارات بلا وسيلة وصول: {unreachable}")
print("=" * 60)
sys.exit(1 if errors else 0)
