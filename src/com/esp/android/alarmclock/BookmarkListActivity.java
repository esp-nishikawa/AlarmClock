package com.esp.android.alarmclock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ブックマーク一覧画面
 */
public class BookmarkListActivity extends Activity {

    // ダイアログのID
    private static final int DIALOG_NO_DATA           = 1;
    private static final int DIALOG_MENU              = 2;
    private static final int DIALOG_DELETE_BOOKMARK   = 3;

    // Adapterオブジェクト
    private MusicItemArrayAdapter mAdapter;

    // リストビュー
    private ListView mListView;

    // OKボタン
    private Button mButtonOk;

    // キャンセルボタン
    private Button mButtonCancel;

    // クリックしたアイテム
    private MusicItem mClickItem;

    // 音楽のボリューム
    private int mMusicVolume = 100;

    /**
     * 音楽リストのViewHolder
     */
    private static class ViewHolder {
        RadioButton buttonRadio;
        TextView textMusicTitle;
        TextView textMusicLength;
    }

    /**
     * 音楽リストのArrayAdapter
     */
    private class MusicItemArrayAdapter extends ArrayAdapter<MusicItem> {
        private final int mResourceId;
        private MusicItem mSelectItem;

        /**
         * コンストラクタ
         */
        public MusicItemArrayAdapter(Context context, int resourceId) {
            super(context, resourceId);
            mResourceId = resourceId;
        }

        /**
         * 選択アイテムの取得
         * @return 音楽アイテム
         */
        public MusicItem getSelectItem() {
            return mSelectItem;
        }

        /**
         * 選択アイテムの設定
         * @param selectItem 音楽アイテム
         */
        public void setSelectItem(MusicItem selectItem) {
            mSelectItem = selectItem;
        }

        /**
         * リストビューが表示される時の処理
         */
        @Override 
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                // リスト項目を取得
                MusicItem musicItem = (MusicItem)getItem(position);

                // Viewオブジェクトがnullの場合はレイアウトファイルより取得
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater)getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(mResourceId, null);

                    // ViewHolderを生成
                    holder = new ViewHolder();
                    holder.buttonRadio = (RadioButton)convertView.findViewById(R.id.music_radiobutton);
                    holder.textMusicTitle = (TextView)convertView.findViewById(R.id.music_title_textview);
                    holder.textMusicLength = (TextView)convertView.findViewById(R.id.music_length_textview);

