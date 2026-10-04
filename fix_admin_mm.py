#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إصلاح 2 أخطاء في AdminMurderMysteryActivity"""

PATH = "app/src/main/java/com/ummah/admin/AdminMurderMysteryActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. حذف attachBaseContext (LocaleHelper ما كانش في Admin) ═══
old_attach = '''    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

'''

if old_attach in c:
    c = c.replace(old_attach, '')
    print("✅ حذفنا attachBaseContext")
else:
    print("⚠️ ما لقيناش attachBaseContext")

# ═══ 2. نبدلو bg_btn_gold بـ drawable موجود ═══
# نجربو bg_btn_gold_alt أو bg_button أو أي حاجة موجودة
import os

# نشوفو Drawables الموجودة
drawables = []
drawable_dir = "app/src/main/res/drawable"
if os.path.exists(drawable_dir):
    for f in os.listdir(drawable_dir):
        if f.endswith('.xml'):
            drawables.append(f[:-4])

print(f"📁 عدد drawables: {len(drawables)}")

# الأولوية للـ drawables اللي كتشبه
preferred = ['bg_btn_primary', 'bg_button_gold', 'bg_gold_btn', 'bg_btn_yellow',
             'bg_btn', 'bg_button', 'bg_primary', 'bg_card']

chosen = None
for p in preferred:
    if p in drawables:
        chosen = p
        break

if not chosen and drawables:
    # نستعملو أول bg_* موجود
    for d in drawables:
        if d.startswith('bg_'):
            chosen = d
            break

if chosen:
    c = c.replace("R.drawable.bg_btn_gold", f"R.drawable.{chosen}")
    print(f"✅ بدلنا bg_btn_gold بـ {chosen}")
else:
    print("⚠️ ما لقيناش drawable بديل")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

# تحقق
print()
print("═══ التحقق ═══")
print(f"  LocaleHelper:  {'❌' if 'LocaleHelper' in c else '✅ حُذف'}")
print(f"  bg_btn_gold:   {'❌' if 'bg_btn_gold' in c else '✅ حُذف'}")
print(f"  الأقواس:       {c.count('{')}/{c.count('}')}")

# نعرضو Drawables الموجودة
print()
print("═══ Drawables الموجودة (bg_*) ═══")
for d in sorted(drawables):
    if d.startswith('bg_'):
        print(f"  • {d}")
