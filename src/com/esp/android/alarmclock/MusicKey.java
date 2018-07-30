package com.esp.android.alarmclock;

/**
 * 音楽を検索する時のキーをあらわすクラス
 */
public class MusicKey {

    // 固定アラーム音のID
    public static final int ORIGINAL_ID = R.raw.original;

    // 音楽のコンテンツ
    int content;

    // 音楽のID
    long id;

    // 音楽のパス
    String path;

    /**
     * コンストラクタ
     */
    public MusicKey() {
        this.content = MusicItem.CONTENT_ORIGINAL;
        this.id = ORIGINAL_ID;
        this.path = "";
    }
}
