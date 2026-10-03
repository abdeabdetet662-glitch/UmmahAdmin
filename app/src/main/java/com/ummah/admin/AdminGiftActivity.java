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

/**
 * AdminGiftActivity — إرسال هدايا (فردي / جماعي)
 */
public class AdminGiftActivity extends Activity {

    private AdminCodeManager acm;
    private RadioGroup modeGroup;
    private EditText etNationalId;
    private EditText etAmount;
    private EditText etNote;
    private LinearLayout targetBox;
    private TextView header;

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
        icon.setText("🎁");
        icon.setTextSize(60);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        header = UiHelper.goldTitle(this, "إرسال هدايا", 26);
        root.addView(header);

        TextView sub = new TextView(this);
        sub.setText("أرسل عملات لكل المواطنين أو لمستخدم محدد");
        sub.setTextColor(Color.parseColor("#9E9E9E"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 30);
        root.addView(sub);

        // ═══ اختيار النوع ═══
        LinearLayout typeCard = UiHelper.card(this);

        TextView typeTitle = new TextView(this);
        typeTitle.setText("📋 نوع الإرسال");
        typeTitle.setTextColor(Color.parseColor("#D4AF37"));
        typeTitle.setTextSize(15);
        typeTitle.setTypeface(null, Typeface.BOLD);
        typeCard.addView(typeTitle);

        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.VERTICAL);
        modeGroup.setPadding(0, 15, 0, 0);

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

        typeCard.addView(modeGroup);
        root.addView(typeCard);

        // ═══ المُستهدف ═══
        targetBox = UiHelper.card(this);
        targetBox.setVisibility(View.GONE); // مخفي افتراضياً

        TextView targetTitle = new TextView(this);
        targetTitle.setText("🆔 رقم المواطن");
        targetTitle.setTextColor(Color.parseColor("#D4AF37"));
        targetTitle.setTextSize(15);
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

        root.addView(targetBox);

        // ═══ المبلغ ═══
        LinearLayout amountCard = UiHelper.card(this);

        TextView amountTitle = new TextView(this);
        amountTitle.setText("💰 المبلغ (Đ)");
        amountTitle.setTextColor(Color.parseColor("#D4AF37"));
        amountTitle.setTextSize(15);
        amountTitle.setTypeface(null, Typeface.BOLD);
        amountCard.addView(amountTitle);

        etAmount = new EditText(this);
        etAmount.setHint("مثال: 100");
        etAmount.setTextColor(Color.WHITE);
        etAmount.setHintTextColor(Color.parseColor("#666666"));
        etAmount.setTextSize(18);
        etAmount.setBackgroundResource(R.drawable.bg_input);
        etAmount.setPadding(30, 30, 30, 30);
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etAmount.setSingleLine(true);
        LinearLayout.LayoutParams amLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        amLp.setMargins(0, 15, 0, 0);
        etAmount.setLayoutParams(amLp);
        amountCard.addView(etAmount);

        // أزرار سريعة
        LinearLayout quickRow = new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        quickRow.setPadding(0, 15, 0, 0);

        int[] amounts = {50, 100, 500, 1000};
        for (int amt : amounts) {
            final int val = amt;
            Button b = new Button(this);
            b.setText(String.valueOf(amt));
            b.setTextSize(13);
            b.setAllCaps(false);
            b.setTextColor(Color.parseColor("#D4AF37"));
            b.setBackgroundResource(R.drawable.bg_btn_outline);
            LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            qLp.setMargins(4, 0, 4, 0);
            b.setLayoutParams(qLp);
            b.setOnClickListener(v -> etAmount.setText(String.valueOf(val)));
            quickRow.addView(b);
        }
        amountCard.addView(quickRow);
        root.addView(amountCard);

        // ═══ الملاحظة ═══
        LinearLayout noteCard = UiHelper.card(this);

