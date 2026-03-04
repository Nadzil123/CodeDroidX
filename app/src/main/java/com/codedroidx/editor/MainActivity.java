package com.codedroidx.editor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private final ActivityResultLauncher<Intent> openLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        currentFile = uri;
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
                        saveFile(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editor = findViewById(R.id.editor);
        editor.setText("// CodeDroidX ready!\n");

        TextView btnOpen = findViewById(R.id.btnOpen);
        TextView btnSave = findViewById(R.id.btnSave);

        btnOpen.setOnClickListener(v -> openFile());
        btnSave.setOnClickListener(v -> {
            if (currentFile == null) saveAs();
            else saveFile(currentFile);
        });
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
        intent.putExtra(Intent.EXTRA_TITLE, "code.txt");
        saveAsLauncher.launch(intent);
    }

    private void loadFile(Uri uri) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
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

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
