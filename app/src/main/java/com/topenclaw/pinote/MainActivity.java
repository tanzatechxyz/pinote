package com.topenclaw.pinote;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int NOTIFICATION_PERMISSION = 1;
    private EditText note;
    private String pendingNote;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        NoteNotifications.ensureChannel(this);
        NoteNotifications.restoreAll(this);

        note = new EditText(this);
        note.setHint(R.string.note_hint);
        note.setGravity(Gravity.TOP | Gravity.START);
        note.setMinLines(8);
        note.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        Button done = new Button(this);
        done.setText(R.string.done);
        done.setOnClickListener(v -> save());

        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.addView(note, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(done, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        note.requestFocus();
        note.postDelayed(() -> ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(note, InputMethodManager.SHOW_IMPLICIT), 200);
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
        NoteNotifications.show(this, id, text);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == NOTIFICATION_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && pendingNote != null) {
            post(pendingNote);
        } else if (requestCode == NOTIFICATION_PERMISSION) {
            Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_SHORT).show();
        }
    }
}
