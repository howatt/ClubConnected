package com.clubconnected.dj;

import android.content.ContentValues;
import android.content.Context;

/**
 * Created by Howatt on 16/04/14.
 * A basic model for requests to be inserted to the database.
 * No validation performed here;
 */
public class Request {
    private Long user_id;
    private String song_id;
    private Context context;
    private Long insertedID = -1L;

    // basic constructor
    public Request(long user_id_in, String song_id_in, Context aContext) {
        user_id = user_id_in;
        song_id = song_id_in;
        context = aContext;
    }

    // save the request to the local database
    public boolean saveToLocalDB(){
        // connect to database & create new map for the values to insert
        DataBaseManager db = DataBaseManager.instance(context);
        ContentValues values = new ContentValues();
        values.put("SONG_ID", song_id);
        values.put("USER_ID", user_id);

        // perform the insert, which returns the newly created row, or -1 (unsuccessful)
        insertedID = db.insert("REQUEST", values);
        if (insertedID == -1) {
            return false;
        } else {
            // if the result code is anything but -1, it inserted OK.
            return true;
        }
    }

    // getter to return the newly inserted ID.
    public String getRequestID() {
        return insertedID.toString();
    }
}
