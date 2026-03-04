package com.codedroidx.editor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import io.github.rosemoe.sora.widget.CodeEditor;

public class MainActivity extends AppCompatActivity {

    private CodeEditor editor;
    private Uri currentFile;
    private String currentName = "Untitled.txt";
    private String currentExt  = "txt";

    private TextView tvTitle, tvLang;

    private final ActivityResultLauncher<Intent> openLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        currentFile = uri;
                        currentName = queryName(uri);
                        currentExt  = Ext.ext(currentName);
                        applyModeByExt(currentExt);
                        loadFile(uri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> saveAsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        currentFile = uri;
                        currentName = queryName(uri);
                        currentExt  = Ext.ext(currentName);
                        applyModeByExt(currentExt);
                        saveFile(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editor  = findViewById(R.id.editor);
        tvTitle = findViewById(R.id.tvTitle);
        tvLang  = findViewById(R.id.tvLang);

        editor.setText("// CodeDroidX generated\n");

        findViewById(R.id.btnOpen).setOnClickListener(v -> openFile());
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (currentFile == null) saveAs();
            else saveFile(currentFile);
        });

        findViewById(R.id.btnJson).setOnClickListener(v -> {
            if (!"json".equals(currentExt)) {
                toast("Open a .json file first");
                return;
            }
            String text = editor.getText().toString();
            try {
                JsonTools.validate(text);
                String pretty = JsonTools.pretty(text);
                editor.setText(pretty);
                toast("JSON formatted ✅");
            } catch (Exception e) {
                toast("JSON invalid ❌ " + e.getMessage());
            }
        });

        applyModeByExt(currentExt);
        refreshTitle();
    }

    private void applyModeByExt(String ext) {
        // “Highlight” mode selector (safe build)
        // Kamu bisa upgrade ke TextMate nanti tanpa ubah arsitektur.
        String tag = "TEXT";

        switch (ext) {
            case "lua":
            case "luau":
                tag = "LUA";
                break;
            case "json":
                tag = "JSON";
                break;
            case "py":
                tag = "PY";
                break;
            case "java":
                tag = "JAVA";
                break;
            case "xml":
                tag = "XML";
                break;
            case "html":
            case "htm":
                tag = "HTML";
                break;
            case "js":
                tag = "JS";
                break;
            default:
                tag = ext.isEmpty() ? "TEXT" : ext.toUpperCase();
                break;
        }

        tvLang.setText(tag);
        refreshTitle();
    }

    private void refreshTitle() {
        tvTitle.setText(currentName == null ? "CodeDroidX" : currentName);
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        openLauncher.launch(intent);
    }

    private void saveAs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, currentName == null ? "code.txt" : currentName);
        saveAsLauncher.launch(intent);
    }

    private void loadFile(Uri uri) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            editor.setText(sb.toString());
            toast("Opened");
        } catch (Exception e) {
            toast("Open failed: " + e.getMessage());
        }
    }

    private void saveFile(Uri uri) {
        try (OutputStream os = getContentResolver().openOutputStream(uri, "wt")) {
            if (os == null) {
                toast("Save failed: stream null");
                return;
            }
            os.write(editor.getText().toString().getBytes());
            os.flush();
            toast("Saved");
        } catch (Exception e) {
            toast("Save failed: " + e.getMessage());
        }
    }

    private String queryName(Uri uri) {
        try (android.database.Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return "file";
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
