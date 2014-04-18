package com.clubconnected.dj;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.clubconnected.dj.Models.Message;

/**
 * Created by Newd on 4/6/14.
 */
public class SongActivity  extends ActionBarActivity {
    // class level variables for searching & ordering.
    private String searchTerm = "";
    private String orderTerm = "SONG_NAME ASC";

    // from columns & toViews, needed to select which map database columns to view objects within the UI.
    private final String[] fromColumns = {"_id", "SONG_NAME" ,"SONG_ARTIST","SONG_GENRE"};
    private final int[] toViews = {R.id.song_id, R.id.song_name, R.id.song_artist, R.id.song_genre};
    private CustomCursorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this is some magic that i can't remove
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        // end magic

        // query, returning a cursor
        final String sqlQuery = "SELECT * FROM SONG ORDER BY SONG_NAME ASC";
        DataBaseManager db = DataBaseManager.instance(SongActivity.this);
        Cursor rs = db.select(sqlQuery);

        // create a new custom cursor adapter, attaching the result set to the adapter.
        adapter = new CustomCursorAdapter(this,rs);

        // attach the adapter to the listview to show the results.
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // giving the searchview ontext change listeners, this listens for any text entered and
        // updates the query against the database.
        SearchView search = (SearchView) findViewById(R.id.searchSong);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.isEmpty()) {
                    searchTerm = "";
                }
                else {
                    searchTerm = s.replace("'", "\\'");
                }
                doQuery();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    searchTerm = "";
                }
                else {
                    searchTerm = s.replace("'", "");
                }
                doQuery();
                return false;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // onclick listener for the headings, allowing for sorting.
    // using a generic onclick listener for each of the headings, so we use a switch statement
    // to determine which button was pressed.
    public void sortSongs(View v) {

        // determine sorting order (taking into account the previous sort as well
        // only single column sorting will be required for now.
        switch(v.getId()) {
            case R.id.songHeading:
                if (orderTerm.equals("SONG_NAME ASC")) {
                    orderTerm = "SONG_NAME DESC";
                } else {
                    orderTerm = "SONG_NAME ASC";
                }
                break;
            case R.id.artistHeading:
                if (orderTerm.equals("SONG_ARTIST ASC")) {
                    orderTerm = "SONG_ARTIST DESC";
                } else {
                    orderTerm = "SONG_ARTIST ASC";
                }
                break;
            case R.id.genreHeading:
                if (orderTerm.equals("SONG_GENRE ASC")) {
                    orderTerm = "SONG_GENRE DESC";
                } else {
                    orderTerm = "SONG_GENRE ASC";
                }
                break;
        }

        // standard function to swap the cursors associated with the ListView
        doQuery();
    }

    // performs a standard query and attaches the new cursor to the adapter attached to the listview
    private void doQuery() {
        // get db conn
        DataBaseManager db = DataBaseManager.instance(SongActivity.this);

        // generate query, allow the user to search by song name, artist, or genre.
        // Unfortunately, this query doesn't provide reasonable "relevancy" as opposed to a full-text index.
        // however, it is more than suitable for a basic interface such as this.
        String sqlQuery = "SELECT * FROM SONG WHERE SONG_NAME LIKE '%" + searchTerm + "%' OR " +
                "SONG_ARTIST LIKE '%" + searchTerm + "%' OR " +
                "SONG_GENRE LIKE '%" + searchTerm + "%' " +
                "ORDER BY " + orderTerm;

        // perform query
        Cursor rs = db.select(sqlQuery);

        // swap the cursor attached to the adapter
        adapter.changeCursor(rs);
    }

    // when the user taps the shoutout button, show a dialog form allowing for user entry.
    public void shoutoutClick(View v) {

        // create the alert dialog

        final EditText input = new EditText(this);
        input.setLines(5);
        input.setMaxLines(8);
        input.setGravity(Gravity.TOP | Gravity.LEFT);
        input.setBackgroundResource(R.drawable.registerback);
        input.setHint("Type your shout-out here and press send below!");

        final AlertDialog confirm = new AlertDialog.Builder(SongActivity.this)
                .setTitle("DJ Shoutout Request")
                .setMessage("Use this form to send a message to the DJ.  Let him know why you're here, what song you want to hear, or what you're celebrating!")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(input)
                        // positive button w/ onclick
                .setPositiveButton("Shout It!", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        // get the user ID & Date, create a message object, and insert to the database.
                        SharedPreferences prefs = SongActivity.this.getSharedPreferences(
                                "com.clubconnected.dj", Context.MODE_PRIVATE);
                        Long userID = prefs.getLong("ID", 0);
                        String userMessage = input.getText().toString(); // get the message from the programatically added editText

                        Message thisMessage = new Message(userID, userMessage, SongActivity.this);

                        if (thisMessage.saveToLocalDB()) {
                            Toast.makeText(SongActivity.this, "Message #" + thisMessage.getMessageID() + " was submitted successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SongActivity.this, "Your message could not be processed! try again later.", Toast.LENGTH_LONG).show();
                        }


                    }
                })
                        // negative button w/ empty onclick.  Was going to put a toast in here, but thought I would use them
                        // as sparcely as possible to ensure they remain relevant to the user.
                .setNegativeButton("Nah", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // empty onclick.. don't do anything if they hit cancel.
                    }
                }).create(); // create

               confirm.show();

    }
    /**
     * A placeholder fragment containing a simple view.
     * no magic happens here; however, don't remove it or the app breaks.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        //@Override
       /* public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
           // View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            //return rootView;
        } */
    }

}
