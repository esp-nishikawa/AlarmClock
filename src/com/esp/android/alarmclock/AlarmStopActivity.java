package com.esp.android.alarmclock;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * アラーム停止画面
 */
public class AlarmStopActivity extends Activity {

    // アクション
    public static final String ACTION_FINISH_ALARM_STOP_ACTIVITY = "com.esp.android.alarmclock.ACTION_FINISH_ALARM_STOP_ACTIVITY";

    // ダイアログのID
    private static final int DIALOG_ALARM_STOPPED = 1;

    // 停止ボタン
    private Button mButtonStop;

    // アラーム名
    private TextView mTextTitle;

    // 現在時刻
    private TextView mTextTime;

    // アラームのID
    private long mAlarmId;

    // 定期的に画面を更新する
    private Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // 現在時刻表示
            setTextTime();

            // 5秒後に再帰呼び出し
            mHandler.postDelayed(mRunnable, 5000);
        }
    };

    // BroadcastReciever
    private BroadcastReceiver mReceiver;

    /**
     * 起動時の処理
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // View作成
        prepareContent();

        // BroadcastRecieverを登録する
        registerBroadcastReceiver();
    }

    /**
     * 終了時の処理
     */
    @Override
    public void onDestroy() {
        // BroadcastRecieverの登録を解除する
        unregisterBroadcastReceiver();

        // View解放
        releaseContent();

        super.onDestroy();
    }

    /**
     * フォアグラウンドに入った時の処理
     */
    @Override
    public void onResume() {
        super.onResume();

        // スクリーンロックを解除する
        disableScreenLock();

        // 画面更新用ハンドラ作成
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    /**
     * バックグラウンドに入った時の処理
     */
    @Override
    public void onPause() {

        // スクリーンロックの解除を戻す
        reenableScreenLock();

        // 画面更新用ハンドラクリア
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onPause();
    }

    /**
     * バックキー押下時の処理
     */
    @Override
    public void onBackPressed() {
        // アラームを停止
        stopAlarm(mAlarmId);
    }

    /**
     * View作成
     */
    private void prepareContent() {
        try {
            setContentView(R.layout.alarm_stop);

            // パラメータを取得
            Intent intent = getIntent();
            mAlarmId = intent.getLongExtra("alarm_id", -1);
            final String alarmKey = intent.getStringExtra("alarm_key");

            // アラームキーチェック
            if (alarmKey == null || alarmKey.length() == 0 ||
                !alarmKey.equals(DataManager.getAlarmKey(getApplicationContext()))) {
                // アラーム停止済みダイアログを表示
                showDialog(DIALOG_ALARM_STOPPED);
                return;
            }

            // アラーム設定取得
            DataManager dataManager = new DataManager(getApplicationContext());
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(mAlarmId);

            // 停止ボタン押下時の処理
            mButtonStop = (Button)findViewById(R.id.stop_button);
            mButtonStop.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // アラームを停止
                    stopAlarm(mAlarmId);
                }
            });

            // アラーム名を表示
            mTextTitle = (TextView)findViewById(R.id.stop_title_textview);
            mTextTitle.setText(alarmSetting.title);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare content error!", e, true, true);
        }
    }

    /**
     * View解放
     */
    private void releaseContent() {
        try {
            // 停止ボタン
            if (mButtonStop != null) {
                mButtonStop.setOnClickListener(null);
                mButtonStop = null;
            }

            // アラーム名
            if (mTextTitle != null) {
                mTextTitle = null;
            }

            // 現在時刻
            if (mTextTime != null) {
                mTextTime = null;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Release content error!", e, true, true);
        }
    }

    /**
     * ダイアログ作成時の処理
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);

        // アラーム停止済みダイアログを作成
        if (id == DIALOG_ALARM_STOPPED) {
            dialog = createAlarmStoppedDialog();
        }
        return dialog;
    }

    /**
     * アラーム停止済みダイアログを作成
     */
    private Dialog createAlarmStoppedDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(AlarmStopActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.alarm_stopped_message);
            ad.setCancelable(false);
            ad.setNegativeButton(R.string.close_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // アラーム停止画面を終了
                    finish();
                }
            });
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create alarm stopped dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * BroadcastRecieverを登録する
     */
    private void registerBroadcastReceiver() {
        try {
            if (mReceiver != null) {
                return;
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_FINISH_ALARM_STOP_ACTIVITY);
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // アラーム停止画面を終了
                    if (ACTION_FINISH_ALARM_STOP_ACTIVITY.equals(intent.getAction())) {
                        finish();
                    }
                }
            };
            registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Register BroadcastReceiver error!", e, true, true);
        }
    }

    /**
     * BroadcastRecieverの登録を解除する
     */
    private void unregisterBroadcastReceiver() {
        try {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver);
                mReceiver = null;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Unregister BroadcastReceiver error!", e, true, true);
        }
    }

    /**
     * スクリーンロックを解除する
     */
    private void disableScreenLock() {
        try {
            Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                          WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                          WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Disable screen lock error!", e, true, true);
        }
    }

    /**
     * スクリーンロックの解除を戻す
     */
    private void reenableScreenLock() {
        try {
            Window win = getWindow();
            win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                          WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                          WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                          WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Reenable screen lock error!", e, true, true);
        }
    }

    /**
     * 現在時刻表示
     */
    private void setTextTime() {
        try {
            if (mTextTime == null) {
                mTextTime = (TextView)findViewById(R.id.stop_time_textview);
            }
            GregorianCalendar nowCalendar = new GregorianCalendar();
            String format = "M/d(E) HH:mm";
            String time = new SimpleDateFormat(format).format(nowCalendar.getTime());
            mTextTime.setText(time);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set text time error!", e, true, true);
        }
    }

    /**
     * アラームの停止
     * @param settingId アラーム設定のID
     */
    private void stopAlarm(long settingId) {
        try {
            Intent intent = new Intent(AlarmClockWidget.ACTION_STOP_ALARM);
            intent.putExtra("alarm_id", settingId);
            getApplicationContext().sendBroadcast(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Stop alarm error!", e, true, true);
        }
    }
}
