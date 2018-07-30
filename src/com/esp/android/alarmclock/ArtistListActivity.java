package com.esp.android.alarmclock;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * アーティスト一覧画面
 */
public class ArtistListActivity extends Activity {

    // サブ画面のID
    private static final int SUBACTIVITY_MUSIC_LIST = 1;

    // ダイアログのID
    private static final int DIALOG_NO_DATA  = 1;

    // Adapterオブジェクト
    private ArrayAdapter<String> mAdapter;

    // リストビュー
    private ListView mListView;

    // キャンセルボタン
    private Button mButtonCancel;

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
            setContentView(R.layout.artist_list);

            // パラメータを取得
            Intent intent = getIntent();
            final MusicItem defaultItem = (MusicItem)intent.getSerializableExtra("default_item");
            final int musicType = intent.getIntExtra("music_type", MusicItem.TYPE_ALARM);
            final int musicVolume = intent.getIntExtra("music_volume", 100);

            // タイトル設定
            String[] typeList = getResources().getStringArray(R.array.music_type_list);
            setTitle(typeList[musicType]);

            // アーティスト一覧を取得
            DataManager dataManager = new DataManager(getApplicationContext());
            List<String> artistList = dataManager.selectMusicArtists();
            if (artistList == null || artistList.size() == 0) {
                // データなしダイアログを表示
                showDialog(DIALOG_NO_DATA);
                return;
            }

            // Adapterオブジェクトを作成
            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<String>(ArtistListActivity.this, android.R.layout.simple_list_item_single_choice);
            } else {
                mAdapter.clear();
            }

            // Adapterオブジェクトにリスト項目を追加
            for (String artist : artistList) {
                mAdapter.add(artist);
            }

            // リストビューにAdapterオブジェクトを設定
            mListView = (ListView)findViewById(R.id.artist_listview);
            mListView.setAdapter(mAdapter);

            // デフォルトのチェック状態を設定
            boolean check = false;
            if (defaultItem != null) {
                for (int i=0; i<artistList.size(); i++) {
                    String artist = artistList.get(i);
                    if (artist.equals(defaultItem.artist)) {
                        mListView.setItemChecked(i, true);
                        check = true;
                        break;
                    }
                }
            }
            // デフォルトアイテムが選択できなかった場合は先頭のデータを選択
            if (!check) {
                mListView.setItemChecked(0, true);
            }

            // リストビューのアイテムがクリックされた時に呼び出されるリスナーを登録
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView)parent;

                    // クリックされたアイテムを取得
                    String artist = (String)listView.getItemAtPosition(position);

                    // 音楽一覧画面を表示
                    startMusicListActivity(defaultItem, musicType, musicVolume, artist);
                }
            });

            // キャンセルボタン押下時の処理
            mButtonCancel = (Button)findViewById(R.id.artist_cancel_button);
            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // アーティスト一覧画面を終了
                    finishArtistListActivity(RESULT_CANCELED, null);
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
                mListView.setOnItemClickListener(null);
                mListView = null;
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
     * ダイアログ作成時の処理
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);

        // データなしダイアログを作成
        if (id == DIALOG_NO_DATA) {
            dialog = createNoDataDialog();
        }
        return dialog;
    }

    /**
     * データなしダイアログを作成
     */
    private Dialog createNoDataDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(ArtistListActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.no_data_message);
            ad.setCancelable(false);
            ad.setNegativeButton(R.string.close_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // アーティスト一覧画面を終了
                    finishArtistListActivity(RESULT_CANCELED, null);
                }
            });
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create no data dialog error!", e, true, true);
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
                // アーティスト一覧画面を終了
                finishArtistListActivity(RESULT_OK, musicItem);
            } else {
                // アーティスト一覧画面を終了
                finishArtistListActivity(RESULT_CANCELED, null);
            }
        }
    }

    /**
     * 音楽一覧画面を表示
     * @param defaultItem 選択中アイテム
     * @param musicType 音楽の種類
     * @param musicVolume 音楽のボリューム
     * @param artist アーティスト
     */
    private void startMusicListActivity(MusicItem defaultItem, int musicType, int musicVolume, String artist) {
        try {
            Intent intent = new Intent(ArtistListActivity.this, MusicListActivity.class);

            // 選択中アイテムを設定
            intent.putExtra("default_item", defaultItem);

            // 音楽の種類を設定
            intent.putExtra("music_type", musicType);

            // 音楽のボリュームを設定
            intent.putExtra("music_volume", musicVolume);

            // アーティストを設定
            intent.putExtra("artist", artist);

            startActivityForResult(intent, SUBACTIVITY_MUSIC_LIST);
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Start music list activity error!", e, true, true);
        }
    }

    /**
     * アーティスト一覧画面を終了
     * @param resultCode 結果コード
     * @param musicItem 選択したアイテム
     */
    private void finishArtistListActivity(int resultCode, MusicItem musicItem) {
        try {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("music_item", musicItem);
                setResult(resultCode, intent);
            } else {
                setResult(resultCode);
            }
            finish();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Finish artist list activity error!", e, true, true);
        }
    }
}
