package com.example.photosandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.storage.DataStorage;

public class AlbumActivity extends AppCompatActivity {

    private PhotoManager photoManager;
    private Album album;
    private String albumName;

    private PhotoGridAdapter photoGridAdapter;

    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_album);

        TextView albumTitleTextView = findViewById(R.id.albumTitleTextView);
        Button addPhotoButton = findViewById(R.id.addPhotoButton);
        GridView photoGridView = findViewById(R.id.photoGridView);

        albumName = getIntent().getStringExtra("albumName");

        photoManager = DataStorage.loadData(this);
        album = photoManager.getAlbumByName(albumName);

        if (album == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        albumTitleTextView.setText(album.getName());

        photoGridAdapter = new PhotoGridAdapter();
        photoGridView.setAdapter(photoGridAdapter);

        setupPhotoPicker();

        addPhotoButton.setOnClickListener(v -> openPhotoPicker());

        photoGridView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AlbumActivity.this, PhotoDisplayActivity.class);
            intent.putExtra("albumName", album.getName());
            intent.putExtra("photoIndex", position);
            startActivity(intent);
        });

        photoGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            showRemovePhotoDialog(position);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoManager = DataStorage.loadData(this);
        album = photoManager.getAlbumByName(albumName);

        if (album != null && photoGridAdapter != null) {
            photoGridAdapter.notifyDataSetChanged();
        }
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();

                        if (uri != null) {
                            try {
                                final int takeFlags = result.getData().getFlags()
                                        & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            album.addPhoto(new Photo(uri.toString()));
                            DataStorage.saveData(this, photoManager);
                            photoGridAdapter.notifyDataSetChanged();

                            Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        photoPickerLauncher.launch(intent);
    }

    private void showRemovePhotoDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Photo")
                .setMessage("Remove this photo from the album?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    album.getPhotos().remove(position);
                    DataStorage.saveData(this, photoManager);
                    photoGridAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class PhotoGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return album.getPhotos().size();
        }

        @Override
        public Object getItem(int position) {
            return album.getPhotos().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(AlbumActivity.this);
                int size = parent.getWidth() / 3;
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            Photo photo = album.getPhotos().get(position);
            imageView.setImageURI(Uri.parse(photo.getUriString()));

            return imageView;
        }
    }
}