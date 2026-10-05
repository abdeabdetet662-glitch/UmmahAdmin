package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminNotifyActivity — إرسال إشعارات مخصصة
 * 
 * المشرف يقدر:
 * - يكتب رسالة إشعار
 * - يختار نوع الإشعار (معلومة/تحذير/مكافأة/عرض)
 * - يرسل لمستخدم محدد أو للجميع
 * - يحدد إجراء (فتح شاشة معينة)
 */
public class AdminNotifyActivity extends Activity {

    private FirebaseFirestore db;
    private RadioGroup modeGroup, typeGroup, actionGroup;
    private EditText etNationalId, etTitle, etMessage;
    private LinearLayout targetBox;

    private static final String COLLECTION = "notifications";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(30, 50, 30, 60);
        scroll.addView(root);

        // ═══ Header ═══
        TextView icon = new TextView(this);
        icon.setText("🔔");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = UiHelper.goldTitle(this, "إرسال إشعار", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("أرسل إشعار مخصص لكل المواطنين أو لمستخدم");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // ═══ 1. نوع الإرسال ═══
        addSectionTitle(root, "🎯 نوع الإرسال");

        LinearLayout modeCard = UiHelper.card(this);
        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.VERTICAL);

        RadioButton rbAll = new RadioButton(this);
        rbAll.setText("🌍 جماعي — لكل المواطنين");
        rbAll.setTextColor(Color.WHITE);
        rbAll.setTextSize(15);
        rbAll.setId(1);
        rbAll.setChecked(true);
        modeGroup.addView(rbAll);

        RadioButton rbOne = new RadioButton(this);
        rbOne.setText("👤 فردي — لمستخدم محدد");
        rbOne.setTextColor(Color.WHITE);
        rbOne.setTextSize(15);
        rbOne.setId(2);
        modeGroup.addView(rbOne);

        modeCard.addView(modeGroup);
        root.addView(modeCard);

        // ═══ المُستهدف (للإرسال الفردي) ═══
        targetBox = new LinearLayout(this);
        targetBox.setOrientation(LinearLayout.VERTICAL);
        targetBox.setBackgroundResource(R.drawable.bg_card);
        targetBox.setPadding(40, 30, 40, 30);
        targetBox.setVisibility(View.GONE);

        TextView targetTitle = new TextView(this);
        targetTitle.setText("🆔 رقم المواطن");
        targetTitle.setTextColor(Color.parseColor("#D4AF37"));
        targetTitle.setTextSize(14);
        targetTitle.setTypeface(null, Typeface.BOLD);
        targetBox.addView(targetTitle);

        etNationalId = new EditText(this);
        etNationalId.setHint("UMM-XXXX-XXXX-XXXX");
        etNationalId.setTextColor(Color.WHITE);
        etNationalId.setHintTextColor(Color.parseColor("#666666"));
        etNationalId.setTextSize(15);
        etNationalId.setBackgroundResource(R.drawable.bg_input);
        etNationalId.setPadding(30, 30, 30, 30);
        etNationalId.setSingleLine(true);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        idLp.setMargins(0, 15, 0, 0);
        etNationalId.setLayoutParams(idLp);
        targetBox.addView(etNationalId);

        LinearLayout.LayoutParams targetLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        targetLp.setMargins(0, 15, 0, 0);
        targetBox.setLayoutParams(targetLp);

