package com.handen.memes;

import com.handen.memes.database.Database;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Vanya on 29.05.2018.
 */

public class PostFetcher {
    private static int POSTQUERYCOUNT = 25;
    private static int FETCHCOUNT = 10;
    static ArrayList<Group> groups = Database.getGroupsIds();

    public static ArrayList<Item>  fetchItems(int pageNumber) {
        long period = Preferences.getPeriod();
        long currentMillis = new Date().getTime();
        //Стартовая дата 18:00
        long startMillis = (currentMillis - pageNumber * FETCHCOUNT * period) - period;
        //Конечная дата 18:30
        long endMillis = currentMillis - pageNumber * FETCHCOUNT * period;

        ArrayList<VKRequest> requests = new ArrayList<>();

        for(int i = 0; i < FETCHCOUNT; i++) {

            for(Group g : groups) {
                if(g.getLastPostDownloadedMillis() < endMillis) {
                    requests.add(generateRequest(g.getId(), g.getPostDownloadedCount()));
                }
            }

            startMillis -= period;
            endMillis -= period;
        }

        if(requests.size() > 0) {
            generateAndSendBatch(requests);
        }
    }

    private static VKRequest generateRequest(int id, int postDownloadedCount) {
        return VKApi.wall().get(VKParameters.from(
                VKApiConst.OWNER_ID, id, //Владелец группы
                VKApiConst.COUNT, POSTQUERYCOUNT, //Кол-во постов
                VKApiConst.OFFSET, postDownloadedCount));
    }

    private static void generateAndSendBatch(ArrayList<VKRequest> requests) {
        VKRequest[] requestsArray = requests.toArray(new VKRequest[requests.size()]);
        VKBatchRequest batchRequest = new VKBatchRequest(requestsArray);

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
}

