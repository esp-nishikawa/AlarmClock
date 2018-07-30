package com.esp.android.alarmclock;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

/**
 * 長押ししたらクリック処理をリピートする処理を付加するアダプタ
 */
public class LongClickRepeatAdapter {

    // 連続してボタンを押す間隔のデフォルト値 (ms)
    private static final int REPEAT_INTERVAL = 100;

    // boolean値を保持するためのクラス
    private static class BooleanWrapper {
        public boolean value;
        public BooleanWrapper(boolean value) {
            this.value = value;
        }
    }

    /**
     * Viewに長押し時のリピート処理を付加する
     * @param view 付加対象のView
     */
    public static void bless(View view) {
        bless(REPEAT_INTERVAL, view);
    }

    /**
     * リピート間隔を指定して、Viewに長押しリピート処理を付加する
     * @param repeatInterval 連続してボタンを押す間隔(ms)
     * @param view 付加対象のView
     */
    public static void bless(final int repeatInterval, final View view) {
        final Handler handler = new Handler();
        final BooleanWrapper isContinue = new BooleanWrapper(false);

        final Runnable repeatRunnable = new Runnable() {
            @Override
            public void run() {
                // 連打フラグをみて処理を続けるか判断する
                if (!isContinue.value) {
                    return;
                }

                // 無効状態なら処理を抜ける
                if (!view.isEnabled()) {
                    return;
                }

                // クリック処理を実行する
                view.performClick();

                // 連打間隔を過ぎた後に、再び自分を呼び出す
                handler.postDelayed(this, repeatInterval);
            }
        };

        // 長押しイベント
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 長押しをきっかけに連打を開始する
                isContinue.value = true;
                handler.post(repeatRunnable);

                return true;
            }
        });

        // タッチイベント
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // キーから指が離されたら連打をオフにする
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isContinue.value = false;
                }
                return false;
            }
        });
    }
}
