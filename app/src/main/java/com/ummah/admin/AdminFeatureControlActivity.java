package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminFeatureControlActivity extends Activity {

    private AdminFeatureManager fm;
    private LinearLayout featuresContainer;
    private LinearLayout statsContainer;
    private ListenerRegistration featuresListener;

    // الحالة الحالية
    private final Map<String, Boolean> currentState = new HashMap<>();
    private boolean maintenanceMode = false;
    private String maintenanceMessage = "";
    private int wheelMultiplier = 1;
    private int taxRate = 5;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        fm = AdminFeatureManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // ═══ العنوان ═══
        TextView icon = new TextView(this);
        icon.setText("🎛️");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = UiHelper.goldTitle(this, "التحكم في الميزات", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("تفعيل وإيقاف ميزات التطبيق فورياً");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        // ═══ الإحصائيات ═══
        statsContainer = new LinearLayout(this);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(statsContainer);

        // ═══ أزرار سريعة ═══
        addSection(root, "⚡  إجراءات سريعة");

        Button btnEnableAll = UiHelper.secondaryButton(this, "✅  تفعيل كل الميزات");
        btnEnableAll.setOnClickListener(v -> confirmAction(
                "تفعيل كل الميزات؟",
                "كل الميزات رايحين يرجعو مفعلة",
                () -> fm.enableAll(new AdminFeatureManager.OnDone() {
                    @Override public void onSuccess() { toast("✅ تم تفعيل الكل"); }
                    @Override public void onError(String msg) { toast("❌ " + msg); }
                })
        ));
        root.addView(btnEnableAll);

        Button btnDisableAll = UiHelper.dangerButton(this, "❌  إيقاف كل الميزات");
        btnDisableAll.setOnClickListener(v -> confirmAction(
                "إيقاف كل الميزات؟",
                "كل الميزات رايحين يتوقفوا (باستثناء التسجيل)",
                () -> fm.disableAll(new AdminFeatureManager.OnDone() {
                    @Override public void onSuccess() { toast("⏸️ تم الإيقاف"); }
                    @Override public void onError(String msg) { toast("❌ " + msg); }
                })
        ));
        root.addView(btnDisableAll);

        // ═══ قائمة الميزات ═══
        addSection(root, "🎯  الميزات");

        featuresContainer = new LinearLayout(this);
        featuresContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(featuresContainer);

        // ═══ إعدادات متقدمة ═══
        addSection(root, "⚙️  إعدادات متقدمة");

        // المضاعف
        addMultiplierRow(root);
        // الضريبة
        addTaxRow(root);

        // ═══ إجراءات إضافية ═══
        addSection(root, "🚨  إجراءات إضافية");

        Button btnMaintenance = UiHelper.secondaryButton(this, "🔒  وضع الصيانة");
        btnMaintenance.setOnClickListener(v -> showMaintenanceDialog());
        root.addView(btnMaintenance);

        Button btnGiftAll = UiHelper.secondaryButton(this, "🎁  هدية جماعية");
        btnGiftAll.setOnClickListener(v -> showBulkGiftDialog());
        root.addView(btnGiftAll);

        // ═══ Footer ═══
        TextView footer = new TextView(this);
        footer.setText("🔄 التغييرات تُطبق فورياً على كل المواطنين");
        footer.setTextColor(Color.parseColor("#9E9E9E"));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 40, 0, 0);
        root.addView(footer);

        setContentView(scroll);

        // ═══ التحميل ═══
        loadFeatures();
        loadSettings();
    }

    // ═══════════════════════════════════════════
    //  تحميل الميزات
    // ═══════════════════════════════════════════
    private void loadFeatures() {
        featuresListener = fm.listenFeatures(new AdminFeatureManager.FeaturesListener() {
            @Override
            public void onFeatures(Map<String, Boolean> features) {
                currentState.clear();
                currentState.putAll(features);
                renderFeatures();
                renderStats();
            }

            @Override
            public void onError(String msg) {
                toast("❌ " + msg);
            }
        });
    }

    private void renderFeatures() {
        featuresContainer.removeAllViews();
        List<AdminFeatureManager.Feature> list = AdminFeatureManager.getFeatureList();

        for (AdminFeatureManager.Feature f : list) {
            Boolean enabledObj = currentState.get(f.key);
            boolean enabled = enabledObj == null || enabledObj;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundResource(R.drawable.bg_card_premium);
            row.setPadding(30, 24, 30, 24);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.setMargins(0, 8, 0, 8);
            row.setLayoutParams(rowLp);
            row.setElevation(6f);

            // أيقونة
            TextView tvIcon = new TextView(this);
            tvIcon.setText(f.icon);
            tvIcon.setTextSize(28);
            tvIcon.setPadding(0, 0, 20, 0);
            row.addView(tvIcon);

            // الاسم
            TextView tvName = new TextView(this);
            tvName.setText(f.name);
            tvName.setTextColor(Color.WHITE);
            tvName.setTextSize(16);
            tvName.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvName.setLayoutParams(nameLp);
            row.addView(tvName);

            // الحالة
            TextView tvStatus = new TextView(this);
            tvStatus.setText(enabled ? "✅" : "⏸️");
            tvStatus.setTextSize(22);
            tvStatus.setPadding(0, 0, 15, 0);
            row.addView(tvStatus);

            // Switch
            Switch sw = new Switch(this);
            sw.setChecked(enabled);
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    toggleFeature(f.key, f.name, isChecked);
                }
            });
            row.addView(sw);

            featuresContainer.addView(row);
        }
    }

    private void toggleFeature(String key, String name, boolean enabled) {
        fm.setFeature(key, enabled, new AdminFeatureManager.OnDone() {
            @Override
            public void onSuccess() {
                toast((enabled ? "✅ " : "⏸️ ") + name + (enabled ? " مُفعل" : " متوقف"));
                // سجل
                fm.logAction((enabled ? "تفعيل: " : "إيقاف: ") + name,
                        new AdminFeatureManager.OnDone() {
                            @Override public void onSuccess() {}
                            @Override public void onError(String msg) {}
                        });
            }

            @Override
            public void onError(String msg) {
                toast("❌ " + msg);
                // نرجع Switch
                renderFeatures();
            }
        });
    }

    // ═══════════════════════════════════════════
    //  الإحصائيات
    // ═══════════════════════════════════════════
    private void renderStats() {
        statsContainer.removeAllViews();

        int total = AdminFeatureManager.getFeatureList().size();
        int enabled = 0;
        int disabled = 0;

        for (AdminFeatureManager.Feature f : AdminFeatureManager.getFeatureList()) {
            Boolean b = currentState.get(f.key);
            if (b == null || b) enabled++;
            else disabled++;
        }

        // بطاقة إحصائيات
        LinearLayout card = UiHelper.card(this);

        TextView title = new TextView(this);
        title.setText("📊 الحالة العامة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        card.addView(title);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, 20, 0, 0);

        row.addView(statBox(String.valueOf(enabled), "مفعلة", "#4CAF50"));
        row.addView(statBox(String.valueOf(disabled), "متوقفة", "#F44336"));
        row.addView(statBox(String.valueOf(total), "الإجمالي", "#D4AF37"));

        card.addView(row);
        statsContainer.addView(card);
    }

    private LinearLayout statBox(String value, String label, String color) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        box.setLayoutParams(lp);

        TextView tvVal = new TextView(this);
        tvVal.setText(value);
        tvVal.setTextColor(Color.parseColor(color));
        tvVal.setTextSize(28);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setGravity(Gravity.CENTER);
        box.addView(tvVal);

        TextView tvLbl = new TextView(this);
        tvLbl.setText(label);
        tvLbl.setTextColor(Color.parseColor("#9E9E9E"));
        tvLbl.setTextSize(11);
        tvLbl.setGravity(Gravity.CENTER);
        box.addView(tvLbl);

        return box;
    }

    // ═══════════════════════════════════════════
    //  المضاعف والضريبة
    // ═══════════════════════════════════════════
    private void addMultiplierRow(LinearLayout root) {
        LinearLayout card = UiHelper.card(this);

        TextView label = new TextView(this);
        label.setText("🎯 مضاعف مكافآت العجلة");
        label.setTextColor(Color.WHITE);
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        card.addView(label);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 15, 0, 0);

        int[] options = {1, 2, 5, 10};
        for (int opt : options) {
            final int val = opt;
            Button b = new Button(this);
            b.setText("×" + opt);
            b.setTextSize(14);
            b.setAllCaps(false);
            b.setTextColor(Color.parseColor("#D4AF37"));
            b.setBackgroundResource(R.drawable.bg_btn_outline);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(4, 0, 4, 0);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> {
                fm.setWheelMultiplier(val, new AdminFeatureManager.OnDone() {
                    @Override public void onSuccess() {
                        wheelMultiplier = val;
                        toast("🎯 المضاعف: ×" + val);
                    }
                    @Override public void onError(String msg) { toast("❌ " + msg); }
                });
            });
            row.addView(b);
        }

        card.addView(row);
        root.addView(card);
    }

    private void addTaxRow(LinearLayout root) {
        LinearLayout card = UiHelper.card(this);

        TextView label = new TextView(this);
        label.setText("💰 ضريبة التحويلات");
        label.setTextColor(Color.WHITE);
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        card.addView(label);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 15, 0, 0);

        int[] options = {0, 2, 5, 10};
        for (int opt : options) {
            final int val = opt;
            Button b = new Button(this);
            b.setText(val + "%");
            b.setTextSize(14);
            b.setAllCaps(false);
            b.setTextColor(Color.parseColor("#D4AF37"));
            b.setBackgroundResource(R.drawable.bg_btn_outline);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(4, 0, 4, 0);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> {
                fm.setTaxRate(val, new AdminFeatureManager.OnDone() {
                    @Override public void onSuccess() {
                        taxRate = val;
                        toast("💰 الضريبة: " + val + "%");
                    }
                    @Override public void onError(String msg) { toast("❌ " + msg); }
                });
            });
            row.addView(b);
        }

        card.addView(row);
        root.addView(card);
    }

    // ═══════════════════════════════════════════
    //  وضع الصيانة
    // ═══════════════════════════════════════════
    private void showMaintenanceDialog() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 30, 40, 20);

        TextView hint = new TextView(this);
        hint.setText("اكتب رسالة الصيانة:");
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(13);
        c.addView(hint);

        EditText input = new EditText(this);
        input.setHint("مثال: صيانة مؤقتة، نرجع بعد قليل");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams inpLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inpLp.setMargins(0, 15, 0, 0);
        input.setLayoutParams(inpLp);
        c.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("🔒 وضع الصيانة")
                .setView(c)
                .setPositiveButton("🔴 تشغيل", (d, w) -> {
                    String msg = input.getText().toString().trim();
                    if (msg.isEmpty()) msg = "صيانة مؤقتة";
                    fm.setMaintenanceMode(true, msg, new AdminFeatureManager.OnDone() {
                        @Override public void onSuccess() { toast("🔒 وضع الصيانة مفعل"); }
                        @Override public void onError(String m) { toast("❌ " + m); }
                    });
                })
                .setNeutralButton("🟢 إيقاف", (d, w) -> {
                    fm.setMaintenanceMode(false, "", new AdminFeatureManager.OnDone() {
                        @Override public void onSuccess() { toast("✅ وضع الصيانة متوقف"); }
                        @Override public void onError(String m) { toast("❌ " + m); }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ═══════════════════════════════════════════
    //  هدية جماعية
    // ═══════════════════════════════════════════
    private void showBulkGiftDialog() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(40, 30, 40, 20);

        TextView hint = new TextView(this);
        hint.setText("💰 المبلغ:");
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(13);
        c.addView(hint);

        EditText amount = new EditText(this);
        amount.setHint("100");
        amount.setTextColor(Color.WHITE);
        amount.setHintTextColor(Color.parseColor("#666666"));
        amount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        amount.setPadding(30, 30, 30, 30);
        c.addView(amount);

        TextView hint2 = new TextView(this);
        hint2.setText("📝 السبب:");
        hint2.setTextColor(Color.parseColor("#9E9E9E"));
        hint2.setTextSize(13);
        hint2.setPadding(0, 20, 0, 0);
        c.addView(hint2);

        EditText reason = new EditText(this);
        reason.setHint("حدث خاص");
        reason.setTextColor(Color.WHITE);
        reason.setHintTextColor(Color.parseColor("#666666"));
        reason.setPadding(30, 30, 30, 30);
        c.addView(reason);

        new AlertDialog.Builder(this)
                .setTitle("🎁 هدية جماعية")
                .setView(c)
                .setPositiveButton("إرسال للكل", (d, w) -> {
                    int amt;
                    try {
                        amt = Integer.parseInt(amount.getText().toString().trim());
                    } catch (Exception e) {
                        toast("❌ مبلغ غير صحيح");
                        return;
                    }
                    String rsn = reason.getText().toString().trim();
                    if (rsn.isEmpty()) rsn = "حدث خاص";

                    final int finalAmt = amt;
                    final String finalRsn = rsn;
                    fm.sendGiftToAll(amt, rsn, new AdminFeatureManager.OnDone() {
                        @Override public void onSuccess() {
                            toast("🎁 هدية " + finalAmt + " Đ للكل");
                        }
                        @Override public void onError(String m) { toast("❌ " + m); }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ═══════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════
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

    private void confirmAction(String title, String msg, Runnable onYes) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("نعم", (d, w) -> onYes.run())
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void loadSettings() {
        fm.loadSettings(new AdminFeatureManager.SettingsListener() {
            @Override
            public void onSettings(boolean mMode, String mMsg, int mult, int tax) {
                maintenanceMode = mMode;
                maintenanceMessage = mMsg;
                wheelMultiplier = mult;
                taxRate = tax;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (featuresListener != null) featuresListener.remove();
    }
}
