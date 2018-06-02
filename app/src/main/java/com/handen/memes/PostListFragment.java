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

import java.util.ArrayList;

public class PostListFragment extends Fragment {

    PostDownloader<PostAdapter.ViewHolder> postDownloader;
    public ArrayList<Post> items = new ArrayList<>();

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
                new PostDownloader.PostDownloaderListener<PostAdapter.ViewHolder>() {
                    @Override
                    public void onPostDownloaded(PostAdapter.ViewHolder target, Post post) {
                        target.bindDrawable(new BitmapDrawable(getResources(), post.getImage()));
                        //items.add(target.getAdapterPosition(), post);
                        items.add(post);
                    }
                });

        postDownloader.start();
        postDownloader.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new PostAdapter());
        }
        return view;
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

    public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
        public PostAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Drawable placeholder = holder.mView.getContext().getResources().getDrawable(R.drawable.ic_launcher_background);
            holder.imageView.setImageDrawable(placeholder);
            ///Drawable placeholder = holder.mView.getContext().getResources().getDrawable(R.drawable.ic_launcher_background);

            if(items.size() > position && items.get(position) != null) {
                holder.bindDrawable(new BitmapDrawable(getResources(), items.get(position).getImage()));
            }
            else {
                postDownloader.getPost(holder, position);
            }
        }

        @Override
        public int getItemCount() {
            return 25;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                imageView = view.findViewById(R.id.image);
            }

            public void bindDrawable(Drawable drawable) {
                imageView.setImageDrawable(drawable);
                notifyDataSetChanged();
            }
        }
    }
}
