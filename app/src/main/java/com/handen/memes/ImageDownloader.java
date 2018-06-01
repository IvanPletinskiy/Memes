package com.handen.memes;

import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * Created by Vanya on 01.06.2018.
 */

public class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
    private String url;

    public ImageDownloader(String url) {
        this.url = url;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
    }
}
