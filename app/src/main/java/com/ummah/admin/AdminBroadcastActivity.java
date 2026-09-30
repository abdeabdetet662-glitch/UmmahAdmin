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

public class AdminBroadcastActivity extends Activity {

    private AdminManager am;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        am = AdminManager.get();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        TextView title = UiHelper.goldTitle(this, "📢  البث العام", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل رسالة لكل مواطني أُمّة");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        TextView label = new TextView(this);
        label.setText("📝 نص الرسالة");
        label.setTextColor(Color.parseColor("#D4AF37"));
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 0, 0, 12);
        card.addView(label);

        final EditText input = UiHelper.input(this, "اكتب الرسالة هنا...");
        input.setMinLines(4);
        input.setGravity(Gravity.TOP | Gravity.START);
        card.addView(input);

        root.addView(card);

        Button sendBtn = UiHelper.primaryButton(this, "📢  إرسال البث العام");
        sendBtn.setMinHeight(160);
        sendBtn.setTextSize(18);
        sendBtn.setOnClickListener(v -> {
            String msg = input.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "اكتب رسالة أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("تأكيد البث")
                    .setMessage("راح يوصل هذا الإعلان لـ كل المواطنين:\n\n" + msg)
                    .setPositiveButton("إرسال", (d, w) -> {
                        am.broadcast(msg, new AdminManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(AdminBroadcastActivity.this,
                                        "✅ تم إرسال البث!", Toast.LENGTH_LONG).show();
                                input.setText("");
                            }
                            @Override public void onError(String err) {
                                Toast.makeText(AdminBroadcastActivity.this,
                                        "❌ " + err, Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .setNegativeButton("إلغاء", null)
                    .show();
        });
        root.addView(sendBtn);

        setContentView(scroll);
    }
}
