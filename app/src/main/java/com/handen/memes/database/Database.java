package com.handen.memes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.handen.memes.App;
import com.handen.memes.Group;
import com.handen.memes.Item;
import com.handen.memes.Post;

import java.util.ArrayList;

import static com.handen.memes.database.Schema.GroupsTable.GROUPSTABLE;
import static com.handen.memes.database.Schema.GroupsTable.ID;
import static com.handen.memes.database.Schema.GroupsTable.NAME;
import static com.handen.memes.database.Schema.GroupsTable.SELECTED;
import static com.handen.memes.database.Schema.ImageTable.IMAGETABLE;

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
        String[] columns = new String[2];
        columns[0] = ID;
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
            int id = cursor.getInt(0);
            int selected = cursor.getInt(1);
            Group group = new Group(id, selected == 1);
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

    public static ArrayList<Post> getPosts() {
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
            cursor.moveToNext();
        }
        cursor.close();
        return ret;
    }

    public static ArrayList<Post> getLikedPosts() {
        ArrayList<Post> ret = new ArrayList<>();
        Cursor cursor = mDatabase.query(Schema.PostsTable.POSTSTABLE,
                null,
                "liked = 1",
                null,
                null,
                null,
                null
                );
        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++) {
            int id = cursor.getInt(0);
            String text = cursor.getString(1);
            String url = cursor.getString(2);
            long date = cursor.getLong(3);
            int likes = cursor.getInt(4);
            int reposts = cursor.getInt(5);
            boolean isLiked = cursor.getInt(6) == 1;
            byte[] array = cursor.getBlob(7);
            Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);

            ret.add(new Post(id, text, url, date, likes, reposts, isLiked, bitmap));
            cursor.moveToNext();
        }
        cursor.close();
        return ret;
    }

    public static ArrayList<Integer> getLikedPostsIds() {
        ArrayList<Integer> ret = new ArrayList<>();
        String[] columns = new String[1];
        columns[0] = Schema.PostsTable.ID;
        Cursor cursor = mDatabase.query(Schema.PostsTable.POSTSTABLE,
                columns,
                "liked = 1",
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++) {
            ret.add(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();
        return ret;
    }

    public static void saveLikedPost(Post post) {
            mDatabase.insert(Schema.PostsTable.POSTSTABLE, null, post.toContentValues());
    }

    public static void deleteLikedPost(Post post) {
        mDatabase.delete(Schema.PostsTable.POSTSTABLE, "id = " + post.getId(), null);
    }

    public static boolean containsImage(String imageUrl) {
        String[] columns = new String[1];
        columns[0] = Schema.ImageTable.IMAGEURL;
        Cursor cursor = mDatabase.query(IMAGETABLE,
                columns,
                Schema.ImageTable.IMAGEURL + " = '" +  imageUrl + "'",
                null,
                null,
                null,
                null
                );
        boolean isNotEmpty = cursor.moveToFirst();
        cursor.close();
        return isNotEmpty;
    }

    public static void saveImage(Item item, Bitmap image) {
        mDatabase.insert(Schema.ImageTable.IMAGETABLE, null, item.toContentValues(image));
    }
}
