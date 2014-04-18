package com.clubconnected.dj.Models;

import android.content.ContentValues;
import android.content.Context;

import com.clubconnected.dj.DataBaseManager;

/**
 * Created by Howatt on 16/04/14.
 * A basic model for messages to be inserted to the database.
 * No validation performed here;
 */
public class Message extends Model{
    private Long user_id;
    private String message_text;

    public static String TABLE_NAME = "MESSAGE";

    // basic constructor
    public Message(long user_id_in, String message_text_in, Context aContext) {
        super(aContext);

        user_id = user_id_in;
        message_text = message_text_in;

    }

    // save the request to the local database
    public boolean saveToLocalDB(){

        // create map with values.
        ContentValues values = new ContentValues();
        values.put("MESSAGE_CONTENT", message_text);
        values.put("USER_ID", user_id);

        return insertToLocalDB(TABLE_NAME, values);
    }

    // getter to return the newly inserted ID.
    public String getMessageID() {
        return insertedID.toString();
    }
}
