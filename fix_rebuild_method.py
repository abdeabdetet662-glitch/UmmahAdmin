#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إضافة rebuildForBadges + onDestroy اللي نقصو"""
import re

PATH = "app/src/main/java/com/ummah/admin/AdminMainActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# ═══ 1. نتأكدو واش الدالة معرّفة (ماشي مستدعاة) ═══
has_definition = "private void rebuildForBadges()" in c

if has_definition:
    print("✅ rebuildForBadges معرّفة")
else:
    # نضيفو قبل آخر }
    last = c.rfind("}")
    
    methods = '''
    
    /** إعادة بناء الشاشة لتحديث Badges */
    private void rebuildForBadges() {
        recreate();
    }
    
    @Override
    protected void onDestroy() {
        if (unreadReg != null) {
            unreadReg.remove();
            unreadReg = null;
        }
        super.onDestroy();
    }
'''
    c = c[:last] + methods + c[last:]
    print("✅ أضفنا rebuildForBadges + onDestroy")

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

# ═══ التحقق ═══
with open(PATH, "r", encoding="utf-8") as f:
    c2 = f.read()

print()
print("═══ التحقق ═══")
print(f"  rebuildForBadges معرّفة: {'✅' if 'private void rebuildForBadges()' in c2 else '❌'}")
print(f"  onDestroy معرّفة:        {'✅' if 'protected void onDestroy()' in c2 else '❌'}")
print(f"  الأقواس:                 {{ = {c2.count('{')}, }} = {c2.count('}')}")
