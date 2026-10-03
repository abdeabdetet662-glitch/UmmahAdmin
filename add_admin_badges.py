#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""إضافة Badges للـ Admin App — شكاوى، رسائل، إبلاغات"""
import os

# ═══════════════════════════════════════════
# 1. AdminBadgeHelper.java
# ═══════════════════════════════════════════
BADGE_FILE = "app/src/main/java/com/ummah/admin/AdminBadgeHelper.java"

badge_code = '''package com.ummah.admin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * AdminBadgeHelper — Badges للوحة التحكم
 */
public class AdminBadgeHelper {

    /** إنشاء TextView للـ Badge */
    public static TextView createBadge(Context ctx, int count) {
        TextView badge = new TextView(ctx);
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.bg_badge_red);
        badge.setElevation(10f);
        int size = dp(ctx, 20);
        badge.setMinWidth(size);
        badge.setMinHeight(size);
        badge.setPadding(dp(ctx, 6), 0, dp(ctx, 6), 0);
        return badge;
    }

    /** لفّ الـ View مع Badge (للأزرار) */
    public static FrameLayout withBadge(Activity act, View view, int count) {
        FrameLayout wrapper = new FrameLayout(act);
        wrapper.setClipChildren(false);
        wrapper.setClipToPadding(false);

        FrameLayout.LayoutParams vLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(vLp);
        wrapper.addView(view);

        if (count > 0) {
            TextView badge = createBadge(act, count);
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            blp.gravity = Gravity.TOP | Gravity.END;
            blp.setMargins(0, dp(act, 8), dp(act, 20), 0);
            badge.setLayoutParams(blp);
            wrapper.addView(badge);
        }

        LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(wrapperLp);
        return wrapper;
    }

    /** تحديث Badge أيقونة التطبيق */
    public static void updateAppBadge(Context ctx, int count) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                android.app.NotificationManager nm =
                    (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null && count > 0) {
                    android.app.Notification.Builder builder =
                        new android.app.Notification.Builder(ctx, "admin_badge")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("لوحة تحكم أُمّة")
                            .setContentText(count + " عنصر جديد")
                            .setNumber(count)
                            .setAutoCancel(true);
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        builder.setChannelId("admin_badge");
                    }
                    nm.notify(888001, builder.build());
                } else if (nm != null) {
                    nm.cancel(888001);
                }
            }
        } catch (Exception ignored) {}
    }

    /** إنشاء قناة Badge */
    public static void createChannels(Context ctx) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                android.app.NotificationManager nm =
                    (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    android.app.NotificationChannel ch = new android.app.NotificationChannel(
                            "admin_badge", "Badges الإدارة", android.app.NotificationManager.IMPORTANCE_LOW);
                    ch.setShowBadge(true);
                    nm.createNotificationChannel(ch);
                }
            }
        } catch (Exception ignored) {}
    }

    private static int dp(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
}
'''

with open(BADGE_FILE, "w", encoding="utf-8") as f:
    f.write(badge_code)
print("✅ AdminBadgeHelper.java جاهز")

# ═══════════════════════════════════════════
# 2. تعديل AdminMainActivity
# ═══════════════════════════════════════════
MAIN = "app/src/main/java/com/ummah/admin/AdminMainActivity.java"

with open(MAIN, "r", encoding="utf-8") as f:
    c = f.read()

# 2.1 Fields
if "unreadComplaints" not in c:
    c = c.replace(
        "private LinearLayout statsContainer;",
        """private LinearLayout statsContainer;
    private int unreadComplaints = 0;
    private int unreadMessages = 0;
    private int unreadReports = 0;
    private com.google.firebase.firestore.ListenerRegistration unreadReg;"""
    )
    print("✅ أضفنا fields")

# 2.2 تعديل استدعاءات الأزرار لتشمل badges
# ⚠️ نبغيو ننشئو نسخة جديدة من الأزرار مع badge

