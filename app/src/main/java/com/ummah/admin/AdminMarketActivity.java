package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class AdminMarketActivity extends Activity {

    private AdminManager am;
    private FirebaseFirestore db;
    private LinearLayout container;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "🛒  إدارة السوق", 26);
        root.addView(title);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = db.collection("market_items").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            runOnUiThread(() -> {
                container.removeAllViews();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    addItemCard(d);
                }
            });
        });
    }

    private void addItemCard(final DocumentSnapshot d) {
        String name = d.getString("name");
        Long priceL = d.getLong("price");
        int price = priceL != null ? priceL.intValue() : 0;
        String img = d.getString("imageUrl");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        if (img != null && !img.isEmpty()) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 350);
            iv.setLayoutParams(ivLp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            com.bumptech.glide.Glide.with(this).load(img).into(iv);
            card.addView(iv);
        }

        TextView n = new TextView(this);
        n.setText(name);
        n.setTextColor(Color.WHITE);
        n.setTextSize(16);
        n.setTypeface(null, Typeface.BOLD);
        n.setPadding(0, 12, 0, 6);
        card.addView(n);

        TextView p = new TextView(this);
        p.setText("💰 " + price + " Đ");
        p.setTextColor(Color.parseColor("#D4AF37"));
        p.setTextSize(14);
        p.setTypeface(null, Typeface.BOLD);
        card.addView(p);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 0);

        Button edit = new Button(this);
        edit.setText("✏️");
        edit.setTextSize(16);
        edit.setBackgroundResource(R.drawable.bg_btn_gold_hero);
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(0, 140, 1f);
        elp.setMargins(0, 0, 8, 0);
        edit.setLayoutParams(elp);
        edit.setOnClickListener(v -> editPrice(d.getId(), price));
        row.addView(edit);

        Button del = new Button(this);
        del.setText("🗑️");
        del.setTextSize(16);
        del.setBackgroundResource(R.drawable.bg_button_danger);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, 140, 1f);
        dlp.setMargins(8, 0, 0, 0);
        del.setLayoutParams(dlp);
        del.setOnClickListener(v -> confirmDelete(d.getId(), name));
        row.addView(del);

        card.addView(row);
        container.addView(card);
    }

    private void editPrice(final String id, int current) {
        final EditText input = UiHelper.input(this, "السعر الجديد");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(current));

        LinearLayout box = new LinearLayout(this);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("تعديل السعر")
                .setView(box)
                .setPositiveButton("حفظ", (d, w) -> {
                    String t = input.getText().toString().trim();
                    if (t.isEmpty()) return;
                    int np;
                    try { np = Integer.parseInt(t); } catch (Exception e) { return; }
                    am.updateMarketPrice(id, np, new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(AdminMarketActivity.this, "✅ تم", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(AdminMarketActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void confirmDelete(final String id, String name) {
        new AlertDialog.Builder(this)
                .setTitle("حذف المنتج")
                .setMessage("هل تريد حذف: " + name + " ؟")
                .setPositiveButton("حذف", (d, w) -> {
                    am.deleteMarketItem(id, new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(AdminMarketActivity.this, "✅ تم الحذف", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(AdminMarketActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                        }
                    });
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
