package com.example.recorderchunks;// MainActivity.java



import static com.example.recorderchunks.API_Updation.KEY_CHATGPT;
import static com.example.recorderchunks.API_Updation.KEY_GEMINI;
import static com.example.recorderchunks.API_Updation.KEY_PROMPT;
import static com.example.recorderchunks.API_Updation.KEY_SELECTED_API;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.arthenica.mobileffmpeg.FFmpeg;

import com.example.recorderchunks.utils.TranscriptionHelper;
import com.example.recorderchunks.utils.ZipUtils;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class Add_Event extends AppCompatActivity {

    private TextView selectedDateTime;
    private EditText eventDescription;

    private  Button datePickerBtn,timePickerBtn,make_note,stop_recording_animation;
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    Button recordButton,Start_Transcriptio;
    private DatabaseHelper databaseHelper;
    /////////////////////////////////Pocket_sphinex
    private TranscriptionHelper transcriptionHelper;
    private  EditText eventdescription_PocketSphinx;

    //////////////////////////////////saving audio//////////////////////////////////////////
    private MediaRecorder mediaRecorder;
    private boolean isRecordingCompleted = false;
    private String audioFilePath;
    private long startTime, stopTime;

    ////////////////////////////////////playing and pausing audio///////////////////////////
    private CardView recordingCard,recording_animation_card;
    private FloatingActionButton playPauseButton;
    private SeekBar playbackProgressBar;
    private TextView playbackTimer;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private  boolean recorder=false;

    //////////////////// vosk model

    private Model voskModel;

    //////////////////////////////for gemini api

    private RequestQueue requestQueue;

    private SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        setupModel();
        initializeVoskModel();
        ////////////////////////
        recordingCard = findViewById(R.id.recordingCard);
        playPauseButton = findViewById(R.id.playPauseButton);
        playbackProgressBar = findViewById(R.id.playbackProgressBar);
        playbackTimer = findViewById(R.id.playbackTimer);
        Start_Transcriptio=findViewById(R.id.Start_Transcription);
        make_note=findViewById(R.id.make_note);
        make_note.setVisibility(View.GONE);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("ApiKeysPref", MODE_PRIVATE);
        String mes = sharedPreferences.getString(KEY_PROMPT, "");
        //Toast.makeText(this,"the prompt is : "+ mes, Toast.LENGTH_SHORT).show();
        recording_animation_card=findViewById(R.id.recording_card);
        recording_animation_card.setVisibility(View.GONE);
        stop_recording_animation=findViewById(R.id.stop_recording_animation);
        stop_recording_animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder=false;
                recordButton.setText("Start Recording");
                stopRecording();
            }
        });


        Start_Transcriptio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizeSpeech(convertToWav(audioFilePath));

            }
        });
        //////////////////////// f389or pocket sphinex model
        eventdescription_PocketSphinx=findViewById(R.id.eventdiscription_pocketsphinex);
        ////////////////////////
        datePickerBtn = findViewById(R.id.datePickerBtn);
        timePickerBtn = findViewById(R.id.timePickerBtn);
        recordButton = findViewById(R.id.recordButton);
        selectedDateTime = findViewById(R.id.selectedDateTime);
        eventDescription = findViewById(R.id.eventDescription);
        // Set up the toolbar as the app bar
        Toolbar toolbar = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Button saveEventButton = findViewById(R.id.saveEventButton);
        saveEventButton.setOnClickListener(view -> {
            isRecordingCompleted=true;
            // Get the event title, description, selected date, and selected time
            if (isRecordingCompleted) {  // Show only when recording is completed
                String title = ((EditText) findViewById(R.id.eventTitle)).getText().toString();
                String eventdescription = eventDescription.getText().toString();
                String selectedDate = datePickerBtn.getText().toString();
                String selectedTime = timePickerBtn.getText().toString();

                saveEventData(title, eventdescription, selectedDate, selectedTime);
            } else {
                Toast.makeText(this, "Please complete the recording first", Toast.LENGTH_SHORT).show();
            }
        });

        make_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt="";// If the EditText is empty, load the saved prompt from SharedPreferences
                if (TextUtils.isEmpty(prompt)) {
                    prompt = sharedPreferences.getString("Prompt", "");

                    if (TextUtils.isEmpty(prompt)) {
                        //   Toast.makeText(this, , Toast.LENGTH_SHORT).show();
                        Toast.makeText(Add_Event.this, "Prompt is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Check which API is selected
                String selectedApi = sharedPreferences.getString("SelectedApi", "use ChatGpt");

                switch (selectedApi) {
                    case "use ChatGpt":
                        getoutput_chatgpt(prompt+":"+eventDescription.getText().toString());
                        break;

                    case "use Gemini Ai":
                        getoutput_gemini(prompt+":"+eventDescription.getText().toString());
                        break;

                    default:
                        Toast.makeText(Add_Event.this, "No API selected", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        // Display Current Date and Time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        selectedDateTime.setText("Current Date and Time: " + currentDateTime);

        // Date Picker
        datePickerBtn.setOnClickListener(view -> showDatePicker());

        // Time Picker
        timePickerBtn.setOnClickListener(view -> showTimePicker());

        // Speech to Text
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recorder)
                {
                    recorder=true;
                    recordButton.setText("Stop Recording");
                    recording_animation_card.setVisibility(View.VISIBLE);
                    startRecording();
                }
                else
                {
                    recorder=false;
                    recordButton.setText("Start Recording");
                    stopRecording();
                }

//
            }
        });
        playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });

    }
    private  void  getoutput_chatgpt(String prompt)
    {
        String apiKey = sharedPreferences.getString("ChatGptApiKey", "");
        String apiUrl = "https://api.openai.com/v1/completions"; // Replace with ChatGPT endpoint

        if (TextUtils.isEmpty(apiKey)) {
            Toast.makeText(this, "ChatGPT API key is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", "text-davinci-003"); // Replace with your model
            jsonRequest.put("prompt", prompt);
            jsonRequest.put("max_tokens", 100);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    apiUrl,
                    jsonRequest,
                    response -> {
                        try {
                            String output = response.getString("choices");
                            Toast.makeText(this, "ChatGPT Output: " + output, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "ChatGPT API Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + apiKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
    private void getoutput_gemini(String prompt) {
        // Set up the URL for the Gemini API endpoint
       get_opt(prompt);
    }


    // Method to show DatePicker
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Update the button text to the selected date
            datePickerBtn.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    // Method to show TimePicker
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            // Update the button text to the selected time
            timePickerBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));

            // Update the date and time display
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    // Update DateTime Display
    public  void get_opt(String prompt)
    {
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash-001",
               "AIzaSyDp4QqV17XLUsZsSjgCLKdZdVTcWCZqeUk"
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        String mes = sharedPreferences.getString(KEY_PROMPT, "");
        Toast.makeText(this, mes+"'"+prompt+"'", Toast.LENGTH_SHORT).show();
        Content content = new Content.Builder()
                .addText(mes+"'"+prompt+"'")
                .build();


        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    Toast.makeText(Add_Event.this, resultText, Toast.LENGTH_SHORT).show();
                    eventDescription.setText(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            }, this.getMainExecutor());
        }
    }

    // Start Speech-to-Text Intent
    private void setupModel() {
        File modelDir = new File(getExternalFilesDir(null), "vosk-model");
        if (!modelDir.exists()) {

            modelDir.mkdirs();
            try (InputStream zipInputStream = getAssets().open("model.zip")) {
                ZipUtils.extractZip(zipInputStream, modelDir);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "error in extracting zip", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void initializeVoskModel() {
        try {
            // Ensure model extraction
           setupModel();
           try {

               File modelDir = new File(getExternalFilesDir(null), "vosk-model/vosk-model-small-en-in-0.4");

               // Load the Vosk model
               voskModel = new Model(modelDir.getPath());
           }
           catch (Exception e)
           {
               Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
           }
            //Toast.makeText(this, "Vosk model loaded successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Vosk model failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle Speech-to-Text Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                eventDescription.setText(result.get(0));
            }
        }
        stopRecording();
    }

    private void saveEventData(String title, String eventDescription, String selectedDate, String selectedTime) {
        if (title == null || title.trim().isEmpty()) {
            Toast.makeText(this, "Event title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventDescription == null || eventDescription.trim().isEmpty()) {
            Toast.makeText(this, "Event description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null || selectedDate.trim().isEmpty()||selectedDate.contains("Pick")) {
            Toast.makeText(this, "Event date cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime == null || selectedTime.trim().isEmpty()||selectedTime.contains("Pick")) {
            Toast.makeText(this, "Event time cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assuming `DatabaseHelper` is your class that interacts with the SQLite database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String creationDate = dateFormat.format(new Date());
        String creationTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Save event to database
        boolean isInserted = databaseHelper.insertEvent(
                title,
                eventDescription,
                creationDate,
                creationTime,
                selectedDate,
                selectedTime,
                audioFilePath
        );

        // Show success or failure message
        if (isInserted) {
            try {
                Intent i=new Intent(Add_Event.this,MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();

            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save event", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////////functions to save audio///////////////////////////////////////////
    private void startRecording() {
        recording_animation_card.setVisibility(View.VISIBLE);
        File audioDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (audioDir != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            audioFilePath = audioDir.getAbsolutePath() + "/recording_" + timeStamp + ".3gp";
        } else {
            Toast.makeText(this, "Failed to get storage directory", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
           // Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error preparing MediaRecorder: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private String convertToWav(String inputPath) {
        // Check if the input file exists
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            Toast.makeText(this, "Input file not found", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Ensure the output directory exists



        // Construct the FFmpeg command
        String command = "-i " + inputPath + " -ar 16000 -ac 1 -y " + convertToWavFilePath(inputPath);

        // Execute FFmpeg command
        int rc = FFmpeg.execute(command);
        if (rc == 0) {
            //Toast.makeText(this, "Conversion to WAV successful: " , Toast.LENGTH_SHORT).show();
            return convertToWavFilePath(inputPath);
        } else {
            Toast.makeText(this, "Conversion failed with RC: " + rc, Toast.LENGTH_SHORT).show();
            return null;
        }
    }



    private void stopRecording() {
        recording_animation_card.setVisibility(View.GONE);
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            stopTime = System.currentTimeMillis();

            long duration = (stopTime - startTime) / 1000; // Duration in seconds

            // Unique code generation for file identification (if needed)
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startTime));
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(startTime));
            String uniqueCode = date.replace("-", "") + time.replace(":", "") + duration;

           // Toast.makeText(this, "Recording saved ", Toast.LENGTH_LONG).show();
            //recognizeSpeech(convertToWav(audioFilePath));

            //recognizeSpeech_usingPy(convertToWav(audioFilePath));
            recordingCard.setVisibility(View.VISIBLE);


        }
        isRecordingCompleted=true;
    }

    public String processAudioFiletoflac(String wavFilePath) {
        // Define the FLAC file path
        File wavFile = new File(wavFilePath);
        if (!wavFile.exists()) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
            return null;
        }
        String flacFilePath = replaceWavWithFlac(wavFilePath);

        // Convert WAV to FLAC using FFmpeg
        String command = "-i " + wavFilePath + " -f flac -y " + flacFilePath;
        int resultCode = FFmpeg.execute(command);

        if (resultCode == 0) {

            Toast.makeText(this, "File successfully converted to FLAC: ", Toast.LENGTH_SHORT).show();
            return flacFilePath; // Return the FLAC file path
        } else {

            Toast.makeText(this, "FFmpeg conversion failed"+resultCode, Toast.LENGTH_LONG).show();
            return null; // Indicate failure
        }
    }
    public static String replaceWavWithFlac(String filePath) {
        if (filePath == null || !filePath.endsWith(".wav")) {
            throw new IllegalArgumentException("Invalid .wav file path");
        }
        return filePath.substring(0, filePath.lastIndexOf(".")) + ".flac";
    }


    private String convertToWavFilePath(String audioFilePath) {
        if (audioFilePath.endsWith(".3gp")) {
            return audioFilePath.substring(0, audioFilePath.lastIndexOf(".")) + ".wav";
        } else {
            throw new IllegalArgumentException("Invalid file format. Expected a .3gp file.");
        }
    }

    private void recognizeSpeech(String wavFilePath) {
        try (FileInputStream fis = new FileInputStream(wavFilePath)) {
            Recognizer recognizer = new Recognizer(voskModel, 16000);
            byte[] buffer = new byte[4000]; // Try doubling the size
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    //Toast.makeText(this, "Result: " + recognizer.getResult(), Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(this, "Partial: " + recognizer.getPartialResult(), Toast.LENGTH_SHORT).show();
                }
            }

            eventDescription.setText(extractTextFromResult(recognizer.getFinalResult()));
            make_note.setVisibility(View.VISIBLE);

            //Toast.makeText(this, "Final Result: " + recognizer.getFinalResult(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error during recognition"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private String extractTextFromResult(String jsonResult) {
        try {
            JSONObject result = new JSONObject(jsonResult);
            return result.optString("text", ""); // Extract the "text" field
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void playAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
            updateSeekBar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
            isPlaying = false;
        });
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
        }
    }
    private void updateSeekBar() {
        playbackProgressBar.setMax(mediaPlayer.getDuration());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    playbackProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                    playbackTimer.setText(formatTime(mediaPlayer.getCurrentPosition()));
                    playbackProgressBar.postDelayed(this, 100);
                }
            }
        });
    }

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
