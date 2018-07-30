package com.esp.android.alarmclock;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

/**
 * 音声読み上げサービス
 */
public class TextToSpeechService extends Service implements OnInitListener {

    // アクション
    public static final String ACTION_VOICE_PLAY = "com.esp.android.alarmclock.ACTION_VOICE_PLAY";
    public static final String ACTION_VOICE_STOP = "com.esp.android.alarmclock.ACTION_VOICE_STOP";

    // TextToSpeech
    private TextToSpeech mTextToSpeech;

    // 状態
    enum State {
        Stopped,    // 停止中
        Preparing,  // 準備中
        Playing     // 再生中
    };
    private State mState = State.Stopped;

    // ロック用オブジェクト
    private static Object sTextToSpeechLock = new Object();

    // 時刻読み上げスレッド
    private boolean bSpeechTimeThread;
    private Thread mSpeechTimeThread;

    // 停止後、1000秒再生がなかったらサービスを止める
    private Thread mSelfStopThread = new Thread() {
        public void run() {
            long stopTime = System.currentTimeMillis();
            while (true) {
                // 10秒スリープ
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                }
                // 停止中？
                if (mState == TextToSpeechService.State.Stopped) {
                    // 1000秒経過？
                    if (stopTime + 1000 * 1000 < System.currentTimeMillis()) {
                        break;
                    }
                } else {
                    stopTime = System.currentTimeMillis();
                }
            }
            TextToSpeechService.this.stopSelf();
        }
    };

    /**
     * サービス起動時の処理
     */
    @Override
    public void onCreate() {
        mSelfStopThread.start();
    }

    /**
     * サービス終了時の処理
     */
    @Override
    public void onDestroy() {
        stopTextToSpeech();
    }

    /**
     * サービス開始時の処理
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        // 再生要求
        if (action.equals(ACTION_VOICE_PLAY)) {
            // 音声読み上げを準備する
            if (mState == State.Stopped) {
                prepareTextToSpeech();
            }

        // 停止要求
        } else if (action.equals(ACTION_VOICE_STOP)) {
            stopTextToSpeech();
        }

        return START_NOT_STICKY; // Means we started the service,
                                 // but don't want it to
                                 // restart in case it's killed.
    }

    /**
     * 準備完了時の処理
     */
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 音声読み上げを開始する
            startTextToSpeech();
        } else {
            AlarmClockApp.outputError(getApplicationContext(), "Init TextToSpeech error! Resetting.", null, true, true);
            stopTextToSpeech();
        }
    }

    /**
     * 音声読み上げを準備する
     */
    private void prepareTextToSpeech() {
        try {
            synchronized (sTextToSpeechLock) {
                // 状態を準備中にする
                mState = State.Preparing;

                // TextToSpeechを作成
                mTextToSpeech = new TextToSpeech(getApplicationContext(), this);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare TextToSpeech error!", e, true, true);
            stopTextToSpeech();
        }
    }

    /**
     * 音声読み上げを開始する
     */
    private void startTextToSpeech() {
        try {
            synchronized (sTextToSpeechLock) {
                // 音の高低を設定
                float pitch = 1.0f;
                mTextToSpeech.setPitch(pitch);

                // 話すスピードを設定
                float rate = 0.8f;
                mTextToSpeech.setSpeechRate(rate);

                // 対象言語のロケールを設定
                Locale locale = Locale.getDefault();
                if (mTextToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) { 
                    mTextToSpeech.setLanguage(locale);
                }

                // 時刻読み上げスレッドを開始する
                startSpeechTimeThread();

                // 状態を再生中にする
                mState = State.Playing;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start TextToSpeech error!", e, true, true);
            stopTextToSpeech();
        }
    }

    /**
     * 音声読み上げを停止する
     */
    private void stopTextToSpeech() {
        try {
            synchronized (sTextToSpeechLock) {
                // TextToSpeechの終了
                if (mTextToSpeech != null) {
                    // 読み上げ中なら止める
                    if (mTextToSpeech.isSpeaking()) {
                        mTextToSpeech.stop();
                    }
                    mTextToSpeech.shutdown();
                    mTextToSpeech = null;
                }

                // 時刻読み上げスレッドを終了する
                stopSpeechTimeThread();

                // stop being a foreground service
                stopForeground(true);

                // 状態を停止中にする
                mState = State.Stopped;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Stop TextToSpeech error!", e, true, true);
        }
    }

    /**
     * 時刻を読み上げる
     */
    private void speechTime() {
        try {
            synchronized (sTextToSpeechLock) {
                // 時刻を取得
                GregorianCalendar nowCalendar = new GregorianCalendar();
                String format = "HH:mm";
                String time = new SimpleDateFormat(format).format(nowCalendar.getTime());

                // ボリュームを設定
                HashMap<String, String> params = null;
                params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(1.0f));

                // 読み上げ開始
                mTextToSpeech.speak(time, TextToSpeech.QUEUE_ADD, params);
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Speech time error!", e, true, true);
            stopTextToSpeech();
        }
    }

    /**
     * 時刻読み上げスレッドを開始する
     */
    private void startSpeechTimeThread() {
        try {
            mSpeechTimeThread = new Thread() {
                public void run() {
                    while (bSpeechTimeThread) {
                        // 10秒スリープ
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                        }

                        // 再生中なら時刻を読み上げる
                        if (bSpeechTimeThread && mState == TextToSpeechService.State.Playing) {
                            speechTime();
                        }
                    }
                }
            };
            bSpeechTimeThread = true;
            mSpeechTimeThread.start();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start SpeechTimeThread error!", e, true, true);
        }
    }

    /**
     * 時刻読み上げスレッドを終了する
     */
    private void stopSpeechTimeThread() {
        try {
            bSpeechTimeThread = false;
            if (mSpeechTimeThread != null) {
                if (mSpeechTimeThread.isAlive()) {
                    mSpeechTimeThread.interrupt();
                    try {
                        mSpeechTimeThread.join();
                    } catch (InterruptedException e) {
                    }
                }
                mSpeechTimeThread = null;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Stop SpeechTimeThread error!", e, true, true);
        }
    }

    /**
     * サービスバインド時の処理
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
