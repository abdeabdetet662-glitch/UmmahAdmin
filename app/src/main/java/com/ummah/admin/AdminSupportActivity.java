package com.ummah.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AdminSupportActivity — إدارة الدعم (ديوان أُمّة)
 */
public class AdminSupportActivity extends Activity {

    private FirebaseFirestore db;
    private LinearLayout ticketsContainer;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();
        buildUI();
        loadTickets();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(50), dp(20), dp(50));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        TextView icon = new TextView(this);
        icon.setText("🏛️");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("ديوان أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(26);
        title.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(12), 0, dp(4));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("طلبات المواطنين");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, dp(24));
        root.addView(sub);

        ticketsContainer = new LinearLayout(this);
        ticketsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(ticketsContainer);

        setContentView(scroll);
    }

    private void loadTickets() {
        reg = db.collection("support_tickets")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                runOnUiThread(() -> renderTickets(snap));
            });
    }

    private void renderTickets(com.google.firebase.firestore.QuerySnapshot snap) {
        ticketsContainer.removeAllViews();

        if (snap.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("📭 لا توجد تذاكر");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(30), 0, dp(30));
            ticketsContainer.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.US);

        for (DocumentSnapshot doc : snap.getDocuments()) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(10));
            card.setLayoutParams(lp);

            String subject = doc.getString("subject");
            String status = doc.getString("status");
            String priority = doc.getString("priority");
            String cat = doc.getString("category");
            String userName = doc.getString("userName");
            String userId = doc.getString("userId");
            Long updatedAt = doc.getLong("updatedAt");

            // Row 1
            LinearLayout row1 = new LinearLayout(this);
            row1.setOrientation(LinearLayout.HORIZONTAL);

            TextView s = new TextView(this);
            s.setText("📌 " + (subject != null ? subject : "بدون موضوع"));
            s.setTextColor(Color.WHITE);
            s.setTextSize(14);
            s.setTypeface(null, Typeface.BOLD);
            s.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row1.addView(s);

            TextView st = new TextView(this);
            st.setText(statusAr(status));
            st.setTextColor(Color.parseColor(statusColor(status)));
            st.setTextSize(10);
            st.setTypeface(null, Typeface.BOLD);
            st.setPadding(dp(8), dp(4), dp(8), dp(4));
            st.setBackgroundResource(R.drawable.bg_card);
            row1.addView(st);

            card.addView(row1);

            // Row 2: user
            TextView u = new TextView(this);
            u.setText("👤 " + (userName != null ? userName : "مواطن"));
            u.setTextColor(Color.parseColor("#D4AF37"));
            u.setTextSize(12);
            u.setPadding(0, dp(8), 0, dp(4));
            card.addView(u);

            // Row 3: category + priority
            TextView info = new TextView(this);
            info.setText(catAr(cat) + " • " + priorityAr(priority));
            info.setTextColor(Color.parseColor("#888888"));
            info.setTextSize(11);
            card.addView(info);

            // Row 4: date
            if (updatedAt != null) {
                TextView d = new TextView(this);
                d.setText("🕐 " + sdf.format(new Date(updatedAt)));
                d.setTextColor(Color.parseColor("#666666"));
                d.setTextSize(10);
                d.setPadding(0, dp(4), 0, 0);
                card.addView(d);
            }

            final String tid = doc.getId();
            card.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminSupportChatActivity.class)
                        .putExtra("ticket_id", tid));
            });
            card.setClickable(true);
            card.setFocusable(true);

            ticketsContainer.addView(card);
        }
    }

    private String statusAr(String s) {
        if (s == null) return "مفتوحة";
        switch (s) {
            case "in_progress": return "⏳ معالجة";
            case "resolved": return "✅ محلولة";
            default: return "📬 مفتوحة";
        }
    }

    private String statusColor(String s) {
        if (s == null) return "#3B82F6";
        switch (s) {
            case "in_progress": return "#F59E0B";
            case "resolved": return "#10B981";
            default: return "#3B82F6";
        }
    }

    private String catAr(String c) {
        if (c == null) return "📌 أخرى";
        switch (c) {
            case "transfer": return "💰 التحويلات";
            case "market": return "🛒 السوق";
            case "account": return "👤 الحساب";
            case "technical": return "🔧 تقنية";
            default: return "📌 أخرى";
        }
    }

    private String priorityAr(String p) {
        if (p == null) return "عادي";
        switch (p) {
            case "high": return "🔴 عاجل";
            case "low": return "🟢 منخفض";
            default: return "🟡 عادي";
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (reg != null) reg.remove();
        super.onDestroy();
    }
}
