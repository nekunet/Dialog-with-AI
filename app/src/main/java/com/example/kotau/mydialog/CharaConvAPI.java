package com.example.kotau.mydialog;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public class CharaConvAPI extends AsyncTask<String, Void, String>{

    private Listener listener;

    private static final String APIKEY = "YOUR API KEY";


    @Override
    protected String doInBackground(String... params) { // String... は String[] みたいなもん
        // リクエストのレスポンスを格納するための変数
        String responseData = "";

        /* APIにデータを送信 */
        HttpURLConnection urlConnection = null;
        try {
            /* 接続先のURLを決める*/
            // エンドポイントのURL
            String endpoint = "https://api.apigw.smt.docomo.ne.jp/naturalCharaConv/v1/dialogue" + "?APIKEY=" + APIKEY;
            URL url = new URL(endpoint);

            /* URLへのコネクションを取得 */
            urlConnection = (HttpURLConnection) url.openConnection();

            /* 接続設定(メソッドの決定,タイムアウト値,ヘッダー値等)を行う */
            // 接続タイムアウトを設定する。
            urlConnection.setConnectTimeout(100000);
            // レスポンスデータ読み取りタイムアウトを設定する。
            urlConnection.setReadTimeout(100000);
            // ヘッダーにUser-Agentを設定する。
            urlConnection.setRequestProperty("User-Agent", "Android");
            // ヘッダーにAccept-Languageを設定する。
            urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().toString());
            // ヘッダーにContent-Typeを設定する
            urlConnection.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // HTTPのメソッドをPOSTに設定する。
            urlConnection.setRequestMethod("POST");
            // リクエストのボディ送信を許可する
            urlConnection.setDoOutput(true);
            // レスポンスのボディ受信を許可する
            urlConnection.setDoInput(true);

            /* コネクションを開く */
            urlConnection.connect();

            /* リクエストボディの書き込みを行う */
            OutputStream outputStream = urlConnection.getOutputStream();
            HashMap<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("language", "ja-JP");
            jsonMap.put("botId", "CharaConv");
            jsonMap.put("appId", params[0]);
            jsonMap.put("voiceText", params[1]);
            HashMap<String, Object> tMap = new HashMap<>();
            tMap.put("t", params[2]);
            HashMap<String, Object> optionMap = new HashMap<>();
            optionMap.put("option", tMap);
            jsonMap.put("clientData", optionMap);
            jsonMap.put("appRecvTime", "YYYY-MM-DD hh:mm");
            jsonMap.put("appSendTime", "YYYY-MM-DD hh:mm");
            if (jsonMap.size() > 0) {
                //JSON形式の文字列に変換する。
                JSONObject responseJsonObject = new JSONObject(jsonMap);
                String jsonText = responseJsonObject.toString();
                PrintStream ps = new PrintStream(urlConnection.getOutputStream());
                ps.print(jsonText);
                ps.close();
            }
            outputStream.close();

            /* レスポンスボディの読み出しを行う */
            int statusCode = urlConnection.getResponseCode();


            InputStream stream = urlConnection.getInputStream();
            StringBuffer sb = new StringBuffer();
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            responseData = sb.toString();

            Log.v("CharaConvAPI", "レスポンスデータ = " + responseData);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                // コネクションを閉じる。
                urlConnection.disconnect();
            }
        }

        return responseData;
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(String result);
    }

}
