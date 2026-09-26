package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminReportsActivity extends Activity {
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
        title.setText("📢 الإبلاغات");
        title.setTextColor(Color.parseColor("#FFC107"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("بلاغات المواطنين عن الرسائل المسيئة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 20);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);
        reg = am.listenReports(list -> runOnUiThread(() -> render(list)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }

    private void render(List<AdminManager.Report> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("✅ لا توجد إبلاغات");
            empty.setTextColor(Color.parseColor("#4CAF50"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            listContainer.addView(empty);
            return;
        }
        for (AdminManager.Report r : list) listContainer.addView(buildCard(r));
    }

    private LinearLayout buildCard(final AdminManager.Report r) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1A1A1A"));
        card.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView who = new TextView(this);
        who.setText("📢 " + r.reporterName + "  ➜  " + r.reportedName);
        who.setTextColor(Color.WHITE);
        who.setTextSize(14);
        who.setTypeface(null, Typeface.BOLD);
        card.addView(who);

        TextView reportedId = new TextView(this);
        reportedId.setText(r.reportedId);
        reportedId.setTextColor(Color.parseColor("#757575"));
        reportedId.setTextSize(10);
        reportedId.setTypeface(Typeface.MONOSPACE);
        card.addView(reportedId);

        TextView msg = new TextView(this);
        msg.setText("💬 \"" + (r.messageText != null ? r.messageText : "") + "\"");
        msg.setTextColor(Color.parseColor("#FF9800"));
        msg.setTextSize(13);
        msg.setPadding(0, 12, 0, 12);
        msg.setBackgroundColor(Color.parseColor("#0A0A0A"));
        card.addView(msg);

        TextView time = new TextView(this);
        time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(r.timestamp)));
        time.setTextColor(Color.parseColor("#616161"));
        time.setTextSize(10);
        card.addView(time);

        // أزرار
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 0);

        Button blockBtn = new Button(this);
        blockBtn.setText("🚫 حظر");
        blockBtn.setTextSize(11);
        blockBtn.setTextColor(Color.WHITE);
        blockBtn.setBackgroundColor(Color.parseColor("#C62828"));
        blockBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        blockBtn.setOnClickListener(v -> {
            am.setBlocked(r.reportedId, true, new AdminManager.OnDone() {
                @Override public void onSuccess() {
                    Toast.makeText(AdminReportsActivity.this, "🚫 تم حظر " + r.reportedName, Toast.LENGTH_SHORT).show();
                    am.deleteReport(r.id, new AdminManager.OnDone() {
                        @Override public void onSuccess() {}
                        @Override public void onError(String m) {}
                    });
                }
                @Override public void onError(String m) {
                    Toast.makeText(AdminReportsActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                }
            });
        });
        row.addView(blockBtn);

        Button muteBtn = new Button(this);
        muteBtn.setText("🔇 كتم 24 س");
        muteBtn.setTextSize(11);
        muteBtn.setTextColor(Color.WHITE);
        muteBtn.setBackgroundColor(Color.parseColor("#EF6C00"));
        muteBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        muteBtn.setOnClickListener(v -> {
            long until = System.currentTimeMillis() + 24L * 60 * 60 * 1000;
            am.setMuted(r.reportedId, true, until, new AdminManager.OnDone() {
                @Override public void onSuccess() {
                    Toast.makeText(AdminReportsActivity.this, "🔇 تم الكتم 24 ساعة", Toast.LENGTH_SHORT).show();
                    am.deleteReport(r.id, new AdminManager.OnDone() {
                        @Override public void onSuccess() {}
                        @Override public void onError(String m) {}
                    });
                }
                @Override public void onError(String m) {}
            });
        });
        row.addView(muteBtn);

        Button delBtn = new Button(this);
        delBtn.setText("🗑️ حذف");
        delBtn.setTextSize(11);
        delBtn.setTextColor(Color.WHITE);
        delBtn.setBackgroundColor(Color.parseColor("#424242"));
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        delBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("حذف البلاغ")
                .setMessage("هل تريد حذف هذه الرسالة وهذا البلاغ؟")
                .setPositiveButton("حذف الكل", (d, w) -> {
                    am.deleteReportAndMessage(r.id, r.messageId, new AdminManager.OnDone() {
                        @Override public void onSuccess() {
                            Toast.makeText(AdminReportsActivity.this, "✅ تم الحذف", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onError(String m) {}
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
        });
        row.addView(delBtn);

        card.addView(row);
        return card;
    }
}
