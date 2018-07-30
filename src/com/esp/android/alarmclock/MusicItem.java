package com.esp.android.alarmclock;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.media.MediaPlayer;

/**
 * 外部ストレージ上の音楽をあらわすクラス
 */
public class MusicItem implements Serializable, Comparable<Object> {
    private static final long serialVersionUID = 1L;

    // デフォルト文字列
    public static final String DEF_STRING = " ";

    // コンテンツ
    public static final int CONTENT_ORIGINAL = 0; // オリジナル
    public static final int CONTENT_INTERNAL = 1; // 端末内部
    public static final int CONTENT_EXTERNAL = 2; // 外部メディア
    public static final int CONTENT_RANDOM   = 3; // ランダム

    // メディアの種類
    public static final int TYPE_ALARM     = 0; // アラーム音
    public static final int TYPE_MUSIC     = 1; // 音楽
    public static final int TYPE_BOOKMARK  = 2; // ブックマーク

    // コンテンツ
    final int content;

    // ID
    final long id;

    // メディアの種類
    final int type;

    // アーティスト
    final String artist;

    // タイトル
    final String title;

    // 長さ(ms)
    final long duration;

    /**
     * コンストラクタ
     */
    public MusicItem(int content, long id, int type, String artist, String title, long duration) {
        this.content = content;
        this.id = id;
        this.type = type;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    /**
     * コピーコンストラクタ
     */
    public MusicItem(MusicItem item) {
        this.content = item.content;
        this.id = item.id;
        this.type = item.type;
        this.artist = item.artist;
        this.title = item.title;
        this.duration = item.duration;
    }

    /**
     * URIを取得する
     * @param context コンテキスト
     */
    public Uri getURI(Context context) {
        if (content == CONTENT_ORIGINAL) {
            return Uri.parse("android.resource://" + context.getPackageName() + "/" + id);
        } else if (content == CONTENT_EXTERNAL) {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        } else if (content == CONTENT_INTERNAL) {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, id);
        }
        return null;
    }

    /**
     * ファイルパスを取得する
     * @param context コンテキスト
     */
    public String getPath(Context context) {
        String filePath = "";
        if (content == CONTENT_ORIGINAL || content == CONTENT_RANDOM) {
            filePath = "/" + artist + "/" + title;
        } else {
            Uri uri = getURI(context);
            if ("content".equals(uri.getScheme())) {
                ContentResolver cr = context.getContentResolver();
                Cursor cur = cr.query(uri, new String[] { MediaStore.Audio.Media.DATA }, null, null, null);
                cur.moveToFirst();
                filePath = cur.getString(0);
                cur.close();
            } else {
                filePath = uri.getPath();
            }
        }
        return filePath;
    }

