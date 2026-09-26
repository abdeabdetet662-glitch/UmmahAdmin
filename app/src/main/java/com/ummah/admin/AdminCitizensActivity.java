package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdminCitizensActivity extends Activity {

    private AdminManager am;
    private LinearLayout listContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0A0A"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 40, 20, 40);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👥 إدارة المواطنين");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("اضغط على أي مواطن لعرض كل المعلومات");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 20);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);
        reg = am.listenAllCitizens(list -> runOnUiThread(() -> render(list)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void render(List<AdminManager.Citizen> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا يوجد مواطنون");
            empty.setTextColor(Color.parseColor("#616161"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (AdminManager.Citizen c : list) listContainer.addView(buildCard(c));
    }

    private LinearLayout buildCard(final AdminManager.Citizen c) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(c.blocked ? Color.parseColor("#3E0B0B") :
                                c.muted ? Color.parseColor("#3E2A0B") :
                                Color.parseColor("#141414"));
        card.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        // الحالة (أونلاين / أوفلاين)
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView dot = new TextView(this);
        dot.setText(c.online ? "🟢" : "⚫");
        dot.setTextSize(16);
        dot.setPadding(0, 0, 8, 0);
        statusRow.addView(dot);

        TextView statusText = new TextView(this);
        if (c.online) {
            statusText.setText("متصل الآن");
            statusText.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            statusText.setText("آخر ظهور: " + getTimeAgo(c.lastSeen));
            statusText.setTextColor(Color.parseColor("#9E9E9E"));
        }
        statusText.setTextSize(11);
        statusRow.addView(statusText);
        card.addView(statusRow);

        // الاسم
        TextView name = new TextView(this);
        String badge = "";
        if (c.blocked) badge = "  🚫";
        if (c.muted) badge += "  🔇";
        name.setText((c.name != null ? c.name : "مجهول") + badge);
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        name.setPadding(0, 6, 0, 4);
        card.addView(name);

        // الرقم الوطني + زر نسخ
        LinearLayout idRow = new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        idRow.setPadding(0, 6, 0, 6);

        TextView id = new TextView(this);
        id.setText("🆔 " + c.nationalId);
        id.setTextColor(Color.parseColor("#9E9E9E"));
        id.setTextSize(11);
        id.setTypeface(Typeface.MONOSPACE);
        id.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        idRow.addView(id);

        TextView copyBtn = new TextView(this);
        copyBtn.setText(" 📋 نسخ ");
        copyBtn.setTextColor(Color.WHITE);
        copyBtn.setTextSize(11);
        copyBtn.setPadding(16, 8, 16, 8);
        copyBtn.setBackgroundColor(Color.parseColor("#1565C0"));
        copyBtn.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("national_id", c.nationalId));
            Toast.makeText(AdminCitizensActivity.this, "✅ تم نسخ الرقم", Toast.LENGTH_SHORT).show();
        });
        idRow.addView(copyBtn);

        card.addView(idRow);

        // الرصيد
        TextView bal = new TextView(this);
        bal.setText("💰 " + c.balance + " Đ");
        bal.setTextColor(Color.parseColor("#FFD700"));
        bal.setTextSize(16);
        bal.setTypeface(null, Typeface.BOLD);
        bal.setPadding(0, 8, 0, 0);
        card.addView(bal);

        // تاريخ الانضمام
        if (c.joinDate != null && !c.joinDate.isEmpty()) {
            TextView join = new TextView(this);
            join.setText("📅 انضم: " + c.joinDate);
            join.setTextColor(Color.parseColor("#9E9E9E"));
            join.setTextSize(11);
            card.addView(join);
        }

        // شارات الحالة
        if (c.blocked) {
            TextView bl = new TextView(this);
            bl.setText("🚫 محظور");
            bl.setTextColor(Color.parseColor("#F44336"));
            bl.setTextSize(12);
            bl.setTypeface(null, Typeface.BOLD);
            bl.setPadding(0, 6, 0, 0);
            card.addView(bl);
        }
        if (c.muted) {
            TextView mu = new TextView(this);
            mu.setText("🔇 مكتوم");
            mu.setTextColor(Color.parseColor("#FF9800"));
            mu.setTextSize(12);
            mu.setTypeface(null, Typeface.BOLD);
            mu.setPadding(0, 4, 0, 0);
            card.addView(mu);
        }

        // زر عرض التفاصيل
        Button details = new Button(this);
        details.setText("👁️  عرض كل المعلومات");
        details.setTextSize(13);
        details.setTextColor(Color.WHITE);
        details.setBackgroundColor(Color.parseColor("#1565C0"));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dlp.setMargins(0, 14, 0, 6);
        details.setLayoutParams(dlp);
        details.setOnClickListener(v -> showFullDetails(c));
        card.addView(details);

        // أزرار سريعة
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(makeBtn("➕ 100", "#2E7D32", v -> am.addBalance(c.nationalId, 100, done())));
        row1.addView(makeBtn("➖ 100", "#C62828", v -> am.addBalance(c.nationalId, -100, done())));
        row1.addView(makeBtn("🎁 هدية", "#C2185B", v -> quickGift(c)));
        card.addView(row1);

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(makeBtn(c.blocked ? "✅ فك الحظر" : "🚫 حظر", c.blocked ? "#2E7D32" : "#424242", v -> {
            am.setBlocked(c.nationalId, !c.blocked, done());
        }));
        row2.addView(makeBtn(c.muted ? "🔊 فك الكتم" : "🔇 كتم", c.muted ? "#2E7D32" : "#EF6C00", v -> {
            am.setMuted(c.nationalId, !c.muted, c.muted ? 0 : System.currentTimeMillis() + 24L * 60 * 60 * 1000, done());
        }));
        row2.addView(makeBtn("🗑️ حذف", "#B71C1C", v -> confirmDelete(c)));
        card.addView(row2);

        return card;
    }

    private void showFullDetails(final AdminManager.Citizen c) {
        am.loadCitizenDetails(c.nationalId, new AdminManager.SingleCitizenListener() {
            @Override public void onFound(final AdminManager.Citizen full) {
                runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📛 الاسم: ").append(full.name).append("\n\n");
                    sb.append("🆔 الرقم الوطني:\n").append(full.nationalId).append("\n\n");
                    sb.append("🌍 البلد: ").append(full.country != null ? full.country : "غير محدد").append("\n\n");
                    sb.append("💰 الرصيد: ").append(full.balance).append(" Đ\n\n");
                    sb.append("📅 تاريخ الانضمام: ").append(full.joinDate != null ? full.joinDate : "—").append("\n\n");
                    sb.append("🕐 الحالة: ").append(full.online ? "🟢 متصل الآن" : "⚫ " + getTimeAgo(full.lastSeen)).append("\n\n");
                    if (full.lastSeen > 0) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                        sb.append("📆 آخر ظهور: ").append(sdf.format(new Date(full.lastSeen))).append("\n\n");
                    }
                    sb.append("🚫 محظور: ").append(full.blocked ? "نعم" : "لا").append("\n");
                    sb.append("🔇 مكتوم: ").append(full.muted ? "نعم" : "لا");

                    new AlertDialog.Builder(AdminCitizensActivity.this)
                        .setTitle("👤 بطاقة المواطن")
                        .setMessage(sb.toString())
                        .setPositiveButton("نسخ المعلومات", (d, w) -> {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            cm.setPrimaryClip(ClipData.newPlainText("citizen", sb.toString()));
                            Toast.makeText(AdminCitizensActivity.this, "✅ تم النسخ", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("إغلاق", null)
                        .show();
                });
            }
            @Override public void onNotFound() {
                runOnUiThread(() -> Toast.makeText(AdminCitizensActivity.this, "❌ غير موجود", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getTimeAgo(long timestamp) {
        if (timestamp == 0) return "غير معروف";
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if (seconds < 60) return "قبل " + seconds + " ثانية";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 60) return "قبل " + minutes + " دقيقة";
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours < 24) return "قبل " + hours + " ساعة";
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days < 30) return "قبل " + days + " يوم";
        return "قبل أكثر من شهر";
    }

    private Button makeBtn(String text, String color, android.view.View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor(color));
        b.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        b.setOnClickListener(l);
        return b;
    }

    private AdminManager.OnDone done() {
        return new AdminManager.OnDone() {
            @Override public void onSuccess() { Toast.makeText(AdminCitizensActivity.this, "✅ تم", Toast.LENGTH_SHORT).show(); }
            @Override public void onError(String m) { Toast.makeText(AdminCitizensActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show(); }
        };
    }

    private void confirmDelete(final AdminManager.Citizen c) {
        final EditText input = new EditText(this);
        input.setHint("اكتب: " + c.nationalId);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 20, 40, 20);
        box.addView(input);

        new AlertDialog.Builder(this)
            .setTitle("⚠️ حذف " + c.name + " نهائياً")
            .setMessage("سيُحذف الحساب مع رصيده ورسائله.\nلا يمكن التراجع.\n\nاكتب الرقم الوطني للتأكيد:")
            .setView(box)
            .setPositiveButton("حذف نهائي", (d, w) -> {
                if (!input.getText().toString().trim().equals(c.nationalId)) {
                    Toast.makeText(this, "❌ الرقم غير صحيح", Toast.LENGTH_SHORT).show();
                    return;
                }
                am.deleteCitizen(c.nationalId, new AdminManager.OnDone() {
                    @Override public void onSuccess() {
                        Toast.makeText(AdminCitizensActivity.this, "🗑️ تم الحذف", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String m) {
                        Toast.makeText(AdminCitizensActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void quickGift(final AdminManager.Citizen c) {
        final String[] gifts = {"🌹 وردة (1Đ)", "❤️ قلب (2Đ)", "🏆 كأس (25Đ)", "👑 تاج (50Đ)", "💎 ألماسة (100Đ)", "🕋 مكة (25000Đ)"};
        final String[][] data = {
            {"🌹", "وردة", "تقدير بسيط", "1"},
            {"❤️", "قلب", "محبة صافية", "2"},
            {"🏆", "كأس", "أنت الأفضل", "25"},
            {"👑", "تاج", "يا ملك", "50"},
            {"💎", "ألماسة", "أنت جوهرة", "100"},
            {"🕋", "مكة", "أعظم هدية", "25000"}
        };

        new AlertDialog.Builder(this)
            .setTitle("🎁 إرسال هدية إلى " + c.name)
            .setItems(gifts, (d, which) -> {
                String[] g = data[which];
                am.sendGift("الإدارة", c.nationalId, g[0], g[1], g[2], Integer.parseInt(g[3]),
                    new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(AdminCitizensActivity.this, "✅ تم إرسال الهدية", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onError(String m) {
                            Toast.makeText(AdminCitizensActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .show();
    }
}
