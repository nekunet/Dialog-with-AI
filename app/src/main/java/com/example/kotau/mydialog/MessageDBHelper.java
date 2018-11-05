package com.example.kotau.mydialog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kotau.mydialog.UserMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageDBHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "message.db";
    private static final int DATABASE_VERSION = 1 ;
    public static final String TABLE_NAME = "Message";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_MESSAGEBODY = "messagebody";
    public static final String COLUMN_CREATEDAT = "createdat";


    public MessageDBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENDER + " INTEGER NOT NULL, " +
                COLUMN_NICKNAME + " TEXT NOT NULL, " +
                COLUMN_MESSAGEBODY + " TEXT NOT NULL, " +
                COLUMN_CREATEDAT + " TEXT NOT NULL); "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    /**create record**/
    public void saveNewMessage(UserMessage um) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, um.getSender());
        values.put(COLUMN_NICKNAME, um.getNickname());
        values.put(COLUMN_MESSAGEBODY, um.getMessageBody());
        values.put(COLUMN_CREATEDAT, um.getCreatedAt());

        // insert
        db.insert(TABLE_NAME,null, values);
        db.close();
    }


    /** Query records, give options to filter results **/
    public List<UserMessage> MessageList(String filter) {
        String query;
        if(filter.equals("")){
            //regular query
            query = "SELECT  * FROM " + TABLE_NAME;
        }else{
            // filter results by filter option provided
            // 今回のメッセージDBでは使わない
            query = "SELECT  * FROM " + TABLE_NAME + " ORDER BY "+ filter;
        }

        List<UserMessage> messageList = new ArrayList<UserMessage>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        UserMessage um;

        if (cursor.moveToFirst()) {
            do {
                um = new UserMessage();

                //um.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                um.setSender( Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_SENDER))) );
                um.setNickname(cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME)));
                um.setMessageBody(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGEBODY)));
                um.setCreatedAt(cursor.getString(cursor.getColumnIndex(COLUMN_CREATEDAT)));

                messageList.add(um);
            } while (cursor.moveToNext());
        }

        return messageList;
    }

    // テーブル（メッセージ）を削除する関数
    public void clearMessage() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(" CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SENDER + " INTEGER NOT NULL, " +
                COLUMN_NICKNAME + " TEXT NOT NULL, " +
                COLUMN_MESSAGEBODY + " TEXT NOT NULL, " +
                COLUMN_CREATEDAT + " TEXT NOT NULL); "
        );
        db.close();
    }


}
