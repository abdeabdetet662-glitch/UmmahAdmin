package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminWheelActivity extends Activity {

    private FirebaseFirestore db;
    private LinearLayout root;
    private TextView statusView;
    private TextView winnerView;
    private boolean jackpotEnabled = true;
    private boolean jackpotClaimed = false;
    private String jackpotWinnerId = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        setContentView(buildUI());
        loadConfig();
    }

    private View buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0510"));
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(60), dp(20), dp(40));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🎡 التحكم في العجلة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("إدارة الجاكبوت والمكافآت");
        sub.setTextColor(Color.parseColor("#9CA3AF"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(8), 0, dp(32));
        root.addView(sub);

        // بطاقة الحالة
        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setBackgroundColor(Color.parseColor("#1A0F2E"));
        statusCard.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView statusLabel = new TextView(this);
        statusLabel.setText("📊 الحالة الحالية");
        statusLabel.setTextColor(Color.parseColor("#D4AF37"));
        statusLabel.setTextSize(14);
        statusLabel.setTypeface(null, Typeface.BOLD);
        statusCard.addView(statusLabel);

        statusView = new TextView(this);
        statusView.setText("جارٍ التحميل...");
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(14);
        statusView.setPadding(0, dp(12), 0, 0);
        statusView.setLineSpacing(0, 1.4f);
        statusCard.addView(statusView);

        root.addView(statusCard);

        // زر تبديل الحالة
        Button toggleBtn = new Button(this);
        toggleBtn.setText("🔄 تبديل حالة الجاكبوت");
        toggleBtn.setTextColor(Color.parseColor("#0A0510"));
        toggleBtn.setBackgroundColor(Color.parseColor("#D4AF37"));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, dp(56));
        tlp.topMargin = dp(20);
        toggleBtn.setLayoutParams(tlp);
        toggleBtn.setOnClickListener(v -> toggleJackpot());
        root.addView(toggleBtn);

        // زر اختيار الفائز
        Button winnerBtn = new Button(this);
        winnerBtn.setText("👑 اختيار الفائز");
        winnerBtn.setTextColor(Color.WHITE);
        winnerBtn.setBackgroundColor(Color.parseColor("#9333EA"));
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(-1, dp(56));
        wlp.topMargin = dp(12);
        winnerBtn.setLayoutParams(wlp);
        winnerBtn.setOnClickListener(v -> selectWinner());
        root.addView(winnerBtn);

        // زر إعادة تعيين
        Button resetBtn = new Button(this);
        resetBtn.setText("🔄 إعادة تعيين الجاكبوت");
        resetBtn.setTextColor(Color.WHITE);
        resetBtn.setBackgroundColor(Color.parseColor("#DC2626"));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(-1, dp(56));
        rlp.topMargin = dp(12);
        resetBtn.setLayoutParams(rlp);
        resetBtn.setOnClickListener(v -> confirmReset());
        root.addView(resetBtn);

        return scroll;
    }

    private void loadConfig() {
        db.collection("wheel_config").document("main").get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) return;
                Boolean je = doc.getBoolean("jackpotEnabled");
                Boolean jc = doc.getBoolean("jackpotClaimed");
                String jw = doc.getString("jackpotWinnerId");
                jackpotEnabled = je != null ? je : true;
                jackpotClaimed = jc != null ? jc : false;
                jackpotWinnerId = jw != null ? jw : "";
                renderStatus();
            });
    }

    private void renderStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎛️ الجاكبوت: ").append(jackpotEnabled ? "✅ مُفعّل" : "❌ معطّل").append("\n");
        sb.append("🎁 القيمة: 5,000 Đ\n");
        if (jackpotClaimed) {
            sb.append("❌ تم استلامه من: ").append(jackpotWinnerId);
        } else if (jackpotWinnerId.isEmpty()) {
            sb.append("👑 الفائز: (ما تم تحديده بعد)");
        } else {
            sb.append("👑 الفائز المُحدد: ").append(jackpotWinnerId);
        }
        statusView.setText(sb.toString());
    }

    private void toggleJackpot() {
        jackpotEnabled = !jackpotEnabled;
        Map<String, Object> upd = new HashMap<>();
        upd.put("jackpotEnabled", jackpotEnabled);
        db.collection("wheel_config").document("main").update(upd)
            .addOnSuccessListener(a -> {
                Toast.makeText(this, "✅ الجاكبوت " + (jackpotEnabled ? "مُفعّل" : "معطّل"),
                    Toast.LENGTH_SHORT).show();
                renderStatus();
            });
    }

    private void selectWinner() {
        db.collection("citizens").limit(100).get()
            .addOnSuccessListener(q -> {
                List<String> ids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                q.forEach(doc -> {
                    String id = doc.getId();
                    String name = doc.getString("name");
                    ids.add(id);
                    names.add(name != null ? name + " (" + id + ")" : id);
                });

                new AlertDialog.Builder(this)
                    .setTitle("اختر الفائز")
                    .setItems(names.toArray(new String[0]), (d, w) -> {
                        String selected = ids.get(w);
                        db.collection("wheel_config").document("main").update(
                            "jackpotWinnerId", selected,
                            "jackpotClaimed", false
                        ).addOnSuccessListener(a -> {
                            jackpotWinnerId = selected;
                            jackpotClaimed = false;
                            Toast.makeText(this, "👑 تم تحديد الفائز: " + selected,
                                Toast.LENGTH_LONG).show();
                            renderStatus();
                        });
                    })
                    .show();
            });
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
            .setTitle("🔄 إعادة تعيين")
            .setMessage("هل تريد إعادة تعيين الجاكبوت؟ راح يمسح الفائز ويصير متاح للكل.")
            .setPositiveButton("نعم", (d, w) -> {
                db.collection("wheel_config").document("main").update(
                    "jackpotClaimed", false,
                    "jackpotWinnerId", ""
                ).addOnSuccessListener(a -> {
                    jackpotWinnerId = "";
                    jackpotClaimed = false;
                    Toast.makeText(this, "✅ تمت إعادة التعيين", Toast.LENGTH_SHORT).show();
                    renderStatus();
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }
}
