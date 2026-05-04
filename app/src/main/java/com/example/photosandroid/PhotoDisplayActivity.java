package com.example.photosandroid;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.storage.DataStorage;

public class PhotoDisplayActivity extends AppCompatActivity {

    private PhotoManager photoManager;
    private Album album;
    private int photoIndex;

    private TextView photoTitleTextView;
    private ImageView fullPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo_display);

        photoTitleTextView = findViewById(R.id.photoTitleTextView);
        fullPhotoImageView = findViewById(R.id.fullPhotoImageView);
        Button previousPhotoButton = findViewById(R.id.previousPhotoButton);
        Button nextPhotoButton = findViewById(R.id.nextPhotoButton);

        String albumName = getIntent().getStringExtra("albumName");
        photoIndex = getIntent().getIntExtra("photoIndex", 0);

        photoManager = DataStorage.loadData(this);
        album = photoManager.getAlbumByName(albumName);

        if (album == null || album.getPhotos().isEmpty()) {
            Toast.makeText(this, "Photo not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (photoIndex < 0 || photoIndex >= album.getPhotos().size()) {
            photoIndex = 0;
        }

        showCurrentPhoto();

        previousPhotoButton.setOnClickListener(v -> {
            if (photoIndex > 0) {
                photoIndex--;
                showCurrentPhoto();
            } else {
                Toast.makeText(this, "Already at first photo", Toast.LENGTH_SHORT).show();
            }
        });

        nextPhotoButton.setOnClickListener(v -> {
            if (photoIndex < album.getPhotos().size() - 1) {
                photoIndex++;
                showCurrentPhoto();
            } else {
                Toast.makeText(this, "Already at last photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCurrentPhoto() {
        Photo photo = album.getPhotos().get(photoIndex);
        fullPhotoImageView.setImageURI(Uri.parse(photo.getUriString()));

        String title = "Photo " + (photoIndex + 1) + " of " + album.getPhotos().size();
        photoTitleTextView.setText(title);
    }
}