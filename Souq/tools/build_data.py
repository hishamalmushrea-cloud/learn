#!/usr/bin/env python3
# ============================================================
#  مولّد بيانات تطبيق «موسوعة السوق» (Souq)
#  يقرأ المصادر الأصلية من ../source/ وينتج ../js/data.js
#  يحتوي على كل الفصول (15) + قاعدة العبارات (CSV) + نظرة عامة.
#  لا يتم تجاهل أي محتوى: كل نص يُضمَّن كما هو.
# ============================================================
import csv, json, os, re, glob

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "..", "source")
OUT = os.path.join(HERE, "..", "js", "data.js")

# أيقونات وألوان كل قسم (مرتبة حسب رقم الملف 01..15)
ICONS = {
    1:  ("🧭", "c-teal"),
    2:  ("👋", "c-amber"),
    3:  ("🗣️", "c-blue"),
    4:  ("🛍️", "c-green"),
    5:  ("🤝", "c-purple"),
    6:  ("😄", "c-rose"),
    7:  ("🧑‍🤝‍🧑", "c-orange"),
    8:  ("🏪", "c-cyan"),
    9:  ("🌍", "c-teal"),
    10: ("📋", "c-amber"),
    11: ("💬", "c-blue"),
    12: ("🔤", "c-green"),
    13: ("⚠️", "c-rose"),
    14: ("⭐", "c-purple"),
    15: ("📚", "c-slate"),
}

PREFIX_RE = re.compile(r'^(?:القسم|الأقسام)\s+[\d\u2013\u2014–-]+\s*[\u2014\u2013-]\s*(.*)$')

def short_title(title):
    m = PREFIX_RE.match(title.strip())
    label = m.group(1) if m else title.strip()
    label = re.sub(r'\s*\([^)]*\)\s*$', '', label).strip()
    return label or title.strip()

def read_chapters():
    chapters = []
    files = sorted(glob.glob(os.path.join(SRC, "[0-9][0-9]-*.md")))
    for path in files:
        base = os.path.basename(path)
        num = int(base[:2])
        with open(path, encoding="utf-8") as f:
            raw = f.read()
        # عنوان الفصل = أول سطر يبدأ بـ #
        title = base
        for line in raw.splitlines():
            if line.startswith("# "):
                title = line[2:].strip()
                break
        icon, color = ICONS.get(num, ("📄", "c-slate"))
        chapters.append({
            "id": f"ch{num:02d}",
            "num": num,
            "file": base,
            "title": title,
            "label": short_title(title),
            "icon": icon,
            "color": color,
            "raw": raw,
        })
    return chapters

def read_about():
    path = os.path.join(SRC, "README.md")
    if not os.path.exists(path):
        return {"id": "about", "title": "عن الموسوعة", "raw": ""}
    with open(path, encoding="utf-8") as f:
        raw = f.read()
    title = "موسوعة لغة السوق والبائع والزبون"
    for line in raw.splitlines():
        if line.startswith("# "):
            title = line[2:].strip()
            break
    return {"id": "about", "title": title, "raw": raw}

def read_phrases():
    path = os.path.join(SRC, "phrases-db.csv")
    phrases = []
    with open(path, encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        for row in reader:
            phrases.append({
                "id": (row.get("id") or "").strip(),
                "phrase": (row.get("phrase") or "").strip(),
                "msa": (row.get("msa") or "").strip(),
                "country": (row.get("country") or "").strip(),
                "dialect": (row.get("dialect") or "").strip(),
                "situation": (row.get("situation") or "").strip(),
                "addressee": (row.get("addressee") or "").strip(),
                "formality": (row.get("formality") or "").strip(),
                "familiarity": (row.get("familiarity") or "").strip(),
                "humor": (row.get("humor") or "").strip(),
                "frequency": (row.get("frequency") or "").strip(),
                "notes": (row.get("notes") or "").strip(),
            })
    return phrases

def main():
    chapters = read_chapters()
    about = read_about()
    phrases = read_phrases()

    countries = sorted({p["country"] for p in phrases if p["country"]})
    dialects = sorted({p["dialect"] for p in phrases if p["dialect"]})
    situations = sorted({p["situation"] for p in phrases if p["situation"]})
    addressees = sorted({p["addressee"] for p in phrases if p["addressee"]})

    meta = {
        "appName": "موسوعة السوق",
        "subtitle": "دليل لغة البائع والزبون في الأسواق العربية",
        "source": "مشروع seller — فرع arena/019ff95f-seller",
        "chaptersCount": len(chapters),
        "phrasesCount": len(phrases),
        "countriesCount": len(countries),
        "dialectsCount": len(dialects),
        "situationsCount": len(situations),
        "countries": countries,
        "dialects": dialects,
        "situations": situations,
        "addressees": addressees,
        "generated": "2026-08-18",
    }

    out = []
    out.append("/* ============================================================")
    out.append("   موسوعة السوق — ملف البيانات المولّد تلقائياً")
    out.append("   يحتوي على كل الفصول + قاعدة العبارات + نظرة عامة.")
    out.append("   لا تعدّل هذا الملف يدوياً؛ عدّل المصادر في ../source ثم شغّل build_data.py")
    out.append("   ============================================================ */")
    out.append("")
    out.append("const SOUQ_META = " + json.dumps(meta, ensure_ascii=False, indent=1) + ";")
    out.append("")
    out.append("const CHAPTERS = " + json.dumps(chapters, ensure_ascii=False, indent=1) + ";")
    out.append("")
    out.append("const ABOUT = " + json.dumps(about, ensure_ascii=False, indent=1) + ";")
    out.append("")
    out.append("const PHRASES = " + json.dumps(phrases, ensure_ascii=False, indent=1) + ";")
    out.append("")

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(out))

    print(f"✔ تم توليد data.js")
    print(f"   الفصول: {len(chapters)}")
    print(f"   العبارات: {len(phrases)}")
    print(f"   الدول/المناطق: {len(countries)} | اللهجات: {len(dialects)} | المواقف: {len(situations)}")
    print(f"   الحجم: {os.path.getsize(OUT)//1024} KB")

if __name__ == "__main__":
    main()
