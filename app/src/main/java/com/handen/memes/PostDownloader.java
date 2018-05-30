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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Vanya on 29.05.2018.
 */

public class PostDownloader<T> extends HandlerThread {
    private static final String TAG = "PostThread";
    private static Date period;
    private Date currentDate;

    private static int POSTQUERYCOUNT = 25;

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
        final ArrayList<Post> postsPool = new ArrayList<>();
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

        ArrayList<Group> groups = Database.getGroupsNames();
        for(Group g : groups) {
            final boolean[] isDone = {false};
            final int[] i = {0};
            while (!isDone[0]) {
                VKRequest request = VKApi.wall().get(VKParameters.from(
                        VKApiConst.OWNER_ID, g.getId(),
                        VKApiConst.COUNT, 25,
                        VKApiConst.OFFSET, POSTQUERYCOUNT * i[0]));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse resp) {
                        try {
                            JSONArray postsArray = resp.json.getJSONObject("response").getJSONArray("items");
                            for(int j = 0; j < postsArray.length(); j++) {
                                JSONObject postObject = postsArray.getJSONObject(j);
                                long postDate = postObject.getLong("date");
                                if(postDate > endMillis)
                                    continue;
                                if(postDate < beginMillis) {
                                    isDone[0] = true;
                                    break;
                                }
                                if(checkPost(postObject)) {
                                    postsPool.add(Post.fromJSON(postObject));
                                }
                            }
                            if(!isDone[0]) {
                                i[0]++;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                //i++;
            }
        }

        Collections.sort(postsPool, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                long currentMillis = new Date().getTime();

                long post1Millis = currentMillis - p1.postDate;
                long post2Millis = currentMillis - p2.postDate;

                double post1Value = (((p1.likes + 1) * 100) + ((p1.reposts + 1) * 500) / post1Millis);
                double post2Value = (((p2.likes + 1) * 100) + ((p2.reposts + 1) * 500) / post2Millis);

                p1.setValue(post1Value);
                p2.setValue(post2Value);
                if (post1Value > post2Value)
                    return -1;
                else if (post2Value > post1Value)
                    return 1;
                else
                    return 0;
            }
        }););

     /*   VKRequest request = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, -460389, VKApiConst.COUNT, 1));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse resp) {
                try {
                    JSONObject body = resp.json;

                    JSONObject response = body.getJSONObject("response");
                    JSONArray postsArray = response.getJSONArray("items");
                    //            for (int i = 0; i < postsArray.length(); i++) {
                    JSONObject postObject = postsArray.getJSONObject(0);
                    long date = postObject.getLong("date");
                    JSONArray attachments = postObject.getJSONArray(("attachments"));
                    if (attachments.length() > 1) return;
                    JSONObject attachment = attachments.getJSONObject(0);
                    if (!attachment.get("type").equals("photo")) return;
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); //TODO удалить
                    StrictMode.setThreadPolicy(policy);
                    final Bitmap bitmap = downloadImage(getPostImagePath(attachment));

                    mResponseHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!mRequestMap.get(target).equals(offset)) {
                                return;
                            }
                            mRequestMap.remove(target);
                            postDownloaderListener.onPostDownloaded(target, bitmap);
                        }
                    });
                    //              }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        */
    }

    public boolean checkPost(JSONObject postObject) {
        return true; //TODO Сделать checkPost, проверять на рекламу, ссылки, слова текста и т.д. кол-во картинок
    }

    public String getPostImagePath(JSONObject attachment) throws JSONException {
        //Set<String> set = attachment.getJSONObject("photo").().keySet();
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
        // String path = (JSONObject)values.get().toString();
        String path = (String) attachment.getJSONObject("photo").get(max);

        return path;
    }

    public int getStringSum(String s) {
        int sum = 0;
        for (char c : s.toCharArray()) {
            sum += c;
        }
        return sum;
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
