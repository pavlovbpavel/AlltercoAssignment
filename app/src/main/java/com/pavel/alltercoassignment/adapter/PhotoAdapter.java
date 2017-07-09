package com.pavel.alltercoassignment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pavel.alltercoassignment.R;
import com.pavel.alltercoassignment.adapter.PhotoAdapter.PhotoHolder;
import com.pavel.alltercoassignment.model.BitmapWrapper;

import java.util.ArrayList;

/**
 * Created by Pavel Pavlov on 5/19/2017.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

    private ArrayList<BitmapWrapper> photos;
    private PhotoDeleteListener listener;

    public PhotoAdapter(ArrayList<BitmapWrapper> photos) {
        this.photos = photos;
    }

    public void setListener(PhotoDeleteListener listener) {
        this.listener = listener;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoHolder(parent.inflate(parent.getContext(), R.layout.row_photo, null));
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, final int position) {
        final BitmapWrapper bitmapWrapper = photos.get(position);
        holder.photo.setImageBitmap(bitmapWrapper.getBitmap());
        holder.photo.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onDelete(bitmapWrapper.getFileName(), position);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView photo;

        PhotoHolder(View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.row_photos_item);
        }
    }

    public interface PhotoDeleteListener {
        void onDelete(String fileName, int position);
    }
}
