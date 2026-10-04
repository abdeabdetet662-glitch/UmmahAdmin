package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminMurderMysteryActivity — إدارة جريمة أُمّة
 */
public class AdminMurderMysteryActivity extends Activity {

    private FirebaseFirestore db;
    private LinearLayout root;
    private LinearLayout gamesContainer;

    private ListenerRegistration gamesReg;

    private static final String COLLECTION = "murder_mysteries";

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = FirebaseFirestore.getInstance();

        buildUI();
        loadGames();
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundResource(R.drawable.bg_screen);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(50), dp(24), dp(60));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root);

        // Header
        TextView icon = new TextView(this);
        icon.setText("🕵️");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("إدارة جريمة أُمّة");
        title.setTextColor(Color.parseColor("#D4AF37"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(12), 0, dp(4));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("تحكم كامل في القضايا الأسبوعية");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, dp(24));
        root.addView(sub);

        // زر إنشاء جلسة جديدة
        Button newBtn = new Button(this);
        newBtn.setText("➕ إنشاء جلسة جديدة");
        newBtn.setTextSize(15);
        newBtn.setTextColor(Color.parseColor("#0A0510"));
        newBtn.setAllCaps(false);
        newBtn.setTypeface(null, Typeface.BOLD);
        newBtn.setBackgroundResource(R.drawable.bg_btn_gold);
        LinearLayout.LayoutParams nbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nbLp.setMargins(0, 0, 0, dp(20));
        newBtn.setLayoutParams(nbLp);
        newBtn.setOnClickListener(v -> showCreateDialog());
        root.addView(newBtn);

        // Games Container
        TextView gamesHeader = new TextView(this);
        gamesHeader.setText("═══ الجلسات ═══");
        gamesHeader.setTextColor(Color.parseColor("#D4AF37"));
        gamesHeader.setTextSize(16);
        gamesHeader.setTypeface(null, Typeface.BOLD);
        gamesHeader.setGravity(Gravity.CENTER);
        gamesHeader.setPadding(0, dp(20), 0, dp(16));
        root.addView(gamesHeader);

        gamesContainer = new LinearLayout(this);
        gamesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(gamesContainer);

        setContentView(scroll);
    }

    private void loadGames() {
        gamesReg = db.collection(COLLECTION)
            .limit(10)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                runOnUiThread(() -> renderGames(snap));
            });
    }

    private void renderGames(com.google.firebase.firestore.QuerySnapshot snap) {
        gamesContainer.removeAllViews();

        if (snap.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("لا توجد جلسات\nاضغط \"إنشاء جلسة جديدة\"");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));
            gamesContainer.addView(empty);
            return;
        }

        for (DocumentSnapshot doc : snap.getDocuments()) {
            View card = createGameCard(doc);
            gamesContainer.addView(card);
        }
    }

    private View createGameCard(DocumentSnapshot doc) {
        String gameId = doc.getId();
        String title = doc.getString("title");
        String status = doc.getString("status");
        Long currentPlayers = doc.getLong("currentPlayers");
        Long maxPlayers = doc.getLong("maxPlayers");
        String killerId = doc.getString("killerId");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(dp(20), dp(16), dp(20), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(lp);

        // Title + Status
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView t = new TextView(this);
        t.setText(title != null ? title : "بدون عنوان");
        t.setTextColor(Color.WHITE);
        t.setTextSize(15);
        t.setTypeface(null, Typeface.BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(t);

        TextView badge = new TextView(this);
        badge.setText(getStatusAr(status));
        badge.setTextColor(Color.parseColor(getStatusColor(status)));
        badge.setTextSize(11);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setPadding(dp(12), dp(4), dp(12), dp(4));
        badge.setBackgroundResource(R.drawable.bg_card);
        headerRow.addView(badge);

        card.addView(headerRow);

        // Players count
        TextView players = new TextView(this);
        players.setText("👥 " + (currentPlayers != null ? currentPlayers : 0) +
                " / " + (maxPlayers != null ? maxPlayers : 20));
        players.setTextColor(Color.parseColor("#10B981"));
        players.setTextSize(12);
        players.setPadding(0, dp(8), 0, 0);
        card.addView(players);

        // Killer info
        if (killerId != null) {
            TextView k = new TextView(this);
            k.setText("🎭 القاتل: " + killerId);
            k.setTextColor(Color.parseColor("#F44336"));
            k.setTextSize(11);
            k.setPadding(0, dp(4), 0, 0);
            card.addView(k);
        }

        // Action buttons
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(12), 0, 0);
        actions.setGravity(Gravity.CENTER_VERTICAL);

        // زر "اختيار القاتل" (إذا registration)
        if ("registration".equals(status)) {
            TextView startBtn = createActionBtn("🎯 ابدأ القضية", "#D4AF37");
            startBtn.setOnClickListener(v -> chooseKiller(gameId));
            actions.addView(startBtn);
        }

        // زر "إضافة دليل" (إذا playing)
        if ("playing".equals(status)) {
            TextView clueBtn = createActionBtn("🔍 دليل", "#3B82F6");
            clueBtn.setOnClickListener(v -> showClueDialog(gameId));
            actions.addView(clueBtn);

            TextView voteBtn = createActionBtn("🗳️ تصويت", "#F59E0B");
            voteBtn.setOnClickListener(v -> startVoting(gameId));
            actions.addView(voteBtn);
        }

        // زر "أكشف" (إذا voting)
        if ("voting".equals(status)) {
            TextView announceBtn = createActionBtn("🏆 أكشف", "#10B981");
            announceBtn.setOnClickListener(v -> announceResult(gameId));
            actions.addView(announceBtn);
        }

        // زر حذف دائماً
        TextView delBtn = createActionBtn("🗑️", "#F44336");
        delBtn.setOnClickListener(v -> confirmDelete(gameId));
        actions.addView(delBtn);

        card.addView(actions);
        return card;
    }

    private TextView createActionBtn(String text, String color) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.parseColor(color));
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(12), dp(8), dp(12), dp(8));
        btn.setBackgroundResource(R.drawable.bg_card);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, dp(8), 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    // ═══════════════════════════════════════════
    //  Actions
    // ═══════════════════════════════════════════

    /** إنشاء جلسة جديدة */
    private void showCreateDialog() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("➕ جلسة جديدة");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(16), dp(24), dp(16));

        EditText titleInput = createInput("عنوان الجريمة", layout);
        EditText descInput = createInput("الوصف القصير", layout);
        EditText storyInput = createInput("القصة الكاملة", layout);
        EditText feeInput = createInput("رسوم الاشتراك (500)", layout);
        EditText prizeInput = createInput("الجائزة (100000)", layout);

        feeInput.setText("500");
        prizeInput.setText("100000");

        d.setView(layout);
        d.setPositiveButton("إنشاء", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String story = storyInput.getText().toString().trim();
            int fee = parseInt(feeInput.getText().toString(), 500);
            int prize = parseInt(prizeInput.getText().toString(), 100000);

            if (title.isEmpty()) {
                toast("⚠️ اكتب العنوان");
                return;
            }

            createGame(title, desc, story, fee, prize);
        });
        d.setNegativeButton("إلغاء", null);
        d.show();
    }

    private EditText createInput(String hint, LinearLayout parent) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setHintTextColor(Color.parseColor("#666666"));
        et.setTextColor(Color.WHITE);
        et.setBackgroundResource(R.drawable.bg_card);
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        et.setLayoutParams(lp);
        parent.addView(et);
        return et;
    }

    private void createGame(String title, String desc, String story, int fee, int prize) {
        long now = System.currentTimeMillis();
        long regEnd = now + (24 * 60 * 60 * 1000L); // بعد 24 ساعة

        Map<String, Object> game = new HashMap<>();
        game.put("title", title);
        game.put("description", desc);
        game.put("story", story);
        game.put("status", "registration");
        game.put("entryFee", fee);
        game.put("prizePool", prize);
        game.put("maxPlayers", 20);
        game.put("currentPlayers", 0);
        game.put("registrationEnd", regEnd);
        game.put("createdAt", now);

        db.collection(COLLECTION).add(game)
            .addOnSuccessListener(doc -> toast("✅ تم إنشاء الجلسة"))
            .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
    }

    /** اختيار القاتل وبدء اللعبة */
    private void chooseKiller(String gameId) {
        new AlertDialog.Builder(this)
            .setTitle("🎯 اختيار القاتل")
            .setMessage("رايح يتم اختيار القاتل عشوائياً من المشاركين\n\nالمشاركون الحاليون؟")
            .setPositiveButton("✅ ابدأ", (d, w) -> {
                db.collection(COLLECTION).document(gameId)
                    .collection("players").get()
                    .addOnSuccessListener(playersSnap -> {
                        if (playersSnap.isEmpty()) {
                            toast("⚠️ ما فيهاش لاعبين");
                            return;
                        }

                        java.util.List<String> ids = new java.util.ArrayList<>();
                        for (DocumentSnapshot p : playersSnap.getDocuments()) {
                            ids.add(p.getId());
                        }

                        java.util.Collections.shuffle(ids);
                        String killerId = ids.get(0);

                        // تعيين القاتل
                        db.collection(COLLECTION).document(gameId)
                            .collection("players").document(killerId)
                            .update("role", "killer");

                        // تحديث الحالة
                        Map<String, Object> upd = new HashMap<>();
                        upd.put("killerId", killerId);
                        upd.put("status", "playing");
                        upd.put("startTime", System.currentTimeMillis());
                        upd.put("endTime", System.currentTimeMillis() + 24 * 60 * 60 * 1000L);

                        db.collection(COLLECTION).document(gameId).update(upd);

                        // إضافة دليل أولي
                        Map<String, Object> clue1 = new HashMap<>();
                        clue1.put("title", "الجثة");
                        clue1.put("description", "تم العثور على الملك ميتاً في مكتبه");
                        clue1.put("icon", "💀");
                        clue1.put("revealedAt", System.currentTimeMillis());
                        clue1.put("revealsFor", "all");
                        clue1.put("isKeyClue", true);
                        clue1.put("order", 1);

                        db.collection(COLLECTION).document(gameId)
                            .collection("clues").add(clue1);

                        toast("🎯 القاتل: " + killerId + "\nاللعبة بدأت!");
                    });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    /** إضافة دليل */
    private void showClueDialog(String gameId) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("🔍 إضافة دليل");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(16), dp(24), dp(16));

        EditText iconInput = createInput("الأيقونة (🗡️ 💀 🔑 ...)", layout);
        EditText titleInput = createInput("عنوان الدليل", layout);
        EditText descInput = createInput("الوصف", layout);

        iconInput.setText("🔍");

        d.setView(layout);
        d.setPositiveButton("إضافة", (dialog, which) -> {
            String icon = iconInput.getText().toString().trim();
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();

            if (title.isEmpty()) {
                toast("⚠️ اكتب العنوان");
                return;
            }

            // نحسبو الـ order
            db.collection(COLLECTION).document(gameId)
                .collection("clues").get()
                .addOnSuccessListener(snap -> {
                    int order = snap.size() + 1;

                    Map<String, Object> clue = new HashMap<>();
                    clue.put("icon", icon.isEmpty() ? "🔍" : icon);
                    clue.put("title", title);
                    clue.put("description", desc);
                    clue.put("revealedAt", System.currentTimeMillis());
                    clue.put("revealsFor", "all");
                    clue.put("isKeyClue", false);
                    clue.put("order", order);

                    db.collection(COLLECTION).document(gameId)
                        .collection("clues").add(clue)
                        .addOnSuccessListener(doc -> toast("✅ دليل جديد"))
                        .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
                });
        });
        d.setNegativeButton("إلغاء", null);
        d.show();
    }

    /** بدء التصويت */
    private void startVoting(String gameId) {
        new AlertDialog.Builder(this)
            .setTitle("🗳️ بدء التصويت")
            .setMessage("المحققون بدأو يجمعون الأدلة.\nرايح نبدأ التصويت النهائي؟")
            .setPositiveButton("✅ نعم", (d, w) -> {
                Map<String, Object> upd = new HashMap<>();
                upd.put("status", "voting");

                db.collection(COLLECTION).document(gameId).update(upd)
                    .addOnSuccessListener(v -> toast("🗳️ التصويت بدأ"))
                    .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    /** كشف النتيجة + توزيع الجائزة */
    private void announceResult(String gameId) {
        new AlertDialog.Builder(this)
            .setTitle("🏆 كشف الحقيقة")
            .setMessage("رايح يتم كشف القاتل وتوزيع الجائزة.\n\nموافق؟")
            .setPositiveButton("✅ أكشف", (d, w) -> {
                db.collection(COLLECTION).document(gameId).get()
                    .addOnSuccessListener(gameDoc -> {
                        String killerId = gameDoc.getString("killerId");
                        Long prizeObj = gameDoc.getLong("prizePool");
                        int prize = prizeObj != null ? prizeObj.intValue() : 100000;

                        if (killerId == null) {
                            toast("⚠️ ما فيه قاتل");
                            return;
                        }

                        // نجيبو التصويتات
                        db.collection(COLLECTION).document(gameId)
                            .collection("votes").get()
                            .addOnSuccessListener(votesSnap -> {
                                Map<String, Integer> counts = new HashMap<>();
                                for (DocumentSnapshot v : votesSnap.getDocuments()) {
                                    String to = v.getString("toUser");
                                    if (to != null) {
                                        counts.put(to, counts.getOrDefault(to, 0) + 1);
                                    }
                                }

                                String mostVoted = null;
                                int max = 0;
                                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                                    if (e.getValue() > max) {
                                        max = e.getValue();
                                        mostVoted = e.getKey();
                                    }
                                }

                                boolean caught = mostVoted != null && mostVoted.equals(killerId);
                                String winner = caught ? mostVoted : killerId;

                                // تحديث الحالة
                                Map<String, Object> upd = new HashMap<>();
                                upd.put("status", "ended");
                                upd.put("winnerId", winner);
                                upd.put("solved", caught);
                                upd.put("endTime", System.currentTimeMillis());

                                db.collection(COLLECTION).document(gameId).update(upd);

                                // توزيع الجائزة
                                db.collection("citizens").document(winner)
                                    .update("balance",
                                        com.google.firebase.firestore.FieldValue.increment(prize))
                                    .addOnSuccessListener(v -> {
                                        String msg = caught
                                            ? "✅ المحققون نجحوا!\n💰 " + winner + " أخذ " + prize + " Đ"
                                            : "❌ المحققون فشلوا!\n💰 " + winner + " (القاتل) أخذ " + prize + " Đ";
                                        toast(msg);
                                    });
                            });
                    });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    /** تأكيد الحذف */
    private void confirmDelete(String gameId) {
        new AlertDialog.Builder(this)
            .setTitle("🗑️ حذف الجلسة")
            .setMessage("رايح يتم حذف الجلسة نهائياً.\n\nمتأكد؟")
            .setPositiveButton("✅ احذف", (d, w) -> {
                db.collection(COLLECTION).document(gameId).delete()
                    .addOnSuccessListener(v -> toast("🗑️ تم الحذف"))
                    .addOnFailureListener(e -> toast("❌ " + e.getMessage()));
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ═══ Helpers ═══
    private String getStatusAr(String status) {
        if (status == null) return "؟";
        switch (status) {
            case "registration": return "📝 تسجيل";
            case "playing": return "🔍 جارية";
            case "voting": return "🗳️ تصويت";
            case "ended": return "✅ منتهية";
            default: return status;
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#666666";
        switch (status) {
            case "registration": return "#3B82F6";
            case "playing": return "#F59E0B";
            case "voting": return "#D4AF37";
            case "ended": return "#10B981";
            default: return "#666666";
        }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        if (gamesReg != null) gamesReg.remove();
        super.onDestroy();
    }
}
