package com.esp.android.alarmclock;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * アラーム一覧画面
 */
public class AlarmListActivity extends Activity {

    // ダイアログのID
    private static final int DIALOG_ALARM_RESET = 1;

    // Adapterオブジェクト
    private AlarmSettingArrayAdapter mAdapter;

    // リストビュー
    private ListView mListView;

    /**
     * アラーム設定リストのViewHolder
     */
    private static class ViewHolder {
        ToggleButton buttonOnOff;
        Button buttonEdit;
        TextView textTitle;
        TextView textRepeat;
        TextView textTime;
    }

    /**
     * アラーム設定リストのArrayAdapter
     */
    private class AlarmSettingArrayAdapter extends ArrayAdapter<AlarmSetting> {
        private final int mResourceId;

        /**
         * コンストラクタ
         */
        public AlarmSettingArrayAdapter(Context context, int resourceId) {
            super(context, resourceId);
            mResourceId = resourceId;
        }

        /**
         * リストビューが表示される時の処理
         */
        @Override 
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                // リスト項目を取得
                AlarmSetting alarmSetting = (AlarmSetting)getItem(position);

                // Viewオブジェクトがnullの場合はレイアウトファイルより取得
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater)getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(mResourceId, null);

                    // ViewHolderを生成
                    holder = new ViewHolder();
                    holder.buttonOnOff = (ToggleButton)convertView.findViewById(R.id.alarm_onoff_button);
                    holder.buttonEdit = (Button)convertView.findViewById(R.id.alarm_edit_button);
                    holder.textTitle = (TextView)convertView.findViewById(R.id.alarm_title_textview);
                    holder.textRepeat = (TextView)convertView.findViewById(R.id.repeat_textview);
                    holder.textTime = (TextView)convertView.findViewById(R.id.alarm_time_textview);

