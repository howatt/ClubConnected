package com.clubconnected.dj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * Created by Newd on 4/6/14.
 */
public class SongActivity  extends ActionBarActivity {
    private String searchTerm = "";
    private String orderTerm = "SONG_NAME ASC";

    private final String[] fromColumns = {"SONG_NAME" ,"SONG_ARTIST","SONG_GENRE"};
    private final int[] toViews = {R.id.song_name, R.id.song_artist, R.id.song_genre};
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

        final String sqlQuery = "SELECT * FROM SONG ORDER BY SONG_NAME ASC";

        DataBaseManager db = DataBaseManager.instance(SongActivity.this);

        Cursor rs = db.select(sqlQuery);

        adapter = new CustomCursorAdapter(this,rs);

        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(adapter);

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

        doQuery();
    }

    private void doQuery() {
        DataBaseManager db = DataBaseManager.instance(SongActivity.this);

        String sqlQuery = "SELECT * FROM SONG WHERE SONG_NAME LIKE '%" + searchTerm + "%' OR " +
                "SONG_ARTIST LIKE '%" + searchTerm + "%' OR " +
                "SONG_GENRE LIKE '%" + searchTerm + "%' " +
                "ORDER BY " + orderTerm;

        Cursor rs = db.select(sqlQuery);

        adapter.changeCursor(rs);
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
