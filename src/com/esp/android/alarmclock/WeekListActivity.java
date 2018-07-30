package com.esp.android.alarmclock;

import java.util.EnumSet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 曜日一覧画面
 */
public class WeekListActivity extends Activity {

    // Adapterオブジェクト
    private ArrayAdapter<String> mAdapter;

    // リストビュー
    private ListView mListView;

    // OKボタン
    private Button mButtonOk;

    // キャンセルボタン
    private Button mButtonCancel;

    // 繰返し
    AlarmSetting.Repeat mRepeat;

    /**
     * 起動時の処理
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // View作成
        prepareContent();
    }

    /**
     * 終了時の処理
     */
    @Override
    public void onDestroy() {
        // View解放
        releaseContent();

        super.onDestroy();
    }

    /**
     * View作成
     */
    private void prepareContent() {
        try {
            setContentView(R.layout.week_list);

            // パラメータを取得
            Intent intent = getIntent();
            int defaultWeek = intent.getIntExtra("default_week", 0);
            String repeatValue = intent.getStringExtra("repeat");
            mRepeat = AlarmSetting.Repeat.valueOf(repeatValue);

            // タイトル設定
            String[] repeatList = getResources().getStringArray(R.array.repeat_list);
            String[] repeatListValues = getResources().getStringArray(R.array.repeat_list_values);
            for (int i=0; i<repeatListValues.length; i++) {
                String value = repeatListValues[i];
                if (mRepeat == AlarmSetting.Repeat.valueOf(value)) {
                    setTitle(repeatList[i]);
                    break;
                }
            }

            // Adapterオブジェクトを作成
            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<String>(WeekListActivity.this, android.R.layout.simple_list_item_multiple_choice);
            } else {
                mAdapter.clear();
            }

            // Adapterオブジェクトにリスト項目を追加
            String[] weekList = getResources().getStringArray(R.array.week_list);
            String[] weekListValues = getResources().getStringArray(R.array.week_list_values);
            for (int i=0; i<weekListValues.length; i++) {
                String value = weekListValues[i];
                if (mRepeat == AlarmSetting.Repeat.Weekday &&
                    AlarmSetting.Week.valueOf(value) == AlarmSetting.Week.Sunday) {
                    continue;
                }
                mAdapter.add(weekList[i]);
            }

            // リストビューにAdapterオブジェクトを設定
            mListView = (ListView)findViewById(R.id.week_listview);
            mListView.setAdapter(mAdapter);

            // デフォルトのチェック状態を設定
            EnumSet<AlarmSetting.Week> alarmWeek = AlarmSetting.intToWeekSet(defaultWeek);
            for (int i=0; i<weekListValues.length; i++) {
                String value = weekListValues[i];
                boolean check = alarmWeek.contains(AlarmSetting.Week.valueOf(value));
                for (int j=0; j<mListView.getCount(); j++) {
                    String week = (String)mListView.getItemAtPosition(j);
                    if (weekList[i].equals(week)) {
                        mListView.setItemChecked(j, check);
                        break;
                    }
                }
            }

            // OKボタン押下時の処理
            mButtonOk = (Button)findViewById(R.id.week_ok_button);
            mButtonOk.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // 曜日一覧画面を終了
                    finishWeekListActivity(RESULT_OK);
                }
            });

            // キャンセルボタン押下時の処理
            mButtonCancel = (Button)findViewById(R.id.week_cancel_button);
            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // 曜日一覧画面を終了
                    finishWeekListActivity(RESULT_CANCELED);
                }
            });
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

            // OKボタン
            if (mButtonOk != null) {
                mButtonOk.setOnClickListener(null);
                mButtonOk = null;
            }

            // キャンセルボタン
            if (mButtonCancel != null) {
                mButtonCancel.setOnClickListener(null);
                mButtonCancel = null;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Release content error!", e, true, true);
        }
    }

    /**
     * 曜日一覧画面を終了
     * @param resultCode 結果コード
     */
    private void finishWeekListActivity(int resultCode) {
        try {
            if (resultCode == RESULT_OK) {
                // チェック状態を取得
                SparseBooleanArray checked = mListView.getCheckedItemPositions();

                // パラメータ設定用に変換
                EnumSet<AlarmSetting.Week> alarmWeek = EnumSet.noneOf(AlarmSetting.Week.class);
                String[] weekList = getResources().getStringArray(R.array.week_list);
                String[] weekListValues = getResources().getStringArray(R.array.week_list_values);
                for (int i=0; i<weekListValues.length; i++) {
                    String value = weekListValues[i];
                    if (mRepeat == AlarmSetting.Repeat.Weekday &&
                        AlarmSetting.Week.valueOf(value) == AlarmSetting.Week.Sunday) {
                        continue;
                    }
                    for (int j=0; j<mListView.getCount(); j++) {
                        int key = checked.valueAt(j) ? checked.keyAt(j) : -1;
                        if (key == j) { // チェックON
                            String week = (String)mListView.getItemAtPosition(j);
                            if (weekList[i].equals(week)) {
                                alarmWeek.add(AlarmSetting.Week.valueOf(value));
                                break;
                            }
                        }
                    }
                }

                // パラメータに設定
                Intent intent = new Intent();
                intent.putExtra("alarm_week", AlarmSetting.weekSetToInt(alarmWeek));
                intent.putExtra("repeat", mRepeat.toString());

                setResult(resultCode, intent);
            } else {
                setResult(resultCode);
            }
            finish();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Finish week list activity error!", e, true, true);
        }
    }
}
