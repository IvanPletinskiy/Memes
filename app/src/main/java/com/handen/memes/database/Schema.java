package com.handen.memes.database;

/**
 * Created by user2 on 29.05.2018.
 */

public class Schema {
    public static final class GroupsTable {
        public static final String GROUPSTABLE = "groups";

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String SELECTED = "selected";
    }

    public static final class PostsTable {
        public static final String POSTSTABLE = "posts";

        public static final String ID = "id";
        public static final String TEXT = "text";
        public static final String IMAGEURL = "imageUrl";
        public static final String DATE = "date";
        public static final String LIKES = "likes";
        public static final String REPOSTS = "reposts";
        public static final String LIKED = "liked";
        public static final String IMAGE = "image";
    }
}
