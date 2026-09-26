package com.ummah.admin;

import android.app.Activity;
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

import java.util.List;

public class AdminBlockedActivity extends Activity {
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
        title.setText("🚫 المواطنون المحظورون");
        title.setTextColor(Color.parseColor("#F44336"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("لا يمكنهم إرسال أي شيء في الدولة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 20);
        root.addView(sub);

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(listContainer);

        setContentView(scroll);
        reg = am.listenBlocked(list -> runOnUiThread(() -> render(list)));
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
            empty.setText("✅ لا يوجد محظورون حالياً");
            empty.setTextColor(Color.parseColor("#4CAF50"));
            empty.setTextSize(14);
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
        card.setBackgroundColor(Color.parseColor("#3E0B0B"));
        card.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);

        TextView name = new TextView(this);
        name.setText("🚫 " + (c.name != null ? c.name : "مجهول"));
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);
        name.setTypeface(null, Typeface.BOLD);
        card.addView(name);

        TextView id = new TextView(this);
        id.setText(c.nationalId);
        id.setTextColor(Color.parseColor("#757575"));
        id.setTextSize(10);
        id.setTypeface(Typeface.MONOSPACE);
        card.addView(id);

        TextView bal = new TextView(this);
        bal.setText("💰 " + c.balance + " Đ");
        bal.setTextColor(Color.parseColor("#FFD700"));
        bal.setTextSize(14);
        bal.setPadding(0, 8, 0, 12);
        card.addView(bal);

        Button unblock = new Button(this);
        unblock.setText("✅  رفع الحظر");
        unblock.setTextSize(14);
        unblock.setTextColor(Color.WHITE);
        unblock.setBackgroundColor(Color.parseColor("#2E7D32"));
        unblock.setOnClickListener(v -> {
            am.setBlocked(c.nationalId, false, new AdminManager.OnDone() {
                @Override public void onSuccess() {
                    Toast.makeText(AdminBlockedActivity.this, "✅ تم رفع الحظر", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String m) {
                    Toast.makeText(AdminBlockedActivity.this, "❌ " + m, Toast.LENGTH_SHORT).show();
                }
            });
        });
        card.addView(unblock);

        return card;
    }
}
