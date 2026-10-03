package com.ummah.admin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * AdminBadgeHelper — Badges للوحة التحكم
 */
public class AdminBadgeHelper {

    /** إنشاء TextView للـ Badge */
    public static TextView createBadge(Context ctx, int count) {
        TextView badge = new TextView(ctx);
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(R.drawable.bg_badge_red);
        badge.setElevation(10f);
        int size = dp(ctx, 20);
        badge.setMinWidth(size);
        badge.setMinHeight(size);
        badge.setPadding(dp(ctx, 6), 0, dp(ctx, 6), 0);
        return badge;
    }

    /** لفّ الـ View مع Badge (للأزرار) */
    public static FrameLayout withBadge(Activity act, View view, int count) {
        FrameLayout wrapper = new FrameLayout(act);
        wrapper.setClipChildren(false);
        wrapper.setClipToPadding(false);

        FrameLayout.LayoutParams vLp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(vLp);
        wrapper.addView(view);

        if (count > 0) {
            TextView badge = createBadge(act, count);
            FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            blp.gravity = Gravity.TOP | Gravity.END;
            blp.setMargins(0, dp(act, 8), dp(act, 20), 0);
            badge.setLayoutParams(blp);
            wrapper.addView(badge);
        }

        LinearLayout.LayoutParams wrapperLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wrapper.setLayoutParams(wrapperLp);
        return wrapper;
    }

    /** تحديث Badge أيقونة التطبيق */
    public static void updateAppBadge(Context ctx, int count) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                android.app.NotificationManager nm =
                    (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null && count > 0) {
                    android.app.Notification.Builder builder =
                        new android.app.Notification.Builder(ctx, "admin_badge")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("لوحة تحكم أُمّة")
                            .setContentText(count + " عنصر جديد")
                            .setNumber(count)
                            .setAutoCancel(true);
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        builder.setChannelId("admin_badge");
                    }
                    nm.notify(888001, builder.build());
                } else if (nm != null) {
                    nm.cancel(888001);
                }
            }
        } catch (Exception ignored) {}
    }

    /** إنشاء قناة Badge */
    public static void createChannels(Context ctx) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                android.app.NotificationManager nm =
                    (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) {
                    android.app.NotificationChannel ch = new android.app.NotificationChannel(
                            "admin_badge", "Badges الإدارة", android.app.NotificationManager.IMPORTANCE_LOW);
                    ch.setShowBadge(true);
                    nm.createNotificationChannel(ch);
                }
            }
        } catch (Exception ignored) {}
    }

    private static int dp(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
}
