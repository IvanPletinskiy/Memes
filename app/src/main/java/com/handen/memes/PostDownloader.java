package com.handen.memes;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.handen.memes.database.Database;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Vanya on 29.05.2018.
 */

public class PostDownloader<T> extends HandlerThread {
    private static final String TAG = "PostThread";
    private static Date period;
    private Date currentDate;

    private static int POSTQUERYCOUNT = 100;

    private static final int MESSSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, Integer> mRequestMap = new ConcurrentHashMap<>();
    private PostDownloaderListener<T> postDownloaderListener;

    public interface PostDownloaderListener<T> {
        void onPostDownloaded(T target, Bitmap icon);
    }

    public void setPostDownloaderListener(PostDownloaderListener<T> listener) {
        postDownloaderListener = listener;
    }

    public PostDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    /*
        Для каждого промежутка от текущей даты мы получаем все посты групп, выбираем самым популярный, скачиваем картинку для него
    */

    public static void getPosts() {

    }

    public static void downloadImage() {

    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSSAGE_DOWNLOAD);
    }

    @SuppressLint ("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };
    }

    public void addToQueue(T target, int offset) {

        if (offset == -1) {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, offset);
            mRequestHandler.obtainMessage(MESSSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    private void handleRequest(final T target) {
        final ArrayList<Post> postsPool = new ArrayList<>(); //Список постов за период времени из ВСЕХ групп, отсюда выбирается лучший пост
        final int offset = mRequestMap.get(target);
        long currentMillis = new Date().getTime();
        long period = Preferences.getPeriod();
        /**
         Стартовая дата, например 18:00
         */
        final long beginMillis = currentMillis - (offset + 1) * period;
        /**
         Конечная дата, например 18:30
         */
        final long endMillis = currentMillis - offset * period;

        ArrayList<Group> groups = Database.getGroupsIds(); //Список груп, откуда берутся посты
        for (Group g : groups) { //Для каждой группы делаем запрос
            final int[] i = {0};
            VKRequest request = VKApi.wall().get(VKParameters.from(
                    VKApiConst.OWNER_ID, g.getId(), //Владелец группы
                    VKApiConst.COUNT, POSTQUERYCOUNT, //Кол-во постов
                    VKApiConst.OFFSET, POSTQUERYCOUNT * i[0]));
            request.executeWithListener(new VKRequest.VKRequestListener() { //Выполняем запрос, ответ приходит спустя время в onComplete
                @Override
                public void onComplete(VKResponse resp) {
                    try {
                        JSONArray postsArray = resp.json.getJSONObject("response").getJSONArray("items");
                        //Каждый пост мы проверяем, подходит ли он и добавляем в postsPool
                        for (int j = 0; j < postsArray.length(); j++) {
                            JSONObject postObject = postsArray.getJSONObject(j);
                            if (postObject.has("is_pinned")) {
                                if (postObject.getInt("is_pinned") == 1)
                                    continue;
                            }
                            //Умножаем на 1000, т.к. ВК возвращает секунды, а не милли
                            long postDate = postObject.getLong("date") * 1000;
                            if (postDate > endMillis) {
                                continue;
                            }
                            else {
                                if (postDate < beginMillis) {
                                    break;
                                }
                                else {
                                    if (checkPost(postObject)) {
                                        postsPool.add(Post.fromJSON(postObject));
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (postsPool.size() != 0) { //Выбираем лучший пост и скачиваем его
            double maxValue = 0;
            int maxPostIndex = 0;
            for (int i = 0; i < postsPool.size(); i++) {
                long millis = new Date().getTime();
                Post post = postsPool.get(i);
                long post1Millis = millis - post.getPostMillis();

                double postValue = (((post.getLikes() + 1) * 100) + ((post.getReposts() + 1) * 500) / post1Millis);

                if (i == 0)
                    maxValue = postValue;
                if (postValue > maxValue)
                    maxPostIndex = i;
            }

            final Bitmap bitmap = downloadImage(postsPool.get(maxPostIndex).imageUrl);
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != (offset)) {
                        return;
                    }
                    mRequestMap.remove(target);
                    postDownloaderListener.onPostDownloaded(target, bitmap);
                }
            });
        }
    }

    public boolean checkPost(JSONObject postObject) {
        return true; //TODO Сделать checkPost, проверять на рекламу, ссылки, слова текста и т.д. кол-во картинок
    }

    public Bitmap downloadImage(String path) {
        try {
            java.net.URL url = new java.net.URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
