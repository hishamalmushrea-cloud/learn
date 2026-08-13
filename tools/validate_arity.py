#!/usr/bin/env python3
"""
يتحقق أن كل استدعاء لبانٍ كيان في بيانات البذر يمرر عدد وسائط صحيحاً.

لماذا: الكود المُسلَّم احتوى على صفّين يمرران 7 وسائط لبانٍ يتطلب 8
(LessonDetailEntity 204 و205) — وهو خطأ تجميع فعلي كان سيمنع بناء التطبيق.

الاستخدام: python3 tools/validate_arity.py   (من جذر المستودع)
"""
import os, sys
os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                      "IndoLearn", "app", "src", "main", "java", "com", "indolearn"))

import re

def strip_comments(s):
    out=[];i=0;n=len(s)
    while i<n:
        c=s[i]
        if c=='/' and i+1<n and s[i+1]=='/':
            while i<n and s[i]!='\n': i+=1
            continue
        if c=='/' and i+1<n and s[i+1]=='*':
            d=1;i+=2
            while i<n and d>0:
                if s[i]=='/' and i+1<n and s[i+1]=='*': d+=1;i+=2;continue
                if s[i]=='*' and i+1<n and s[i+1]=='/': d-=1;i+=2;continue
                if s[i]=='\n': out.append('\n')
                i+=1
            continue
        if c=='"':
            out.append(c);i+=1
            while i<n and s[i]!='"':
                if s[i]=='\\': out.append(s[i]);i+=1
                if i<n: out.append(s[i]);i+=1
            if i<n: out.append('"');i+=1
            continue
        out.append(c);i+=1
    return ''.join(out)
def split_top(buf):
    parts=[];d=0;ins=False;es=False;cur=''
    for c in buf:
        if es: cur+=c;es=False;continue
        if c=='\\': cur+=c;es=True;continue
        if c=='"': ins=not ins
        if not ins:
            if c in '([<': d+=1
            elif c in ')]>': d-=1
            elif c==',' and d==0:
                parts.append(cur);cur='';continue
        cur+=c
    if cur.strip(): parts.append(cur)
    return parts
BASE=os.path.dirname(os.path.abspath(__file__)) if False else '.'
def entity_params(name):
    p='data/local/entity/%s.kt'%name
    if not os.path.exists(p): return None
    s=strip_comments(open(p,encoding='utf-8').read())
    m=re.search(r'data class \w+\s*\(',s)
    i=m.end()-1; d=0; buf=''
    for ch in s[i:]:
        if ch=='(':
            d+=1
            if d==1: continue
        if ch==')':
            d-=1
            if d==0: break
        buf+=ch
    parts=split_top(buf)
    return len(parts), sum(1 for x in parts if '=' not in x)
repo=strip_comments(open('data/repository/LearnRepository.kt',encoding='utf-8').read())
def calls(name):
    out=[]
    for m in re.finditer(r'\b'+re.escape(name)+r'\(',repo):
        i=m.end();depth=1;buf='';ins=False;esc=False
        while i<len(repo) and depth>0:
            c=repo[i]
            if esc: buf+=c;esc=False;i+=1;continue
            if c=='\\': buf+=c;esc=True;i+=1;continue
            if c=='"': ins=not ins
            if not ins:
                if c=='(':depth+=1
                elif c==')':
                    depth-=1
                    if depth==0:break
            buf+=c;i+=1
        out.append((repo[:m.start()].count('\n')+1,len(split_top(buf))))
    return out
problems=[]
for e in ["LessonEntity","LessonDetailEntity","VocabularyEntity","GrammarEntity","DialogueEntity",
          "CasualExpressionEntity","DailyScenarioEntity","TrainingItemEntity","StageEntity","UnitEntity"]:
    ep=entity_params(e)
    if not ep: continue
    total,req=ep
    for line,a in calls(e):
        if a<req or a>total:
            problems.append("  ✗ %s line %d: %d args (needs %d..%d)"%(e,line,a,req,total))
if problems:
    print("\n".join(problems)); print("TOTAL ARITY ERRORS:",len(problems)); sys.exit(1)
print("  ✓ all entity constructor calls have valid arity")
