package com.topenclaw.pinote;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION = 1;
    private static final String STATE_PENDING_NOTE = "pending_note";
    private EditText note;
    private String pendingNote;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        NoteNotifications.ensureChannel(this);
        NoteNotifications.restoreAll(this);
        if (state != null) pendingNote = state.getString(STATE_PENDING_NOTE);

        note = new EditText(this);
        note.setId(R.id.note_input);
        note.setHint(R.string.note_hint);
        note.setGravity(Gravity.TOP | Gravity.START);
        note.setMinLines(8);
        note.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        Button done = new Button(this);
        done.setId(R.id.done_button);
        done.setText(R.string.done);
        done.setOnClickListener(v -> save());

        int pad = dp(16);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(R.color.pinote_bg));
        root.setPadding(pad, pad, pad, pad);
        note.setBackgroundResource(R.drawable.note_box);
        note.setHintTextColor(getColor(R.color.pinote_hint));
        note.setPadding(pad, pad, pad, pad);
        note.setTextColor(getColor(R.color.pinote_text));
        done.setBackgroundResource(R.drawable.done_button);
        done.setTextColor(getColor(R.color.pinote_button_text));
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        noteParams.bottomMargin = pad;
        root.addView(note, noteParams);
        root.addView(done, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        note.requestFocus();
        note.postDelayed(() -> {
            InputMethodManager input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (input != null) input.showSoftInput(note, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingNote != null) outState.putString(STATE_PENDING_NOTE, pendingNote);
    }

    private void save() {
        String text = note.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.empty_note, Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingNote = text;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
            return;
        }
        post(text);
    }

    private void post(String text) {
        long id = NoteStore.add(this, text);
        if (!NoteNotifications.show(this, id, text)) {
            NoteStore.remove(this, id);
            pendingNote = null;
            Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_SHORT).show();
            return;
        }
        pendingNote = null;
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != NOTIFICATION_PERMISSION) return;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && pendingNote != null) {
            String text = pendingNote;
            pendingNote = null;
            post(text);
            return;
        }
        pendingNote = null;
        Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
