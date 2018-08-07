package com.handen.memes;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.util.List;

public class PostListFragment extends Fragment implements PostFetcher.OnPostsSelectedListener, ImageDownloader.ImageDownloadListener<PostListFragment.ViewHolder> {
    private ImageDownloader<PostListFragment.ViewHolder> imageDownloader;
    private RecyclerView recyclerView;

    private List<Item> items = new ArrayList<>();
    private int pageCount = 0;

    public PostListFragment() {

    }

    public static PostListFragment newInstance() {

        return new PostListFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Handler responseHandler = new Handler();
        imageDownloader = new ImageDownloader<>(responseHandler);
        imageDownloader.setImageDownloadListener(PostListFragment.this);

        imageDownloader.start();
        imageDownloader.getLooper();

        new PostFetcher(pageCount++, PostListFragment.this).fetchItems();
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
            PostAdapter adapter = new PostAdapter(this);
            adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                @Override
                public void onBottomReached(int position) {
                    new PostFetcher(pageCount++, PostListFragment.this).fetchItems();
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageDownloader.quit();
    }

    @Override
    public void onStop() {
        super.onStop();
        imageDownloader.clearQueue();
    }

    @Override
    public void onPostSelected(ArrayList<Item> mItems) {
        items.addAll(mItems);
        if(mItems.size() > 0) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onImageDownloaded(ViewHolder target, Bitmap image) {
        target.bindImage(image);
        recyclerView.getAdapter().notifyItemChanged(target.getAdapterPosition());
    }

    class PostAdapter extends RecyclerView.Adapter<PostListFragment.ViewHolder> implements PostListFragment.OnBottomReachedListener {
        private PostListFragment mPostListFragment;
        PostListFragment.OnBottomReachedListener onBottomReachedListener;

        public PostAdapter(PostListFragment postListFragment) {
            mPostListFragment = postListFragment;
        }

        @Override
        public PostListFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_post, parent, false);
            return new PostListFragment.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PostListFragment.ViewHolder holder, final int p) {
            final int position = holder.getAdapterPosition();
            Item item = mPostListFragment.items.get(position);
            Drawable placeholder = holder.mView.getContext().getResources().getDrawable(R.drawable.placeholder);
            holder.imageView.setImageDrawable(placeholder);
            holder.postText.setText(item.getText());

            if(Database.containsImage(item.getUrl())) {
                //Показываем картинку
            }
            else {
                //Отправляем запрос на скачивание
                mPostListFragment.imageDownloader.addRequest(holder, item.getUrl());
            }

            if(position == mPostListFragment.items.size() - 1) {
                onBottomReachedListener.onBottomReached(position);
            }
        }

        @Override
        public int getItemCount() {
            return mPostListFragment.items.size();
        }

        public void setOnBottomReachedListener(PostListFragment.OnBottomReachedListener onBottomReachedListener) {
            this.onBottomReachedListener = onBottomReachedListener;
        }

        @Override
        public void onBottomReached(int position) {
            new PostFetcher(mPostListFragment.pageCount++, mPostListFragment).fetchItems();
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

        public void bindImage(Bitmap bitmap) {
            imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }
}