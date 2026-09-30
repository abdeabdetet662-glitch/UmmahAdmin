package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

public class AdminNewsActivity extends Activity {

    private FirebaseFirestore db;
    private LinearLayout container;
    private ListenerRegistration reg;

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

        TextView title = UiHelper.goldTitle(this, "📰  الأخبار", 26);
        root.addView(title);

        Button addBtn = UiHelper.primaryButton(this, "➕  إضافة خبر");
        addBtn.setOnClickListener(v -> addNews());
        root.addView(addBtn);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = db.collection("news").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            runOnUiThread(() -> {
                container.removeAllViews();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    addNewsCard(d);
                }
            });
        });
    }

    private void addNewsCard(final DocumentSnapshot d) {
        String content = d.getString("content");
        String author = d.getString("author");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 18, 24, 18);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        card.setLayoutParams(lp);

        TextView a = new TextView(this);
        a.setText(author != null ? author : "مجهول");
        a.setTextColor(Color.parseColor("#D4AF37"));
        a.setTextSize(12);
        a.setTypeface(null, Typeface.BOLD);
        card.addView(a);

        TextView c = new TextView(this);
        c.setText(content);
        c.setTextColor(Color.WHITE);
        c.setTextSize(14);
        c.setPadding(0, 6, 0, 8);
        card.addView(c);

        Button del = new Button(this);
        del.setText("🗑️  حذف");
        del.setTextSize(12);
        del.setBackgroundResource(R.drawable.bg_button_danger);
        del.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 120));
        del.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("حذف الخبر")
                    .setMessage("تأكيد الحذف؟")
                    .setPositiveButton("حذف", (dd, ww) -> {
                        d.getReference().delete();
                        Toast.makeText(this, "✅ تم الحذف", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
        card.addView(del);

        container.addView(card);
    }

    private void addNews() {
        final EditText input = UiHelper.input(this, "نص الخبر...");
        input.setMinLines(4);
        input.setGravity(Gravity.TOP | Gravity.START);

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("خبر جديد")
                .setView(box)
                .setPositiveButton("نشر", (d, w) -> {
                    String txt = input.getText().toString().trim();
                    if (txt.isEmpty()) return;
                    Map<String, Object> data = new HashMap<>();
                    data.put("author", "📰 الإدارة");
                    data.put("content", txt);
                    data.put("timestamp", System.currentTimeMillis());
                    db.collection("news").add(data)
                            .addOnSuccessListener(r -> Toast.makeText(this, "✅ تم النشر", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
