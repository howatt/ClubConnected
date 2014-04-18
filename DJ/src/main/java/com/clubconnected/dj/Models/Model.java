package com.clubconnected.dj.Models;

import android.content.ContentValues;
import android.content.Context;

import com.clubconnected.dj.DataBaseManager;

/**
 * A model object for other models to inherit from.
 * Contains two variables for context and the insertedID
 * Communicates with server when needed / handles saving to the database.
 */
public abstract class Model {
    protected Context context;
    protected Long insertedID = -1L;

    // straightforward constructor.
    public Model(Context context_in) {
        context = context_in;
    }

    // Inserts the passed values to the passed tableName.
    protected boolean insertToLocalDB(String tableName, ContentValues values) {
        // connect to database & create new map for the values to insert
        DataBaseManager db = DataBaseManager.instance(context);

        // perform the insert, which returns the newly created row, or -1 (unsuccessful)
        insertedID = db.insert(tableName, values);
        if (insertedID == -1) {
            return false;
        } else {
            // if the result code is anything but -1, it inserted OK.
            return true;
        }
    }

}
