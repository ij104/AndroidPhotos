package com.example.photosandroid;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.model.Tag;
import com.example.photosandroid.storage.DataStorage;

import java.util.ArrayList;

public class PhotoDisplayActivity extends AppCompatActivity {

    private PhotoManager photoManager;
    private Album album;
    private int photoIndex;

    private TextView photoTitleTextView;
    private ImageView fullPhotoImageView;
    private Button backFromPhotoButton;
    private TextView tagsSummaryTextView;
    private Button addPersonTagButton;
    private Button addLocationTagButton;
    private Button removeTagButton;
    private Button previousPhotoButton;
    private Button nextPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo_display);

        int contentPad = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photoDisplayMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + contentPad,
                    systemBars.top + contentPad,
                    systemBars.right + contentPad,
                    systemBars.bottom + contentPad
            );
            return insets;
        });

        photoTitleTextView = findViewById(R.id.photoTitleTextView);
        fullPhotoImageView = findViewById(R.id.fullPhotoImageView);
        backFromPhotoButton = findViewById(R.id.backFromPhotoButton);
        tagsSummaryTextView = findViewById(R.id.tagsSummaryTextView);
        addPersonTagButton = findViewById(R.id.addPersonTagButton);
        addLocationTagButton = findViewById(R.id.addLocationTagButton);
        removeTagButton = findViewById(R.id.removeTagButton);
        previousPhotoButton = findViewById(R.id.previousPhotoButton);
        nextPhotoButton = findViewById(R.id.nextPhotoButton);

        backFromPhotoButton.setOnClickListener(v -> finish());

        addPersonTagButton.setOnClickListener(v -> showAddTagDialog("person", "Add person tag"));
        addLocationTagButton.setOnClickListener(v -> showAddTagDialog("location", "Add location tag"));
        removeTagButton.setOnClickListener(v -> showRemoveTagDialog());

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

    private Photo getCurrentPhoto() {
        return album.getPhotos().get(photoIndex);
    }

    private void showAddTagDialog(final String tagType, String title) {
        EditText input = new EditText(this);
        input.setHint("person".equals(tagType) ? "Person name" : "Place or location");

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(this, "Tag cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    getCurrentPhoto().addTag(new Tag(tagType, value));
                    DataStorage.saveData(this, photoManager);
                    refreshTagsUi();
                    Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveTagDialog() {
        Photo photo = getCurrentPhoto();
        ArrayList<Tag> tags = photo.getTags();
        if (tags.isEmpty()) {
            Toast.makeText(this, "No tags to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            String type = t.getType();
            String typeLabel = type.substring(0, 1).toUpperCase() + type.substring(1);
            items[i] = typeLabel + ": " + t.getValue();
        }

        new AlertDialog.Builder(this)
                .setTitle("Remove tag")
                .setItems(items, (dialog, which) -> {
                    tags.remove(which);
                    DataStorage.saveData(this, photoManager);
                    refreshTagsUi();
                    Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshTagsUi() {
        Photo photo = getCurrentPhoto();
        if (photo.getTags().isEmpty()) {
            tagsSummaryTextView.setText(
                    "No tags yet. Tap Add person or Add location below.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Tag t : photo.getTags()) {
            String type = t.getType();
            String typeLabel = type.substring(0, 1).toUpperCase() + type.substring(1);
            sb.append(typeLabel).append(": ").append(t.getValue()).append('\n');
        }
        tagsSummaryTextView.setText(sb.toString().trim());
    }

    private void showCurrentPhoto() {
        Photo photo = album.getPhotos().get(photoIndex);
        fullPhotoImageView.setImageURI(Uri.parse(photo.getUriString()));

        String title = "Photo " + (photoIndex + 1) + " of " + album.getPhotos().size();
        photoTitleTextView.setText(title);
        refreshTagsUi();
    }
}