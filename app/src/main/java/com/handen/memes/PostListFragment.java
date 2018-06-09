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

import com.handen.memes.database.Database;

import java.util.ArrayList;

public class PostListFragment extends Fragment {

    PostDownloader<PostListFragment.ViewHolder> postDownloader;
    public ArrayList<Post> items = new ArrayList<>(10);

    private RecyclerView recyclerView;

    private int postCount = 5;

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
                        if(post != null)
                            target.bindDrawable(new BitmapDrawable(getResources(), post.getImage()));
                        //items.add(target.getAdapterPosition(), post);
                        items.add(post);
                        recyclerView.getAdapter().notifyItemChanged(items.size() - 1);
                    }
                });

        postDownloader.start();
        postDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            setupAdapter();
        }
        return view;
    }

    private void setupAdapter() {
        if (isAdded()) {
            PostAdapter adapter = new PostAdapter();

            adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                @Override
                public void onBottomReached(int position) {

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

    public class PostAdapter extends RecyclerView.Adapter<ViewHolder> implements OnBottomReachedListener {
        OnBottomReachedListener onBottomReachedListener;

        public PostAdapter() {
        }

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
                         //   notifyItemChanged(position);
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
            ArrayList<Integer> likedIds = Database.getLikedPostsIds();
     //       if(likedIds.size() > 0)
               // if(likedIds.contains(items.get(holder.getAdapterPosition()).getId()))
            //if(items.size() > !holder.getAdapterPosition() && items.get(holder.getAdapterPosition()).isLiked())
      //              holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));

            if(items.size() > position && items.get(position) != null) {
                holder.bindDrawable(new BitmapDrawable(getResources(), items.get(position).getImage()));
                if(likedIds.size() > 0)
                    holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
            }
            else {
                postDownloader.getPost(holder, position);
            }

            if (position == items.size() - 1 || items.size() == 0) {
                onBottomReachedListener.onBottomReached(position);
                //                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return postCount;
        }

        public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
            this.onBottomReachedListener = onBottomReachedListener;
        }

        @Override
        public void onBottomReached(int position) {
            postCount++;
            postDownloader.getPost((ViewHolder) recyclerView.findViewHolderForAdapterPosition(position), position);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public ImageView imageView;
        public ImageView likeView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.image);
            likeView = view.findViewById(R.id.like);
        }

        public void bindDrawable(Drawable drawable) {
            imageView.setImageDrawable(drawable);
            //                notifyDataSetChanged();
        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}
