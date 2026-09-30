package com.ummah.admin;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminLogActivity extends Activity {

    private AdminManager am;
    private LinearLayout container;
    private ListenerRegistration reg;

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

        TextView title = UiHelper.goldTitle(this, "📜  سجل النشاط", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("كل الإجراءات الإدارية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 24);
        root.addView(sub);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        setContentView(scroll);
        startListener();
    }

    private void startListener() {
        if (reg != null) reg.remove();
        reg = am.listenLogs(new AdminManager.LogListener() {
            @Override public void onLogs(List<AdminManager.LogEntry> list) {
                runOnUiThread(() -> render(list));
            }
        });
    }

    private void render(List<AdminManager.LogEntry> list) {
        container.removeAllViews();

        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("ما فيه سجل بعد");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 60, 0, 0);
            container.addView(empty);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.US);

        for (AdminManager.LogEntry e : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_card_premium);
            card.setPadding(24, 18, 24, 18);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 6, 0, 6);
            card.setLayoutParams(lp);

            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);

            TextView act = new TextView(this);
            act.setText(iconFor(e.action) + "  " + e.action);
            act.setTextColor(Color.parseColor("#D4AF37"));
            act.setTextSize(13);
            act.setTypeface(null, Typeface.BOLD);
            act.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            top.addView(act);

            TextView time = new TextView(this);
            time.setText(sdf.format(new Date(e.timestamp)));
            time.setTextColor(Color.parseColor("#757575"));
            time.setTextSize(11);
            top.addView(time);

            card.addView(top);

            TextView det = new TextView(this);
            det.setText(e.details != null ? e.details : "");
            det.setTextColor(Color.WHITE);
            det.setTextSize(13);
            det.setPadding(0, 6, 0, 0);
            card.addView(det);

            container.addView(card);
        }
    }

    private String iconFor(String action) {
        if (action == null) return "•";
        switch (action) {
            case "broadcast": return "📢";
            case "add_balance": return "➕";
            case "set_balance": return "💰";
            case "block": return "🚫";
            case "unblock": return "✅";
            case "mute": return "🔇";
            case "unmute": return "🔊";
            case "delete_citizen": return "🗑️";
            case "delete_item": return "🗑️";
            case "update_price": return "💵";
            case "private_msg": return "💌";
            case "delete_complaint": return "🗑️";
            case "resolve_complaint": return "⚖️";
        }
        return "•";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) reg.remove();
    }
}
