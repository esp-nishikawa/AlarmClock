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
import android.widget.Button;
import android.widget.TextView;

/**
 * スヌーズ解除画面
 */
public class SnoozeReleaseActivity extends Activity {

    // アクション
    public static final String ACTION_FINISH_SNOOZE_RELEASE_ACTIVITY = "com.esp.android.alarmclock.ACTION_FINISH_SNOOZE_RELEASE_ACTIVITY";

    // ダイアログのID
    private static final int DIALOG_ALARM_STOPPED = 1;
    private static final int DIALOG_SNOOZE_RELEASE = 2;

    // アラーム時刻
    long mAlarmTime = -1;

    // 解除ボタン
    private Button mButtonRelease;

    // アラーム名
    private TextView mTextTitle;

    // 残り回数
    private TextView mTextSnoozeTimes;

    // 現在時刻
    private TextView mTextTime;

    // 残り時間
    private TextView mTextRemainLength;

    // 定期的に画面を更新する
    private Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // 現在時刻と残り時間を表示
            setTextTimeAndRemainLength();

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

        // 画面更新用ハンドラ作成
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    /**
     * バックグラウンドに入った時の処理
     */
    @Override
    public void onPause() {
        // 画面更新用ハンドラクリア
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
        super.onPause();
    }

    /**
     * View作成
     */
    private void prepareContent() {
        try {
            setContentView(R.layout.snooze_release);

            // パラメータを取得
            Intent intent = getIntent();
            final long alarmId = intent.getLongExtra("alarm_id", -1);
            mAlarmTime = intent.getLongExtra("alarm_time", -1);
            final int remainTimes = intent.getIntExtra("remain_times", -1);
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
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(alarmId);

            // 解除ボタン押下時の処理
            mButtonRelease = (Button)findViewById(R.id.release_button);
            mButtonRelease.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // スヌーズ解除ダイアログを表示
                    showDialog(DIALOG_SNOOZE_RELEASE);
                }
            });

            // アラーム名を表示
            mTextTitle = (TextView)findViewById(R.id.snooze_title_textview);
            mTextTitle.setText(alarmSetting.title);

            // 残り回数を表示
            mTextSnoozeTimes = (TextView)findViewById(R.id.snooze_times_textview);
            mTextSnoozeTimes.setText(String.valueOf(remainTimes+1));
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare content error!", e, true, true);
        }
    }

    /**
     * View解放
     */
    private void releaseContent() {
        try {
            // 解除ボタン
            if (mButtonRelease != null) {
                mButtonRelease.setOnClickListener(null);
                mButtonRelease = null;
            }

            // アラーム名
            if (mTextTitle != null) {
                mTextTitle = null;
            }

            // 残り回数
            if (mTextSnoozeTimes != null) {
                mTextSnoozeTimes = null;
            }

            // 現在時刻
            if (mTextTime != null) {
                mTextTime = null;
            }

            // 残り時間
            if (mTextRemainLength != null) {
                mTextRemainLength = null;
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

        // スヌーズ解除ダイアログを作成
        } else if (id == DIALOG_SNOOZE_RELEASE) {
            dialog = createSnoozeReleaseDaialog();
        }
        return dialog;
    }

    /**
     * アラーム停止済みダイアログを作成
     */
    private Dialog createAlarmStoppedDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(SnoozeReleaseActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.alarm_stopped_message);
            ad.setCancelable(false);
            ad.setNegativeButton(R.string.close_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // スヌーズ解除画面を終了
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
     * スヌーズ解除ダイアログを作成
     */
    private Dialog createSnoozeReleaseDaialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(SnoozeReleaseActivity.this);
            ad.setTitle(R.string.snooze_release_title);
            ad.setMessage(R.string.snooze_release_message);
            ad.setPositiveButton(R.string.ok_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // アラームを停止
                    AlarmClockWidget.stopAlarm(getApplicationContext());

                    // 次のアラームを設定
                    AlarmClockWidget.setAlarm(getApplicationContext());

                    // スヌーズ解除画面を終了
                    finish();

                    // アラーム停止画面を終了
                    AlarmClockWidget.finishAlarmStopActivity(getApplicationContext());
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create snooze release dialog error!", e, true, true);
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
            intentFilter.addAction(ACTION_FINISH_SNOOZE_RELEASE_ACTIVITY);
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // スヌーズ解除画面を終了
                    if (ACTION_FINISH_SNOOZE_RELEASE_ACTIVITY.equals(intent.getAction())) {
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
     * 現在時刻と残り時間を表示
     */
    private void setTextTimeAndRemainLength() {
        try {
            // 現在時刻
            if (mTextTime == null) {
                mTextTime = (TextView)findViewById(R.id.snooze_time_textview);
            }
            GregorianCalendar nowCalendar = new GregorianCalendar();
            String format = "M/d(E) HH:mm";
            String time = new SimpleDateFormat(format).format(nowCalendar.getTime());
            mTextTime.setText(time);

            // 残り時間
            if (mTextRemainLength == null) {
                mTextRemainLength = (TextView)findViewById(R.id.snooze_length_textview);
            }
            long remainLength = (mAlarmTime - nowCalendar.getTimeInMillis()) / (60*1000);
            if (remainLength < 0) {
                mTextRemainLength.setText("");
            } else {
                mTextRemainLength.setText(String.valueOf(remainLength+1));
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set text time and remain length error!", e, true, true);
        }
    }
}