# نستبدلو الـ 3 استدعاءات
replacements = [
    (
        'addSecondaryBtn(root, "💌  رسائل خاصة", AdminPrivateMessagesActivity.class);',
        'addSecondaryBtnBadge(root, "💌  رسائل خاصة", AdminPrivateMessagesActivity.class, unreadMessages);'
    ),
    (
        'addSecondaryBtn(root, "📢  الإبلاغات", AdminReportsActivity.class);',
        'addSecondaryBtnBadge(root, "📢  الإبلاغات", AdminReportsActivity.class, unreadReports);'
    ),
    (
        'addSecondaryBtn(root, "⚖️  الشكاوى", AdminComplaintsActivity.class);',
        'addSecondaryBtnBadge(root, "⚖️  الشكاوى", AdminComplaintsActivity.class, unreadComplaints);'
    ),
]

for old, new in replacements:
    if old in c:
        c = c.replace(old, new)
        print(f"✅ عدلنا: {old[:40]}...")

# 2.3 نضيفو دالة addSecondaryBtnBadge
if "addSecondaryBtnBadge" not in c.split("private void addSecondaryBtnBadge")[0][-500:]:
    old_method = '''private void addSecondaryBtn(LinearLayout root, String text, final Class<?> cls) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        root.addView(btn);
    }'''
    
    new_method = '''private void addSecondaryBtn(LinearLayout root, String text, final Class<?> cls) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        root.addView(btn);
    }
    
    /** نسخة مع Badge */
    private void addSecondaryBtnBadge(LinearLayout root, String text, final Class<?> cls, int badgeCount) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        
        if (badgeCount > 0) {
            root.addView(AdminBadgeHelper.withBadge(this, btn, badgeCount));
        } else {
            root.addView(btn);
        }
    }'''
    
    if old_method in c:
        c = c.replace(old_method, new_method)
        print("✅ أضفنا addSecondaryBtnBadge")

# 2.4 نضيفو مستمع Firestore + إنشاء channels في onCreate
if "AdminBadgeHelper.createChannels" not in c:
    # نضيفو قبل "setContentView(scroll);"
    marker = "setContentView(scroll);"
    listener_code = '''
        // ═══ Badges للإدارة ═══
        AdminBadgeHelper.createChannels(this);
        
        unreadReg = com.google.firebase.firestore.FirebaseFirestore
                .getInstance()
                .collection("unread_admin")
                .document("main")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || !snap.exists()) return;
                    
                    Long complaints = snap.getLong("complaints");
                    Long messages = snap.getLong("messages");
                    Long reports = snap.getLong("reports");
                    
                    unreadComplaints = complaints != null ? complaints.intValue() : 0;
                    unreadMessages = messages != null ? messages.intValue() : 0;
                    unreadReports = reports != null ? reports.intValue() : 0;
                    
                    int total = unreadComplaints + unreadMessages + unreadReports;
                    AdminBadgeHelper.updateAppBadge(this, total);
                    
                    // نعيد بناء الواجهة
                    runOnUiThread(() -> rebuildForBadges());
                });
        
        '''
    
    if marker in c:
        c = c.replace(marker, listener_code + marker, 1)
        print("✅ أضفنا مستمع unread_admin")

# 2.5 نضيفو دالة rebuildForBadges (بسيطة: تعيد تشغيل onCreate)
if "rebuildForBadges" not in c:
    old_create_end = '''        setContentView(scroll);
        loadStats();'''
    
    new_create_end = '''        setContentView(scroll);
        loadStats();'''
    
    # نضيفو دالة rebuildForBadges قبل آخر }
    last = c.rfind("}")
    rebuild_method = '''
    
    /** إعادة بناء بسيطة لتحديث Badges */
    private void rebuildForBadges() {
        recreate();
    }
    
    @Override
    protected void onDestroy() {
        if (unreadReg != null) unreadReg.remove();
        super.onDestroy();
    }
'''
    c = c[:last] + rebuild_method + c[last:]
    print("✅ أضفنا rebuildForBadges + onDestroy")

# 2.6 Imports إضافية
if "import android.widget.FrameLayout;" not in c:
    c = c.replace(
        "import android.widget.Button;",
        "import android.widget.Button;\nimport android.widget.FrameLayout;"
    )
    print("✅ أضفنا import FrameLayout")

with open(MAIN, "w", encoding="utf-8") as f:
    f.write(c)

print(f"📊 الأقواس: {{ = {c.count('{')}, }} = {c.count('}')}")
print()
print("═══════════════════════════════════════════")
print("✅ تم إضافة Badges للـ Admin App")
print("═══════════════════════════════════════════")
