package com.ummah.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AdminMainActivity extends Activity {

    private AdminManager am;
    private LinearLayout statsContainer;
    private int unreadComplaints = 0;
    private int unreadMessages = 0;
    private int unreadReports = 0;
    private com.google.firebase.firestore.ListenerRegistration unreadReg;
    private android.widget.TextView badgeComplaints;
    private android.widget.TextView badgeMessages;
    private android.widget.TextView badgeReports;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("👑");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = UiHelper.goldTitle(this, "لوحة تحكم أُمّة", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("الإدارة العليا للدولة الرقمية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        statsContainer = new LinearLayout(this);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(statsContainer);

        addSection(root, "⚙️  الإدارة");
        addPrimaryBtn(root, "📊  إحصائيات شاملة", AdminStatsActivity.class);
        addPrimaryBtn(root, "📢  البث العام", AdminBroadcastActivity.class);
        addPrimaryBtn(root, "📜  سجل النشاط", AdminLogActivity.class);
        addPrimaryBtn(root, "👑  إدارة الرؤساء", AdminPresidentActivity.class);

        addSection(root, "👥  المواطنون");
        addSecondaryBtn(root, "👥  قائمة المواطنين", AdminCitizensActivity.class);
        addSecondaryBtn(root, "🚫  المحظورون", AdminBlockedActivity.class);
        addSecondaryBtn(root, "🔇  المكتومون", AdminMutedActivity.class);
        addSecondaryBtnBadge(root, "💌  رسائل خاصة", AdminPrivateMessagesActivity.class, unreadMessages, 2);

        addSection(root, "💬  المحتوى");
        addSecondaryBtn(root, "💬  إدارة الدردشة", AdminChatActivity.class);
        addSecondaryBtnBadge(root, "📢  الإبلاغات", AdminReportsActivity.class, unreadReports, 3);
        addSecondaryBtnBadge(root, "⚖️  الشكاوى", AdminComplaintsActivity.class, unreadComplaints, 1);
        addSecondaryBtn(root, "📰  الأخبار", AdminNewsActivity.class);
        addSecondaryBtn(root, "🕵️  جريمة أُمّة", AdminMurderMysteryActivity.class);

        addSection(root, "💰  الاقتصاد");
        addSecondaryBtn(root, "🏦  الخزينة العامة", AdminTreasuryActivity.class);
        addSecondaryBtn(root, "🛒  إدارة السوق", AdminMarketActivity.class);

        addSection(root, "🏛️  الحكم");
        addSecondaryBtn(root, "🗳️  التصويت على الدستور", AdminVotesActivity.class);
        addSecondaryBtn(root, "📰  الاقتراحات", AdminProposalsActivity.class);
        addSecondaryBtn(root, "👑  الانتخابات", AdminElectionActivity.class);

        addSection(root, "🎁  الهدايا والأكواد");
        addPrimaryBtn(root, "💰  إرسال هدايا", AdminGiftActivity.class);
        addPrimaryBtn(root, "🎫  توليد أكواد", AdminCodesActivity.class);
        addPrimaryBtn(root, "🔔  إرسال إشعار", AdminNotifyActivity.class);

        addSection(root, "🔧  النظام");
        addSecondaryBtn(root, "🎛️  التحكم في الميزات", AdminFeatureControlActivity.class);
        addSecondaryBtn(root, "⚙️  الإعدادات", AdminSettingsActivity.class);

        Button logout = new Button(this);
        logout.setText("🚪  تسجيل الخروج");
        logout.setTextSize(14);
        logout.setTextColor(Color.parseColor("#F44336"));
        logout.setAllCaps(false);
        logout.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 40, 0, 0);
        logout.setLayoutParams(lp);
        logout.setPadding(40, 30, 40, 30);
        logout.setOnClickListener(v -> {
            getSharedPreferences("admin", MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });
        root.addView(logout);

        
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
                    runOnUiThread(() -> updateBadgesOnly());
                });
        
        setContentView(scroll);
        loadStats();

        // ═══ عرض UID الأدمن (مؤقت — للربط بـ admins collection) ═══
        com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener(result -> {
                    // ✅ تم تسجيل الدخول
                });
    }

    private void addSection(LinearLayout root, String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 12);

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(this);
        t.setText("  " + text + "  ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(16);
        t.setTypeface(null, Typeface.BOLD);
        container.addView(t);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineR);

        root.addView(container);
    }

    private void addPrimaryBtn(LinearLayout root, String text, final Class<?> cls) {
        Button btn = UiHelper.primaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        root.addView(btn);
    }

    private void addSecondaryBtn(LinearLayout root, String text, final Class<?> cls) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        root.addView(btn);
    }
    
    /** نسخة مع Badge */
    private void addSecondaryBtnBadge(LinearLayout root, String text, final Class<?> cls, int badgeCount, int tag) {
        Button btn = UiHelper.secondaryButton(this, text);
        btn.setOnClickListener(v -> startActivity(new Intent(this, cls)));
        
        FrameLayout wrapper = new FrameLayout(this);
        wrapper.setClipChildren(false);
        wrapper.setClipToPadding(false);
        
        FrameLayout.LayoutParams vLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        btn.setLayoutParams(vLp);
        wrapper.addView(btn);
        
        // نضيفو Badge دائماً (حتى لو = 0) باش نحدّثوه بعدين
        android.widget.TextView badge = AdminBadgeHelper.createBadge(this, badgeCount);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        blp.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        blp.setMargins(0, dp(8), dp(20), 0);
        badge.setLayoutParams(blp);
        badge.setVisibility(badgeCount > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        wrapper.addView(badge);
        
        // نحفظو الـ reference حسب tag
        switch (tag) {
            case 1: badgeComplaints = badge; break;
            case 2: badgeMessages = badge; break;
            case 3: badgeReports = badge; break;
        }
        
        LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(wrapperLp);
        root.addView(wrapper);
    }
    
    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }
    
    /** تحديث Badges بدون recreate */
    private void updateBadgesOnly() {
        if (badgeComplaints != null) {
            badgeComplaints.setText(unreadComplaints > 99 ? "99+" : String.valueOf(unreadComplaints));
            badgeComplaints.setVisibility(unreadComplaints > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (badgeMessages != null) {
            badgeMessages.setText(unreadMessages > 99 ? "99+" : String.valueOf(unreadMessages));
            badgeMessages.setVisibility(unreadMessages > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        if (badgeReports != null) {
            badgeReports.setText(unreadReports > 99 ? "99+" : String.valueOf(unreadReports));
            badgeReports.setVisibility(unreadReports > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

    private void loadStats() {
        am.loadStats(new AdminManager.StatsListener() {
            @Override public void onStats(int citizens, int gifts, int transfers, int news,
                                           int proposals, int complaints, long treasury, int votes) {
                runOnUiThread(() -> {
                    statsContainer.removeAllViews();
                    addStat(statsContainer, "👥", "المواطنون", String.valueOf(citizens), "#0D47A1");
                    addStat(statsContainer, "🎁", "الهدايا", String.valueOf(gifts), "#C2185B");
                    addStat(statsContainer, "💸", "التحويلات", String.valueOf(transfers), "#1B5E20");
                    addStat(statsContainer, "🏦", "الخزينة", treasury + " Đ", "#1A237E");
                });
            }
        });
    }

    private void addStat(LinearLayout parent, String emoji, String label, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 18, 24, 18);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(24);
        e.setPadding(0, 0, 16, 0);
        card.addView(e);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.WHITE);
        l.setTextSize(14);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor(color));
        v.setTextSize(16);
        v.setTypeface(null, Typeface.BOLD);
        card.addView(v);

        parent.addView(card);
    }

        
    @Override
    protected void onDestroy() {
        if (unreadReg != null) {
            unreadReg.remove();
            unreadReg = null;
        }
        super.onDestroy();
    }
}
