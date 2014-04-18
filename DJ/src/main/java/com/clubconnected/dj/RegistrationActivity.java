package com.clubconnected.dj;

/**
 * Created by Newd on 4/5/14.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import com.clubconnected.dj.Models.User;
import com.clubconnected.dj.Network.httpHandler;
import java.util.Calendar;

public class RegistrationActivity extends ActionBarActivity {

    ProgressDialog pDialog;
    httpHandler myHandler;
    String MAIN_URL = "http://www.tutlezone.com/dj/registerUser.php?";
    String url;
    private User thisUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    // onclick listener for the register button.
    public void registerOnClick(View v) {
        EditText txtFname = (EditText) findViewById(R.id.txtFname);
        EditText txtLname = (EditText) findViewById(R.id.txtLname);
        EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        DatePicker dateDOB = (DatePicker) findViewById(R.id.DOB);

        String fname = txtFname.getText().toString();
        String lname = txtLname.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        int day = dateDOB.getDayOfMonth();
        int month = dateDOB.getMonth();
        int year =  dateDOB.getYear();

        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);


        thisUser = new User(email, password, fname, lname, dob, RegistrationActivity.this);

        // if any errors were found, we'll generate a formatted error string from the array list.
        if (thisUser.hasErrors()) {
            String errorString = "Your registration failed: \n";
            for (String error: thisUser.getErrorsFound()) {
                errorString += "\n   -" + error;
            }

            // construct an alert dialog with the errors.
            final AlertDialog confirm = new AlertDialog.Builder(this)
                    .setTitle("Error(s) Found!")
                    .setMessage(errorString + "\n")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create();

            // display error in modal form.
            confirm.show();

        }
        else {
            url = MAIN_URL + "email=" + email + "&password=" + password + "&fname=" + fname + "&lname=" + lname;
            new Register().execute();

        }

    }

    // process the results from the user's registration
    private void processRegistrationAttempt() {
        try {

            String httpResponse = myHandler.getRawData();

            if (httpResponse.isEmpty() || httpResponse.equals("failure") || httpResponse.equals("invalid")) {
                Toast.makeText(RegistrationActivity.this, "Registration Failed . Did you fill out all the fields?", Toast.LENGTH_SHORT).show();
            } else {
                Long userID = Long.parseLong(httpResponse.trim());
                User.saveUserToScope(thisUser.getFirstName(), thisUser.getLastName(), thisUser.getUsername(), userID, RegistrationActivity.this);

                // redirect to song page.
                startActivity(new Intent(RegistrationActivity.this, SongActivity.class));
            }
        }
        catch (Exception e) {
            // print errors & show toast for failure if exception is caught (invalid data, no json data, etc)
            Log.e("Exception", e.getMessage());
            Toast.makeText(RegistrationActivity.this, "Your Registration Failed...", Toast.LENGTH_SHORT).show();
        }
    }

    // inner class to register the user to the database
    // only way to perform http requests is on a background thread.
    class Register extends AsyncTask<String, String, String> {


            /**
             * Before starting background thread Show Progress Dialog
             * */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(RegistrationActivity.this);
                pDialog.setMessage("Registering your account");
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
                processRegistrationAttempt();

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
