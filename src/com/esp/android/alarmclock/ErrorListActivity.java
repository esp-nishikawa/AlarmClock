package com.esp.android.alarmclock;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * エラー一覧画面
 */
public class ErrorListActivity extends Activity {

    // ダイアログのID
    private static final int DIALOG_NO_DATA  = 1;
    private static final int DIALOG_CLEAR  = 2;

    // Adapterオブジェクト
    private ArrayAdapter<String> mAdapter;

    // リストビュー
    private ListView mListView;

    // キャンセルボタン
    private Button mButtonCancel;

    // クリアボタン
    private Button mButtonClear;

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
            setContentView(R.layout.error_list);

            // タイトル設定
            String errorLabel = getResources().getString(R.string.error_label);
            setTitle(errorLabel);

            // エラー一覧を取得
            DataManager dataManager = new DataManager(getApplicationContext());
            List<String> errorList = dataManager.selectErrorInf();
            if (errorList == null || errorList.size() == 0) {
                // データなしダイアログを表示
                showDialog(DIALOG_NO_DATA);
                return;
            }

            // Adapterオブジェクトを作成
            if (mAdapter == null) {
                mAdapter = new ArrayAdapter<String>(ErrorListActivity.this, android.R.layout.simple_list_item_1);
            } else {
                mAdapter.clear();
            }

            // Adapterオブジェクトにリスト項目を追加
            for (String error : errorList) {
                mAdapter.add(error);
            }

            // リストビューにAdapterオブジェクトを設定
            mListView = (ListView)findViewById(R.id.error_listview);
            mListView.setAdapter(mAdapter);

            // リストビューのアイテムが長押しされた時に呼び出されるリスナーを登録
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView)parent;

                    // 長押しされたアイテムを取得
                    String error = (String)listView.getItemAtPosition(position);

                    // エラーを表示
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            // キャンセルボタン押下時の処理
            mButtonCancel = (Button)findViewById(R.id.error_cancel_button);
            mButtonCancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // エラー一覧画面を終了
                    finish();
                }
            });

            // クリアボタン押下時の処理
            mButtonClear = (Button)findViewById(R.id.error_clear_button);
            mButtonClear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    // クリアダイアログを表示
                    showDialog(DIALOG_CLEAR);
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
                mListView.setOnItemLongClickListener(null);
                mListView = null;
            }

            // キャンセルボタン
            if (mButtonCancel != null) {
                mButtonCancel.setOnClickListener(null);
                mButtonCancel = null;
            }

            // クリアボタン
            if (mButtonClear != null) {
                mButtonClear.setOnClickListener(null);
                mButtonClear = null;
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

        // クリアダイアログを作成
        } else if (id == DIALOG_CLEAR) {
            dialog = createClearDialog();
        }
        return dialog;
    }

    /**
     * データなしダイアログを作成
     */
    private Dialog createNoDataDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(ErrorListActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.no_data_message);
            ad.setCancelable(false);
            ad.setNegativeButton(R.string.close_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // エラー一覧画面を終了
                    finish();
                }
            });
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create no data dialog error!", e, true, true);
        }
        return dialog;
    }

    /**
     * クリアダイアログを作成
     */
    private Dialog createClearDialog() {
        Dialog dialog = null;
        try {
            AlertDialog.Builder ad = new AlertDialog.Builder(ErrorListActivity.this);
            ad.setTitle(getTitle());
            ad.setMessage(R.string.clear_error_message);
            ad.setPositiveButton(R.string.ok_label, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // クリア
                    DataManager dataManager = new DataManager(getApplicationContext());
                    dataManager.deleteErrorInf();

                    // 画面を更新
                    releaseContent();
                    prepareContent();
                }
            });
            ad.setNegativeButton(R.string.cancel_label, null);
            dialog = ad.create();
        } catch (Exception e) {
            AlarmClockApp.outputError(getApplicationContext(), "Create clear dialog error!", e, true, true);
        }
        return dialog;
    }
}