                    // ViewHolderをセット
                    convertView.setTag(holder);
                } else {
                    // ViewHolderを取得
                    holder = (ViewHolder)convertView.getTag();
                }

                // ON/OFFボタンの表示
                holder.buttonOnOff.setChecked(AlarmSetting.intToBoolean(alarmSetting.onOff));

                // ON/OFFボタンにクリックイベントを登録する（初回のみ）
                if ( holder.buttonOnOff.getTag() == null ) {
                    holder.buttonOnOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // クリックされたアイテムを取得
                            AlarmSetting selectSetting = (AlarmSetting)getItem(((Integer)view.getTag()).intValue());

                            // ON/OFFを設定する
                            if (selectSetting.onOff == 0) {
                                selectSetting.onOff = 1;
                            } else {
                                selectSetting.onOff = 0;
                            }
                            saveAlarmOnOff(selectSetting.id, selectSetting.onOff);

                            // アラームを設定し直す
                            AlarmClockWidget.stopAlarm(getApplicationContext());
                            AlarmClockWidget.finishAlarmStopActivity(getApplicationContext());
                            AlarmClockWidget.finishSnoozeReleaseActivity(getApplicationContext());
                            AlarmClockWidget.setAlarm(getApplicationContext());

                            // 表示更新
                            notifyDataSetChanged();
                        }
                    });
                    // テスト用
                    holder.buttonOnOff.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            // クリックされたアイテムを取得
                            AlarmSetting selectSetting = (AlarmSetting)getItem(((Integer)view.getTag()).intValue());
                            if ("reset".equals(selectSetting.title)) {
                                // アラームリセットダイアログを表示
                                showDialog(DIALOG_ALARM_RESET);
                                return true;

                            } else if ("error".equals(selectSetting.title)) {
                                // エラー一覧画面を表示
                                startErrorListActivity();
                                return true;

                            } else if ("alarmtest".equals(selectSetting.title)) {
                                // アラームの開始
                                startAlarm(selectSetting.id);
                                return true;
                            }
                            return false;
                        }
                    });
                }

                // ON/OFFボタンに行番号をセット（常に最新状態にする）
                holder.buttonOnOff.setTag(Integer.valueOf(position));

                // 編集ボタンにクリックイベントを登録する（初回のみ）
                if ( holder.buttonEdit.getTag() == null ) { 
                    holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // クリックされたアイテムを取得
                            AlarmSetting selectSetting = (AlarmSetting)getItem(((Integer)view.getTag()).intValue());

                            // アラーム編集画面を表示
                            startAlarmEditActivity(selectSetting.id);
                        }
                    });
                }

                // 編集ボタンに行番号をセット（常に最新状態にする）
                holder.buttonEdit.setTag(Integer.valueOf(position));

                // アラーム名の表示
                holder.textTitle.setText(alarmSetting.title);

                // 繰返しの表示
                StringBuffer repeat = new StringBuffer();
                if (alarmSetting.repeat == AlarmSetting.Repeat.SpecifyDay) { // 曜日指定
                    String[] weekList = getResources().getStringArray(R.array.week_list);
                    String[] weekListValues = getResources().getStringArray(R.array.week_list_values);
                    for (int i=0; i<weekListValues.length; i++) {
                        String str = weekList[i];
                        String value = weekListValues[i];
                        if (alarmSetting.week.contains(AlarmSetting.Week.valueOf(value))) {
                            repeat.append(str.substring(0,1));
                        }
                    }
                } else { // その他
                    String[] repeatList = getResources().getStringArray(R.array.repeat_list);
                    String[] repeatListValues = getResources().getStringArray(R.array.repeat_list_values);
                    for (int i=0; i<repeatListValues.length; i++) {
                        String value = repeatListValues[i];
                        if (alarmSetting.repeat == AlarmSetting.Repeat.valueOf(value)) {
                            repeat.append(repeatList[i]);
                            break;
                        }
                    }
                }
                holder.textRepeat.setText(repeat);

                // 時間の表示
                GregorianCalendar nextCalendar = alarmSetting.getNextCalendar();
                if (nextCalendar != null) {
                    String format = "yyyy/MM/dd HH:mm";
                    String time = new SimpleDateFormat(format).format(nextCalendar.getTime());
                    holder.textTime.setText(time);
                } else {
                    holder.textTime.setText("----------------");
                }
            } catch (Exception e) {
                AlarmClockApp.outputError(getApplicationContext(), "ArrayAdapter getView error!", e, true, true);
            }
            return convertView;
        }
    }

    /**
     * 起動時の処理
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    /**
     * 終了時の処理
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * フォアグラウンドに入った時の処理
     */
    @Override
    public void onResume() {
        super.onResume();

        // View作成
        prepareContent();
    }

    /**
     * バックグラウンドに入った時の処理
     */
    @Override
    public void onPause() {
        // View解放
        releaseContent();

        super.onPause();
    }

    /**
     * View作成
     */
    private void prepareContent() {
        try {
            setContentView(R.layout.alarm_list);

            // Adapterオブジェクトを作成
            if (mAdapter == null) {
                mAdapter = new AlarmSettingArrayAdapter(AlarmListActivity.this, R.layout.alarm_list_row);
            } else {
                mAdapter.clear();
            }

            // アラーム一覧を取得
            DataManager dataManager = new DataManager(getApplicationContext());
            List<AlarmSetting> alarmSettings = dataManager.selectAlarmSettings();
            if (alarmSettings == null || alarmSettings.size() == 0) {
                // アラームリセットダイアログを表示
                showDialog(DIALOG_ALARM_RESET);
                return;
            }

            // Adapterオブジェクトにリスト項目を追加
            for (AlarmSetting alarmSetting : alarmSettings) {
                mAdapter.add(alarmSetting);
            }

            // リストビューにAdapterオブジェクトを設定
            mListView = (ListView)findViewById(R.id.alarm_listview);
            mListView.setAdapter(mAdapter);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare content error!", e, true, true);
        }
    }

    /**
     * View解放
     */
    private void releaseContent() {
        try {
            // Adapterオブジェクト
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter = null;
            }

            // リストビュー
            if (mListView != null) {
                mListView.setAdapter(null);
                mListView = null;
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

        // アラームリセットダイアログを作成
        if (id == DIALOG_ALARM_RESET) {
            dialog = createAlarmResetDialog();
        }
        return dialog;
    }

    /**
     * アラームリセットダイアログを作成
     */
    private Dialog createAlarmResetDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(AlarmListActivity.this);
            ad.setTitle(R.string.alarm_reset_title);
            ad.setMessage(R.string.alarm_reset_message);
            ad.setPositiveButton(R.string.ok_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // アラームのリセット
                    resetAlarm();

                    // アラーム一覧画面を終了
                    finish();
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create alarm reset dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * アラームの開始
     * @param settingId アラーム設定のID
     */
    private void startAlarm(long settingId) {
        try {
            Intent intent = new Intent(AlarmClockWidget.ACTION_START_ALARM);
            intent.putExtra("alarm_id", settingId);
            getApplicationContext().sendBroadcast(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start alarm error!", e, true, true);
        }
    }

    /**
     * アラームのリセット
     */
    private void resetAlarm() {
        try {
            DataManager dataManager = new DataManager(getApplicationContext());
            dataManager.deleteAlarmSettings();
            Intent intent = new Intent(AlarmClockWidget.ACTION_RESET);
            getApplicationContext().sendBroadcast(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Reset alarm error!", e, true, true);
        }
    }

    /**
     * アラームON/OFFを保存
     * @param settingId アラーム設定のID
     * @param onOff ON/OFFの設定値
     */
    private void saveAlarmOnOff(long settingId, int onOff) {
        try {
            DataManager dataManager = new DataManager(getApplicationContext());
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(settingId);
            alarmSetting.onOff = onOff;
            dataManager.updateAlarmSetting(settingId, alarmSetting);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Save alarm on/off error!", e, true, true);
        }
    }

    /**
     * アラーム編集画面を表示
     * @param settingId アラーム設定のID
     */
    private void startAlarmEditActivity(long settingId) {
        try {
            Intent intent = new Intent(AlarmListActivity.this, AlarmEditActivity.class);
            intent.putExtra("setting_id", settingId);
            startActivity(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start alarm edit activity error!", e, true, true);
        }
    }

    /**
     * エラー一覧画面を表示
     */
    private void startErrorListActivity() {
        try {
            Intent intent = new Intent(AlarmListActivity.this, ErrorListActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start error list activity error!", e, true, true);
        }
    }
}
