/* ============================================================
   موسوعة السوق — منطق التطبيق (PWA يعمل بدون إنترنت)
   يعرض كل فصول الموسوعة + قاعدة العبارات + النظرة العامة.
   ============================================================ */
(function(){
  'use strict';

  const $ = (s,el=document)=>el.querySelector(s);
  const $$ = (s,el=document)=>Array.from(el.querySelectorAll(s));
  const esc = s => String(s==null?'':s).replace(/[&<>"']/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const store = {
    get(k,d){ try{const v=localStorage.getItem(k); return v==null?d:JSON.parse(v);}catch(e){return d;} },
    set(k,v){ try{localStorage.setItem(k,JSON.stringify(v));}catch(e){} }
  };

  if(!window.matchMedia){ window.matchMedia = function(q){ return {matches:false,media:q,addListener(){},removeListener(){},addEventListener(){},removeEventListener(){},dispatchEvent(){return false;}}; }; }
  if(!window.scrollTo){ window.scrollTo = function(){}; }

  let currentRoute='home';
  let theme = store.get('souq_theme','light');
  let favorites = store.get('souq_favs',[]);
  let activeFunc='';

  /* ---------- شاشة البداية ---------- */
  window.addEventListener('load', ()=>{
    setTimeout(()=>{
      $('#splash').classList.add('hide');
      setTimeout(()=>{ $('#splash').hidden=true; init(); }, 600);
    }, 1000);
  });

  function init(){
    applyTheme();
    buildNav();
    buildBottomNav();
    bindEvents();
    registerSW();
    $('#appHeader').hidden=false;
    $('#content').hidden=false;
    $('#bottomNav').hidden=false;
    if(window.matchMedia('(min-width:900px)').matches){ $('#drawer').hidden=false; }
    route();
  }

  function applyTheme(){ document.documentElement.setAttribute('data-theme', theme); }

  /* ============================================================
     محرّك الماركدون (عربي) — يعرض كل المحتوى كما هو
     ============================================================ */
  const C0='\u0000';

  function inline(raw){
    const codes=[], links=[];
    let t = String(raw==null?'':raw);
    t = t.replace(/`([^`]+)`/g, (m,c)=>{ codes.push(c); return C0+'C'+(codes.length-1)+C0; });
    t = t.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (m,txt,url)=>{ links.push([txt,url]); return C0+'L'+(links.length-1)+C0; });
    t = esc(t);
    t = t.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    t = t.replace(new RegExp(C0+'C(\\d+)'+C0,'g'), (m,n)=> '<code>'+esc(codes[+n])+'</code>');
    t = t.replace(new RegExp(C0+'L(\\d+)'+C0,'g'), (m,n)=>{ const x=links[+n]; return '<a href="'+esc(x[1])+'" target="_blank" rel="noopener">'+esc(x[0])+'</a>'; });
    return t;
  }

  function renderTable(rows){
    const cells = row => row.replace(/^\|/,'').replace(/\|$/,'').split('|').map(c=>c.trim());
    const header = cells(rows[0]);
    const body = rows.slice(2).map(r=>cells(r));
    let html='<div class="table-wrap"><table><thead><tr>'+header.map(h=>'<th>'+inline(h)+'</th>').join('')+'</tr></thead><tbody>';
    body.forEach(r=>{ html+='<tr>'+r.map(c=>'<td>'+inline(c)+'</td>').join('')+'</tr>'; });
    html+='</tbody></table></div>';
    return html;
  }

  function mdRender(md){
    const lines = String(md==null?'':md).replace(/\r\n/g,'\n').split('\n');
    let html='', i=0;
    const isBlank = l=>/^\s*$/.test(l);
    const isH = l=>/^#{1,6}\s+/.test(l);
    const isHR = l=>/^---+\s*$/.test(l) || /^\*\*\*+\s*$/.test(l);
    const isQuote = l=>/^>\s?/.test(l);
    const isTable = l=>/^\|/.test(l);
    const isUL = l=>/^\s*[-*]\s+/.test(l);
    const isOL = l=>/^\s*\d+\.\s+/.test(l);
    const blockStart = l=> isBlank(l)||isH(l)||isHR(l)||isQuote(l)||isTable(l)||isUL(l)||isOL(l);

    while(i<lines.length){
      while(i<lines.length && isBlank(lines[i])) i++;
      if(i>=lines.length) break;
      let line=lines[i];
      let h=line.match(/^(#{1,6})\s+(.*)$/);
      if(h){ const lvl=h[1].length; const txt=h[2].trim(); html+='<h'+lvl+' id="h'+i+'">'+inline(txt)+'</h'+lvl+'>'; i++; continue; }
      if(isHR(line)){ html+='<hr>'; i++; continue; }
      if(isQuote(line)){
        let buf=[];
        while(i<lines.length && isQuote(lines[i])){ buf.push(lines[i].replace(/^>\s?/,'')); i++; }
        html+='<blockquote>'+mdRender(buf.join('\n'))+'</blockquote>';
        continue;
      }
      if(isTable(line) && i+1<lines.length && /^\|[\s:|-]*\|?\s*$/.test(lines[i+1]) && lines[i+1].includes('-')){
        let tbuf=[];
        while(i<lines.length && isTable(lines[i])){ tbuf.push(lines[i]); i++; }
        html+=renderTable(tbuf);
        continue;
      }
      if(isUL(line)){
        let items=[];
        while(i<lines.length){
          if(isUL(lines[i])){ items.push(lines[i].replace(/^\s*[-*]\s+/,'')); i++; }
          else if(!blockStart(lines[i])){ items[items.length-1]+=' '+lines[i].trim(); i++; }
          else break;
        }
        html+='<ul>'+items.map(li=>'<li>'+inline(li)+'</li>').join('')+'</ul>';
        continue;
      }
      if(isOL(line)){
        let items=[];
        while(i<lines.length){
          if(isOL(lines[i])){ items.push(lines[i].replace(/^\s*\d+\.\s+/,'')); i++; }
          else if(!blockStart(lines[i])){ items[items.length-1]+=' '+lines[i].trim(); i++; }
          else break;
        }
        html+='<ol>'+items.map(li=>'<li>'+inline(li)+'</li>').join('')+'</ol>';
        continue;
      }
      let buf=[];
      while(i<lines.length && !blockStart(lines[i])){ buf.push(lines[i]); i++; }
      html+='<p>'+inline(buf.join(' '))+'</p>';
    }
    return html;
  }

  function mdToc(md){
    const lines=String(md==null?'':md).replace(/\r\n/g,'\n').split('\n');
    let items=[];
    lines.forEach((l,idx)=>{
      const m=l.match(/^##\s+(.*)$/);
      if(m) items.push({id:'h'+idx, text:m[1].replace(/\*\*/g,'').trim()});
    });
    return items;
  }

  /* ============================================================
     القائمة الجانبية
     ============================================================ */
  function buildNav(){
    const list=$('#navList');
    let html='';
    html+=navItem('home','🏠','الرئيسية','c-teal');
    html+='<li class="nav-label">الأقسام</li>';
    CHAPTERS.forEach(c=>{
      html+='<li><button class="nav-item" data-route="'+c.id+'"><span class="ni-num">'+c.num+'</span><span>'+esc(c.label)+'</span></button></li>';
    });
    html+='<li class="nav-label">استكشاف</li>';
    html+=navItem('phrases','🔎','قاعدة العبارات','c-blue');
    html+=navItem('functions','🗂️','التصنيف الوظيفي','c-teal');
    html+=navItem('search','🔍','بحث شامل','c-purple');
    html+=navItem('about','ℹ️','عن الموسوعة','c-teal');
    html+=navItem('favorites','⭐','المفضلة','c-amber');
    list.innerHTML=html;
    list.addEventListener('click', e=>{
      const btn=e.target.closest('.nav-item'); if(!btn) return;
      navigate(btn.dataset.route);
      closeDrawer();
    });
  }
  function navItem(id,icon,title,color){
    return '<li><button class="nav-item" data-route="'+id+'"><span class="ni-num '+color+'" style="color:#fff">'+icon+'</span><span>'+title+'</span></button></li>';
  }

  function buildBottomNav(){
    $$('.nav-tab').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.route)));
  }

  function bindEvents(){
    $('#menuBtn').addEventListener('click', openDrawer);
    $('#drawerOverlay').addEventListener('click', closeDrawer);
    $('#toTop').addEventListener('click', ()=>window.scrollTo({top:0,behavior:'smooth'}));
    window.addEventListener('scroll', ()=>{ $('#toTop').hidden = window.scrollY < 400; });

    let deferredPrompt;
    window.addEventListener('beforeinstallprompt', e=>{
      e.preventDefault(); deferredPrompt=e;
      const ib=$('#installBtn'); ib.hidden=false;
      ib.addEventListener('click', async()=>{
        ib.hidden=true; deferredPrompt.prompt();
        await deferredPrompt.userChoice; deferredPrompt=null;
      },{once:true});
    });
    window.addEventListener('appinstalled', ()=>{ $('#installBtn').hidden=true; toast('تم تثبيت التطبيق بنجاح ✅'); });

    $('#resetFavs').addEventListener('click',()=>{
      if(confirm('مسح كل العبارات المحفوظة في المفضلة؟')){
        favorites=[]; store.set('souq_favs',[]); toast('تم مسح المفضلة');
        if(currentRoute==='favorites') render('favorites');
      }
    });
    $('#exportData')?.addEventListener('click', ()=>{
      const dataStr=JSON.stringify({favs:favorites});
      const blob=new Blob([dataStr],{type:'application/json'});
      const url=URL.createObjectURL(blob);
      const a=document.createElement('a'); a.href=url; a.download='souq-favorites.json'; a.click();
      URL.revokeObjectURL(url); toast('تم التصدير 📁'); closeDrawer();
    });

    $('#themeBtn')?.addEventListener('click', ()=>{
      theme = theme==='dark'?'light':'dark'; store.set('souq_theme',theme); applyTheme();
    });

    let textSizes=['normal','text-lg','text-sm'];
    let currSizeIdx=store.get('souq_text_size',0);
    const applyTextSize=()=>{ document.body.classList.remove('text-lg','text-sm'); if(textSizes[currSizeIdx]!=='normal') document.body.classList.add(textSizes[currSizeIdx]); };
    applyTextSize();
    $('#textSizeBtn')?.addEventListener('click', ()=>{
      currSizeIdx=(currSizeIdx+1)%textSizes.length; store.set('souq_text_size',currSizeIdx); applyTextSize();
      toast(currSizeIdx===0?'حجم الخط: عادي':currSizeIdx===1?'حجم الخط: كبير':'حجم الخط: صغير');
    });

    window.addEventListener('hashchange', route);
  }

  function openDrawer(){
    $('#drawer').hidden=false;
    requestAnimationFrame(()=>{ $('#drawer').classList.add('show'); $('#drawerOverlay').hidden=false; requestAnimationFrame(()=>$('#drawerOverlay').classList.add('show')); });
  }
  function closeDrawer(){
    if(window.matchMedia('(min-width:900px)').matches) return;
    $('#drawer').classList.remove('show');
    const ov=$('#drawerOverlay'); ov.classList.remove('show');
    setTimeout(()=>{ $('#drawer').hidden=true; ov.hidden=true; },320);
  }

  function navigate(route){ currentRoute=route; location.hash=route; }

  /* ============================================================
     التوجيه (Routing)
     ============================================================ */
  const ROUTES=['home','chapters','phrases','about','favorites','functions','search'].concat(CHAPTERS.map(c=>c.id));
  const isChapter = id => CHAPTERS.some(c=>c.id===id);

  function route(){
    const parts=(location.hash||'#home').slice(1).split('?');
    let clean=parts[0]||'home';
    const params=new URLSearchParams(parts[1]||'');
    currentRoute = ROUTES.includes(clean)?clean:'home';
    activeFunc = currentRoute==='phrases' ? (params.get('func')||'') : '';
    updateActiveNav();
    render(currentRoute);
    window.scrollTo({top:0});
    if(window.matchMedia('(max-width:899px)').matches) closeDrawer();
  }

  function updateActiveNav(){
    $$('.nav-item').forEach(b=>b.classList.toggle('active', b.dataset.route===currentRoute));
    let tab = currentRoute==='phrases'?'phrases':(currentRoute==='chapters'||isChapter(currentRoute))?'chapters':'home';
    $$('.nav-tab').forEach(b=>b.classList.toggle('active', b.dataset.route===tab));
    const map={home:SOUQ_META.appName, chapters:'أقسام الموسوعة', phrases:'قاعدة العبارات', about:'عن الموسوعة', favorites:'المفضلة', functions:'التصنيف الوظيفي', search:'البحث الشامل'};
    let title=map[currentRoute];
    if(!title){ const ch=CHAPTERS.find(c=>c.id===currentRoute); title=ch?ch.label:SOUQ_META.appName; }
    $('#sectionTitle').textContent=title;
  }

  function render(route){
    const c=$('#content');
    const map={home:renderHome, chapters:renderChapters, phrases:renderPhrases, about:renderAbout, favorites:renderFavorites, functions:renderFunctions, search:renderSearch};
    if(isChapter(route)){ c.innerHTML=renderChapter(route); }
    else { c.innerHTML=(map[route]||renderHome)(); }
    if(route==='phrases') bindPhrases();
    if(route==='search') bindSearch();
  }

  /* ============================================================
     الشاشات
     ============================================================ */
  function chaptersGrid(){
    const tiles=CHAPTERS.map(c=>
      '<button class="section-tile" data-go="'+c.id+'">'+
        '<span class="ic '+c.color+'">'+c.icon+'</span>'+
        '<span class="t">'+esc(c.label)+'</span>'+
        '<span class="n">القسم '+c.num+'</span>'+
      '</button>').join('');
    return '<div class="section-grid">'+tiles+'</div>';
  }

  function renderHome(){
    const m=SOUQ_META;
    const stats=[
      {ic:'📘', v:m.chaptersCount, l:'قسم منظّم'},
      {ic:'🔤', v:m.phrasesCount, l:'عبارة وجملة'},
      {ic:'🌍', v:m.countriesCount, l:'دولة ومنطقة'},
      {ic:'💬', v:m.situationsCount, l:'موقفاً ومجالاً'}
    ];
    const statHtml=stats.map(s=>'<div class="stat-pill"><span class="sp-ic">'+s.ic+'</span><div><div class="sp-v">'+s.v+'</div><div class="sp-l">'+s.l+'</div></div></div>').join('');
    const quick=[
      {id:'phrases', ic:'🔎', c:'c-blue', t:'قاعدة العبارات', n:'بحث وفلترة'},
      {id:'about', ic:'ℹ️', c:'c-teal', t:'عن الموسوعة', n:'المنهجية والرموز'},
      {id:'favorites', ic:'⭐', c:'c-amber', t:'المفضلة', n:'عباراتك المحفوظة'}
    ];
    const quickHtml=quick.map(q=>'<button class="section-tile" data-go="'+q.id+'"><span class="ic '+q.c+'">'+q.ic+'</span><span class="t">'+esc(q.t)+'</span><span class="n">'+esc(q.n)+'</span></button>').join('');
    return ''+
      '<section class="hero">'+
        '<h1>'+esc(m.appName)+'</h1>'+
        '<p>'+esc(m.subtitle)+'</p>'+
        '<div class="hero-stats">'+statHtml+'</div>'+
      '</section>'+
      '<div class="card"><h2>🔎 ما هي موسوعة السوق؟</h2><p style="color:var(--text-soft);font-size:14.5px">دليل عربي شامل يجمع عبارات وأساليب ولهجات الباعة والزبائن في الأسواق العربية: الجذب، الترحيب، العرض، الإقناع، المساومة، المزاح، إغلاق البيع، والتوديع — مع قاعدة بيانات ضخمة قابلة للبحث والفلترة. كل المحتوى متاح دون إنترنت.</p></div>'+
      '<div class="sec-intro">أقسام الموسوعة ('+m.chaptersCount+')</div>'+
      chaptersGrid()+
      '<div class="sec-intro">أدوات إضافية</div>'+
      '<div class="section-grid">'+quickHtml+'</div>';
  }

  function renderChapters(){
    return '<div class="sec-intro">'+esc(SOUQ_META.appName)+' — '+esc(SOUQ_META.subtitle)+'. تصفّح '+SOUQ_META.chaptersCount+' قسماً منظّماً يغطي كل جوانب لغة السوق الحية.</div>'+chaptersGrid();
  }

  function renderChapter(id){
    const c=CHAPTERS.find(x=>x.id===id);
    if(!c) return renderHome();
    const toc=mdToc(c.raw);
    const tocHtml = toc.length>1 ? '<div class="toc"><div class="toc-title">📑 محتويات القسم</div>'+toc.map(t=>'<a data-anchor="'+t.id+'"><span class="dot"></span>'+esc(t.text)+'</a>').join('')+'</div>' : '';
    const idx=CHAPTERS.findIndex(x=>x.id===id);
    const prev=idx>0?CHAPTERS[idx-1]:null;
    const next=idx<CHAPTERS.length-1?CHAPTERS[idx+1]:null;
    const nav='<div class="filter-row" style="margin-top:18px">'+(prev?'<button class="btn-primary ghost" data-go="'+prev.id+'" style="flex:1">→ '+esc(prev.label)+'</button>':'<span style="flex:1"></span>')+(next?'<button class="btn-primary ghost" data-go="'+next.id+'" style="flex:1">'+esc(next.label)+' ←</button>':'<span style="flex:1"></span>')+'</div>';
    return ''+
      '<div class="chapter-head">'+
        '<div style="display:flex;align-items:center">'+
          '<span class="ch-num">'+c.icon+'</span>'+
          '<div><h1>'+esc(c.title)+'</h1><p>القسم '+c.num+' من '+SOUQ_META.chaptersCount+'</p></div>'+
        '</div>'+
      '</div>'+
      tocHtml+
      mdRender(c.raw)+
      nav;
  }

  function renderAbout(){
    const a=ABOUT, m=SOUQ_META;
    return ''+
      '<div class="chapter-head"><div style="display:flex;align-items:center"><span class="ch-num">ℹ️</span><div><h1>'+esc(a.title)+'</h1><p>نظرة عامة ومنهجية الموسوعة</p></div></div></div>'+
      '<div class="about-card"><div class="about-meta">'+
        '<span class="badge gold">'+m.chaptersCount+' قسم</span>'+
        '<span class="badge teal">'+m.phrasesCount+' عبارة</span>'+
        '<span class="badge blue">'+m.countriesCount+' دولة/منطقة</span>'+
        '<span class="badge purple">'+m.dialectsCount+' لهجة</span>'+
        '<span class="badge rose">'+m.situationsCount+' موقف</span>'+
      '</div></div>'+
      mdRender(a.raw);
  }

  /* ---------- قاعدة العبارات ---------- */
  function phraseCard(p){
    const isFav=favorites.includes(p.id);
    const b=(t,cls)=> t?'<span class="badge '+(cls||'')+'">'+esc(t)+'</span>':'';
    return '<div class="phrase-card" data-pid="'+esc(p.id)+'">'+
      '<button class="fav-star '+(isFav?'on':'')+'" data-pid="'+esc(p.id)+'" aria-label="مفضلة">'+(isFav?'★':'☆')+'</button>'+
      '<div class="phrase-main">«'+esc(p.phrase)+'»</div>'+
      (p.msa?'<div class="phrase-msa"><b>الفصحى:</b> '+esc(p.msa)+'</div>':'')+
      '<div class="phrase-meta">'+
        b(p.func,'teal')+b(p.country,'gold')+b(p.dialect,'teal')+b(p.situation,'blue')+b(p.addressee,'purple')+b(p.formality)+b(p.familiarity)+b(p.frequency,'rose')+b(p.humor)+
      '</div>'+
      (p.notes?'<div class="phrase-notes"><b>ملاحظات:</b> '+esc(p.notes)+'</div>':'')+
    '</div>';
  }

  function renderPhrases(){
    const m=SOUQ_META;
    const opt=arr=>'<option value="">الكل</option>'+arr.map(x=>'<option value="'+esc(x)+'">'+esc(x)+'</option>').join('');
      return funcBanner()+
      '<div class="sec-intro">قاعدة بيانات فيها <b>'+m.phrasesCount+'</b> عبارة وجملة من أسواق العربية، مع ترجمتها الفصحى ودولتها ولهجتها وموقفها. ابحث وصفِّ وفقاً لحاجتك.</div>'+
      '<div class="filter-bar">'+
        '<div class="search-box" style="margin-bottom:0">'+
          '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M15.5 14h-.8l-.3-.3a6.5 6.5 0 10-.7.7l.3.3v.8l5 5 1.5-1.5-5-5zm-6 0A4.5 4.5 0 1114 9.5 4.5 4.5 0 019.5 14z"/></svg>'+
          '<input id="phSearch" type="search" placeholder="ابحث في العبارات (العبارة، الفصحى، الملاحظات...)">'+
        '</div>'+
        '<div class="filter-row">'+
          '<select id="fCountry" class="filter-select">'+opt(m.countries)+'</select>'+
          '<select id="fDialect" class="filter-select">'+opt(m.dialects)+'</select>'+
          '<select id="fSituation" class="filter-select">'+opt(m.situations)+'</select>'+
          '<select id="fAddressee" class="filter-select">'+opt(m.addressees)+'</select>'+
        '</div>'+
        '<div class="filter-row">'+
          '<button id="fClear" class="btn-primary ghost" style="flex:0 0 auto;padding:10px 18px">مسح الفلاتر</button>'+
        '</div>'+
      '</div>'+
      '<div id="phCount" class="result-count"></div>'+
      '<div id="phResults"></div>';
  }

  function getFilteredPhrases(){
    const q=$('#phSearch')?$('#phSearch').value.trim().toLowerCase():'';
    const c=$('#fCountry')?$('#fCountry').value:'';
    const d=$('#fDialect')?$('#fDialect').value:'';
    const s=$('#fSituation')?$('#fSituation').value:'';
    const a=$('#fAddressee')?$('#fAddressee').value:'';
    return PHRASES.filter(p=>{
      if(activeFunc && p.func!==activeFunc) return false;
      if(c && p.country!==c) return false;
      if(d && p.dialect!==d) return false;
      if(s && p.situation!==s) return false;
      if(a && p.addressee!==a) return false;
      if(q){
        const hay=(p.phrase+' '+p.msa+' '+p.notes+' '+p.country+' '+p.dialect+' '+p.situation+' '+p.addressee).toLowerCase();
        if(!hay.includes(q)) return false;
      }
      return true;
    });
  }

  function renderPhrasesResults(){
    const list=getFilteredPhrases();
    const cnt=$('#phCount'); if(cnt) cnt.innerHTML=list.length+' نتيجة';
    const box=$('#phResults'); if(!box) return;
    if(!list.length){ box.innerHTML='<p style="text-align:center;color:var(--text-mute);padding:24px">لا توجد نتائج مطابقة.</p>'; return; }
    box.innerHTML=list.map(phraseCard).join('');
  }

  function bindPhrases(){
    ['#phSearch','#fCountry','#fDialect','#fSituation','#fAddressee'].forEach(sel=>{
      const el=$(sel); if(!el) return;
      el.addEventListener('input', renderPhrasesResults);
      if(sel!=='#phSearch') el.addEventListener('change', renderPhrasesResults);
    });
    const clr=$('#fClear'); if(clr) clr.addEventListener('click',()=>{
      if($('#phSearch'))$('#phSearch').value='';
      ['#fCountry','#fDialect','#fSituation','#fAddressee'].forEach(s=>{const e=$(s);if(e)e.value='';});
      renderPhrasesResults();
    });
    renderPhrasesResults();
  }

  function funcBanner(){
    if(!activeFunc) return '';
    const f=FUNCTIONS.find(x=>x.code===activeFunc);
    if(!f) return '';
    return '<div class="box tip" style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px">'+
      '<div><span class="bt">عبارات الوظيفة '+esc(f.code)+'</span> '+esc(f.name)+' — '+f.count+' عبارة</div>'+
      '<button class="btn-primary ghost" data-go="phrases" style="flex:0 0 auto;padding:8px 14px">مسح التصفية</button>'+
    '</div>';
  }

  function renderFunctions(){
    const items=FUNCTIONS.map(f=>{
      const ch=f.chapterId?CHAPTERS.find(c=>c.id===f.chapterId):null;
      return '<div class="card">'+
        '<h2><span class="ic c-teal" style="width:34px;height:34px;border-radius:10px;font-size:16px;display:inline-flex;align-items:center;justify-content:center;color:#fff;margin-left:8px">'+esc(f.code)+'</span>'+esc(f.name)+'</h2>'+
        (f.examples?'<p style="color:var(--text-soft);font-size:14px">أمثلة: '+esc(f.examples)+'</p>':'')+
        '<div class="phrase-meta" style="margin-top:8px">'+
          '<span class="badge gold">'+f.count+' عبارة</span>'+
          (ch?'<span class="badge blue">'+esc(ch.label)+'</span>':'')+
        '</div>'+
        '<div class="filter-row" style="margin-top:12px">'+
          (f.count>0?'<button class="btn-primary ghost" data-go="phrases?func='+f.code+'" style="flex:1">عرض عبارات هذه الوظيفة</button>':'<span style="flex:1"></span>')+
          (ch?'<button class="btn-primary ghost" data-go="'+ch.id+'" style="flex:1">القسم المختص ←</button>':'<span hidden></span>')+
        '</div>'+
      '</div>';
    }).join('');
    return '<div class="sec-intro">التصنيف الوظيفي (A–K) هو العمود الفقري للموسوعة: كل عبارة تنتمي إلى وظيفة بيعية. اختر وظيفة لتصفّح عباراتها أو تنتقل إلى قسمها المختص.</div><div style="display:flex;flex-direction:column;gap:14px">'+items+'</div>';
  }

  function renderSearch(){
    return '<div class="sec-intro">بحث شامل في كل أقسام الموسوعة وقاعدة العبارات معاً.</div>'+
      '<div class="filter-bar">'+
        '<div class="search-box" style="margin-bottom:0">'+
          '<svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M15.5 14h-.8l-.3-.3a6.5 6.5 0 10-.7.7l.3.3v.8l5 5 1.5-1.5-5-5zm-6 0A4.5 4.5 0 1114 9.5 4.5 4.5 0 019.5 14z"/></svg>'+
          '<input id="gsSearch" type="search" placeholder="ابحث عن عبارة، موضوع، كلمة، أو موقف...">'+
        '</div>'+
        '<div class="filter-row">'+
          '<select id="gsScope" class="filter-select">'+
            '<option value="all">الكل (أقسام + عبارات)</option>'+
            '<option value="chapters">الأقسام فقط</option>'+
            '<option value="phrases">العبارات فقط</option>'+
          '</select>'+
        '</div>'+
      '</div>'+
      '<div id="gsCount" class="result-count"></div>'+
      '<div id="gsResults"></div>';
  }

  function renderGlobalResults(){
    const q=$('#gsSearch')?$('#gsSearch').value.trim().toLowerCase():'';
    const scope=$('#gsScope')?$('#gsScope').value:'all';
    const box=$('#gsResults'); if(!box) return;
    if(!q){ box.innerHTML='<p style="text-align:center;color:var(--text-mute);padding:20px">اكتب كلمة للبحث في الموسوعة.</p>'; const c=$('#gsCount'); if(c)c.textContent=''; return; }
    let html=''; let total=0;
    if(scope!=='phrases'){
      const chRes=CHAPTERS.map(c=>({c,idx:c.raw.toLowerCase().indexOf(q)})).filter(x=>x.idx>=0);
      if(chRes.length){
        total+=chRes.length;
        html+='<h3 style="margin:16px 0 8px;color:var(--teal-700)">📘 الأقسام ('+chRes.length+')</h3>';
        html+=chRes.map(({c})=>{
          const idx=c.raw.toLowerCase().indexOf(q);
          const start=Math.max(0,idx-40);
          const snip=c.raw.substring(start,start+120).replace(/\n+/g,' ').replace(/\*\*/g,'').replace(/[#>*|]/g,'');
          return '<button class="section-tile" data-go="'+c.id+'" style="text-align:right;align-items:flex-start">'+
            '<span class="ic '+c.color+'">'+c.icon+'</span>'+
            '<span class="t">'+esc(c.label)+'</span>'+
            '<span class="n">…'+esc(snip)+'…</span>'+
          '</button>';
        }).join('');
      }
    }
    if(scope!=='chapters'){
      const phRes=PHRASES.filter(p=>(p.phrase+' '+p.msa+' '+p.notes+' '+p.situation+' '+p.country+' '+p.dialect).toLowerCase().includes(q));
      if(phRes.length){
        total+=phRes.length;
        html+='<h3 style="margin:16px 0 8px;color:var(--teal-700)">🔤 العبارات ('+phRes.length+')</h3>';
        html+=phRes.slice(0,60).map(phraseCard).join('');
        if(phRes.length>60) html+='<p style="text-align:center;color:var(--text-mute);padding:10px">وهناك '+(phRes.length-60)+' نتيجة أخرى — استخدم «قاعدة العبارات» للفلترة الكاملة.</p>';
      }
    }
    const cnt=$('#gsCount'); if(cnt) cnt.textContent=total+' نتيجة لـ «'+q+'»';
    box.innerHTML = total ? html : '<p style="text-align:center;color:var(--text-mute);padding:24px">لا توجد نتائج مطابقة لـ «'+esc(q)+'».</p>';
  }

  function bindSearch(){
    const s=$('#gsSearch'); if(s) s.addEventListener('input', renderGlobalResults);
    const sc=$('#gsScope'); if(sc) sc.addEventListener('change', renderGlobalResults);
    renderGlobalResults();
  }

  function renderFavorites(){
    if(!favorites.length) return '<div class="sec-intro">عباراتك المفضلة تظهر هنا.</div><div class="card" style="text-align:center;color:var(--text-mute)">لم تضف أي عبارة للمفضلة بعد. اضغط ☆ على أي عبارة في «قاعدة العبارات».</div>';
    const list=PHRASES.filter(p=>favorites.includes(p.id));
    return '<div class="sec-intro">لديك <b>'+list.length+'</b> عبارة في المفضلة.</div><div id="phResults">'+list.map(phraseCard).join('')+'</div>';
  }

  function toggleFav(star){
    const pid=star.dataset.pid;
    if(favorites.includes(pid)){
      favorites=favorites.filter(x=>x!==pid);
      star.classList.remove('on'); star.textContent='☆'; toast('أُزيلت من المفضلة');
    } else {
      favorites.push(pid);
      star.classList.add('on'); star.textContent='★'; toast('أُضيفت للمفضلة ⭐');
    }
    store.set('souq_favs',favorites);
    if(currentRoute==='favorites') render('favorites');
  }

  function toast(msg){
    const t=$('#toast'); if(!t) return;
    t.textContent=msg; t.hidden=false;
    clearTimeout(toast._t); toast._t=setTimeout(()=>t.hidden=true,2200);
  }

  /* ---------- تفويض النقر ---------- */
  document.addEventListener('click', e=>{
    const go=e.target.closest('[data-go]');
    if(go){ navigate(go.dataset.go); return; }
    const anc=e.target.closest('[data-anchor]');
    if(anc){ e.preventDefault(); const el=document.getElementById(anc.dataset.anchor); if(el) el.scrollIntoView({behavior:'smooth',block:'start'}); return; }
    const star=e.target.closest('.fav-star');
    if(star){ toggleFav(star); return; }
    const acc=e.target.closest('.acc-head');
    if(acc){ acc.parentElement.classList.toggle('open'); return; }
  });

  /* ---------- Service Worker ---------- */
  function registerSW(){
    if('serviceWorker' in navigator){
      window.addEventListener('load',()=>{ navigator.serviceWorker.register('sw.js').catch(()=>{}); });
    }
  }

})();
