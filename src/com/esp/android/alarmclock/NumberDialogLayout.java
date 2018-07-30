package com.esp.android.alarmclock;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

/**
 * 数字入力用ダイアログのレイアウト
 */
public class NumberDialogLayout extends LinearLayout {

    // プラスボタン
    private Button mPlusButton;

    // マイナスボタン
    private Button mMinusButton;

    // テキスト表示
    private EditText mNumberText;

    // シークバー
    private SeekBar mSeekBar;

    // 最小値
    final private int mMinValue;

    // 最大値
    final private int mMaxValue;

    // 現在値
    private int mCurrentValue;

    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param maxValue 最大値
     * @param minValue 最小値
     */
    public NumberDialogLayout(Context context, int minValue, int maxValue) {
        super(context);
        mMinValue = minValue;
        mMaxValue = maxValue;

        // View作成
        prepareContent();
    }

    /**
     * View作成
     */
    private void prepareContent() {
        try {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            final View layout = inflater.inflate(R.layout.number_dialog_layout, this);

            // プラスボタン
            mPlusButton = (Button)layout.findViewById(R.id.number_plus_button);
            mPlusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentValue(mCurrentValue+1);
                }
            });
            LongClickRepeatAdapter.bless(mPlusButton);

            // マイナスボタン
            mMinusButton = (Button)layout.findViewById(R.id.number_minus_button);
            mMinusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentValue(mCurrentValue-1);
                }
            });
            LongClickRepeatAdapter.bless(mMinusButton);

            // テキスト表示
            mNumberText = (EditText)layout.findViewById(R.id.number_edittext);
            mNumberText.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // Do nothing
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Do nothing
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count == 0) {
                        setCurrentValue(mMinValue);
                    } else {
                        try {
                            setCurrentValue(Integer.parseInt(s.toString()));
                        } catch (NumberFormatException e) {}
                    }
                }
            });
            mNumberText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // フォーカスが外れたときソフトキーボードを閉じる
                    if (!hasFocus) {
                        hideSoftInput();
                    }
                }
            });
            mNumberText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // 起動時にソフトキーボードが表示しないようにフォーカスを無効にしているので有効にする
                    if (!mNumberText.isFocusable()) {
                        mNumberText.setFocusable(true);
                        mNumberText.setFocusableInTouchMode(true);
                    }
                    return false;
                }
            });

            // シークバー
            mSeekBar = (SeekBar)layout.findViewById(R.id.number_seekbar);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Do nothing
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Do nothing
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        double value = (double)(mMaxValue-mMinValue)*((double)progress/100.0)+mMinValue;
                        setCurrentValue((int)value);
                    }
                }
            });
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Prepare content error!", e, true, true);
        }
    }

    /**
     * ソフトキーボードを閉じる
     */
    private void hideSoftInput() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mNumberText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Hide soft input error!", e, true, true);
        }
    }

    /**
     * 現在値を取得
     * @return 現在値
     */
    public int getCurrentValue() {
        return mCurrentValue;
    }

    /**
     * 現在値を設定
     * @param currentvalue 現在値
     */
    public void setCurrentValue(int currentValue) {
        try {
            if (currentValue < mMinValue) {
                mCurrentValue = mMinValue;
            } else if (currentValue > mMaxValue) {
                mCurrentValue = mMaxValue;
            } else {
                mCurrentValue = currentValue;
            }

            // プラスボタン
            if (mCurrentValue == mMaxValue) {
                mPlusButton.setEnabled(false);
            } else if (mPlusButton.isEnabled() == false) {
                mPlusButton.setEnabled(true);
            }

            // マイナスボタン
            if (mCurrentValue == mMinValue) {
                mMinusButton.setEnabled(false);
            } else if (mMinusButton.isEnabled() == false) {
                mMinusButton.setEnabled(true);
            }

            // テキスト表示
            String value = String.valueOf(mCurrentValue);
            SpannableStringBuilder sb = (SpannableStringBuilder)mNumberText.getText();
            if (!value.equals(sb.toString())) {
                mNumberText.setText(value);
                mNumberText.setSelection(value.length());
            }

            // シークバー
            double progress = (double)(mCurrentValue-mMinValue)/(double)(mMaxValue-mMinValue)*100.0;
            if (mSeekBar.getProgress() != (int)progress) {
                mSeekBar.setProgress((int)progress);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getContext(), "Set current value error!", e, true, true);
        }
    }
}
