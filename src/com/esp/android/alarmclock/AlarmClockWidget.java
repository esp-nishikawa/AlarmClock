package com.esp.android.alarmclock;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;
import java.util.Random;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.os.PowerManager;
import android.os.Vibrator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 時計のウィジット
 */
public class AlarmClockWidget extends AppWidgetProvider implements
        PrepareMusicRetrieverTask.MusicRetrieverPreparedListener {

    // アクション
    public static final String ACTION_CLOCK_CLICK = "com.esp.android.alarmclock.ACTION_CLOCK_CLICK";
    public static final String ACTION_START_ALARM = "com.esp.android.alarmclock.ACTION_START_ALARM";
    public static final String ACTION_STOP_ALARM = "com.esp.android.alarmclock.ACTION_STOP_ALARM";
    public static final String ACTION_RESET = "com.esp.android.alarmclock.ACTION_RESET";
    public static final String ACTION_ERROR = "com.esp.android.alarmclock.ACTION_ERROR";

    // スリープ解除ロック
    private static PowerManager.WakeLock mWakeLock;

    /**
     * 最初に1つ起動した時の処理
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        // 外部ストレージから音楽ファイルを探すための非同期タスクを実行
        prepareMusicRetriever(context);
    }

    /**
     * 起動時の処理
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // アラームを設定
        setAlarm(context);
    }

    /**
     * 終了時の処理
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * すべて終了させた時の処理
     */
    @Override
    public void onDisabled(Context context) {
        // アラームを停止
        stopAlarm(context);

        super.onDisabled(context);
    }

    /**
     * アクション受信時の処理
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // クリック
        if (ACTION_CLOCK_CLICK.equals(intent.getAction())) {
            if (PrepareMusicRetrieverTask.isRetriever()) {
                Toast.makeText(context, "Please wait. Loading data...", Toast.LENGTH_SHORT).show();
            } else {
                // アラーム一覧画面を表示する
                startAlarmListActivity(context);
            }

        // アラーム開始
        } else if (ACTION_START_ALARM.equals(intent.getAction())) {
            // アラーム設定のIDを取得
            long alarmId = intent.getLongExtra("alarm_id", -1);

            // アラームを鳴らす
            startAlarm(context, alarmId);

            // アラーム停止画面を表示する
            startAlarmStopActivity(context, alarmId);

            // スヌーズ解除画面を終了
            finishSnoozeReleaseActivity(context);

            // テスト
            AlarmClockApp.checkMemory(context);

        // アラーム停止
        } else if (ACTION_STOP_ALARM.equals(intent.getAction())) {
            // アラーム設定のIDを取得
            long alarmId = intent.getLongExtra("alarm_id", -1);

            // アラームを停止
            stopAlarm(context);

            // スヌーズをセット
            long alarmTimeInMillis = setSnooze(context, alarmId);
            if (alarmTimeInMillis >= 0) {
                // スヌーズ解除画面を表示
                startSnoozeReleaseActivity(context, alarmId, alarmTimeInMillis);
            } else {
                // スヌーズ解除画面を終了
                finishSnoozeReleaseActivity(context);

                // アラーム停止画面を終了
                finishAlarmStopActivity(context);

                // 次のアラームを設定
                setAlarm(context);
            }

        // メディアスキャン完了
        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
            // 外部ストレージから音楽ファイルを探すための非同期タスクを実行
            prepareMusicRetriever(context);

        // リセット
        } else if (ACTION_RESET.equals(intent.getAction())) {
            // プリファレンスのクリア
            DataManager.clearPreferences(context);

            // 次のアラームを設定
            setAlarm(context);

            // 外部ストレージから音楽ファイルを探すための非同期タスクを実行
            prepareMusicRetriever(context);

        // エラー発生
        } else if (ACTION_ERROR.equals(intent.getAction())) {
            String text = intent.getStringExtra("text");
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 音楽ファイル検索完了時の処理
     * @param context コンテキスト
     * @param items 見つかった音楽のリスト
     */
    @Override
    public void onMusicRetrieverPrepared(Context context, Boolean result) {
        AlarmClockApp.checkMemory(context);
    }

    /**
     * 外部ストレージから音楽ファイルを探すための非同期タスクを実行
     * @param context コンテキスト
     */
    private void prepareMusicRetriever(Context context) {
        try {
            PrepareMusicRetrieverTask task = new PrepareMusicRetrieverTask(context, this);
            task.execute(context);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Prepare music retriever error!", e, true, true);
        }
    }

    /**
     * ウィジットを更新する
     * @param context コンテキスト
     * @param date アラーム日時
     */
    public static void updateAppWidget(Context context, Date date) {
        try {
            ComponentName widget = new ComponentName(context, AlarmClockWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = manager.getAppWidgetIds(widget);
            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);

                // クリックのアクションを設定する
                Intent intent = new Intent(ACTION_CLOCK_CLICK);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.main_analogclock, pendingIntent);

                // アラーム日時を表示する
                if (date != null) {
                    views.setTextViewText(R.id.main_date_textview, new SimpleDateFormat("M/d(E)").format(date));
                    views.setTextViewText(R.id.main_time_textview, new SimpleDateFormat("HH:mm").format(date));
                } else {
                    views.setTextViewText(R.id.main_date_textview, "ALARM");
                    views.setTextViewText(R.id.main_time_textview, "OFF");
                }

                // ウィジットを更新する
                manager.updateAppWidget(appWidgetId, views);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Update AppWidget error!", e, true, true);
        }
    }

    /**
     * アラーム一覧画面を表示
     * @param context コンテキスト
     */
    public static void startAlarmListActivity(Context context) {
        try {
            Intent intent = new Intent(context, AlarmListActivity.class);
            // Activity以外からActivityを呼び出すためのフラグを設定
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Start alarm list activity error!", e, true, true);
        }
    }

    /**
     * アラーム停止画面を表示
     * @param context コンテキスト
     * @param alarmId アラーム設定のID
     */
    public static void startAlarmStopActivity(Context context, long alarmId) {
        try {
            Intent intent = new Intent(context, AlarmStopActivity.class);
            // Activity以外からActivityを呼び出すためのフラグを設定
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("alarm_id", alarmId);
            String alarmKey = DataManager.getAlarmKey(context);
            intent.putExtra("alarm_key", alarmKey);
            context.startActivity(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Start alarm stop activity error!", e, true, true);
        }
    }

    /**
     * アラーム停止画面を終了
     * @param context コンテキスト
     */
    public static void finishAlarmStopActivity(Context context) {
        try {
            Intent intent = new Intent(AlarmStopActivity.ACTION_FINISH_ALARM_STOP_ACTIVITY);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Finish alarm stop activity error!", e, true, true);
        }
    }

    /**
     * スヌーズ解除画面を表示
     * @param context コンテキスト
     * @param alarmId アラーム設定のID
     * @param alarmTimeInMillis アラームをセットしたUTC時刻
     */
    public static void startSnoozeReleaseActivity(Context context, long alarmId, long alarmTimeInMillis) {
        try {
            Intent intent = new Intent(context, SnoozeReleaseActivity.class);
            // Activity以外からActivityを呼び出すためのフラグを設定
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("alarm_id", alarmId);
            intent.putExtra("alarm_time", alarmTimeInMillis);
            int snoozeRemainTimes = DataManager.getSnoozeRemainTimes(context);
            intent.putExtra("remain_times", snoozeRemainTimes);
            String alarmKey = DataManager.getAlarmKey(context);
            intent.putExtra("alarm_key", alarmKey);
            context.startActivity(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Start snooze release activity error!", e, true, true);
        }
    }

    /**
     * スヌーズ解除画面を終了
     * @param context コンテキスト
     */
    public static void finishSnoozeReleaseActivity(Context context) {
        try {
            Intent intent = new Intent(SnoozeReleaseActivity.ACTION_FINISH_SNOOZE_RELEASE_ACTIVITY);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Finish snooze release activity error!", e, true, true);
        }
    }

    /**
     * アラームをセット
     * @param context コンテキスト
     * @return セットしたUTC時刻
     */
    public static long setAlarm(Context context) {
        long alarmTimeInMillis = -1;
        try {
            // 次に鳴らすアラーム設定の取得
            AlarmSetting nextSetting = null;
            GregorianCalendar nextCalendar = null;
            DataManager dataManager = new DataManager(context);
            List<AlarmSetting> alarmSettings = dataManager.selectAlarmSettings();
            for (AlarmSetting alarmSetting : alarmSettings) {
                if (alarmSetting.onOff != 0) { // アラームONのみ
                    GregorianCalendar calendar = alarmSetting.getNextCalendar();
                    if (calendar != null) {
                        if (nextCalendar == null || calendar.before(nextCalendar)) {
                            nextCalendar = calendar;
                            nextSetting = alarmSetting;
                        }
                    }
                }
            }

            // アラームをセット
            if (nextCalendar != null && nextSetting != null) {
                alarmTimeInMillis = nextCalendar.getTimeInMillis();
                setAction(context, ACTION_START_ALARM, nextSetting.id, alarmTimeInMillis);
                if (nextSetting.snoozeMode != AlarmSetting.SnoozeMode.SnoozeOff) {
                    DataManager.setSnoozeRemainTimes(context, nextSetting.snoozeTimes);
                }
                String alarmKey = new SimpleDateFormat("yyyyMMddHHmmss").format(nextCalendar.getTime());
                DataManager.setAlarmKey(context, alarmKey);
                updateAppWidget(context, nextCalendar.getTime());
            } else {
                cancelAction(context, ACTION_START_ALARM);
                DataManager.setAlarmKey(context, "");
                updateAppWidget(context, null);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Set alarm error!", e, true, true);
            return -1;
        }
        return alarmTimeInMillis;
    }

    /**
     * アラームを鳴らす
     * @param context コンテキスト
     * @param alarmId アラーム設定のID
     */
    public static void startAlarm(Context context, long alarmId) {
        try {
            // 音楽ファイル検索中の場合は検索完了まで待つ
            while (PrepareMusicRetrieverTask.isRetriever()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            // スリープ解除
            if (mWakeLock == null) {
                PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                                           PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                           PowerManager.ON_AFTER_RELEASE, "AlarmClockWidget");
                mWakeLock.acquire();
            }

            // アラーム設定を取得
            DataManager dataManager = new DataManager(context);
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(alarmId);
            if (alarmSetting == null || alarmSetting.onOff == 0) {
                AlarmClockApp.outputError(context, "Alarm is turned off.", null, true, false);
                setAlarm(context);
                return;
            }

            // バイブレータを開始
            if (alarmSetting.vibrator != 0) {
                Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(new long[] { 500, 500 }, 0);
            }

            // ボリュームを計算
            int musicVolume = alarmSetting.musicVolume;
            if (alarmSetting.snoozeMode == AlarmSetting.SnoozeMode.VolumeUp) {
                int snoozeRemainTimes = DataManager.getSnoozeRemainTimes(context);
                double scale = (double)(100 - alarmSetting.musicVolume) / (double)alarmSetting.snoozeTimes;
                double gains = scale * (double)(alarmSetting.snoozeTimes - snoozeRemainTimes);
                musicVolume = (int)Math.round((double)alarmSetting.musicVolume + gains);
            }
            if (musicVolume < 0) musicVolume = 0;
            if (musicVolume > 100) musicVolume = 100;

            // 音楽を取得
            MusicItem musicItem = dataManager.selectMusicItem(alarmSetting.musicKey.content, alarmSetting.musicKey.id);

            // 音楽を再生
            musicPlay(context, musicItem, musicVolume);

            // 音声を再生
            if (alarmSetting.voice != 0) {
                voicePlay(context);
            }

            // アラーム停止のアクションを設定
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.add(Calendar.SECOND, alarmSetting.musicLength);
            setAction(context, ACTION_STOP_ALARM, alarmId, calendar.getTimeInMillis());
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Start alarm error!", e, true, true);
        }
    }

    /**
     * アラームを停止
     * @param context コンテキスト
     */
    public static void stopAlarm(Context context) {
        try {
            // アラームをキャンセル
            cancelAction(context, ACTION_STOP_ALARM);
            cancelAction(context, ACTION_START_ALARM);

            // 音声を停止
            voiceStop(context);

            // 音楽を停止
            musicStop(context);

            // バイブレータを停止
            Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();

            // スリープ解除ロックの解放
            if (mWakeLock != null) {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                mWakeLock = null;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Stop alarm error!", e, true, true);
        }
    }

    /**
     * スヌーズをセット
     * @param context コンテキスト
     * @param alarmId アラーム設定のID
     * @return セットしたUTC時刻
     */
    public static long setSnooze(Context context, long alarmId) {
        long alarmTimeInMillis = -1;
        try {
            // アラーム設定を取得
            DataManager dataManager = new DataManager(context);
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(alarmId);
            if (alarmSetting == null || alarmSetting.onOff == 0) {
                AlarmClockApp.outputError(context, "Alarm is turned off.", null, true, false);
                return -1;
            }

            // スヌーズをセットするかどうかチェック
            int snoozeRemainTimes = DataManager.getSnoozeRemainTimes(context);
            if (alarmSetting.snoozeMode == AlarmSetting.SnoozeMode.SnoozeOff || snoozeRemainTimes == 0) {
                return -1;
            }

            // アラームをセット
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.add(Calendar.MINUTE, alarmSetting.snoozeLength);
            alarmTimeInMillis = calendar.getTimeInMillis();
            setAction(context, ACTION_START_ALARM, alarmId, alarmTimeInMillis);
            snoozeRemainTimes--;
            DataManager.setSnoozeRemainTimes(context, snoozeRemainTimes);
            updateAppWidget(context, calendar.getTime());
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Set snooze error!", e, true, true);
            return -1;
        }
        return alarmTimeInMillis;
    }

    /**
     * 音楽を再生
     * @param context コンテキスト
     * @param musicItem 再生する音楽
     * @param volume ボリューム（％）
     */
    public static void musicPlay(Context context, MusicItem musicItem, int volume) {
        try {
            DataManager dataManager = new DataManager(context);

            // 再生する音楽を選択する
            MusicItem playItem = null;
            if (musicItem != null) {
                // ランダム再生
                if (musicItem.content == MusicItem.CONTENT_RANDOM) {
                    if (musicItem.type == MusicItem.TYPE_BOOKMARK) { // ブックマークのみ
                        long size = dataManager.getBookmarksCount();
                        if (size > 0) {
                            Random rnd = new Random();
                            int index = rnd.nextInt((int)size);
                            MusicKey musicKey = dataManager.selectBookmark(index);
                            playItem = dataManager.selectMusicItem(musicKey.content, musicKey.id);
                        }
                    }
                } else {
                    playItem = musicItem;
                }
            }

            // 見つからない場合は固定アラーム音を再生
            if (playItem == null) {
                MusicKey musicKey = new MusicKey();
                playItem = dataManager.selectMusicItem(musicKey.content, musicKey.id);
                AlarmClockApp.outputError(context, "No available music to play.", null, true, false);
            }

            // サービススタート
            Intent serviceIntent = new Intent(MusicPlayerService.ACTION_MUSIC_PLAY);
            serviceIntent.putExtra("music_item", playItem);
            serviceIntent.putExtra("volume", volume);
            context.startService(serviceIntent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Music play error!", e, true, true);
        }
    }

    /**
     * 音楽を停止
     * @param context コンテキスト
     */
    public static void musicStop(Context context) {
        try {
            // サービススタート
            Intent serviceIntent = new Intent(MusicPlayerService.ACTION_MUSIC_STOP);
            context.startService(serviceIntent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Music stop error!", e, true, true);
        }
    }

    /**
     * 音声を再生
     * @param context コンテキスト
     */
    public static void voicePlay(Context context) {
        try {
            // サービススタート
            Intent serviceIntent = new Intent(TextToSpeechService.ACTION_VOICE_PLAY);
            context.startService(serviceIntent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Tts play error!", e, true, true);
        }
    }

    /**
     * 音声を停止
     * @param context コンテキスト
     */
    public static void voiceStop(Context context) {
        try {
            // サービススタート
            Intent serviceIntent = new Intent(TextToSpeechService.ACTION_VOICE_STOP);
            context.startService(serviceIntent);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Tts stop error!", e, true, true);
        }
    }

    /**
     * アクションをセット
     * @param context コンテキスト
     * @param action セットするアクション
     * @param alarmId アラーム設定のID
     * @param triggerAtTime セットするUTC時刻
     */
    private static void setAction(Context context, String action, long alarmId, long triggerAtTime) {
        try {
            // アラームマネージャの取得
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            // 発行するIntentの生成
            Intent intent = new Intent(context, AlarmClockWidget.class);
            intent.setAction(action);
            intent.putExtra("alarm_id", alarmId);
            PendingIntent operation = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // アクションをセット
            am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, operation);
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Set action error!", e, true, true);
        }
    }

    /**
     * アクションをキャンセル
     * @param context コンテキスト
     * @param action キャンセルするアクション
     */
    private static void cancelAction(Context context, String action) {
        try {
            // アラームマネージャの取得
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            // 発行するIntentの生成
            Intent intent = new Intent(context, AlarmClockWidget.class);
            intent.setAction(action);
            PendingIntent operation = PendingIntent.getBroadcast(context, 0, intent, 0);

            // アクションをキャンセル
            am.cancel(operation); 
        } catch (Exception e) {
            AlarmClockApp.outputError(context, "Cancel action error!", e, true, true);
        }
    }
}
