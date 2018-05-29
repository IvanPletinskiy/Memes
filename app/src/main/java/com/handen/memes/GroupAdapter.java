package com.handen.memes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.handen.memes.GroupListFragment.OnListFragmentInteractionListener;
import com.handen.memes.dummy.DummyContent.DummyItem;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final ArrayList<Group> mValues;
    private final OnListFragmentInteractionListener mListener;

    public GroupAdapter(ArrayList<Group> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Group group = mValues.get(position);
        holder.checkBox.setChecked(group.getIsSelected());
        holder.textView.setText(group.getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    //TODO сделать листенер по нажатию
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        private CheckBox checkBox;
        private TextView textView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            checkBox = view.findViewById(R.id.checkbox);
            textView = view.findViewById(R.id.name);
        }
    }
}
