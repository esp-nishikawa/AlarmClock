package com.esp.android.alarmclock;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.preference.PreferenceManager;

/**
 * データ管理クラス
 */
public class DataManager {

    // DB名
    static final String DATABASE_NAME = "alarmclock.db";

    // DBバージョン
    static final int DATABASE_VERSION = 4;

    // 音楽テーブル
    public static final String MUSICITEM_TABLE_NAME = "musicitem";
    public static final String MUSICITEM_COL_CONTENT = "_content";
    public static final String MUSICITEM_COL_ID = "_id";
    public static final String MUSICITEM_COL_ARTIST = "artist";
    public static final String MUSICITEM_COL_TITLE = "title";
    public static final String MUSICITEM_COL_DURATION = "duration";
    public static final String MUSICITEM_COL_TYPE = "type";

    // ブックマークテーブル
    public static final String BOOKMARK_TABLE_NAME = "bookmark";
    public static final String BOOKMARK_COL_PATH = "_path";
    public static final String BOOKMARK_COL_CONTENT = "content";
    public static final String BOOKMARK_COL_ID = "id";

    // アラーム設定テーブル
    public static final String ALARM_SETTING_TABLE_NAME = "alarm_setting";
    public static final String ALARM_SETTING_COL_ID = "_id";
    public static final String ALARM_SETTING_COL_ON_OFF = "on_off";
    public static final String ALARM_SETTING_COL_TITLE = "title";
    public static final String ALARM_SETTING_COL_REPEAT = "repeat";
    public static final String ALARM_SETTING_COL_WEEK = "week";
    public static final String ALARM_SETTING_COL_YEAR = "year";
    public static final String ALARM_SETTING_COL_MONTH = "month";
    public static final String ALARM_SETTING_COL_DAY = "day";
    public static final String ALARM_SETTING_COL_HOUR = "hour";
    public static final String ALARM_SETTING_COL_MINUTE = "minute";
    public static final String ALARM_SETTING_COL_MUSIC_CONTENT = "music_content";
    public static final String ALARM_SETTING_COL_MUSIC_ID = "music_id";
    public static final String ALARM_SETTING_COL_MUSIC_PATH = "music_path";
    public static final String ALARM_SETTING_COL_MUSIC_VOLUME = "music_volume";
    public static final String ALARM_SETTING_COL_MUSIC_LENGTH = "music_length";
    public static final String ALARM_SETTING_COL_VOICE = "voice";
    public static final String ALARM_SETTING_COL_VIBRATOR = "vibrator";
    public static final String ALARM_SETTING_COL_SNOOZE_MODE = "snooze_mode";
    public static final String ALARM_SETTING_COL_SNOOZE_LENGTH = "snooze_length";
    public static final String ALARM_SETTING_COL_SNOOZE_TIMES = "snooze_times";

    // エラー情報テーブル
    public static final String ERROR_INF_TABLE_NAME = "error_inf";
    public static final String ERROR_INF_COL_ID = "_id";
    public static final String ERROR_INF_COL_TEXT = "text";

    // プリファレンス保存時のキー名
    private final static String KEY_SNOOZE_REMAIN_TIMES = "snooze_remain_times";
    private final static String KEY_ALARM_KEY = "alarm_key";

    // コンテキスト
    private final Context mContext;

