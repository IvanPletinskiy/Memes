package com.handen.memes;

public class Item {
    private String Url;
    private String imageSize;
    private String text;

    public Item(String url, String imageSize, String text) {
        Url = url;
        this.imageSize = imageSize;
        this.text = text;
    }
}
