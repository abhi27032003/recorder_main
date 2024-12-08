package com.example.recorderchunks.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.example.recorderchunks.Model_Class.Recording;
import com.example.recorderchunks.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.AudioViewHolder> {
    private final List<Recording> recordingList;
    private final Context context;

    private MediaPlayer mediaPlayer;

    private int currentlyPlaying = -1;


    public AudioRecyclerAdapter(List<Recording> recordingList, Context context) {
        this.recordingList = recordingList;
        this.context = context;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recorder, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        ArrayList<JcAudio> jcAudios = new ArrayList<>();
        Uri audioUri = Uri.fromFile(new File(recordingList.get(position).getUrl()));

        // Convert the Uri to a URL string
        String audioUrl = audioUri.toString();
        //jcAudios.add(JcAudio.createFromAssets("url audio","http://xxxx/abc.mp3" ));
        holder.recordingLabel.setText(recordingList.get(position).getName());
        holder.ceation_d_and_t.setText("Created at : "+recordingList.get(position).getDate());
        holder.total_time.setText(" / "+convertSecondsToTime(Integer.parseInt(recordingList.get(position).getLength())));
      //  holder.JcPlayerView.initPlaylist(jcAudios, null);
       // JcPlayerView.createNotification(R.drawable.baseline_play_circle_24); // Your icon resource
        String audioPath = recordingList.get(position).getUrl();
        holder.playPauseButton.setOnClickListener(v -> {
            if (currentlyPlaying == position) {
                pauseAudio();
                holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
            } else {
                playAudio(audioPath, holder);
                holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
            }
        });
        if(recordingList.get(position).isRecorded())
        {
            holder.Start_Transcription.setText("Recorded");
            holder.Start_Transcription.setBackgroundColor(context.getResources().getColor(R.color.secondary));
        }
        else
        {
            holder.Start_Transcription.setText("Imported");
            holder.Start_Transcription.setBackgroundColor(context.getResources().getColor(R.color.nav));
        }

        // Reset view if not playing
        if (currentlyPlaying != position) {
            holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
            holder.playbackProgressBar.setProgress(0);
        }
    }

    private void playAudio(String audioPath, AudioViewHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentlyPlaying = holder.getAdapterPosition();
            updateSeekBar(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    private void updateSeekBar(AudioViewHolder holder) {
        holder.playbackProgressBar.setMax(mediaPlayer.getDuration());
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                holder.playbackProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                holder.playbackTimer.post(() -> holder.playbackTimer.setText(formatTime(mediaPlayer.getCurrentPosition())));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    public static String convertSecondsToTime(int totalSeconds) {
        // Convert seconds to hours, minutes, and seconds
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        // Return formatted time
        if (hours > 0) {
            // Include hours if applicable
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            // Only minutes and seconds
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        FloatingActionButton playPauseButton;
        SeekBar playbackProgressBar;
        TextView playbackTimer;
        Button Start_Transcription;
        JcPlayerView JcPlayerView;
        TextView recordingLabel,ceation_d_and_t,total_time;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            total_time=itemView.findViewById(R.id.total_time);
            ceation_d_and_t=itemView.findViewById(R.id.ceation_d_and_t);
            recordingLabel=itemView.findViewById(R.id.recordingLabel);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            playbackProgressBar = itemView.findViewById(R.id.playbackProgressBar);
            playbackTimer = itemView.findViewById(R.id.playbackTimer);
            JcPlayerView =  itemView.findViewById(R.id.jcplayer);
            Start_Transcription=itemView.findViewById(R.id.Start_Transcription);
        }
    }
}
