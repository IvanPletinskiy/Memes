package com.handen.memes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PostListFragment extends Fragment {
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
        new FetchItemsTask(pageCount++).execute(); //TODO Перенести в onCreate
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
            PostAdapter adapter = new PostAdapter();
            adapter.setOnBottomReachedListener(new OnBottomReachedListener() {
                @Override
                public void onBottomReached(int position) {
                    new FetchItemsTask(pageCount++).execute();
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public class PostAdapter extends RecyclerView.Adapter<ViewHolder> implements OnBottomReachedListener{
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
                    /*

                        holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite));
                        items.get(position).setLiked(true);
                        Database.saveLikedPost(items.get(position));

                        if(items.get(position).isLiked()) {
                                holder.likeView.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border));
                                items.get(position).setLiked(false);
                                Database.deleteLikedPost(items.get(position));
                        }
                    }
                    */
                }
            });

            if (position == items.size() - 1) {
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

        @Override
        public void onBottomReached(int position) {
            new FetchItemsTask(pageCount++).execute();
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
    }

    public interface OnBottomReachedListener {
        void onBottomReached(int position);
    }

    public class FetchItemsTask extends AsyncTask<Void, Void, List<Item>> {
        int pageNumber;

        public FetchItemsTask(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {
            Looper.prepare();
            PostFetcher postFetcher = new PostFetcher(pageCount++, new PostFetcher.OnPostsSelectedListener(){
                @Override
                public void onPostSelected(ArrayList<Item> items) {
                    onPostExecute(items);
                }
            });
            postFetcher.fetchItems();
        /*    while(!postFetcher.isDone()) {
                try {
                    Thread.sleep(100);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Item> result) {
            items.addAll(result);
            if(result.size() > 0)
                recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}