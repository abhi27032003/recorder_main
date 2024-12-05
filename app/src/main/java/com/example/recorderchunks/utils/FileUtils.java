package com.example.recorderchunks.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static final String TAG = "FileUtils";

    // Copy the base.pt model from assets to internal storage and return the path
    public static String copyModelFromAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        File modelFile = new File(context.getFilesDir(), "tiny.pt");

        try {
            // Check if the file already exists
            if (modelFile.exists()) {
                Log.d(TAG, "Model file already exists: " + modelFile.getAbsolutePath());
                Toast.makeText(context, "Model file already exists: " + modelFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                return modelFile.getAbsolutePath();
            } else {
                Toast.makeText(context, "Model file does not exist, starting copy process.", Toast.LENGTH_SHORT).show();
            }

            // Open the asset as input stream
            InputStream in = assetManager.open("tiny.pt");  // "base.pt" is the model in assets
            Toast.makeText(context, "Asset input stream opened successfully.", Toast.LENGTH_SHORT).show();

            OutputStream out = new FileOutputStream(modelFile);
            Toast.makeText(context, "Output stream opened successfully.", Toast.LENGTH_SHORT).show();

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            // Close the streams
            in.close();
            out.close();

            Toast.makeText(context, "Model file copied successfully to: " + modelFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Model file copied successfully: " + modelFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error copying model file from assets", e);
            // Display error with Toast
            Toast.makeText(context, "Error copying model file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Return the path to the copied model
        return modelFile.getAbsolutePath();
    }
}
