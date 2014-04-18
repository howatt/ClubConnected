package com.clubconnected.dj;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.clubconnected.dj.Models.Message;
import com.clubconnected.dj.Network.httpHandler;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * SongActivity
 * This page is where most of the work happens.
 * Presents a list of songs for the user to select from, user can submit song requests
 * User can also use the message button at the bottom to submit messages to the DJ.
 * This data is all stored on the web database, so the DJ can access it through an admin page.
 * Searching is provided (to search through the song list) as is sorting, by tapping on a heading.
 */
public class SongActivity  extends ActionBarActivity {
    // class level variables for searching & ordering.
    private String searchTerm = "";
    private String orderTerm = "SONG_NAME ASC";

    // from columns & toViews, needed to select which map database columns to view objects within the UI.
    private final String[] fromColumns = {"_id", "SONG_NAME" ,"SONG_ARTIST","SONG_GENRE"};
    private final int[] toViews = {R.id.song_id, R.id.song_name, R.id.song_artist, R.id.song_genre};
    private CustomCursorAdapter adapter;

    private Message thisMessage;
    ProgressDialog pDialog;
    httpHandler myHandler;
    String MAIN_URL = "http://www.tutlezone.com/dj/insertMessage.php?";
    String url;

    // JSON Node names
    private static final String TAG_SONG_ID = "_id";
    private static final String TAG_SONG_NAME = "SONG_NAME";
    private static final String TAG_SONG_ARTIST = "SONG_ARTIST";
    private static final String TAG_SONG_GENRE = "SONG_GENRE";
    private static final String TAG_SONG_PLAYS = "SONG_PLAYS";

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

        // execute the async task to download, compare and show the song list.
        new CompareSongs().execute();

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

    // initialize the song list view with everything from the database.
    private void initializeSongList() {
        // query, returning a cursor
        final String sqlQuery = "SELECT * FROM SONG ORDER BY SONG_NAME ASC";
        DataBaseManager db = DataBaseManager.instance(SongActivity.this);
        Cursor rs = db.select(sqlQuery);

        // create a new custom cursor adapter, attaching the result set to the adapter.
        adapter = new CustomCursorAdapter(this,rs);

        // attach the adapter to the listview to show the results.
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
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


    // inner class to insert a message to the database
    // only way to perform http requests is on a background thread.
    class InsertMessage extends AsyncTask<String, String, String> {


        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SongActivity.this);
            pDialog.setMessage("Inserting Your Message");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }


        /**
         * contact the website using the http handler
         * */
        protected String doInBackground(String... args) {

            myHandler = new httpHandler(url);

            return null;
        }

        /**
         * After completing the background task Dismiss the progress dialog & show a toast
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all announcements
            pDialog.dismiss();

            // have the page attempt to process the returned json data.
            processMessageAttempt();

        }

    }

    // inner class to compare the local database to the remote
    // extensive process, however by saving locally we're saving the users data and making it more user friendly on future loads.
    // could be optimized for sure. (not saying it's perfect, more or less proof of concept)
    class CompareSongs extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SongActivity.this);
            pDialog.setMessage("Contacting the Server");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // each time we publish an update during the task, it will adjust the message on the progress bar.
        protected void onProgressUpdate(String...progress) {
            pDialog.setMessage(progress[0]);
        }


        /**
         * contact the website using the http handler
         * */
        protected String doInBackground(String... args) {

            // first we'll grab a check sum from the server and tell the user what we are doing
            myHandler = new httpHandler("http://www.tutlezone.com/dj/databaseCheckSum.php");
            publishProgress("Server Contacted, comparing to local database");

            // we'll compare that checksum to the local version
            DataBaseManager db = DataBaseManager.instance(SongActivity.this);
            if (db.compareToDB(myHandler.getRawData().trim())) {
              // good to go, they match, no change necessary.
            }
            else {
                // update the user, a change was identified, so we'll grab a song list.
                publishProgress("Songs need to be updated, Getting list from server.");
                myHandler = new httpHandler("http://www.tutlezone.com/dj/getSongs.php");
                // parse the data into a JSON array, which in turn we loop through, inserting each obj to the db.
                try {
                    JSONArray jsonArr = myHandler.getJsonArray();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        if (i % 3 == 0) {
                            // update user along the way, 3 was an arbitrary selection.
                            publishProgress("Songs Updating " + i + " out of " + jsonArr.length());
                        }
                        // parse the json object and insert it
                        JSONObject json = jsonArr.getJSONObject(i);
                        ContentValues values = new ContentValues();
                        values.put(TAG_SONG_NAME, json.getString(TAG_SONG_NAME));
                        values.put(TAG_SONG_ID, json.getLong(TAG_SONG_ID));
                        values.put(TAG_SONG_ARTIST, json.getString(TAG_SONG_ARTIST));
                        values.put(TAG_SONG_GENRE, json.getString(TAG_SONG_GENRE));
                        values.put(TAG_SONG_PLAYS, json.getLong(TAG_SONG_PLAYS));
                        db.insert("SONG", values);
                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }

            }

            return null;
        }

        /**
         * After completing the background task Dismiss the progress dialog & show a toast
         * **/
        protected void onPostExecute(String file_url) {
            // after we have the song list confirmed, update the user again, and load it into a cursor adapter via initializeloadlist.
            pDialog.setMessage("Loading your song list");
            initializeSongList();
            pDialog.dismiss();


        }

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

                        thisMessage = new Message(userID, userMessage, SongActivity.this);

                        // construct a URL & submit a get request to that URL
                        url = MAIN_URL + "user_id=" + userID + "&user_message=" + userMessage;
                        new InsertMessage().execute();

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

    private void processMessageAttempt() {
        try {

            // grab the raw data
            String httpResponse = myHandler.getRawData();

            // if it's empty or it equals failure strings, show an error.
            if (httpResponse.isEmpty() || httpResponse.equals("failure") || httpResponse.equals("invalid")) {
                Toast.makeText(SongActivity.this, "Insert Message Failed... are you connected to the internet?", Toast.LENGTH_SHORT).show();
            } else {
                // otherwise, parse the user ID from the response & save user preferences to scope.
                Long messageID = Long.parseLong(httpResponse.trim());
                Toast.makeText(SongActivity.this, "Message #" + messageID + " was submitted successfully!", Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            // print errors & show toast for failure if exception is caught (invalid data, no json data, etc)
            Log.e("Exception", e.getMessage());
            Toast.makeText(SongActivity.this, "Insert Message Failed...", Toast.LENGTH_SHORT).show();
        }
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
