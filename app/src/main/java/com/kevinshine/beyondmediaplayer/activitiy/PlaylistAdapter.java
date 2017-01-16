package com.kevinshine.beyondmediaplayer.activitiy;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kevinshine.beyondmediaplayer.R;
import com.kevinshine.beyondmediaplayer.ThumbHelper;
import com.kevinshine.beyondmediaplayer.model.bean.PlaylistItemBean;
import com.kevinshine.beyondmediaplayer.utils.CacheUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gary on 16-2-16.
 */
class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private List<PlaylistItemBean> mDataset;
    private ThumbHelper mThumbHelper;
    private Context mContext;
    private CacheUtil mCacheUtil;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlaylistAdapter(Context context) {
        mThumbHelper = new ThumbHelper(context);
        mContext = context;
        mCacheUtil = new CacheUtil(context);
    }

    // Provide a reference to the type of views that you are using
    // (custom viewholder)
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        PlaylistItemBean mData;
        ImageView mThumbImage;

        public ViewHolder(View v) {
            super(v);

            mTextView = (TextView) v.findViewById(R.id.tv_first_line);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Play media file
                    VideoActivity.intentTo(mContext, mData.getUri(), "Video", getAdapterPosition());
                }
            });

            mThumbImage = (ImageView) v.findViewById(R.id.iv_media_thumbnail);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaylistItemBean bean = mDataset.get(position);

        holder.mThumbImage.setImageBitmap(null);

        new ThumbTask(mThumbHelper, holder.mThumbImage).execute(bean.getUri());

        holder.mTextView.setText(bean.getTitle());
        holder.mData = bean;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mDataset == null)
            return 0;

        return mDataset.size();
    }

    public void reset() {
        if (mDataset != null)
            mDataset.clear();
    }

    public void setList(ArrayList<PlaylistItemBean> list){
        mDataset = list;
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    private class ThumbTask extends AsyncTask<String, Void, Bitmap> {
        private ThumbHelper mThumbHelper;
        private ImageView mImageView;

        public ThumbTask(ThumbHelper helper, ImageView imageView) {
            mThumbHelper = helper;
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap thumbBitmap = null;
            if (params != null && params.length == 1) {
                String path = params[0];
                try {
                    if (mCacheUtil.isExist(path)) {
                        thumbBitmap = mCacheUtil.getBitmapFromDisk(path);
                    } else {
                        thumbBitmap = mThumbHelper.createThumbImage(path);

                        // Write bitmap to disk
                        mCacheUtil.writeToDisk(path, thumbBitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return thumbBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null){
                mImageView.setImageResource(R.mipmap.ic_media);
            }else {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}


