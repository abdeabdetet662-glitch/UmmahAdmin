package com.ummah.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AdminMainActivity extends Activity {

    private AdminManager am;
    private LinearLayout statsContainer;

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

        addSection(root, "👥  المواطنون");
        addSecondaryBtn(root, "👥  قائمة المواطنين", AdminCitizensActivity.class);
        addSecondaryBtn(root, "🚫  المحظورون", AdminBlockedActivity.class);
        addSecondaryBtn(root, "🔇  المكتومون", AdminMutedActivity.class);
        addSecondaryBtn(root, "💌  رسائل خاصة", AdminPrivateMessagesActivity.class);

        addSection(root, "💬  المحتوى");
        addSecondaryBtn(root, "💬  إدارة الدردشة", AdminChatActivity.class);
        addSecondaryBtn(root, "📢  الإبلاغات", AdminReportsActivity.class);
        addSecondaryBtn(root, "⚖️  الشكاوى", AdminComplaintsActivity.class);
        addSecondaryBtn(root, "📰  الأخبار", AdminNewsActivity.class);

        addSection(root, "💰  الاقتصاد");
        addSecondaryBtn(root, "🏦  الخزينة العامة", AdminTreasuryActivity.class);
        addSecondaryBtn(root, "🛒  إدارة السوق", AdminMarketActivity.class);

        addSection(root, "🏛️  الحكم");
        addSecondaryBtn(root, "🗳️  التصويت على الدستور", AdminVotesActivity.class);
        addSecondaryBtn(root, "📰  الاقتراحات", AdminProposalsActivity.class);
        addSecondaryBtn(root, "👑  الانتخابات", AdminElectionActivity.class);

        addSection(root, "🔧  النظام");
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

        setContentView(scroll);
        loadStats();
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
}
