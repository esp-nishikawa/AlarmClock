package com.esp.android.alarmclock;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;

/**
 * 音楽プレイヤーサービス
 */
public class MusicPlayerService extends Service implements
        OnCompletionListener, OnPreparedListener, OnErrorListener {

    // アクション
    public static final String ACTION_MUSIC_PLAY = "com.esp.android.alarmclock.ACTION_MUSIC_PLAY";
    public static final String ACTION_MUSIC_PAUSE = "com.esp.android.alarmclock.ACTION_MUSIC_PAUSE";
    public static final String ACTION_MUSIC_STOP = "com.esp.android.alarmclock.ACTION_MUSIC_STOP";

    // プレイヤー
    private MediaPlayer mPlayer;

    // 状態
    enum State {
        Stopped,    // 停止中
        Preparing,  // 準備中
        Playing,    // 再生中
        Paused      // 一時停止中
    };
    private State mState = State.Stopped;

    // 再生中の音楽
    private MusicItem mPlayingItem;

    // 要求された音楽
    private MusicItem mMusicItem;

    // ボリューム（％）
    private int mVolume = 100;

    // リトライ回数
    private int mRetryCount = 5;

    // ロック用オブジェクト
    private static Object sMusicPlayerLock = new Object();

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
                if (mState == MusicPlayerService.State.Stopped) {
                    // 1000秒経過？
                    if (stopTime + 1000 * 1000 < System.currentTimeMillis()) {
                        break;
                    }
                } else {
                    stopTime = System.currentTimeMillis();
                }
            }
            MusicPlayerService.this.stopSelf();
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
        stopMusic();
    }

    /**
     * サービス開始時の処理
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        // 再生要求
        if (action.equals(ACTION_MUSIC_PLAY)) {
            // パラメータを取得
            mMusicItem = (MusicItem)intent.getSerializableExtra("music_item");
            mVolume = intent.getIntExtra("volume", 100);
            mRetryCount = 5;

            // 停止中の場合は再生準備
            if (mState == State.Stopped) {
                prepareMusic();

            // 準備中なら自動で再生される
            } else if (mState == State.Preparing) {
                // 何もしない

            // 再生中、一時停止中の場合は再生
            } else {
                // 再生する音楽が変更になった場合は再生準備から
                if (!mMusicItem.equals(mPlayingItem)) {
                    prepareMusic();
                } else {
                    playMusic();
                }
            }

        // 一時停止要求
        } else if (action.equals(ACTION_MUSIC_PAUSE)) {
            if (mState == State.Playing) {
                pauseMusic();
            }

        // 停止要求
        } else if (action.equals(ACTION_MUSIC_STOP)) {
            stopMusic();
        }

        return START_NOT_STICKY; // Means we started the service,
                                 // but don't want it to
                                 // restart in case it's killed.
    }

    /**
     * 準備完了時の処理
     */
    public void onPrepared(MediaPlayer player) {
        playMusic();
    }

    /**
     * 再生完了時の処理
     */
    public void onCompletion(MediaPlayer player) {
        stopMusic();
    }

    /**
     * エラー発生時の処理
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {
        AlarmClockApp.outputError(getApplicationContext(),
                "Media player error! Resetting." + " What:" + String.valueOf(what) + " Extra:" + String.valueOf(extra),
                null, true, true);
        stopMusic();

        // リトライ
        if (mRetryCount > 0) {
            mRetryCount--;
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
            }
            prepareMusic();
        }

        return true; // true indicates we handled the error
    }

    /**
     * 再生を準備する
     */
    private void prepareMusic() {
        try {
            synchronized (sMusicPlayerLock) {
                // 状態を準備中にする
                mState = State.Preparing;

                // プレイヤーを作成
                if (mPlayer == null) {
                    mPlayer = new MediaPlayer();
                    mPlayer.setOnPreparedListener(this);
                    mPlayer.setOnCompletionListener(this);
                    mPlayer.setOnErrorListener(this);
                } else {
                    mPlayer.reset();
                }

                // プレイヤーに音楽を設定
                mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mPlayer.setDataSource(getApplicationContext(), mMusicItem.getURI(getApplicationContext()));

                // プレイヤーの準備
                mPlayer.prepareAsync();
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Prepare music error!", e, true, true);
            stopMusic();
        }
    }

    /**
     * 音楽を再生する
     */
    private void playMusic() {
        try {
            synchronized (sMusicPlayerLock) {
                // ボリュームを設定
                mPlayer.setVolume(mVolume/100.0f, mVolume/100.0f);

                // ループ再生
                mPlayer.setLooping(true);

                // 再生
                if (!mPlayer.isPlaying()) {
                    mPlayer.start();
                }
                mPlayingItem = new MusicItem(mMusicItem);

                // 状態を再生中にする
                mState = State.Playing;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Play music error!", e, true, true);
            stopMusic();
        }
    }

    /**
     * 音楽を一時停止する
     */
    private void pauseMusic() {
        try {
            synchronized (sMusicPlayerLock) {
                // 一時停止
                mPlayer.pause();

                // 状態を一時停止中にする
                mState = State.Paused;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Pause music error!", e, true, true);
            stopMusic();
        }
    }

    /**
     * 音楽を停止する
     */
    private void stopMusic() {
        try {
            synchronized (sMusicPlayerLock) {
                // リソースを解放
                if (mPlayer != null) {
                    mPlayer.reset();
                    mPlayer.release();
                    mPlayer = null;
                }
                mPlayingItem = null;

                // stop being a foreground service
                stopForeground(true);

                // 状態を停止中にする
                mState = State.Stopped;
            }
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Stop music error!", e, true, true);
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
