package com.ummah.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AdminCodesActivity — توليد وعرض أكواد الاستبدال
 */
public class AdminCodesActivity extends Activity {

    private AdminCodeManager acm;
    private LinearLayout codesContainer;
    private ListenerRegistration codesReg;
    private TextView statsView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        acm = AdminCodeManager.get();

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
        icon.setText("🎫");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = UiHelper.goldTitle(this, "أكواد الاستبدال", 26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("ولّد أكواد لا نهائية للعملات أو الهدايا");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // ═══ إحصائيات ═══
        statsView = new TextView(this);
        statsView.setText("⏳ جاري التحميل...");
        statsView.setTextColor(Color.WHITE);
        statsView.setTextSize(14);
        statsView.setGravity(Gravity.CENTER);
        statsView.setPadding(0, 0, 0, 20);
        root.addView(statsView);

        // ═══ زر توليد ═══
        Button btnGen = UiHelper.primaryButton(this, "➕ توليد أكواد جديدة");
        btnGen.setOnClickListener(v -> showGenerateDialog());
        root.addView(btnGen);

        // ═══ زر حذف المستعملة ═══
        Button btnClean = UiHelper.secondaryButton(this, "🗑️ حذف الأكواد المستعملة");
        btnClean.setOnClickListener(v -> confirmClean());
        root.addView(btnClean);

        // ═══ قائمة الأكواد ═══
        TextView listTitle = new TextView(this);
        listTitle.setText("📋 الأكواد الحالية");
        listTitle.setTextColor(Color.parseColor("#D4AF37"));
        listTitle.setTextSize(18);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setGravity(Gravity.CENTER);
        listTitle.setPadding(0, 30, 0, 15);
        root.addView(listTitle);

        codesContainer = new LinearLayout(this);
        codesContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(codesContainer);

        setContentView(scroll);

        // ═══ الاستماع ═══
        codesReg = acm.listenCodes(codes -> renderCodes(codes));
    }

    // ═══════════════════════════════════════════
    //  عرض الأكواد
    // ═══════════════════════════════════════════
    private void renderCodes(List<AdminCodeManager.RedeemCode> codes) {
        codesContainer.removeAllViews();

        int total = codes.size();
        int used = 0;
        int active = 0;
        int expired = 0;

        for (AdminCodeManager.RedeemCode c : codes) {
            if (c.used) used++;
            else if (c.isExpired()) expired++;
            else active++;
        }

        statsView.setText("📊 الإجمالي: " + total +
                "  |  ✅ نشط: " + active +
                "  |  ✔️ مستعمل: " + used +
                "  |  ⏰ منتهي: " + expired);

        if (codes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("ماكانش أكواد بعد");
            empty.setTextColor(Color.parseColor("#9E9E9E"));
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 40, 0, 0);
            codesContainer.addView(empty);
            return;
        }

