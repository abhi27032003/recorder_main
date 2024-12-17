package com.example.recorderchunks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.recorderchunks.Adapter.PromptAdapter;
import com.example.recorderchunks.Model_Class.Prompt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Manage_Prompt extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PromptAdapter adapter;
    private Prompt_Database_Helper databaseHelper;
    private ImageView add_prompt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_prompt);

        Toolbar toolbar = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        add_prompt=findViewById(R.id.add_prompt);
        recyclerView = findViewById(R.id.Prompt_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new Prompt_Database_Helper(this);
        List<Prompt> promptsList = databaseHelper.getAllPrompts();
        adapter = new PromptAdapter(this, promptsList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        add_prompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPromptDialog();
            }
        });



    }
    private void showPromptDialog() {
        // Create a LinearLayout to hold input fields
        LinearLayout layout = new LinearLayout(Manage_Prompt.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 5, 5, 5);

        // Create EditTexts for input
        EditText titleInput = new EditText(Manage_Prompt.this);
        titleInput.setHint("Enter prompt title");
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(titleInput);

        EditText textInput = new EditText(Manage_Prompt.this);
        textInput.setHint("Enter prompt text");
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(textInput);

        // Fetch previously saved data


        // Create and show the dialog
        new AlertDialog.Builder(Manage_Prompt.this)
                .setTitle("Enter Prompt Details")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save to SharedPreferences
                    String title = titleInput.getText().toString().trim();
                    String text = textInput.getText().toString().trim();

                    if (!title.isEmpty() && !text.isEmpty()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy 'at' HH:mm");
                        String date= sdf.format(new Date());
                        Prompt_Database_Helper pdh=new Prompt_Database_Helper(Manage_Prompt.this);
                        pdh.addPrompt(title,text,date);
                        List<Prompt> promptsList = databaseHelper.getAllPrompts();
                        adapter = new PromptAdapter(this, promptsList);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();


                        Toast.makeText(Manage_Prompt.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Manage_Prompt.this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
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