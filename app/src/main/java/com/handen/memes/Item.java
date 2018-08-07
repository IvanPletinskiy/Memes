package com.handen.memes;

import android.content.ContentValues;
import android.graphics.Bitmap;

import com.handen.memes.database.Schema;

import java.io.ByteArrayOutputStream;

public class Item {
    private String url;
    private String imageSize;
    private String text;

    public Item(String url, String imageSize, String text) {
        this.url = url;
        this.imageSize = imageSize;
        this.text = text;
    }

    public static Item fromPost(Post post) {
        return new Item(
                post.getImageUrl(),
                "dummyImageSize",
                post.getText()
        );
    }

    public String getUrl() {
        return url;
    }

    public String getImageSize() {
        return imageSize;
    }

    public String getText() {
        return text;
    }


    public ContentValues toContentValues(Bitmap image) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.ImageTable.IMAGEURL, url);
        contentValues.put(Schema.ImageTable.IMAGESIZE, imageSize);
        if(image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 0, stream);
            contentValues.put(Schema.PostsTable.IMAGE, stream.toByteArray());
        }
        return contentValues;
    }
}
