package com.kongqw.androidbluetoothmanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kongqw.androidbluetoothmanager.R;
import com.kongqw.androidbluetoothmanager.entity.ChatEntity;

import java.util.ArrayList;

/**
 * Created by kongqingwei on 2017/3/16.
 * 聊天列表适配器 entity
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private static final String TAG = "ChatListAdapter";
    private ArrayList<ChatEntity> chatEntities = new ArrayList<>();

    private static final int MESSAGE_TYPE_READ = 0;
    private static final int MESSAGE_TYPE_SEND = 1;
    private final LayoutInflater mLayoutInflater;

    // RecyclerView.ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTimeRead;
        private TextView tvBluetoothNameRead;
        private TextView tvBluetoothMacAddressRead;
        private TextView tvMessageRead;

        private TextView tvTimeSend;
        private TextView tvBluetoothNameSend;
        private TextView tvBluetoothMacAddressSend;
        private TextView tvMessageSend;

        ViewHolder(View v) {
            super(v);
            tvTimeRead = (TextView) v.findViewById(R.id.tv_time_read);
            tvBluetoothNameRead = (TextView) v.findViewById(R.id.tv_bluetooth_name_read);
            tvBluetoothMacAddressRead = (TextView) v.findViewById(R.id.tv_bluetooth_mac_address_read);
            tvMessageRead = (TextView) v.findViewById(R.id.tv_message_read);


            tvTimeSend = (TextView) v.findViewById(R.id.tv_time_send);
            tvBluetoothNameSend = (TextView) v.findViewById(R.id.tv_bluetooth_name_send);
            tvBluetoothMacAddressSend = (TextView) v.findViewById(R.id.tv_bluetooth_mac_address_send);
            tvMessageSend = (TextView) v.findViewById(R.id.tv_message_send);
        }
    }

    public ChatListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void addChatMessage(ChatEntity chatEntity) {
        chatEntities.add(chatEntity);
        notifyDataSetChanged();
    }

    // 用来创建新视图（由布局管理器调用）
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        switch (viewType) {
            case MESSAGE_TYPE_READ:
                vh = new ViewHolder(mLayoutInflater.inflate(R.layout.item_chat_message_read, parent, false));
                break;
            case MESSAGE_TYPE_SEND:
                vh = new ViewHolder(mLayoutInflater.inflate(R.layout.item_chat_message_send, parent, false));
                break;
            default:
                break;
        }
        return vh;
    }

    // 用来替换视图的内容（由布局管理器调用）
    @Override
    public void onBindViewHolder(ChatListAdapter.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case MESSAGE_TYPE_READ:
                holder.tvTimeRead.setText(chatEntities.get(position).getTime());
                holder.tvBluetoothNameRead.setText(chatEntities.get(position).getBluetoothName());
                holder.tvBluetoothMacAddressRead.setText(chatEntities.get(position).getBluetoothMacAddress());
                holder.tvMessageRead.setText(chatEntities.get(position).getMessage());
                break;
            case MESSAGE_TYPE_SEND:
                holder.tvTimeSend.setText(chatEntities.get(position).getTime());
                holder.tvBluetoothNameSend.setText(chatEntities.get(position).getBluetoothName());
                holder.tvBluetoothMacAddressSend.setText(chatEntities.get(position).getBluetoothMacAddress());
                holder.tvMessageSend.setText(chatEntities.get(position).getMessage());
                break;
            default:
                break;
        }
    }

    // 返回数据集的大小（由布局管理器调用）
    @Override
    public int getItemCount() {
        return chatEntities.size();
    }

    @Override
    public int getItemViewType(int position) {
        // return super.getItemViewType(position);
        return chatEntities.get(position).isSend() ? MESSAGE_TYPE_SEND : MESSAGE_TYPE_READ;
    }
}
