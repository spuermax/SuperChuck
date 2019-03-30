package com.developers.super_chuck.internal.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.developers.super_chuck.R;
import com.developers.super_chuck.internal.data.HttpTransaction;
import com.developers.super_chuck.internal.view.TransactionListFragment;

/**
 * @Author yinzh
 * @Date 2019/3/26 13:34
 * @Description
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder>{

    private final Context context;
    private final TransactionListFragment.OnListFragmentInteractionListener listener;
    private final CursorAdapter cursorAdapter;

    private final int colorDefault;
    private final int colorRequested;
    private final int colorError;
    private final int color500;
    private final int color400;
    private final int color300;

    public TransactionAdapter(Context context, TransactionListFragment.OnListFragmentInteractionListener listener) {
        this.listener = listener;
        this.context = context;
        colorDefault = ContextCompat.getColor(context, R.color.chuck_status_default);
        colorRequested = ContextCompat.getColor(context, R.color.chuck_status_requested);
        colorError = ContextCompat.getColor(context, R.color.chuck_status_error);
        color500 = ContextCompat.getColor(context, R.color.chuck_status_500);
        color400 = ContextCompat.getColor(context, R.color.chuck_status_400);
        color300 = ContextCompat.getColor(context, R.color.chuck_status_300);


        cursorAdapter = new CursorAdapter(TransactionAdapter.this.context, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chuck_list_item_transaction, parent, false);
                ViewHolder holder = new ViewHolder(itemView);
                itemView.setTag(holder);
                return itemView;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

            }

            private void setStatusColor(ViewHolder holder, HttpTransaction transaction) {
                int color;
                if (transaction.getStatus() == HttpTransaction.Status.Failed) {
                    color = colorError;
                } else if (transaction.getStatus() == HttpTransaction.Status.Requested) {
                    color = colorRequested;
                } else if (transaction.getResponseCode() >= 500) {
                    color = color500;
                } else if (transaction.getResponseCode() >= 400) {
                    color = color400;
                } else if (transaction.getResponseCode() >= 300) {
                    color = color300;
                } else {
                    color = colorDefault;
                }
                holder.code.setTextColor(color);
                holder.path.setTextColor(color);
            }
        };
    }

    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursorAdapter.getCursor().moveToPosition(position);
        cursorAdapter.bindView(holder.itemView, context, cursorAdapter.getCursor());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    public void swapCursor(Cursor newCursor) {
        cursorAdapter.swapCursor(newCursor);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView code;
        public final TextView path;
        public final TextView host;
        public final TextView start;
        public final TextView duration;
        public final TextView size;
        public final ImageView ssl;
        HttpTransaction transaction;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            code = (TextView) view.findViewById(R.id.code);
            path = (TextView) view.findViewById(R.id.path);
            host = (TextView) view.findViewById(R.id.host);
            start = (TextView) view.findViewById(R.id.start);
            duration = (TextView) view.findViewById(R.id.duration);
            size = (TextView) view.findViewById(R.id.size);
            ssl = (ImageView) view.findViewById(R.id.ssl);
        }
    }

}
