package com.example.recorderchunks.utils;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.IOException;

public class AudioUtils {

    public static String processAudioFiletoflac(String wavFilePath , Context context) {
        // Define the FLAC file path
        File wavFile = new File(wavFilePath);
        if (!wavFile.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
            return null;
        }
        String flacFilePath = replaceWavWithFlac(wavFilePath);

        // Convert WAV to FLAC using FFmpeg
        String command = "-i " + wavFilePath + " -f flac -y " + flacFilePath;
        int resultCode = FFmpeg.execute(command);

        if (resultCode == 0) {

            Toast.makeText(context, "File successfully converted to FLAC: ", Toast.LENGTH_SHORT).show();
            return flacFilePath; // Return the FLAC file path
        } else {

            Toast.makeText(context, "FFmpeg conversion failed"+resultCode, Toast.LENGTH_LONG).show();
            return null; // Indicate failure
        }
    }
    public static String replaceWavWithFlac(String filePath) {
        if (filePath == null || !filePath.endsWith(".wav")) {
            throw new IllegalArgumentException("Invalid .wav file path");
        }
        return filePath.substring(0, filePath.lastIndexOf(".")) + ".flac";
    }

    public static String convertToWavFilePath(String audioFilePath) {
        return audioFilePath.substring(0, audioFilePath.lastIndexOf('.')) + ".wav";
    }
    public static  boolean isWavFormatValid(String inputPath ,Context context) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(inputPath);
            MediaFormat format = extractor.getTrackFormat(0);

            int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

            // Desired parameters: sample rate = 16000 Hz, channel count = 1
            return sampleRate == 16000 && channelCount == 1;
        } catch (IOException e) {
            Toast.makeText(context, "Error reading file format", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            extractor.release();
        }
    }

    public static String performConversion(String inputPath, String outputPath,Context context) {
        File outputFile = new File(outputPath);
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // Construct the FFmpeg command for conversion
        String command = String.format(
                "-i \"%s\" -ar 16000 -ac 1 -c:a pcm_s16le -y \"%s\"",
                inputPath, outputPath
        );

        int rc = FFmpeg.execute(command);
        if (rc == 0) {
            Toast.makeText(context, "Conversion to WAV successful ", Toast.LENGTH_SHORT).show();
            return outputPath;
        } else {
            Toast.makeText(context, "Conversion failed: " + rc, Toast.LENGTH_LONG).show();
            return null;
        }
    }




}
