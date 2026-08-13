#!/usr/bin/env python3
"""
فاحص بنيوي لملفات Kotlin: توازن الأقواس والتعليقات والسلاسل النصية.

لماذا: لا يمكن تشغيل مترجم Kotlin على كود Android هنا (مستودعات محجوبة).
هذا الفحص يلتقط أخطاء بنيوية فادحة مثل تعليق غير مغلق — وهو خطأ
وقع فعلاً أثناء العمل بسبب وجود */ داخل نص تعليق.

الاستخدام: python3 tools/validate_structure.py <dir>
"""

import sys, os
# Lightweight structural validator: balanced braces/parens/brackets outside strings & comments.
def check(path):
    s=open(path,encoding='utf-8').read()
    i=0; n=len(s)
    stack=[]
    line=1
    errs=[]
    while i<n:
        c=s[i]
        if c=='\n': line+=1; i+=1; continue
        # line comment
        if c=='/' and i+1<n and s[i+1]=='/':
            while i<n and s[i]!='\n': i+=1
            continue
        # block comment (kotlin allows nesting)
        if c=='/' and i+1<n and s[i+1]=='*':
            depth=1; i+=2
            while i<n and depth>0:
                if s[i]=='\n': line+=1
                if s[i]=='/' and i+1<n and s[i+1]=='*': depth+=1; i+=2; continue
                if s[i]=='*' and i+1<n and s[i+1]=='/': depth-=1; i+=2; continue
                i+=1
            if depth>0: errs.append((line,"unclosed block comment"))
            continue
        # raw string
        if s.startswith('"""',i):
            i+=3
            while i<n and not s.startswith('"""',i):
                if s[i]=='\n': line+=1
                i+=1
            i+=3
            continue
        # normal string
        if c=='"':
            i+=1
            while i<n and s[i]!='"':
                if s[i]=='\\': i+=2; continue
                if s[i]=='\n':
                    errs.append((line,"newline in string literal")); break
                i+=1
            i+=1
            continue
        if c=="'":
            i+=1
            while i<n and s[i]!="'":
                if s[i]=='\\': i+=2; continue
                i+=1
            i+=1
            continue
        if c in '([{': stack.append((c,line)); i+=1; continue
        if c in ')]}':
            if not stack: errs.append((line,f"unmatched closing {c}")); i+=1; continue
            o,ol=stack.pop()
            if '([{'.index(o)!=')]}'.index(c):
                errs.append((line,f"mismatched {o} (line {ol}) vs {c}"))
            i+=1; continue
        i+=1
    for o,ol in stack:
        errs.append((ol,f"unclosed {o}"))
    return errs

bad=0
for root,d,files in os.walk(sys.argv[1]):
    for f in files:
        if f.endswith('.kt'):
            p=os.path.join(root,f)
            e=check(p)
            if e:
                bad+=1
                print("FAIL",p)
                for l,m in e[:5]: print("   line",l,m)
print("الملفات المفحوصة في %s — ملفات بها مشاكل: %d"%(sys.argv[1],bad))
sys.exit(1 if bad else 0)
