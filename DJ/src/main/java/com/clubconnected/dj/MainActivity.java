package com.clubconnected.dj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this is some magic that i can't remove
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        // end magic

    }

    // onclick listener for the registration button.
    public void registrationOnClick(View v) {
        // when the registration button is clicked, redirect to registration page.
        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));

    }

    // onclick listener for the login button.
    public void loginOnClick(View v) {

        // get any buttons or views tha twe may need.
        Button btnLogin = (Button) v;
        EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

        // get the username and password entered. getText().toString(), really?
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();
        Boolean hasError = false;

        // make sure fields aren't blank.
        if (username.equals("") || password.equals("")) {
            Toast.makeText(MainActivity.this, "You are missing some data. Try again.", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        // if an error hasn't been set, query the DB with the user & pass.
        if (!hasError) {

            // construct query
            final String sqlQuery = "SELECT * FROM USER WHERE USER_NAME = '" + username + "' AND USER_PASSWORD = '" + password + "'";

            // get database connection via singleton pattern
            DataBaseManager db = DataBaseManager.instance(MainActivity.this);

            // perform the query, returning a cursor to the results
            Cursor rs = db.select(sqlQuery);

            // if the cursor is still null (issue with DB) or 0 rows were returned.
            if (rs == null || rs.getCount() == 0) {
                // show a toast & set an error flag
                Toast.makeText(MainActivity.this, "Invalid Username and/or Password. Try again.", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                if (rs.moveToFirst()) {
                    // get the data from the row returned and save in application scope (private preferences)
                    SharedPreferences prefs = this.getSharedPreferences(
                            "com.clubconnected.dj", Context.MODE_PRIVATE);

                    // get the  necessary data from the cursor
                    String fname = rs.getString(rs.getColumnIndex("USER_FNAME"));
                    String lname = rs.getString(rs.getColumnIndex("USER_FNAME"));
                    String aUsername = rs.getString(rs.getColumnIndex("USER_NAME"));
                    Long id = rs.getLong(rs.getColumnIndex("_id"));

                    // put all the data into the preferences
                    prefs.edit().putString("FNAME", fname).commit();
                    prefs.edit().putString("LNAME", lname).commit();
                    prefs.edit().putString("USERNAME", aUsername).commit();
                    prefs.edit().putLong("ID", id).commit();

                    // now redirect
                    startActivity(new Intent(MainActivity.this, SongActivity.class));


                }

            }



        }





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
