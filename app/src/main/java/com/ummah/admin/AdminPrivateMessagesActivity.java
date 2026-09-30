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

public class AdminPrivateMessagesActivity extends Activity {

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

        TextView title = UiHelper.goldTitle(this, "💌  رسائل خاصة", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل رسالة لمواطن محدد");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        LinearLayout card = UiHelper.card(this);

        TextView label = new TextView(this);
        label.setText("🎯 الرقم الوطني");
        label.setTextColor(Color.parseColor("#D4AF37"));
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 0, 0, 12);
        card.addView(label);

        final EditText idInput = UiHelper.input(this, "UMM-XXXX-XXXX-XXXX");
        card.addView(idInput);

        TextView label2 = new TextView(this);
        label2.setText("📝 الرسالة");
        label2.setTextColor(Color.parseColor("#D4AF37"));
        label2.setTextSize(14);
        label2.setTypeface(null, Typeface.BOLD);
        label2.setGravity(Gravity.CENTER);
        label2.setPadding(0, 20, 0, 12);
        card.addView(label2);

        final EditText msgInput = UiHelper.input(this, "نص الرسالة...");
        msgInput.setMinLines(3);
        msgInput.setGravity(Gravity.TOP | Gravity.START);
        card.addView(msgInput);

        root.addView(card);

        Button sendBtn = UiHelper.primaryButton(this, "💌  إرسال الرسالة");
        sendBtn.setMinHeight(160);
        sendBtn.setTextSize(18);
        sendBtn.setOnClickListener(v -> {
            String id = idInput.getText().toString().trim();
            String msg = msgInput.getText().toString().trim();
            if (id.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "أكمل الحقول", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("تأكيد الإرسال")
                    .setMessage("إرسال لـ: " + id + "\n\n" + msg)
                    .setPositiveButton("إرسال", (d, w) -> {
                        am.sendPrivateMessage(id, msg, new AdminManager.OnDone() {
                            @Override public void onSuccess() {
                                Toast.makeText(AdminPrivateMessagesActivity.this,
                                        "✅ تم الإرسال!", Toast.LENGTH_LONG).show();
                                idInput.setText("");
                                msgInput.setText("");
                            }
                            @Override public void onError(String err) {
                                Toast.makeText(AdminPrivateMessagesActivity.this,
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
