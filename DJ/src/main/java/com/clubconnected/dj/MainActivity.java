package com.clubconnected.dj;

import android.content.Intent;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        // works?
        /*final String sqlQuery = "SELECT * FROM USER";
        DataBaseManager db = new DataBaseManager(MainActivity.this);
        Cursor rs = db.select(sqlQuery);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setText("Hello " + rs.getCount()); */




    }

    public void registrationOnClick(View v) {
        // when the registration button is clicked
        Button b = (Button) v;
        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));

    }

    public void loginOnClick(View v) {
        Button btnLogin = (Button) v;
        EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);

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
            final String sqlQuery = "SELECT * FROM USER WHERE USER_NAME = '" + username + "' AND USER_PASSWORD = '" + password + "'";
            DataBaseManager db = new DataBaseManager(MainActivity.this);
            Cursor rs = db.select(sqlQuery);

            if (rs.getCount() == 0) {
                Toast.makeText(MainActivity.this, "Invalid Username and/or Password. Try again.", Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                if (rs.moveToFirst()) {
                    String fname = rs.getString(rs.getColumnIndex("USER_FNAME"));
                    String lname = rs.getString(rs.getColumnIndex("USER_FNAME"));

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
