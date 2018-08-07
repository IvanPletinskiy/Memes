package com.handen.memes;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Vanya on 29.05.2018.
 */

public class PostFetcher {
    private static int POSTQUERYCOUNT = 25;
    private static int FETCHCOUNT = 10;
    static ArrayList<Group> groups = Database.getGroupsIds();
    private int pageNumber;
    private long period = Preferences.getPeriod();

    private OnPostsSelectedListener postSelectedListener;
    private ArrayList<Item> resultItems = new ArrayList<>();
    private boolean mDone = false;

    public PostFetcher(int pageNumber, OnPostsSelectedListener listener) {
        this.pageNumber = pageNumber;
        postSelectedListener = listener;
    }

    public void fetchItems() {

        long currentMillis = new Date().getTime();
        //Стартовая дата 18:00
        long startMillis = (currentMillis - pageNumber * FETCHCOUNT * period) - period;
        //Конечная дата 18:30
        long endMillis = currentMillis - pageNumber * FETCHCOUNT * period;

        ArrayList<VKRequest> requests = new ArrayList<>();

 //       for(int i = 0; i < FETCHCOUNT; i++) { //TODO в первый раз посылается 40 запросов, это слишком много. Если g.getLastPostDownloadedMillis(), значит запускается первый раз.
            for(Group g : groups) {
                if(g.getLastPostDownloadedMillis() < endMillis) {
                    requests.add(generateRequest(g.getId(), g.getPostDownloadedCount()));
                }
            }
            startMillis -= period;
            endMillis -= period;
   //     }

        if(requests.size() > 0) {
            generateAndSendBatch(requests);
        }
    }

    private VKRequest generateRequest(int id, int postDownloadedCount) {
        return VKApi.wall().get(VKParameters.from(
                VKApiConst.OWNER_ID, id, //Владелец группы
                VKApiConst.COUNT, POSTQUERYCOUNT, //Кол-во постов
                VKApiConst.OFFSET, postDownloadedCount));
    }

    private void generateAndSendBatch(ArrayList<VKRequest> requests) {
        VKRequest[] requestsArray = requests.toArray(new VKRequest[requests.size()]);
        VKBatchRequest batchRequest = new VKBatchRequest(requestsArray);

        batchRequest.executeWithListener(new VKBatchRequest.VKBatchRequestListener() {
            @Override
            public void onComplete(VKResponse[] responses) {
                super.onComplete(responses);
                parseResponse(responses);
            }
        });
    }

    private void parseResponse(VKResponse[] responses) {
        ArrayList<Post> posts = new ArrayList<>();

        long currentMillis = new Date().getTime();
        period = Preferences.getPeriod();
        //Стартовая дата 12:30
        long startMillis = currentMillis - (pageNumber + 1) * FETCHCOUNT * period;
        //Конечная дата 18:30
        long endMillis = currentMillis - pageNumber * FETCHCOUNT * period;

        for(VKResponse resp : responses) {
            try {
                JSONArray postsArray = resp.json.getJSONObject("response").getJSONArray("items");

                long groupId = postsArray.getJSONObject(0).getLong("owner_id");

                for(Group group : groups) {
                    if(group.getId() == groupId) {
                        group.setPostDownloadedCount(group.getPostDownloadedCount() + POSTQUERYCOUNT);
                        group.setLastPostDownloadedMillis(postsArray.getJSONObject(postsArray.length() - 1).getLong("date") * 1000);
                        break;
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
                    if(postDate < startMillis) {
                        break;
                    }
                    if(checkPost(postObject)) {
                        posts.add(Post.fromJSON(postObject));
                    }
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        queryAndSelectPosts(posts);
    }

    private void queryAndSelectPosts(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                if(p1.getPostMillis() > p2.getPostMillis()) {
                    return 1;
                }
                if(p1.getPostMillis() < p2.getPostMillis()) {
                    return -1;
                }
                return 0;
            }
        });
        long currentMillis = new Date().getTime();
        long startMillis = currentMillis - (pageNumber * FETCHCOUNT * period) - period;
        ArrayList<Post> selectedPosts = new ArrayList<>();
        for(Post p : posts) {
            if(p.getPostMillis() > startMillis) {
                selectedPosts.add(p);
            }
            else {
                selectPost(selectedPosts);
                selectedPosts.clear();
                startMillis -= period;
            }
        }
        mDone = true;
        postSelectedListener.onPostSelected(resultItems);
    }

    private void selectPost(ArrayList<Post> periodPosts) {
        if(periodPosts.size() == 0) {
            return;
        }
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
        resultItems.add(Item.fromPost(periodPosts.get(maxPostIndex)));
    }

    private boolean checkPost(JSONObject postObject) throws JSONException {
        JSONArray attachments = postObject.getJSONArray(("attachments"));
        if(attachments.length() > 1) {
            return false;
        }
        JSONObject attachment = attachments.getJSONObject(0);
        //Set<String> set = attachment.getJSONObject("photo").().keySet();
        Set<String> set = new HashSet<>();
        if(!attachment.has("photo")) {
            return false;
        }
        return true; //TODO Сделать checkPost, проверять на рекламу, ссылки, слова текста и т.д. кол-во картинок
    }

    public boolean isDone() {
        return mDone;
    }

    public interface OnPostsSelectedListener {
        void onPostSelected(ArrayList<Item> items);
    }
}

