package com.handen.memes.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import com.handen.memes.database.Schema.GroupsTable;

import static com.handen.memes.database.Schema.GroupsTable.TABLENAME;

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
        db.execSQL("create table " + TABLENAME + "(" +
                " _id integer primary key autoincrement, " +
                Schema.GroupsTable.ID + "," +
                Schema.GroupsTable.NAME + ", " +
                Schema.GroupsTable.SELECTED +
                ")"
        );
        //TODO добавить вторую таблицу
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
