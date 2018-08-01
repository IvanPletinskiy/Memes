package com.handen.memes;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.handen.memes.database.Database;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class PostListFragment extends Fragment {
    PostDownloader<PostListFragment.ViewHolder> postDownloader;
    public CopyOnWriteArrayList<Post> items = new CopyOnWriteArrayList<>();

    private RecyclerView recyclerView;

    public PostListFragment() {

    }

    public static PostListFragment newInstance() {
        PostListFragment fragment = new PostListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler responseHandler = new Handler();
        postDownloader = new PostDownloader<>(responseHandler);
        postDownloader.setPostDownloaderListener(
                new PostDownloader.PostDownloaderListener<PostListFragment.ViewHolder>() {
                    @Override
                    public void onPostDownloaded(ViewHolder target, Post post) {
                        if(post != null) {
                            target.bindPost(post);
                        }
                        try {
                            items.set(target.getAdapterPosition(), post);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });

        postDownloader.start();
        postDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        if(view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            setupAdapter();
        }
        return view;
    }

    private void setupAdapter() {
        if(isAdded()) {
            items.add(null);
            PostAdapter adapter = new PostAdapter();
            adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                @Override
                public void onBottomReached(int position) {
                    items.add(null);
                    //     if(!recyclerView.isComputingLayout())
                    //        recyclerView.getAdapter().notifyDataSetChanged();
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        postDownloader.quit();
    }

    @Override
    public void onStop() {
        super.onStop();
        postDownloader.clearQueue();
    }

    public class PostAdapter extends RecyclerView.Adapter<ViewHolder> {
        OnBottomReachedListener onBottomReachedListener;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int p) {
            final int position = holder.getAdapterPosition();
            Drawable placeholder = holder.mView.getContext().getResources().getDrawable(R.drawable.placeholder);
            holder.imageView.setImageDrawable(placeholder);
            holder.likeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(items.size() > position && items.get(position) != null) {
                        if(!items.get(position).isLiked()) {
                            holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                            items.get(position).setLiked(true);
                            Database.saveLikedPost(items.get(position));
                        }
                        else {
                            if(items.get(position).isLiked()) {
                                holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border));
                                items.get(position).setLiked(false);
                                Database.deleteLikedPost(items.get(position));
                            }
                        }
                    }
                }
            });

            if(items.size() > position && items.get(position) != null) {
                holder.bindPost(items.get(position));
            }
            else {
                postDownloader.getPost(holder, position);
            }

            if(position == items.size() - 1 || items.size() == 0) {
                onBottomReachedListener.onBottomReached(position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
            this.onBottomReachedListener = onBottomReachedListener;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private ImageView imageView;
        private ImageView likeView;
        private TextView postText;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            postText = view.findViewById(R.id.postText);
            imageView = view.findViewById(R.id.image);
            likeView = view.findViewById(R.id.like);
        }

        public void bindPost(Post post) {
            imageView.setImageDrawable(new BitmapDrawable(getResources(), post.getImage()));
            if(post.getText().length() == 0) {
                postText.setVisibility(View.GONE);
            }
            else {
                postText.setText(post.getText());
            }
            ArrayList<Integer> likedIds = Database.getLikedPostsIds();
            if(likedIds.size() > 0) {
                if(likedIds.contains(post.getId())) {
                    likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                }
            }
        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}
