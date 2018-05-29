package com.handen.memes.database;

/**
 * Created by user2 on 29.05.2018.
 */

public class Schema {
    public static final class GroupsTable {
        public static final String TABLENAME = "groups";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String SELECTED = "selected";
    }

    public static final class FavoritesTable {
        public static final String TABLENAME = "favorites";
        public static final String ID = "id";
        //TODO доделать
    }

}
