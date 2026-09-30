package com.ummah.admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentReference;

public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public static final String ADMIN_PASSWORD = "ummah2026";

    private AdminManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static AdminManager get() {
        if (instance == null) instance = new AdminManager();
        return instance;
    }

    public interface OnDone { void onSuccess(); void onError(String msg); }

    public void signIn(final OnDone cb) {
        if (auth.getCurrentUser() != null) { cb.onSuccess(); return; }
        auth.signInAnonymously()
            .addOnSuccessListener(r -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ المواطنون ============
    public static class Citizen {
        public String nationalId, name, joinDate, country;
        public int balance;
        public long lastSeen;
        public boolean online, blocked, muted;
    }

    public interface CitizensListener { void onList(List<Citizen> list); }

    public ListenerRegistration listenAllCitizens(final CitizensListener l) {
        return db.collection("citizens").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Citizen> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Citizen c = new Citizen();
                c.nationalId = d.getId();
                c.name = d.getString("name");
                c.joinDate = d.getString("joinDate");
                c.country = d.getString("country");
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

    public void setBalance(String nationalId, int newBalance, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", newBalance)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addBalance(String nationalId, int amount, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setBlocked(String nationalId, boolean blocked, OnDone cb) {
        db.collection("citizens").document(nationalId)
            .update("blocked", blocked)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setMuted(String nationalId, boolean muted, long until, OnDone cb) {
        Map<String, Object> u = new HashMap<>();
        u.put("muted", muted);
        u.put("mutedUntil", until);
        db.collection("citizens").document(nationalId).update(u)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteCitizen(String nationalId, OnDone cb) {
        db.collection("citizens").document(nationalId).delete()
            .addOnSuccessListener(a -> {
                db.collection("global_chat").whereEqualTo("nationalId", nationalId).get()
                    .addOnSuccessListener(q -> {
                        for (QueryDocumentSnapshot d : q) d.getReference().delete();
                        cb.onSuccess();
                    })
                    .addOnFailureListener(e -> cb.onSuccess());
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public interface SingleCitizenListener {
        void onFound(Citizen c);
        void onNotFound();
    }

    public void loadCitizenDetails(String nationalId, final SingleCitizenListener l) {
        db.collection("citizens").document(nationalId).get()
            .addOnSuccessListener(doc -> {
                if (!doc.exists()) { l.onNotFound(); return; }
                Citizen c = new Citizen();
                c.nationalId = doc.getId();
                c.name = doc.getString("name");
                c.joinDate = doc.getString("joinDate");
                c.country = doc.getString("country");
                Long b = doc.getLong("balance");
                c.balance = b != null ? b.intValue() : 0;
                Long ls = doc.getLong("lastSeen");
                c.lastSeen = ls != null ? ls : 0;
                Boolean on = doc.getBoolean("online");
                c.online = on != null && on;
                Boolean bl = doc.getBoolean("blocked");
                c.blocked = bl != null && bl;
                Boolean mu = doc.getBoolean("muted");
                c.muted = mu != null && mu;
                l.onFound(c);
            })
            .addOnFailureListener(e -> l.onNotFound());
    }

    // ============ المحظورون / المكتومون ============
    public interface BannedListener { void onList(List<Citizen> list); }

    public ListenerRegistration listenBlocked(final BannedListener l) {
        return db.collection("citizens").whereEqualTo("blocked", true)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Citizen> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Citizen c = new Citizen();
                    c.nationalId = d.getId();
                    c.name = d.getString("name");
                    Long b = d.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    list.add(c);
                }
                l.onList(list);
            });
    }

    public ListenerRegistration listenMuted(final BannedListener l) {
        return db.collection("citizens").whereEqualTo("muted", true)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Citizen> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Citizen c = new Citizen();
                    c.nationalId = d.getId();
                    c.name = d.getString("name");
                    Long b = d.getLong("balance");
                    c.balance = b != null ? b.intValue() : 0;
                    list.add(c);
                }
                l.onList(list);
            });
    }

    // ============ الإبلاغات ============
    public static class Report {
        public String id, reporterName, reportedName, reportedId, messageText, messageId;
        public long timestamp;
    }

    public interface ReportsListener { void onList(List<Report> list); }

    public ListenerRegistration listenReports(final ReportsListener l) {
        return db.collection("reports").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Report> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Report r = new Report();
                r.id = d.getId();
                r.reporterName = d.getString("reporterName");
                r.reportedName = d.getString("reportedName");
                r.reportedId = d.getString("reportedId");
                r.messageText = d.getString("messageText");
                r.messageId = d.getString("messageId");
                Long t = d.getLong("timestamp");
                r.timestamp = t != null ? t : 0;
                list.add(r);
            }
            java.util.Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            l.onList(list);
        });
    }

    public void deleteReport(String id, OnDone cb) {
        db.collection("reports").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الهدايا ============
    public void sendGift(String fromName, String toId, String emoji, String giftName, String meaning, int price, OnDone cb) {
        Map<String, Object> gift = new HashMap<>();
        gift.put("fromId", "ADMIN");
        gift.put("fromName", "الإدارة - " + fromName);
        gift.put("toId", toId);
        gift.put("emoji", emoji);
        gift.put("giftName", giftName);
        gift.put("meaning", meaning);
        gift.put("price", price);
        gift.put("timestamp", System.currentTimeMillis());
        db.collection("gifts").add(gift)
            .addOnSuccessListener(x -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الإذاعة ============
    public void broadcast(String message, OnDone cb) {
        Map<String, Object> n = new HashMap<>();
        n.put("author", "📢 الإدارة");
        n.put("content", message);
        n.put("timestamp", System.currentTimeMillis());
        n.put("isBroadcast", true);
        db.collection("news").add(n)
            .addOnSuccessListener(d -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الإحصائيات ============
    public interface StatsListener {
        void onStats(int citizens, int gifts, int transfers, int news, int proposals, int complaints, long treasury, int totalVotes);
    }

    public void loadStats(final StatsListener l) {
        final int[] counts = new int[6];
        final long[] treasuryVal = new long[1];

        db.collection("citizens").get().addOnSuccessListener(q -> {
            counts[0] = q.size();
            db.collection("gifts").get().addOnSuccessListener(q2 -> {
                counts[1] = q2.size();
                db.collection("transfers").get().addOnSuccessListener(q3 -> {
                    counts[2] = q3.size();
                    db.collection("news").get().addOnSuccessListener(q4 -> {
                        counts[3] = q4.size();
                        db.collection("proposals").get().addOnSuccessListener(q5 -> {
                            counts[4] = q5.size();
                            db.collection("court_cases").get().addOnSuccessListener(q6 -> {
                                counts[5] = q6.size();
                                db.collection("treasury").document("main").get()
                                    .addOnSuccessListener(doc -> {
                                        Long tb = doc.getLong("balance");
                                        treasuryVal[0] = tb != null ? tb : 0;
                                        db.collection("constitution_votes").get()
                                            .addOnSuccessListener(q7 ->
                                                l.onStats(counts[0], counts[1], counts[2],
                                                    counts[3], counts[4], counts[5],
                                                    treasuryVal[0], q7.size()));
                                    });
                            });
                        });
                    });
                });
            });
        });
    }

    // ============ الشكاوى ============
    public static class Complaint {
        public String id, plaintiffName, defendantName, claim, recommendation, status;
        public long timestamp;
    }

    public interface ComplaintsListener { void onList(List<Complaint> list); }

    public ListenerRegistration listenComplaints(final ComplaintsListener l) {
        return db.collection("court_cases").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Complaint> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Complaint c = new Complaint();
                c.id = d.getId();
                c.plaintiffName = d.getString("plaintiffName");
                c.defendantName = d.getString("defendantName");
                c.claim = d.getString("claim");
                c.recommendation = d.getString("recommendation");
                c.status = d.getString("status");
                Long t = d.getLong("timestamp");
                c.timestamp = t != null ? t : 0;
                list.add(c);
            }
            java.util.Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            l.onList(list);
        });
    }

    public void updateComplaintStatus(String caseId, String status, OnDone cb) {
        db.collection("court_cases").document(caseId).update("status", status)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteComplaint(String caseId, OnDone cb) {
        db.collection("court_cases").document(caseId).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ التصويت ============
    public interface VotesListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenConstitutionVotes(final VotesListener l) {
        return db.collection("constitution_votes").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Map<String, Object>> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Map<String, Object> m = d.getData();
                m.put("_id", d.getId());
                list.add(m);
            }
            l.onList(list);
        });
    }

    public void deleteConstitutionVote(String docId, OnDone cb) {
        db.collection("constitution_votes").document(docId).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addConstitutionVote(String nationalId, int article, boolean yes, OnDone cb) {
        Map<String, Object> v = new HashMap<>();
        v.put("nationalId", nationalId);
        v.put("article", article);
        v.put("yes", yes);
        v.put("timestamp", System.currentTimeMillis());
        v.put("adminAdded", true);
        db.collection("constitution_votes").document("admin_" + System.currentTimeMillis()).set(v)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الاقتراحات ============
    public interface ProposalsListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenProposals(final ProposalsListener l) {
        return db.collection("proposals").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Map<String, Object>> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Map<String, Object> m = d.getData();
                m.put("_id", d.getId());
                list.add(m);
            }
            l.onList(list);
        });
    }

    public void deleteProposal(String id, OnDone cb) {
        db.collection("proposals").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void setProposalVotes(String id, int yes, int no, OnDone cb) {
        Map<String, Object> u = new HashMap<>();
        u.put("yes", yes);
        u.put("no", no);
        db.collection("proposals").document(id).update(u)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الدردشة ============
    public interface ChatsListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenGlobalChat(final ChatsListener l) {
        return db.collection("global_chat").limit(200)
            .addSnapshotListener((snap, e) -> {
                if (snap == null) return;
                List<Map<String, Object>> list = new ArrayList<>();
                for (QueryDocumentSnapshot d : snap) {
                    Map<String, Object> m = d.getData();
                    m.put("_id", d.getId());
                    list.add(m);
                }
                java.util.Collections.sort(list, (a, b) -> {
                    Long ta = (Long) a.get("timestamp");
                    Long tb = (Long) b.get("timestamp");
                    if (ta == null || tb == null) return 0;
                    return Long.compare(tb, ta);
                });
                l.onList(list);
            });
    }

    public void deleteChatMessage(String id, OnDone cb) {
        db.collection("global_chat").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الخزينة ============
    public void setTreasury(long newBalance, OnDone cb) {
        Map<String, Object> data = new HashMap<>();
        data.put("balance", newBalance);
        db.collection("treasury").document("main").set(data)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void addTreasury(long amount, OnDone cb) {
        db.collection("treasury").document("main")
            .update("balance", FieldValue.increment(amount))
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    // ============ الانتخابات ============
    public interface CandidatesListener { void onList(List<Map<String, Object>> list); }

    public ListenerRegistration listenCandidates(final CandidatesListener l) {
        return db.collection("candidates").addSnapshotListener((snap, e) -> {
            if (snap == null) return;
            List<Map<String, Object>> list = new ArrayList<>();
            for (QueryDocumentSnapshot d : snap) {
                Map<String, Object> m = d.getData();
                m.put("_id", d.getId());
                list.add(m);
            }
            l.onList(list);
        });
    }

    public void setCandidateVotes(String id, int votes, OnDone cb) {
        db.collection("candidates").document(id).update("votes", votes)
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void deleteCandidate(String id, OnDone cb) {
        db.collection("candidates").document(id).delete()
            .addOnSuccessListener(a -> cb.onSuccess())
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void resetElection(OnDone cb) {
        db.collection("votes").get().addOnSuccessListener(q -> {
            for (QueryDocumentSnapshot d : q) d.getReference().delete();
            db.collection("candidates").get().addOnSuccessListener(q2 -> {
                for (QueryDocumentSnapshot d : q2) d.getReference().update("votes", 0);
                cb.onSuccess();
            });
        });
    }

    public void deleteReportAndMessage(String reportId, String messageId, OnDone cb) {
        if (messageId != null && !messageId.isEmpty()) {
            db.collection("global_chat").document(messageId).delete();
        }
        deleteReport(reportId, cb);
    }


    // ═══════════════════════════════════════
    //  sendPrivateMessage (رسالة خاصة)
    // ═══════════════════════════════════════
    public void sendPrivateMessage(String nationalId, String message, OnDone cb) {
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
    //  logAction (تسجيل إجراء إداري)
    // ═══════════════════════════════════════
    public void logAction(String action, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("details", details);
        log.put("timestamp", System.currentTimeMillis());
        log.put("admin", "admin");
        db.collection("admin_logs").add(log);
    }

    // ═══════════════════════════════════════
    //  LogEntry + listenLogs
    // ═══════════════════════════════════════
    public static class LogEntry {
        public String action, details, admin;
        public long timestamp;
    }

    public interface LogListener { void onLogs(List<LogEntry> list); }

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
    //  Market management
    // ═══════════════════════════════════════
    public void deleteMarketItem(String itemId, OnDone cb) {
        db.collection("market_items").document(itemId).delete()
            .addOnSuccessListener(a -> {
                logAction("delete_item", itemId);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

    public void updateMarketPrice(String itemId, int newPrice, OnDone cb) {
        db.collection("market_items").document(itemId)
            .update("price", newPrice)
            .addOnSuccessListener(a -> {
                logAction("update_price", itemId + " → " + newPrice);
                cb.onSuccess();
            })
            .addOnFailureListener(e -> cb.onError(e.getMessage()));
    }

}
