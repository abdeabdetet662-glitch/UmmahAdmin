package com.ummah.admin;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AdminSupportChatActivity — شات الأدمن مع المستخدم
 */
public class AdminSupportChatActivity extends Activity {

    private FirebaseFirestore db;
    private String ticketId;
    private String userId;
    private String userName;

    private LinearLayout chatContainer;
    private EditText input;
    private ScrollView scroll;

    private ListenerRegistration msgsReg;
    private ListenerRegistration ticketReg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        ticketId = getIntent().getStringExtra("ticket_id");

        if (ticketId == null) { finish(); return; }

        buildUI();
        loadTicket();
        loadMessages();
    }

    private void buildUI() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundResource(R.drawable.bg_screen);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#0a0510"));
        top.setPadding(dp(16), dp(50), dp(16), dp(14));

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.parseColor("#D4AF37"));
        back.setTextSize(24);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        top.addView(back);

        TextView title = new TextView(this);
        title.setText("🏛️ ديوان أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(title);

        // Status button
        TextView statusBtn = new TextView(this);
        statusBtn.setText("✅");
        statusBtn.setTextSize(22);
        statusBtn.setPadding(dp(8), dp(8), dp(8), dp(8));
        statusBtn.setOnClickListener(v -> resolveTicket());
        top.addView(statusBtn);

        main.addView(top);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scroll.setLayoutParams(sl);

        chatContainer = new LinearLayout(this);
        chatContainer.setOrientation(LinearLayout.VERTICAL);
        chatContainer.setPadding(dp(16), dp(16), dp(16), dp(16));
        scroll.addView(chatContainer);
        main.addView(scroll);

        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setBackgroundColor(Color.parseColor("#0a0510"));
        inputBar.setPadding(dp(12), dp(10), dp(12), dp(10));

        input = new EditText(this);
        input.setHint("اكتب رد...");
        input.setHintTextColor(Color.parseColor("#666666"));
        input.setTextColor(Color.WHITE);
        input.setTextSize(14);
        input.setBackgroundResource(R.drawable.bg_input);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        inputBar.addView(input);

        TextView send = new TextView(this);
        send.setText("➤");
        send.setTextColor(Color.parseColor("#0a0510"));
        send.setTextSize(22);
        send.setGravity(Gravity.CENTER);
        send.setBackgroundResource(R.drawable.bg_btn_gold);
        send.setPadding(dp(16), dp(8), dp(16), dp(8));
        send.setOnClickListener(v -> sendMessage());
        inputBar.addView(send);

        main.addView(inputBar);
        setContentView(main);
    }

    private void loadTicket() {
        ticketReg = db.collection("support_tickets").document(ticketId)
            .addSnapshotListener((doc, e) -> {
                if (doc != null && doc.exists()) {
                    userId = doc.getString("userId");
                    userName = doc.getString("userName");
                }
            });
    }

    private void loadMessages() {
        msgsReg = db.collection("support_tickets").document(ticketId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                runOnUiThread(() -> {
                    chatContainer.removeAllViews();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String senderType = doc.getString("senderType");
                        String senderName = doc.getString("senderName");
                        String text = doc.getString("text");
                        Long createdAt = doc.getLong("createdAt");

                        boolean isAdmin = "admin".equals(senderType);

                        LinearLayout row = new LinearLayout(this);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setPadding(0, dp(6), 0, dp(6));

                        LinearLayout bubble = new LinearLayout(this);
                        bubble.setOrientation(LinearLayout.VERTICAL);
                        bubble.setBackgroundResource(R.drawable.bg_card);
                        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));

                        TextView name = new TextView(this);
                        if (isAdmin) {
                            name.setText("🏛️ أنت (الديوان)");
                            name.setTextColor(Color.parseColor("#D4AF37"));
                        } else {
                            name.setText("👤 " + (senderName != null ? senderName : "مواطن"));
                            name.setTextColor(Color.parseColor("#3B82F6"));
                        }
                        name.setTextSize(10);
                        name.setTypeface(null, Typeface.BOLD);
                        bubble.addView(name);

                        TextView t = new TextView(this);
                        t.setText(text);
                        t.setTextColor(Color.WHITE);
                        t.setTextSize(13);
                        t.setPadding(0, dp(4), 0, 0);
                        bubble.addView(t);

                        if (createdAt != null) {
                            TextView tm = new TextView(this);
                            tm.setText(sdf.format(new Date(createdAt)));
                            tm.setTextColor(Color.parseColor("#666666"));
                            tm.setTextSize(9);
                            bubble.addView(tm);
                        }

                        View spacer = new View(this);
                        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));

                        if (isAdmin) {
                            row.addView(spacer);
                            row.addView(bubble);
                        } else {
                            row.addView(bubble);
                            row.addView(spacer);
                        }

                        chatContainer.addView(row);
                    }

                    scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
                });
            });
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");

        long now = System.currentTimeMillis();

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", "admin");
        msg.put("senderName", "ديوان أُمّة");
        msg.put("senderType", "admin");
        msg.put("text", text);
        msg.put("createdAt", now);

        db.collection("support_tickets").document(ticketId)
            .collection("messages").add(msg)
            .addOnSuccessListener(doc -> {
                Map<String, Object> upd = new HashMap<>();
                upd.put("lastMessage", text);
                upd.put("lastMessageAt", now);
                upd.put("updatedAt", now);
                upd.put("status", "in_progress");
                db.collection("support_tickets").document(ticketId).update(upd);
            });
    }

    private void resolveTicket() {
        Map<String, Object> upd = new HashMap<>();
        upd.put("status", "resolved");
        upd.put("updatedAt", System.currentTimeMillis());

        db.collection("support_tickets").document(ticketId).update(upd)
            .addOnSuccessListener(v ->
                Toast.makeText(this, "✅ تم حل التذكرة", Toast.LENGTH_SHORT).show());
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (msgsReg != null) msgsReg.remove();
        if (ticketReg != null) ticketReg.remove();
        super.onDestroy();
    }
}
