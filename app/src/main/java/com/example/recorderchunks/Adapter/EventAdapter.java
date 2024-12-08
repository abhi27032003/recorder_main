package com.example.recorderchunks.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.recorderchunks.Add_Event;
import com.example.recorderchunks.DatabaseHelper;
import com.example.recorderchunks.Model_Class.Event;
import com.example.recorderchunks.R;
import com.example.recorderchunks.Showdetails;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private Context context;
    private List<Event> events;
    public int eventid;
    ///////////////////////////////////////////// for playing and pausing audio ////////////


    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;



    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Event event = events.get(position);
        eventid=event.getId();

        if (event.getTitle() != null) {
            holder.titleTextView.setText(event.getTitle());
        } else {
            holder.titleTextView.setText("Not Available"); // Set empty text or a placeholder
        }

        if (event.getDescription() != null) {
            holder.descriptionTextView.setText(event.getDescription());
        } else {
            holder.descriptionTextView.setText("Not Available");
        }

        if (event.getCreationDate() != null) {
            holder.dateTextView.setText("created on : "+event.getCreationDate()+" at "+event.getCreationTime());
        } else {
            holder.dateTextView.setText("Not Available");
        }
        holder.playPauseButton.setOnClickListener(v -> {
            if (isPlaying) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPlaying = false;
                    holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
                }
                pauseAudio();
            } else {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(event.audioPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    isPlaying = true;
                    holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    updateSeekBar();
                    holder.playbackProgressBar.setMax(mediaPlayer.getDuration());
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer != null && isPlaying) {
                                    holder.playbackProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                                    holder.playbackTimer.setText(formatTime(mediaPlayer.getCurrentPosition()));
                                    holder.playbackProgressBar.postDelayed(this, 100);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.setOnCompletionListener(mp -> {
                    holder.playPauseButton.setImageResource(R.drawable.baseline_play_circle_24);
                    isPlaying = false;
                });
            }
        });


        if (event.getEventDate() != null) {
            holder.eventDate.setText(event.getEventDate());
        } else {
            holder.eventDate.setText("Not Available");
        }

        if (event.getEventTime() != null) {
            holder.eventTime.setText(event.getEventTime());
        } else {
            holder.eventTime.setText("Not Available");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Add_Event.class);
                intent.putExtra("eventId", event.getId());
                context.startActivity(intent);
            }
        });

        holder.deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inside the onClickListener for the delete button
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Confirmation")
                        .setMessage("Are you sure you want to delete this Event?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Handle the delete action here
                            DatabaseHelper db=new DatabaseHelper(context);
                            db.deleteEvent(event.getId());
                            db.deleteRecording(event.getId());
                            Toast.makeText(context,"Event Deleted Successfully..",Toast.LENGTH_LONG).show();
                            try {
                                if (position != RecyclerView.NO_POSITION) { // Ensure position is valid
                                    events.remove(position); // Remove the item from the data source
                                    notifyItemRemoved(position); // Notify the adapter of item removal
                                }
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog if 'No' is clicked
                            dialog.dismiss();
                        })
                        .show();

            }
        });

    }
    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    private void playAudio() {

    }

    private void pauseAudio() {

    }
    private void updateSeekBar() {

    }


    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, dateTextView;
        MaterialButton eventDate,eventTime;
        ImageView deleteEvent;
        private FloatingActionButton playPauseButton;
        private SeekBar playbackProgressBar;
        private TextView playbackTimer;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle);
            descriptionTextView = itemView.findViewById(R.id.eventDescription);
            dateTextView = itemView.findViewById(R.id.eventCreationDate);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventTime = itemView.findViewById(R.id.eventTime);
            deleteEvent=itemView.findViewById(R.id.deleteButton);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            playbackProgressBar = itemView.findViewById(R.id.playbackProgressBar);
            playbackTimer = itemView.findViewById(R.id.playbackTimer);
        }
    }
}
