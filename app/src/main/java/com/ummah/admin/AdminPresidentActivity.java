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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPresidentActivity extends Activity {

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

        TextView title = UiHelper.goldTitle(this, "👑  إدارة الرؤساء", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("وثّق مواطناً كرئيس معتمد للدولة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        // زر توثيق
        Button addBtn = UiHelper.primaryButton(this, "➕  توثيق رئيس جديد");
        addBtn.setOnClickListener(v -> showCertifyDialog());
        root.addView(addBtn);

        // عنوان القائمة
        TextView listTitle = new TextView(this);
        listTitle.setText("👑  الرؤساء المعتمدون");
        listTitle.setTextColor(Color.parseColor("#D4AF37"));
        listTitle.setTextSize(16);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setGravity(Gravity.CENTER);
        listTitle.setPadding(0, 32, 0, 16);
        root.addView(listTitle);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = am.listenPresidents(new AdminManager.PresidentsListener() {
            @Override public void onList(List<AdminManager.President> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<AdminManager.President> list) {
        container.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("ما فيه رؤساء معتمدون حالياً");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            container.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (AdminManager.President p : list) {
            addPresidentCard(p, sdf);
        }
    }

    private void addPresidentCard(final AdminManager.President p, SimpleDateFormat sdf) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_premium);
        card.setPadding(24, 24, 24, 24);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        card.setLayoutParams(lp);

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView crown = new TextView(this);
        crown.setText("👑");
        crown.setTextSize(36);
        crown.setPadding(0, 0, 16, 0);
        header.addView(crown);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(p.name);
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        info.addView(name);

        TextView id = new TextView(this);
        id.setText(p.nationalId);
        id.setTextColor(Color.parseColor("#D4AF37"));
        id.setTextSize(11);
        id.setPadding(0, 4, 0, 0);
        info.addView(id);

        header.addView(info);
        card.addView(header);

        // Info
        TextView date = new TextView(this);
        date.setText("📅 وُثّق في: " + sdf.format(new Date(p.certifiedAt)));
        date.setTextColor(Color.parseColor("#9E9E9E"));
        date.setTextSize(12);
        date.setPadding(0, 12, 0, 4);
        card.addView(date);

        TextView note = new TextView(this);
        note.setText("📝 " + (p.note != null ? p.note : "—"));
        note.setTextColor(Color.parseColor("#CCCCCC"));
        note.setTextSize(12);
        note.setPadding(0, 4, 0, 12);
        card.addView(note);

        // زر إلغاء التوثيق
        Button revoke = UiHelper.dangerButton(this, "🚫  إلغاء التوثيق");
        revoke.setOnClickListener(v -> confirmRevoke(p));
        card.addView(revoke);

        container.addView(card);
    }

    private void showCertifyDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);

        final EditText idInput = UiHelper.input(this, "الرقم الوطني (UMM-XXXX-XXXX-XXXX)");
        box.addView(idInput);

        final EditText noteInput = UiHelper.input(this, "ملاحظة (اختياري)");
        box.addView(noteInput);

        new AlertDialog.Builder(this)
                .setTitle("👑  توثيق رئيس")
                .setMessage("أدخل الرقم الوطني للمواطن اللي تريد توثيقو كرئيس معتمد.")
                .setView(box)
                .setPositiveButton("توثيق", (d, w) -> {
                    String id = idInput.getText().toString().trim();
                    String note = noteInput.getText().toString().trim();
                    if (id.isEmpty()) {
                        Toast.makeText(this, "أدخل الرقم الوطني", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!id.startsWith("UMM-")) {
                        Toast.makeText(this, "الرقم الوطني يجب أن يبدأ بـ UMM-", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    certify(id, note);
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void certify(final String nationalId, final String note) {
        // نجيبو اسم المواطن
        db.collection("citizens").document(nationalId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "❌ المواطن غير موجود", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String name = doc.getString("name");
                    if (name == null) name = "مواطن";

                    final String finalName = name;
                    new AlertDialog.Builder(this)
                            .setTitle("تأكيد التوثيق")
                            .setMessage("توثيق:\n\n👤 " + finalName +
                                    "\n🆔 " + nationalId +
                                    "\n\nكرئيس معتمد للدولة؟")
                            .setPositiveButton("توثيق", (d2, w2) -> {
                                am.certifyAsPresident(nationalId, finalName, note,
                                        new AdminManager.OnDone() {
                                    @Override public void onSuccess() {
                                        Toast.makeText(AdminPresidentActivity.this,
                                                "✅ تم توثيق " + finalName + " كرئيس!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    @Override public void onError(String msg) {
                                        Toast.makeText(AdminPresidentActivity.this,
                                                "❌ " + msg, Toast.LENGTH_LONG).show();
                                    }
                                });
                            })
                            .setNegativeButton("إلغاء", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmRevoke(final AdminManager.President p) {
        new AlertDialog.Builder(this)
                .setTitle("🚫  إلغاء التوثيق")
                .setMessage("إلغاء توثيق الرئيس:\n\n👑 " + p.name +
                        "\n🆔 " + p.nationalId +
                        "\n\nسيفقد كل الصلاحيات الرئاسية.")
                .setPositiveButton("إلغاء التوثيق", (d, w) -> {
                    am.revokePresident(p.nationalId, new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(AdminPresidentActivity.this,
                                    "✅ تم إلغاء التوثيق", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String msg) {
                            Toast.makeText(AdminPresidentActivity.this,
                                    "❌ " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("رجوع", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
