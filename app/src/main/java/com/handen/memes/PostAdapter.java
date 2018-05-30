package com.handen.memes;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.handen.memes.PostListFragment.OnListFragmentInteractionListener;
import com.handen.memes.dummy.DummyContent.DummyItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    PostDownloader<PostAdapter.ViewHolder> mPostDownloader;

    Bitmap mBitmap;

    public PostAdapter(PostDownloader<PostAdapter.ViewHolder> postDownloader, OnListFragmentInteractionListener listener) {
        mPostDownloader = postDownloader;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_post, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Drawable placeholder = holder.mView.getContext().getResources().getDrawable(R.drawable.ic_launcher_background);
        holder.bindDrawable(placeholder);

        mPostDownloader.addToQueue(holder, position);
       // mPostDownloader.addToQueue(holder);
   //     Bitmap bitmap = PostDownloader.downloadImage("https://pp.userapi.com/c834400/v834400925/150fde/iLhIi5RgdFw.jpg");

/*        try {
            Thread.sleep(250);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
*/
 //       holder.imageView.setImageBitmap(bitmap);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                }
            }
        });
    }

    @Override
    public int getItemCount() {
       return  1; //TODO ВНИМАНИЕ!
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
        }
    }
}
