package com.handen.memes;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;

import com.handen.memes.database.Database;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Vanya on 29.05.2018.
 */

public class PostDownloader<T> extends HandlerThread implements PostsPreparedListener {
    private static final String TAG = "PostThread";

    private static int POSTQUERYCOUNT = 25;

    ArrayList<Group> groups;

    private static final int MESSSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<T, Integer> mRequestMap = new ConcurrentHashMap<>();
    private PostDownloaderListener<T> postDownloaderListener;

    private ArrayList<Post> postsPool;

    public interface PostDownloaderListener<T> {
        void onPostDownloaded(T target, Post post);
    }

    public void setPostDownloaderListener(PostDownloaderListener<T> listener) {
        postDownloaderListener = listener;
    }

    public PostDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        groups = Database.getGroupsIds();
        postsPool = Database.getPosts();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSSAGE_DOWNLOAD);
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

    public void getPost(T target, @Nullable Integer offset) {

        if(offset == null) {
            mRequestMap.remove(target);
        }
        else {
            mRequestMap.put(target, offset);
            mRequestHandler.obtainMessage(MESSSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    private void handleRequest(T target) {
        int offset = mRequestMap.get(target);
        long currentMillis = new Date().getTime();
        long period = Preferences.getPeriod();
        long endPeriodMillis = currentMillis - (offset + 1) * period;

        ArrayList<Group> groups = Database.getGroupsIds();

        ArrayList<VKRequest> requests = new ArrayList<>();
        for(Group g : groups) {
            if(g.getLastPostDownloadedMillis() < endPeriodMillis) {
                requests.add(generateRequest(g.getId(), g.getPostDownloadedCount()));
            }
        }

        if(requests.size() > 0) {
            generateAndSendBatch(requests, target);
        }
        else {
            onPostsPrepared(target);
        }
    }

    private void generateAndSendBatch(ArrayList<VKRequest> requests, final T target) {
        VKRequest[] requestsArray = requests.toArray(new VKRequest[requests.size()]);
        VKBatchRequest batchRequest = new VKBatchRequest(requestsArray);

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

        batchRequest.executeWithListener(new VKBatchRequest.VKBatchRequestListener() {
            @Override
            public void onComplete(VKResponse[] responses) {
                super.onComplete(responses);

                for(VKResponse resp : responses) {
                    try {
                        JSONArray postsArray = resp.json.getJSONObject("response").getJSONArray("items");

                        long groupId = postsArray.getJSONObject(0).getLong("owner_id");

                        for(Group group : groups) {
                            if(group.getId() == groupId) {
                                group.setPostDownloadedCount(group.getPostDownloadedCount() + POSTQUERYCOUNT);
                                group.setLastPostDownloadedMillis(postsArray.getJSONObject(postsArray.length() - 1).getLong("date") * 1000);
                            }
                        }

                        //Каждый пост мы проверяем, подходит ли он и добавляем в postsPool
                        for(int j = 0; j < postsArray.length(); j++) {
                            JSONObject postObject = postsArray.getJSONObject(j);
                            if(postObject.has("is_pinned")) {
                                if(postObject.getInt("is_pinned") == 1) {
                                    continue;
                                }
                            }
                            //Умножаем на 1000, т.к. ВК возвращает секунды, а не милли
                            long postDate = postObject.getLong("date") * 1000;
                            if(postDate > endMillis) {
                                continue;
                            }
                            else {
                                if(postDate < beginMillis) {
                                    break;
                                }
                                else {
                                    if(checkPost(postObject)) {
                                        postsPool.add(Post.fromJSON(postObject));
                                    }
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                onPostsPrepared(target);
            }
        });
    }

    private boolean checkPost(JSONObject postObject) throws JSONException {
        JSONArray attachments = postObject.getJSONArray(("attachments"));
        if (attachments.length() > 1)
            return false;
        JSONObject attachment = attachments.getJSONObject(0);
        //Set<String> set = attachment.getJSONObject("photo").().keySet();
        Set<String> set = new HashSet<>();
        if(!attachment.has("photo"))
            return false;
        return true; //TODO Сделать checkPost, проверять на рекламу, ссылки, слова текста и т.д. кол-во картинок
    }

    private VKRequest generateRequest(int id, int postDownloadedCount) {
        return VKApi.wall().get(VKParameters.from(
                VKApiConst.OWNER_ID, id, //Владелец группы
                VKApiConst.COUNT, POSTQUERYCOUNT, //Кол-во постов
                VKApiConst.OFFSET, postDownloadedCount));
    }

    @Override
    public void onPostsPrepared(Object target) {
        int offset = mRequestMap.get((T) target);
        Post post = choosePost(getPeriodPosts(offset));
        new ImageDownloader(post, (T) target).execute();
    }

    private Post choosePost(ArrayList<Post> periodPosts) {
        double maxValue = 0;
        int maxPostIndex = 0;
        for(int i = 0; i < periodPosts.size(); i++) {
            long millis = new Date().getTime();
            Post post = periodPosts.get(i);
            long post1Millis = millis - post.getPostMillis();

            double postValue = (((post.getLikes() + 1) * 100) + ((post.getReposts() + 1) * 500) / post1Millis);

            if(i == 0) {
                maxValue = postValue;
            }
            if(postValue > maxValue) {
                maxPostIndex = i;
            }
        }
        return periodPosts.get(maxPostIndex);
    }

    private ArrayList<Post> getPeriodPosts(Integer offset) {
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
        ArrayList<Post> ret = new ArrayList<>();
        for(Post p : postsPool) {
            if(p.getPostMillis() > beginMillis && p.getPostMillis() < endMillis)
                ret.add(p);
        }
        return ret;
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

    private class ImageDownloader extends AsyncTask<Void, Void, Post> {
        private Post post;
        private T target;
        private Integer offset;

        public ImageDownloader(Post post, T target) {
            this.post = post;
            this.target = target;
            this.offset = mRequestMap.get(target);
        }

        @Override
        protected Post doInBackground(Void... voids) {
            post.setImage(downloadImage(post.getImageUrl()));
            return post;
        }

        @Override
        protected void onPostExecute(final Post post) {
            super.onPostExecute(post);
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != (offset)) {
                        return;
                    }
                    mRequestMap.remove(target);
                    postDownloaderListener.onPostDownloaded(target, post);
                }
            });
        }
    }
}

