package com.ummah.admin;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * إدارة الميزات (Feature Control) من لوحة التحكم
 * 
 * يقرا ويكتب في: app_config/features
 */
public class AdminFeatureManager {

    private static final String COLLECTION = "app_config";
    private static final String DOC = "features";

    private static AdminFeatureManager instance;
    private final FirebaseFirestore db;

    // ═══════════════════════════════════════════
    //  قائمة الميزات
    // ═══════════════════════════════════════════
    public static class Feature {
        public String key;
        public String icon;
        public String name;
        public boolean enabled;

        public Feature(String key, String icon, String name) {
            this.key = key;
            this.icon = icon;
            this.name = name;
            this.enabled = true;
        }
    }

    public static List<Feature> getFeatureList() {
        List<Feature> list = new ArrayList<>();
        // الميزات الرئيسية
        list.add(new Feature("wheel", "🎡", "عجلة الحظ"));
        list.add(new Feature("market", "🛒", "السوق العام"));
        list.add(new Feature("chat", "💬", "الدردشة العامة"));
        list.add(new Feature("private_chat", "💌", "الدردشة الخاصة"));
        list.add(new Feature("parliament", "🏛️", "البرلمان"));
        list.add(new Feature("elections", "🗳️", "الانتخابات"));
        list.add(new Feature("court", "⚖️", "المحكمة"));
        list.add(new Feature("jobs", "💼", "الوظائف"));
        list.add(new Feature("city", "🏙️", "مدينة أُمّة"));
        // الميزات الثانوية
        list.add(new Feature("citizen_market", "🤝", "سوق المواطنين"));
        list.add(new Feature("gifts", "🎁", "الهدايا"));
        list.add(new Feature("news", "📰", "الأخبار"));
        list.add(new Feature("leaderboard", "🏆", "المتصدرون"));
        list.add(new Feature("transfers", "💰", "التحويلات"));
        list.add(new Feature("stats", "📊", "الإحصائيات"));
        list.add(new Feature("treasury", "🏦", "الخزينة"));
        list.add(new Feature("daily_reward", "🎁", "المكافأة اليومية"));
        list.add(new Feature("president", "👑", "نظام الرئيس"));
        list.add(new Feature("registration", "📝", "تسجيل مواطنين جدد"));
        return list;
    }

    // ═══════════════════════════════════════════
    //  Singleton
    // ═══════════════════════════════════════════
    private AdminFeatureManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static AdminFeatureManager get() {
        if (instance == null) instance = new AdminFeatureManager();
        return instance;
    }