    // DB用メンバ変数
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * SQLiteOpenHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        /**
         * このデータベースを初めて使用する時に実行される処理
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                // 音楽テーブル作成
                db.execSQL(
                    "CREATE TABLE " + MUSICITEM_TABLE_NAME + " ("
                    + MUSICITEM_COL_CONTENT + " INTEGER,"
                    + MUSICITEM_COL_ID + " INTEGER,"
                    + MUSICITEM_COL_TYPE + " INTEGER,"
                    + MUSICITEM_COL_ARTIST + " TEXT,"
                    + MUSICITEM_COL_TITLE + " TEXT,"
                    + MUSICITEM_COL_DURATION + " INTEGER, PRIMARY KEY("
                    + MUSICITEM_COL_CONTENT + ", "
                    + MUSICITEM_COL_ID + "));"
                );

                // ブックマークテーブル作成
                db.execSQL(
                    "CREATE TABLE " + BOOKMARK_TABLE_NAME + " ("
                    + BOOKMARK_COL_PATH + " TEXT PRIMARY KEY,"
                    + BOOKMARK_COL_CONTENT + " INTEGER,"
                    + BOOKMARK_COL_ID + " INTEGER);"
                );

                // アラーム設定テーブル作成
                db.execSQL(
                    "CREATE TABLE " + ALARM_SETTING_TABLE_NAME + " ("
                    + ALARM_SETTING_COL_ID + " INTEGER PRIMARY KEY,"
                    + ALARM_SETTING_COL_ON_OFF + " INTEGER NOT NULL,"
                    + ALARM_SETTING_COL_TITLE + " TEXT NOT NULL,"
                    + ALARM_SETTING_COL_REPEAT + " TEXT NOT NULL,"
                    + ALARM_SETTING_COL_WEEK + " INTEGER,"
                    + ALARM_SETTING_COL_YEAR + " INTEGER,"
                    + ALARM_SETTING_COL_MONTH + " INTEGER,"
                    + ALARM_SETTING_COL_DAY + " INTEGER,"
                    + ALARM_SETTING_COL_HOUR + " INTEGER,"
                    + ALARM_SETTING_COL_MINUTE + " INTEGER,"
                    + ALARM_SETTING_COL_MUSIC_CONTENT + " INTEGER,"
                    + ALARM_SETTING_COL_MUSIC_ID + " INTEGER,"
                    + ALARM_SETTING_COL_MUSIC_PATH + " TEXT,"
                    + ALARM_SETTING_COL_MUSIC_VOLUME + " INTEGER,"
                    + ALARM_SETTING_COL_MUSIC_LENGTH + " INTEGER,"
                    + ALARM_SETTING_COL_VOICE + " INTEGER,"
                    + ALARM_SETTING_COL_VIBRATOR + " INTEGER,"
                    + ALARM_SETTING_COL_SNOOZE_MODE + " TEXT,"
                    + ALARM_SETTING_COL_SNOOZE_LENGTH + " INTEGER,"
                    + ALARM_SETTING_COL_SNOOZE_TIMES + " INTEGER);"
                );

                // エラー情報テーブル作成
                db.execSQL(
                    "CREATE TABLE " + ERROR_INF_TABLE_NAME + " ("
                    + ERROR_INF_COL_ID + " INTEGER PRIMARY KEY,"
                    + ERROR_INF_COL_TEXT + " TEXT);"
                );
            } catch (Exception e) {
                AlarmClockApp.outputError(mContext, "DB create error!", e, true, false);
            }
        }

        /**
         * アプリケーションの更新などによって、データベースのバージョンが上がった場合に実行される処理
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + MUSICITEM_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ALARM_SETTING_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ERROR_INF_TABLE_NAME);
                clearPreferences(mContext);
            } catch (Exception e) {
                AlarmClockApp.outputError(mContext, "DB upgrade error!", e, true, false);
            }
            onCreate(db);
        }

        /**
         * アプリケーションの更新などによって、データベースのバージョンが下がった場合に実行される処理
         */
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + MUSICITEM_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ALARM_SETTING_TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ERROR_INF_TABLE_NAME);
                clearPreferences(mContext);
            } catch (Exception e) {
                AlarmClockApp.outputError(mContext, "DB downgrade error!", e, true, false);
            }
            onCreate(db);
        }
    }

    /**
     * コンストラクタ
     * @param context コンテキスト
     */
    public DataManager(Context context){
        mContext = context;
        mDbHelper = new DatabaseHelper(context);
    }

    /**
     * 音楽テーブル追加
     * @param items アイテムのリスト
     * @return 追加した場合はtrue
     */
    public boolean insertMusicItems(List<MusicItem> items) {
        long id = -1;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            // SQL作成
            SQLiteStatement stmt = mDb.compileStatement("insert into " + MUSICITEM_TABLE_NAME + "(" +
                                                         MUSICITEM_COL_CONTENT + "," +
                                                         MUSICITEM_COL_ID + "," +
                                                         MUSICITEM_COL_TYPE + "," +
                                                         MUSICITEM_COL_ARTIST + "," +
                                                         MUSICITEM_COL_TITLE + "," +
                                                         MUSICITEM_COL_DURATION + ") values (?,?,?,?,?,?);"); 

            // データ追加
            Iterator<MusicItem> itr = items.iterator(); 
            while(itr.hasNext()){ 
                MusicItem item = (MusicItem)itr.next();
                stmt.bindLong(1, item.content);
                stmt.bindLong(2, item.id);
                stmt.bindLong(3, item.type);
                stmt.bindString(4, item.artist);
                stmt.bindString(5, item.title);
                stmt.bindLong(6, item.duration);
                id = stmt.executeInsert();
                if (id < 0) {
                    AlarmClockApp.outputError(mContext, "Insert music items error!", null, true, false);
                    break;
                }
            }

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Insert music items error!", e, true, false);
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (id < 0) {
            return false;
        }

        return true;
    }

    /**
     * 音楽テーブル取得
     * @param type メディアの種類
     * @param artist アーティスト
     * @return アイテムのリスト
     */
    public List<MusicItem> selectMusicItems(int type, String artist) {
        List<MusicItem> items = new LinkedList<MusicItem>();

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c;
            if (artist == null || artist.length() == 0) {
                c = mDb.query(MUSICITEM_TABLE_NAME, null,
                        MUSICITEM_COL_TYPE + " = ?",
                        new String[]{String.valueOf(type)}, null, null, null);
            } else {
                c = mDb.query(MUSICITEM_TABLE_NAME, null,
                        MUSICITEM_COL_TYPE + " = ? and " + MUSICITEM_COL_ARTIST + " = ?",
                        new String[]{String.valueOf(type), artist}, null, null, null);
            }

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    do {
                        MusicItem item = new MusicItem(
                            c.getInt(c.getColumnIndex(MUSICITEM_COL_CONTENT)),
                            c.getLong(c.getColumnIndex(MUSICITEM_COL_ID)),
                            c.getInt(c.getColumnIndex(MUSICITEM_COL_TYPE)),
                            c.getString(c.getColumnIndex(MUSICITEM_COL_ARTIST)),
                            c.getString(c.getColumnIndex(MUSICITEM_COL_TITLE)),
                            c.getLong(c.getColumnIndex(MUSICITEM_COL_DURATION))
                        );
                        items.add(item);
                    } while(c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select music items error!", e, true, false);
            items = null;
        } finally {
            // DBクローズ
            close();
        }
        return items;
    }

    /**
     * 音楽テーブル取得（1件）
     * @param content コンテンツ
     * @param id ID
     * @return アイテム
     */
    public MusicItem selectMusicItem(int content, long id) {
        MusicItem item = null;

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(MUSICITEM_TABLE_NAME, null,
                    MUSICITEM_COL_CONTENT + " = ? and " + MUSICITEM_COL_ID + " = ?",
                    new String[]{String.valueOf(content), String.valueOf(id)}, null, null, null);

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    item = new MusicItem(
                        c.getInt(c.getColumnIndex(MUSICITEM_COL_CONTENT)),
                        c.getLong(c.getColumnIndex(MUSICITEM_COL_ID)),
                        c.getInt(c.getColumnIndex(MUSICITEM_COL_TYPE)),
                        c.getString(c.getColumnIndex(MUSICITEM_COL_ARTIST)),
                        c.getString(c.getColumnIndex(MUSICITEM_COL_TITLE)),
                        c.getLong(c.getColumnIndex(MUSICITEM_COL_DURATION))
                    );
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select music item error!", e, true, false);
            item = null;
        } finally {
            // DBクローズ
            close();
        }
        return item;
    }

    /**
     * 音楽テーブル削除
     * @return 削除した場合はtrue
     */
    public boolean deleteMusicItems() {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }
        
        try {
            // トランザクション開始
            mDb.beginTransaction();
            
            // データ削除
            num = mDb.delete(MUSICITEM_TABLE_NAME, null, null);
            
            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Delete music items error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * アーティスト一覧取得
     * @return アーティストのリスト
     */
    public List<String> selectMusicArtists() {
        List<String> artists = new LinkedList<String>();

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(MUSICITEM_TABLE_NAME, new String[]{MUSICITEM_COL_ARTIST},
                    MUSICITEM_COL_TYPE + " = ?",
                    new String[]{String.valueOf(MusicItem.TYPE_MUSIC)},
                    MUSICITEM_COL_ARTIST, null, null);

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    do {
                        String artist = c.getString(c.getColumnIndex(MUSICITEM_COL_ARTIST));
                        artists.add(artist);
                    } while(c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select music artists error!", e, true, false);
            artists = null;
        } finally {
            // DBクローズ
            close();
        }
        return artists;
    }

    /**
     * ブックマークテーブル追加
     * @param musicKey 音楽のキー
     * @return 追加した場合はtrue
     */
    public boolean insertBookmark(MusicKey musicKey) {
        long id = -1;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            // SQL作成
            SQLiteStatement stmt = mDb.compileStatement("insert into " + BOOKMARK_TABLE_NAME + "(" +
                                                         BOOKMARK_COL_PATH + "," +
                                                         BOOKMARK_COL_CONTENT + "," +
                                                         BOOKMARK_COL_ID +
                                                         ") values (?,?,?);"); 

            // データ追加
            stmt.bindString(1, musicKey.path);
            stmt.bindLong(2, musicKey.content);
            stmt.bindLong(3, musicKey.id);
            id = stmt.executeInsert();
            if (id < 0) {
                AlarmClockApp.outputError(mContext, "Insert bookmark error!", null, true, false);
            }

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Insert bookmark error!", e, true, false);
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (id < 0) {
            return false;
        }

        return true;
    }

    /**
     * ブックマークテーブル取得（全件）
     * @return 音楽のキーのリスト
     */
    public List<MusicKey> selectBookmarks() {
        List<MusicKey> musicKeys = new LinkedList<MusicKey>();

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(BOOKMARK_TABLE_NAME, null, null, null, null, null, null);  

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    do {
                        MusicKey musicKey = new MusicKey();
                        musicKey.path = c.getString(c.getColumnIndex(BOOKMARK_COL_PATH));
                        musicKey.content = c.getInt(c.getColumnIndex(BOOKMARK_COL_CONTENT));
                        musicKey.id = c.getLong(c.getColumnIndex(BOOKMARK_COL_ID));
                        musicKeys.add(musicKey);
                    } while(c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select bookmarks error!", e, true, false);
            musicKeys = null;
        } finally {
            // DBクローズ
            close();
        }
        return musicKeys;
    }

    /**
     * ブックマークテーブル取得（1件）
     * @param offset 行番号
     * @return 音楽のキー
     */
    public MusicKey selectBookmark(int offset) {
        MusicKey musicKey = null;

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(BOOKMARK_TABLE_NAME, null,
                    null, null, null, null, null,
                    String.valueOf(offset) + ", 1");

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    musicKey = new MusicKey();
                    musicKey.path = c.getString(c.getColumnIndex(BOOKMARK_COL_PATH));
                    musicKey.content = c.getInt(c.getColumnIndex(BOOKMARK_COL_CONTENT));
                    musicKey.id = c.getLong(c.getColumnIndex(BOOKMARK_COL_ID));
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select bookmark error!", e, true, false);
            musicKey = null;
        } finally {
            // DBクローズ
            close();
        }
        return musicKey;
    }

    /**
     * ブックマークテーブル取得（1件）
     * @param path パス
     * @return 音楽のキー
     */
    public MusicKey selectBookmark(String path) {
        MusicKey musicKey = null;

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(BOOKMARK_TABLE_NAME, null,
                    BOOKMARK_COL_PATH + " = ?", new String[]{path}, null, null, null);

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    musicKey = new MusicKey();
                    musicKey.path = c.getString(c.getColumnIndex(BOOKMARK_COL_PATH));
                    musicKey.content = c.getInt(c.getColumnIndex(BOOKMARK_COL_CONTENT));
                    musicKey.id = c.getLong(c.getColumnIndex(BOOKMARK_COL_ID));
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select bookmark error!", e, true, false);
            musicKey = null;
        } finally {
            // DBクローズ
            close();
        }
        return musicKey;
    }

    /**
     * ブックマークテーブル更新
     * @param path パス
     * @param musicKey 音楽のキー
     * @return 更新した場合はtrue
     */
    public boolean updateBookmark(String path, MusicKey musicKey) {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            //入力するデータ生成
            ContentValues values = new ContentValues();
            values.put(BOOKMARK_COL_PATH, musicKey.path);
            values.put(BOOKMARK_COL_CONTENT, musicKey.content);
            values.put(BOOKMARK_COL_ID, musicKey.id);

            // データ更新
            num = mDb.update(BOOKMARK_TABLE_NAME, values,
                BOOKMARK_COL_PATH + " = ?", new String[]{path});

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Update bookmark error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * ブックマークテーブル削除
     * @param path パス
     * @return 削除した場合はtrue
     */
    public boolean deleteBookmark(String path) {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            // データ削除
            num = mDb.delete(BOOKMARK_TABLE_NAME,
                BOOKMARK_COL_PATH + " = ?", new String[]{path});

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Delete bookmark error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * ブックマークテーブル件数取得
     * @return 件数
     */
    public long getBookmarksCount() {
        long count = 0;

        // DBオープン
        if (!open(false)) {
            return 0;
        }

        try {
            // 件数取得
            Cursor c = mDb.rawQuery("select count(*) from " + BOOKMARK_TABLE_NAME, null);

            // 件数の取得
            if (c != null) {
                if(c.moveToLast()){
                    count = c.getLong(0);
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Count bookmarks error!", e, true, false);
        } finally {
            // DBクローズ
            close();
        }
        return count;
    }

    /**
     * アラーム設定テーブル追加
     * @param setting アラーム設定
     * @return 追加した場合はtrue
     */
    public boolean insertAlarmSetting(AlarmSetting setting) {
        long id = -1;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            // SQL作成
            SQLiteStatement stmt = mDb.compileStatement("insert into " + ALARM_SETTING_TABLE_NAME + "(" +
                                                         ALARM_SETTING_COL_ID + "," +
                                                         ALARM_SETTING_COL_ON_OFF + "," +
                                                         ALARM_SETTING_COL_TITLE + "," +
                                                         ALARM_SETTING_COL_REPEAT + "," +
                                                         ALARM_SETTING_COL_WEEK + "," +
                                                         ALARM_SETTING_COL_YEAR + "," +
                                                         ALARM_SETTING_COL_MONTH + "," +
                                                         ALARM_SETTING_COL_DAY + "," +
                                                         ALARM_SETTING_COL_HOUR + "," +
                                                         ALARM_SETTING_COL_MINUTE + "," +
                                                         ALARM_SETTING_COL_MUSIC_CONTENT + "," +
                                                         ALARM_SETTING_COL_MUSIC_ID + "," +
                                                         ALARM_SETTING_COL_MUSIC_PATH + "," +
                                                         ALARM_SETTING_COL_MUSIC_VOLUME + "," +
                                                         ALARM_SETTING_COL_MUSIC_LENGTH + "," +
                                                         ALARM_SETTING_COL_VOICE + "," +
                                                         ALARM_SETTING_COL_VIBRATOR + "," +
                                                         ALARM_SETTING_COL_SNOOZE_MODE + "," +
                                                         ALARM_SETTING_COL_SNOOZE_LENGTH + "," +
                                                         ALARM_SETTING_COL_SNOOZE_TIMES +
                                                         ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);"); 

            // データ追加
            stmt.bindLong(1, setting.id);
            stmt.bindLong(2, setting.onOff);
            stmt.bindString(3, setting.title);
            stmt.bindString(4, setting.repeat.toString());
            stmt.bindLong(5, AlarmSetting.weekSetToInt(setting.week));
            stmt.bindLong(6, setting.ymd.year);
            stmt.bindLong(7, setting.ymd.month);
            stmt.bindLong(8, setting.ymd.day);
            stmt.bindLong(9, setting.hour);
            stmt.bindLong(10, setting.minute);
            stmt.bindLong(11, setting.musicKey.content);
            stmt.bindLong(12, setting.musicKey.id);
            stmt.bindString(13, setting.musicKey.path);
            stmt.bindLong(14, setting.musicVolume);
            stmt.bindLong(15, setting.musicLength);
            stmt.bindLong(16, setting.voice);
            stmt.bindLong(17, setting.vibrator);
            stmt.bindString(18, setting.snoozeMode.toString());
            stmt.bindLong(19, setting.snoozeLength);
            stmt.bindLong(20, setting.snoozeTimes);
            id = stmt.executeInsert();
            if (id < 0) {
                AlarmClockApp.outputError(mContext, "Insert alarm setting error!", null, true, false);
            }

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Insert alarm setting error!", e, true, false);
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (id < 0) {
            return false;
        }

        return true;
    }

    /**
     * アラーム設定テーブル取得（全件）
     * @return アラーム設定のリスト
     */
    public List<AlarmSetting> selectAlarmSettings() {
        List<AlarmSetting> settings = new LinkedList<AlarmSetting>();

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(ALARM_SETTING_TABLE_NAME, null, null, null, null, null, null);  

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    do {
                        AlarmSetting setting = new AlarmSetting();
                        setting.id = c.getLong(c.getColumnIndex(ALARM_SETTING_COL_ID));
                        setting.onOff = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_ON_OFF));
                        setting.title = c.getString(c.getColumnIndex(ALARM_SETTING_COL_TITLE));
                        setting.repeat = AlarmSetting.Repeat.valueOf(c.getString(c.getColumnIndex(ALARM_SETTING_COL_REPEAT)));
                        setting.week = AlarmSetting.intToWeekSet(c.getInt(c.getColumnIndex(ALARM_SETTING_COL_WEEK)));
                        setting.ymd.year = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_YEAR));
                        setting.ymd.month = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MONTH));
                        setting.ymd.day = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_DAY));
                        setting.hour = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_HOUR));
                        setting.minute = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MINUTE));
                        setting.musicKey.content = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_CONTENT));
                        setting.musicKey.id = c.getLong(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_ID));
                        setting.musicKey.path = c.getString(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_PATH));
                        setting.musicVolume = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_VOLUME));
                        setting.musicLength = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_LENGTH));
                        setting.voice = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_VOICE));
                        setting.vibrator = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_VIBRATOR));
                        setting.snoozeMode = AlarmSetting.SnoozeMode.valueOf(c.getString(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_MODE)));
                        setting.snoozeLength = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_LENGTH));
                        setting.snoozeTimes = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_TIMES));
                        settings.add(setting);
                    } while(c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select alarm settings error!", e, true, false);
            settings = null;
        } finally {
            // DBクローズ
            close();
        }
        return settings;
    }

    /**
     * アラーム設定テーブル取得（1件）
     * @param id ID
     * @return アラーム設定
     */
    public AlarmSetting selectAlarmSetting(long id) {
        AlarmSetting setting = null;

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(ALARM_SETTING_TABLE_NAME, null,
                    ALARM_SETTING_COL_ID + " = ?",
                    new String[]{Long.toString(id)}, null, null, null);

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    setting = new AlarmSetting();
                    setting.id = c.getLong(c.getColumnIndex(ALARM_SETTING_COL_ID));
                    setting.onOff = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_ON_OFF));
                    setting.title = c.getString(c.getColumnIndex(ALARM_SETTING_COL_TITLE));
                    setting.repeat = AlarmSetting.Repeat.valueOf(c.getString(c.getColumnIndex(ALARM_SETTING_COL_REPEAT)));
                    setting.week = AlarmSetting.intToWeekSet(c.getInt(c.getColumnIndex(ALARM_SETTING_COL_WEEK)));
                    setting.ymd.year = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_YEAR));
                    setting.ymd.month = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MONTH));
                    setting.ymd.day = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_DAY));
                    setting.hour = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_HOUR));
                    setting.minute = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MINUTE));
                    setting.musicKey.content = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_CONTENT));
                    setting.musicKey.id = c.getLong(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_ID));
                    setting.musicKey.path = c.getString(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_PATH));
                    setting.musicVolume = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_VOLUME));
                    setting.musicLength = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_MUSIC_LENGTH));
                    setting.voice = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_VOICE));
                    setting.vibrator = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_VIBRATOR));
                    setting.snoozeMode = AlarmSetting.SnoozeMode.valueOf(c.getString(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_MODE)));
                    setting.snoozeLength = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_LENGTH));
                    setting.snoozeTimes = c.getInt(c.getColumnIndex(ALARM_SETTING_COL_SNOOZE_TIMES));
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select alarm setting error!", e, true, false);
            setting = null;
        } finally {
            // DBクローズ
            close();
        }
        return setting;
    }

    /**
     * アラーム設定テーブル更新
     * @param id ID
     * @param setting アラーム設定
     * @return 更新した場合はtrue
     */
    public boolean updateAlarmSetting(long id, AlarmSetting setting) {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }
        
        try {
            // トランザクション開始
            mDb.beginTransaction();
            
            //入力するデータ生成
            ContentValues values = new ContentValues();
            values.put(ALARM_SETTING_COL_ID, setting.id);
            values.put(ALARM_SETTING_COL_ON_OFF, setting.onOff);
            values.put(ALARM_SETTING_COL_TITLE, setting.title);
            values.put(ALARM_SETTING_COL_REPEAT, setting.repeat.toString());
            values.put(ALARM_SETTING_COL_WEEK, AlarmSetting.weekSetToInt(setting.week));
            values.put(ALARM_SETTING_COL_YEAR, setting.ymd.year);
            values.put(ALARM_SETTING_COL_MONTH, setting.ymd.month);
            values.put(ALARM_SETTING_COL_DAY, setting.ymd.day);
            values.put(ALARM_SETTING_COL_HOUR, setting.hour);
            values.put(ALARM_SETTING_COL_MINUTE, setting.minute);
            values.put(ALARM_SETTING_COL_MUSIC_CONTENT, setting.musicKey.content);
            values.put(ALARM_SETTING_COL_MUSIC_ID, setting.musicKey.id);
            values.put(ALARM_SETTING_COL_MUSIC_PATH, setting.musicKey.path);
            values.put(ALARM_SETTING_COL_MUSIC_VOLUME, setting.musicVolume);
            values.put(ALARM_SETTING_COL_MUSIC_LENGTH, setting.musicLength);
            values.put(ALARM_SETTING_COL_VOICE, setting.voice);
            values.put(ALARM_SETTING_COL_VIBRATOR, setting.vibrator);
            values.put(ALARM_SETTING_COL_SNOOZE_MODE, setting.snoozeMode.toString());
            values.put(ALARM_SETTING_COL_SNOOZE_LENGTH, setting.snoozeLength);
            values.put(ALARM_SETTING_COL_SNOOZE_TIMES, setting.snoozeTimes);

            // データ更新
            num = mDb.update(ALARM_SETTING_TABLE_NAME, values,
                ALARM_SETTING_COL_ID + " = ?",  new String[]{Long.toString(id)});

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Update alarm setting error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * アラーム設定テーブル削除
     * @return 削除した場合はtrue
     */
    public boolean deleteAlarmSettings() {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }
        
        try {
            // トランザクション開始
            mDb.beginTransaction();
            
            // データ削除
            num = mDb.delete(ALARM_SETTING_TABLE_NAME, null, null);
            
            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Delete alarm settings error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * アラーム設定テーブル件数取得
     * @return 件数
     */
    public long getAlarmSettingCount() {
        long count = 0;

        // DBオープン
        if (!open(false)) {
            return 0;
        }

        try {
            // 件数取得
            Cursor c = mDb.rawQuery("select count(*) from " + ALARM_SETTING_TABLE_NAME, null);

            // 件数の取得
            if (c != null) {
                if(c.moveToLast()){
                    count = c.getLong(0);
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Count alarm setting error!", e, true, false);
        } finally {
            // DBクローズ
            close();
        }
        return count;
    }

    /**
     * エラー情報テーブル追加
     * @param text テキスト
     * @return 追加した場合はtrue
     */
    public boolean insertErrorInf(String text) {
        long id = -1;

        // DBオープン
        if (!open(true)) {
            return false;
        }

        try {
            // トランザクション開始
            mDb.beginTransaction();

            // SQL作成
            SQLiteStatement stmt = mDb.compileStatement("insert into " + ERROR_INF_TABLE_NAME + "(" +
                                                         ERROR_INF_COL_TEXT +
                                                         ") values (?);"); 

            // データ追加
            stmt.bindString(1, text);
            id = stmt.executeInsert();
            if (id < 0) {
                AlarmClockApp.outputError(mContext, "Insert error inf error!", null, true, false);
            }

            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Insert error inf error!", e, true, false);
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (id < 0) {
            return false;
        }

        return true;
    }

    /**
     * エラー情報テーブル取得
     * @return テキストのリスト
     */
    public List<String> selectErrorInf() {
        List<String> texts = new LinkedList<String>();

        // DBオープン
        if (!open(false)) {
            return null;
        }

        try {
            // データ取得
            Cursor c = mDb.query(ERROR_INF_TABLE_NAME, null, null, null, null, null, null);  

            // 値の取得
            if (c != null) {
                if(c.moveToFirst()){
                    do {
                        String text = c.getString(c.getColumnIndex(ERROR_INF_COL_TEXT));
                        texts.add(text);
                    } while(c.moveToNext());
                }
                c.close();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Select error inf error!", e, true, false);
            texts = null;
        } finally {
            // DBクローズ
            close();
        }
        return texts;
    }

    /**
     * エラー情報テーブル削除
     * @return 削除した場合はtrue
     */
    public boolean deleteErrorInf() {
        int num = 0;

        // DBオープン
        if (!open(true)) {
            return false;
        }
        
        try {
            // トランザクション開始
            mDb.beginTransaction();
            
            // データ削除
            num = mDb.delete(ERROR_INF_TABLE_NAME, null, null);
            
            // コミット
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "Delete error inf error!", e, true, false);
            return false;
        } finally {
            // トランザクション終了
            mDb.endTransaction();

            // DBクローズ
            close();
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    /**
     * DBオープン
     * @param bWritable 書き込み用の場合はtrue
     * @return オープンした場合はtrue
     */
    private boolean open(boolean bWritable) {
        try {
            if (bWritable) {
                mDb = mDbHelper.getWritableDatabase();
            } else {
                mDb = mDbHelper.getReadableDatabase();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "DB Open error!", e, true, false);
            return false;
        }
        return true;
    }

    /**
     * DBクローズ
     */
    private void close() {
        try {
            mDbHelper.close();
        } catch (Exception e) {
            AlarmClockApp.outputError(mContext, "DB Close error!", e, true, false);
        }
    }

    /**
     * スヌーズ残り回数取得
     * @param context コンテキスト
     * @return スヌーズ残り回数
     */
    public static int getSnoozeRemainTimes(Context context) {
        int snoozeRemainTimes = 0;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 
            snoozeRemainTimes = sp.getInt(KEY_SNOOZE_REMAIN_TIMES, snoozeRemainTimes);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Get snooze remain times error!", e, true, true);
        }
        return snoozeRemainTimes;
    }

    /**
     * スヌーズ残り回数設定
     * @param context コンテキスト
     * @param snoozeRemainTimes スヌーズ残り回数
     */
    public static void setSnoozeRemainTimes(Context context, int snoozeRemainTimes) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Editor e = sp.edit();
            e.putInt(KEY_SNOOZE_REMAIN_TIMES, snoozeRemainTimes);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Set snooze remain times error!", e, true, true);
        }
    }

    /**
     * アラームキー取得
     * @param context コンテキスト
     * @return アラームキー
     */
    public static String getAlarmKey(Context context) {
        String alarmKey = "";
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context); 
            alarmKey = sp.getString(KEY_ALARM_KEY, alarmKey);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Get alarm key error!", e, true, true);
        }
        return alarmKey;
    }

    /**
     * アラームキー設定
     * @param context コンテキスト
     * @param alarmKey アラームキー
     */
    public static void setAlarmKey(Context context, String alarmKey) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Editor e = sp.edit();
            e.putString(KEY_ALARM_KEY, alarmKey);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Set alarm key error!", e, true, true);
        }
    }

    /**
     * プリファレンスのクリア
     * @param context コンテキスト
     */
    public static void clearPreferences(Context context) {
        try {
            String packageName = context.getPackageName();
            String path = "/data/data/" + packageName + "/shared_prefs/" + packageName + "_preferences.xml";
            File f = new File(path);
            if (f.exists()) {
                if (!f.delete()) {
                    AlarmClockApp.outputError(context, "Clear preferences error!", null, true, true);
                }
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Clear preferences error!", e, true, true);
        }
    }
}
