package com.handen.memes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.handen.memes.App;
import com.handen.memes.Group;
import com.handen.memes.Post;

import java.util.ArrayList;

import static com.handen.memes.database.Schema.GroupsTable.GROUPSTABLE;
import static com.handen.memes.database.Schema.GroupsTable.ID;
import static com.handen.memes.database.Schema.GroupsTable.NAME;
import static com.handen.memes.database.Schema.GroupsTable.SELECTED;

/**
 * Created by user2 on 29.05.2018.
 */

public class Database {
    private static Database database;
    private static SQLiteDatabase mDatabase;

    public static Database get() {
        if(database == null)
            database = new Database(App.getContext());
        return database;
    }

    private Database(Context context) {
        mDatabase = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
    }

    public static ArrayList<Group> getGroupsNames() {
        ArrayList<Group> ret = new ArrayList<>();
        String[] columns = new String[2];
        columns[0] = NAME;
        columns[1] = SELECTED;
        Cursor cursor = mDatabase.query(false,
                GROUPSTABLE,
                columns,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i ++) {
            String name = cursor.getString(0);
            boolean isSelected = cursor.getInt(1) == 1;
            Group group = new Group(name, isSelected);
            ret.add(group);
            cursor.moveToNext();
        }
        cursor.close();
        return  ret;
    }

    public static ArrayList<Group> getGroupsIds() {
        ArrayList<Group> ret = new ArrayList<>();
        String[] columns = new String[1];
        columns[0] = ID;
        Cursor cursor = mDatabase.query(false,
                GROUPSTABLE,
                columns,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i ++) {
            int id = cursor.getInt(0);
            Group group = new Group(id);
            ret.add(group);
            cursor.moveToNext();
        }
        cursor.close();
        return  ret;
    }

    public void addGroup(Group group) {
        ContentValues contentValues = Group.toContentValues(group);
        mDatabase.insert(GROUPSTABLE, null, contentValues);
    }

    public ArrayList<Post> getPosts() {
        ArrayList<Post> ret = new ArrayList<>();
        Cursor cursor = mDatabase.query(Schema.PostsTable.POSTSTABLE,
                null,
                null,
                null,
                null,
                null,
                null);

        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i ++) {
            int id = cursor.getInt(0);
            String text = cursor.getString(1);
            String url = cursor.getString(2);
            long date = cursor.getLong(3);
            int likes = cursor.getInt(4);
            int reposts = cursor.getInt(5);
            boolean isLiked = cursor.getInt(6) == 1;
            ret.add(new Post(id, text, url, date, likes, reposts, isLiked));

        }
        cursor.close();
        return ret;
    }

    public void savePosts(ArrayList<Post> posts) {
        for(Post p : posts)
            mDatabase.insert(Schema.PostsTable.POSTSTABLE, null, p.toContentValues());
    }

}
