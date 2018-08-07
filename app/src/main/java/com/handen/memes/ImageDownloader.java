package com.handen.memes;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Vanya on 01.06.2018.
 */

public class ImageDownloader<T> extends HandlerThread {
    private static final String TAG = "ImageThread";
    private static final int MESSSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private static final int MESSAGE_DOWNLOAD = 0;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ImageDownloadListener<T> imageDownloadListener;

    public ImageDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    public void setImageDownloadListener(ImageDownloadListener<T> listener) {
        imageDownloadListener = listener;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MESSSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };
    }

    public void addRequest(T target, String url) {
        if(url == null) {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    private void handleRequest(T target) {
        String url = mRequestMap.get(target);
        downloadImage(target, url);
    }

    private void downloadImage(T target, String path) {
        try {
            java.net.URL url = new java.net.URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            imageDownloadListener.onImageDownloaded(target , myBitmap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ImageDownloadListener<T> {
        void onImageDownloaded(T target, Bitmap image);
    }
}
