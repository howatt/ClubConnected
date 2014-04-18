package com.clubconnected.dj;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.clubconnected.dj.Models.Request;

/**
 * Created by Howatt on 2/28/14.
 */
public class CustomCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    // Accepts the view(the row), also accepts context(the class containing the listview), also accepts a cursor(recordset)
    public void bindView(View view, Context context, Cursor cursor) {
        final Context context1 = context;

        // alternate background colours (for visual appeal only)
        if(cursor.getPosition()% 2 == 0) {
            view.setBackgroundColor(0xFFF4FFED);
        }
        else {
            view.setBackgroundColor(0xFFE7FFCF);
        }

        // add an onclick listener to each view added to the listview.
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // get the values from within the listview item
                final String songName = ((TextView) view.findViewById(R.id.song_name)).getText().toString();
                final String songID = ((TextView) view.findViewById(R.id.song_id)).getText().toString();
                final String songArtist = ((TextView) view.findViewById(R.id.song_artist)).getText().toString();

                // create a confirm dialog, so users can confirm the song request.
                final AlertDialog confirm = new AlertDialog.Builder(context1)
                        .setTitle("Song Request")
                        .setMessage("Submit Request for " + songArtist + "'s " + songName + "?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        // positive button w/ onclick
                        .setPositiveButton("Yeah!", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                // user clicked the positive "yeah" button, so let's insert the request.
                                // first we'll get the userID from the preferences.
                                SharedPreferences prefs = context1.getSharedPreferences(
                                        "com.clubconnected.dj", Context.MODE_PRIVATE);
                                Long userID = prefs.getLong("ID", 0);

                                Request thisRequest = new Request(userID, songID, context1);

                                if (thisRequest.saveToLocalDB()) {
                                    Toast.makeText(context1, "Song Request #" + thisRequest.getRequestID() + " was submitted successfully!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context1, "Your request could not be processed! try again later.", Toast.LENGTH_LONG).show();
                                }


                            }
                        })
                        // negative button w/ empty onclick.  Was going to put a toast in here, but thought I would use them
                        // as sparcely as possible to ensure they remain relevant to the user.
                        .setNegativeButton("No way, this ain't my jam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // empty onclick.. don't do anything if they hit cancel.
                            }
                        }).create(); // create & show
                confirm.show();

            }
        });

        // Set local variables for any Textviews you want to use
        TextView songNameTextView = (TextView) view.findViewById(R.id.song_name);
        TextView songArtistTextView = (TextView) view.findViewById(R.id.song_artist);
        TextView songGenreTextView = (TextView) view.findViewById(R.id.song_genre);
        TextView songIdTextView = (TextView) view.findViewById(R.id.song_id);

        // Link textviews to a certain column from the recordset
        songIdTextView.setText(cursor.getString(0)); //id
        songNameTextView.setText(cursor.getString(1)); // name
        songArtistTextView.setText(cursor.getString(2)); // artist
        songGenreTextView.setText(cursor.getString(3)); // genre

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.activity_listview, parent, false);
    }

}
