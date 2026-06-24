package com.topenclaw.pinote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteReceiver extends BroadcastReceiver {
    static final String EXTRA_ID = "id";
    static final String ACTION_DONE = "com.topenclaw.pinote.DONE";
    static final String ACTION_RESTORE = "com.topenclaw.pinote.RESTORE";

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(EXTRA_ID, 0);
        if (id == 0) return;
        if (ACTION_DONE.equals(intent.getAction())) {
            NoteStore.remove(context, id);
            NoteNotifications.cancel(context, id);
            return;
        }
        String text = NoteStore.get(context, id);
        if (text != null) NoteNotifications.show(context, id, text);
    }
}