                    // ViewHolderをセット
                    convertView.setTag(holder);
                } else {
                    // ViewHolderを取得
                    holder = (ViewHolder)convertView.getTag();
                }

                // Radioボタンにクリックイベントを登録する（初回のみ）
                if ( holder.buttonRadio.getTag() == null ) { 
                    holder.buttonRadio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // クリックされたアイテムを取得
                            MusicItem selectItem = (MusicItem)getItem(((Integer)view.getTag()).intValue());

                            // 選択中のアイテムと異なる場合
                            if (!selectItem.equals(mSelectItem)) {
                                // 音楽を停止
                                AlarmClockWidget.musicStop(getApplicationContext());

                                // 選択アイテムを更新
                                mSelectItem = selectItem;

                                // 表示更新
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

                // Radioボタンに行番号をセット（常に最新状態にする）
                holder.buttonRadio.setTag(Integer.valueOf(position));

                // RadioボタンのON/OFFをセット
                if (musicItem.equals(mSelectItem)) {
                    holder.buttonRadio.setChecked(true);
                } else {
                    holder.buttonRadio.setChecked(false);
                }

                // 音楽のタイトルを表示
                holder.textMusicTitle.setText(musicItem.title);

                // 音楽の長さを表示
                if (musicItem.duration < 0) {
                    holder.textMusicLength.setText("");
                } else {
                    holder.textMusicLength.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toSeconds(musicItem.duration) / 60,
                            TimeUnit.MILLISECONDS.toSeconds(musicItem.duration) % 60));
                }
            } catch (Exception e) {
                AlarmClockApp.outputError(getApplicationContext(), "ArrayAdapter getView error!", e, true, true);
            }
            return convertView;
        }
    }

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
            setContentView(R.layout.music_list);

            // パラメータを取得
            Intent intent = getIntent();
            final MusicItem defaultItem = (MusicItem)intent.getSerializableExtra("default_item");
            final int musicType = MusicItem.TYPE_BOOKMARK; // 固定
            mMusicVolume = intent.getIntExtra("music_volume", 100);

            // タイトル設定
            String[] typeList = getResources().getStringArray(R.array.music_type_list);
            setTitle(typeList[musicType]);

            // ブックマーク一覧を取得
            DataManager dataManager = new DataManager(getApplicationContext());
            List<MusicKey> musicKeys = dataManager.selectBookmarks();
            if (musicKeys == null || musicKeys.size() == 0) {
                // データなしダイアログを表示
                showDialog(DIALOG_NO_DATA);
                return;
            }

            // Adapterオブジェクトを作成
            if (mAdapter == null) {
                mAdapter = new MusicItemArrayAdapter(BookmarkListActivity.this, R.layout.music_list_row);
            } else {
                mAdapter.clear();
            }

            // Adapterオブジェクトにリスト項目を追加
            for (MusicKey musicKey : musicKeys) {
                MusicItem item = dataManager.selectMusicItem(musicKey.content, musicKey.id);
                if (item != null) {
                    mAdapter.add(item);

                    // デフォルトアイテムを選択
                    if (mAdapter.getSelectItem() == null && item.equals(defaultItem)) {
                        mAdapter.setSelectItem(item);
                    }
                }
            }

            // 音楽一覧を取得して追加
            List<MusicItem> musicItems = dataManager.selectMusicItems(musicType, null);
            if (musicItems != null) {
                for (MusicItem item : musicItems) {
                    mAdapter.add(item);

                    // デフォルトアイテムを選択
                    if (mAdapter.getSelectItem() == null && item.equals(defaultItem)) {
                        mAdapter.setSelectItem(item);
                    }
                }
            }

            // デフォルトアイテムが選択できなかった場合は先頭のデータを選択
            if (mAdapter.getSelectItem() == null) {
                MusicItem firstItem = mAdapter.getItem(0);
                mAdapter.setSelectItem(firstItem);
            }

            // リストビューにAdapterオブジェクトを設定
            mListView = (ListView)findViewById(R.id.music_listview);
            mListView.setAdapter(mAdapter);

            // リストビューのアイテムがクリックされた時に呼び出されるリスナーを登録
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView)parent;

                    // クリックされたアイテムを取得
                    mClickItem = (MusicItem)listView.getItemAtPosition(position);

                    // 選択中のアイテムと異なる場合
                    if (!mClickItem.equals(mAdapter.getSelectItem())) {
                        // 音楽を停止
                        AlarmClockWidget.musicStop(getApplicationContext());

                        // 選択アイテムを更新
                        mAdapter.setSelectItem(mClickItem);

                        // 表示更新
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

            // リストビューのアイテムが長押しされた時に呼び出されるリスナーを登録
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView)parent;

                    // 音楽を停止
                    AlarmClockWidget.musicStop(getApplicationContext());

                    // 長押しされたアイテムを取得
                    mClickItem = (MusicItem)listView.getItemAtPosition(position);

                    // メニューダイアログを表示
                    if (mClickItem.content != MusicItem.CONTENT_RANDOM) {
                        showDialog(DIALOG_MENU);
                    }

                    return false;
                }
            });

            // OKボタン押下時の処理
            mButtonOk = (Button)findViewById(R.id.music_ok_button);
            mButtonOk.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // 音楽を停止
                    AlarmClockWidget.musicStop(getApplicationContext());

                    // ブックマーク一覧画面を終了
                    finishBookmarkListActivity(RESULT_OK);
                }
            });

            // キャンセルボタン押下時の処理
            mButtonCancel = (Button)findViewById(R.id.music_cancel_button);
            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // 音楽を停止
                    AlarmClockWidget.musicStop(getApplicationContext());

                    // ブックマーク一覧画面を終了
                    finishBookmarkListActivity(RESULT_CANCELED);
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
                mListView.setOnItemLongClickListener(null);
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

            // クリックしたアイテム
            mClickItem = null;
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

        // メニューダイアログを作成
        } else if (id == DIALOG_MENU) {
            if (mClickItem != null) {
                dialog = createMenuDialog();
            }

        // ブックマーク削除ダイアログを作成
        } else if (id == DIALOG_DELETE_BOOKMARK) {
            if (mClickItem != null) {
                dialog = createDeleteBookmarkDialog();
            }
        }
        return dialog;
    }

    /**
     * ダイアログ表示時の処理
     */
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        // ダイアログのタイトルを設定
        if (id == DIALOG_MENU || id == DIALOG_DELETE_BOOKMARK) {
            if (mClickItem != null) {
                ((AlertDialog)dialog).setTitle(mClickItem.title);
            }
        }
    }
    
    /**
     * データなしダイアログを作成
     */
    private Dialog createNoDataDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(BookmarkListActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.no_data_message);
            ad.setCancelable(false);
            ad.setNegativeButton(R.string.close_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ブックマーク一覧画面を終了
                    finishBookmarkListActivity(RESULT_CANCELED);
                }
            });
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create no data dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * メニューダイアログを作成
     */
    private Dialog createMenuDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(BookmarkListActivity.this);
            ad.setTitle(mClickItem.title);
            String[] menuList = getResources().getStringArray(R.array.bookmarklist_menu_list);
            ad.setItems(menuList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                    // 再生
                    if (which == 0) {
                        AlarmClockWidget.musicPlay(getApplicationContext(), mClickItem, mMusicVolume);

                    // ブックマークから削除
                    } else if (which == 1) {
                        showDialog(DIALOG_DELETE_BOOKMARK);

                    // パスを表示
                    } else if (which == 2) {
                        Toast.makeText(getApplicationContext(), mClickItem.getPath(getApplicationContext()), Toast.LENGTH_LONG).show();
                    }
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create menu dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * ブックマーク削除ダイアログを作成
     */
    private Dialog createDeleteBookmarkDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(BookmarkListActivity.this);
            ad.setTitle(mClickItem.title);
            ad.setMessage(R.string.delete_bookmark_message);
            ad.setPositiveButton(R.string.ok_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 削除
                    DataManager dataManager = new DataManager(getApplicationContext());
                    String path = mClickItem.getPath(getApplicationContext());
                    dataManager.deleteBookmark(path);

                    // 画面を更新
                    releaseContent();
                    prepareContent();
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create delete bookmark dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * ブックマーク一覧画面を終了
     * @param resultCode 結果コード
     */
    private void finishBookmarkListActivity(int resultCode) {
        try {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("music_item", mAdapter.getSelectItem());
                setResult(resultCode, intent);
            } else {
                setResult(resultCode);
            }
            finish();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Finish bookmark list activity error!", e, true, true);
        }
    }
}
