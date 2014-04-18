package com.clubconnected.dj.Models;


import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.impl.cookie.DateParseException;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Howatt on 17/04/14.
 * a data carrier for user data.
 */
public class User extends Model {


    private String username;
    private String password;
    private Calendar dob;
    private String firstName;
    private String lastName;

    private ArrayList<String> errorsFound = new ArrayList();
    public final static int AGE_LIMIT = 19;

    public static String TABLE_NAME = "USER";

    // basic
    public User(String username_in, String password_in,
                String first_name_in, String last_name_in, Calendar dob_in,
                Context aContext){

        super(aContext);

        setUsername(username_in);
        setPassword(password_in);
        setFirstName(first_name_in);
        setLastName(last_name_in);
        setDob(dob_in);

        context = aContext;
    }

    public static Boolean validateCredentials(String username, String password, Context aContext) {
        if (username.equals("") || password.equals("")) {
            return false;
        }
        return true;
    }

    public static void saveUserToScope(String fname, String lname, String username, Long userid, Context aContext) {
        // save the provided data in application scope (preferences)
        SharedPreferences prefs = aContext.getSharedPreferences(
                "com.clubconnected.dj", Context.MODE_PRIVATE);

        // put all the data into the preferences
        prefs.edit().putString("FNAME", fname).commit();
        prefs.edit().putString("LNAME", lname).commit();
        prefs.edit().putString("USERNAME", username).commit();
        prefs.edit().putLong("ID", userid).commit();
    }

    public boolean hasErrors() {
        if (errorsFound.size() != 0) {
            return true;
        }
        return false;
    }

    private int calculateAge(Calendar dob) throws DateParseException {
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

    public ArrayList<String> getErrorsFound() {
        return errorsFound;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(username);
        if (matcher.find()) {
            this.username = username;
        } else {
            errorsFound.add("Invalid Username");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password.equals("") || password.length() < 6) {
            errorsFound.add("Invalid Password");
        } else {
            this.password = password;
        }

    }

    public Calendar getDob() {
        return dob;
    }

    public void setDob(Calendar dob) {
        try {
            int age = calculateAge(dob);
            if (age < AGE_LIMIT) {
                errorsFound.add("You have been deemed underage! Do you even lift?");
            }
            else {
                this.dob = dob;
            }
        } catch (DateParseException e) {
            errorsFound.add("You can't be born in the future!");
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName.equals("")) {
            errorsFound.add("Invalid First Name");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName.equals("")) {
            errorsFound.add("Invalid Last Name");
        }
        this.lastName = lastName;
    }


    public Long getUserID() {
        return insertedID;
    }



}
