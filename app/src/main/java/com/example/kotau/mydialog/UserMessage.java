package com.example.kotau.mydialog;

public class UserMessage {

    // コンストラクタ
    public UserMessage(){

    }

    // 0:自分 1:相手
    int Sender;

    // 名前
    String Nickname;

    // メッセージ本文
    String MessageBody;

    // 作成された時間
    String CreatedAt;

    public void setSender(int Sender) {
        this.Sender = Sender;
    }

    public void setNickname(String Nickname) {
        this.Nickname = Nickname;
    }

    public void setMessageBody(String MessageBody) {
        this.MessageBody = MessageBody;
    }

    public void setCreatedAt(String CreatedAt) {
        this.CreatedAt = CreatedAt;
    }


    public int getSender() {
        return this.Sender;
    }

    public String getNickname() {
        return this.Nickname;
    }

    public String getMessageBody() {
        return this.MessageBody;
    }

    public String getCreatedAt() {
        return this.CreatedAt;
    }



}