    // ═══════════════════════════════════════════
    //  Callbacks
    // ═══════════════════════════════════════════
    public interface FeaturesListener {
        void onFeatures(Map<String, Boolean> features);
        void onError(String msg);
    }

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    // ═══════════════════════════════════════════
    //  قراءة الميزات (Live)
    // ═══════════════════════════════════════════
    public ListenerRegistration listenFeatures(final FeaturesListener l) {
        return db.collection(COLLECTION).document(DOC)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        l.onError(error.getMessage());
                        return;
                    }
                    Map<String, Boolean> map = new HashMap<>();
                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            for (Map.Entry<String, Object> e : data.entrySet()) {
                                Object v = e.getValue();
                                boolean boolVal = (v instanceof Boolean) ? (Boolean) v : true;
                                map.put(e.getKey(), boolVal);
                            }
                        }
                    }
                    // إذا ماكانش، نعطي الافتراضية
                    if (map.isEmpty()) {
                        for (Feature f : getFeatureList()) {
                            map.put(f.key, true);
                        }
                    }
                    l.onFeatures(map);
                });
    }

    // ═══════════════════════════════════════════
    //  تحديث ميزة واحدة
    // ═══════════════════════════════════════════
    public void setFeature(String key, boolean enabled, OnDone cb) {
        Map<String, Object> update = new HashMap<>();
        update.put(key, enabled);
        update.put("last_update", System.currentTimeMillis());
        update.put("updated_by", "admin");

        db.collection(COLLECTION).document(DOC)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════════
    //  تحديث جماعي
    // ═══════════════════════════════════════════
    public void setBulk(Map<String, Object> updates, OnDone cb) {
        updates.put("last_update", System.currentTimeMillis());
        updates.put("updated_by", "admin");

        db.collection(COLLECTION).document(DOC)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════════
    //  تفعيل الكل
    // ═══════════════════════════════════════════
    public void enableAll(OnDone cb) {
        Map<String, Object> updates = new HashMap<>();
        for (Feature f : getFeatureList()) {
            updates.put(f.key, true);
        }
        setBulk(updates, cb);
    }

    // ═══════════════════════════════════════════
    //  تعطيل الكل (باستثناء حرج)
    // ═══════════════════════════════════════════
    public void disableAll(OnDone cb) {
        Map<String, Object> updates = new HashMap<>();
        for (Feature f : getFeatureList()) {
            // ما نعطلش التسجيل
            if (!f.key.equals("registration")) {
                updates.put(f.key, false);
            }
        }
        setBulk(updates, cb);
    }

    // ═══════════════════════════════════════════
    //  إجراءات سريعة
    // ═══════════════════════════════════════════
    public void setRegistrationOpen(boolean open, OnDone cb) {
        setFeature("registration", open, cb);
    }

    public void setMaintenanceMode(boolean on, String message, OnDone cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("maintenance_mode", on);
        updates.put("maintenance_message", message == null ? "" : message);
        setBulk(updates, cb);
    }

    public void setWheelMultiplier(int multiplier, OnDone cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("wheel_multiplier", multiplier);
        setBulk(updates, cb);
    }

    public void setTaxRate(int rate, OnDone cb) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("tax_rate", rate);
        setBulk(updates, cb);
    }

    // ═══════════════════════════════════════════
    //  قراءة الإعدادات العامة (مرة وحدة)
    // ═══════════════════════════════════════════
    public interface SettingsListener {
        void onSettings(boolean maintenanceMode, String maintenanceMessage,
                        int wheelMultiplier, int taxRate);
    }

    public void loadSettings(final SettingsListener l) {
        db.collection(COLLECTION).document(DOC)
                .get()
                .addOnSuccessListener(snapshot -> {
                    boolean maint = false;
                    String msg = "";
                    int mult = 1;
                    int tax = 5;

                    if (snapshot != null && snapshot.exists()) {
                        Boolean m = snapshot.getBoolean("maintenance_mode");
                        if (m != null) maint = m;
                        String s = snapshot.getString("maintenance_message");
                        if (s != null) msg = s;
                        Long ml = snapshot.getLong("wheel_multiplier");
                        if (ml != null) mult = ml.intValue();
                        Long t = snapshot.getLong("tax_rate");
                        if (t != null) tax = t.intValue();
                    }
                    l.onSettings(maint, msg, mult, tax);
                });
    }

    // ═══════════════════════════════════════════
    //  هدية جماعية
    // ═══════════════════════════════════════════
    public void sendGiftToAll(int amount, String reason, final OnDone cb) {
        // نحدث treasury كإجراء بسيط
        Map<String, Object> data = new HashMap<>();
        data.put("last_bulk_gift_amount", amount);
        data.put("last_bulk_gift_reason", reason);
        data.put("last_bulk_gift_time", System.currentTimeMillis());
        setBulk(data, cb);
    }

    // ═══════════════════════════════════════════
    //  سجل التغييرات (اختياري - نضيفه لاحقاً)
    // ═══════════════════════════════════════════
    public void logAction(String action, OnDone cb) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("timestamp", System.currentTimeMillis());
        log.put("by", "admin");

        db.collection("admin_logs")
                .add(log)
                .addOnSuccessListener(ref -> cb.onSuccess())
                .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
