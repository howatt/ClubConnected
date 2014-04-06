package com.clubconnected.dj;

/**
 * Created by Newd on 4/5/14.
 */

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.impl.cookie.DateParseException;

import android.database.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends ActionBarActivity {

    final static int AGE_LIMIT = 19;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        /* testing to see if I can access user preferences from another activity.
        SharedPreferences prefs = this.getSharedPreferences(
                "com.clubconnected.dj", Context.MODE_PRIVATE);
        String username = prefs.getString("FNAME", "");
        Toast.makeText(RegistrationActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show(); */
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

        ArrayList<String> errorsFound = new ArrayList();
        Boolean hasError = false;
        int age;

        // validate all the fields (should be pretty straight forward)
        // collecting errors in arraylist along the way.
        try {
            age = calculateAge(dob);
            if (age < AGE_LIMIT) {
                errorsFound.add("You have been deemed underage! Do you even lift?");
                hasError = true;
            }
        } catch (DateParseException e) {
            errorsFound.add("You can't be born in the future brah!");
            hasError = true;
        }

        if (!checkNameInput(fname)) {
            errorsFound.add("Invalid first name");
            hasError = true;
        }

        if (!checkNameInput(lname)) {
            errorsFound.add("Invalid last name");
            hasError = true;
        }

        if (!checkEmailInput(email)) {
            errorsFound.add("Invalid Email");
            hasError = true;
        }

        if (!checkPasswordInput(password)){
            errorsFound.add("Invalid Password, must be at least 6 characters");
            hasError = true;
        }

        // if any errors were found, we'll generate a formatted error string from the array list.
        if (hasError) {
            String errorString = "Your registration failed, brah: \n";
            for (String error: errorsFound) {
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
            // no errors were found, so let's attempt to register the user.

            // get database connection via singleton pattern
            DataBaseManager db = DataBaseManager.instance(RegistrationActivity.this);
            ContentValues values = new ContentValues();
            values.put("USER_NAME", email);
            values.put("USER_PASSWORD", password);
            values.put("USER_TYPE", "1");
            values.put("USER_FNAME", fname);
            values.put("USER_LNAME", lname);

            try {
                long result = db.insert("USER", values);
                if (result == -1) {

                    final AlertDialog confirm = new AlertDialog.Builder(this)
                            .setTitle("An Error Occurred")
                            .setMessage("Your account could not be created.  Is your email address already in use?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .create();

                    // display error in modal form.
                    confirm.show();
                }
                else {
                    // user was created successfully and the "result" of the query represents their user ID

                    // we'll save all relevant details in the preferences, incase we need it later?
                    SharedPreferences prefs = this.getSharedPreferences(
                            "com.clubconnected.dj", Context.MODE_PRIVATE);

                    // put all the data into the preferences
                    prefs.edit().putString("FNAME", fname).commit();
                    prefs.edit().putString("LNAME", lname).commit();
                    prefs.edit().putString("USERNAME", email).commit();
                    prefs.edit().putLong("ID", result).commit();



                    // now redirect to the main page?
                    startActivity(new Intent(RegistrationActivity.this, SongActivity.class));

                }
            } catch (SQLException e) {
                // if there were SQL issues inserting, show an alert message.
                final AlertDialog confirm = new AlertDialog.Builder(this)
                        .setTitle("An Error Occurred")
                        .setMessage("The database may be unavailable, please try again later")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                // display error in modal form.
                confirm.show();

            }
            catch (Exception e) {
                // if there were general issues inserting, show an alert message.
                final AlertDialog confirm = new AlertDialog.Builder(this)
                        .setTitle("An Error Occurred")
                        .setMessage("An unknown error occurred.  Please try again later.  If the issue persists, please contact ClubConnected support")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();

                // display error in modal form.
                confirm.show();

            }


        }









    }

    private int calculateAge(Calendar dob) throws DateParseException{
        Calendar now = Calendar.getInstance();
        if (dob.after(now)) {
            throw new DateParseException("Can't be born in the future");
        }
        int year1 = now.get(Calendar.YEAR);
        int year2 = dob.get(Calendar.YEAR);
        int age = year1 - year2;
        int month1 = now.get(Calendar.MONTH);
        int month2 = dob.get(Calendar.MONTH);
        if (month2 > month1) {
            age--;
        } else if (month1 == month2) {
            int day1 = now.get(Calendar.DAY_OF_MONTH);
            int day2 = dob.get(Calendar.DAY_OF_MONTH);
            if (day2 > day1) {
                age--;
            }
        }
        return age;
    }

    private Boolean checkNameInput(String input) {
        if (input.equals("")) {
            return false;
        }
        return true;
    }

    private Boolean checkEmailInput(String email) {
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        return matcher.find();
    }

    private Boolean checkPasswordInput(String password) {
        if (password.equals("") || password.length() < 6) {
            return false;
        }
        return true;
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
