package com.esp.android.alarmclock;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * Applicationクラス
 */
public class AlarmClockApp extends Application {

    /**
     * 起動時の処理
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // キャッチされなかった例外発生時の処理
        final UncaughtExceptionHandler savedUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            private volatile boolean mCrashing = false;
            @Override
            public void uncaughtException(Thread thread, Throwable t) {
                if (mCrashing) {
                    return;
                }
                mCrashing = true;
                outputError(AlarmClockApp.this, "Application Fatal!", t, false, true);
                savedUncaughtExceptionHandler.uncaughtException(thread, t);
            }
        });
    }

    /**
     * 終了時の処理
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 使用出来るメモリが少なくなった時の処理
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        outputError(AlarmClockApp.this, "Low Memory.", null, true, true);
        checkMemory(AlarmClockApp.this);
    }

    /**
     * エラーを出力する
     * @param context コンテキスト
     * @param text 通知する文字列
     * @param th Throwable
     * @param bToast トーストを表示する場合はtrue
     * @param bDB DBに出力する場合はtrue
     */
    public static void outputError(Context context, String text, Throwable th, boolean bToast, boolean bDB) {
        StringBuffer outputText = new StringBuffer(text);
        if (th != null) {
            outputText.append(" ");
            outputText.append(th.toString());
        }
        if (bToast) {
            Intent intent = new Intent(AlarmClockWidget.ACTION_ERROR);
            intent.putExtra("text", outputText.toString());
            context.sendBroadcast(intent);
        }
        if (bDB) {
            GregorianCalendar nowCalendar = new GregorianCalendar();
            String format = "[yyyy/MM/dd HH:mm:ss]";
            String time = new SimpleDateFormat(format).format(nowCalendar.getTime());
            DataManager dataManager = new DataManager(context);
            dataManager.insertErrorInf(time + " " + outputText.toString());
        }
    }

    /**
     * メモリをチェック
     * @param context コンテキスト
     */
    private static double sMaxRatio = 0.0;
    public static void checkMemory(Context context) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long max = runtime.maxMemory() / 1024;
            long total = runtime.totalMemory() / 1024;
            long free = runtime.freeMemory() / 1024;
            long used = total - free;
            double ratio = ((double)used / (double)max);
            if (ratio > sMaxRatio) {
                sMaxRatio = ratio;
                DecimalFormat f1 = new DecimalFormat("#,###KB");
                DecimalFormat f2 = new DecimalFormat("##.#");
                String info = "Used memory = " + f1.format(used) + " (" + f2.format(ratio * 100.0) + "%)";
                outputError(context, info, null, false, true);
                //System.gc();
            }
        } catch (Exception e) {
            outputError(context, "Check memory error!", e, true, true);
        }
    }
}
