package com.handen.memes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.handen.memes.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class PostListFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    PostDownloader<PostAdapter.ViewHolder> postDownloader;

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
                    public void onPostDownloaded(PostAdapter.ViewHolder target, Bitmap bitmap) {
                        target.bindDrawable(new BitmapDrawable(getResources(), bitmap));
                    }
                });

 //       postDownloader = new PostDownloader<>();
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
      //      Bitmap bitmap = PostDownloader.addToQueue();
       //     try {
       //         Thread.sleep(550);
     //       }
     //       catch (InterruptedException e) {
     //           e.printStackTrace();
     //       }
            recyclerView.setAdapter(new PostAdapter(postDownloader, mListener));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
        else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DummyItem item);
    }
}
