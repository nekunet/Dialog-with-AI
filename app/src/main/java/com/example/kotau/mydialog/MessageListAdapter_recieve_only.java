package com.example.kotau.mydialog;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MessageListAdapter_recieve_only extends RecyclerView.Adapter<MessageListAdapter_recieve_only.ViewHolder> {


    private Context mContext;
    private List<UserMessage> mMessageList;

    public MessageListAdapter_recieve_only(Context context, List<UserMessage> messageList) {
        mContext = context;
        mMessageList = messageList;

        // debug
        Log.v("Now MessageListAdapter", "bodyr = " + mMessageList.get(0).getMessageBody());
        //
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        //ImageView profileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            nameText = itemView.findViewById(R.id.text_message_name);
            //profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
        }
    }

    @Override
    public int getItemCount() {
        Log.v("Now getItemCount", "count = " + mMessageList.size());
        return mMessageList.size();
    }


    // Inflates the appropriate layout according to the ViewType.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.v("now onBindViewHolder", "");
        UserMessage message = mMessageList.get(position);

        holder.messageText.setText(message.getMessageBody());
        holder.nameText.setText(message.getNickname());
        holder.timeText.setText(message.getCreatedAt());
    }

}