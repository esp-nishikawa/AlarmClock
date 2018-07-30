package com.esp.android.alarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * 時刻設定用プリファレンス
 */
public class TimePickerPreference extends DialogPreference {

    // プリファレンス保存時のキー名の追加文字列
    private final static String KEY_HOUR = "_hour";
    private final static String KEY_MINUTE = "_minute";

    // 追加属性の名称
    private final static String ATTR_IS_24HOUR = "is24Hour";

    // ２４時間表示
    private final boolean mIs24HourView;

    // TimePicker
    private TimePicker mTimePicker;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param attrs AttributeSet
     */
    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIs24HourView = getAttributeIs24Hour(attrs);
    }

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param attrs AttributeSet
     * @param defStyle スタイル
     */
    public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mIs24HourView = getAttributeIs24Hour(attrs);
    }

    /**
     * ダイアログを表示した時の処理
     */
    @Override
    protected View onCreateDialogView() {
        return createDialogView();
    }

    /**
     * ダイアログを閉じた時の処理
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (mTimePicker != null) {
                // 設定保存
                setCurrentHour(mTimePicker.getCurrentHour());
                setCurrentMinute(mTimePicker.getCurrentMinute());

                // サマリーを設定
                setSummary();
            }
        }
        super.onDialogClosed(positiveResult);
    }

    /**
     * ダイアログのビューを作成
     * @return ビュー
     */
    private View createDialogView() {
        try {
            // 設定を読み込み
            int currentHour = getCurrentHour();
            int currentMinute = getCurrentMinute();

            // TimePickerを作成
            mTimePicker = new TimePicker(getContext());
            mTimePicker.setIs24HourView(mIs24HourView);
            mTimePicker.setCurrentHour(currentHour);
            mTimePicker.setCurrentMinute(currentMinute);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Create dialog view error!", e, true, true);
        }
        return mTimePicker;
    }

    /**
     * サマリーを設定
     */
    public void setSummary() {
        try {
            // 設定を読み込み
            int currentHour = getCurrentHour();
            int currentMinute = getCurrentMinute();

            // サマリーに現在値を設定
            String summary = "";
            if (mIs24HourView) {
                summary = String.format("%02d:%02d", currentHour, currentMinute);
            } else {
                if (currentHour > 12) {
                    summary = String.format("%02d:%02d PM", currentHour - 12, currentMinute);
                } else if (currentHour == 12) {
                    summary = String.format("%02d:%02d PM", 12, currentMinute);
                } else {
                    summary = String.format("%02d:%02d AM", currentHour, currentMinute);
                }
            }
            setSummary(summary);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * AttributeSetから２４時間表示を取得
     * @param attrs AttributeSet
     * @return ２４時間表示
     */
    private boolean getAttributeIs24Hour(AttributeSet attrs) {
        boolean is24HourView = false;
        try {
            String is24Hour = attrs.getAttributeValue(null, ATTR_IS_24HOUR);
            if (is24Hour != null) {
                if (is24Hour.toLowerCase().compareTo("true") == 0) {
                    is24HourView = true;
                }
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get attribute is24hour error!", e, true, true);
        }
        return is24HourView;
    }

    /**
     * 時を取得
     */
    public int getCurrentHour() {
        int currentHour = 0;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            currentHour = sp.getInt(getKey() + KEY_HOUR, currentHour);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get current hour error!", e, true, true);
        }
        return currentHour;
    }

    /**
     * 時を設定
     */
    public void setCurrentHour(int currentHour) { 
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            Editor e = sp.edit();
            e.putInt(getKey() + KEY_HOUR, currentHour);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set current hour error!", e, true, true);
        }
    }

    /**
     * 分を取得
     */
    public int getCurrentMinute() {
        int currentMinute = 0;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            currentMinute = sp.getInt(getKey() + KEY_MINUTE, currentMinute);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get current minute error!", e, true, true);
        }
        return currentMinute;
    }

    /**
     * 分を設定
     */
    public void setCurrentMinute(int currentMinute) { 
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            Editor e = sp.edit();
            e.putInt(getKey() + KEY_MINUTE, currentMinute);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set current minute error!", e, true, true);
        }
    }
}
