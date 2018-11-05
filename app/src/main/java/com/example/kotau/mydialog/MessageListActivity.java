package com.example.kotau.mydialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {

    // appIdを保存するための.txt
    private static final String MESSAGE_APPID_TXTFILE = "message_appId.txt";
    private static final String CHARACONV_APPID_TXTFILE = "characonv_appId.txt";

    // RycycleViewとAdapterの定義
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;

    // メッセージを保存するためのDBHelper
    private MessageDBHelper dbHelper;

    // RecycleViewに表示するためのメッセージリスト
    private List<UserMessage> messageList = new ArrayList<UserMessage>();

    // 非同期通信のためのクラス
    private GetappId getappId;
    private SendMessageToAPI sendmessagetoapi;
    private GetappId2 getappId2;
    private CharaConvAPI characonvapi;

    // Docomoの対話APIに渡すためのappId
    private String appId;
    // Docomoのキャラクタ変換APIに渡すためのappID
    private String appId2;

    // 話し方
    private String character = "";
    private int character_num = 0;

    // チャットボックスのEditText
    private EditText editText;

    // 現在時刻取得の関数
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("MM/dd HH:mm");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    // appIdをセットするための関数
    private void setappId(String appid) {
        appId = appid;
        Log.v("API Debug", "appId = " + appId);
    }

    // appId2をセットするための関数
    private void setappId2(String appid) {
        appId2 = appid;
        Log.v("API Debug", "appId2 = " + appId2);
    }

    // characterをセットするための関数
    private void setcharacter(int num){
        switch (num) {
            case 0:
                character = "";
                break;
            case 1:
                character = "ehime1";
                break;
            case 2:
                character = "ehime2";
                break;
            case 3:
                character = "ehime3";
                break;
            case 4:
                character = "kansai";
                break;
            case 5:
                character = "hakata";
                break;
            case 6:
                character = "fukushima";
                break;
            case 7:
                character = "mie";
                break;
            case 8:
                character = "maiko";
                break;
            case 9:
                character = "ojo";
                break;
            case 10:
                character = "bushi";
                break;
            case 11:
                character = "gyaru";
                break;
            case 12:
                character = "burikko";
                break;
            case 13:
                character = "akachan";
                break;
        }
    }


    // リストにメッセージを追加し、RecycleView更新する関数
    // DBに挿入する部分もここに記入
    private void addmessageList(int sender, String text) {
        UserMessage um = new UserMessage();
        um.setSender(sender);
        um.setMessageBody(text);
        um.setCreatedAt(getNowDate());
        if (sender != 0){
            um.setNickname("AIちゃん");
        } else{
            um.setNickname("me");
        }
        // 現在のリストに追加
        messageList.add(um);

        // DBに保存
        saveMessage(um);

        // アイテムの追加をAdapterに通知し、スクロール
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        mMessageRecycler.scrollToPosition(messageList.size()-1);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        editText = findViewById(R.id.edittext_chatbox);

        // 対話APIに送るappIdの取得
        File message_appid_file = this.getFileStreamPath(MESSAGE_APPID_TXTFILE);
        if( message_appid_file.exists() ){
            setappId(readFile(MESSAGE_APPID_TXTFILE));
        } else{
            GetappId();
        }

        // キャラクラ変換APIに送るappIdの取得
        File characonv_appid_file = this.getFileStreamPath(CHARACONV_APPID_TXTFILE);
        if( characonv_appid_file.exists() ){
            setappId2(readFile(CHARACONV_APPID_TXTFILE));
        } else{
            GetappId2();
        }

        // RecycleViewの初期化
        initRecycleView();

        // SENDボタンのクリックリスナー
        findViewById(R.id.button_chatbox_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // クリック時の処理
                String text = editText.getText().toString();
                editText.getEditableText().clear();

                // ユーザが送ったテキストをリストに追加しAPIに送信
                if(!text.isEmpty()){
                    addmessageList(0, text);
                    SendMessage(text);
                }else{
                    toastMake("メッセージを入力して下さい");
                }

            }
        });


        // キーボードを開いた時の画面の調整（リストの最後尾に）
        mMessageRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom) {
                mMessageRecycler.scrollToPosition(messageList.size()-1);
            }
        });

        // 既存メッセージがあれば画面を開いた時デフォルトでリストの一番下にスクロール
        if(messageList.size() != 0){
            mMessageRecycler.scrollToPosition(messageList.size()-1);
        }

    }


    // オプションメニューを作成する
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // menuにcustom_menuレイアウトを適用
        getMenuInflater().inflate(R.menu.custom_menu, menu);
        // オプションメニュー表示する場合はtrue
        return true;
    }

    // メニュー選択時の処理
    // 話し方の選択をここでやる
    // メッセージ削除の機能をここでやる
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // 押されたメニューのIDで処理を振り分ける
        switch (menuItem.getItemId()){
            // 話し方変更時の処理
            case R.id.menu_character:
                showCharacterDialog();
                break;
            // 削除選択時の処理
            case R.id.menu_delete:
                final int size = messageList.size();
                // リストを削除
                messageList.clear();
                // DBでの削除処理
                dbHelper.clearMessage();
                // 削除をAdapterに通知
                mMessageAdapter.notifyItemRangeRemoved(0, size);
                toastMake("メッセージを削除しました");
                break;
            case R.id.menu_reset_appid:
                resetappId();
            default:
                break;
        }
        return true;
    }



    // デバッグのための関数
    private void initDebugData() {
        UserMessage um1 = new UserMessage();
        um1.setSender(1);
        um1.setNickname("aaa");
        um1.setMessageBody("こんにちは\nああああ\nあああああ\nあああ！!!");
        um1.setCreatedAt("2018/07/21 19:00");

        UserMessage um2 = new UserMessage();
        um2.setSender(0);
        um2.setNickname("bbb");
        um2.setMessageBody("こんにちは!");
        um2.setCreatedAt("2018/07/21 19:00");

        messageList.add(um1);
        messageList.add(um1);
        messageList.add(um1);
        messageList.add(um1);
        messageList.add(um2);
        messageList.add(um1);
        messageList.add(um1);
        messageList.add(um2);

        initRecycleView();
    }


    // RecycleViewの初期化
    private void initRecycleView() {
        // DBから前のメッセージを読み込む
        dbHelper = new MessageDBHelper(this);
        messageList = dbHelper.MessageList("");

        mMessageRecycler = findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void SendMessage(String inputtext) {
        // APIにメッセージの送信
        sendmessagetoapi = new SendMessageToAPI();
        sendmessagetoapi.setListener(createListener2());
        sendmessagetoapi.execute(appId, inputtext);
    }

    private void GetappId() {
        // appIdの取得
        getappId = new GetappId();
        getappId.setListener(createListener());
        getappId.execute();
    }

    private void GetappId2() {
        // appId2の取得
        getappId2 = new GetappId2();
        getappId2.setListener(createListener3());
        getappId2.execute();
    }

    private void CharaConv(String inputtext) {
        // APIにメッセージを送信
        characonvapi = new CharaConvAPI();
        characonvapi.setListener(createListener4());
        characonvapi.execute(appId2, inputtext, character);
    }


    @Override
    protected void onDestroy() {
        getappId.setListener(null);
        sendmessagetoapi.setListener(null);
        getappId2.setListener(null);
        characonvapi.setListener(null);
        super.onDestroy();
    }

    // GetappIdの非同期通信のリスナー
    private GetappId.Listener createListener() {
        return new GetappId.Listener() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.v("GetappId Listener", "result = " + result);

                    // GerappIdの結果(JSON)がStringで返ってくるのでJSONObjectに変換してappIdをセットする
                    JSONObject obj = new JSONObject(result);
                    setappId(obj.getString("appId"));

                    // テキストファイルに保存
                    saveFile(MESSAGE_APPID_TXTFILE, obj.getString("appId"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // SendMessageToAPIの非同期通信のリスナー
    private SendMessageToAPI.Listener createListener2() {
        return new SendMessageToAPI.Listener() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.v("Debug", "SendMessageToAPI Listener result = " + result);

                    // 結果(JSON)がStringで返ってくるのでJSONObjectに変換して話し方を変換する関数にわたす
                    JSONObject obj = new JSONObject(result);
                    // 指定された話し方に変換
                    CharaConv(obj.getJSONObject("systemText").getString("expression"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // GetappId2の非同期通信のリスナー
    private GetappId2.Listener createListener3() {
        return new GetappId2.Listener() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.v("GetappId2 Listener", "result = " + result);

                    // GerappId2の結果(JSON)がStringで返ってくるのでJSONObjectに変換してappIdをセットする
                    JSONObject obj = new JSONObject(result);
                    setappId2(obj.getString("appId"));

                    // テキストファイルに保存
                    saveFile(CHARACONV_APPID_TXTFILE, obj.getString("appId"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // CharaConvの非同期通信のリスナー
    private CharaConvAPI.Listener createListener4() {
        return new CharaConvAPI.Listener() {
            @Override
            public void onSuccess(String result) {
                try {
                    Log.v("Debug", "CharaConvAPI Listener result = " + result);

                    // 結果(JSON)がStringで返ってくるのでJSONObjectに変換してaddmessageList関数に渡す
                    JSONObject obj = new JSONObject(result);
                    addmessageList(1, obj.getJSONObject("systemText").getString("expression"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }




    // メッセージをDBに保存するための関数
    private void saveMessage(UserMessage um) {
        dbHelper.saveNewMessage(um);
    }

    // ファイルをwriteする関数
    public void saveFile(String file, String str) {
        try (FileOutputStream  fileOutputstream = openFileOutput(file, Context.MODE_PRIVATE) ){
            fileOutputstream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ファイルをreadする関数
    public String readFile(String file) {
        String text = null;
        // try-with-resources
        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(new InputStreamReader(fileInputStream,"UTF-8"))) {
                String lineBuffer;
                while( (lineBuffer = reader.readLine()) != null ) {
                    text = lineBuffer;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }


    // appIdをリセットする関数
    private void resetappId() {
        File message_appid_file = this.getFileStreamPath(MESSAGE_APPID_TXTFILE);
        if( message_appid_file.exists() ){
            message_appid_file.delete();
        }

        File characonv_appid_file = this.getFileStreamPath(CHARACONV_APPID_TXTFILE);
        if( characonv_appid_file.exists() ){
            characonv_appid_file.delete();
        }

        GetappId();
        GetappId2();

        toastMake("appIDをリセットしました");
    }


    // 話し方変更のダイアログを表示する関数
    public void showCharacterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.character_dialog_title);

        //list of items
        String[] items = getResources().getStringArray(R.array.character_array);

        final int prev_characternum = character_num;


        builder.setSingleChoiceItems(items, character_num, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // item selected logic
                        Log.v("Dialog", "choice item which = " + which);
                        character_num = which;

                    }
                });

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        Log.v("Dialog", "set positive which = " + which);
                        setcharacter(character_num);
                        //toastMake("話し方を変更しました");
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                        character_num = prev_characternum;
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }




    // Toastを表示する関数
    private void toastMake(String message){
        // 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        // 位置調整
        //toast.setGravity(Gravity.CENTER, x, y);
        toast.show();
    }

}