        TextView noteTitle = new TextView(this);
        noteTitle.setText("📝 السبب (اختياري)");
        noteTitle.setTextColor(Color.parseColor("#D4AF37"));
        noteTitle.setTextSize(15);
        noteTitle.setTypeface(null, Typeface.BOLD);
        noteCard.addView(noteTitle);

        etNote = new EditText(this);
        etNote.setHint("مثال: حدث خاص، بمناسبة العيد...");
        etNote.setTextColor(Color.WHITE);
        etNote.setHintTextColor(Color.parseColor("#666666"));
        etNote.setTextSize(14);
        etNote.setBackgroundResource(R.drawable.bg_input);
        etNote.setPadding(30, 30, 30, 30);
        etNote.setSingleLine(true);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        noteLp.setMargins(0, 15, 0, 0);
        etNote.setLayoutParams(noteLp);
        noteCard.addView(etNote);

        root.addView(noteCard);

        // ═══ زر الإرسال ═══
        Button btnSend = UiHelper.primaryButton(this, "🚀 إرسال");
        btnSend.setOnClickListener(v -> sendGift());
        root.addView(btnSend);

        // ═══ نصيحة ═══
        TextView hint = new TextView(this);
        hint.setText("⚠️ هذي العملية تُسجل في سجل الإدارة");
        hint.setTextColor(Color.parseColor("#9E9E9E"));
        hint.setTextSize(11);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 20, 0, 0);
        root.addView(hint);

        setContentView(scroll);

        // ═══ ربط RadioGroup ═══
        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == 2) {
                targetBox.setVisibility(View.VISIBLE);
            } else {
                targetBox.setVisibility(View.GONE);
            }
        });
    }

    // ═══════════════════════════════════════════
    //  إرسال
    // ═══════════════════════════════════════════
    private void sendGift() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            toast("⚠️ اكتب المبلغ");
            return;
        }

        final int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (Exception e) {
            toast("❌ مبلغ غير صحيح");
            return;
        }

        if (amount <= 0) {
            toast("⚠️ المبلغ لازم > 0");
            return;
        }

        String note = etNote.getText().toString().trim();
        if (note.isEmpty()) note = "هدية من الإدارة";

        int checked = modeGroup.getCheckedRadioButtonId();
        final String finalNote = note;

        if (checked == 2) {
            // ═══ فردي ═══
            String id = etNationalId.getText().toString().trim();
            if (id.isEmpty()) {
                toast("⚠️ اكتب الرقم الوطني");
                return;
            }

            // تأكيد
            new AlertDialog.Builder(this)
                .setTitle("🎁 تأكيد الإرسال")
                .setMessage("المُستهدف: " + id +
                        "\n💰 المبلغ: " + amount + " Đ" +
                        "\n📝 السبب: " + finalNote)
                .setPositiveButton("✅ إرسال", (d, w) -> {
                    acm.sendToUser(id, amount, finalNote, new AdminCodeManager.OnDone() {
                        @Override public void onSuccess() {
                            toast("✅ تم إرسال " + amount + " Đ لـ " + id);
                            clearForm();
                        }
                        @Override public void onError(String msg) {
                            toast("❌ " + msg);
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
        } else {
            // ═══ جماعي ═══
            long total = (long) amount * 100; // تقديري
            new AlertDialog.Builder(this)
                .setTitle("🌍 هدية جماعية")
                .setMessage("رايح ترسل " + amount + " Đ لكل مواطن." +
                        "\n\n⚠️ تأكد قبل الإرسال!")
                .setPositiveButton("✅ إرسال للكل", (d, w) -> {
                    acm.sendToAll(amount, finalNote, new AdminCodeManager.OnDone() {
                        @Override public void onSuccess() {
                            toast("✅ تم إرسال " + amount + " Đ لكل المواطنين");
                            clearForm();
                        }
                        @Override public void onError(String msg) {
                            toast("❌ " + msg);
                        }
                    });
                })
                .setNegativeButton("إلغاء", null)
                .show();
        }
    }

    private void clearForm() {
        etAmount.setText("");
        etNote.setText("");
        etNationalId.setText("");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
