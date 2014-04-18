package com.clubconnected.dj;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.clubconnected.dj.Models.User;
import com.clubconnected.dj.Network.httpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * MainActivity
 * The launch screen for the club connected app
 * User must log in on this screen, or select the registration button to move forward.
 */
public class MainActivity extends ActionBarActivity {
    ProgressDialog pDialog;
    httpHandler myHandler;
    String MAIN_URL = "http://www.tutlezone.com/dj/usercheck.php?";
    String url;

    // JSON Node names
    private static final String TAG_USERNAME = "USERNAME";
    private static final String TAG_USER_FNAME = "USER_FNAME";
    private static final String TAG_USER_LNAME = "USER_LNAME";
    private static final String TAG_USER_ID = "USER_ID";

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


        // validate credentials
         if (User.validateCredentials(username, password, MainActivity.this)) {
             // if valid, formulate URL & execute HTTP request (async)
             url = MAIN_URL + "username=" + username + "&password=" + password;
             new LoginCheck().execute();
           } else {
             // if invalid credentials, show toast.
             Toast.makeText(MainActivity.this, "Invalid Username and/or Password. Try again.", Toast.LENGTH_SHORT).show();
         }

    }

    // called from within the async task
    // checks the JSON result to see data is available, stores said data within the preferences, and redirects to the song page.
    private void processLoginAttempt() {
        try {
            // returned as an array, turn that array into an object (only accepting 1 row)
            JSONArray jsonArr = myHandler.getJsonArray();
            JSONObject json = jsonArr.getJSONObject(0);

            // grab data from the json string (throws exception if unavailable.
            Long userid = json.getLong(TAG_USER_ID);
            String user_fname = json.getString(TAG_USER_FNAME);
            String user_lname = json.getString(TAG_USER_LNAME);
            String username = json.getString(TAG_USERNAME);
            // add the data to preference scope.
            User.saveUserToScope(user_fname, user_lname, username, userid, MainActivity.this);

            // redirect to song page.
            startActivity(new Intent(MainActivity.this, SongActivity.class));
        }
        catch (Exception e) {
            // print errors & show toast for failure if exception is caught (invalid data, no json data, etc)
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
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

    // inner class to check the login against the database.
    // only way to perform http requests is on a background thread.
    class LoginCheck extends AsyncTask<String, String, String> {


        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Verifying your credentials");
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
            processLoginAttempt();

        }

    }
}


