package com.topenclaw.pinote;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import java.util.Map;

final class NoteNotifications {
    private static final String CHANNEL = "notes";

    private NoteNotifications() {}

    static void ensureChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Notes", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Pinned notes");
        manager(context).createNotificationChannel(channel);
    }

    static void show(Context context, long id, String text) {
        if (!canPost(context)) return;
        PendingIntent done = receiver(context, id, NoteReceiver.ACTION_DONE);
        PendingIntent restore = receiver(context, id, NoteReceiver.ACTION_RESTORE);
        PendingIntent open = PendingIntent.getActivity(
            context,
            (int) id,
            new Intent(context, MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Notification notification = new Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_note)
            .setContentTitle(context.getString(R.string.note_title))
            .setContentText(text)
            .setStyle(new Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setDeleteIntent(restore)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.drawable.ic_stat_note), context.getString(R.string.done), done).build())
            .build();
        manager(context).notify((int) id, notification);
    }

    static void restoreAll(Context context) {
        for (Map.Entry<Long, String> note : NoteStore.all(context).entrySet()) {
            show(context, note.getKey(), note.getValue());
        }
    }

    static void cancel(Context context, long id) {
        manager(context).cancel((int) id);
    }

    private static PendingIntent receiver(Context context, long id, String action) {
        Intent intent = new Intent(context, NoteReceiver.class).setAction(action).putExtra(NoteReceiver.EXTRA_ID, id);
        return PendingIntent.getBroadcast(context, (int) (id + action.hashCode()), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static boolean canPost(Context context) {
        return Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private static NotificationManager manager(Context context) {
        return context.getSystemService(NotificationManager.class);
    }
}
