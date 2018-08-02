package com.handen.memes;

import android.graphics.Bitmap;

public class ListItem {

    private Bitmap image;
    private String text;

    public ListItem(Post post) {
        image = post.image;
        text = post.text;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getText() {
        return text;
    }
}
