package com.ummah.admin;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;

    public static final String ADMIN_PASSWORD = "ummah2026";

    public interface OnDone {
        void onSuccess();
        void onError(String msg);
    }

    public interface OnDoneString {
        void onSuccess(String data);
        void onError(String msg);
    }

    private AdminManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static AdminManager get() {
        if (instance == null) instance = new AdminManager();
        return instance;
    }

    // ═══════════════════════════════════════
    // 1. Citizen Model
    // ═══════════════════════════════════════
    public static class Citizen {
        public String nationalId, name, joinDate, country;
        public int balance;
        public long lastSeen;
        public boolean online, blocked, muted;
        public String photoUrl;
    }

    // ═══════════════════════════════════════
    // 2. Stats Model
    // ═══════════════════════════════════════
    public static class Stats {
        public int citizens, onlineNow, gifts, transfers, news, proposals, complaints;
        public long treasury, totalBalance;
        public int votes;
    }

    public interface CitizensListener { void onList(List<Citizen> list); }
    public interface StatsListener { void onStats(Stats s); }
    public interface CountListener { void onCount(long total); }
    public interface StringListListener { void onList(List<String> list); }

    // ═══════════════════════════════════════
    // 3. Citizens
    // ═══════════════════════════════════════
    public ListenerRegistration listenAllCitizens(final CitizensListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Citizen> list = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (QueryDocumentSnapshot d : snap) {
                Citizen c = new Citizen();
                c.nationalId = d.getId();
                c.name = d.getString("name");
                c.joinDate = d.getString("joinDate");
                c.country = d.getString("country");
                c.photoUrl = d.getString("photoUrl");
                Long b = d.getLong("balance");
                c.balance = b != null ? b.intValue() : 0;
                Long ls = d.getLong("lastSeen");
                c.lastSeen = ls != null ? ls : 0;
                Boolean on = d.getBoolean("online");
                c.online = on != null && on;
                Boolean bl = d.getBoolean("blocked");
                c.blocked = bl != null && bl;
                Boolean mu = d.getBoolean("muted");
                c.muted = mu != null && mu;
                list.add(c);
            }
            l.onList(list);
        });
    }

    public void addBalance(String nationalId, int amount, final OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener(a -> {
                logAction("add_balance", "أضاف " + amount + " Đ لـ " + nationalId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setBalance(String nationalId, int newBalance, final OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", newBalance)
            .addOnSuccessListener(a -> {
                logAction("set_balance", "ضبط الرصيد " + nationalId + " → " + newBalance);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setBlocked(String nationalId, boolean blocked, final OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("blocked", blocked)
            .addOnSuccessListener(a -> {
                logAction(blocked ? "block" : "unblock", nationalId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setMuted(String nationalId, boolean muted, long until, final OnDone cb) {
        Map<String, Object> u = new HashMap<>();
        u.put("muted", muted);
        u.put("mutedUntil", until);
        db.collection("citizens").document(nationalId).update(u)
            .addOnSuccessListener(a -> {
                logAction(muted ? "mute" : "unmute", nationalId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteCitizen(String nationalId, final OnDone cb) {
        db.collection("citizens").document(nationalId).delete()
            .addOnSuccessListener(a -> {
                logAction("delete_citizen", nationalId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    // 4. Stats
    // ═══════════════════════════════════════
    public void loadStats(final StatsListener l) {
        Stats s = new Stats();

        db.collection("citizens").get().addOnSuccessListener(q -> {
            s.citizens = q.size();
            long total = 0;
            for (DocumentSnapshot d : q.getDocuments()) {
                Long b = d.getLong("balance");
                if (b != null) total += b;
                Boolean on = d.getBoolean("online");
                if (on != null && on) s.onlineNow++;
            }
            s.totalBalance = total;

            db.collection("gifts").get().addOnSuccessListener(q2 -> {
                s.gifts = q2.size();
                db.collection("transfers").get().addOnSuccessListener(q3 -> {
                    s.transfers = q3.size();
                    db.collection("news").get().addOnSuccessListener(q4 -> {
                        s.news = q4.size();
                        db.collection("proposals").get().addOnSuccessListener(q5 -> {
                            s.proposals = q5.size();
                            db.collection("complaints").get().addOnSuccessListener(q6 -> {
                                s.complaints = q6.size();
                                // Treasury
                                db.collection("treasury").document("main").get()
                                    .addOnSuccessListener(t -> {
                                        Long tb = t.getLong("balance");
                                        s.treasury = tb != null ? tb : 0;
                                        l.onStats(s);
                                    })
                                    .addOnFailureListener(e -> {
                                        s.treasury = 0;
                                        l.onStats(s);
                                    });
                            });
                        });
                    });
                });
            });
        });
    }

    // ═══════════════════════════════════════
    // 5. Broadcast
    // ═══════════════════════════════════════
    public void broadcast(String message, final OnDone cb) {
        Map<String, Object> n = new HashMap<>();
        n.put("author", "📢 الإدارة");
        n.put("content", message);
        n.put("timestamp", System.currentTimeMillis());
        n.put("isBroadcast", true);
        n.put("priority", "high");

        db.collection("news").add(n)
            .addOnSuccessListener(d -> {
                logAction("broadcast", message);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void sendPrivateMessage(String nationalId, String message, final OnDone cb) {
        Map<String, Object> n = new HashMap<>();
        n.put("toNationalId", nationalId);
        n.put("content", message);
        n.put("timestamp", System.currentTimeMillis());
        n.put("from", "الإدارة");
        n.put("read", false);

        db.collection("admin_messages").add(n)
            .addOnSuccessListener(d -> {
                logAction("private_msg", "→ " + nationalId + ": " + message);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    // 6. Audit Log
    // ═══════════════════════════════════════
    public void logAction(String action, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("details", details);
        log.put("timestamp", System.currentTimeMillis());
        log.put("admin", "admin");
        db.collection("admin_logs").add(log);
    }

    public interface LogListener { void onLogs(List<LogEntry> list); }

    public static class LogEntry {
        public String action, details, admin;
        public long timestamp;
    }

    public ListenerRegistration listenLogs(final LogListener l) {
        return db.collection("admin_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<LogEntry> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    LogEntry e2 = new LogEntry();
                    e2.action = d.getString("action");
                    e2.details = d.getString("details");
                    e2.admin = d.getString("admin");
                    Long ts = d.getLong("timestamp");
                    e2.timestamp = ts != null ? ts : 0;
                    list.add(e2);
                }
                l.onLogs(list);
            });
    }

    // ═══════════════════════════════════════
    // 7. Market Management
    // ═══════════════════════════════════════
    public void deleteMarketItem(String itemId, final OnDone cb) {
        db.collection("market_items").document(itemId).delete()
            .addOnSuccessListener(a -> {
                logAction("delete_item", itemId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void updateMarketPrice(String itemId, int newPrice, final OnDone cb) {
        db.collection("market_items").document(itemId)
            .update("price", newPrice)
            .addOnSuccessListener(a -> {
                logAction("update_price", itemId + " → " + newPrice);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ═══════════════════════════════════════
    // 8. Complaints
    // ═══════════════════════════════════════
    public void deleteComplaint(String id, final OnDone cb) {
        db.collection("complaints").document(id).delete()
            .addOnSuccessListener(a -> {
                logAction("delete_complaint", id);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void resolveComplaint(String id, String verdict, final OnDone cb) {
        Map<String, Object> u = new HashMap<>();
        u.put("status", "resolved");
        u.put("verdict", verdict);
        u.put("resolvedAt", System.currentTimeMillis());
        db.collection("complaints").document(id).update(u)
            .addOnSuccessListener(a -> {
                logAction("resolve_complaint", id + ": " + verdict);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }
}
