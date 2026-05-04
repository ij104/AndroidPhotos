package com.example.photosandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.PhotoManager;

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.Holder> {

    public interface Listener {
        void onAlbumClick(Album album);

        void onAlbumLongClick(Album album);
    }

    private final Listener listener;
    private PhotoManager photoManager;

    public AlbumRecyclerAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setPhotoManager(PhotoManager photoManager) {
        this.photoManager = photoManager;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return photoManager == null ? 0 : photoManager.getAlbums().size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album_row, parent, false);
        return new Holder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Album album = photoManager.getAlbums().get(position);
        holder.nameTextView.setText(album.getName());
        holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onAlbumLongClick(album);
            return true;
        });
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView nameTextView;

        Holder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.albumNameTextView);
        }
    }
}
