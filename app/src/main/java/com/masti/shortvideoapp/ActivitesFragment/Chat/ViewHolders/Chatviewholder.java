package com.masti.shortvideoapp.ActivitesFragment.Chat.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.masti.shortvideoapp.ActivitesFragment.Chat.ChatAdapter;
import com.masti.shortvideoapp.ActivitesFragment.Chat.ChatModel;
import com.masti.shortvideoapp.R;

public class Chatviewholder extends RecyclerView.ViewHolder {
    public TextView message, datetxt, messageSeen;
    public View view;

    public Chatviewholder(View itemView) {
        super(itemView);
        view = itemView;
        this.message = view.findViewById(R.id.msgtxt);
        this.datetxt = view.findViewById(R.id.datetxt);
        messageSeen = view.findViewById(R.id.message_seen);
    }

    public void bind(final ChatModel item, final ChatAdapter.OnLongClickListener long_listener) {
        message.setOnLongClickListener(v -> {
            long_listener.onLongclick(item, v);
            return false;

        });
    }
}
