package com.ummah.admin;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AdminCodeManager — إدارة الأكواد والهدايا
 */
public class AdminCodeManager {

    private static final String COLLECTION = "redeem_codes";
    private static AdminCodeManager instance;
    private final FirebaseFirestore db;

    private AdminCodeManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static AdminCodeManager get() {
        if (instance == null) instance = new AdminCodeManager();
        return instance;
    }

    // ═══════════════════════════════════════════
    //  نموذج الكود
    // ═══════════════════════════════════════════
    public static class RedeemCode {
        public String code;
        public String type;         // "coins" | "gift"
        public int amount;          // للعملات
        public String giftEmoji;    // للهدية
        public String giftName;
        public String note;
        public boolean used;
        public String usedBy;
        public long usedAt;
        public long createdAt;
        public long expiresAt;

        public boolean isExpired() {
            return expiresAt > 0 && System.currentTimeMillis() > expiresAt;
        }
    }

    // ═══════════════════════════════════════════
    //  Callbacks
    // ═══════════════════════════════════════════
    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public interface CodesListener {
        void onCodes(List<RedeemCode> codes);
    }

    // ═══════════════════════════════════════════
    //  توليد كود عشوائي
    // ═══════════════════════════════════════════
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public static String generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append("UMM-");
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        sb.append("-");
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(r.nextInt(CHARS.length())));
        return sb.toString();
    }

    // ═══════════════════════════════════════════
    //  توليد مجموعة أكواد
    // ═══════════════════════════════════════════
    public void generateCodes(int count, String type, int amount,
                              String giftEmoji, String giftName,
                              long expiresInDays, String note,
                              final OnDone cb) {
        final List<String> generatedCodes = new ArrayList<>();
        final long now = System.currentTimeMillis();
        final long expiresAt = expiresInDays > 0
                ? now + (expiresInDays * 24L * 60 * 60 * 1000)
                : 0;

        for (int i = 0; i < count; i++) {
            String code = generateCode();

            Map<String, Object> data = new HashMap<>();
            data.put("code", code);
            data.put("type", type);
            data.put("amount", amount);
            data.put("giftEmoji", giftEmoji != null ? giftEmoji : "");
            data.put("giftName", giftName != null ? giftName : "");
            data.put("note", note != null ? note : "");
            data.put("used", false);
            data.put("usedBy", "");
            data.put("usedAt", 0);
            data.put("createdAt", now);
            data.put("createdBy", "admin");
            data.put("expiresAt", expiresAt);

            final String finalCode = code;
            db.collection(COLLECTION).document(code).set(data);

            generatedCodes.add(code);
        }

        cb.onSuccess();
    }

    // ═══════════════════════════════════════════
    //  إرسال هدية مباشرة لمستخدم
    // ═══════════════════════════════════════════
    public void sendToUser(String nationalId, int amount, String note, final OnDone cb) {
        if (amount <= 0) {
            cb.onError("المبلغ لازم يكون أكثر من 0");
            return;
        }

        db.collection("citizens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    cb.onError("المواطن غير موجود");
                    return;
                }

                Long bal = doc.getLong("balance");
                int currentBal = bal != null ? bal.intValue() : 0;
                int newBal = currentBal + amount;

                db.collection("citizens").document(nationalId)
                    .update("balance", newBal)
                    .addOnSuccessListener(v -> {
                        // نسجلو العملية
                        Map<String, Object> log = new HashMap<>();
                        log.put("to", nationalId);
                        log.put("amount", amount);
                        log.put("note", note != null ? note : "هدية من الإدارة");
                        log.put("timestamp", System.currentTimeMillis());
                        log.put("by", "admin");
                        db.collection("admin_gifts").add(log);
                        cb.onSuccess();
                    })
                    .addOnFailureListener(e -> cb.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════════
    //  إرسال هدية جماعية لكل المواطنين
    // ═══════════════════════════════════════════
    public void sendToAll(int amount, String note, final OnDone cb) {
        if (amount <= 0) {
            cb.onError("المبلغ لازم يكون أكثر من 0");
            return;
        }

        db.collection("citizens").get()
            .addOnSuccessListener(query -> {
                int totalCitizens = query.size();
                if (totalCitizens == 0) {
                    cb.onError("ماكانش مواطنين");
                    return;
                }

                final int[] count = {0};
                final int[] failed = {0};

                for (DocumentSnapshot doc : query.getDocuments()) {
                    Long bal = doc.getLong("balance");
                    int currentBal = bal != null ? bal.intValue() : 0;
                    int newBal = currentBal + amount;

                    doc.getReference().update("balance", newBal)
                        .addOnSuccessListener(v -> {
                            count[0]++;
                            if (count[0] + failed[0] == totalCitizens) {
                                logBulkGift(amount, note, totalCitizens);
                                cb.onSuccess();
                            }
                        })
                        .addOnFailureListener(e -> {
                            failed[0]++;
                            if (count[0] + failed[0] == totalCitizens) {
                                cb.onSuccess();
                            }
                        });
                }
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    private void logBulkGift(int amount, String note, int total) {
        Map<String, Object> log = new HashMap<>();
        log.put("to", "ALL");
        log.put("amount", amount);
        log.put("total", total);
        log.put("totalSent", (long) amount * total);
        log.put("note", note != null ? note : "هدية جماعية");
        log.put("timestamp", System.currentTimeMillis());
        log.put("by", "admin");
        db.collection("admin_gifts").add(log);
    }

    // ═══════════════════════════════════════════
    //  الاستماع لكل الأكواد
    // ═══════════════════════════════════════════
    public ListenerRegistration listenCodes(final CodesListener l) {
        return db.collection(COLLECTION)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<RedeemCode> list = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    RedeemCode c = new RedeemCode();
                    c.code = d.getString("code");
                    c.type = d.getString("type");
                    Long a = d.getLong("amount");
                    c.amount = a != null ? a.intValue() : 0;
                    c.giftEmoji = d.getString("giftEmoji");
                    c.giftName = d.getString("giftName");
                    c.note = d.getString("note");
                    Boolean u = d.getBoolean("used");
                    c.used = u != null && u;
                    c.usedBy = d.getString("usedBy");
                    Long ua = d.getLong("usedAt");
                    c.usedAt = ua != null ? ua : 0;
                    Long ca = d.getLong("createdAt");
                    c.createdAt = ca != null ? ca : 0;
                    Long ea = d.getLong("expiresAt");
                    c.expiresAt = ea != null ? ea : 0;
                    list.add(c);
                }
                l.onCodes(list);
            });
    }

    // ═══════════════════════════════════════════
    //  حذف كود
    // ═══════════════════════════════════════════
    public void deleteCode(String code, final OnDone cb) {
        db.collection(COLLECTION).document(code).delete()
            .addOnSuccessListener(v -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════════
    //  حذف كل الأكواد المستعملة
    // ═══════════════════════════════════════════
    public void deleteUsedCodes(final OnDone cb) {
        db.collection(COLLECTION).whereEqualTo("used", true).get()
            .addOnSuccessListener(query -> {
                for (DocumentSnapshot d : query.getDocuments()) {
                    d.getReference().delete();
                }
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
