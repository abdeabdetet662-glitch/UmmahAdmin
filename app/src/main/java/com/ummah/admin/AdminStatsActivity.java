package com.ummah.admin;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AdminStatsActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        AdminManager am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "📊  إحصائيات شاملة", 26);
        root.addView(title);

        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);

        am.loadStats(new AdminManager.StatsListener() {
            @Override public void onStats(final AdminManager.Stats s) {
                runOnUiThread(() -> {
                    addCard(container, "👥", "المواطنون المسجلون", String.valueOf(s.citizens), "#0D47A1");
                    addCard(container, "🟢", "متصلون الآن", String.valueOf(s.onlineNow), "#2E7D32");
                    addCard(container, "💰", "إجمالي الرصيد المتداول", s.totalBalance + " Đ", "#D4AF37");
                    addCard(container, "🏦", "رصيد الخزينة", s.treasury + " Đ", "#1A237E");
                    addCard(container, "🎁", "الهدايا المرسلة", String.valueOf(s.gifts), "#C2185B");
                    addCard(container, "💸", "التحويلات", String.valueOf(s.transfers), "#1B5E20");
                    addCard(container, "📰", "الأخبار", String.valueOf(s.news), "#4A148C");
                    addCard(container, "🗳️", "الاقتراحات", String.valueOf(s.proposals), "#00695C");
                    addCard(container, "⚖️", "الشكاوى", String.valueOf(s.complaints), "#5D4037");
                });
            }
        });
    }

    private void addCard(LinearLayout parent, String emoji, String label, String value, String color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(30, 24, 30, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        TextView e = new TextView(this);
        e.setText(emoji);
        e.setTextSize(32);
        e.setPadding(0, 0, 20, 0);
        card.addView(e);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.WHITE);
        l.setTextSize(15);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.parseColor(color));
        v.setTextSize(20);
        v.setTypeface(null, Typeface.BOLD);
        card.addView(v);

        parent.addView(card);
    }
}
