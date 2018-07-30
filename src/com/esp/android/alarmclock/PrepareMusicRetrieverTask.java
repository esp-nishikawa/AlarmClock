package com.esp.android.alarmclock;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

/**
 * 外部ストレージから音楽ファイルを探すための非同期タスク
 */
public class PrepareMusicRetrieverTask extends AsyncTask<Context, Void, Boolean> {
    private Context mContext;
    private MusicRetrieverPreparedListener mListener;

    // 音楽ファイル検索中フラグ
    private static boolean bRetriever;

    // ロック用オブジェクト
    private static Object sMusicRetrieverLock = new Object();

    // コールバック用インタフェース
    public interface MusicRetrieverPreparedListener {
        public void onMusicRetrieverPrepared(Context context, Boolean result);
    }

    /**
     * コンストラクタ
     */
    public PrepareMusicRetrieverTask(Context context, MusicRetrieverPreparedListener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * バックグラウンドで実行される処理
     */
    @Override
    protected Boolean doInBackground(Context... arg) {
        Boolean result = Boolean.TRUE;
        Context context = arg[0];
        try {
            synchronized (sMusicRetrieverLock) {
                bRetriever = true;
                DataManager dataManager = new DataManager(context);

                // アラーム設定がなければ追加
                if (dataManager.getAlarmSettingCount() == 0) {
                    String alarmLabel = context.getResources().getString(R.string.alarm_label);
                    for (int i=0; i<12; i++) {
                        AlarmSetting alarmSetting = new AlarmSetting();
                        alarmSetting.id = i;
                        alarmSetting.title = alarmLabel + Integer.toString(i+1);
                        dataManager.insertAlarmSetting(alarmSetting);
                    }
                }

                // 音楽を全て削除
                dataManager.deleteMusicItems();

                // 音楽を検索して追加
                List<MusicItem> items = MusicItem.getItems(context);
                if (items != null) {
                    dataManager.insertMusicItems(items);
                } else {
                    AlarmClockApp.outputError(context, "Prepare music retriever error!", null, true, true);
                    result = Boolean.FALSE;
                }

                // アラームに設定されている音楽を更新する
                List<AlarmSetting> alarmSettings = dataManager.selectAlarmSettings();
                for (AlarmSetting alarmSetting : alarmSettings) {
                    MusicItem musicItem = searchMusicItem(context, items, alarmSetting.musicKey.path);
                    if (musicItem != null) {
                        alarmSetting.musicKey.content = musicItem.content;
                        alarmSetting.musicKey.id = musicItem.id;
                        dataManager.updateAlarmSetting(alarmSetting.id, alarmSetting);
                    }
                }

                // ブックマークの音楽を更新する
                List<MusicKey> musicKeys = dataManager.selectBookmarks();
                for (MusicKey musicKey : musicKeys) {
                    MusicItem musicItem = searchMusicItem(context, items, musicKey.path);
                    if (musicItem != null) {
                        musicKey.content = musicItem.content;
                        musicKey.id = musicItem.id;
                        dataManager.updateBookmark(musicKey.path, musicKey);
                    }
                }
                bRetriever = false;
            }
        } catch (Exception e) {
            bRetriever = false;
            AlarmClockApp.outputError(context, "Prepare music retriever error!", e, true, true);
            result = Boolean.FALSE;
        }

        return result;
    }

    /**
     * ファイルパスから音楽リストを検索する
     * @param context コンテキスト
     * @param items アイテムのリスト
     * @param path ファイルパス
     * @return 見つかった音楽
     */
    private static MusicItem searchMusicItem(Context context, List<MusicItem> items, String path) {
        try {
            if (path == null || path.length() == 0) {
                return null;
            }
            Iterator<MusicItem> itr = items.iterator(); 
            while (itr.hasNext()) { 
                MusicItem item = (MusicItem)itr.next();
                if (path.equals(item.getPath(context))) {
                    return item;
                }
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Search music item error!", e, true, true);
            return null;
        }
        return null;
    }

    /**
     * 最初にUIスレッドで呼び出される処理
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * doInBackgroundが終わった時の処理
     */  
    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onMusicRetrieverPrepared(mContext, result);
    }

    /**
     * コンストラクタ
     */
    public static boolean isRetriever() {
        return bRetriever;
    }
}
