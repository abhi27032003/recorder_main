package com.example.recorderchunks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import nl.bryanderidder.themedtogglebuttongroup.ThemedButton;
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup;

public class API_Updation extends AppCompatActivity {

    private EditText etChatGptApi, etGeminiApi, etPrompt;
    private Button btnSave, btnUpdate, manage_prompt;
    private ThemedToggleButtonGroup toggleGroup;

    private static final String PREF_NAME = "ApiKeysPref";
    public static final String KEY_CHATGPT = "ChatGptApiKey";
    public static final String KEY_GEMINI = "GeminiApiKey";
    public static final String KEY_PROMPT = "Prompt";
    public static final String KEY_SELECTED_API = "SelectedApi";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_updation);
        Toolbar toolbar = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Initialize UI components
        etChatGptApi = findViewById(R.id.et_chatgpt_api);
        etGeminiApi = findViewById(R.id.et_gemini_api);
        etPrompt = findViewById(R.id.et_prompt);
        btnSave = findViewById(R.id.btn_save);
        btnUpdate = findViewById(R.id.btn_update);
        toggleGroup = findViewById(R.id.time);
        manage_prompt=findViewById(R.id.manage_prompt);
        manage_prompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i=new Intent(API_Updation.this, Manage_Prompt.class);
                startActivity(i);
            }
        });

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load saved data if available
        loadSavedData();

        // Save button functionality
        btnSave.setOnClickListener(v -> saveData());

        // Update button functionality
        btnUpdate.setOnClickListener(v -> updateData());

        // Handle toggle button selection
        toggleGroup.setOnSelectListener(selectedButton -> {
            String selectedApi = ((ThemedButton) selectedButton).getText();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SELECTED_API, selectedApi);
            editor.apply();
            //  Toast.makeText(this, "Selected API: " + selectedApi, Toast.LENGTH_SHORT).show();
            return null;
        });
    }

    private void loadSavedData() {
        String chatGptApi = sharedPreferences.getString(KEY_CHATGPT, "");
        String geminiApi = sharedPreferences.getString(KEY_GEMINI, "");
        String prompt = sharedPreferences.getString(KEY_PROMPT, "");
        String selectedApi = sharedPreferences.getString(KEY_SELECTED_API, "Use ChatGPT"); // Default to "Use ChatGPT"

        if (!TextUtils.isEmpty(chatGptApi)) etChatGptApi.setText(chatGptApi);
        if (!TextUtils.isEmpty(geminiApi)) etGeminiApi.setText(geminiApi);
        if (!TextUtils.isEmpty(prompt)) etPrompt.setText(prompt);

        // Set the toggle button state based on the saved API selection
        if ("Use ChatGPT".equalsIgnoreCase(selectedApi)) {
            toggleGroup.selectButton(R.id.btn1); // Assuming btn1 is for ChatGPT
        } else if ("Use Gemini AI".equalsIgnoreCase(selectedApi)) {
            toggleGroup.selectButton(R.id.btn2); // Assuming btn2 is for Gemini AI
        }

       // Toast.makeText(this, "Loaded saved data", Toast.LENGTH_SHORT).show();
    }

    private void saveData() {
        String chatGptApi = etChatGptApi.getText().toString().trim();
        String geminiApi = etGeminiApi.getText().toString().trim();
        String prompt = etPrompt.getText().toString().trim();
       // String selected_api= etPrompt.getText().toString().trim();

        if (TextUtils.isEmpty(chatGptApi) || TextUtils.isEmpty(geminiApi) || TextUtils.isEmpty(prompt)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CHATGPT, chatGptApi);
        editor.putString(KEY_GEMINI, geminiApi);
        editor.putString(KEY_PROMPT, prompt);
        editor.apply();

        Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateData() {
        saveData();
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
