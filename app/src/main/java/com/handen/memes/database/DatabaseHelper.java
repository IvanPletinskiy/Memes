package com.handen.memes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.handen.memes.database.Schema.GroupsTable.GROUPSTABLE;
import static com.handen.memes.database.Schema.PostsTable.POSTSTABLE;

/**
 * Created by user2 on 29.05.2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "base.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + GROUPSTABLE + "(" +
       //         " _id integer primary key autoincrement, " +
                Schema.GroupsTable.ID + "," +
                Schema.GroupsTable.NAME + ", " +
                Schema.GroupsTable.SELECTED +
                ")"
        );
        db.execSQL("create table " + POSTSTABLE + "(" +
       //         "_id integer primary key, " +
                Schema.PostsTable.ID + "," +
                Schema.PostsTable.TEXT + "," +
                Schema.PostsTable.IMAGEURL + "," +
                Schema.PostsTable.DATE + "," +
                Schema.PostsTable.LIKES + "," +
                Schema.PostsTable.REPOSTS + "," +
                Schema.PostsTable.LIKED + "," +
                Schema.PostsTable.IMAGE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