        root.addView(targetBox);

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            targetBox.setVisibility(checkedId == 2 ? View.VISIBLE : View.GONE);
        });

        // ═══ 2. نوع الإشعار ═══
        addSectionTitle(root, "🏷️ نوع الإشعار");

        LinearLayout typeCard = UiHelper.card(this);
        typeGroup = new RadioGroup(this);
        typeGroup.setOrientation(RadioGroup.VERTICAL);

        String[][] types = {
            {"1", "ℹ️ معلومة — إشعار عادي"},
            {"2", "⚠️ تحذير — تحذير مهم"},
            {"3", "🎁 مكافأة — هدية/جائزة"},
            {"4", "📢 عرض — عرض خاص"},
            {"5", "🎉 حدث — حدث مهم"}
        };

        for (String[] t : types) {
            RadioButton rb = new RadioButton(this);
            rb.setText(t[1]);
            rb.setTextColor(Color.WHITE);
            rb.setTextSize(14);
            rb.setId(Integer.parseInt(t[0]));
            if (t[0].equals("1")) rb.setChecked(true);
            typeGroup.addView(rb);
        }

        typeCard.addView(typeGroup);
        root.addView(typeCard);

        // ═══ 3. العنوان ═══
        addSectionTitle(root, "✏️ العنوان");

        LinearLayout titleCard = UiHelper.card(this);
        etTitle = new EditText(this);
        etTitle.setHint("مثال: مكافأة أسبوعية");
        etTitle.setTextColor(Color.WHITE);
        etTitle.setHintTextColor(Color.parseColor("#666666"));
        etTitle.setTextSize(16);
        etTitle.setBackgroundResource(R.drawable.bg_input);
        etTitle.setPadding(30, 30, 30, 30);
        etTitle.setSingleLine(true);
        etTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        titleCard.addView(etTitle);
        root.addView(titleCard);

        // ═══ 4. الرسالة ═══
        addSectionTitle(root, "💬 الرسالة");

        LinearLayout msgCard = UiHelper.card(this);
        etMessage = new EditText(this);
        etMessage.setHint("اكتب تفاصيل الإشعار...");
        etMessage.setTextColor(Color.WHITE);
        etMessage.setHintTextColor(Color.parseColor("#666666"));
        etMessage.setTextSize(15);
        etMessage.setBackgroundResource(R.drawable.bg_input);
        etMessage.setPadding(30, 30, 30, 30);
        etMessage.setMinLines(3);
        etMessage.setGravity(Gravity.TOP);
        etMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300));
        msgCard.addView(etMessage);
        root.addView(msgCard);

        // ═══ 5. الإجراء (اختياري) ═══
        addSectionTitle(root, "🎬 إجراء عند الضغط (اختياري)");

        LinearLayout actionCard = UiHelper.card(this);
        actionGroup = new RadioGroup(this);
        actionGroup.setOrientation(RadioGroup.VERTICAL);

        String[][] actions = {
            {"0", "🚫 بلا إجراء"},
            {"1", "💰 المحفظة"},
            {"2", "🎡 عجلة الحظ"},
            {"3", "🏙️ المدينة"},
            {"4", "🛒 السوق"},
            {"5", "👑 الانتخابات"}
        };

        for (String[] a : actions) {
            RadioButton rb = new RadioButton(this);
            rb.setText(a[1]);
            rb.setTextColor(Color.WHITE);
            rb.setTextSize(14);
            rb.setId(Integer.parseInt(a[0]));
            if (a[0].equals("0")) rb.setChecked(true);
            actionGroup.addView(rb);
        }

        actionCard.addView(actionGroup);
        root.addView(actionCard);

        // ═══ زر الإرسال ═══
        Button btnSend = UiHelper.primaryButton(this, "🚀 إرسال الإشعار");
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, 40, 0, 0);
        btnSend.setLayoutParams(btnLp);
        btnSend.setOnClickListener(v -> sendNotification());
        root.addView(btnSend);

        // ═══ نصيحة ═══
        TextView hint = new TextView(this);
        hint.setText("⚠️ الإشعار رايح يوصل للمواطنين فوراً");
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 20, 0, 0);
        root.addView(hint);

        setContentView(scroll);
    }

    // ═══════════════════════════════════════════
    //  إرسال الإشعار
    // ═══════════════════════════════════════════
    private void sendNotification() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        if (title.isEmpty()) {
            toast("⚠️ اكتب العنوان");
            return;
        }
        if (message.isEmpty()) {
            toast("⚠️ اكتب الرسالة");
            return;
        }

        int modeId = modeGroup.getCheckedRadioButtonId();
        int typeId = typeGroup.getCheckedRadioButtonId();
        int actionId = actionGroup.getCheckedRadioButtonId();

        final String type = typeFromId(typeId);
        final String action = actionFromId(actionId);
        final String iconEmoji = emojiFromType(type);

        if (modeId == 2) {
            // ═══ فردي ═══
            String target = etNationalId.getText().toString().trim();
            if (target.isEmpty()) {
                toast("⚠️ اكتب الرقم الوطني");
                return;
            }

            new AlertDialog.Builder(this)
                .setTitle("🔔 تأكيد الإرسال")
                .setMessage("المُستهدف: " + target +
                        "\n📌 العنوان: " + title +
                        "\n💬 الرسالة: " + message)
                .setPositiveButton("✅ إرسال", (d, w) -> {
                    sendToUser(target, title, message, type, action, iconEmoji);
                })
                .setNegativeButton("إلغاء", null)
                .show();
        } else {
            // ═══ جماعي ═══
            new AlertDialog.Builder(this)
                .setTitle("🌍 إشعار جماعي")
                .setMessage("رايح ترسل لكل المواطنين:" +
                        "\n📌 " + title +
                        "\n💬 " + message)
                .setPositiveButton("✅ إرسال للكل", (d, w) -> {
                    sendToAll(title, message, type, action, iconEmoji);
                })
                .setNegativeButton("إلغاء", null)
                .show();
        }
    }

    private void sendToUser(final String nationalId, final String title, final String message,
                            final String type, final String action, final String emoji) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("type", type);
        data.put("action", action);
        data.put("emoji", emoji);
        data.put("target", nationalId);
        data.put("timestamp", System.currentTimeMillis());
        data.put("from", "admin");

        db.collection(COLLECTION).add(data)
            .addOnSuccessListener(doc -> {
                toast("✅ Firestore: تم");
                sendFcmToUser(nationalId, title, message, type, emoji);
                clearForm();
            })
            .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
    }

    /** ═══ إرسال FCM Push للمستخدم ═══ */
    private void sendFcmToUser(String nationalId, String title, String message,
                                String type, String emoji) {
        db.collection("fcm_tokens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    toast("⚠️ ما فيه FCM token — الإشعار في التطبيق فقط");
                    return;
                }
                String fcmToken = doc.getString("token");
                if (fcmToken == null || fcmToken.isEmpty()) {
                    toast("⚠️ Token فارغ");
                    return;
                }

                java.util.Map<String, String> data = new java.util.HashMap<>();
                data.put("type", type != null ? type : "admin");
                data.put("emoji", emoji != null ? emoji : "🔔");

                String fullTitle = (emoji != null ? emoji + " " : "🔔 ") + title;

                FcmSender.sendToToken(this, fcmToken, fullTitle, message, data,
                    new FcmSender.Callback() {
                        @Override public void onSuccess() {
                            runOnUiThread(() -> toast("✅ FCM: تم الإرسال"));
                        }
                        @Override public void onError(String error) {
                            runOnUiThread(() -> toast("⚠️ FCM: " + error));
                        }
                    });
            })
            .addOnFailureListener(e -> toast("⚠️ ما لقيناش FCM token"));
    }

    private void sendToAll(String title, String message, String type,
                            String action, String emoji) {
        
        // نجيبو كل المواطنين
        db.collection("citizens").get()
                .addOnSuccessListener(snapshot -> {
                    int total = snapshot.size();
                    if (total == 0) {
                        toast("⚠️ ما فيهش مواطنين");
                        return;
                    }
                    
                    toast("📤 جاري إرسال " + total + " إشعار...");
                    
                                        final int[] success = {0};
                    final int[] failed = {0};
                    final int[] fcmSent = {0};
                    long now = System.currentTimeMillis();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nationalId = doc.getString("nationalId");
                        if (nationalId == null || nationalId.isEmpty()) {
                            failed[0]++;
                            continue;
                        }

                        // ═══ 1. Firestore notification ═══
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("title", title);
                        data.put("message", message);
                        data.put("type", type);
                        data.put("action", action);
                        data.put("emoji", emoji);
                        data.put("target", nationalId);
                        data.put("timestamp", now);
                        data.put("from", "admin");

                        db.collection(COLLECTION).add(data)
                                .addOnSuccessListener(d -> success[0]++)
                                .addOnFailureListener(e -> failed[0]++);

                        // ═══ 2. FCM Push ═══
                        final String nid = nationalId;
                        final String fTitle = title;
                        final String fMessage = message;
                        final String fType = type;
                        final String fEmoji = emoji;

                        db.collection("fcm_tokens").document(nid).get()
                                .addOnSuccessListener(tokenDoc -> {
                                    if (tokenDoc.exists()) {
                                        String token = tokenDoc.getString("token");
                                        if (token != null && !token.isEmpty()) {
                                            java.util.Map<String, String> extras = new java.util.HashMap<>();
                                            extras.put("type", fType != null ? fType : "admin");
                                            extras.put("emoji", fEmoji != null ? fEmoji : "🔔");

                                            String fullTitle = (fEmoji != null ? fEmoji + " " : "🔔 ") + fTitle;

                                            FcmSender.sendToToken(
                                                AdminNotifyActivity.this,
                                                token, fullTitle, fMessage, extras,
                                                new FcmSender.Callback() {
                                                    @Override public void onSuccess() {
                                                        fcmSent[0]++;
                                                    }
                                                    @Override public void onError(String e) {}
                                                });
                                        }
                                    }
                                });
                    }

                    // ═══ النتيجة ═══
                    new android.os.Handler().postDelayed(() -> {
                        toast("✅ Firestore: " + success[0] + "/" + total + " | 📱 FCM: " + fcmSent[0]);
                        clearForm();
                    }, 4000);
                })
                .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
    }

    private void clearForm() {
        etTitle.setText("");
        etMessage.setText("");
        etNationalId.setText("");
    }

    // ═══════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════
    private String typeFromId(int id) {
        switch (id) {
            case 2: return "warning";
            case 3: return "reward";
            case 4: return "promo";
            case 5: return "event";
        }
        return "info";
    }

    private String emojiFromType(String type) {
        switch (type) {
            case "warning": return "⚠️";
            case "reward": return "🎁";
            case "promo": return "📢";
            case "event": return "🎉";
        }
        return "ℹ️";
    }

    private String actionFromId(int id) {
        switch (id) {
            case 1: return "wallet";
            case 2: return "wheel";
            case 3: return "city";
            case 4: return "market";
            case 5: return "election";
        }
        return "none";
    }

    private void addSectionTitle(LinearLayout root, String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setPadding(0, 32, 0, 12);

        View lineL = new View(this);
        LinearLayout.LayoutParams lLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineL.setLayoutParams(lLp);
        lineL.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineL);

        TextView t = new TextView(this);
        t.setText("  " + text + "  ");
        t.setTextColor(Color.parseColor("#D4AF37"));
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
        container.addView(t);

        View lineR = new View(this);
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(0, 2, 1f);
        lineR.setLayoutParams(rLp);
        lineR.setBackgroundColor(Color.parseColor("#2A3D32"));
        container.addView(lineR);

        root.addView(container);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
