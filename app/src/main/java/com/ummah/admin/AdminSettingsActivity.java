package com.ummah.admin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminSettingsActivity extends Activity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "⚙️  الإعدادات", 26);
        root.addView(title);

        LinearLayout card = UiHelper.card(this);

        TextView lbl = new TextView(this);
        lbl.setText("🎛️  إعدادات النظام");
        lbl.setTextColor(Color.parseColor("#D4AF37"));
        lbl.setTextSize(16);
        lbl.setTypeface(null, Typeface.BOLD);
        lbl.setGravity(Gravity.CENTER);
        lbl.setPadding(0, 0, 0, 16);
        card.addView(lbl);

        addToggle(card, "chat_enabled", "💬 تفعيل الدردشة", true);
        addToggle(card, "market_enabled", "🛒 تفعيل السوق", true);
        addToggle(card, "jobs_enabled", "💼 تفعيل الوظائف", true);
        addToggle(card, "elections_enabled", "👑 تفعيل الانتخابات", true);

        root.addView(card);

        LinearLayout card2 = UiHelper.card(this);

        TextView lbl2 = new TextView(this);
        lbl2.setText("ℹ️  معلومات النظام");
        lbl2.setTextColor(Color.parseColor("#D4AF37"));
        lbl2.setTextSize(16);
        lbl2.setTypeface(null, Typeface.BOLD);
        lbl2.setGravity(Gravity.CENTER);
        lbl2.setPadding(0, 0, 0, 16);
        card2.addView(lbl2);

        addInfo(card2, "إصدار لوحة التحكم", "v2.0");
        addInfo(card2, "Firebase Project", "ummah-15bad");
        addInfo(card2, "Admin Password", AdminManager.ADMIN_PASSWORD);

        root.addView(card2);

        Button logoutBtn = UiHelper.dangerButton(this, "🚪  تسجيل الخروج");
        logoutBtn.setOnClickListener(v -> {
            getSharedPreferences("admin", MODE_PRIVATE).edit().clear().apply();
            finish();
        });
        root.addView(logoutBtn);

        setContentView(scroll);
    }

    private void addToggle(LinearLayout parent, final String key, String label, boolean defaultVal) {
        SharedPreferences prefs = getSharedPreferences("admin_settings", MODE_PRIVATE);
        final boolean current = prefs.getBoolean(key, defaultVal);

        Button btn = new Button(this);
        btn.setText((current ? "✅ " : "❌ ") + label);
        btn.setTextSize(14);
        btn.setAllCaps(false);
        btn.setTextColor(current ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        btn.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        btn.setLayoutParams(lp);
        btn.setPadding(30, 24, 30, 24);

        btn.setOnClickListener(v -> {
            boolean newVal = !prefs.getBoolean(key, defaultVal);
            prefs.edit().putBoolean(key, newVal).apply();

            Map<String, Object> d = new HashMap<>();
            d.put(key, newVal);
            db.collection("app_config").document("features")
                    .set(d, com.google.firebase.firestore.SetOptions.merge());

            btn.setText((newVal ? "✅ " : "❌ ") + label);
            btn.setTextColor(newVal ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
            Toast.makeText(this, (newVal ? "✅ فُعّل" : "❌ أُوقف"), Toast.LENGTH_SHORT).show();
        });

        parent.addView(btn);
    }

    private void addInfo(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 10, 0, 10);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextColor(Color.parseColor("#9E9E9E"));
        l.setTextSize(13);
        l.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(l);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(Color.WHITE);
        v.setTextSize(13);
        v.setTypeface(null, Typeface.BOLD);
        row.addView(v);

        parent.addView(row);
    }
}
