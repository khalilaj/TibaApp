package com.quickiepos.example.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.quickiepos.example.HistorySingleActivity;
import com.quickiepos.example.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView consultId;
    public TextView time;

    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        consultId = (TextView) itemView.findViewById(R.id.consultId);
        time = (TextView) itemView.findViewById(R.id.time);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("consultId", consultId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}
