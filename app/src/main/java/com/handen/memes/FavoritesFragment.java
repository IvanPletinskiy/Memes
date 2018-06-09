package com.handen.memes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.handen.memes.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavoritesFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoritesFragment() {
    }

    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_list, container, false);

        if(view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            ArrayList<Post> posts = Database.getLikedPosts();
            Collections.sort(posts, new Comparator<Post>() {
                @Override
                public int compare(Post p1, Post p2) {
                    long date1 = p1.getPostMillis();
                    long date2 = p2.getPostMillis();
                    if(date1 < date2)
                        return -1;
                    else
                        return 1;

                }
            });
            Collections.reverse(posts);
            recyclerView.setAdapter(new FavoritesAdapter(posts));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public static class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
        private ArrayList<Post> mValues;


        public FavoritesAdapter(ArrayList<Post> likedPosts) {
            mValues = likedPosts;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_favorite, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int p) {
            final int position = holder.getAdapterPosition();
            Post post = mValues.get(position);

            final Resources resources = holder.mImageView.getResources();
            holder.mImageView.setImageDrawable(new BitmapDrawable(holder.mImageView.getResources(), post.getImage()));
            if(post.isLiked())
                holder.mLikeView.setImageDrawable(resources.getDrawable(R.drawable.ic_favorite));
            else
                holder.mLikeView.setImageDrawable(resources.getDrawable(R.drawable.ic_favorite_border));
            holder.mLikeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mValues.size() > position && mValues.get(position) != null) {
                        if(!mValues.get(position).isLiked()) {
                            holder.mLikeView.setImageDrawable(resources.getDrawable(R.drawable.ic_favorite));
                            mValues.get(position).setLiked(true);
                            Database.saveLikedPost(mValues.get(position));
                            //   notifyItemChanged(position);
                        }
                        else {
                            if(mValues.get(position).isLiked()) {
                                holder.mLikeView.setImageDrawable(resources.getDrawable(R.drawable.ic_favorite_border));
                                mValues.get(position).setLiked(false);
                                Database.deleteLikedPost(mValues.get(position));
                            }
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView, mLikeView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.image);
                mLikeView = view.findViewById(R.id.like);
            }
        }
    }
}
