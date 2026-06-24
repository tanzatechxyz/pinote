package com.topenclaw.pinote;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class NoteStore {
    private static final String PREFS = "notes";
    private static final String IDS = "ids";
    private static final String NEXT = "next";
    private static final String NOTE = "note_";

    private NoteStore() {}

    static long add(Context context, String text) {
        SharedPreferences prefs = prefs(context);
        long id = prefs.getLong(NEXT, 1);
        Set<String> ids = ids(prefs);
        ids.add(Long.toString(id));
        prefs.edit().putLong(NEXT, id + 1).putString(NOTE + id, text).putStringSet(IDS, ids).apply();
        return id;
    }

    static void remove(Context context, long id) {
        SharedPreferences prefs = prefs(context);
        Set<String> ids = ids(prefs);
        ids.remove(Long.toString(id));
        prefs.edit().remove(NOTE + id).putStringSet(IDS, ids).apply();
    }

    static String get(Context context, long id) {
        return prefs(context).getString(NOTE + id, null);
    }

    static Map<Long, String> all(Context context) {
        SharedPreferences prefs = prefs(context);
        Map<Long, String> notes = new HashMap<>();
        for (String raw : ids(prefs)) {
            long id = Long.parseLong(raw);
            String text = prefs.getString(NOTE + id, null);
            if (text != null) notes.put(id, text);
        }
        return notes;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static Set<String> ids(SharedPreferences prefs) {
        return new HashSet<>(prefs.getStringSet(IDS, new HashSet<>()));
    }
}
