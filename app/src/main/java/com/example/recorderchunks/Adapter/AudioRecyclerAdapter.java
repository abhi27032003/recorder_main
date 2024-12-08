package com.example.recorderchunks.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.example.recorderchunks.Add_Event;
import com.example.recorderchunks.DatabaseHelper;
import com.example.recorderchunks.Model_Class.Recording;
import com.example.recorderchunks.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.datepicker.OnSelectionChangedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.AudioViewHolder> {
    private final ArrayList<Recording> recordingList;
    private final Context context;
    private static MediaPlayer mediaPlayer;
    private static int currentPlayingPosition = -1;
    private static final Handler handler = new Handler();
    private static Runnable updateSeekBarRunnable;
    private ArrayList<String> selectedItems;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "PromptSelectionPrefs";
    private OnSelectionChangedListener selectionChangedListener;






    public AudioRecyclerAdapter(ArrayList<Recording> recordingList, Context context, OnSelectionChangedListener listener) {
        this.recordingList = recordingList;
        this.context = context;
        this.selectedItems = new ArrayList<>();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.selectionChangedListener = listener;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recorder, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.itemView.setFocusable(false);

        holder.playbackProgressBar.setFocusable(false);

        Recording audioItem = recordingList.get(position);
        loadSelectionState(audioItem.getEventId());
        if(audioItem.getIs_transcribed().equals("no"))
        {

        }

        if (selectedItems.contains(audioItem.getDescription())) {
            holder.recordingCard.setCardBackgroundColor(context.getResources().getColor(R.color.third));
            holder.add_to_list.setImageResource(R.drawable.baseline_cancel_24); // Replace with your "remove" icon resource
        } else {
            holder.recordingCard.setCardBackgroundColor(context.getResources().getColor(R.color.fourth));
            holder.add_to_list.setImageResource(R.drawable.baseline_add_24); // Replace with your "add" icon resource
        }

        holder.add_to_list.setOnClickListener(v -> {
            if (selectedItems.contains(audioItem.getDescription())) {
                // Remove the item from the selected list
                selectedItems.remove(audioItem.getDescription());
                holder.add_to_list.setImageResource(R.drawable.baseline_add_24); // Change to "add" icon
                holder.recordingCard.setCardBackgroundColor(context.getResources().getColor(R.color.fourth));
//           holder.recordingCard.setCardBackgroundColor(context.getResources().getColor(R.color.fourth));

                saveSelectionState(audioItem.getEventId());
                notifySelectionChanged();
               // Toast.makeText(context, "Removed from selection", Toast.LENGTH_SHORT).show();
            } else {
                // Add the item to the selected list
                selectedItems.add(audioItem.getDescription());
                holder.add_to_list.setImageResource(R.drawable.baseline_cancel_24);
                holder.recordingCard.setCardBackgroundColor(context.getResources().getColor(R.color.third));
// Change to "remove" icon
                saveSelectionState(audioItem.getEventId());
                notifySelectionChanged();
               // Toast.makeText(context, "Added to selection", Toast.LENGTH_SHORT).show();
            }
        });
       holder.bindAudioData(audioItem, position);
       if(position!=currentPlayingPosition)
       {
           //resetPlayerUI(holder);
       }

        //
        /////////////////////////////////////////
        holder.playPauseButton.setOnClickListener(v -> {
            if (currentPlayingPosition == position) {
                togglePlayPause(holder);
            } else {

                playNewAudio(holder, audioItem, position);
            }
        });

        // Handle seek bar changes
        holder.playbackProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updateElapsedTime(holder);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        holder.delete.setOnClickListener(v -> {
            DatabaseHelper databaseHelper=new DatabaseHelper(context);
            // Confirm deletion
            new AlertDialog.Builder(context)
                    .setTitle("Delete Recording")
                    .setMessage("Are you sure you want to delete this recording?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the recording from database and file storage
                        boolean isDeleted = databaseHelper.deleteRecordingById(audioItem.getRecordingId());
                        if (isDeleted) {
                            // Remove from the list and notify adapter
                            recordingList.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Failed to delete recording.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        holder.recordingLabel.setText(audioItem.getName());
        holder.ceation_d_and_t.setText("Created at : "+audioItem.getDate());
        holder.total_time.setText(" / "+convertSecondsToTime(Integer.parseInt(audioItem.getLength())));
        holder.Description.setText("Transcription\n"+audioItem.getDescription());
        holder.expand_button.setOnClickListener(v -> {
            if (holder.Description.getMaxLines() == 1) {
                // Expand the TextView
                holder.Description.setMaxLines(Integer.MAX_VALUE);
                holder.expand_button.setImageResource(R.mipmap.collapse);
            } else {
                // Collapse the TextView
                holder.Description.setMaxLines(1);
                holder.expand_button.setImageResource(R.mipmap.expand);
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

    }
    private void togglePlayPause(AudioViewHolder holder) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            holder.playPauseButton.setImageResource(com.example.jean.jcplayer.R.drawable.ic_play);
            stopSeekBarUpdates();
        } else {
            mediaPlayer.start();
            holder.playPauseButton.setImageResource(com.example.jean.jcplayer.R.drawable.ic_pause);
            startSeekBarUpdates(holder);
        }
    }

    private void playNewAudio(AudioViewHolder holder, Recording audioItem, int position) {

        // Stop the previous playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            stopSeekBarUpdates();
        }
        int previousPlayingPosition = currentPlayingPosition;
        currentPlayingPosition = position;
        if(previousPlayingPosition!=-1)
        {
            notifyItemChanged(previousPlayingPosition); // Reset the previous item's view

        }
        //notifyItemChanged(position);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioItem.getUrl());
            mediaPlayer.prepare(); // Prepares the player synchronously


            // Start playback
            mediaPlayer.start();
            holder.playPauseButton.setImageResource(com.example.jean.jcplayer.R.drawable.ic_pause);
            holder.playbackProgressBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnCompletionListener(mp -> resetPlayerUI(holder,position));
            currentPlayingPosition = position;

            // Start updating the SeekBar
            startSeekBarUpdates(holder);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to play audio", Toast.LENGTH_SHORT).show();
        }
    }


    private void startSeekBarUpdates(AudioViewHolder holder) {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    holder.playbackProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                    updateElapsedTime(holder);
                    handler.postDelayed(this, 10);
                    if(mediaPlayer.getDuration()==holder.playbackProgressBar.getProgress())
                    {
                        holder.playbackProgressBar.setProgress(0);
                        holder.playbackTimer.setText("00:00");
                        holder.playPauseButton.setImageResource(com.example.jean.jcplayer.R.drawable.ic_play);

                    }
                }
            }
        };
        handler.post(updateSeekBarRunnable);
    }
    private void saveSelectionState(int id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("selected_items_"+id, new HashSet<>(selectedItems));
        editor.apply();
    }

    private void loadSelectionState(int id) {
        Set<String> savedItems = sharedPreferences.getStringSet("selected_items_"+id, new HashSet<>());
        selectedItems.clear();
        if (savedItems != null) {
            selectedItems.addAll(savedItems);
        }
        notifySelectionChanged();
    }

    private void stopSeekBarUpdates() {
        if (updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void resetPlayerUI(AudioViewHolder holder,int currentPlayingPosition) {
        holder.playbackProgressBar.setProgress(0);
        holder.playbackTimer.setText("00:00");
        holder.playPauseButton.setImageResource(com.example.jean.jcplayer.R.drawable.ic_play);


    }

    private void updateElapsedTime(AudioViewHolder holder) {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            holder.playbackTimer.setText(formatTime(currentPosition));
        }
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
        ImageView playPauseButton;
        SeekBar playbackProgressBar;
        TextView playbackTimer;
        Button Start_Transcription;
        JcPlayerView JcPlayerView;
        TextView recordingLabel,ceation_d_and_t,total_time,Description;
        ImageView expand_button,add_to_list,delete;
        PlayerView pw;
        CardView recordingCard;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            total_time=itemView.findViewById(R.id.total_time);
            Description=itemView.findViewById(R.id.Description);
            ceation_d_and_t=itemView.findViewById(R.id.ceation_d_and_t);
            recordingLabel=itemView.findViewById(R.id.recordingLabel);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            playbackProgressBar = itemView.findViewById(R.id.playbackProgressBar);
            playbackTimer = itemView.findViewById(R.id.playbackTimer);
            JcPlayerView =  itemView.findViewById(R.id.jcplayer);
            Start_Transcription=itemView.findViewById(R.id.Start_Transcription);
            recordingCard=itemView.findViewById(R.id.recordingCard);
            pw=itemView.findViewById(R.id.playerView);
            expand_button=itemView.findViewById(R.id.expand_btn);
            delete=itemView.findViewById(R.id.deleteButton);
            add_to_list=itemView.findViewById(R.id.add_to_list);


        }
        public void bindAudioData(Recording audioItem, int position) {
            playbackTimer.setText("00:00");
            playbackProgressBar.setProgress(0);
            playbackProgressBar.setMax(0); // Reset seek bar until new audio is loaded
            playPauseButton.setImageResource(ir.one_developer.file_picker.R.drawable.ic_play);
        }

    }
    private void notifySelectionChanged() {
        if (selectionChangedListener != null) {
            selectionChangedListener.onSelectionChanged(selectedItems);
        }
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(ArrayList<String> updatedSelection);
    }
}
