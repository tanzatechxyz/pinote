package com.topenclaw.pinote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class PinoteTest {
    private Application context;
    private NotificationManager notifications;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        notes().edit().clear().commit();
        notifications = context.getSystemService(NotificationManager.class);
        notifications.cancelAll();
        NoteNotifications.ensureChannel(context);
    }

    @Test
    public void storeAddsReadsAndRemovesNotes() {
        long first = NoteStore.add(context, "one");
        long second = NoteStore.add(context, "two");

        assertEquals(1L, first);
        assertEquals(2L, second);
        assertEquals("one", NoteStore.get(context, first));
        assertEquals("two", NoteStore.all(context).get(second));

        NoteStore.remove(context, first);

        assertNull(NoteStore.get(context, first));
        assertEquals(1, NoteStore.all(context).size());
    }

    @Test
    public void storeIgnoresCorruptIds() {
        SharedPreferences.Editor editor = notes().edit();
        editor.putStringSet("ids", new HashSet<>(Arrays.asList("1", "bad")));
        editor.putString("note_1", "kept");
        assertTrue(editor.commit());

        Map<Long, String> restored = NoteStore.all(context);

        assertEquals(1, restored.size());
        assertEquals("kept", restored.get(1L));
    }

    @Test
    public void noteReceiverIgnoresUnknownActions() {
        long id = NoteStore.add(context, "restore me");
        NoteReceiver receiver = new NoteReceiver();

        receiver.onReceive(context, new Intent("unexpected").putExtra(NoteReceiver.EXTRA_ID, id));

        assertNull(shadowOf(notifications).getNotification((int) id));

        receiver.onReceive(context, new Intent(NoteReceiver.ACTION_RESTORE).putExtra(NoteReceiver.EXTRA_ID, id));

        assertNotNull(shadowOf(notifications).getNotification((int) id));

        receiver.onReceive(context, new Intent(NoteReceiver.ACTION_DONE).putExtra(NoteReceiver.EXTRA_ID, id));

        assertNull(NoteStore.get(context, id));
        assertNull(shadowOf(notifications).getNotification((int) id));
    }

    @Test
    public void bootReceiverIgnoresUnknownActions() {
        long id = NoteStore.add(context, "boot note");
        BootReceiver receiver = new BootReceiver();

        receiver.onReceive(context, new Intent("unexpected"));

        assertNull(shadowOf(notifications).getNotification((int) id));

        receiver.onReceive(context, new Intent(Intent.ACTION_BOOT_COMPLETED));

        assertNotNull(shadowOf(notifications).getNotification((int) id));
    }

    @Test
    @Config(sdk = 33)
    public void showReturnsFalseWithoutNotificationPermission() {
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS);

        assertFalse(NoteNotifications.show(context, 1L, "blocked"));
        assertNull(shadowOf(notifications).getNotification(1));
    }

    private SharedPreferences notes() {
        return context.getSharedPreferences("notes", Context.MODE_PRIVATE);
    }
}
