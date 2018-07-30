package com.esp.android.alarmclock;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.view.View;

/**
 * アラーム編集画面
 */
public class AlarmEditActivity extends PreferenceActivity
                                    implements OnPreferenceChangeListener {

    // プリファレンス保存時のキー名
    private final static String KEY_MUSIC_CONTENT = "music_content";
    private final static String KEY_MUSIC_ID = "music_id";
    private final static String KEY_MUSIC_PATH = "music_path";
    private final static String KEY_ALARM_YMD_YEAR = "alarm_ymd_year";
    private final static String KEY_ALARM_YMD_MONTH = "alarm_ymd_month";
    private final static String KEY_ALARM_YMD_DAY = "alarm_ymd_day";
    private final static String KEY_ALARM_WEEK = "alarm_week";

    // サブ画面のID
    private static final int SUBACTIVITY_MUSIC_LIST = 1;
    private static final int SUBACTIVITY_WEEK_LIST  = 2;

    // ダイアログのID
    private static final int DIALOG_DATE_PICKER  = 1;

    // OKボタン
    private Button mButtonOk;

    // キャンセルボタン
    private Button mButtonCancel;

    // プリファレンス
    private EditTextPreference mAlarmNamePreference;
    private TimePickerPreference mAlarmTimePreference;
    private ListPreference mRepeatPreference;
    private ListPreference mMusicTypePreference;
    private NumberPreference mMusicVolumePreference;
    private NumberPreference mMusicLengthPreference;
    private CheckBoxPreference mVoicePreference;
    private CheckBoxPreference mVibratorPreference;
    private ListPreference mSnoozeModePreference;
    private NumberPreference mSnoozeLengthPreference;
    private NumberPreference mSnoozeTimesPreference;

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
            setContentView(R.layout.alarm_edit);
            addPreferencesFromResource(R.xml.alarm_preference);

            // パラメータを取得
            Intent intent = getIntent();
            final long settingId = intent.getLongExtra("setting_id", -1);

            // プリファレンスの取得
            mAlarmNamePreference = (EditTextPreference)findPreference("alarm_name");
            mAlarmTimePreference = (TimePickerPreference)findPreference("alarm_time");
            mRepeatPreference = (ListPreference)findPreference("repeat");
            mMusicTypePreference = (ListPreference)findPreference("music_type");
            mMusicVolumePreference = (NumberPreference)findPreference("music_volume");
            mMusicLengthPreference = (NumberPreference)findPreference("music_length");
            mVoicePreference = (CheckBoxPreference)findPreference("voice");
            mVibratorPreference = (CheckBoxPreference)findPreference("vibrator");
            mSnoozeModePreference = (ListPreference)findPreference("snooze_mode");
            mSnoozeLengthPreference = (NumberPreference)findPreference("snooze_length");
            mSnoozeTimesPreference = (NumberPreference)findPreference("snooze_times");

            // リスナーを設定する
            mAlarmNamePreference.setOnPreferenceChangeListener(this);
            mVoicePreference.setOnPreferenceChangeListener(this);
            mVibratorPreference.setOnPreferenceChangeListener(this);
            mSnoozeModePreference.setOnPreferenceChangeListener(this);

            // 繰返し
            mRepeatPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        // 選択された値を取得
                        String param = (String)newValue;
                        AlarmSetting.Repeat repeat = AlarmSetting.Repeat.valueOf(param);

                        // 曜日指定または平日
                        if (repeat == AlarmSetting.Repeat.SpecifyDay ||
                            repeat == AlarmSetting.Repeat.Weekday) {
                            // 曜日一覧画面を表示
                            startWeekListActivity(param);
                            return false; // まだ更新しない

                        // 1回のみ
                        } else if (repeat == AlarmSetting.Repeat.OnceOnly) {
                            // 日付選択ダイアログを表示
                            showDialog(DIALOG_DATE_PICKER);
                            return false; // まだ更新しない

                        // その他
                        } else {
                            setSummary((ListPreference)preference, repeat);
                            return true;
                        }
                    }
                    return false;
                }
            });

            // アラーム音
            mMusicTypePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null) {
                        // 選択された値を取得
                        String param = (String)newValue;

                        // 音楽一覧画面を表示
                        startMusicListActivity(param);

                        return false; // まだ更新しない
                    }
                    return false;
                }
            });

            // OKボタン押下時の処理
            mButtonOk = (Button)findViewById(R.id.edit_ok_button);
            mButtonOk.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // アラーム設定を保存
                    saveAlarmSetting(settingId);

                    // アラームを設定し直す
                    AlarmClockWidget.stopAlarm(getApplicationContext());
                    AlarmClockWidget.finishAlarmStopActivity(getApplicationContext());
                    AlarmClockWidget.finishSnoozeReleaseActivity(getApplicationContext());
                    AlarmClockWidget.setAlarm(getApplicationContext());

                    // アラーム編集画面を終了
                    finish();
                }
            });

            // キャンセルボタン押下時の処理
            mButtonCancel = (Button)findViewById(R.id.edit_cancel_button);
            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // アラーム編集画面を終了
                    finish();
                }
            });

            // アラーム設定の読み込み
            readAlarmSetting(settingId);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare content error!", e, true, true);
        }
    }

    /**
     * View解放
     */
    private void releaseContent() {
        try {
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

            // プリファレンス
            if (mAlarmNamePreference != null) {
                mAlarmNamePreference.setOnPreferenceChangeListener(null);
                mAlarmNamePreference = null;
            }
            if (mAlarmTimePreference != null) {
                mAlarmTimePreference = null;
            }
            if (mRepeatPreference != null) {
                mRepeatPreference.setOnPreferenceChangeListener(null);
                mRepeatPreference = null;
            }
            if (mMusicTypePreference != null) {
                mMusicTypePreference.setOnPreferenceChangeListener(null);
                mMusicTypePreference = null;
            }
            if (mMusicVolumePreference != null) {
                mMusicVolumePreference = null;
            }
            if (mMusicLengthPreference != null) {
                mMusicLengthPreference = null;
            }
            if (mVoicePreference != null) {
                mVoicePreference.setOnPreferenceChangeListener(null);
                mVoicePreference = null;
            }
            if (mVibratorPreference != null) {
                mVibratorPreference.setOnPreferenceChangeListener(null);
                mVibratorPreference = null;
            }
            if (mSnoozeModePreference != null) {
                mSnoozeModePreference.setOnPreferenceChangeListener(null);
                mSnoozeModePreference = null;
            }
            if (mSnoozeLengthPreference != null) {
                mSnoozeLengthPreference = null;
            }
            if (mSnoozeTimesPreference != null) {
                mSnoozeTimesPreference = null;
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

        // 日付選択ダイアログを作成
        if (id == DIALOG_DATE_PICKER) {
            dialog = createDatePickerDialog();
        }
        return dialog;
    }

    /**
     * 日付選択ダイアログを作成
     */
    private Dialog createDatePickerDialog() {
        Dialog dialog = null;
        try {
            // 設定値を取得
            AlarmSetting.Ymd alarmYmd;
            if (AlarmSetting.Repeat.valueOf(mRepeatPreference.getValue()) == AlarmSetting.Repeat.OnceOnly) {
                alarmYmd = getAlarmYmd();
            } else {
                alarmYmd = new AlarmSetting().ymd;
            }

            // 初期値の場合は現在の日付を設定
            GregorianCalendar calendar;
            if (alarmYmd.year == 0) {
                calendar = new GregorianCalendar();
            } else {
                calendar = new GregorianCalendar(alarmYmd.year, alarmYmd.month, alarmYmd.day);
            }

            // DatePickerを作成
            final DatePicker picker = new DatePicker(AlarmEditActivity.this);
            picker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), null);

            // ダイアログを作成
            AlertDialog.Builder ad = new AlertDialog.Builder(AlarmEditActivity.this);
            ad.setTitle(R.string.alarm_date_title);
            ad.setView((View)picker);
            ad.setPositiveButton(R.string.ok_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 設定を保存
                    setAlarmYmd(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                    mRepeatPreference.setValue(AlarmSetting.Repeat.OnceOnly.toString());

                    // サマリーを表示
                    setSummary(mRepeatPreference, AlarmSetting.Repeat.OnceOnly);
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create DatePicker dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * サブ画面から戻った時の処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 音楽一覧画面
        if (requestCode == SUBACTIVITY_MUSIC_LIST) {
            if (resultCode == RESULT_OK) {
                MusicItem musicItem = (MusicItem)intent.getSerializableExtra("music_item");
                if (musicItem != null) {
                    // 選択された値を保存
                    mMusicTypePreference.setValue(String.valueOf(musicItem.type));
                    setMusicKey(musicItem.content, musicItem.id, musicItem.getPath(getApplicationContext()));
                    mMusicTypePreference.setSummary(musicItem.title);
                }
            }

        // 曜日一覧画面
        } else if (requestCode == SUBACTIVITY_WEEK_LIST) {
            if (resultCode == RESULT_OK) {
                // 選択された値を保存
                int alarmWeek = intent.getIntExtra("alarm_week", 0);
                String repeatValue = intent.getStringExtra("repeat");
                setAlarmWeek(alarmWeek);
                mRepeatPreference.setValue(repeatValue);

                // サマリーを表示
                setSummary(mRepeatPreference, AlarmSetting.Repeat.valueOf(repeatValue));
            }
        }
    }

    /**
     * 音楽一覧画面を表示
     * @param musicTypeValue 音楽の種類
     */
    private void startMusicListActivity(String musicTypeValue) {
        try {
            // 音楽の種類によって表示する画面を分ける
            int musicType = Integer.parseInt(musicTypeValue);
            Intent intent;
            if (musicType == MusicItem.TYPE_MUSIC) {
                intent = new Intent(AlarmEditActivity.this, ArtistListActivity.class);
            } else if (musicType == MusicItem.TYPE_BOOKMARK) {
                intent = new Intent(AlarmEditActivity.this, BookmarkListActivity.class);
            } else {
                intent = new Intent(AlarmEditActivity.this, MusicListActivity.class);
            }

            // 選択中アイテムを設定
            DataManager dataManager = new DataManager(getApplicationContext());
            MusicKey musicKey = getMusicKey();
            MusicItem defaultItem = dataManager.selectMusicItem(musicKey.content, musicKey.id);
            intent.putExtra("default_item", defaultItem);

            // 音楽の種類を設定
            intent.putExtra("music_type", musicType);

            // 音楽のボリュームを設定
            intent.putExtra("music_volume", mMusicVolumePreference.getCurrentValue());

            startActivityForResult(intent, SUBACTIVITY_MUSIC_LIST);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start music list activity error!", e, true, true);
        }
    }

    /**
     * 曜日一覧画面を表示
     * @param repeatValue 繰返し
     */
    private void startWeekListActivity(String repeatValue) {
        try {
            Intent intent = new Intent(AlarmEditActivity.this, WeekListActivity.class);

            // 繰返しを設定
            intent.putExtra("repeat", repeatValue);

            // 初期値を設定
            if (repeatValue.equals(mRepeatPreference.getValue())) {
                intent.putExtra("default_week", getAlarmWeek());
            } else {
                intent.putExtra("default_week", AlarmSetting.weekSetToInt(EnumSet.allOf(AlarmSetting.Week.class)));
            }
            startActivityForResult(intent, SUBACTIVITY_WEEK_LIST);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start week list activity error!", e, true, true);
        }
    }

    /**
     * 値が変更された時の処理
     */
    public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {

        if (newValue != null) {
            // newValueの型でサマリーの設定を分ける
            if (newValue instanceof String) {
                // preferenceの型でサマリーの設定を分ける
                if (preference instanceof ListPreference) {
                    setSummary((ListPreference)preference, (String)newValue);
                } else if (preference instanceof EditTextPreference) {
                    setSummary((EditTextPreference)preference, (String)newValue);
                }
            } else if(newValue instanceof Boolean) {
                if (preference instanceof CheckBoxPreference) {
                    setSummary((CheckBoxPreference)preference, ((Boolean)newValue).booleanValue());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * サマリーを設定（エディットテキスト）
     */
    private void setSummary(EditTextPreference ep, String param) {
        try {
            if (param == null) {
                ep.setSummary("");
            } else {
                ep.setSummary(param);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * サマリーを設定（チェックボックス）
     */
    private void setSummary(CheckBoxPreference cp, boolean param) {
        try {
            if (param) {
                cp.setSummary("ON");
            } else {
                cp.setSummary("OFF");
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * サマリーを設定（リスト）
     */
    private void setSummary(ListPreference lp, String param) {
        try {
            if (param == null) {
                lp.setSummary("");
            } else {
                lp.setSummary(getListPreferenceText(lp, param));
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * サマリーを設定（繰返しリスト）
     */
    private void setSummary(ListPreference lp, AlarmSetting.Repeat repeat) {
        try {
            StringBuffer summary = new StringBuffer(getListPreferenceText(lp, repeat.toString()));

            // 曜日指定または平日
            if (repeat == AlarmSetting.Repeat.SpecifyDay ||
                repeat == AlarmSetting.Repeat.Weekday) {
                summary.append(" ");
                EnumSet<AlarmSetting.Week> alarmWeek = AlarmSetting.intToWeekSet(getAlarmWeek());
                String[] weekList = getResources().getStringArray(R.array.week_list);
                String[] weekListValues = getResources().getStringArray(R.array.week_list_values);
                for (int i=0; i<weekListValues.length; i++) {
                    String str = weekList[i];
                    String value = weekListValues[i];
                    if (alarmWeek.contains(AlarmSetting.Week.valueOf(value))) {
                        summary.append(str.substring(0,1));
                    }
                }

            // 1回のみ
            } else if (repeat == AlarmSetting.Repeat.OnceOnly) {
                summary.append(" ");
                AlarmSetting.Ymd alarmYmd = getAlarmYmd();
                summary.append(String.format("%04d/%02d/%02d", alarmYmd.year, alarmYmd.month+1, alarmYmd.day));
            }

            lp.setSummary(summary);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * リストプリファレンスの文字列を取得
     */
    private String getListPreferenceText(ListPreference lp, String param) {
        String text = "";
        try {
            if (param != null) {
                int listId = lp.findIndexOfValue(param);
                if (listId >= 0) {
                    CharSequence[] entries = lp.getEntries();
                    text = (String)entries[listId];
                }
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Get ListPreference text error!", e, true, true);
        }
        return text;
    }

    /**
     * 音楽のキー取得
     * @return 音楽のキー
     */
    private MusicKey getMusicKey() {
        MusicKey musicKey = new MusicKey();
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            musicKey.content = sp.getInt(KEY_MUSIC_CONTENT, musicKey.content);
            musicKey.id = sp.getLong(KEY_MUSIC_ID, musicKey.id);
            musicKey.path = sp.getString(KEY_MUSIC_PATH, musicKey.path);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Get music key error!", e, true, true);
        }
        return musicKey;
    }

    /**
     * 音楽のキー設定
     * @param content 音楽のコンテンツ
     * @param id 音楽のID
     * @param path 音楽のパス
     */
    private void setMusicKey(int content, long id, String path) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Editor e = sp.edit();
            e.putInt(KEY_MUSIC_CONTENT, content);
            e.putLong(KEY_MUSIC_ID, id);
            e.putString(KEY_MUSIC_PATH, path);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set music key error!", e, true, true);
        }
    }

    /**
     * アラーム年月日取得
     * @return 年月日
     */
    private AlarmSetting.Ymd getAlarmYmd() {
        AlarmSetting.Ymd alarmYmd = new AlarmSetting().ymd;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
            alarmYmd.year = sp.getInt(KEY_ALARM_YMD_YEAR, alarmYmd.year);
            alarmYmd.month = sp.getInt(KEY_ALARM_YMD_MONTH, alarmYmd.month);
            alarmYmd.day = sp.getInt(KEY_ALARM_YMD_DAY, alarmYmd.day);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Get alarm ymd error!", e, true, true);
        }
        return alarmYmd;
    }

    /**
     * アラーム年月日設定
     * @param year 年
     * @param month 月
     * @param day 日
     */
    private void setAlarmYmd(int year, int month, int day) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Editor e = sp.edit();
            e.putInt(KEY_ALARM_YMD_YEAR, year);
            e.putInt(KEY_ALARM_YMD_MONTH, month);
            e.putInt(KEY_ALARM_YMD_DAY, day);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set alarm ymd error!", e, true, true);
        }
    }

    /**
     * 曜日取得
     * @return 曜日
     */
    private int getAlarmWeek() {
        int alarmWeek = 0;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
            alarmWeek = sp.getInt(KEY_ALARM_WEEK, alarmWeek);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Get alarm week error!", e, true, true);
        }
        return alarmWeek;
    }

    /**
     * 曜日設定
     * @param alarmWeek 曜日
     */
    private void setAlarmWeek(int alarmWeek) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Editor e = sp.edit();
            e.putInt(KEY_ALARM_WEEK, alarmWeek);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Set alarm week error!", e, true, true);
        }
    }

    /**
     * アラーム設定の読み込み
     * @param settingId アラーム設定のID
     */
    private void readAlarmSetting(long settingId) {
        try {
            DataManager dataManager = new DataManager(getApplicationContext());
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(settingId);
            MusicItem musicItem = dataManager.selectMusicItem(alarmSetting.musicKey.content, alarmSetting.musicKey.id);

            // アラーム名の初期設定
            mAlarmNamePreference.setText(alarmSetting.title);
            setSummary(mAlarmNamePreference, alarmSetting.title);

            // アラーム時刻の初期設定
            mAlarmTimePreference.setCurrentHour(alarmSetting.hour);
            mAlarmTimePreference.setCurrentMinute(alarmSetting.minute);
            mAlarmTimePreference.setSummary();

            // 繰返しの初期設定
            mRepeatPreference.setValue(alarmSetting.repeat.toString());
            setAlarmYmd(alarmSetting.ymd.year, alarmSetting.ymd.month, alarmSetting.ymd.day);
            setAlarmWeek(AlarmSetting.weekSetToInt(alarmSetting.week));
            setSummary(mRepeatPreference, alarmSetting.repeat);

            // アラーム音の初期設定
            mMusicTypePreference.setValue(String.valueOf(musicItem.type));
            setMusicKey(alarmSetting.musicKey.content, alarmSetting.musicKey.id, alarmSetting.musicKey.path);
            mMusicTypePreference.setSummary(musicItem.title);

            // ボリュームの初期設定
            mMusicVolumePreference.setCurrentValue(alarmSetting.musicVolume);
            mMusicVolumePreference.setSummary();

            // 鳴動時間の初期設定
            mMusicLengthPreference.setCurrentValue(alarmSetting.musicLength);
            mMusicLengthPreference.setSummary();

            // 音声の初期設定
            mVoicePreference.setChecked(AlarmSetting.intToBoolean(alarmSetting.voice));
            setSummary(mVoicePreference, AlarmSetting.intToBoolean(alarmSetting.voice));

            // バイブレータの初期設定
            mVibratorPreference.setChecked(AlarmSetting.intToBoolean(alarmSetting.vibrator));
            setSummary(mVibratorPreference, AlarmSetting.intToBoolean(alarmSetting.vibrator));

            // スヌーズモードの初期設定
            mSnoozeModePreference.setValue(alarmSetting.snoozeMode.toString());
            setSummary(mSnoozeModePreference, alarmSetting.snoozeMode.toString());

            // スヌーズ間隔の初期設定
            mSnoozeLengthPreference.setCurrentValue(alarmSetting.snoozeLength);
            mSnoozeLengthPreference.setSummary();

            // スヌーズ回数の初期設定
            mSnoozeTimesPreference.setCurrentValue(alarmSetting.snoozeTimes);
            mSnoozeTimesPreference.setSummary();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Read alarm setting error!", e, true, true);
        }
    }

    /**
     * アラーム設定を保存
     * @param settingId アラーム設定のID
     */
    private void saveAlarmSetting(long settingId) {
        try {
            DataManager dataManager = new DataManager(getApplicationContext());
            AlarmSetting alarmSetting = dataManager.selectAlarmSetting(settingId);
            alarmSetting.title = mAlarmNamePreference.getText();
            alarmSetting.hour = mAlarmTimePreference.getCurrentHour();
            alarmSetting.minute = mAlarmTimePreference.getCurrentMinute();
            AlarmSetting.Ymd alarmYmd = getAlarmYmd();
            alarmSetting.ymd.year = alarmYmd.year;
            alarmSetting.ymd.month = alarmYmd.month;
            alarmSetting.ymd.day = alarmYmd.day;
            alarmSetting.week = AlarmSetting.intToWeekSet(getAlarmWeek());
            alarmSetting.repeat = AlarmSetting.Repeat.valueOf(mRepeatPreference.getValue());
            MusicKey musicKey = getMusicKey();
            alarmSetting.musicKey.content = musicKey.content;
            alarmSetting.musicKey.id = musicKey.id;
            alarmSetting.musicKey.path = musicKey.path;
            alarmSetting.musicVolume = mMusicVolumePreference.getCurrentValue();
            alarmSetting.musicLength = mMusicLengthPreference.getCurrentValue();
            alarmSetting.voice = AlarmSetting.booleanToInt(mVoicePreference.isChecked());
            alarmSetting.vibrator = AlarmSetting.booleanToInt(mVibratorPreference.isChecked());
            alarmSetting.snoozeMode = AlarmSetting.SnoozeMode.valueOf(mSnoozeModePreference.getValue());
            alarmSetting.snoozeLength = mSnoozeLengthPreference.getCurrentValue();
            alarmSetting.snoozeTimes = mSnoozeTimesPreference.getCurrentValue();
            dataManager.updateAlarmSetting(settingId, alarmSetting);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Save alarm setting error!", e, true, true);
        }
    }
}
