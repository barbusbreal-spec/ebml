package com.eblan.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BrowserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "browser.db";
    private static final int DB_VERSION = 1;

    private static BrowserDatabase instance;

    public static synchronized BrowserDatabase getInstance(Context ctx) {
        if (instance == null) {
            instance = new BrowserDatabase(ctx.getApplicationContext());
        }
        return instance;
    }

    private BrowserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT," +
            "url TEXT UNIQUE," +
            "created_at INTEGER" +
            ")");
        db.execSQL("CREATE TABLE history (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT," +
            "url TEXT," +
            "visited_at INTEGER" +
            ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookmarks");
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public void addBookmark(String title, String url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("url", url);
        cv.put("created_at", System.currentTimeMillis());
        db.insertWithOnConflict("bookmarks", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeBookmark(long id) {
        getWritableDatabase().delete("bookmarks", "id=?", new String[]{String.valueOf(id)});
    }

    public List<BrowserItem> getBookmarks() {
        List<BrowserItem> list = new ArrayList<BrowserItem>();
        Cursor c = getReadableDatabase().query(
            "bookmarks", new String[]{"id", "title", "url", "created_at"},
            null, null, null, null, "created_at DESC"
        );
        while (c.moveToNext()) {
            list.add(new BrowserItem(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3)));
        }
        c.close();
        return list;
    }

    public void addHistory(String title, String url) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("url", url);
        cv.put("visited_at", System.currentTimeMillis());
        db.insert("history", null, cv);
        // Ограничиваем историю 500 записями
        db.execSQL("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY visited_at DESC LIMIT 500)");
    }

    public List<BrowserItem> getHistory() {
        List<BrowserItem> list = new ArrayList<BrowserItem>();
        Cursor c = getReadableDatabase().query(
            "history", new String[]{"id", "title", "url", "visited_at"},
            null, null, null, null, "visited_at DESC", "100"
        );
        while (c.moveToNext()) {
            list.add(new BrowserItem(c.getLong(0), c.getString(1), c.getString(2), c.getLong(3)));
        }
        c.close();
        return list;
    }

    public void clearHistory() {
        getWritableDatabase().delete("history", null, null);
    }

    public static class BrowserItem {
        public long id;
        public String title;
        public String url;
        public long timestamp;

        public BrowserItem(long id, String title, String url, long timestamp) {
            this.id = id;
            this.title = title;
            this.url = url;
            this.timestamp = timestamp;
        }
    }
}
