package com.handen.memes;

import android.content.ContentValues;
import android.graphics.Bitmap;

import com.handen.memes.database.Schema;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by user2 on 29.05.2018.
 */

public class Post {
    int id;
    String text;
    long postMillis;
    Bitmap image;
    boolean isLiked;
    String imageUrl;
    int likes;
    int reposts;

    public Post(int id, String text, String url, long date, int likes, int reposts, boolean isLiked) {
        this.id = id;
        this.text = text;
        this.imageUrl = url;
        this.postMillis = date;
        this.likes = likes;
        this.reposts = reposts;
        this.isLiked = isLiked;
    }

    public Post(int id, String text, String url, long date, int likes, int reposts, boolean isLiked, Bitmap bitmap) {
        this.id = id;
        this.text = text;
        this.imageUrl = url;
        this.postMillis = date;
        this.likes = likes;
        this.reposts = reposts;
        this.isLiked = isLiked;
        this.image = bitmap;
    }

    public Post(int id, String text, long postMillis, String imageUrl, int likes, int reposts) {
        this.id = id;
        this.text = text;
        this.postMillis = postMillis;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.reposts = reposts;
    }

    public static Post fromJSON(JSONObject postObject) throws Exception {
        return new Post(
                postObject.getInt("id"),
                postObject.getString("text"),
                postObject.getLong("date") * 1000,
                getPostImagePath(postObject),
                countLikes(postObject),
                countReposts(postObject)
                );
    }

    private static int countReposts(JSONObject postObject) throws Exception {
        return postObject.getJSONObject("reposts").getInt("count");
    }

    private static int countLikes(JSONObject postObject) throws Exception{
        return postObject.getJSONObject("likes").getInt("count");
    }

    public static String getPostImagePath(JSONObject postObject) throws Exception {
        JSONArray attachments = postObject.getJSONArray(("attachments"));
        if (attachments.length() > 1)
            throw new Exception("У поста более одного прикрепления");
        JSONObject attachment = attachments.getJSONObject(0);

        Set<String> set = new HashSet<>();
        JSONArray values = attachment.getJSONObject("photo").names();
        for (int i = 0; i < values.length(); i++) {
            set.add(values.getString(i));
        }

        String max = "";
        int maxSum = 0;
        for (String s : set) {
            int currentSum = 0;
            if (s.contains("photo")) currentSum = getStringSum(s);
            if (currentSum > maxSum) {
                max = s;
                maxSum = currentSum;
            }
        }

        String path = (String) attachment.getJSONObject("photo").get(max);

        //       String path = (String) attachment.getJSONObject("photo").get("photo_1280");
        //String path = (String) attachment.getJSONObject("photo").get("photo_1280");
        return path;
    }

    public static int getStringSum(String s) {
        int sum = 0;
        for (char c : s.toCharArray()) {
            sum += c;
        }
        return sum;
    }

    public int getLikes() {
        return likes;
    }

    public int getReposts() {
        return reposts;
    }

    public long getPostMillis() {
        return postMillis;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public ContentValues toContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Schema.PostsTable.ID, id);
        contentValues.put(Schema.PostsTable.TEXT, text);
        contentValues.put(Schema.PostsTable.IMAGEURL, imageUrl);
        contentValues.put(Schema.PostsTable.DATE, postMillis);
        contentValues.put(Schema.PostsTable.LIKES, likes);
        contentValues.put(Schema.PostsTable.REPOSTS, reposts);
        contentValues.put(Schema.PostsTable.LIKED, isLiked ? 1 : 0);
        if(image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 0, stream);
            contentValues.put(Schema.PostsTable.IMAGE, stream.toByteArray());
        }
        return contentValues;
    }
}
