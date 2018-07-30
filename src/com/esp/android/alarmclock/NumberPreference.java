package com.esp.android.alarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 数字入力用プリファレンス
 */
public class NumberPreference extends DialogPreference {

    // プリファレンス保存時のキー名の追加文字列
    private final static String KEY_VALUE = "_value";

    // 追加属性の名称
    private final static String ATTR_MIN_VALUE = "minValue";
    private final static String ATTR_MAX_VALUE = "maxValue";

    // 最小値
    private final int mMinValue;

    // 最大値
    private final int mMaxValue;

    // 数字入力用ダイアログのレイアウト
    private NumberDialogLayout mNumberDialogLayout;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param attrs AttributeSet
     */
    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMinValue = getAttributeMinValue(attrs);
        mMaxValue = getAttributeMaxValue(attrs);
    }

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param attrs AttributeSet
     * @param defStyle スタイル
     */
    public NumberPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMinValue = getAttributeMinValue(attrs);
        mMaxValue = getAttributeMaxValue(attrs);
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
            if (mNumberDialogLayout != null) {
                // 設定保存
                setCurrentValue(mNumberDialogLayout.getCurrentValue());

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
            int currentValue = getCurrentValue();

            // レイアウトを作成
            mNumberDialogLayout = new NumberDialogLayout(getContext(), mMinValue, mMaxValue);
            mNumberDialogLayout.setCurrentValue(currentValue);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Create dialog view error!", e, true, true);
        }
        return mNumberDialogLayout;
    }

    /**
     * サマリーを設定
     */
    public void setSummary() {
        try {
            // 設定を読み込み
            int currentValue = getCurrentValue();

            // サマリーに現在値を設定
            String summary = String.valueOf(currentValue);
            setSummary(summary);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set summary error!", e, true, true);
        }
    }

    /**
     * AttributeSetから最小値を取得
     * @param attrs AttributeSet
     * @return 最小値
     */
    private int getAttributeMinValue(AttributeSet attrs) {
        int minValue = 0;
        try {
            String minValueStr = attrs.getAttributeValue(null, ATTR_MIN_VALUE);
            if (minValueStr != null) {
                minValue = Integer.parseInt(minValueStr);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get attribute min value error!", e, true, true);
        }
        return minValue;
    }

    /**
     * AttributeSetから最大値を取得
     * @param attrs AttributeSet
     * @return 最大値
     */
    private int getAttributeMaxValue(AttributeSet attrs) {
        int maxValue = 0;
        try {
            String maxValueStr = attrs.getAttributeValue(null, ATTR_MAX_VALUE);
            if (maxValueStr != null) {
                maxValue = Integer.parseInt(maxValueStr);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get attribute max value error!", e, true, true);
        }
        return maxValue;
    }

    /**
     * 現在値を取得
     * @return 現在値
     */
    public int getCurrentValue() {
        int currentValue = mMinValue;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            currentValue = sp.getInt(getKey() + KEY_VALUE, currentValue);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Get current value error!", e, true, true);
        }
        return currentValue;
    }

    /**
     * 現在値を設定
     * @param currentValue 現在値
     */
    public void setCurrentValue(int currentValue) { 
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            Editor e = sp.edit();
            e.putInt(getKey() + KEY_VALUE, currentValue);
            e.commit();
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set current value error!", e, true, true);
        }
    }
}
