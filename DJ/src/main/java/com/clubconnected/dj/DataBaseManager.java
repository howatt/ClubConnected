package com.clubconnected.dj;

/**
 * Created by Newd on 4/5/14.
 */
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DataBaseManager extends SQLiteOpenHelper {

    // The Android's default system path of your application database.
    //data/data/ and /databases remain the same always. The one that must be changed is com.example which represents
    //the MAIN package of your project
    private static String DB_PATH = "/data/data/com.clubconnected.dj/databases/";

    //the name of your database
    private static String DB_NAME = "clubConnected";

    // database version
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     */
    public DataBaseManager() {
        super(ApplicationContextProvider.getContext(), DB_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statements to create the required tables.
        String CREATE_USERS_TABLE = "CREATE TABLE USER ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "USER_NAME CHAR(15), "+
                "USER_PASSWORD CHAR(15), "+
                "USER_TYPE CHAR(15))";

        String CREATE_SONGS_TABLE = "CREATE TABLE SONG ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "SONG_NAME CHAR(15), "+
                "SONG_ARTIST CHAR(15), "+
                "SONG_GENRE CHAR(15), "+
                "SONG_PLAYS INTEGER)";

        String CREATE_MESSAGES_TABLE = "CREATE TABLE MESSAGE ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MESSAGE_CONTENT CHAR(254), "+
                "MESSAGE_FROM CHAR(15), "+
                "MESSAGE_READ BOOLEAN)";

        // create the tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SONGS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);

        // now we'll fill them with reasonable data
        String POPULATE_USERS_TABLE = "INSERT INTO USER(USER_NAME, USER_PASSWORD, USER_TYPE) VALUES(" +
                "('adam', 'adam', 'admin')" +
                "('josiah', 'josiah', 'admin')" +
                "('admin', 'admin', 'admin')";

        db.execSQL(POPULATE_USERS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables
        db.execSQL("DROP TABLE IF EXISTS USER");
        db.execSQL("DROP TABLE IF EXISTS SONG");
        db.execSQL("DROP TABLE IF EXISTS MESSAGE");

        // create fresh tables
        this.onCreate(db);
    }

    /**
     * Select method
     *
     * @param query select query
     * @return - Cursor with the results
     * @throws android.database.SQLException sql exception
     */
    public Cursor select(String query) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor aC =  db.rawQuery(query, null);
        return aC;
    }

    /**
     * Insert method
     *
     * @param table  - name of the table
     * @param values values to insert
     * @throws android.database.SQLException sql exception
     */
    public void insert(String table, ContentValues values) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        db.insert(table, null, values);
    }

    /**
     * Delete method
     *
     * @param table - table name
     * @param where WHERE clause, if pass null, all the rows will be deleted
     * @throws android.database.SQLException sql exception
     */
    public void delete(String table, String where) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(table, where, null);

    }

    /**
     * Update method
     *
     * @param table  - table name
     * @param values - values to update
     * @param where  - WHERE clause, if pass null, all rows will be updated
     */
    public void update(String table, ContentValues values, String where) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.update(table, values, where, null);

    }

    /**
     * Let you make a raw query
     *
     * @param command - the sql comand you want to run
     */
    public void sqlCommand(String command) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(command);
    }




}