    /**
     * 音楽を探してリストを返す
     * @param context コンテキスト
     * @return 見つかった音楽のリスト
     */
    public static List<MusicItem> getItems(Context context) {
        List<MusicItem> items = new LinkedList<MusicItem>();

        try {
            // Rクラスの全ての内部クラスを取得
            Class<?>[] classes = R.class.getClasses();
            for (Class<?> cls : classes) {
                // rawデータを取得
                if (cls.getSimpleName().equals("raw")) {
                    Field[] fields = cls.getFields();
                    for (Field field : fields) {
                        // リストに追加
                        try {
                            String fieldName = field.getName();
                            int resId = ((Integer)field.get(fieldName)).intValue();
                            MediaPlayer mp = MediaPlayer.create(context, resId);
                            if (mp != null) {
                                String original = context.getResources().getString(R.string.original_label);
                                items.add(new MusicItem(CONTENT_ORIGINAL, resId, TYPE_ALARM,
                                            original, fieldName, mp.getDuration()));
                                mp.release();
                            }
                        } catch (Exception e) {}
                    }
                }
            }

            // ContentResolverを取得
            ContentResolver cr = context.getContentResolver();
            Cursor cur = null;

            for (int content = CONTENT_INTERNAL; content <= CONTENT_EXTERNAL; content++) {
                // 端末内部から音楽を検索
                if (content == CONTENT_INTERNAL) {
                    cur = cr.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                            new String[] {
                                    MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media.TITLE,
                                    MediaStore.Audio.Media.DURATION,
                                    MediaStore.Audio.Media._ID,
                                    MediaStore.Audio.Media.IS_MUSIC},
                            MediaStore.Audio.Media.IS_MUSIC + " = 1 OR " +
                            MediaStore.Audio.Media.IS_ALARM + " = 1 OR " +
                            MediaStore.Audio.Media.IS_RINGTONE + " = 1",
                            null, null);

                // SDカードから音楽を検索
                } else if (content == CONTENT_EXTERNAL) {
                    cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            new String[] {
                                    MediaStore.Audio.Media.ARTIST,
                                    MediaStore.Audio.Media.TITLE,
                                    MediaStore.Audio.Media.DURATION,
                                    MediaStore.Audio.Media._ID,
                                    MediaStore.Audio.Media.IS_MUSIC},
                            MediaStore.Audio.Media.IS_MUSIC + " = 1 OR " +
                            MediaStore.Audio.Media.IS_ALARM + " = 1 OR " +
                            MediaStore.Audio.Media.IS_RINGTONE + " = 1",
                            null, null);
                }

                if (cur != null) {
                    if (cur.moveToFirst()) {
                        // 曲情報のカラムを取得
                        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
                        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                        int isMusicColumn = cur.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);

                        do {
                            // IDを取得
                            long id = cur.getLong(idColumn);

                            // メディアの種類を取得
                            int type = TYPE_ALARM;
                            int isMusic = cur.getInt(isMusicColumn);
                            if (isMusic == 1) {
                                type = TYPE_MUSIC;
                            }

                            // アーティストを取得
                            String artist = cur.getString(artistColumn);
                            if (artist == null || artist.length() == 0) {
                                artist = DEF_STRING;
                            }

                            // タイトルを取得
                            String title = cur.getString(titleColumn);
                            if (title == null || title.length() == 0) {
                                title = DEF_STRING;
                            }

                            // 長さを取得
                            long duration = cur.getLong(durationColumn);

                            // 種類が音楽の場合は3秒以上のアイテムを追加
                            if (type != TYPE_MUSIC || duration >= 3000) {
                                // リストに追加
                                items.add(new MusicItem(content, id, type,
                                            artist, title, duration));
                            }
                        } while (cur.moveToNext());
                    }
                    // カーソルを閉じる
                    cur.close();
                }
            }

            // ランダム再生用のアイテム
            String[] typeList = context.getResources().getStringArray(R.array.music_type_list);
            String random = context.getResources().getString(R.string.random_label);
            items.add(new MusicItem(CONTENT_RANDOM, -1, TYPE_BOOKMARK,
                                    typeList[TYPE_BOOKMARK], random, -1));

            // 見つかる順番はソートされていないためソートする
            Collections.sort(items);
        } catch (Exception e) {
            return null;
        }
        return items;
    }

    /**
     * 比較する
     */
    @Override
    public int compareTo(Object another) {
        if (another == null) {
            return 1;
        }
        MusicItem anotherItem = (MusicItem)another;

        // コンテンツで比較する
        if (content != anotherItem.content) {
            if (content == CONTENT_ORIGINAL) {
                return -1;
            } else if (content == CONTENT_RANDOM) {
                return 1;
            } else if (anotherItem.content == CONTENT_ORIGINAL) {
                return 1;
            } else if (anotherItem.content == CONTENT_RANDOM) {
                return -1;
            }
        }

        // メディアの種類で比較する
        int result = type - anotherItem.type;
        if (result != 0) {
            return result;
        }

        // アーティストで比較する
        if (type == TYPE_MUSIC) {
            result = artist.compareTo(anotherItem.artist);
            if (result != 0) {
                return result;
            }
        }

        // タイトルで比較する
        return title.compareTo(anotherItem.title);
    }

    /**
     * 等価チェック
     */
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        MusicItem item = (MusicItem)another;
        if (item.content == content && item.id == id) {
            return true;
        }
        return false;
    }
}
