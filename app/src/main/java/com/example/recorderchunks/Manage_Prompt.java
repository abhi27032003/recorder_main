package com.example.recorderchunks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Manage_Prompt extends AppCompatActivity {

    TextView Prompt_Title , Prompt_text;
    ImageView copy, share,edit,show_dick,delete,save;
    Boolean visible=false;
 ///coomit
    private static final String SHARED_PREF_NAME = "ApiKeysPref";
    private static final String KEY_PROMPT_TITLE = "promptTitle";
    private static final String KEY_PROMPT_TEXT = "promptText";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_prompt);

        Toolbar toolbar = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Prompt_text=findViewById(R.id.prompt_text);
        Prompt_Title=findViewById(R.id.title_prompt);

        copy=findViewById(R.id.copy_p);
        share=findViewById(R.id.share_p);
        edit=findViewById(R.id.edit_p);
        show_dick=findViewById(R.id.show_dick);
        delete=findViewById(R.id.delete_p);
        save=findViewById(R.id.save_p);
        fetchAndSetText();
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prompt_text.setVisibility(View.VISIBLE);
                visible=true;
                showPromptDialog();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTexts();
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteValues();

            }
        });
        show_dick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visible)
                {
                    Prompt_text.setVisibility(View.GONE);
                    visible=false;
                }
                else {
                    Prompt_text.setVisibility(View.VISIBLE);
                    visible=true;

                }
            }
        });

    }
    private void showPromptDialog() {
        // Create a LinearLayout to hold input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Create EditTexts for input
        EditText titleInput = new EditText(this);
        titleInput.setHint("Enter prompt title");
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(titleInput);

        EditText textInput = new EditText(this);
        textInput.setHint("Enter prompt text");
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(textInput);

        // Fetch previously saved data
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedTitle = sharedPreferences.getString(KEY_PROMPT_TITLE, "");
        String savedText = sharedPreferences.getString(KEY_PROMPT_TEXT, "");
        if (!savedTitle.isEmpty() || !savedText.isEmpty()) {
            titleInput.setText(savedTitle);
            textInput.setText(savedText);
        }

        // Create and show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Enter Prompt Details")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save to SharedPreferences
                    String title = titleInput.getText().toString().trim();
                    String text = textInput.getText().toString().trim();

                    if (!title.isEmpty() && !text.isEmpty()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_PROMPT_TITLE, title);
                        editor.putString(KEY_PROMPT_TEXT, text);
                        Prompt_text.setVisibility(View.VISIBLE);
                        Prompt_Title.setText(title);
                        Prompt_text.setText(text);
                        editor.apply();

                        Toast.makeText(Manage_Prompt.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Manage_Prompt.this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void fetchAndSetText() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedTitle = sharedPreferences.getString(KEY_PROMPT_TITLE, "No Title Saved");
        String savedText = sharedPreferences.getString(KEY_PROMPT_TEXT, "No Text Saved");

        Prompt_Title.setText(savedTitle);
        Prompt_text.setText(savedText);
    }
    private void shareTexts() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedTitle = sharedPreferences.getString(KEY_PROMPT_TITLE, "No Title Saved");
        String savedText = sharedPreferences.getString(KEY_PROMPT_TEXT, "No Text Saved");

        if (savedTitle.equals("No Title Saved") && savedText.equals("No Text Saved")) {
            Toast.makeText(this, "Nothing to share. Please add texts first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareContent = "Title: " + savedTitle + "\nText: " + savedText;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
    private void copyToClipboard() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedTitle = sharedPreferences.getString(KEY_PROMPT_TITLE, "No Title Saved");
        String savedText = sharedPreferences.getString(KEY_PROMPT_TEXT, "No Text Saved");

        if (savedTitle.equals("No Title Saved") && savedText.equals("No Text Saved")) {
            Toast.makeText(this, "Nothing to copy. Please add texts first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String copyContent = "Title: " + savedTitle + "\nText: " + savedText;

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Prompt Details", copyContent);
        clipboardManager.setPrimaryClip(clip);

        Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }private void deleteValues() {
        // Create an AlertDialog to confirm deletion
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete the saved values?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Perform deletion
                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if (sharedPreferences.contains(KEY_PROMPT_TITLE) || sharedPreferences.contains(KEY_PROMPT_TEXT)) {
                        editor.remove(KEY_PROMPT_TITLE);
                        editor.remove(KEY_PROMPT_TEXT);
                        editor.apply();

                        // Clear the TextViews
                        Prompt_Title.setText("");
                        Prompt_text.setText("");

                        Toast.makeText(this, "Values deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No values to delete!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}