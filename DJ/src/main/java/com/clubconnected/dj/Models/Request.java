package com.clubconnected.dj.Models;

import android.content.ContentValues;
import android.content.Context;

import com.clubconnected.dj.DataBaseManager;

/**
 * Created by Howatt on 16/04/14.
 * A basic model for requests to be inserted to the database.
 * No validation performed here;
 */
public class Request extends Model {
    private Long user_id;
    private String song_id;

    public static String TABLE_NAME = "REQUEST";


    // basic constructor
    public Request(long user_id_in, String song_id_in, Context aContext) {
        super(aContext);
        user_id = user_id_in;
        song_id = song_id_in;

    }

    // save the request to the local database
    public boolean saveToLocalDB(){

        // generate a content values with the necessary fields/values
        ContentValues values = new ContentValues();
        values.put("SONG_ID", song_id);
        values.put("USER_ID", user_id);

        // call superclass to insert the request to the database.
        return insertToLocalDB(TABLE_NAME, values);
    }

    // getter to return the newly inserted ID.
    public String getRequestID() {
        return insertedID.toString();
    }
}