        for (AdminCodeManager.RedeemCode c : codes) {
            addCodeCard(c);
        }
    }

    private void addCodeCard(final AdminCodeManager.RedeemCode c) {
        LinearLayout card = UiHelper.card(this);

        // ═══ الصف الأول: الكود + الحالة ═══
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvCode = new TextView(this);
        tvCode.setText(c.code);
        tvCode.setTextColor(Color.parseColor("#D4AF37"));
        tvCode.setTextSize(16);
        tvCode.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams codeLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvCode.setLayoutParams(codeLp);
        row1.addView(tvCode);

        TextView tvStatus = new TextView(this);
        if (c.used) {
            tvStatus.setText("✔️ مستعمل");
            tvStatus.setTextColor(Color.parseColor("#9E9E9E"));
        } else if (c.isExpired()) {
            tvStatus.setText("⏰ منتهي");
            tvStatus.setTextColor(Color.parseColor("#F44336"));
        } else {
            tvStatus.setText("✅ نشط");
            tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        }
        tvStatus.setTextSize(12);
        row1.addView(tvStatus);

        card.addView(row1);

        // ═══ التفاصيل ═══
        TextView details = new TextView(this);
        if ("coins".equals(c.type)) {
            details.setText("💰 " + c.amount + " Đ");
        } else {
            details.setText("🎁 " + (c.giftEmoji != null ? c.giftEmoji : "") +
                    " " + (c.giftName != null ? c.giftName : "هدية"));
        }
        details.setTextColor(Color.WHITE);
        details.setTextSize(13);
        details.setPadding(0, 10, 0, 0);
        card.addView(details);

        if (c.note != null && !c.note.isEmpty()) {
            TextView note = new TextView(this);
            note.setText("📝 " + c.note);
            note.setTextColor(Color.parseColor("#9E9E9E"));
            note.setTextSize(11);
            note.setPadding(0, 6, 0, 0);
            card.addView(note);
        }

        // التاريخ
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        TextView date = new TextView(this);
        String dateStr = "📅 " + sdf.format(new Date(c.createdAt));
        if (c.used && c.usedAt > 0) {
            dateStr += "\n👤 استعمله: " + (c.usedBy != null ? c.usedBy : "?");
        }
        date.setText(dateStr);
        date.setTextColor(Color.parseColor("#666666"));
        date.setTextSize(10);
        date.setPadding(0, 8, 0, 0);
        card.addView(date);

        // ═══ الأزرار ═══
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 15, 0, 0);

        Button btnCopy = new Button(this);
        btnCopy.setText("📋 نسخ");
        btnCopy.setTextSize(12);
        btnCopy.setAllCaps(false);
        btnCopy.setTextColor(Color.parseColor("#D4AF37"));
        btnCopy.setBackgroundResource(R.drawable.bg_btn_outline);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnLp.setMargins(4, 0, 4, 0);
        btnCopy.setLayoutParams(btnLp);
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("code", c.code));
            toast("✅ تم نسخ: " + c.code);
        });
        btnRow.addView(btnCopy);

        Button btnDel = new Button(this);
        btnDel.setText("🗑️");
        btnDel.setTextSize(12);
        btnDel.setAllCaps(false);
        btnDel.setTextColor(Color.parseColor("#F44336"));
        btnDel.setBackgroundResource(R.drawable.bg_btn_outline);
        btnDel.setLayoutParams(btnLp);
        btnDel.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("حذف الكود؟")
                .setMessage(c.code)
                .setPositiveButton("🗑️ حذف", (d, w) -> {
                    acm.deleteCode(c.code, new AdminCodeManager.OnDone() {
                        @Override public void onSuccess() { toast("✅ تم الحذف"); }
                        @Override public void onError(String msg) { toast("❌ " + msg); }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
        });
        btnRow.addView(btnDel);

        card.addView(btnRow);
        codesContainer.addView(card);
    }

    // ═══════════════════════════════════════════
    //  Dialog التوليد
    // ═══════════════════════════════════════════
    private void showGenerateDialog() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(40, 30, 40, 30);

        // ═══ النوع ═══
        TextView typeLbl = new TextView(this);
        typeLbl.setText("🎁 نوع الكود:");
        typeLbl.setTextColor(Color.WHITE);
        typeLbl.setTextSize(14);
        typeLbl.setTypeface(null, Typeface.BOLD);
        box.addView(typeLbl);

        final android.widget.RadioGroup typeGroup = new android.widget.RadioGroup(this);
        typeGroup.setOrientation(android.widget.RadioGroup.HORIZONTAL);
        typeGroup.setPadding(0, 10, 0, 15);

        android.widget.RadioButton rbCoins = new android.widget.RadioButton(this);
        rbCoins.setText("💰 عملات");
        rbCoins.setTextColor(Color.WHITE);
        rbCoins.setId(1);
        rbCoins.setChecked(true);
        typeGroup.addView(rbCoins);

        android.widget.RadioButton rbGift = new android.widget.RadioButton(this);
        rbGift.setText("🎁 هدية");
        rbGift.setTextColor(Color.WHITE);
        rbGift.setId(2);
        typeGroup.addView(rbGift);

        box.addView(typeGroup);

        // ═══ العدد ═══
        TextView countLbl = new TextView(this);
        countLbl.setText("🔢 عدد الأكواد:");
        countLbl.setTextColor(Color.WHITE);
        countLbl.setTextSize(14);
        countLbl.setTypeface(null, Typeface.BOLD);
        box.addView(countLbl);

        final EditText etCount = new EditText(this);
        etCount.setHint("مثال: 10");
        etCount.setText("10");
        etCount.setTextColor(Color.WHITE);
        etCount.setHintTextColor(Color.parseColor("#666666"));
        etCount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etCount.setPadding(20, 20, 20, 20);
        etCount.setBackgroundResource(R.drawable.bg_input);
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        etLp.setMargins(0, 10, 0, 15);
        etCount.setLayoutParams(etLp);
        box.addView(etCount);

        // ═══ المبلغ ═══
        TextView amtLbl = new TextView(this);
        amtLbl.setText("💰 المبلغ لكل كود (Đ):");
        amtLbl.setTextColor(Color.WHITE);
        amtLbl.setTextSize(14);
        amtLbl.setTypeface(null, Typeface.BOLD);
        box.addView(amtLbl);

        final EditText etAmt = new EditText(this);
        etAmt.setHint("مثال: 100");
        etAmt.setText("100");
        etAmt.setTextColor(Color.WHITE);
        etAmt.setHintTextColor(Color.parseColor("#666666"));
        etAmt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etAmt.setPadding(20, 20, 20, 20);
        etAmt.setBackgroundResource(R.drawable.bg_input);
        etAmt.setLayoutParams(etLp);
        box.addView(etAmt);

        // ═══ السبب ═══
        TextView noteLbl = new TextView(this);
        noteLbl.setText("📝 السبب:");
        noteLbl.setTextColor(Color.WHITE);
        noteLbl.setTextSize(14);
        noteLbl.setTypeface(null, Typeface.BOLD);
        box.addView(noteLbl);

        final EditText etNote = new EditText(this);
        etNote.setHint("حدث خاص");
        etNote.setTextColor(Color.WHITE);
        etNote.setHintTextColor(Color.parseColor("#666666"));
        etNote.setPadding(20, 20, 20, 20);
        etNote.setBackgroundResource(R.drawable.bg_input);
        etNote.setLayoutParams(etLp);
        box.addView(etNote);

        // ═══ الصلاحية ═══
        TextView expLbl = new TextView(this);
        expLbl.setText("⏰ الصلاحية (أيام) — 0 = بلا نهاية:");
        expLbl.setTextColor(Color.WHITE);
        expLbl.setTextSize(14);
        expLbl.setTypeface(null, Typeface.BOLD);
        box.addView(expLbl);

        final EditText etExp = new EditText(this);
        etExp.setHint("30");
        etExp.setText("30");
        etExp.setTextColor(Color.WHITE);
        etExp.setHintTextColor(Color.parseColor("#666666"));
        etExp.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etExp.setPadding(20, 20, 20, 20);
        etExp.setBackgroundResource(R.drawable.bg_input);
        etExp.setLayoutParams(etLp);
        box.addView(etExp);

        scroll.addView(box);

        new AlertDialog.Builder(this)
            .setTitle("🎫 توليد أكواد جديدة")
            .setView(scroll)
            .setPositiveButton("✅ توليد", (d, w) -> {
                try {
                    int count = Integer.parseInt(etCount.getText().toString().trim());
                    int amount = Integer.parseInt(etAmt.getText().toString().trim());
                    int expDays = Integer.parseInt(etExp.getText().toString().trim());
                    String note = etNote.getText().toString().trim();
                    if (note.isEmpty()) note = "كود خاص";

                    if (count <= 0 || count > 1000) {
                        toast("⚠️ العدد: 1-1000");
                        return;
                    }
                    if (amount <= 0) {
                        toast("⚠️ المبلغ لازم > 0");
                        return;
                    }

                    final int finalCount = count;
                    acm.generateCodes(count, "coins", amount,
                            "", "", expDays, note,
                            new AdminCodeManager.OnDone() {
                        @Override public void onSuccess() {
                            toast("✅ تم توليد " + finalCount + " كود!");
                        }
                        @Override public void onError(String msg) {
                            toast("❌ " + msg);
                        }
                    });
                } catch (Exception e) {
                    toast("❌ قيم غير صحيحة");
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    // ═══════════════════════════════════════════
    //  حذف المستعملة
    // ═══════════════════════════════════════════
    private void confirmClean() {
        new AlertDialog.Builder(this)
            .setTitle("🗑️ حذف المستعملة؟")
            .setMessage("رايح نحذف كل الأكواد المستعملة.")
            .setPositiveButton("🗑️ حذف", (d, w) -> {
                acm.deleteUsedCodes(new AdminCodeManager.OnDone() {
                    @Override public void onSuccess() { toast("✅ تم الحذف"); }
                    @Override public void onError(String msg) { toast("❌ " + msg); }
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (codesReg != null) codesReg.remove();
    }
}
