#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إصلاح sendToAll: loop على كل المواطنين"""
import re

PATH = "app/src/main/java/com/ummah/admin/AdminNotifyActivity.java"

with open(PATH, "r", encoding="utf-8") as f:
    c = f.read()

# نلقاو دالة sendToAll كاملة
match = re.search(
    r'(private void sendToAll\(String title, String message, String type,\s*\n\s*String action, String emoji\) \{)(.*?)(\n\s*\})',
    c,
    re.DOTALL
)

if not match:
    # نجربو صيغة أخرى
    match = re.search(
        r'(private void sendToAll\([^)]+\)\s*\{)(.*?)(\n\s*\n\s*private|\n\s*\n\s*//|\n\s*\n\s*@)',
        c,
        re.DOTALL
    )

if not match:
    print("❌ ما لقيناش sendToAll")
    exit(1)

print("✅ لقينا sendToAll")

# الكود الجديد
NEW_METHOD = '''private void sendToAll(String title, String message, String type,
                            String action, String emoji) {
        
        // نجيبو كل المواطنين
        db.collection("citizens").get()
                .addOnSuccessListener(snapshot -> {
                    int total = snapshot.size();
                    if (total == 0) {
                        toast("⚠️ ما فيهش مواطنين");
                        return;
                    }
                    
                    toast("📤 جاري إرسال " + total + " إشعار...");
                    
                    final int[] success = {0};
                    final int[] failed = {0};
                    long now = System.currentTimeMillis();
                    
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nationalId = doc.getString("nationalId");
                        if (nationalId == null || nationalId.isEmpty()) {
                            failed[0]++;
                            continue;
                        }
                        
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("title", title);
                        data.put("message", message);
                        data.put("type", type);
                        data.put("action", action);
                        data.put("emoji", emoji);
                        data.put("target", nationalId);
                        data.put("timestamp", now);
                        data.put("from", "admin");
                        
                        db.collection(COLLECTION).add(data)
                                .addOnSuccessListener(d -> success[0]++)
                                .addOnFailureListener(e -> failed[0]++);
                    }
                    
                    // نعرضو النتيجة بعد ثانيتين
                    new android.os.Handler().postDelayed(() -> {
                        toast("✅ تم إرسال " + success[0] + " من " + total + " إشعار");
                        clearForm();
                    }, 2500);
                })
                .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
    }'''

# نستبدلو
c = c[:match.start()] + NEW_METHOD + c[match.end():]

with open(PATH, "w", encoding="utf-8") as f:
    f.write(c)

print("✅ تم تحديث sendToAll")
print()
print("📊 الفرق:")
print("   قبل: target = 'ALL' (ما يوصلش)")
print("   بعد: loop على كل المواطنين + target = nationalId")

# توازن
o = c.count('{')
cl = c.count('}')
print()
print(f"📊 الأقواس: {{ = {o}, }} = {cl}")
