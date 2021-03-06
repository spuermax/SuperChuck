package com.developers.super_chuck.internal.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.developers.super_chuck.R;
import com.developers.super_chuck.internal.data.ChuckContentProvider;
import com.developers.super_chuck.internal.data.HttpTransaction;
import com.developers.super_chuck.internal.helper.NotificationHelper;
import com.developers.super_chuck.internal.helper.SQLiteUtils;
import com.developers.super_chuck.internal.view.adapter.TransactionAdapter;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:21
 * @Description
 */
public class TransactionListFragment extends Fragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private String currentFilter;
    private OnListFragmentInteractionListener listener;
    private TransactionAdapter adapter;


    public static TransactionListFragment newInstance() {
        TransactionListFragment fragment = new TransactionListFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chuck_fragment_transaction_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));
            adapter = new TransactionAdapter(getContext(), listener);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chuck_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            getContext().getContentResolver().delete(ChuckContentProvider.TRANSACTION_URI, null, null);
            NotificationHelper.clearBuffer();
            return true;
        } else if (item.getItemId() == R.id.browse_sql) {
            SQLiteUtils.browseDatabase(getContext());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }




    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = new CursorLoader(getContext());
        loader.setUri(ChuckContentProvider.TRANSACTION_URI);
        if (!TextUtils.isEmpty(currentFilter)) {
            if (TextUtils.isDigitsOnly(currentFilter)) {
                loader.setSelection("responseCode LIKE ?");
                loader.setSelectionArgs(new String[]{ currentFilter + "%" });
            } else {
                loader.setSelection("path LIKE ?");
                loader.setSelectionArgs(new String[]{ "%" + currentFilter + "%" });
            }
        }
        loader.setProjection(HttpTransaction.PARTIAL_PROJECTION);
        loader.setSortOrder("requestDate DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        currentFilter = newText;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(HttpTransaction transaction);
    }
}
